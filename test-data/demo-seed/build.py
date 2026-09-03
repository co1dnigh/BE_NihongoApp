# -*- coding: utf-8 -*-
"""
Sinh toan bo du lieu demo cho BE_NihongoApp.

Dau vao : data_kana.py, data_topics_a.py, data_topics_b.py
Dau ra  : out/seed.sql          - script SQL nap thang vao MySQL
          out/media.json        - danh sach file audio/anh can tao (make_media.py doc file nay)

Chay: python build.py
"""
import json
import os
import random
import re
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from data_kana import HIRAGANA_LESSONS, KATAKANA_LESSONS      # noqa: E402,F401
from data_topics_a import TOPICS_A                            # noqa: E402
from data_topics_b import TOPICS_B                            # noqa: E402
from data_topics_c import TOPICS_C                            # noqa: E402
from romaji import kana_to_romaji                             # noqa: E402

random.seed(20260823)  # cố định để chạy lại ra kết quả giống hệt

OUT_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "out")
AUDIO_URL_PREFIX = "/uploads/audios"
IMAGE_URL_PREFIX = "/uploads/images"

# ===========================================================================
# Media registry — mỗi từ/câu chỉ tạo 1 file, dùng lại ở mọi bài
# ===========================================================================
audio_registry = {}   # text tiếng Nhật -> {"url":..., "path":..., "text":..., "voice":...}
image_registry = {}   # slug            -> {"url":..., "path":..., "emoji":..., "label":...}
_used_audio_slugs = set()
_used_image_slugs = set()


def slugify(text, fallback="item"):
    s = re.sub(r"[^a-zA-Z0-9]+", "-", (text or "").strip().lower()).strip("-")
    return s or fallback


def _unique(slug, used):
    base, i = slug, 2
    while slug in used:
        slug = "%s-%d" % (base, i)
        i += 1
    used.add(slug)
    return slug


def audio_for(jp_text, romaji_hint="", folder="words", voice="ja-JP-NanamiNeural"):
    """Đăng ký 1 file audio TTS cho đoạn tiếng Nhật, trả về URL phục vụ tĩnh."""
    if not jp_text:
        return None
    key = (folder, jp_text, voice)
    if key in audio_registry:
        return audio_registry[key]["url"]
    slug = _unique(slugify(romaji_hint or jp_text, "audio"), _used_audio_slugs)
    rel = "%s/%s.mp3" % (folder, slug)
    audio_registry[key] = {
        "url": "%s/%s" % (AUDIO_URL_PREFIX, rel),
        "rel": rel,
        "text": jp_text,
        "voice": voice,
    }
    return audio_registry[key]["url"]


def image_for(emoji, romaji_hint, label=""):
    """Đăng ký 1 ảnh minh hoạ (emoji vector trên nền gradient), trả về URL."""
    if not emoji:
        return None
    key = emoji + "|" + romaji_hint
    if key in image_registry:
        return image_registry[key]["url"]
    slug = _unique(slugify(romaji_hint, "img"), _used_image_slugs)
    rel = "vocab/%s.png" % slug
    image_registry[key] = {
        "url": "%s/%s" % (IMAGE_URL_PREFIX, rel),
        "rel": rel,
        "emoji": emoji,
        "label": label,
    }
    return image_registry[key]["url"]


# ===========================================================================
# Romaji & bảng tra nghĩa
# ===========================================================================
# Người học chưa biết một chữ tiếng Nhật nào, nên MỌI mẩu chữ Nhật hiện trên màn
# hình đều phải kèm romaji. Từ vựng và câu mẫu đã có romaji gõ tay trong file dữ
# liệu, nhưng đáp án nhiễu và các thẻ rời của câu sắp xếp thì không — phần đó để
# `kana_to_romaji` lo. Ưu tiên bản gõ tay vì nó xử lý đúng chỗ trợ từ đọc lệch.
JAPANESE_CHARS = re.compile(r"[぀-ヿ一-鿿]")
ROMAJI_LOOKUP = {}   # kana -> romaji (gõ tay)
GLOSSARY = {}        # kana -> {"r": romaji, "v": nghĩa tiếng Việt}


def register_vocab(vocab_list):
    for v in vocab_list:
        ROMAJI_LOOKUP.setdefault(v["kana"], v["romaji"])
        GLOSSARY.setdefault(v["kana"], {"r": v["romaji"], "v": v["vn"]})


def is_japanese(text):
    return bool(text) and bool(JAPANESE_CHARS.search(text))


def romaji_of(text):
    """Romaji của một mẩu chữ Nhật bất kỳ, hoặc None nếu đó không phải chữ Nhật."""
    if not is_japanese(text):
        return None
    t = text.strip()
    return ROMAJI_LOOKUP.get(t) or (kana_to_romaji(t) or None)


