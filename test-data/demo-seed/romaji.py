# -*- coding: utf-8 -*-
"""
Chuyen kana -> romaji (kieu Hepburn, nguyen am dai viet lap chu khong dung dau ngang).

Vi sao can: nguoi hoc chua biet mot chu tieng Nhat nao. Moi the dap an viet bang
kana deu phai co dong romaji nho ben duoi, ke ca dap an nhieu va cac the roi cua
cau sap xep — nhung nhung thu do khong co san romaji trong file du lieu (chi tu
vung va ca cau moi co). Chuyen tu dong o day thi 100% chu tieng Nhat trong de bai
deu doc duoc, khong phai go tay hang nghin dong.

Kieu viet bam theo dung phong cach romaji da go tay trong data_topics_*.py de hai
nguon khong danh nhau: "ohayou" (giu u), "kooto" (lap nguyen am cho dau ー),
"juppun" (lap phu am cho っ).
"""

# Am ghep (youon) phai tra truoc vi dai hon 1 ky tu.
_YOUON = {
    "きゃ": "kya", "きゅ": "kyu", "きょ": "kyo",
    "しゃ": "sha", "しゅ": "shu", "しょ": "sho",
    "ちゃ": "cha", "ちゅ": "chu", "ちょ": "cho",
    "にゃ": "nya", "にゅ": "nyu", "にょ": "nyo",
    "ひゃ": "hya", "ひゅ": "hyu", "ひょ": "hyo",
    "みゃ": "mya", "みゅ": "myu", "みょ": "myo",
    "りゃ": "rya", "りゅ": "ryu", "りょ": "ryo",
    "ぎゃ": "gya", "ぎゅ": "gyu", "ぎょ": "gyo",
    "じゃ": "ja", "じゅ": "ju", "じょ": "jo",
    "ぢゃ": "ja", "ぢゅ": "ju", "ぢょ": "jo",
    "びゃ": "bya", "びゅ": "byu", "びょ": "byo",
    "ぴゃ": "pya", "ぴゅ": "pyu", "ぴょ": "pyo",
    # to hop chi co o Katakana, dung cho tu muon: フォ, ティ, ウィ...
    "ふぁ": "fa", "ふぃ": "fi", "ふぇ": "fe", "ふぉ": "fo",
    "うぃ": "wi", "うぇ": "we", "うぉ": "wo",
    "てぃ": "ti", "でぃ": "di", "とぅ": "tu", "どぅ": "du",
    "しぇ": "she", "ちぇ": "che", "じぇ": "je",
    "つぁ": "tsa", "つぃ": "tsi", "つぇ": "tse", "つぉ": "tso",
    "ゔぁ": "va", "ゔぃ": "vi", "ゔぇ": "ve", "ゔぉ": "vo",
}

_MONO = {
    "あ": "a", "い": "i", "う": "u", "え": "e", "お": "o",
    "か": "ka", "き": "ki", "く": "ku", "け": "ke", "こ": "ko",
    "さ": "sa", "し": "shi", "す": "su", "せ": "se", "そ": "so",
    "た": "ta", "ち": "chi", "つ": "tsu", "て": "te", "と": "to",
    "な": "na", "に": "ni", "ぬ": "nu", "ね": "ne", "の": "no",
    "は": "ha", "ひ": "hi", "ふ": "fu", "へ": "he", "ほ": "ho",
    "ま": "ma", "み": "mi", "む": "mu", "め": "me", "も": "mo",
    "や": "ya", "ゆ": "yu", "よ": "yo",
    "ら": "ra", "り": "ri", "る": "ru", "れ": "re", "ろ": "ro",
    "わ": "wa", "を": "o", "ん": "n",
    "が": "ga", "ぎ": "gi", "ぐ": "gu", "げ": "ge", "ご": "go",
    "ざ": "za", "じ": "ji", "ず": "zu", "ぜ": "ze", "ぞ": "zo",
    "だ": "da", "ぢ": "ji", "づ": "zu", "で": "de", "ど": "do",
    "ば": "ba", "び": "bi", "ぶ": "bu", "べ": "be", "ぼ": "bo",
    "ぱ": "pa", "ぴ": "pi", "ぷ": "pu", "ぺ": "pe", "ぽ": "po",
    "ゔ": "vu",
    # kana nho dung mot minh (hiem, chu yeu trong tu muon)
    "ぁ": "a", "ぃ": "i", "ぅ": "u", "ぇ": "e", "ぉ": "o",
    "ゃ": "ya", "ゅ": "yu", "ょ": "yo",
}

# Dau cau va khoang trang giu nguyen, khong doc thanh am.
_PUNCT = {"、": ", ", "。": ".", "？": "?", "！": "!", "・": " ", "　": " ", " ": " "}

_VOWELS = set("aiueo")

# Ba tro tu doc LECH mat chu. Chi ap dung khi ca doan chinh la tro tu do — trong
# de bai chung luon dung mot the roi, con nam trong tu (こんにちは) thi da co romaji
# go tay trong data nen khong den luot bang nay.
_PARTICLE = {"は": "wa", "へ": "e", "を": "o"}


def _to_hiragana(text):
    """Doi Katakana -> Hiragana de chi phai giu MOT bang tra."""
    out = []
    for ch in text:
        code = ord(ch)
        # Katakana ア(0x30A1) .. ン(0x30F6) lech dung 0x60 so voi Hiragana tuong ung.
        if 0x30A1 <= code <= 0x30F6:
            out.append(chr(code - 0x60))
        else:
            out.append(ch)
    return "".join(out)


def kana_to_romaji(text):
    """Tra ve romaji cua mot doan kana. Ky tu la (kanji, chu Latin) giu nguyen."""
    if not text:
        return ""
    stripped = text.strip()
    if stripped in _PARTICLE:
        return _PARTICLE[stripped]
    s = _to_hiragana(text)
    out = []
    i = 0
    n = len(s)
    while i < n:
        ch = s[i]

        # 1) っ nho: lap phu am dau cua am ke tiep (きって -> kitte, じゅっぷん -> juppun)
        if ch == "っ":
            nxt = kana_to_romaji(s[i + 1:i + 3]) if i + 1 < n else ""
            if nxt and nxt[0] not in _VOWELS:
                out.append("tt" if nxt.startswith("ch") else nxt[0])
            i += 1
            continue

        # 2) ー (dau truong am cua Katakana): keo dai nguyen am vua doc
        if ch in ("ー", "－", "ー"):
            if out and out[-1] and out[-1][-1] in _VOWELS:
                out.append(out[-1][-1])
            i += 1
            continue

        # 3) am ghep 2 ky tu
        if i + 1 < n and s[i:i + 2] in _YOUON:
            out.append(_YOUON[s[i:i + 2]])
            i += 2
            continue

        # 4) am don
        if ch in _MONO:
            r = _MONO[ch]
            # ん truoc nguyen am phai co dau nhay, neu khong "しんいち" doc thanh shi-ni-chi.
            # Truoc y thi KHONG them, de khop voi cach viet san trong data (honya, kinyoubi).
            if r == "n" and i + 1 < n:
                nxt = kana_to_romaji(s[i + 1])
                if nxt and nxt[0] in _VOWELS:
                    r = "n'"
            out.append(r)
            i += 1
            continue

        # 5) dau cau / ky tu khong phai kana
        out.append(_PUNCT.get(ch, ch))
        i += 1

    return "".join(out).strip()
