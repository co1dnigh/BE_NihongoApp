# -*- coding: utf-8 -*-
"""
Chuong trinh hoc bang chu cai (Hiragana + Katakana) cho nguoi Viet moi bat dau,
chua biet mot chu tieng Nhat nao.

Nguyen tac su pham quan trong nhat cua file nay:
  MOI BAI CHI DUNG NHUNG CHU DA HOC O CAC BAI TRUOC + chu cua chinh bai do.
Nho vay nguoi hoc luon doc duoc 100% tu vung xuat hien trong bai, khong bao gio
gap mot ky tu la — dung cach cac giao trinh that (Minna no Nihongo, Genki) lam.
"""

# ---------------------------------------------------------------------------
# HIRAGANA - 10 bai hoc, moi bai 1 hang chu + tu vung ghep tu chu da hoc
# ---------------------------------------------------------------------------
HIRAGANA_LESSONS = [
    {
        "title": "Bài 1: Nguyên âm あ い う え お",
        "desc": "5 nguyên âm là nền móng của toàn bộ tiếng Nhật. Mọi chữ còn lại đều là "
                "phụ âm ghép với 5 nguyên âm này, nên thuộc thứ tự a-i-u-e-o là bước đi đầu tiên.",
        "chars": [("あ", "a"), ("い", "i"), ("う", "u"), ("え", "e"), ("お", "o")],
        "words": [
            {"kana": "あい", "romaji": "ai", "vn": "tình yêu", "emoji": "❤️"},
            {"kana": "いえ", "romaji": "ie", "vn": "ngôi nhà", "emoji": "🏠"},
            {"kana": "うえ", "romaji": "ue", "vn": "phía trên", "emoji": "⬆️"},
            {"kana": "あお", "romaji": "ao", "vn": "màu xanh dương", "emoji": "🔵"},
        ],
    },
    {
        "title": "Bài 2: Hàng K か き く け こ",
        "desc": "Phụ âm K ghép với 5 nguyên âm đã học. Từ bài này bạn đã ghép được "
                "những từ tiếng Nhật thật đầu tiên.",
        "chars": [("か", "ka"), ("き", "ki"), ("く", "ku"), ("け", "ke"), ("こ", "ko")],
        "words": [
            {"kana": "かお", "romaji": "kao", "vn": "khuôn mặt", "emoji": "😀"},
            {"kana": "えき", "romaji": "eki", "vn": "nhà ga", "emoji": "🚉"},
            {"kana": "いけ", "romaji": "ike", "vn": "cái ao", "emoji": "🏞️"},
            {"kana": "きく", "romaji": "kiku", "vn": "nghe", "emoji": "👂"},
        ],
    },
    {
        "title": "Bài 3: Hàng S さ し す せ そ",
        "desc": "Lưu ý し đọc là shi chứ không phải si — đây là ngoại lệ phát âm đầu tiên "
                "bạn gặp trong bảng chữ cái, hãy nhớ thật kỹ.",
        "chars": [("さ", "sa"), ("し", "shi"), ("す", "su"), ("せ", "se"), ("そ", "so")],
        "words": [
            {"kana": "すし", "romaji": "sushi", "vn": "món sushi", "emoji": "🍣"},
            {"kana": "いす", "romaji": "isu", "vn": "cái ghế", "emoji": "🪑"},
            {"kana": "あさ", "romaji": "asa", "vn": "buổi sáng", "emoji": "🌅"},
            {"kana": "うそ", "romaji": "uso", "vn": "lời nói dối", "emoji": "🤥"},
            {"kana": "せかい", "romaji": "sekai", "vn": "thế giới", "emoji": "🌍"},
        ],
    },
    {
        "title": "Bài 4: Hàng T た ち つ て と",
        "desc": "Hàng T có tới 2 ngoại lệ: ち đọc là chi và つ đọc là tsu. "
                "Đây là 2 âm người Việt hay đọc sai nhất khi mới học.",
        "chars": [("た", "ta"), ("ち", "chi"), ("つ", "tsu"), ("て", "te"), ("と", "to")],
        "words": [
            {"kana": "たこ", "romaji": "tako", "vn": "con bạch tuộc", "emoji": "🐙"},
            {"kana": "くつ", "romaji": "kutsu", "vn": "đôi giày", "emoji": "👟"},
            {"kana": "とけい", "romaji": "tokei", "vn": "cái đồng hồ", "emoji": "⌚"},
            {"kana": "て", "romaji": "te", "vn": "bàn tay", "emoji": "✋"},
            {"kana": "そと", "romaji": "soto", "vn": "bên ngoài", "emoji": "🚪"},
        ],
    },
    {
        "title": "Bài 5: Hàng N な に ぬ ね の",
        "desc": "Học xong hàng N bạn đã đọc được tên 2 con vật quen thuộc nhất "
                "trong tiếng Nhật: ねこ (mèo) và いぬ (chó).",
        "chars": [("な", "na"), ("に", "ni"), ("ぬ", "nu"), ("ね", "ne"), ("の", "no")],
        "words": [
            {"kana": "ねこ", "romaji": "neko", "vn": "con mèo", "emoji": "🐱"},
            {"kana": "いぬ", "romaji": "inu", "vn": "con chó", "emoji": "🐶"},
            {"kana": "さかな", "romaji": "sakana", "vn": "con cá", "emoji": "🐟"},
            {"kana": "なつ", "romaji": "natsu", "vn": "mùa hè", "emoji": "☀️"},
            {"kana": "にく", "romaji": "niku", "vn": "thịt", "emoji": "🍖"},
        ],
    },
    {
        "title": "Bài 6: Hàng H は ひ ふ へ ほ",
        "desc": "ふ đọc là fu — môi không chạm răng, giống như đang thổi nến. は và へ khi "
                "làm trợ từ sẽ đọc thành wa và e, điều đó bạn sẽ học ở phần ngữ pháp.",
        "chars": [("は", "ha"), ("ひ", "hi"), ("ふ", "fu"), ("へ", "he"), ("ほ", "ho")],
        "words": [
            {"kana": "はな", "romaji": "hana", "vn": "bông hoa", "emoji": "🌸"},
            {"kana": "ふね", "romaji": "fune", "vn": "con thuyền", "emoji": "⛵"},
            {"kana": "ほし", "romaji": "hoshi", "vn": "ngôi sao", "emoji": "⭐"},
            {"kana": "ひと", "romaji": "hito", "vn": "con người", "emoji": "🧍"},
            {"kana": "はこ", "romaji": "hako", "vn": "cái hộp", "emoji": "📦"},
        ],
    },
    {
        "title": "Bài 7: Hàng M ま み む め も",
        "desc": "Hàng M dễ với người Việt vì phát âm gần như giống hoàn toàn "
                "ma-mi-mu-me-mo trong tiếng Việt.",
        "chars": [("ま", "ma"), ("み", "mi"), ("む", "mu"), ("め", "me"), ("も", "mo")],
        "words": [
            {"kana": "うみ", "romaji": "umi", "vn": "biển", "emoji": "🌊"},
            {"kana": "あめ", "romaji": "ame", "vn": "mưa", "emoji": "🌧️"},
            {"kana": "もも", "romaji": "momo", "vn": "quả đào", "emoji": "🍑"},
            {"kana": "むし", "romaji": "mushi", "vn": "côn trùng", "emoji": "🐛"},
            {"kana": "め", "romaji": "me", "vn": "con mắt", "emoji": "👁️"},
        ],
    },
    {
        "title": "Bài 8: Hàng Y, R, W và ん",
        "desc": "Nhóm chữ cuối của bảng cơ bản: や ゆ よ (chỉ có 3 chữ), ら り る れ ろ, "
                "わ を, và ん — chữ duy nhất không có nguyên âm đi kèm.",
        "chars": [("や", "ya"), ("ゆ", "yu"), ("よ", "yo"),
                  ("ら", "ra"), ("り", "ri"), ("る", "ru"), ("れ", "re"), ("ろ", "ro"),
                  ("わ", "wa"), ("を", "wo"), ("ん", "n")],
        "words": [
            {"kana": "やま", "romaji": "yama", "vn": "ngọn núi", "emoji": "🏔️"},
            {"kana": "ゆき", "romaji": "yuki", "vn": "tuyết", "emoji": "❄️"},
            {"kana": "さくら", "romaji": "sakura", "vn": "hoa anh đào", "emoji": "🌸"},
            {"kana": "とり", "romaji": "tori", "vn": "con chim", "emoji": "🐦"},
            {"kana": "くるま", "romaji": "kuruma", "vn": "xe ô tô", "emoji": "🚗"},
            {"kana": "わたし", "romaji": "watashi", "vn": "tôi", "emoji": "🙋"},
            {"kana": "ほん", "romaji": "hon", "vn": "quyển sách", "emoji": "📖"},
        ],
    },
    {
        "title": "Bài 9: Âm đục ゛ và âm bán đục ゜",
        "desc": "Thêm 2 dấu nháy ゛ vào góc trên phải để biến か thành が (ka thành ga), さ thành ざ, "
                "た thành だ, は thành ば. Riêng hàng は thêm vòng tròn ゜ thành ぱ (pa). "
                "Không có chữ mới, chỉ có dấu mới.",
        "chars": [("が", "ga"), ("ぎ", "gi"), ("ぐ", "gu"), ("げ", "ge"), ("ご", "go"),
                  ("ざ", "za"), ("じ", "ji"), ("ず", "zu"), ("ぜ", "ze"), ("ぞ", "zo"),
                  ("だ", "da"), ("で", "de"), ("ど", "do"),
                  ("ば", "ba"), ("び", "bi"), ("ぶ", "bu"), ("べ", "be"), ("ぼ", "bo"),
                  ("ぱ", "pa"), ("ぴ", "pi"), ("ぷ", "pu"), ("ぺ", "pe"), ("ぽ", "po")],
        "words": [
            {"kana": "かばん", "romaji": "kaban", "vn": "cái cặp", "emoji": "🎒"},
            {"kana": "たまご", "romaji": "tamago", "vn": "quả trứng", "emoji": "🥚"},
            {"kana": "でんわ", "romaji": "denwa", "vn": "điện thoại", "emoji": "☎️"},
            {"kana": "みず", "romaji": "mizu", "vn": "nước", "emoji": "💧"},
            {"kana": "ぶた", "romaji": "buta", "vn": "con lợn", "emoji": "🐷"},
            {"kana": "さんぽ", "romaji": "sanpo", "vn": "đi dạo", "emoji": "🚶"},
        ],
    },
    {
        "title": "Bài 10: Âm ghép, âm ngắt っ và trường âm",
        "desc": "3 quy tắc cuối để đọc trọn vẹn Hiragana: chữ nhỏ ゃ ゅ ょ ghép thành 1 âm "
                "(きゃ = kya), chữ っ nhỏ làm âm ngắt (gấp đôi phụ âm đứng sau), và nguyên âm "
                "kéo dài (おう đọc thành ô dài).",
        "chars": [("きゃ", "kya"), ("きゅ", "kyu"), ("きょ", "kyo"),
                  ("しゃ", "sha"), ("しゅ", "shu"), ("しょ", "sho"),
                  ("ちゃ", "cha"), ("ちゅ", "chu"), ("ちょ", "cho"),
                  ("にゃ", "nya"), ("にゅ", "nyu"), ("にょ", "nyo"),
                  ("ひゃ", "hya"), ("ひゅ", "hyu"), ("ひょ", "hyo"),
                  ("みゃ", "mya"), ("みゅ", "myu"), ("みょ", "myo"),
                  ("りゃ", "rya"), ("りゅ", "ryu"), ("りょ", "ryo"),
                  ("ぎゃ", "gya"), ("ぎゅ", "gyu"), ("ぎょ", "gyo"),
                  ("じゃ", "ja"), ("じゅ", "ju"), ("じょ", "jo"),
                  ("びゃ", "bya"), ("びゅ", "byu"), ("びょ", "byo"),
                  ("ぴゃ", "pya"), ("ぴゅ", "pyu"), ("ぴょ", "pyo")],
        "words": [
            {"kana": "がっこう", "romaji": "gakkou", "vn": "trường học", "emoji": "🏫"},
            {"kana": "きって", "romaji": "kitte", "vn": "con tem", "emoji": "📮"},
            {"kana": "でんしゃ", "romaji": "densha", "vn": "tàu điện", "emoji": "🚃"},
            {"kana": "おちゃ", "romaji": "ocha", "vn": "trà", "emoji": "🍵"},
            {"kana": "りょこう", "romaji": "ryokou", "vn": "du lịch", "emoji": "🧳"},
            {"kana": "びょういん", "romaji": "byouin", "vn": "bệnh viện", "emoji": "🏥"},
        ],
    },
]