def glossary_for(*texts):
    """Từ điển mini kèm theo mỗi câu hỏi, cho tính năng chạm giữ vào chữ để tra nghĩa.

    Chỉ gồm những từ THỰC SỰ xuất hiện trong đề bài và đáp án của chính câu đó,
    nên payload nhỏ và không bao giờ lộ từ của câu khác.
    """
    joined = " ".join(t for t in texts if t)
    if not joined:
        return None
    out = {}
    for kana, info in GLOSSARY.items():
        if kana in joined:
            out[kana] = info
    return out or None


def merge_gloss(meta, *texts):
    gloss = glossary_for(*texts)
    if not gloss:
        return meta
    meta = dict(meta or {})
    meta["glossary"] = gloss
    return meta


# ===========================================================================
# Bộ đệm SQL
# ===========================================================================
class Seed:
    def __init__(self):
        self.topics = []
        self.lessons = []
        self.questions = []
        self.options = []

    def topic(self, title, desc, order_index):
        tid = len(self.topics) + 1
        self.topics.append((tid, title, desc, order_index))
        return tid

    def lesson(self, topic_id, title, order_index, lesson_type, config):
        lid = len(self.lessons) + 1
        self.lessons.append((lid, topic_id, title, order_index, lesson_type, config))
        return lid

    def question(self, lesson_id, qtype, text, audio=None, image=None, meta=None):
        qid = len(self.questions) + 1
        self.questions.append((qid, lesson_id, qtype, text, audio, image, meta))
        return qid

    def option(self, question_id, text, correct, order_index, image=None, audio=None, meta=None):
        oid = len(self.options) + 1
        self.options.append((oid, question_id, text, image, audio, correct, order_index, meta))
        return oid


S = Seed()


def add_mcq(lesson_id, qtype, text, correct, distractors, audio=None, image=None,
            meta=None, opt_images=None, opt_audios=None):
    """Thêm 1 câu trắc nghiệm: 1 đáp án đúng + tối đa 3 đáp án nhiễu, đã trộn sẵn.

    Với câu chọn hình (SELECT_IMAGE) thì thẻ đáp án CHỈ có ảnh — nhãn tiếng Việt được
    giấu vào metadata để không lộ đáp án ngay trên màn hình.
    """
    pool = [d for d in dict.fromkeys(distractors) if d and d != correct][:3]
    items = [(correct, True)] + [(d, False) for d in pool]
    random.shuffle(items)
    labels = [lbl for lbl, _ in items]
    qid = S.question(lesson_id, qtype, text, audio, image,
                     merge_gloss(meta, text, *labels))
    image_only = qtype == "SELECT_IMAGE"
    for i, (label, is_correct) in enumerate(items, start=1):
        if image_only:
            # Thẻ đáp án CHỈ có ảnh; nhãn tiếng Việt giấu vào metadata để không lộ đáp án.
            opt_meta = {"label": label}
        else:
            # Đáp án viết bằng chữ Nhật thì kèm romaji để người mới đọc được.
            opt_meta = {"romaji": romaji_of(label)} if is_japanese(label) else None
        S.option(qid, None if image_only else label, is_correct, i,
                 image=(opt_images or {}).get(label),
                 audio=(opt_audios or {}).get(label),
                 meta=opt_meta)
    return qid


def add_arrange(lesson_id, sentence, audio, image=None):
    """Câu sắp xếp: mỗi block là 1 option, order_index = vị trí đúng trong câu.

    Ảnh minh hoạ ở đây an toàn: đáp án là THỨ TỰ các thẻ chữ, hình không hề gợi ý
    thứ tự đó — nó chỉ giúp người học hình dung nội dung câu.
    """
    qid = S.question(lesson_id, "LISTEN_AND_ARRANGE",
                     "Nghe và sắp xếp thành câu hoàn chỉnh: %s" % sentence["vn"],
                     audio=audio, image=image,
                     meta=merge_gloss({"romaji": sentence["romaji"], "vn": sentence["vn"],
                                       "jp": sentence["jp"]}, sentence["jp"]))
    for i, block in enumerate(sentence["blocks"], start=1):
        r = romaji_of(block)
        S.option(qid, block, True, i, meta={"romaji": r} if r else None)
    return qid


def add_speaking(lesson_id, jp, romaji, vn, audio, image=None):
    qid = S.question(lesson_id, "SPEAKING",
                     "Nhấn vào micro và đọc to câu sau: %s" % jp,
                     audio=audio, image=image,
                     meta=merge_gloss({"romaji": romaji, "vn": vn, "jp": jp}, jp))
    S.option(qid, jp, True, 1, audio=audio, meta={"romaji": romaji})
    return qid


