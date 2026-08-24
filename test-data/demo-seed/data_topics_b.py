# -*- coding: utf-8 -*-
"""Chuong trinh hoc chu de 8 -> 12: thoi gian, dong tu, an uong, gia dinh, tinh tu."""

TOPICS_B = [
    # =======================================================================
    {
        "title": "Thời gian và lịch",
        "desc": "Xem giờ, hẹn giờ, nói thứ ngày tháng. Người Nhật cực kỳ đúng giờ nên đây là "
                "chủ đề bắt buộc phải chắc trước khi đi làm hay đi học ở Nhật.",
        "lessons": [
            {
                "title": "Bài 1: Xem giờ (〜じ)",
                "desc": "Ghép số + じ là ra giờ. Nhưng có 3 giờ đọc bất quy tắc phải học thuộc lòng: "
                        "4 giờ là よじ (không phải よんじ), 7 giờ là しちじ, 9 giờ là くじ (không phải きゅうじ).",
                "vocab": [
                    {"kana": "いちじ", "romaji": "ichiji", "vn": "1 giờ", "emoji": "🕐"},
                    {"kana": "さんじ", "romaji": "sanji", "vn": "3 giờ", "emoji": "🕒"},
                    {"kana": "よじ", "romaji": "yoji", "vn": "4 giờ (bất quy tắc)", "emoji": "🕓"},
                    {"kana": "しちじ", "romaji": "shichiji", "vn": "7 giờ (bất quy tắc)", "emoji": "🕖"},
                    {"kana": "くじ", "romaji": "kuji", "vn": "9 giờ (bất quy tắc)", "emoji": "🕘"},
                    {"kana": "じゅうにじ", "romaji": "juuniji", "vn": "12 giờ", "emoji": "🕛"},
                    {"kana": "なんじ", "romaji": "nanji", "vn": "mấy giờ", "emoji": "❓"},
                ],
                "sentences": [
                    {"jp": "いまなんじですか。", "romaji": "Ima nanji desu ka.",
                     "vn": "Bây giờ là mấy giờ?", "blocks": ["いま", "なんじ", "ですか"]},
                    {"jp": "くじです。", "romaji": "Kuji desu.",
                     "vn": "Là 9 giờ.", "blocks": ["くじ", "です"]},
                ],
            },
            {
                "title": "Bài 2: Phút và giờ rưỡi",
                "desc": "Đuôi phút biến âm theo số đứng trước: いっぷん, にふん, さんぷん, よんぷん, ごふん. "
                        "Quy luật chung là sau các số 1, 3, 6, 8, 10 thì đọc ぷん. Giờ rưỡi dùng はん "
                        "cho gọn: しちじはん = 7 giờ rưỡi.",
                "vocab": [
                    {"kana": "ごふん", "romaji": "gofun", "vn": "5 phút", "emoji": "⏱️"},
                    {"kana": "じゅっぷん", "romaji": "juppun", "vn": "10 phút", "emoji": "⏱️"},
                    {"kana": "さんじゅっぷん", "romaji": "sanjuppun", "vn": "30 phút", "emoji": "⏱️"},
                    {"kana": "はん", "romaji": "han", "vn": "rưỡi", "emoji": "🕧"},
                    {"kana": "なんぷん", "romaji": "nanpun", "vn": "bao nhiêu phút", "emoji": "❓"},
                    {"kana": "いま", "romaji": "ima", "vn": "bây giờ", "emoji": "⏰"},
                ],
                "sentences": [
                    {"jp": "しちじはんです。", "romaji": "Shichiji han desu.",
                     "vn": "Bây giờ là 7 giờ rưỡi.", "blocks": ["しちじ", "はん", "です"]},
                    {"jp": "さんじじゅっぷんです。", "romaji": "Sanji juppun desu.",
                     "vn": "Bây giờ là 3 giờ 10 phút.", "blocks": ["さんじ", "じゅっぷん", "です"]},
                ],
            },
            {
                "title": "Bài 3: Buổi trong ngày, hôm qua và ngày mai",
                "desc": "Nhóm từ chỉ mốc thời gian tương đối. Chú ý あさ (buổi sáng) khác với "
                        "あした (ngày mai) chỉ 1 chữ — người mới học rất hay nhầm 2 từ này.",
                "vocab": [
                    {"kana": "あさ", "romaji": "asa", "vn": "buổi sáng", "emoji": "🌅"},
                    {"kana": "ひる", "romaji": "hiru", "vn": "buổi trưa", "emoji": "☀️"},
                    {"kana": "よる", "romaji": "yoru", "vn": "buổi tối", "emoji": "🌃"},
                    {"kana": "きょう", "romaji": "kyou", "vn": "hôm nay", "emoji": "📅"},
                    {"kana": "あした", "romaji": "ashita", "vn": "ngày mai", "emoji": "➡️"},
                    {"kana": "きのう", "romaji": "kinou", "vn": "hôm qua", "emoji": "⬅️"},
                    {"kana": "まいにち", "romaji": "mainichi", "vn": "hằng ngày", "emoji": "🔁"},
                ],
                "sentences": [
                    {"jp": "まいにちあさろくじにおきます。", "romaji": "Mainichi asa rokuji ni okimasu.",
                     "vn": "Hằng ngày tôi dậy lúc 6 giờ sáng.", "blocks": ["まいにち", "あさ", "ろくじ", "に", "おきます"]},
                ],
            },
            {
                "title": "Bài 4: Thứ trong tuần",
                "desc": "Tên các thứ đặt theo ngũ hành và thiên thể: nguyệt (trăng), hỏa, thủy, mộc, kim, thổ, "
                        "nhật (mặt trời). Đều kết thúc bằng ようび nên chỉ cần nhớ chữ đầu.",
                "vocab": [
                    {"kana": "げつようび", "romaji": "getsuyoubi", "vn": "thứ Hai", "emoji": "🌙"},
                    {"kana": "かようび", "romaji": "kayoubi", "vn": "thứ Ba", "emoji": "🔥"},
                    {"kana": "すいようび", "romaji": "suiyoubi", "vn": "thứ Tư", "emoji": "💧"},
                    {"kana": "もくようび", "romaji": "mokuyoubi", "vn": "thứ Năm", "emoji": "🌳"},
                    {"kana": "きんようび", "romaji": "kinyoubi", "vn": "thứ Sáu", "emoji": "💰"},
                    {"kana": "どようび", "romaji": "doyoubi", "vn": "thứ Bảy", "emoji": "🏔️"},
                    {"kana": "にちようび", "romaji": "nichiyoubi", "vn": "Chủ nhật", "emoji": "☀️"},
                    {"kana": "なんようび", "romaji": "nanyoubi", "vn": "thứ mấy", "emoji": "❓"},
                ],
                "sentences": [
                    {"jp": "きょうはなんようびですか。", "romaji": "Kyou wa nanyoubi desu ka.",
                     "vn": "Hôm nay là thứ mấy?", "blocks": ["きょう", "は", "なんようび", "ですか"]},
                    {"jp": "きょうはきんようびです。", "romaji": "Kyou wa kinyoubi desu.",
                     "vn": "Hôm nay là thứ Sáu.", "blocks": ["きょう", "は", "きんようび", "です"]},
                ],
            },
            {
                "title": "Bài 5: Tháng và ngày",
                "desc": "Tháng rất dễ: số + がつ (いちがつ = tháng 1). Nhưng ngày mùng 1 đến mùng 10 "
                        "đọc hoàn toàn bất quy tắc (ついたち, ふつか, みっか…) — đây là phần khó nhằn "
                        "nhất của lịch tiếng Nhật, cần luyện đi luyện lại.",
                "vocab": [
                    {"kana": "いちがつ", "romaji": "ichigatsu", "vn": "tháng 1", "emoji": "🎍"},
                    {"kana": "しがつ", "romaji": "shigatsu", "vn": "tháng 4 (bất quy tắc)", "emoji": "🌸"},
                    {"kana": "しちがつ", "romaji": "shichigatsu", "vn": "tháng 7 (bất quy tắc)", "emoji": "🎋"},
                    {"kana": "くがつ", "romaji": "kugatsu", "vn": "tháng 9 (bất quy tắc)", "emoji": "🍁"},
                    {"kana": "ついたち", "romaji": "tsuitachi", "vn": "ngày mùng 1", "emoji": "1️⃣"},
                    {"kana": "ふつか", "romaji": "futsuka", "vn": "ngày mùng 2", "emoji": "2️⃣"},
                    {"kana": "はつか", "romaji": "hatsuka", "vn": "ngày 20", "emoji": "🔢"},
                    {"kana": "たんじょうび", "romaji": "tanjoubi", "vn": "sinh nhật", "emoji": "🎂"},
                ],
                "sentences": [
                    {"jp": "たんじょうびはしがつついたちです。", "romaji": "Tanjoubi wa shigatsu tsuitachi desu.",
                     "vn": "Sinh nhật tôi là ngày 1 tháng 4.", "blocks": ["たんじょうび", "は", "しがつ", "ついたち", "です"]},
                ],
            },
            {
                "title": "Bài 6: Từ… đến… (〜から〜まで)",
                "desc": "から = từ (điểm bắt đầu), まで = đến (điểm kết thúc). Dùng được cho cả thời gian "
                        "lẫn địa điểm: くじからごじまで (từ 9 giờ đến 5 giờ), とうきょうからおおさかまで "
                        "(từ Tokyo đến Osaka).",
                "vocab": [
                    {"kana": "から", "romaji": "kara", "vn": "từ…", "emoji": "🅰️"},
                    {"kana": "まで", "romaji": "made", "vn": "đến…", "emoji": "🅱️"},
                    {"kana": "やすみ", "romaji": "yasumi", "vn": "ngày nghỉ", "emoji": "🏖️"},
                    {"kana": "しごと", "romaji": "shigoto", "vn": "công việc", "emoji": "💼"},
                ],
                "sentences": [
                    {"jp": "しごとはくじからごじまでです。", "romaji": "Shigoto wa kuji kara goji made desu.",
                     "vn": "Công việc từ 9 giờ đến 5 giờ.", "blocks": ["しごと", "は", "くじ", "から", "ごじ", "まで", "です"]},
                    {"jp": "にちようびはやすみです。", "romaji": "Nichiyoubi wa yasumi desu.",
                     "vn": "Chủ nhật là ngày nghỉ.", "blocks": ["にちようび", "は", "やすみ", "です"]},
                ],
            },
        ],
    },
    # =======================================================================
    {
        "title": "Động từ và sinh hoạt hằng ngày",
        "desc": "Động từ thể ます — thể lịch sự chuẩn mực để nói chuyện với bất kỳ ai. Chủ đề này "
                "cũng dạy 4 trợ từ quan trọng nhất khi ghép câu có động từ: を, へ, で, に.",
        "lessons": [
            {
                "title": "Bài 1: Động từ đầu tiên",
                "desc": "Điểm khác biệt lớn nhất với tiếng Việt: động từ tiếng Nhật luôn đứng CUỐI CÂU. "
                        "Tôi ăn cơm trong tiếng Nhật là Tôi cơm ăn. Đuôi ます làm câu thành lịch sự "
                        "và mang nghĩa hiện tại hoặc tương lai.",
                "vocab": [
                    {"kana": "おきます", "romaji": "okimasu", "vn": "thức dậy", "emoji": "⏰"},
                    {"kana": "ねます", "romaji": "nemasu", "vn": "đi ngủ", "emoji": "😴"},
                    {"kana": "たべます", "romaji": "tabemasu", "vn": "ăn", "emoji": "🍚"},
                    {"kana": "のみます", "romaji": "nomimasu", "vn": "uống", "emoji": "🥤"},
                    {"kana": "みます", "romaji": "mimasu", "vn": "xem, nhìn", "emoji": "👀"},
                    {"kana": "ききます", "romaji": "kikimasu", "vn": "nghe", "emoji": "🎧"},
                ],
                "sentences": [
                    {"jp": "わたしはねます。", "romaji": "Watashi wa nemasu.",
                     "vn": "Tôi đi ngủ.", "blocks": ["わたし", "は", "ねます"]},
                ],
            },
            {
                "title": "Bài 2: Đi, đến, về nhà",
                "desc": "Bộ 3 động từ di chuyển đi kèm trợ từ へ (chỉ hướng, viết là へ nhưng đọc là E). "
                        "Cấu trúc: [nơi đến] へ いきます. Có thể thay へ bằng に, nghĩa gần như nhau.",
                "vocab": [
                    {"kana": "いきます", "romaji": "ikimasu", "vn": "đi", "emoji": "🚶"},
                    {"kana": "きます", "romaji": "kimasu", "vn": "đến", "emoji": "🏃"},
                    {"kana": "かえります", "romaji": "kaerimasu", "vn": "về nhà", "emoji": "🏠"},
                    {"kana": "へ", "romaji": "e", "vn": "đến (chỉ hướng)", "emoji": "➡️"},
                    {"kana": "でんしゃ", "romaji": "densha", "vn": "tàu điện", "emoji": "🚃"},
                ],
                "sentences": [
                    {"jp": "がっこうへいきます。", "romaji": "Gakkou e ikimasu.",
                     "vn": "Tôi đi đến trường.", "blocks": ["がっこう", "へ", "いきます"]},
                    {"jp": "うちへかえります。", "romaji": "Uchi e kaerimasu.",
                     "vn": "Tôi về nhà.", "blocks": ["うち", "へ", "かえります"]},
                ],
            },
            {
                "title": "Bài 3: Trợ từ を (tân ngữ)",
                "desc": "を đánh dấu đối tượng chịu tác động của động từ. Chữ を chỉ dùng đúng cho "
                        "việc này, không bao giờ dùng để viết từ, và đọc là O chứ không phải WO. "
                        "Cấu trúc: [vật] を [động từ].",
                "vocab": [
                    {"kana": "を", "romaji": "wo", "vn": "trợ từ tân ngữ", "emoji": "🎯"},
                    {"kana": "ごはん", "romaji": "gohan", "vn": "cơm, bữa ăn", "emoji": "🍚"},
                    {"kana": "みず", "romaji": "mizu", "vn": "nước", "emoji": "💧"},
                    {"kana": "テレビ", "romaji": "terebi", "vn": "ti vi", "emoji": "📺"},
                    {"kana": "おんがく", "romaji": "ongaku", "vn": "âm nhạc", "emoji": "🎵"},
                ],
                "sentences": [
                    {"jp": "ごはんをたべます。", "romaji": "Gohan wo tabemasu.",
                     "vn": "Tôi ăn cơm.", "blocks": ["ごはん", "を", "たべます"]},
                    {"jp": "おんがくをききます。", "romaji": "Ongaku wo kikimasu.",
                     "vn": "Tôi nghe nhạc.", "blocks": ["おんがく", "を", "ききます"]},
                ],
            },
            {
                "title": "Bài 4: Trợ từ で (nơi diễn ra hành động)",
                "desc": "Phân biệt cực quan trọng: に chỉ nơi TỒN TẠI (へやにいます = ở trong phòng), "
                        "còn で chỉ nơi DIỄN RA HÀNH ĐỘNG (へやでべんきょうします = học trong phòng). "
                        "Dùng nhầm 2 trợ từ này là lỗi kinh điển của người mới.",
                "vocab": [
                    {"kana": "で", "romaji": "de", "vn": "tại (nơi làm gì đó)", "emoji": "📍"},
                    {"kana": "べんきょうします", "romaji": "benkyou shimasu", "vn": "học bài", "emoji": "📚"},
                    {"kana": "はたらきます", "romaji": "hatarakimasu", "vn": "làm việc", "emoji": "💼"},
                    {"kana": "かいます", "romaji": "kaimasu", "vn": "mua", "emoji": "🛒"},
                    {"kana": "よみます", "romaji": "yomimasu", "vn": "đọc", "emoji": "📖"},
                ],
                "sentences": [
                    {"jp": "としょかんでべんきょうします。", "romaji": "Toshokan de benkyou shimasu.",
                     "vn": "Tôi học bài ở thư viện.", "blocks": ["としょかん", "で", "べんきょうします"]},
                    {"jp": "コンビニでパンをかいます。", "romaji": "Konbini de pan wo kaimasu.",
                     "vn": "Tôi mua bánh mì ở cửa hàng tiện lợi.", "blocks": ["コンビニ", "で", "パン", "を", "かいます"]},
                ],
            },
            {
                "title": "Bài 5: Quá khứ và phủ định",
                "desc": "Chia động từ tiếng Nhật cực kỳ đều đặn, chỉ cần đổi đuôi ます: "
                        "hiện tại ます → phủ định ません → quá khứ ました → quá khứ phủ định ませんでした. "
                        "Không đổi theo ngôi hay số nhiều như tiếng Anh.",
                "vocab": [
                    {"kana": "ません", "romaji": "masen", "vn": "không… (phủ định)", "emoji": "🚫"},
                    {"kana": "ました", "romaji": "mashita", "vn": "đã… (quá khứ)", "emoji": "⏮️"},
                    {"kana": "ませんでした", "romaji": "masen deshita", "vn": "đã không…", "emoji": "❎"},
                ],
                "sentences": [
                    {"jp": "きのうがっこうへいきました。", "romaji": "Kinou gakkou e ikimashita.",
                     "vn": "Hôm qua tôi đã đến trường.", "blocks": ["きのう", "がっこう", "へ", "いきました"]},
                    {"jp": "コーヒーをのみません。", "romaji": "Koohii wo nomimasen.",
                     "vn": "Tôi không uống cà phê.", "blocks": ["コーヒー", "を", "のみません"]},
                ],
            },
            {
                "title": "Bài 6: Trợ từ に (mốc thời gian)",
                "desc": "に đứng sau mốc thời gian CÓ CON SỐ: しちじにおきます (dậy lúc 7 giờ). "
                        "Nhưng các từ như きょう, あした, まいにち thì KHÔNG dùng に — nói あしたにいきます "
                        "là sai, phải nói あしたいきます.",
                "vocab": [
                    {"kana": "に", "romaji": "ni", "vn": "vào lúc (thời điểm)", "emoji": "🕐"},
                    {"kana": "あさごはん", "romaji": "asagohan", "vn": "bữa sáng", "emoji": "🍳"},
                    {"kana": "ひるごはん", "romaji": "hirugohan", "vn": "bữa trưa", "emoji": "🍱"},
                    {"kana": "ばんごはん", "romaji": "bangohan", "vn": "bữa tối", "emoji": "🍲"},
                ],
                "sentences": [
                    {"jp": "しちじにあさごはんをたべます。", "romaji": "Shichiji ni asagohan wo tabemasu.",
                     "vn": "Tôi ăn sáng lúc 7 giờ.", "blocks": ["しちじ", "に", "あさごはん", "を", "たべます"]},
                    {"jp": "じゅうじにねます。", "romaji": "Juuji ni nemasu.",
                     "vn": "Tôi đi ngủ lúc 10 giờ.", "blocks": ["じゅうじ", "に", "ねます"]},
                ],
            },
            {
                "title": "Bài 7: Kể về một ngày của bạn",
                "desc": "Bài tổng hợp: ghép tất cả trợ từ và động từ đã học thành một đoạn kể liền mạch. "
                        "Trật tự câu chuẩn của tiếng Nhật là: [Thời gian] [Nơi chốn] [Tân ngữ] [Động từ].",
                "vocab": [
                    {"kana": "それから", "romaji": "sorekara", "vn": "sau đó", "emoji": "➡️"},
                    {"kana": "まいあさ", "romaji": "maiasa", "vn": "mỗi buổi sáng", "emoji": "🌅"},
                    {"kana": "うち", "romaji": "uchi", "vn": "nhà (của mình)", "emoji": "🏠"},
                    {"kana": "としょかん", "romaji": "toshokan", "vn": "thư viện", "emoji": "📚"},
                ],
                "sentences": [
                    {"jp": "まいあさろくじにおきます。", "romaji": "Maiasa rokuji ni okimasu.",
                     "vn": "Mỗi sáng tôi dậy lúc 6 giờ.", "blocks": ["まいあさ", "ろくじ", "に", "おきます"]},
                    {"jp": "それからがっこうへいきます。", "romaji": "Sorekara gakkou e ikimasu.",
                     "vn": "Sau đó tôi đi đến trường.", "blocks": ["それから", "がっこう", "へ", "いきます"]},
                    {"jp": "よるうちでばんごはんをたべます。", "romaji": "Yoru uchi de bangohan wo tabemasu.",
                     "vn": "Buổi tối tôi ăn cơm tối ở nhà.", "blocks": ["よる", "うち", "で", "ばんごはん", "を", "たべます"]},
                ],
            },
        ],
    },
    # =======================================================================
    {
        "title": "Ăn uống và nhà hàng",
        "desc": "Từ vựng món ăn, đồ uống và trọn bộ mẫu câu để gọi món, hỏi giá, khen ngon. "
                "Chủ đề thực dụng nhất — dùng được ngay trong bữa ăn đầu tiên ở Nhật.",
        "lessons": [
            {
                "title": "Bài 1: Thức ăn cơ bản",
                "desc": "Nhóm từ nền tảng trong mọi thực đơn. ごはん vừa có nghĩa là cơm trắng, "
                        "vừa có nghĩa là bữa ăn nói chung — tuỳ ngữ cảnh mà hiểu.",
                "vocab": [
                    {"kana": "ごはん", "romaji": "gohan", "vn": "cơm", "emoji": "🍚"},
                    {"kana": "パン", "romaji": "pan", "vn": "bánh mì", "emoji": "🍞"},
                    {"kana": "にく", "romaji": "niku", "vn": "thịt", "emoji": "🍖"},
                    {"kana": "さかな", "romaji": "sakana", "vn": "cá", "emoji": "🐟"},
                    {"kana": "たまご", "romaji": "tamago", "vn": "trứng", "emoji": "🥚"},
                    {"kana": "やさい", "romaji": "yasai", "vn": "rau củ", "emoji": "🥬"},
                    {"kana": "くだもの", "romaji": "kudamono", "vn": "trái cây", "emoji": "🍎"},
                ],
                "sentences": [
                    {"jp": "あさごはんにパンをたべます。", "romaji": "Asagohan ni pan wo tabemasu.",
                     "vn": "Bữa sáng tôi ăn bánh mì.", "blocks": ["あさごはん", "に", "パン", "を", "たべます"]},
                ],
            },
            {
                "title": "Bài 2: Đồ uống",
                "desc": "Chữ お ở đầu おちゃ, おさけ là tiếp đầu ngữ lịch sự — bỏ đi vẫn đúng nghĩa "
                        "nhưng nghe kém trang trọng. Người Nhật hầu như luôn nói kèm お cho 2 từ này.",
                "vocab": [
                    {"kana": "みず", "romaji": "mizu", "vn": "nước lọc", "emoji": "💧"},
                    {"kana": "おちゃ", "romaji": "ocha", "vn": "trà", "emoji": "🍵"},
                    {"kana": "コーヒー", "romaji": "koohii", "vn": "cà phê", "emoji": "☕"},
                    {"kana": "ぎゅうにゅう", "romaji": "gyuunyuu", "vn": "sữa bò", "emoji": "🥛"},
                    {"kana": "ジュース", "romaji": "juusu", "vn": "nước ép", "emoji": "🧃"},
                    {"kana": "ビール", "romaji": "biiru", "vn": "bia", "emoji": "🍺"},
                    {"kana": "おさけ", "romaji": "osake", "vn": "rượu", "emoji": "🍶"},
                ],
                "sentences": [
                    {"jp": "おちゃをのみます。", "romaji": "Ocha wo nomimasu.",
                     "vn": "Tôi uống trà.", "blocks": ["おちゃ", "を", "のみます"]},
                ],
            },
            {
                "title": "Bài 3: Món ăn Nhật Bản",
                "desc": "Những món bạn sẽ thấy ở mọi nhà hàng Nhật. おにぎり là món ăn nhanh quốc dân, "
                        "bán ở mọi cửa hàng tiện lợi với giá chỉ hơn 100 yên.",
                "vocab": [
                    {"kana": "すし", "romaji": "sushi", "vn": "sushi", "emoji": "🍣"},
                    {"kana": "ラーメン", "romaji": "raamen", "vn": "mì ramen", "emoji": "🍜"},
                    {"kana": "うどん", "romaji": "udon", "vn": "mì udon", "emoji": "🍲"},
                    {"kana": "てんぷら", "romaji": "tenpura", "vn": "món tempura chiên", "emoji": "🍤"},
                    {"kana": "おにぎり", "romaji": "onigiri", "vn": "cơm nắm", "emoji": "🍙"},
                    {"kana": "カレー", "romaji": "karee", "vn": "cà ri", "emoji": "🍛"},
                    {"kana": "べんとう", "romaji": "bentou", "vn": "cơm hộp", "emoji": "🍱"},
                ],
                "sentences": [
                    {"jp": "すしがすきです。", "romaji": "Sushi ga suki desu.",
                     "vn": "Tôi thích sushi.", "blocks": ["すし", "が", "すき", "です"]},
                ],
            },
            {
                "title": "Bài 4: Gọi món trong nhà hàng",
                "desc": "2 cách gọi món: 〜をください (cho tôi…) dùng khi chỉ vào món cụ thể, còn "
                        "〜をおねがいします lịch sự hơn một bậc, dùng được cả khi nhờ vả việc khác.",
                "vocab": [
                    {"kana": "メニュー", "romaji": "menyuu", "vn": "thực đơn", "emoji": "📋"},
                    {"kana": "ちゅうもん", "romaji": "chuumon", "vn": "gọi món, đặt hàng", "emoji": "✍️"},
                    {"kana": "おねがいします", "romaji": "onegaishimasu", "vn": "làm ơn cho tôi…", "emoji": "🙏"},
                    {"kana": "おかいけい", "romaji": "okaikei", "vn": "tính tiền", "emoji": "🧾"},
                    {"kana": "みせのひと", "romaji": "mise no hito", "vn": "nhân viên cửa hàng", "emoji": "🧑‍💼"},
                ],
                "sentences": [
                    {"jp": "メニューをおねがいします。", "romaji": "Menyuu wo onegaishimasu.",
                     "vn": "Cho tôi xin thực đơn.", "blocks": ["メニュー", "を", "おねがいします"]},
                    {"jp": "ラーメンをひとつください。", "romaji": "Raamen wo hitotsu kudasai.",
                     "vn": "Cho tôi một bát mì ramen.", "blocks": ["ラーメン", "を", "ひとつ", "ください"]},
                    {"jp": "おかいけいをおねがいします。", "romaji": "Okaikei wo onegaishimasu.",
                     "vn": "Làm ơn tính tiền giúp tôi.", "blocks": ["おかいけい", "を", "おねがいします"]},
                ],
            },
            {
                "title": "Bài 5: Nói về mùi vị",
                "desc": "おいしい (ngon) là lời khen được dùng nhiều nhất trên bàn ăn Nhật. "
                        "Ngược lại まずい (dở) nghe khá thô lỗ nếu nói trước mặt người nấu — "
                        "người Nhật thường tránh né bằng cách nói ちょっと… (hơi… ).",
                "vocab": [
                    {"kana": "おいしい", "romaji": "oishii", "vn": "ngon", "emoji": "😋"},
                    {"kana": "まずい", "romaji": "mazui", "vn": "dở, không ngon", "emoji": "😖"},
                    {"kana": "あまい", "romaji": "amai", "vn": "ngọt", "emoji": "🍬"},
                    {"kana": "からい", "romaji": "karai", "vn": "cay", "emoji": "🌶️"},
                    {"kana": "しょっぱい", "romaji": "shoppai", "vn": "mặn", "emoji": "🧂"},
                    {"kana": "つめたい", "romaji": "tsumetai", "vn": "lạnh (đồ uống)", "emoji": "🧊"},
                    {"kana": "あつい", "romaji": "atsui", "vn": "nóng", "emoji": "♨️"},
                ],
                "sentences": [
                    {"jp": "このラーメンはおいしいです。", "romaji": "Kono raamen wa oishii desu.",
                     "vn": "Bát mì ramen này ngon.", "blocks": ["この", "ラーメン", "は", "おいしい", "です"]},
                    {"jp": "カレーはちょっとからいです。", "romaji": "Karee wa chotto karai desu.",
                     "vn": "Món cà ri hơi cay một chút.", "blocks": ["カレー", "は", "ちょっと", "からい", "です"]},
                ],
            },
            {
                "title": "Bài 6: Hội thoại trong nhà hàng",
                "desc": "Ghép toàn bộ mẫu câu đã học thành một cuộc hội thoại hoàn chỉnh từ lúc bước vào "
                        "đến lúc trả tiền. いらっしゃいませ là câu nhân viên chào khách — bạn không cần "
                        "đáp lại, chỉ cần gật đầu.",
                "vocab": [
                    {"kana": "いらっしゃいませ", "romaji": "irasshaimase", "vn": "kính chào quý khách", "emoji": "🙇"},
                    {"kana": "なんめいさま", "romaji": "nanmeisama", "vn": "quý khách mấy người", "emoji": "👥"},
                    {"kana": "しょうしょうおまちください", "romaji": "shoushou omachi kudasai", "vn": "xin đợi một lát", "emoji": "⏳"},
                    {"kana": "げんきん", "romaji": "genkin", "vn": "tiền mặt", "emoji": "💴"},
                    {"kana": "カード", "romaji": "kaado", "vn": "thẻ thanh toán", "emoji": "💳"},
                ],
                "sentences": [
                    {"jp": "いらっしゃいませ。なんめいさまですか。", "romaji": "Irasshaimase. Nanmeisama desu ka.",
                     "vn": "Kính chào quý khách. Quý khách đi mấy người ạ?", "blocks": ["いらっしゃいませ", "。", "なんめいさま", "ですか"]},
                    {"jp": "ふたりです。", "romaji": "Futari desu.",
                     "vn": "Hai người ạ.", "blocks": ["ふたり", "です"]},
                    {"jp": "カードでおねがいします。", "romaji": "Kaado de onegaishimasu.",
                     "vn": "Cho tôi thanh toán bằng thẻ.", "blocks": ["カード", "で", "おねがいします"]},
                ],
            },
        ],
    },
    # =======================================================================
    {
        "title": "Gia đình và con người",
        "desc": "Tiếng Nhật có 2 bộ từ gia đình riêng biệt: một bộ dùng khi nói về nhà mình, "
                "một bộ dùng khi nói về nhà người khác. Dùng nhầm bộ là lỗi lễ nghi khá nặng.",
        "lessons": [
            {
                "title": "Bài 1: Gia đình của tôi",
                "desc": "Bộ từ KHIÊM NHƯỜNG, chỉ dùng khi nói về người nhà MÌNH với người ngoài. "
                        "Người Nhật hạ thấp gia đình mình để đề cao người đối diện — nguyên tắc "
                        "giao tiếp quan trọng nhất của xã hội Nhật.",
                "vocab": [
                    {"kana": "かぞく", "romaji": "kazoku", "vn": "gia đình", "emoji": "👨‍👩‍👧‍👦"},
                    {"kana": "ちち", "romaji": "chichi", "vn": "bố tôi", "emoji": "👨"},
                    {"kana": "はは", "romaji": "haha", "vn": "mẹ tôi", "emoji": "👩"},
                    {"kana": "あに", "romaji": "ani", "vn": "anh trai tôi", "emoji": "👦"},
                    {"kana": "あね", "romaji": "ane", "vn": "chị gái tôi", "emoji": "👧"},
                    {"kana": "おとうと", "romaji": "otouto", "vn": "em trai tôi", "emoji": "🧒"},
                    {"kana": "いもうと", "romaji": "imouto", "vn": "em gái tôi", "emoji": "👶"},
                ],
                "sentences": [
                    {"jp": "ちちはかいしゃいんです。", "romaji": "Chichi wa kaishain desu.",
                     "vn": "Bố tôi là nhân viên công ty.", "blocks": ["ちち", "は", "かいしゃいん", "です"]},
                    {"jp": "いもうとはがくせいです。", "romaji": "Imouto wa gakusei desu.",
                     "vn": "Em gái tôi là học sinh.", "blocks": ["いもうと", "は", "がくせい", "です"]},
                ],
            },
            {
                "title": "Bài 2: Gia đình của người khác",
                "desc": "Bộ từ KÍNH TRỌNG, dùng khi hỏi hoặc nhắc tới người nhà của người khác. "
                        "Dễ nhận ra vì luôn có お ở đầu và さん ở cuối: おとうさん, おかあさん. "
                        "Đây cũng là cách trẻ con Nhật gọi bố mẹ mình.",
                "vocab": [
                    {"kana": "おとうさん", "romaji": "otousan", "vn": "bố (của bạn)", "emoji": "👨"},
                    {"kana": "おかあさん", "romaji": "okaasan", "vn": "mẹ (của bạn)", "emoji": "👩"},
                    {"kana": "おにいさん", "romaji": "oniisan", "vn": "anh trai (của bạn)", "emoji": "👦"},
                    {"kana": "おねえさん", "romaji": "oneesan", "vn": "chị gái (của bạn)", "emoji": "👧"},
                    {"kana": "ごかぞく", "romaji": "gokazoku", "vn": "gia đình (của bạn)", "emoji": "👨‍👩‍👧"},
                ],
                "sentences": [
                    {"jp": "おとうさんはおいくつですか。", "romaji": "Otousan wa oikutsu desu ka.",
                     "vn": "Bố bạn bao nhiêu tuổi ạ?", "blocks": ["おとうさん", "は", "おいくつ", "ですか"]},
                ],
            },
            {
                "title": "Bài 3: Người xung quanh",
                "desc": "Từ vựng gọi người theo vai trò xã hội. Lưu ý ひと (người) là từ trung tính, "
                        "còn かた là cách gọi lịch sự hơn, dùng khi nói về người trên hoặc khách.",
                "vocab": [
                    {"kana": "ひと", "romaji": "hito", "vn": "người", "emoji": "🧍"},
                    {"kana": "こども", "romaji": "kodomo", "vn": "trẻ con", "emoji": "🧒"},
                    {"kana": "おとこのひと", "romaji": "otoko no hito", "vn": "người đàn ông", "emoji": "👨"},
                    {"kana": "おんなのひと", "romaji": "onna no hito", "vn": "người phụ nữ", "emoji": "👩"},
                    {"kana": "ともだち", "romaji": "tomodachi", "vn": "bạn bè", "emoji": "👫"},
                    {"kana": "どうりょう", "romaji": "douryou", "vn": "đồng nghiệp", "emoji": "🤝"},
                ],
                "sentences": [
                    {"jp": "あのおんなのひとはせんせいです。", "romaji": "Ano onna no hito wa sensei desu.",
                     "vn": "Người phụ nữ kia là cô giáo.", "blocks": ["あの", "おんなのひと", "は", "せんせい", "です"]},
                ],
            },
            {
                "title": "Bài 4: Gia đình bạn có mấy người?",
                "desc": "Đếm người dùng đuôi にん, nhưng 1 người và 2 người bất quy tắc: ひとり, ふたり. "
                        "Từ 3 người trở đi mới đều: さんにん, よにん, ごにん.",
                "vocab": [
                    {"kana": "なんにん", "romaji": "nannin", "vn": "mấy người", "emoji": "❓"},
                    {"kana": "さんにん", "romaji": "sannin", "vn": "ba người", "emoji": "👨‍👩‍👦"},
                    {"kana": "よにん", "romaji": "yonin", "vn": "bốn người", "emoji": "👨‍👩‍👧‍👦"},
                    {"kana": "ごにん", "romaji": "gonin", "vn": "năm người", "emoji": "👨‍👩‍👧‍👧"},
                ],
                "sentences": [
                    {"jp": "なんにんかぞくですか。", "romaji": "Nannin kazoku desu ka.",
                     "vn": "Gia đình bạn có mấy người?", "blocks": ["なんにん", "かぞく", "ですか"]},
                    {"jp": "よにんかぞくです。", "romaji": "Yonin kazoku desu.",
                     "vn": "Gia đình tôi có bốn người.", "blocks": ["よにん", "かぞく", "です"]},
                    {"jp": "おとうとがふたりいます。", "romaji": "Otouto ga futari imasu.",
                     "vn": "Tôi có hai em trai.", "blocks": ["おとうと", "が", "ふたり", "います"]},
                ],
            },
            {
                "title": "Bài 5: Giới thiệu gia đình",
                "desc": "Bài tổng hợp: ghép từ vựng gia đình với nghề nghiệp, tuổi tác và động từ います "
                        "thành một đoạn giới thiệu hoàn chỉnh — đúng dạng câu hỏi thường gặp nhất "
                        "trong kỳ thi nói JLPT và phỏng vấn.",
                "vocab": [
                    {"kana": "ぜんぶで", "romaji": "zenbu de", "vn": "tổng cộng", "emoji": "➕"},
                    {"kana": "そして", "romaji": "soshite", "vn": "và, ngoài ra", "emoji": "🔗"},
                    {"kana": "いっしょに", "romaji": "issho ni", "vn": "cùng nhau", "emoji": "👥"},
                ],
                "sentences": [
                    {"jp": "わたしのかぞくはよにんです。", "romaji": "Watashi no kazoku wa yonin desu.",
                     "vn": "Gia đình tôi có bốn người.", "blocks": ["わたし", "の", "かぞく", "は", "よにん", "です"]},
                    {"jp": "ちちとははといもうとがいます。", "romaji": "Chichi to haha to imouto ga imasu.",
                     "vn": "Tôi có bố, mẹ và em gái.", "blocks": ["ちち", "と", "はは", "と", "いもうと", "が", "います"]},
                ],
            },
        ],
    },
    # =======================================================================
    {
        "title": "Tính từ và miêu tả",
        "desc": "Tiếng Nhật có 2 loại tính từ chia khác nhau hoàn toàn: tính từ đuôi い và tính từ đuôi な. "
                "Phân biệt được 2 loại này là bạn đã nắm gần trọn ngữ pháp N5.",
        "lessons": [
            {
                "title": "Bài 1: Tính từ đuôi い",
                "desc": "Nhóm tính từ kết thúc bằng chữ い, đứng trực tiếp trước danh từ mà không cần "
                        "thêm gì: たかいほん (quyển sách đắt). Đây là nhóm đông đảo nhất.",
                "vocab": [
                    {"kana": "おおきい", "romaji": "ookii", "vn": "to, lớn", "emoji": "🐘"},
                    {"kana": "ちいさい", "romaji": "chiisai", "vn": "nhỏ, bé", "emoji": "🐜"},
                    {"kana": "たかい", "romaji": "takai", "vn": "cao, đắt", "emoji": "🏔️"},
                    {"kana": "やすい", "romaji": "yasui", "vn": "rẻ", "emoji": "🏷️"},
                    {"kana": "あたらしい", "romaji": "atarashii", "vn": "mới", "emoji": "✨"},
                    {"kana": "ふるい", "romaji": "furui", "vn": "cũ", "emoji": "🏚️"},
                    {"kana": "いい", "romaji": "ii", "vn": "tốt", "emoji": "👍"},
                    {"kana": "むずかしい", "romaji": "muzukashii", "vn": "khó", "emoji": "😣"},
                ],
                "sentences": [
                    {"jp": "このかばんはあたらしいです。", "romaji": "Kono kaban wa atarashii desu.",
                     "vn": "Cái cặp này mới.", "blocks": ["この", "かばん", "は", "あたらしい", "です"]},
                    {"jp": "にほんごはむずかしいです。", "romaji": "Nihongo wa muzukashii desu.",
                     "vn": "Tiếng Nhật thì khó.", "blocks": ["にほんご", "は", "むずかしい", "です"]},
                ],
            },
            {
                "title": "Bài 2: Tính từ đuôi な",
                "desc": "Nhóm này phải THÊM な khi đứng trước danh từ: きれいなはな (bông hoa đẹp), "
                        "nhưng khi đứng cuối câu thì bỏ な: このはなはきれいです. "
                        "Bẫy thường gặp: きれい và ゆうめい tuy kết thúc bằng い nhưng vẫn là tính từ な.",
                "vocab": [
                    {"kana": "きれい", "romaji": "kirei", "vn": "đẹp, sạch sẽ", "emoji": "🌸"},
                    {"kana": "しずか", "romaji": "shizuka", "vn": "yên tĩnh", "emoji": "🤫"},
                    {"kana": "にぎやか", "romaji": "nigiyaka", "vn": "nhộn nhịp", "emoji": "🎊"},
                    {"kana": "ゆうめい", "romaji": "yuumei", "vn": "nổi tiếng", "emoji": "⭐"},
                    {"kana": "げんき", "romaji": "genki", "vn": "khỏe mạnh", "emoji": "💪"},
                    {"kana": "べんり", "romaji": "benri", "vn": "tiện lợi", "emoji": "🔧"},
                    {"kana": "すき", "romaji": "suki", "vn": "thích", "emoji": "❤️"},
                ],
                "sentences": [
                    {"jp": "このこうえんはしずかです。", "romaji": "Kono kouen wa shizuka desu.",
                     "vn": "Công viên này yên tĩnh.", "blocks": ["この", "こうえん", "は", "しずか", "です"]},
                    {"jp": "きれいなはなですね。", "romaji": "Kirei na hana desu ne.",
                     "vn": "Bông hoa đẹp nhỉ.", "blocks": ["きれい", "な", "はな", "です", "ね"]},
                ],
            },
            {
                "title": "Bài 3: Màu sắc",
                "desc": "4 màu cơ bản あかい, あおい, しろい, くろい là tính từ đuôi い. Nhưng các màu "
                        "còn lại như みどり, ちゃいろ lại là danh từ, phải thêm の khi bổ nghĩa: "
                        "みどりのくるま (chiếc xe màu xanh lá).",
                "vocab": [
                    {"kana": "あかい", "romaji": "akai", "vn": "màu đỏ", "emoji": "🔴"},
                    {"kana": "あおい", "romaji": "aoi", "vn": "màu xanh dương", "emoji": "🔵"},
                    {"kana": "しろい", "romaji": "shiroi", "vn": "màu trắng", "emoji": "⚪"},
                    {"kana": "くろい", "romaji": "kuroi", "vn": "màu đen", "emoji": "⚫"},
                    {"kana": "きいろい", "romaji": "kiiroi", "vn": "màu vàng", "emoji": "🟡"},
                    {"kana": "みどり", "romaji": "midori", "vn": "màu xanh lá", "emoji": "🟢"},
                    {"kana": "ちゃいろ", "romaji": "chairo", "vn": "màu nâu", "emoji": "🟤"},
                ],
                "sentences": [
                    {"jp": "あかいかさをかいます。", "romaji": "Akai kasa wo kaimasu.",
                     "vn": "Tôi mua chiếc ô màu đỏ.", "blocks": ["あかい", "かさ", "を", "かいます"]},
                    {"jp": "そのくるまはしろいです。", "romaji": "Sono kuruma wa shiroi desu.",
                     "vn": "Chiếc xe đó màu trắng.", "blocks": ["その", "くるま", "は", "しろい", "です"]},
                ],
            },
            {
                "title": "Bài 4: Thời tiết và bốn mùa",
                "desc": "Nhật Bản có 4 mùa rõ rệt và người Nhật rất hay mở đầu câu chuyện bằng "
                        "chuyện thời tiết — giống như người Việt hỏi ăn cơm chưa. "
                        "きょうはあついですね là câu bắt chuyện an toàn nhất.",
                "vocab": [
                    {"kana": "はれ", "romaji": "hare", "vn": "trời nắng", "emoji": "☀️"},
                    {"kana": "あめ", "romaji": "ame", "vn": "mưa", "emoji": "🌧️"},
                    {"kana": "ゆき", "romaji": "yuki", "vn": "tuyết", "emoji": "❄️"},
                    {"kana": "くもり", "romaji": "kumori", "vn": "trời nhiều mây", "emoji": "☁️"},
                    {"kana": "さむい", "romaji": "samui", "vn": "lạnh (thời tiết)", "emoji": "🥶"},
                    {"kana": "はる", "romaji": "haru", "vn": "mùa xuân", "emoji": "🌸"},
                    {"kana": "なつ", "romaji": "natsu", "vn": "mùa hè", "emoji": "🏖️"},
                    {"kana": "あき", "romaji": "aki", "vn": "mùa thu", "emoji": "🍁"},
                    {"kana": "ふゆ", "romaji": "fuyu", "vn": "mùa đông", "emoji": "⛄"},
                ],
                "sentences": [
                    {"jp": "きょうはあついですね。", "romaji": "Kyou wa atsui desu ne.",
                     "vn": "Hôm nay trời nóng nhỉ.", "blocks": ["きょう", "は", "あつい", "です", "ね"]},
                    {"jp": "ふゆはとてもさむいです。", "romaji": "Fuyu wa totemo samui desu.",
                     "vn": "Mùa đông thì rất lạnh.", "blocks": ["ふゆ", "は", "とても", "さむい", "です"]},
                ],
            },
            {
                "title": "Bài 5: Phủ định tính từ",
                "desc": "2 loại tính từ phủ định khác nhau: tính từ い bỏ い thêm くない "
                        "(たかい → たかくないです), tính từ な thì thêm じゃありません "
                        "(しずか → しずかじゃありません). Ngoại lệ duy nhất: いい → よくないです.",
                "vocab": [
                    {"kana": "くないです", "romaji": "kunai desu", "vn": "không… (tính từ い)", "emoji": "🚫"},
                    {"kana": "あまり", "romaji": "amari", "vn": "không… lắm", "emoji": "➖"},
                    {"kana": "とても", "romaji": "totemo", "vn": "rất", "emoji": "‼️"},
                    {"kana": "ちょっと", "romaji": "chotto", "vn": "một chút", "emoji": "🤏"},
                ],
                "sentences": [
                    {"jp": "このほんはたかくないです。", "romaji": "Kono hon wa takakunai desu.",
                     "vn": "Quyển sách này không đắt.", "blocks": ["この", "ほん", "は", "たかくない", "です"]},
                    {"jp": "へやはあまりきれいじゃありません。", "romaji": "Heya wa amari kirei ja arimasen.",
                     "vn": "Căn phòng không sạch sẽ lắm.", "blocks": ["へや", "は", "あまり", "きれい", "じゃありません"]},
                ],
            },
            {
                "title": "Bài 6: Nối hai tính từ",
                "desc": "Muốn khen 2 điều cùng lúc: tính từ い đổi い thành くて (やすくて おいしい = rẻ mà ngon), "
                        "tính từ な thì thêm で (しずかで きれい). Đây là mẫu câu ghi điểm trong "
                        "phần thi nói vì cho thấy bạn diễn đạt được ý phức tạp.",
                "vocab": [
                    {"kana": "くて", "romaji": "kute", "vn": "…và… (nối tính từ い)", "emoji": "🔗"},
                    {"kana": "で", "romaji": "de", "vn": "…và… (nối tính từ な)", "emoji": "🔗"},
                    {"kana": "そして", "romaji": "soshite", "vn": "và, hơn nữa", "emoji": "➕"},
                ],
                "sentences": [
                    {"jp": "このみせはやすくておいしいです。", "romaji": "Kono mise wa yasukute oishii desu.",
                     "vn": "Quán này vừa rẻ vừa ngon.", "blocks": ["この", "みせ", "は", "やすくて", "おいしい", "です"]},
                    {"jp": "きょうとはしずかできれいです。", "romaji": "Kyouto wa shizuka de kirei desu.",
                     "vn": "Kyoto vừa yên tĩnh vừa đẹp.", "blocks": ["きょうと", "は", "しずか", "で", "きれい", "です"]},
                ],
            },
        ],
    },
]