# ---------------------------------------------------------------------------
# KATAKANA - 10 bai, dung cho tu muon tieng nuoc ngoai
# ---------------------------------------------------------------------------
KATAKANA_LESSONS = [
    {
        "title": "Bài 1: Nguyên âm ア イ ウ エ オ",
        "desc": "Katakana đọc y hệt Hiragana, chỉ khác cách viết — nét thẳng và góc cạnh. "
                "Katakana dùng để viết từ mượn nước ngoài: cà phê, taxi, tên người nước ngoài.",
        "chars": [("ア", "a"), ("イ", "i"), ("ウ", "u"), ("エ", "e"), ("オ", "o")],
        "words": [],
    },
    {
        "title": "Bài 2: Hàng K カ キ ク ケ コ và dấu trường âm ー",
        "desc": "Katakana có thêm dấu ー để kéo dài nguyên âm — thứ mà Hiragana không có. "
                "ケーキ đọc là kê-ki (bánh kem), không phải ke-ki.",
        "chars": [("カ", "ka"), ("キ", "ki"), ("ク", "ku"), ("ケ", "ke"), ("コ", "ko")],
        "words": [
            {"kana": "ケーキ", "romaji": "keeki", "vn": "bánh kem", "emoji": "🍰"},
        ],
    },
    {
        "title": "Bài 3: Hàng S サ シ ス セ ソ",
        "desc": "Cặp chữ dễ nhầm nhất toàn bảng: シ (shi) với ツ (tsu), ソ (so) với ン (n). "
                "Mẹo: シ và ン có nét cuối hất từ dưới lên, ツ và ソ có nét cuối kéo từ trên xuống.",
        "chars": [("サ", "sa"), ("シ", "shi"), ("ス", "su"), ("セ", "se"), ("ソ", "so")],
        "words": [
            {"kana": "アイス", "romaji": "aisu", "vn": "kem", "emoji": "🍦"},
            {"kana": "キス", "romaji": "kisu", "vn": "nụ hôn", "emoji": "💋"},
        ],
    },
    {
        "title": "Bài 4: Hàng T タ チ ツ テ ト",
        "desc": "Giống Hiragana, チ đọc chi và ツ đọc tsu. Đây là hàng xuất hiện rất nhiều "
                "trong từ mượn tiếng Anh.",
        "chars": [("タ", "ta"), ("チ", "chi"), ("ツ", "tsu"), ("テ", "te"), ("ト", "to")],
        "words": [
            {"kana": "テスト", "romaji": "tesuto", "vn": "bài kiểm tra", "emoji": "📝"},
            {"kana": "タクシー", "romaji": "takushii", "vn": "xe taxi", "emoji": "🚕"},
        ],
    },
    {
        "title": "Bài 5: Hàng N ナ ニ ヌ ネ ノ",
        "desc": "ノ chỉ có đúng 1 nét — chữ đơn giản nhất bảng Katakana. Cẩn thận phân biệt "
                "ノ với ソ và ン.",
        "chars": [("ナ", "na"), ("ニ", "ni"), ("ヌ", "nu"), ("ネ", "ne"), ("ノ", "no")],
        "words": [
            {"kana": "テニス", "romaji": "tenisu", "vn": "quần vợt", "emoji": "🎾"},
            {"kana": "ノート", "romaji": "nooto", "vn": "quyển vở", "emoji": "📓"},
        ],
    },
    {
        "title": "Bài 6: Hàng H ハ ヒ フ ヘ ホ",
        "desc": "ヘ của Katakana và へ của Hiragana gần như giống hệt nhau — một trong "
                "vài cặp chữ song sinh của 2 bảng, viết sao cũng được.",
        "chars": [("ハ", "ha"), ("ヒ", "hi"), ("フ", "fu"), ("ヘ", "he"), ("ホ", "ho")],
        "words": [
            {"kana": "コーヒー", "romaji": "koohii", "vn": "cà phê", "emoji": "☕"},
            {"kana": "ナイフ", "romaji": "naifu", "vn": "con dao", "emoji": "🔪"},
        ],
    },
    {
        "title": "Bài 7: Hàng M マ ミ ム メ モ",
        "desc": "モ của Katakana và も của Hiragana viết khá giống nhau, đều 3 nét — "
                "học chung một lượt sẽ nhớ nhanh hơn.",
        "chars": [("マ", "ma"), ("ミ", "mi"), ("ム", "mu"), ("メ", "me"), ("モ", "mo")],
        "words": [
            {"kana": "トマト", "romaji": "tomato", "vn": "quả cà chua", "emoji": "🍅"},
            {"kana": "ハム", "romaji": "hamu", "vn": "giăm bông", "emoji": "🥓"},
            {"kana": "メモ", "romaji": "memo", "vn": "tờ ghi chú", "emoji": "🗒️"},
        ],
    },
    {
        "title": "Bài 8: Hàng Y, R, W và ン",
        "desc": "Hoàn tất bảng cơ bản Katakana. Từ bài này bạn đã đọc được hầu hết bảng hiệu, "
                "thực đơn và tên thương hiệu trên đường phố Nhật.",
        "chars": [("ヤ", "ya"), ("ユ", "yu"), ("ヨ", "yo"),
                  ("ラ", "ra"), ("リ", "ri"), ("ル", "ru"), ("レ", "re"), ("ロ", "ro"),
                  ("ワ", "wa"), ("ヲ", "wo"), ("ン", "n")],
        "words": [
            {"kana": "ラーメン", "romaji": "raamen", "vn": "mì ramen", "emoji": "🍜"},
            {"kana": "ホテル", "romaji": "hoteru", "vn": "khách sạn", "emoji": "🏨"},
            {"kana": "カメラ", "romaji": "kamera", "vn": "máy ảnh", "emoji": "📷"},
            {"kana": "メロン", "romaji": "meron", "vn": "quả dưa lưới", "emoji": "🍈"},
            {"kana": "ワイン", "romaji": "wain", "vn": "rượu vang", "emoji": "🍷"},
        ],
    },
    {
        "title": "Bài 9: Âm đục và âm bán đục Katakana",
        "desc": "Quy tắc dấu ゛ và ゜ giống hệt Hiragana. パン (bánh mì) và ビール (bia) "
                "là 2 từ bạn sẽ gặp mỗi ngày khi ở Nhật.",
        "chars": [("ガ", "ga"), ("ギ", "gi"), ("グ", "gu"), ("ゲ", "ge"), ("ゴ", "go"),
                  ("ザ", "za"), ("ジ", "ji"), ("ズ", "zu"), ("ゼ", "ze"), ("ゾ", "zo"),
                  ("ダ", "da"), ("デ", "de"), ("ド", "do"),
                  ("バ", "ba"), ("ビ", "bi"), ("ブ", "bu"), ("ベ", "be"), ("ボ", "bo"),
                  ("パ", "pa"), ("ピ", "pi"), ("プ", "pu"), ("ペ", "pe"), ("ポ", "po")],
        "words": [
            {"kana": "パン", "romaji": "pan", "vn": "bánh mì", "emoji": "🍞"},
            {"kana": "ビール", "romaji": "biiru", "vn": "bia", "emoji": "🍺"},
            {"kana": "テレビ", "romaji": "terebi", "vn": "ti vi", "emoji": "📺"},
            {"kana": "パソコン", "romaji": "pasokon", "vn": "máy tính", "emoji": "💻"},
            {"kana": "ゲーム", "romaji": "geemu", "vn": "trò chơi điện tử", "emoji": "🎮"},
        ],
    },
    {
        "title": "Bài 10: Âm ghép và âm ngắt ッ",
        "desc": "Chữ nhỏ ャ ュ ョ ghép âm, chữ nhỏ ッ làm âm ngắt. Đây là bài cuối cùng của "
                "bảng chữ cái — học xong bạn đọc được mọi ký tự kana của tiếng Nhật.",
        "chars": [("キャ", "kya"), ("キュ", "kyu"), ("キョ", "kyo"),
                  ("シャ", "sha"), ("シュ", "shu"), ("ショ", "sho"),
                  ("チャ", "cha"), ("チュ", "chu"), ("チョ", "cho"),
                  ("ニャ", "nya"), ("ニュ", "nyu"), ("ニョ", "nyo"),
                  ("ヒャ", "hya"), ("ヒュ", "hyu"), ("ヒョ", "hyo"),
                  ("ミャ", "mya"), ("ミュ", "myu"), ("ミョ", "myo"),
                  ("リャ", "rya"), ("リュ", "ryu"), ("リョ", "ryo"),
                  ("ギャ", "gya"), ("ギュ", "gyu"), ("ギョ", "gyo"),
                  ("ジャ", "ja"), ("ジュ", "ju"), ("ジョ", "jo"),
                  ("ビャ", "bya"), ("ビュ", "byu"), ("ビョ", "byo"),
                  ("ピャ", "pya"), ("ピュ", "pyu"), ("ピョ", "pyo")],
        "words": [
            {"kana": "シャツ", "romaji": "shatsu", "vn": "áo sơ mi", "emoji": "👕"},
            {"kana": "ジュース", "romaji": "juusu", "vn": "nước ép", "emoji": "🧃"},
            {"kana": "チョコレート", "romaji": "chokoreeto", "vn": "sô cô la", "emoji": "🍫"},
            {"kana": "コップ", "romaji": "koppu", "vn": "cái cốc", "emoji": "🥛"},
            {"kana": "スポーツ", "romaji": "supootsu", "vn": "thể thao", "emoji": "⚽"},
        ],
    },
]