def illustration_for(jp_text, vocab_pool):
    """Ảnh minh hoạ cho một câu: lấy theo danh từ cụ thể dài nhất xuất hiện trong câu."""
    best = None
    for v in vocab_pool:
        if not v.get("emoji") or len(v["kana"]) < 2:
            continue
        if v["kana"] in jp_text and (best is None or len(v["kana"]) > len(best["kana"])):
            best = v
    if best is None:
        return None
    return image_for(best.get("emoji"), best["romaji"], best["vn"])


def sample_other(pool, exclude, k):
    """Lấy k phần tử khác `exclude`. Phần đầu `pool` là từ đã học nên được ưu tiên."""
    cands, seen = [], set(exclude)
    for x in pool:
        if x not in seen:
            seen.add(x)
            cands.append(x)
    head = cands[:max(k * 3, 8)]
    random.shuffle(head)
    return head[:k]


# ===========================================================================
# 1) TOPIC BẢNG CHỮ CÁI
# ===========================================================================
KANA_LESSON_CONFIG = {"questionsPerSession": 10, "entryCostEnergy": 5, "expReward": 15,
                      "replayExpRatio": 0.3}
VOCAB_LESSON_CONFIG = {"questionsPerSession": 10, "entryCostEnergy": 10, "expReward": 20,
                       "replayExpRatio": 0.3}
REVIEW_CONFIG = {"questionsPerSession": 12, "entryCostEnergy": 5, "expReward": 30,
                 "starThresholds": [90, 150], "replayExpRatio": 0.3}
JUMP_CONFIG = {"questionsPerSession": 15, "entryCostEnergy": 15, "expReward": 80}

# Tu topic thu may (order 1-based trong lo trinh) tro di thi moi tao bai TOPIC_REVIEW.
# 2 topic dau khong can on tap xuyen topic vi chua co "topic cu" nao du de on.
TOPIC_REVIEW_FROM_ORDER = 3

# tất cả câu hỏi của 1 topic, để dựng bài ôn tập / thi vượt cấp cuối topic
topic_question_bank = {}


def register_bank(topic_id, qid):
    topic_question_bank.setdefault(topic_id, []).append(qid)


