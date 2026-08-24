# -*- coding: utf-8 -*-
"""
Chuong trinh hoc chu de 3 -> 7 (sau khi hoc xong bang chu cai).

Moi bai gom:
  vocab     : tu vung moi cua bai (kana / romaji / nghia tieng Viet / emoji minh hoa)
  sentences : cau mau dung dung ngu phap cua bai, kem blocks de tach the sap xep cau
  grammar   : giai thich ngu phap ngan gon bang tieng Viet, dat trong description bai hoc

Toan bo cau mau chi dung tu vung da xuat hien o bai do hoac cac bai truoc.
"""

TOPICS_A = [
    # =======================================================================
    {
        "title": "Chào hỏi hằng ngày",
        # các câu chào là khái niệm trừu tượng, emoji không đủ phân biệt nghĩa
        # -> chủ đề này không dùng dạng câu "chọn hình", thay bằng nghe/dịch
        "no_image_quiz": True,
        "desc": "Những câu chào người Nhật dùng hàng chục lần mỗi ngày. Học xong chủ đề này "
                "bạn đã có thể chào hỏi, cảm ơn, xin lỗi và tạm biệt đúng hoàn cảnh — "
                "phần được đánh giá cao nhất khi giao tiếp với người Nhật.",
        "lessons": [
            {
                "title": "Bài 1: Chào theo buổi trong ngày",
                "desc": "Tiếng Nhật không có một câu chào dùng chung cho cả ngày như Xin chào "
                        "của tiếng Việt. Chào sai buổi nghe rất kỳ, nên hãy nhớ mốc thời gian: "
                        "sáng đến ~10h, trưa chiều đến ~18h, sau đó là buổi tối.",
                "vocab": [
                    {"kana": "おはようございます", "romaji": "ohayou gozaimasu", "vn": "chào buổi sáng (lịch sự)", "emoji": "🌅"},
                    {"kana": "おはよう", "romaji": "ohayou", "vn": "chào buổi sáng (thân mật)", "emoji": "🌄"},
                    {"kana": "こんにちは", "romaji": "konnichiwa", "vn": "xin chào (ban ngày)", "emoji": "☀️"},
                    {"kana": "こんばんは", "romaji": "konbanwa", "vn": "chào buổi tối", "emoji": "🌆"},
                    {"kana": "おやすみなさい", "romaji": "oyasuminasai", "vn": "chúc ngủ ngon", "emoji": "🌙"},
                ],
                "sentences": [
                    {"jp": "せんせい、おはようございます。", "romaji": "Sensei, ohayou gozaimasu.",
                     "vn": "Chào buổi sáng ạ, thưa thầy.", "blocks": ["せんせい", "、", "おはよう", "ございます"]},
                    {"jp": "みなさん、こんにちは。", "romaji": "Minasan, konnichiwa.",
                     "vn": "Xin chào mọi người.", "blocks": ["みなさん", "、", "こんにちは"]},
                ],
            },
            {
                "title": "Bài 2: Cảm ơn và xin lỗi",
                "desc": "すみません là từ vạn năng bậc nhất tiếng Nhật: vừa là xin lỗi, vừa là "
                        "làm phiền chút để gọi người khác, vừa có thể thay cho cảm ơn khi ai đó "
                        "giúp mình. Còn ごめんなさい chỉ dùng để xin lỗi khi mình có lỗi thật.",
                "vocab": [
                    {"kana": "ありがとうございます", "romaji": "arigatou gozaimasu", "vn": "cảm ơn (lịch sự)", "emoji": "🙏"},
                    {"kana": "ありがとう", "romaji": "arigatou", "vn": "cảm ơn (thân mật)", "emoji": "😊"},
                    {"kana": "どういたしまして", "romaji": "dou itashimashite", "vn": "không có chi", "emoji": "🤝"},
                    {"kana": "すみません", "romaji": "sumimasen", "vn": "xin lỗi / cho hỏi", "emoji": "🙇"},
                    {"kana": "ごめんなさい", "romaji": "gomen nasai", "vn": "tôi xin lỗi", "emoji": "😔"},
                    {"kana": "だいじょうぶです", "romaji": "daijoubu desu", "vn": "không sao đâu", "emoji": "👌"},
                ],
                "sentences": [
                    {"jp": "どうもありがとうございます。", "romaji": "Doumo arigatou gozaimasu.",
                     "vn": "Cảm ơn bạn rất nhiều.", "blocks": ["どうも", "ありがとう", "ございます"]},
                    {"jp": "すみません、だいじょうぶです。", "romaji": "Sumimasen, daijoubu desu.",
                     "vn": "Xin lỗi, không sao đâu.", "blocks": ["すみません", "、", "だいじょうぶ", "です"]},
                ],
            },
            {
                "title": "Bài 3: Lần đầu gặp mặt",
                "desc": "Bộ đôi はじめまして và よろしくおねがいします là nghi thức bắt buộc khi làm quen "
                        "người Nhật, luôn đi kèm một cái cúi đầu nhẹ. Câu thứ hai không dịch sát được "
                        "sang tiếng Việt, nghĩa gần nhất là mong được bạn giúp đỡ từ nay về sau.",
                "vocab": [
                    {"kana": "はじめまして", "romaji": "hajimemashite", "vn": "rất hân hạnh được gặp", "emoji": "🤝"},
                    {"kana": "よろしくおねがいします", "romaji": "yoroshiku onegaishimasu", "vn": "mong được giúp đỡ", "emoji": "🙇"},
                    {"kana": "おげんきですか", "romaji": "ogenki desu ka", "vn": "bạn có khỏe không?", "emoji": "❓"},
                    {"kana": "げんきです", "romaji": "genki desu", "vn": "tôi khỏe", "emoji": "💪"},
                    {"kana": "おなまえは", "romaji": "onamae wa", "vn": "tên bạn là gì?", "emoji": "📛"},
                ],
                "sentences": [
                    {"jp": "はじめまして、よろしくおねがいします。", "romaji": "Hajimemashite, yoroshiku onegaishimasu.",
                     "vn": "Rất hân hạnh, mong được giúp đỡ.", "blocks": ["はじめまして", "、", "よろしく", "おねがいします"]},
                    {"jp": "おげんきですか。げんきです。", "romaji": "Ogenki desu ka. Genki desu.",
                     "vn": "Bạn khỏe không? Tôi khỏe.", "blocks": ["おげんき", "ですか", "。", "げんき", "です"]},
                ],
            },
            {
                "title": "Bài 4: Tạm biệt",
                "desc": "さようなら nghe khá nặng nề, hàm ý lâu rồi mới gặp lại — người Nhật ít dùng "
                        "với bạn bè hằng ngày. Đi làm, đi học thì dùng またあした hoặc しつれいします.",
                "vocab": [
                    {"kana": "さようなら", "romaji": "sayounara", "vn": "tạm biệt", "emoji": "👋"},
                    {"kana": "またあした", "romaji": "mata ashita", "vn": "hẹn mai gặp lại", "emoji": "📅"},
                    {"kana": "またね", "romaji": "mata ne", "vn": "gặp lại nhé (thân mật)", "emoji": "✌️"},
                    {"kana": "しつれいします", "romaji": "shitsurei shimasu", "vn": "tôi xin phép (đi trước)", "emoji": "🚪"},
                    {"kana": "きをつけて", "romaji": "ki wo tsukete", "vn": "đi cẩn thận nhé", "emoji": "⚠️"},
                ],
                "sentences": [
                    {"jp": "さようなら、またあした。", "romaji": "Sayounara, mata ashita.",
                     "vn": "Tạm biệt, hẹn mai gặp lại.", "blocks": ["さようなら", "、", "また", "あした"]},
                    {"jp": "しつれいします。きをつけて。", "romaji": "Shitsurei shimasu. Ki wo tsukete.",
                     "vn": "Tôi xin phép đi trước. Bạn đi cẩn thận nhé.", "blocks": ["しつれい", "します", "。", "き", "を", "つけて"]},
                ],
            },
            {
                "title": "Bài 5: Chào khi ra khỏi nhà và về nhà",
                "desc": "4 câu này đi thành 2 cặp hỏi - đáp cố định trong mọi gia đình Nhật: "
                        "người đi nói いってきます, người ở nhà đáp いってらっしゃい; lúc về thì ただいま "
                        "và おかえりなさい. Bạn sẽ nghe chúng trong mọi bộ phim, anime Nhật.",
                "vocab": [
                    {"kana": "いってきます", "romaji": "ittekimasu", "vn": "con/tôi đi đây", "emoji": "🚶"},
                    {"kana": "いってらっしゃい", "romaji": "itterasshai", "vn": "đi rồi về nhé", "emoji": "👋"},
                    {"kana": "ただいま", "romaji": "tadaima", "vn": "con/tôi về rồi", "emoji": "🏠"},
                    {"kana": "おかえりなさい", "romaji": "okaerinasai", "vn": "mừng bạn về", "emoji": "🤗"},
                ],
                "sentences": [
                    {"jp": "いってきます。いってらっしゃい。", "romaji": "Ittekimasu. Itterasshai.",
                     "vn": "Con đi đây. Đi rồi về nhé.", "blocks": ["いってきます", "。", "いってらっしゃい"]},
                    {"jp": "ただいま。おかえりなさい。", "romaji": "Tadaima. Okaerinasai.",
                     "vn": "Con về rồi. Mừng con về.", "blocks": ["ただいま", "。", "おかえりなさい"]},
                ],
            },
            {
                "title": "Bài 6: Chào trong bữa ăn và nơi làm việc",
                "desc": "いただきます nói trước khi ăn (kể cả khi ăn một mình) để cảm ơn thức ăn, "
                        "ごちそうさまでした nói sau khi ăn xong. おつかれさまです là câu chào phổ biến nhất "
                        "trong công ty Nhật, dùng được cả khi gặp lẫn khi chia tay đồng nghiệp.",
                "vocab": [
                    {"kana": "いただきます", "romaji": "itadakimasu", "vn": "xin phép được ăn", "emoji": "🍽️"},
                    {"kana": "ごちそうさまでした", "romaji": "gochisousama deshita", "vn": "cảm ơn vì bữa ăn", "emoji": "😋"},
                    {"kana": "おつかれさまです", "romaji": "otsukaresama desu", "vn": "bạn vất vả rồi", "emoji": "💼"},
                    {"kana": "がんばって", "romaji": "ganbatte", "vn": "cố lên nhé", "emoji": "🔥"},
                    {"kana": "おめでとうございます", "romaji": "omedetou gozaimasu", "vn": "xin chúc mừng", "emoji": "🎉"},
                ],
                "sentences": [
                    {"jp": "いただきます。", "romaji": "Itadakimasu.",
                     "vn": "Con xin phép ăn ạ.", "blocks": ["いただきます"]},
                    {"jp": "おつかれさまです。がんばって。", "romaji": "Otsukaresama desu. Ganbatte.",
                     "vn": "Bạn vất vả rồi. Cố lên nhé.", "blocks": ["おつかれさま", "です", "。", "がんばって"]},
                ],
            },
        ],
    },
    # =======================================================================
    {
        "title": "Giới thiệu bản thân",
        "desc": "Mẫu câu N は N です — bộ khung câu đầu tiên và quan trọng nhất của tiếng Nhật. "
                "Hết chủ đề này bạn tự giới thiệu được tên, quốc tịch, nghề nghiệp và đặt câu hỏi "
                "ngược lại cho người đối diện.",
        "lessons": [
            {
                "title": "Bài 1: Tôi là… (わたしは〜です)",
                "desc": "Cấu trúc: A は B です = A thì/là B. は ở đây là trợ từ chủ đề, đọc là WA "
                        "chứ không đọc HA. です đứng cuối câu tạo sắc thái lịch sự — người Nhật "
                        "gần như luôn dùng です khi nói với người mới quen.",
                "vocab": [
                    {"kana": "わたし", "romaji": "watashi", "vn": "tôi", "emoji": "🙋"},
                    {"kana": "あなた", "romaji": "anata", "vn": "bạn", "emoji": "👉"},
                    {"kana": "あのひと", "romaji": "ano hito", "vn": "người kia", "emoji": "🧍"},
                    {"kana": "なまえ", "romaji": "namae", "vn": "tên", "emoji": "📛"},
                    {"kana": "〜さん", "romaji": "-san", "vn": "anh/chị/ông/bà (hậu tố lịch sự)", "emoji": "🎎"},
                ],
                "sentences": [
                    {"jp": "わたしはリンです。", "romaji": "Watashi wa Rin desu.",
                     "vn": "Tôi là Linh.", "blocks": ["わたし", "は", "リン", "です"]},
                    {"jp": "あのひとはたなかさんです。", "romaji": "Ano hito wa Tanaka-san desu.",
                     "vn": "Người kia là anh Tanaka.", "blocks": ["あのひと", "は", "たなか", "さん", "です"]},
                ],
            },
            {
                "title": "Bài 2: Quốc gia và quốc tịch",
                "desc": "Quy tắc cực gọn: tên nước + じん (người) = quốc tịch; tên nước + ご = ngôn ngữ. "
                        "にほん + じん = にほんじん (người Nhật), にほん + ご = にほんご (tiếng Nhật). "
                        "Riêng tiếng Anh là えいご, không phải アメリカご.",
                "vocab": [
                    {"kana": "にほん", "romaji": "nihon", "vn": "Nhật Bản", "emoji": "🇯🇵"},
                    {"kana": "ベトナム", "romaji": "betonamu", "vn": "Việt Nam", "emoji": "🇻🇳"},
                    {"kana": "アメリカ", "romaji": "amerika", "vn": "Mỹ", "emoji": "🇺🇸"},
                    {"kana": "かんこく", "romaji": "kankoku", "vn": "Hàn Quốc", "emoji": "🇰🇷"},
                    {"kana": "ちゅうごく", "romaji": "chuugoku", "vn": "Trung Quốc", "emoji": "🇨🇳"},
                    {"kana": "にほんじん", "romaji": "nihonjin", "vn": "người Nhật", "emoji": "🎎"},
                    {"kana": "ベトナムじん", "romaji": "betonamujin", "vn": "người Việt Nam", "emoji": "👨‍🌾"},
                    {"kana": "にほんご", "romaji": "nihongo", "vn": "tiếng Nhật", "emoji": "🗣️"},
                    {"kana": "えいご", "romaji": "eigo", "vn": "tiếng Anh", "emoji": "🔤"},
                ],
                "sentences": [
                    {"jp": "わたしはベトナムじんです。", "romaji": "Watashi wa betonamujin desu.",
                     "vn": "Tôi là người Việt Nam.", "blocks": ["わたし", "は", "ベトナムじん", "です"]},
                    {"jp": "たなかさんはにほんじんです。", "romaji": "Tanaka-san wa nihonjin desu.",
                     "vn": "Anh Tanaka là người Nhật.", "blocks": ["たなか", "さん", "は", "にほんじん", "です"]},
                ],
            },
            {
                "title": "Bài 3: Nghề nghiệp",
                "desc": "Người Nhật rất hay hỏi nghề nghiệp khi mới quen. Lưu ý せんせい chỉ dùng để gọi "
                        "người khác (thầy cô, bác sĩ, luật sư), không bao giờ tự xưng わたしはせんせいです "
                        "với người ngoài — sẽ nghe như đang tự đề cao mình.",
                "vocab": [
                    {"kana": "がくせい", "romaji": "gakusei", "vn": "học sinh, sinh viên", "emoji": "🎓"},
                    {"kana": "せんせい", "romaji": "sensei", "vn": "giáo viên", "emoji": "👨‍🏫"},
                    {"kana": "かいしゃいん", "romaji": "kaishain", "vn": "nhân viên công ty", "emoji": "💼"},
                    {"kana": "いしゃ", "romaji": "isha", "vn": "bác sĩ", "emoji": "👨‍⚕️"},
                    {"kana": "エンジニア", "romaji": "enjinia", "vn": "kỹ sư", "emoji": "👷"},
                    {"kana": "コック", "romaji": "kokku", "vn": "đầu bếp", "emoji": "👨‍🍳"},
                    {"kana": "かいしゃ", "romaji": "kaisha", "vn": "công ty", "emoji": "🏢"},
                ],
                "sentences": [
                    {"jp": "わたしはがくせいです。", "romaji": "Watashi wa gakusei desu.",
                     "vn": "Tôi là sinh viên.", "blocks": ["わたし", "は", "がくせい", "です"]},
                    {"jp": "やまださんはいしゃです。", "romaji": "Yamada-san wa isha desu.",
                     "vn": "Chị Yamada là bác sĩ.", "blocks": ["やまだ", "さん", "は", "いしゃ", "です"]},
                ],
            },
            {
                "title": "Bài 4: Câu hỏi với か và cách trả lời",
                "desc": "Tiếng Nhật không đảo trật tự từ để hỏi như tiếng Anh. Chỉ cần thêm か vào "
                        "cuối câu là thành câu hỏi, giữ nguyên mọi thứ còn lại: がくせいです → がくせいですか. "
                        "Trong văn viết thường không cần dấu chấm hỏi.",
                "vocab": [
                    {"kana": "はい", "romaji": "hai", "vn": "vâng, đúng vậy", "emoji": "⭕"},
                    {"kana": "いいえ", "romaji": "iie", "vn": "không", "emoji": "❌"},
                    {"kana": "そうです", "romaji": "sou desu", "vn": "đúng vậy", "emoji": "👍"},
                    {"kana": "だれ", "romaji": "dare", "vn": "ai", "emoji": "🕵️"},
                    {"kana": "なん", "romaji": "nan", "vn": "cái gì", "emoji": "❓"},
                ],
                "sentences": [
                    {"jp": "あなたはがくせいですか。", "romaji": "Anata wa gakusei desu ka.",
                     "vn": "Bạn là sinh viên phải không?", "blocks": ["あなた", "は", "がくせい", "ですか"]},
                    {"jp": "はい、そうです。", "romaji": "Hai, sou desu.",
                     "vn": "Vâng, đúng vậy.", "blocks": ["はい", "、", "そう", "です"]},
                    {"jp": "あのひとはだれですか。", "romaji": "Ano hito wa dare desu ka.",
                     "vn": "Người kia là ai vậy?", "blocks": ["あのひと", "は", "だれ", "ですか"]},
                ],
            },
            {
                "title": "Bài 5: Câu phủ định じゃありません",
                "desc": "Để phủ định, thay です bằng じゃありません: がくせいです → がくせいじゃありません "
                        "(không phải sinh viên). Dạng trang trọng hơn là ではありません, thường gặp trong "
                        "văn viết và thông báo chính thức.",
                "vocab": [
                    {"kana": "じゃありません", "romaji": "ja arimasen", "vn": "không phải là…", "emoji": "🚫"},
                    {"kana": "ではありません", "romaji": "dewa arimasen", "vn": "không phải là… (trang trọng)", "emoji": "📄"},
                    {"kana": "ちがいます", "romaji": "chigaimasu", "vn": "không phải, bạn nhầm rồi", "emoji": "🙅"},
                    {"kana": "まだ", "romaji": "mada", "vn": "vẫn chưa", "emoji": "⏳"},
                    {"kana": "でも", "romaji": "demo", "vn": "nhưng mà", "emoji": "🔄"},
                    {"kana": "ぎんこういん", "romaji": "ginkouin", "vn": "nhân viên ngân hàng", "emoji": "🏦"},
                ],
                "sentences": [
                    {"jp": "わたしはにほんじんじゃありません。", "romaji": "Watashi wa nihonjin ja arimasen.",
                     "vn": "Tôi không phải người Nhật.", "blocks": ["わたし", "は", "にほんじん", "じゃありません"]},
                    {"jp": "いいえ、ちがいます。", "romaji": "Iie, chigaimasu.",
                     "vn": "Không, không phải vậy.", "blocks": ["いいえ", "、", "ちがいます"]},
                    {"jp": "たなかさんはいしゃじゃありません。", "romaji": "Tanaka-san wa isha ja arimasen.",
                     "vn": "Anh Tanaka không phải bác sĩ.", "blocks": ["たなか", "さん", "は", "いしゃ", "じゃありません"]},
                    {"jp": "わたしはがくせいじゃありません。かいしゃいんです。",
                     "romaji": "Watashi wa gakusei ja arimasen. Kaishain desu.",
                     "vn": "Tôi không phải sinh viên. Tôi là nhân viên công ty.",
                     "blocks": ["わたし", "は", "がくせい", "じゃありません", "。", "かいしゃいん", "です"]},
                ],
            },
            {
                "title": "Bài 6: Trợ từ の (của)",
                "desc": "の nối 2 danh từ theo thứ tự NGƯỢC với tiếng Việt: わたしのなまえ = tên CỦA TÔI "
                        "(sở hữu đứng trước). Cũng dùng để chỉ nơi công tác hay lĩnh vực: "
                        "にほんごのせんせい = giáo viên tiếng Nhật.",
                "vocab": [
                    {"kana": "の", "romaji": "no", "vn": "của (nối 2 danh từ)", "emoji": "🔗"},
                    {"kana": "ともだち", "romaji": "tomodachi", "vn": "bạn bè", "emoji": "👫"},
                    {"kana": "だいがく", "romaji": "daigaku", "vn": "trường đại học", "emoji": "🏛️"},
                ],
                "sentences": [
                    {"jp": "わたしのなまえはリンです。", "romaji": "Watashi no namae wa Rin desu.",
                     "vn": "Tên tôi là Linh.", "blocks": ["わたし", "の", "なまえ", "は", "リン", "です"]},
                    {"jp": "たなかさんはにほんごのせんせいです。", "romaji": "Tanaka-san wa nihongo no sensei desu.",
                     "vn": "Anh Tanaka là giáo viên tiếng Nhật.", "blocks": ["たなか", "さん", "は", "にほんご", "の", "せんせい", "です"]},
                ],
            },
        ],
    },
    # =======================================================================
    {
        "title": "Số đếm, tuổi và giá tiền",
        "desc": "Con số là thứ bạn dùng ngay ngày đầu đặt chân tới Nhật: xem giá, trả tiền, nói tuổi, "
                "đọc số điện thoại. Chủ đề này đi từ số 1 đến hàng vạn, kèm các đơn vị đếm hay dùng nhất.",
        "lessons": [
            {
                "title": "Bài 1: Số từ 1 đến 10",
                "desc": "4 và 7 có 2 cách đọc: よん/し và なな/しち. Người Nhật thường dùng よん và なな "
                        "vì し trùng âm với từ chết (死) — kiêng kỵ giống số 13 của phương Tây.",
                "vocab": [
                    {"kana": "いち", "romaji": "ichi", "vn": "số 1", "emoji": "1️⃣"},
                    {"kana": "に", "romaji": "ni", "vn": "số 2", "emoji": "2️⃣"},
                    {"kana": "さん", "romaji": "san", "vn": "số 3", "emoji": "3️⃣"},
                    {"kana": "よん", "romaji": "yon", "vn": "số 4", "emoji": "4️⃣"},
                    {"kana": "ご", "romaji": "go", "vn": "số 5", "emoji": "5️⃣"},
                    {"kana": "ろく", "romaji": "roku", "vn": "số 6", "emoji": "6️⃣"},
                    {"kana": "なな", "romaji": "nana", "vn": "số 7", "emoji": "7️⃣"},
                    {"kana": "はち", "romaji": "hachi", "vn": "số 8", "emoji": "8️⃣"},
                    {"kana": "きゅう", "romaji": "kyuu", "vn": "số 9", "emoji": "9️⃣"},
                    {"kana": "じゅう", "romaji": "juu", "vn": "số 10", "emoji": "🔟"},
                ],
                "sentences": [
                    {"jp": "いち、に、さん、よん、ご。", "romaji": "Ichi, ni, san, yon, go.",
                     "vn": "Một, hai, ba, bốn, năm.", "blocks": ["いち", "に", "さん", "よん", "ご"]},
                ],
            },
            {
                "title": "Bài 2: Số từ 11 đến 100",
                "desc": "Cách ghép số của tiếng Nhật rất logic, không có từ bất quy tắc như eleven, "
                        "twelve của tiếng Anh: 11 = 10+1 = じゅういち, 20 = 2×10 = にじゅう, "
                        "35 = 3×10+5 = さんじゅうご. Nắm 10 số đầu là đọc được tới 99.",
                "vocab": [
                    {"kana": "じゅういち", "romaji": "juuichi", "vn": "số 11", "emoji": "🔢"},
                    {"kana": "じゅうご", "romaji": "juugo", "vn": "số 15", "emoji": "🔢"},
                    {"kana": "にじゅう", "romaji": "nijuu", "vn": "số 20", "emoji": "🔢"},
                    {"kana": "さんじゅう", "romaji": "sanjuu", "vn": "số 30", "emoji": "🔢"},
                    {"kana": "ごじゅう", "romaji": "gojuu", "vn": "số 50", "emoji": "🔢"},
                    {"kana": "きゅうじゅう", "romaji": "kyuujuu", "vn": "số 90", "emoji": "🔢"},
                    {"kana": "ひゃく", "romaji": "hyaku", "vn": "số 100", "emoji": "💯"},
                ],
                "sentences": [
                    {"jp": "じゅうごとにじゅうご。", "romaji": "Juugo to nijuugo.",
                     "vn": "Mười lăm và hai mươi lăm.", "blocks": ["じゅうご", "と", "にじゅうご"]},
                ],
            },
            {
                "title": "Bài 3: Số lớn — trăm, nghìn, vạn",
                "desc": "Tiếng Nhật đếm theo đơn vị VẠN (まん = 10.000) chứ không theo nghìn như "
                        "tiếng Việt. 100.000 đồng người Nhật đọc là じゅうまん (10 vạn). Đây là chỗ "
                        "người Việt hay nhầm nhất khi nghe giá tiền.",
                "vocab": [
                    {"kana": "にひゃく", "romaji": "nihyaku", "vn": "200", "emoji": "🔢"},
                    {"kana": "ごひゃく", "romaji": "gohyaku", "vn": "500", "emoji": "🔢"},
                    {"kana": "せん", "romaji": "sen", "vn": "1.000", "emoji": "🔢"},
                    {"kana": "さんぜん", "romaji": "sanzen", "vn": "3.000", "emoji": "🔢"},
                    {"kana": "いちまん", "romaji": "ichiman", "vn": "10.000 (một vạn)", "emoji": "💰"},
                ],
                "sentences": [
                    {"jp": "ごひゃくえんです。", "romaji": "Gohyaku en desu.",
                     "vn": "Là 500 yên.", "blocks": ["ごひゃく", "えん", "です"]},
                    {"jp": "いちまんえんです。", "romaji": "Ichiman en desu.",
                     "vn": "Là 10.000 yên.", "blocks": ["いちまん", "えん", "です"]},
                ],
            },
            {
                "title": "Bài 4: Tuổi và số điện thoại",
                "desc": "Tuổi dùng đuôi さい. Có 3 trường hợp đọc bất quy tắc phải học thuộc: "
                        "1 tuổi いっさい, 8 tuổi はっさい, 20 tuổi はたち (không phải にじゅっさい). "
                        "Đọc số điện thoại thì đọc rời từng số, dấu gạch ngang đọc là の.",
                "vocab": [
                    {"kana": "〜さい", "romaji": "-sai", "vn": "…tuổi", "emoji": "🎂"},
                    {"kana": "はたち", "romaji": "hatachi", "vn": "20 tuổi", "emoji": "🎊"},
                    {"kana": "なんさい", "romaji": "nansai", "vn": "bao nhiêu tuổi", "emoji": "❓"},
                    {"kana": "でんわばんごう", "romaji": "denwa bangou", "vn": "số điện thoại", "emoji": "📱"},
                    {"kana": "なんばん", "romaji": "nanban", "vn": "số mấy", "emoji": "🔢"},
                ],
                "sentences": [
                    {"jp": "わたしははたちです。", "romaji": "Watashi wa hatachi desu.",
                     "vn": "Tôi 20 tuổi.", "blocks": ["わたし", "は", "はたち", "です"]},
                    {"jp": "でんわばんごうはなんばんですか。", "romaji": "Denwa bangou wa nanban desu ka.",
                     "vn": "Số điện thoại của bạn là số mấy?", "blocks": ["でんわばんごう", "は", "なんばん", "ですか"]},
                ],
            },
            {
                "title": "Bài 5: Hỏi giá và mua hàng",
                "desc": "いくらですか là câu bạn sẽ dùng nhiều nhất khi đi mua sắm ở Nhật. "
                        "Đồng yên viết là えん (円). Khi muốn mua, chỉ cần chỉ vào món đồ và nói "
                        "これをください.",
                "vocab": [
                    {"kana": "いくら", "romaji": "ikura", "vn": "bao nhiêu tiền", "emoji": "💴"},
                    {"kana": "えん", "romaji": "en", "vn": "yên (tiền Nhật)", "emoji": "💴"},
                    {"kana": "たかい", "romaji": "takai", "vn": "đắt", "emoji": "📈"},
                    {"kana": "やすい", "romaji": "yasui", "vn": "rẻ", "emoji": "📉"},
                    {"kana": "ください", "romaji": "kudasai", "vn": "cho tôi xin…", "emoji": "🛒"},
                    {"kana": "みせ", "romaji": "mise", "vn": "cửa hàng", "emoji": "🏪"},
                ],
                "sentences": [
                    {"jp": "これはいくらですか。", "romaji": "Kore wa ikura desu ka.",
                     "vn": "Cái này bao nhiêu tiền?", "blocks": ["これ", "は", "いくら", "ですか"]},
                    {"jp": "これをください。", "romaji": "Kore wo kudasai.",
                     "vn": "Cho tôi cái này.", "blocks": ["これ", "を", "ください"]},
                ],
            },
            {
                "title": "Bài 6: Đơn vị đếm đồ vật",
                "desc": "Tiếng Nhật không đếm suông mà phải kèm đơn vị theo hình dạng vật: "
                        "まい cho vật mỏng dẹt (giấy, áo, vé), ほん cho vật dài (bút, chai, ô), "
                        "にん cho người, còn つ là đơn vị vạn năng dùng khi bạn quên mất đơn vị đúng.",
                "vocab": [
                    {"kana": "ひとつ", "romaji": "hitotsu", "vn": "một cái", "emoji": "☝️"},
                    {"kana": "ふたつ", "romaji": "futatsu", "vn": "hai cái", "emoji": "✌️"},
                    {"kana": "みっつ", "romaji": "mittsu", "vn": "ba cái", "emoji": "🤟"},
                    {"kana": "いちまい", "romaji": "ichimai", "vn": "một tờ/tấm", "emoji": "📄"},
                    {"kana": "いっぽん", "romaji": "ippon", "vn": "một cây/chai", "emoji": "🖊️"},
                    {"kana": "ひとり", "romaji": "hitori", "vn": "một người", "emoji": "🧍"},
                    {"kana": "ふたり", "romaji": "futari", "vn": "hai người", "emoji": "👬"},
                ],
                "sentences": [
                    {"jp": "コーヒーをふたつください。", "romaji": "Koohii wo futatsu kudasai.",
                     "vn": "Cho tôi hai ly cà phê.", "blocks": ["コーヒー", "を", "ふたつ", "ください"]},
                    {"jp": "きっぷをいちまいください。", "romaji": "Kippu wo ichimai kudasai.",
                     "vn": "Cho tôi một vé.", "blocks": ["きっぷ", "を", "いちまい", "ください"]},
                ],
            },
        ],
    },
    # =======================================================================
    {
        "title": "Đồ vật quanh ta",
        "desc": "Bộ chỉ định これ・それ・あれ và cách hỏi tên đồ vật. Đây là chủ đề giúp bạn tự học "
                "từ vựng suốt đời: chỉ vào bất cứ thứ gì và hỏi これはなんですか.",
        "lessons": [
            {
                "title": "Bài 1: これ・それ・あれ・どれ",
                "desc": "3 từ chỉ định chia theo khoảng cách: これ = cái này (gần người nói), "
                        "それ = cái đó (gần người nghe), あれ = cái kia (xa cả hai). どれ = cái nào. "
                        "Tiếng Việt chỉ có 2 mức nên hãy chú ý mức giữa それ.",
                "vocab": [
                    {"kana": "これ", "romaji": "kore", "vn": "cái này", "emoji": "👇"},
                    {"kana": "それ", "romaji": "sore", "vn": "cái đó", "emoji": "👉"},
                    {"kana": "あれ", "romaji": "are", "vn": "cái kia", "emoji": "👆"},
                    {"kana": "どれ", "romaji": "dore", "vn": "cái nào", "emoji": "❔"},
                ],
                "sentences": [
                    {"jp": "これはほんです。", "romaji": "Kore wa hon desu.",
                     "vn": "Đây là quyển sách.", "blocks": ["これ", "は", "ほん", "です"]},
                    {"jp": "それはなんですか。", "romaji": "Sore wa nan desu ka.",
                     "vn": "Cái đó là cái gì vậy?", "blocks": ["それ", "は", "なん", "ですか"]},
                ],
            },
            {
                "title": "Bài 2: Đồ dùng học tập",
                "desc": "Từ vựng trong cặp sách. Để ý ノート và ペン viết bằng Katakana vì là từ mượn "
                        "tiếng Anh, còn ほん và えんぴつ là từ thuần Nhật nên viết Hiragana.",
                "vocab": [
                    {"kana": "ほん", "romaji": "hon", "vn": "quyển sách", "emoji": "📖"},
                    {"kana": "ノート", "romaji": "nooto", "vn": "quyển vở", "emoji": "📓"},
                    {"kana": "えんぴつ", "romaji": "enpitsu", "vn": "bút chì", "emoji": "✏️"},
                    {"kana": "ペン", "romaji": "pen", "vn": "cây bút", "emoji": "🖊️"},
                    {"kana": "けしゴム", "romaji": "keshigomu", "vn": "cục tẩy", "emoji": "🧽"},
                    {"kana": "かばん", "romaji": "kaban", "vn": "cái cặp", "emoji": "🎒"},
                    {"kana": "じしょ", "romaji": "jisho", "vn": "quyển từ điển", "emoji": "📕"},
                    {"kana": "つくえ", "romaji": "tsukue", "vn": "cái bàn học", "emoji": "🪑"},
                ],
                "sentences": [
                    {"jp": "これはわたしのじしょです。", "romaji": "Kore wa watashi no jisho desu.",
                     "vn": "Đây là từ điển của tôi.", "blocks": ["これ", "は", "わたし", "の", "じしょ", "です"]},
                ],
            },
            {
                "title": "Bài 3: Đồ dùng hằng ngày",
                "desc": "Những vật bạn mang theo mỗi khi ra khỏi nhà. かさ (ô) là vật cực kỳ quan trọng "
                        "ở Nhật vì mưa rất thất thường — cửa hàng tiện lợi nào cũng bán.",
                "vocab": [
                    {"kana": "とけい", "romaji": "tokei", "vn": "cái đồng hồ", "emoji": "⌚"},
                    {"kana": "かぎ", "romaji": "kagi", "vn": "chìa khóa", "emoji": "🔑"},
                    {"kana": "かさ", "romaji": "kasa", "vn": "cái ô", "emoji": "☂️"},
                    {"kana": "さいふ", "romaji": "saifu", "vn": "cái ví", "emoji": "👛"},
                    {"kana": "めがね", "romaji": "megane", "vn": "cái kính", "emoji": "👓"},
                    {"kana": "スマホ", "romaji": "sumaho", "vn": "điện thoại thông minh", "emoji": "📱"},
                    {"kana": "かぐ", "romaji": "kagu", "vn": "đồ nội thất", "emoji": "🛋️"},
                ],
                "sentences": [
                    {"jp": "あれはたなかさんのかさです。", "romaji": "Are wa Tanaka-san no kasa desu.",
                     "vn": "Cái kia là ô của anh Tanaka.", "blocks": ["あれ", "は", "たなか", "さん", "の", "かさ", "です"]},
                ],
            },
            {
                "title": "Bài 4: この・その・あの + danh từ",
                "desc": "Khác biệt quan trọng: これ đứng MỘT MÌNH (cái này), còn この phải có danh từ "
                        "đi kèm ngay sau (この本 = quyển sách này). Nói このです là sai ngữ pháp, "
                        "và nói これ本 cũng sai.",
                "vocab": [
                    {"kana": "この", "romaji": "kono", "vn": "…này", "emoji": "👇"},
                    {"kana": "その", "romaji": "sono", "vn": "…đó", "emoji": "👉"},
                    {"kana": "あの", "romaji": "ano", "vn": "…kia", "emoji": "👆"},
                    {"kana": "どの", "romaji": "dono", "vn": "…nào", "emoji": "❔"},
                ],
                "sentences": [
                    {"jp": "このほんはたかいです。", "romaji": "Kono hon wa takai desu.",
                     "vn": "Quyển sách này đắt.", "blocks": ["この", "ほん", "は", "たかい", "です"]},
                    {"jp": "あのかばんはやすいです。", "romaji": "Ano kaban wa yasui desu.",
                     "vn": "Cái cặp kia rẻ.", "blocks": ["あの", "かばん", "は", "やすい", "です"]},
                ],
            },
            {
                "title": "Bài 5: Đồ này của ai?",
                "desc": "Khi đã rõ đang nói về vật gì, người Nhật lược bỏ luôn danh từ sau の: "
                        "わたしのです = (cái này) của tôi. Cách nói tỉnh lược này rất phổ biến "
                        "trong hội thoại đời thường.",
                "vocab": [
                    {"kana": "だれの", "romaji": "dare no", "vn": "của ai", "emoji": "🤷"},
                    {"kana": "わたしの", "romaji": "watashi no", "vn": "của tôi", "emoji": "🙋"},
                    {"kana": "かいしゃの", "romaji": "kaisha no", "vn": "của công ty", "emoji": "🏢"},
                    {"kana": "わすれもの", "romaji": "wasuremono", "vn": "đồ bỏ quên", "emoji": "🧳"},
                    {"kana": "おとしもの", "romaji": "otoshimono", "vn": "đồ đánh rơi", "emoji": "💧"},
                    {"kana": "みつけました", "romaji": "mitsukemashita", "vn": "tôi tìm thấy rồi", "emoji": "🔎"},
                ],
                "sentences": [
                    {"jp": "これはだれのかさですか。", "romaji": "Kore wa dare no kasa desu ka.",
                     "vn": "Đây là ô của ai vậy?", "blocks": ["これ", "は", "だれ", "の", "かさ", "ですか"]},
                    {"jp": "わたしのです。", "romaji": "Watashi no desu.",
                     "vn": "Của tôi đấy.", "blocks": ["わたし", "の", "です"]},
                    {"jp": "そのかぎはたなかさんのです。", "romaji": "Sono kagi wa Tanaka-san no desu.",
                     "vn": "Chiếc chìa khóa đó là của anh Tanaka.",
                     "blocks": ["その", "かぎ", "は", "たなか", "さん", "の", "です"]},
                    {"jp": "このスマホはわたしのじゃありません。", "romaji": "Kono sumaho wa watashi no ja arimasen.",
                     "vn": "Chiếc điện thoại này không phải của tôi.",
                     "blocks": ["この", "スマホ", "は", "わたし", "の", "じゃありません"]},
                ],
            },
            {
                "title": "Bài 6: Hỏi tên đồ vật",
                "desc": "これはなんですか là chiếc chìa khóa để tự học từ vựng khi ở Nhật: chỉ vào vật "
                        "lạ bất kỳ và hỏi. Khi trả lời sai, người Nhật lịch sự sẽ nói ちがいます "
                        "chứ ít khi nói thẳng là bạn sai.",
                "vocab": [
                    {"kana": "なんですか", "romaji": "nan desu ka", "vn": "là cái gì vậy?", "emoji": "❓"},
                    {"kana": "にほんご", "romaji": "nihongo", "vn": "tiếng Nhật", "emoji": "🗣️"},
                    {"kana": "ベトナムご", "romaji": "betonamugo", "vn": "tiếng Việt", "emoji": "💬"},
                ],
                "sentences": [
                    {"jp": "これはにほんごでなんですか。", "romaji": "Kore wa nihongo de nan desu ka.",
                     "vn": "Cái này tiếng Nhật gọi là gì?", "blocks": ["これ", "は", "にほんご", "で", "なん", "ですか"]},
                    {"jp": "それはけしゴムです。", "romaji": "Sore wa keshigomu desu.",
                     "vn": "Cái đó là cục tẩy.", "blocks": ["それ", "は", "けしゴム", "です"]},
                ],
            },
        ],
    },
    # =======================================================================
    {
        "title": "Địa điểm và vị trí",
        "desc": "Hỏi đường, chỉ chỗ, nói vật gì nằm ở đâu. Chủ đề này gồm cả cặp động từ あります "
                "và います — điểm ngữ pháp mà người mới học hay dùng sai nhất.",
        "lessons": [
            {
                "title": "Bài 1: ここ・そこ・あそこ・どこ",
                "desc": "Giống bộ これ・それ・あれ nhưng dùng cho ĐỊA ĐIỂM: ここ = chỗ này, そこ = chỗ đó, "
                        "あそこ = chỗ kia, どこ = chỗ nào. Nhận ra quy luật ko-so-a-do rồi thì "
                        "bạn học các bộ từ chỉ định còn lại rất nhanh.",
                "vocab": [
                    {"kana": "ここ", "romaji": "koko", "vn": "chỗ này", "emoji": "📍"},
                    {"kana": "そこ", "romaji": "soko", "vn": "chỗ đó", "emoji": "📌"},
                    {"kana": "あそこ", "romaji": "asoko", "vn": "chỗ kia", "emoji": "🗺️"},
                    {"kana": "どこ", "romaji": "doko", "vn": "chỗ nào, ở đâu", "emoji": "❔"},
                ],
                "sentences": [
                    {"jp": "トイレはどこですか。", "romaji": "Toire wa doko desu ka.",
                     "vn": "Nhà vệ sinh ở đâu ạ?", "blocks": ["トイレ", "は", "どこ", "ですか"]},
                    {"jp": "あそこです。", "romaji": "Asoko desu.",
                     "vn": "Ở đằng kia.", "blocks": ["あそこ", "です"]},
                ],
            },
            {
                "title": "Bài 2: Địa điểm trong thành phố",
                "desc": "Những nơi bạn cần tìm nhất khi mới sang Nhật. コンビニ (cửa hàng tiện lợi) là "
                        "từ rút gọn của convenience store — người Nhật rất hay rút gọn từ mượn dài.",
                "vocab": [
                    {"kana": "えき", "romaji": "eki", "vn": "nhà ga", "emoji": "🚉"},
                    {"kana": "びょういん", "romaji": "byouin", "vn": "bệnh viện", "emoji": "🏥"},
                    {"kana": "がっこう", "romaji": "gakkou", "vn": "trường học", "emoji": "🏫"},
                    {"kana": "ぎんこう", "romaji": "ginkou", "vn": "ngân hàng", "emoji": "🏦"},
                    {"kana": "コンビニ", "romaji": "konbini", "vn": "cửa hàng tiện lợi", "emoji": "🏪"},
                    {"kana": "レストラン", "romaji": "resutoran", "vn": "nhà hàng", "emoji": "🍽️"},
                    {"kana": "ゆうびんきょく", "romaji": "yuubinkyoku", "vn": "bưu điện", "emoji": "📮"},
                    {"kana": "こうえん", "romaji": "kouen", "vn": "công viên", "emoji": "🏞️"},
                ],
                "sentences": [
                    {"jp": "えきはどこですか。", "romaji": "Eki wa doko desu ka.",
                     "vn": "Nhà ga ở đâu ạ?", "blocks": ["えき", "は", "どこ", "ですか"]},
                    {"jp": "コンビニはそこです。", "romaji": "Konbini wa soko desu.",
                     "vn": "Cửa hàng tiện lợi ở chỗ đó.", "blocks": ["コンビニ", "は", "そこ", "です"]},
                ],
            },
            {
                "title": "Bài 3: Các phòng trong nhà",
                "desc": "Nhà Nhật tách riêng おふろ (phòng tắm) và トイレ (nhà vệ sinh) thành 2 phòng "
                        "khác nhau — hỏi nhầm phòng là chuyện thường gặp của người nước ngoài.",
                "vocab": [
                    {"kana": "へや", "romaji": "heya", "vn": "căn phòng", "emoji": "🛏️"},
                    {"kana": "だいどころ", "romaji": "daidokoro", "vn": "nhà bếp", "emoji": "🍳"},
                    {"kana": "トイレ", "romaji": "toire", "vn": "nhà vệ sinh", "emoji": "🚻"},
                    {"kana": "おふろ", "romaji": "ofuro", "vn": "phòng tắm", "emoji": "🛁"},
                    {"kana": "にわ", "romaji": "niwa", "vn": "khu vườn", "emoji": "🌳"},
                    {"kana": "げんかん", "romaji": "genkan", "vn": "lối vào để cởi giày", "emoji": "🚪"},
                    {"kana": "まど", "romaji": "mado", "vn": "cửa sổ", "emoji": "🪟"},
                ],
                "sentences": [
                    {"jp": "だいどころはここです。", "romaji": "Daidokoro wa koko desu.",
                     "vn": "Nhà bếp ở chỗ này.", "blocks": ["だいどころ", "は", "ここ", "です"]},
                ],
            },
            {
                "title": "Bài 4: あります và います",
                "desc": "Cùng nghĩa là CÓ nhưng chia theo sự sống: います dùng cho người và động vật "
                        "(vật biết tự di chuyển), あります dùng cho đồ vật và cây cối. "
                        "Nói ねこがあります là sai — mèo phải dùng います.",
                "vocab": [
                    {"kana": "あります", "romaji": "arimasu", "vn": "có (đồ vật)", "emoji": "📦"},
                    {"kana": "います", "romaji": "imasu", "vn": "có (người, động vật)", "emoji": "🧍"},
                    {"kana": "が", "romaji": "ga", "vn": "trợ từ chủ ngữ", "emoji": "🔗"},
                    {"kana": "に", "romaji": "ni", "vn": "ở (chỉ nơi tồn tại)", "emoji": "📍"},
                ],
                "sentences": [
                    {"jp": "へやにねこがいます。", "romaji": "Heya ni neko ga imasu.",
                     "vn": "Trong phòng có con mèo.", "blocks": ["へや", "に", "ねこ", "が", "います"]},
                    {"jp": "つくえにほんがあります。", "romaji": "Tsukue ni hon ga arimasu.",
                     "vn": "Trên bàn có quyển sách.", "blocks": ["つくえ", "に", "ほん", "が", "あります"]},
                ],
            },
            {
                "title": "Bài 5: Từ chỉ vị trí",
                "desc": "Cấu trúc: A の + từ vị trí + に. Ví dụ つくえのうえに = ở trên bàn. "
                        "Thứ tự ngược với tiếng Việt: vật mốc đứng trước, vị trí đứng sau.",
                "vocab": [
                    {"kana": "うえ", "romaji": "ue", "vn": "phía trên", "emoji": "⬆️"},
                    {"kana": "した", "romaji": "shita", "vn": "phía dưới", "emoji": "⬇️"},
                    {"kana": "なか", "romaji": "naka", "vn": "bên trong", "emoji": "📥"},
                    {"kana": "まえ", "romaji": "mae", "vn": "phía trước", "emoji": "⏪"},
                    {"kana": "うしろ", "romaji": "ushiro", "vn": "phía sau", "emoji": "⏩"},
                    {"kana": "となり", "romaji": "tonari", "vn": "bên cạnh", "emoji": "↔️"},
                    {"kana": "みぎ", "romaji": "migi", "vn": "bên phải", "emoji": "➡️"},
                    {"kana": "ひだり", "romaji": "hidari", "vn": "bên trái", "emoji": "⬅️"},
                ],
                "sentences": [
                    {"jp": "かばんのなかにさいふがあります。", "romaji": "Kaban no naka ni saifu ga arimasu.",
                     "vn": "Trong cặp có cái ví.", "blocks": ["かばん", "の", "なか", "に", "さいふ", "が", "あります"]},
                    {"jp": "ぎんこうはえきのとなりです。", "romaji": "Ginkou wa eki no tonari desu.",
                     "vn": "Ngân hàng ở bên cạnh nhà ga.", "blocks": ["ぎんこう", "は", "えき", "の", "となり", "です"]},
                ],
            },
            {
                "title": "Bài 6: Hỏi đường",
                "desc": "Công thức hỏi đường an toàn nhất: mở đầu bằng すみません để gây chú ý, "
                        "rồi hỏi 〜はどこですか. Kết thúc luôn nhớ nói ありがとうございます — "
                        "người Nhật rất coi trọng lời cảm ơn sau khi được giúp.",
                "vocab": [
                    {"kana": "みち", "romaji": "michi", "vn": "con đường", "emoji": "🛣️"},
                    {"kana": "ちかく", "romaji": "chikaku", "vn": "gần đây", "emoji": "📍"},
                    {"kana": "まっすぐ", "romaji": "massugu", "vn": "đi thẳng", "emoji": "⬆️"},
                    {"kana": "みぎにまがって", "romaji": "migi ni magatte", "vn": "rẽ phải", "emoji": "↩️"},
                ],
                "sentences": [
                    {"jp": "すみません、えきはどこですか。", "romaji": "Sumimasen, eki wa doko desu ka.",
                     "vn": "Xin lỗi, nhà ga ở đâu ạ?", "blocks": ["すみません", "、", "えき", "は", "どこ", "ですか"]},
                    {"jp": "まっすぐいってください。", "romaji": "Massugu itte kudasai.",
                     "vn": "Bạn hãy đi thẳng.", "blocks": ["まっすぐ", "いって", "ください"]},
                ],
            },
        ],
    },
]