def build_kana_topic(title, desc, order_index, lessons_data, kana_type):
    tid = S.topic(title, desc, order_index)
    all_chars = [c for l in lessons_data for c in l["chars"]]
    all_romaji = [r for _, r in all_chars]
    all_symbols = [s for s, _ in all_chars]

    learned_words = []
    learned_chars = []          # chỉ dùng chữ đã học làm đáp án nhiễu — không lộ chữ của bài sau
    for idx, ldata in enumerate(lessons_data, start=1):
        cfg = dict(KANA_LESSON_CONFIG)
        cfg["description"] = ldata["desc"]
        lid = S.lesson(tid, ldata["title"], idx, "NORMAL", cfg)
        chars = ldata["chars"]
        learned_chars.extend(chars)
        pool_romaji = [r for _, r in learned_chars]
        pool_symbols = [sym for sym, _ in learned_chars]
        # bài đầu tiên chỉ có 5 chữ nên lấy thêm vài chữ của bảng làm nhiễu cho đủ 4 lựa chọn
        if len(learned_chars) < 4:
            pool_romaji = pool_romaji + all_romaji[:8]
            pool_symbols = pool_symbols + all_symbols[:8]
        # bài âm đục / âm ghép có tới 23-33 chữ: chỉ hỏi 8 chữ tiêu biểu cho vừa 1 phiên
        quiz_chars = chars if len(chars) <= 8 else chars[:4] + random.sample(chars[4:], 4)

        # (a) Nhìn chữ -> chọn cách đọc
        for sym, rom in quiz_chars:
            qid = add_mcq(lid, "TRANSLATE_TO_VN",
                          "Chữ 「%s」 đọc là gì?" % sym,
                          rom, sample_other(pool_romaji, {rom}, 3),
                          audio=audio_for(sym, "kana-" + rom, folder="kana"),
                          meta={"symbol": sym, "romaji": rom, "type": kana_type})
            register_bank(tid, qid)

        # (b) Nghe -> chọn chữ
        for sym, rom in quiz_chars[:3]:
            qid = add_mcq(lid, "LISTEN_AND_SELECT",
                          "Nghe và chọn chữ đúng",
                          sym, sample_other(pool_symbols, {sym}, 3),
                          audio=audio_for(sym, "kana-" + rom, folder="kana"),
                          meta={"romaji": rom, "type": kana_type})
            register_bank(tid, qid)

        # (c) Biết cách đọc -> chọn mặt chữ
        for sym, rom in quiz_chars[:3]:
            qid = add_mcq(lid, "TRANSLATE_TO_JP",
                          "Âm 「%s」 viết bằng %s là chữ nào?" % (rom, "Hiragana" if kana_type == "HIRAGANA" else "Katakana"),
                          sym, sample_other(pool_symbols, {sym}, 3),
                          meta={"romaji": rom, "type": kana_type})
            register_bank(tid, qid)

        # (d) Từ vựng ghép từ các chữ đã học
        all_topic_words = [w for l in lessons_data for w in l["words"]]
        word_pool_vn = ([w["vn"] for w in learned_words + ldata["words"]]
                        + [w["vn"] for w in all_topic_words])
        word_pool_kana = ([w["kana"] for w in learned_words + ldata["words"]]
                          + [w["kana"] for w in all_topic_words])
        # ảnh nhiễu lấy từ cả chủ đề: thẻ đáp án chỉ có hình nên không lộ chữ chưa học
        img_pool = [w for w in all_topic_words if w.get("emoji")]
        for w_i, w in enumerate(ldata["words"]):
            w_audio = audio_for(w["kana"], w["romaji"])
            w_img = image_for(w.get("emoji"), w["romaji"], w["vn"])
            others = [x for x in img_pool if x["kana"] != w["kana"]]
            random.shuffle(others)
            others = others[:3]
            if w_img and w_i % 3 == 0 and len(others) == 3:
                # chọn hình theo từ (chỉ dùng khi đủ 3 hình nhiễu)
                opt_images = {w["vn"]: w_img}
                for o in others:
                    opt_images[o["vn"]] = image_for(o.get("emoji"), o["romaji"], o["vn"])
                qid = add_mcq(lid, "SELECT_IMAGE",
                              "Chọn hình đúng với từ 「%s」 (%s)" % (w["kana"], w["romaji"]),
                              w["vn"], [o["vn"] for o in others],
                              audio=w_audio, opt_images=opt_images,
                              meta={"kana": w["kana"], "romaji": w["romaji"]})
            elif w_i % 3 == 1:
                qid = add_mcq(lid, "LISTEN_AND_SELECT",
                              "Nghe và chọn nghĩa tiếng Việt đúng",
                              w["vn"], sample_other(word_pool_vn, {w["vn"]}, 3),
                              audio=w_audio,
                              meta={"kana": w["kana"], "romaji": w["romaji"]})
            else:
                qid = add_mcq(lid, "TRANSLATE_TO_JP",
                              "「%s」 viết bằng tiếng Nhật là gì?" % w["vn"],
                              w["kana"], sample_other(word_pool_kana, {w["kana"]}, 3),
                              image=w_img,
                              meta={"romaji": w["romaji"], "vn": w["vn"]})
            register_bank(tid, qid)

            # đánh vần: ghép từng chữ cái thành từ
            if len(w["kana"]) >= 2 and w_i % 2 == 0:
                blocks = split_kana(w["kana"])
                if 2 <= len(blocks) <= 5:
                    qid = S.question(lid, "LISTEN_AND_ARRANGE",
                                     "Nghe và ghép các chữ thành từ 「%s」" % w["vn"],
                                     w_audio, w_img,
                                     {"romaji": w["romaji"], "vn": w["vn"], "jp": w["kana"]})
                    for i, b in enumerate(blocks, start=1):
                        S.option(qid, b, True, i)
                    register_bank(tid, qid)

        # (e) Luyện phát âm
        if ldata["words"]:
            w = ldata["words"][-1]
            qid = add_speaking(lid, w["kana"], w["romaji"], w["vn"],
                               audio_for(w["kana"], w["romaji"]),
                               image=image_for(w.get("emoji"), w["romaji"], w["vn"]))
            register_bank(tid, qid)

        learned_words.extend(ldata["words"])
    return tid


SMALL_KANA = set("ゃゅょぁぃぅぇぉゎャュョァィゥェォヮ")


def split_kana(word):
    """Tách từ thành các ô chữ (âm ghép きゃ đi liền 1 khối, dấu ー gắn vào chữ trước)."""
    blocks = []
    for ch in word:
        if blocks and (ch in SMALL_KANA or ch in "ーっッ"):
            blocks[-1] += ch
        else:
            blocks.append(ch)
    return blocks


# ===========================================================================
# 2) TOPIC TỪ VỰNG / NGỮ PHÁP
# ===========================================================================
# Các mục NGỮ PHÁP (trợ từ, đuôi động từ, từ chỉ định) không thể minh hoạ bằng
# hình: một cái ảnh không thể diễn tả "trợ từ tân ngữ を". Chúng vẫn có icon
# trang trí nhưng không bao giờ bị đem ra làm câu "chọn hình đúng".
FUNCTION_WORDS = {
    "の", "を", "が", "に", "で", "へ", "から", "まで", "と",
    "くて", "で", "はん", "〜さん", "〜さい",
    "これ", "それ", "あれ", "どれ", "この", "その", "あの", "どの",
    "ここ", "そこ", "あそこ", "どこ", "だれの", "わたしの", "かいしゃの",
    "あまり", "とても", "ちょっと", "そして", "それから", "でも", "まだ",
    "はい", "いいえ", "なん", "だれ", "そう", "いくら", "なんさい",
    "なんばん", "なんじ", "なんぷん", "なんようび", "なんにん",
    "ぜんぶで", "いっしょに", "ちがいます",
    "いつも", "よく", "ときどき", "ぜんぜん", "どう", "いつ",
}


def is_function_word(v):
    return v["kana"] in FUNCTION_WORDS


def distinct_image_distractors(target, pool):
    """3 đáp án nhiễu cho câu chọn hình, ép MỖI THẺ MỘT EMOJI KHÁC NHAU.

    Bộ dữ liệu dùng emoji làm ảnh minh hoạ, mà nhiều từ trong cùng một chủ đề lại
    trùng emoji: 11 từ số đếm đều là 🔢, còn ちち / おとうさん / おとこのひと đều là 👨.
    Không lọc thì câu "chọn hình đúng" hiện 4 thẻ ảnh giống hệt nhau — không đáp án
    nào đúng hơn đáp án nào, người học chỉ còn nước đoán. Gom không đủ 3 emoji khác
    nhau thì trả về danh sách thiếu, và bên gọi sẽ bỏ hẳn dạng câu này.
    """
    used = {target.get("emoji")}
    cands = [x for x in pool
             if x.get("emoji") and x["kana"] != target["kana"]
             and x["vn"] != target["vn"] and not is_function_word(x)]
    random.shuffle(cands)
    out = []
    for x in cands:
        if x["emoji"] in used:
            continue
        used.add(x["emoji"])
        out.append(x)
        if len(out) == 3:
            break
    return out


def build_vocab_topic(tdata, order_index):
    tid = S.topic(tdata["title"], tdata["desc"], order_index)
    all_vocab = [v for l in tdata["lessons"] for v in l["vocab"]]
    register_vocab(all_vocab)

    learned = []
    for idx, ldata in enumerate(tdata["lessons"], start=1):
        cfg = dict(VOCAB_LESSON_CONFIG)
        cfg["description"] = ldata["desc"]
        lid = S.lesson(tid, ldata["title"], idx, "NORMAL", cfg)
        vocab = ldata["vocab"]
        learned.extend(vocab)
        # ưu tiên lấy từ đã học làm đáp án nhiễu, không đủ mới mượn thêm của chủ đề
        pool_vn = [v["vn"] for v in learned] + [v["vn"] for v in all_vocab]
        pool_kana = [v["kana"] for v in learned] + [v["kana"] for v in all_vocab]

        for v_i, v in enumerate(vocab):
            v_audio = audio_for(v["kana"], v["romaji"])
            v_img = image_for(v.get("emoji"), v["romaji"], v["vn"])

            # 1) Nhật -> Việt. KHÔNG gắn audio: đề bài đã in sẵn mặt chữ kèm romaji,
            #    nghe hay không nghe cũng không đổi việc phải làm. Gắn vào thì màn hình
            #    mọc thêm một cái loa chẳng phục vụ câu hỏi nào.
            #
            #    Cách đọc vẫn phải có, chỉ là ở chỗ khác: `teachAudio` trong metadata.
            #    Pha DẠY trước bài dựng thẻ từ chính đề bài của phiên đó, và ở đó nghe
            #    phát âm là việc chính. Để riêng một trường mà UI câu hỏi không đọc tới
            #    thì thẻ dạy có tiếng còn đề bài vẫn sạch, không mọc thêm nút loa nào.
            qid = add_mcq(lid, "TRANSLATE_TO_VN",
                          "「%s」 (%s) nghĩa là gì?" % (v["kana"], v["romaji"]),
                          v["vn"], sample_other(pool_vn, {v["vn"]}, 3),
                          meta={"kana": v["kana"], "romaji": v["romaji"],
                                "vn": v["vn"], "teachAudio": v_audio})
            register_bank(tid, qid)

            # 2) Dạng thứ hai xoay vòng để bài học không đơn điệu
            mode = v_i % 3
            picture_ok = (mode == 0 and v_img and not tdata.get("no_image_quiz")
                          and not is_function_word(v))
            others = distinct_image_distractors(v, all_vocab) if picture_ok else []
            if picture_ok and len(others) == 3:
                # Chọn hình: đề bài là CHỮ, đáp án là ẢNH -> không cần âm thanh.
                opt_images = {v["vn"]: v_img}
                for o in others:
                    opt_images[o["vn"]] = image_for(o.get("emoji"), o["romaji"], o["vn"])
                qid = add_mcq(lid, "SELECT_IMAGE",
                              "Chọn hình đúng với từ 「%s」 (%s)" % (v["kana"], v["romaji"]),
                              v["vn"], [o["vn"] for o in others],
                              opt_images=opt_images,
                              meta={"kana": v["kana"], "romaji": v["romaji"],
                                    "vn": v["vn"], "teachAudio": v_audio})
            elif mode == 1 or tdata.get("no_image_quiz") or is_function_word(v) or not v_img:
                # Nghe rồi chọn chữ: ở đây âm thanh CHÍNH LÀ đề bài nên bắt buộc có loa.
                # Nhưng KHÔNG được đính ảnh: ảnh minh hoạ của chính từ đang hỏi làm lộ
                # đáp án — người học nhìn hình đoán ra mà chẳng cần nghe gì cả.
                qid = add_mcq(lid, "LISTEN_AND_SELECT",
                              "Nghe và chọn từ tiếng Nhật đúng",
                              v["kana"], sample_other(pool_kana, {v["kana"]}, 3),
                              audio=v_audio,
                              meta={"romaji": v["romaji"], "vn": v["vn"]})
            else:
                # Việt -> Nhật: ảnh chỉ minh hoạ cho đề bài tiếng Việt nên không lộ gì,
                # còn audio thì đọc thẳng đáp án ra nên tuyệt đối không gắn.
                qid = add_mcq(lid, "TRANSLATE_TO_JP",
                              "「%s」 tiếng Nhật nói thế nào?" % v["vn"],
                              v["kana"], sample_other(pool_kana, {v["kana"]}, 3),
                              image=v_img,
                              meta={"kana": v["kana"], "romaji": v["romaji"],
                                    "vn": v["vn"], "teachAudio": v_audio})
            register_bank(tid, qid)

        # Câu mẫu: sắp xếp câu + dịch câu + luyện nói
        all_sentences = [s for l in tdata["lessons"] for s in l["sentences"]]
        for s_i, sent in enumerate(ldata["sentences"]):
            s_audio = audio_for(sent["jp"], slugify(sent["romaji"])[:60], folder="sentences")
            s_img = illustration_for(sent["jp"], all_vocab)
            register_bank(tid, add_arrange(lid, sent, s_audio, image=s_img))

            # Dịch câu: nguyên câu đã in ra màn hình, không có gì để nghe -> không loa.
            qid = add_mcq(lid, "TRANSLATE_TO_VN",
                          "Câu 「%s」 có nghĩa là gì?" % sent["jp"],
                          sent["vn"],
                          sample_other([x["vn"] for x in all_sentences], {sent["vn"]}, 3),
                          meta={"romaji": sent["romaji"], "jp": sent["jp"],
                                "vn": sent["vn"], "teachAudio": s_audio})
            register_bank(tid, qid)

            if s_i == 0:
                register_bank(tid, add_speaking(lid, sent["jp"], sent["romaji"], sent["vn"],
                                                s_audio, image=s_img))
    return tid


# ===========================================================================
# 3) Bài ôn tập tính giờ + bài thi vượt cấp
# ===========================================================================
def clone_questions(target_lesson_id, question_ids):
    """Nhân bản câu hỏi (kèm đáp án) sang bài thi vượt cấp."""
    by_q = {}
    for o in S.options:
        by_q.setdefault(o[1], []).append(o)
    for qid in question_ids:
        q = S.questions[qid - 1]
        new_qid = S.question(target_lesson_id, q[2], q[3], q[4], q[5], q[6])
        for o in by_q.get(qid, []):
            S.option(new_qid, o[2], o[5], o[6], image=o[3], audio=o[4], meta=o[7])


def add_review_lesson(topic_id, order_index, title, desc, n=14, lesson_type="TOPIC_REVIEW"):
    """Thêm 1 bài TOPIC_REVIEW / TIMED_REVIEW / JUMP_TEST cho 1 topic.

    3 loại này dùng chung Seed.lesson() nhưng khác hẳn nhau ở CÁCH LẤY CÂU HỎI lúc
    /start (xem LessonAttemptServiceImpl.startLesson phía backend Java):
      - TOPIC_REVIEW : ĐỘNG theo lịch SM-2 của user, quét CẢ topic cũ lẫn topic hiện tại
                       (buildTopicReviewQuestionPool) -- lesson row ở đây không cần câu hỏi
                       tĩnh, chỉ cần tồn tại để có vị trí/energy-cost/config trên bản đồ.
      - TIMED_REVIEW : TĨNH, nhân bản từ vựng của CHÍNH topic hiện tại -- bài đánh giá
                       nhanh tốc độ làm bài, không phải cơ chế ôn tập thích ứng.
      - JUMP_TEST    : TĨNH, nhân bản từ vựng của CHÍNH topic (hoặc toàn bộ nếu là bài thi
                       vượt cấp tổng quát) -- vẫn giữ nguyên như thiết kế cũ.
    """
    if lesson_type == "TOPIC_REVIEW":
        cfg = dict(REVIEW_CONFIG)
    elif lesson_type == "TIMED_REVIEW":
        cfg = dict(REVIEW_CONFIG)
    else:
        cfg = dict(JUMP_CONFIG)
    cfg["description"] = desc
    # TOPIC_REVIEW nằm TRÊN đường lộ trình (như NORMAL) nên bắt buộc có order_index.
    # TIMED_REVIEW và JUMP_TEST đều KHÔNG nằm trên đường lộ trình: cột order_index của
    # bảng lessons ghi rõ "NULL cho JUMP_TEST/TIMED_REVIEW (vẽ ở Header/cạnh đường, không
    # nằm trên đường uốn lượn)", và CreateLessonRequest cũng chỉ bắt buộc order_index với
    # NORMAL/TOPIC_REVIEW. Đặt số cho 2 loại kia sẽ khiến frontend vẽ lẫn vào chuỗi bài
    # thường và người học tưởng mình đã hoàn thành thêm một bài.
    if lesson_type in ("TIMED_REVIEW", "JUMP_TEST"):
        order_index = None
    lid = S.lesson(topic_id, title, order_index, lesson_type, cfg)
    if lesson_type in ("TIMED_REVIEW", "JUMP_TEST"):
        # Bộ đề CỐ ĐỊNH theo thiết kế (không phải cơ chế ôn tập thích ứng) -- nhân bản
        # tĩnh từ ngân hàng câu hỏi của topic.
        bank = topic_question_bank.get(topic_id, [])
        picked = random.sample(bank, min(n, len(bank)))
        clone_questions(lid, picked)
    # TOPIC_REVIEW KHÔNG clone câu hỏi tĩnh ở đây: nội dung của nó được chọn ĐỘNG lúc
    # /start dựa trên lịch ôn SM-2 của từng user (xem
    # LessonAttemptServiceImpl.buildTopicReviewQuestionPool phía backend Java) -- ưu tiên
    # từ đang đến hạn quên, rồi mới tới từ sắp đến hạn, rồi mới fallback random trong
    # phạm vi topic hiện tại VÀ mọi topic cũ hơn.
    return lid


# ===========================================================================
# BUILD
# ===========================================================================
def build():
    """Dựng toàn bộ lộ trình.

    Bản đồ lộ trình KHÔNG còn 2 chủ đề bảng chữ cái (Hiragana / Katakana). Ứng dụng
    đã có một trang học chữ riêng đọc từ bảng `characters`, nên để chúng nằm trên
    map nữa là dạy trùng và làm người học phải cày 24 bài mặt chữ trước khi được
    nói một câu tiếng Nhật nào. Hàm `build_kana_topic` vẫn giữ nguyên phía trên
    phòng khi muốn dựng lại, chỉ là không được gọi ở đây.

    Đổi lại, lộ trình gồm 14 chủ đề tình huống đời thường, xếp theo thứ tự bài sau
    chỉ dùng từ và ngữ pháp của các bài trước.
    """
    order = 1
    for t_i, tdata in enumerate(TOPICS_A + TOPICS_B + TOPICS_C, start=1):
        tid = build_vocab_topic(tdata, order)
        n_normal = len(tdata["lessons"])

        # TOPIC_REVIEW ("ôn tập topic cũ", bắt buộc, chặn đường): chỉ từ topic thứ
        # TOPIC_REVIEW_FROM_ORDER trở đi -- 2 topic đầu chưa có topic cũ nào để ôn.
        # Nằm trên đường đi, ngay sau bài NORMAL cuối cùng.
        if t_i >= TOPIC_REVIEW_FROM_ORDER:
            add_review_lesson(tid, n_normal + 1,
                              "Ôn tập: %s" % tdata["title"], "",
                              n=14, lesson_type="TOPIC_REVIEW")

        # TIMED_REVIEW ("ôn tập tốc độ", tuỳ chọn, cạnh đường): mọi topic đều có,
        # dùng vốn từ của CHÍNH topic đó.
        add_review_lesson(tid, None,
                          "Ôn tập tốc độ: %s" % tdata["title"], "",
                          n=14, lesson_type="TIMED_REVIEW")

        # JUMP_TEST riêng của topic ("bài nhảy vượt"): mọi topic đều có, luôn mở, dùng
        # vốn từ của CHÍNH topic đó -- pass là đánh dấu cả topic COMPLETED (nhảy cóc qua
        # topic). Tạo SAU CÙNG trong topic (id lớn nhất) để khi sắp theo order_index
        # kiểu nulls-last, nó luôn là phần tử CUỐI trong danh sách bài học của topic.
        add_review_lesson(tid, None,
                          "Thi vượt: %s" % tdata["title"], "",
                          n=15, lesson_type="JUMP_TEST")
        order += 1


# ===========================================================================
# XUẤT SQL
# ===========================================================================
def q(v):
    if v is None:
        return "NULL"
    if isinstance(v, bool):
        return "1" if v else "0"
    if isinstance(v, (int, float)):
        return str(v)
    if isinstance(v, (dict, list)):
        v = json.dumps(v, ensure_ascii=False)
    return "'" + str(v).replace("\\", "\\\\").replace("'", "''") + "'"


def write_sql():
    L = []
    L.append("-- ==========================================================")
    L.append("-- Du lieu demo BE_NihongoApp - sinh tu test-data/demo-seed/build.py")
    L.append("-- Chay lai build.py se sinh ra file nay y het (random da co seed).")
    L.append("-- ==========================================================")
    L.append("SET NAMES utf8mb4;")
    L.append("SET FOREIGN_KEY_CHECKS = 0;")
    L.append("")
    L.append("-- 1. Xoa sach noi dung hoc cu (giu nguyen bang users, shop, rank, quest)")
    for t in ["lesson_attempt_answers", "user_mistakes", "user_lesson_progress",
              "lesson_question_options", "lesson_questions", "lessons", "topics"]:
        L.append("DELETE FROM %s;" % t)
        L.append("ALTER TABLE %s AUTO_INCREMENT = 1;" % t)
    L.append("SET FOREIGN_KEY_CHECKS = 1;")
    L.append("")

    L.append("-- 2. Chu de")
    for tid, title, desc, oi in S.topics:
        L.append("INSERT INTO topics (id, title, description, order_index) VALUES (%d, %s, %s, %d);"
                 % (tid, q(title), q(desc), oi))
    L.append("")

    L.append("-- 3. Bai hoc")
    for lid, tid, title, oi, ltype, cfg in S.lessons:
        L.append("INSERT INTO lessons (id, topic_id, jlpt_level, title, order_index, lesson_type, "
                 "config_json) VALUES (%d, %d, 'N5', %s, %s, '%s', %s);"
                 % (lid, tid, q(title), q(oi), ltype, q(cfg)))
    L.append("")

    L.append("-- 4. Cau hoi")
    B = []
    for qid, lid, qtype, text, audio, image, meta in S.questions:
        B.append("(%d, %d, '%s', %s, %s, %s, %s)"
                 % (qid, lid, qtype, q(text), q(audio), q(image), q(meta)))
    for i in range(0, len(B), 200):
        L.append("INSERT INTO lesson_questions (id, lesson_id, question_type, question_text, "
                 "audio_url, image_url, metadata_json) VALUES\n" + ",\n".join(B[i:i + 200]) + ";")
    L.append("")

    L.append("-- 5. Dap an")
    B = []
    for oid, qid, text, image, audio, correct, oi, meta in S.options:
        B.append("(%d, %d, %s, %s, %s, %s, %s, %s)"
                 % (oid, qid, q(text), q(image), q(audio), q(correct), q(oi), q(meta)))
    for i in range(0, len(B), 300):
        L.append("INSERT INTO lesson_question_options (id, question_id, option_text, image_url, "
                 "audio_url, is_correct, order_index, metadata_json) VALUES\n"
                 + ",\n".join(B[i:i + 300]) + ";")
    L.append("")

    # Am thanh cua 208 ky tu bang chu cai KHONG sinh o day nua: lo trinh da bo 2 chu
    # de bang chu cai, nen audio_registry khong con muc nao thuoc thu muc "kana".
    # File out/seed_kana_audio.sql (rieng, van chay trong load_all.sh) lo phan do.

    path = os.path.join(OUT_DIR, "seed.sql")
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        f.write("\n".join(L))
    return path


def write_media():
    data = {
        "audio": [{"rel": v["rel"], "text": v["text"], "voice": v["voice"]}
                  for v in audio_registry.values()],
        "image": [{"rel": v["rel"], "emoji": v["emoji"], "label": v["label"]}
                  for v in image_registry.values()],
    }
    path = os.path.join(OUT_DIR, "media.json")
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(data, f, ensure_ascii=False, indent=1)
    return path


if __name__ == "__main__":
    os.makedirs(OUT_DIR, exist_ok=True)
    build()
    sql_path = write_sql()
    media_path = write_media()
    n_normal = sum(1 for l in S.lessons if l[4] == "NORMAL")
    n_topic_review = sum(1 for l in S.lessons if l[4] == "TOPIC_REVIEW")
    n_timed_review = sum(1 for l in S.lessons if l[4] == "TIMED_REVIEW")
    n_jump = sum(1 for l in S.lessons if l[4] == "JUMP_TEST")
    print("topics          : %d" % len(S.topics))
    print("lessons         : %d (NORMAL %d / TOPIC_REVIEW %d / TIMED_REVIEW %d / JUMP_TEST %d)"
          % (len(S.lessons), n_normal, n_topic_review, n_timed_review, n_jump))
    print("questions       : %d" % len(S.questions))
    print("options         : %d" % len(S.options))
    print("audio files     : %d" % len(audio_registry))
    print("image files     : %d" % len(image_registry))
    print("-> %s" % sql_path)
    print("-> %s" % media_path)
