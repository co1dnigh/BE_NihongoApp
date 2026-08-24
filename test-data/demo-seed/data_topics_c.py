# -*- coding: utf-8 -*-
"""
Chuong trinh hoc chu de 11 -> 14: 4 chu de doi song hang ngay bo sung.

Viet moi ngay 2026-08-24 khi bo 2 chu de bang chu cai khoi ban do lo trinh
(nguoi hoc da co trang hoc chu rieng). 4 chu de nay bu lai phan noi dung bi
bo di, va deu la tinh huong nguoi hoc gap that trong ngay: di mua do, di lai,
ke ve so thich, va noi ve suc khoe.

Nguyen tac giu nguyen nhu data_topics_a/b:
  - Cau mau CHI dung tu vung da xuat hien o bai nay hoac cac bai/chu de truoc.
  - emoji CHI dat khi no minh hoa DUNG nghia cua tu. Tu truu tuong (dong tu,
    trang tu, mau cau) de trong -> build.py tu dong khong sinh cau "chon hinh"
    cho tu do, thay bang dang nghe/dich. Tha it anh con hon anh sai.
  - Trong cung 1 chu de khong co 2 tu nao dung chung 1 emoji, vi dap an nhieu
    cua cau chon hinh lay tu chinh chu de do -> trung emoji = 2 the anh giong het.
"""

TOPICS_C = [
    # =======================================================================
    {
        "title": "Mua sắm ở cửa hàng",
        "desc": "Chủ đề đưa bạn đi hết một lần mua sắm: biết tên các loại cửa hàng, gọi tên "
                "món đồ mình cần, hỏi giá, xin cỡ khác và trả tiền. Đây là tình huống người "
                "nước ngoài ở Nhật phải dùng tiếng Nhật nhiều nhất trong tuần đầu tiên.",
        "lessons": [
            {
                "title": "Bài 1: Các loại cửa hàng",
                "desc": "Tiếng Nhật đặt tên cửa hàng cực kỳ dễ đoán: lấy tên món hàng rồi thêm "
                        "や (ya) vào sau. ほん (sách) → ほんや (hiệu sách), にく (thịt) → にくや "
                        "(hàng thịt), パン (bánh mì) → パンや (tiệm bánh). Biết mẹo này là tự đoán "
                        "được tên hàng chục cửa hàng mà không cần học thuộc từng từ.",
                "vocab": [
                    {"kana": "スーパー", "romaji": "suupaa", "vn": "siêu thị", "emoji": "🛒"},
                    {"kana": "デパート", "romaji": "depaato", "vn": "trung tâm thương mại", "emoji": "🏬"},
                    {"kana": "ほんや", "romaji": "honya", "vn": "hiệu sách", "emoji": "📚"},
                    {"kana": "やおや", "romaji": "yaoya", "vn": "hàng rau quả", "emoji": "🥬"},
                    {"kana": "にくや", "romaji": "nikuya", "vn": "hàng thịt", "emoji": "🥩"},
                    {"kana": "パンや", "romaji": "panya", "vn": "tiệm bánh mì", "emoji": "🥖"},
                    {"kana": "くすりや", "romaji": "kusuriya", "vn": "hiệu thuốc", "emoji": "💊"},
                ],
                "sentences": [
                    {"jp": "スーパーでやさいをかいます。", "romaji": "Suupaa de yasai o kaimasu.",
                     "vn": "Tôi mua rau ở siêu thị.", "blocks": ["スーパー", "で", "やさい", "を", "かいます"]},
                    {"jp": "あしたデパートへいきます。", "romaji": "Ashita depaato e ikimasu.",
                     "vn": "Ngày mai tôi đi trung tâm thương mại.", "blocks": ["あした", "デパート", "へ", "いきます"]},
                ],
            },
            {
                "title": "Bài 2: Quần áo và đồ mặc",
                "desc": "Phần lớn tên quần áo trong tiếng Nhật là từ mượn tiếng Anh viết bằng "
                        "Katakana, đọc lên là đoán ra ngay: シャツ (shirt), コート (coat), スカート "
                        "(skirt). Riêng ふく (quần áo nói chung), くつ (giày) và ぼうし (mũ) là từ "
                        "thuần Nhật, phải nhớ riêng.",
                "vocab": [
                    {"kana": "ふく", "romaji": "fuku", "vn": "quần áo", "emoji": "👕"},
                    {"kana": "シャツ", "romaji": "shatsu", "vn": "áo sơ mi", "emoji": "👔"},
                    {"kana": "ズボン", "romaji": "zubon", "vn": "quần dài", "emoji": "👖"},
                    {"kana": "スカート", "romaji": "sukaato", "vn": "váy", "emoji": "👗"},
                    {"kana": "コート", "romaji": "kooto", "vn": "áo khoác", "emoji": "🧥"},
                    {"kana": "くつ", "romaji": "kutsu", "vn": "giày", "emoji": "👟"},
                    {"kana": "ぼうし", "romaji": "boushi", "vn": "mũ, nón", "emoji": "🧢"},
                ],
                "sentences": [
                    {"jp": "このシャツはやすいです。", "romaji": "Kono shatsu wa yasui desu.",
                     "vn": "Chiếc áo sơ mi này rẻ.", "blocks": ["この", "シャツ", "は", "やすい", "です"]},
                    {"jp": "あかいくつをかいました。", "romaji": "Akai kutsu o kaimashita.",
                     "vn": "Tôi đã mua một đôi giày đỏ.", "blocks": ["あかい", "くつ", "を", "かいました"]},
                ],
            },
            {
                "title": "Bài 3: Hỏi giá và trả tiền",
                "desc": "いくらですか là câu bạn sẽ nói nhiều nhất khi mua sắm. Ở quầy thu ngân "
                        "レジ, nhân viên hầu như luôn hỏi hai câu: có cần túi không (ふくろ) và "
                        "trả bằng tiền mặt hay thẻ — げんきん hay カード, hai từ bạn đã học ở chủ đề "
                        "nhà hàng.",
                "vocab": [
                    {"kana": "レジ", "romaji": "reji", "vn": "quầy tính tiền", "emoji": "🧾"},
                    {"kana": "おつり", "romaji": "otsuri", "vn": "tiền thừa, tiền thối", "emoji": "🪙"},
                    {"kana": "ねだん", "romaji": "nedan", "vn": "giá cả", "emoji": "🏷️"},
                    {"kana": "ふくろ", "romaji": "fukuro", "vn": "túi đựng đồ"},
                    {"kana": "レシート", "romaji": "reshiito", "vn": "hoá đơn"},
                ],
                "sentences": [
                    {"jp": "これはいくらですか。", "romaji": "Kore wa ikura desu ka.",
                     "vn": "Cái này bao nhiêu tiền?", "blocks": ["これ", "は", "いくら", "ですか"]},
                    {"jp": "ふくろをおねがいします。", "romaji": "Fukuro o onegaishimasu.",
                     "vn": "Cho tôi xin cái túi.", "blocks": ["ふくろ", "を", "おねがいします"]},
                ],
            },
            {
                "title": "Bài 4: Chọn cỡ và màu",
                "desc": "サイズ mượn thẳng từ size tiếng Anh. Muốn xin cỡ khác chỉ cần ghép tính từ "
                        "đã học ở chủ đề miêu tả với サイズ: おおきいサイズ (cỡ lớn hơn), ちいさいサイズ "
                        "(cỡ nhỏ hơn), rồi thêm はありますか (có … không ạ?).",
                "vocab": [
                    {"kana": "サイズ", "romaji": "saizu", "vn": "cỡ, kích thước", "emoji": "📏"},
                    {"kana": "いろ", "romaji": "iro", "vn": "màu sắc", "emoji": "🎨"},
                    {"kana": "ながい", "romaji": "nagai", "vn": "dài"},
                    {"kana": "みじかい", "romaji": "mijikai", "vn": "ngắn"},
                    {"kana": "ちょうどいい", "romaji": "choudo ii", "vn": "vừa vặn, vừa đủ", "emoji": "👌"},
                    {"kana": "ありますか", "romaji": "arimasu ka", "vn": "có … không ạ?"},
                ],
                "sentences": [
                    {"jp": "おおきいサイズはありますか。", "romaji": "Ookii saizu wa arimasu ka.",
                     "vn": "Có cỡ lớn hơn không ạ?", "blocks": ["おおきい", "サイズ", "は", "ありますか"]},
                    {"jp": "このいろがすきです。", "romaji": "Kono iro ga suki desu.",
                     "vn": "Tôi thích màu này.", "blocks": ["この", "いろ", "が", "すき", "です"]},
                ],
            },
            {
                "title": "Bài 5: Giảm giá và khuyến mãi",
                "desc": "Ở Nhật bảng giá dán chữ はんがく nghĩa là còn một nửa, còn むりょう là miễn "
                        "phí — hai chữ đáng để nhận mặt sớm. セール thì mượn từ sale, thấy ở cửa "
                        "kính mọi cửa hàng vào tháng 1 và tháng 7 hằng năm.",
                "vocab": [
                    {"kana": "セール", "romaji": "seeru", "vn": "đợt giảm giá", "emoji": "🔖"},
                    {"kana": "はんがく", "romaji": "hangaku", "vn": "nửa giá", "emoji": "💸"},
                    {"kana": "むりょう", "romaji": "muryou", "vn": "miễn phí", "emoji": "🆓"},
                    {"kana": "ポイントカード", "romaji": "pointo kaado", "vn": "thẻ tích điểm", "emoji": "💳"},
                    {"kana": "たかすぎます", "romaji": "takasugimasu", "vn": "đắt quá"},
                ],
                "sentences": [
                    {"jp": "きょうはセールです。", "romaji": "Kyou wa seeru desu.",
                     "vn": "Hôm nay có giảm giá.", "blocks": ["きょう", "は", "セール", "です"]},
                    {"jp": "このぼうしははんがくです。", "romaji": "Kono boushi wa hangaku desu.",
                     "vn": "Chiếc mũ này còn nửa giá.", "blocks": ["この", "ぼうし", "は", "はんがく", "です"]},
                ],
            },
            {
                "title": "Bài 6: Hội thoại mua hàng trọn vẹn",
                "desc": "Ghép tất cả lại thành một lần mua hàng thật: xin xem đồ (みせてください), "
                        "hỏi giá, chốt mua (これにします) hoặc từ chối lịch sự (けっこうです). けっこうです "
                        "rất dễ hiểu nhầm — nó nghĩa là thôi, không cần đâu ạ, chứ không phải đồng ý.",
                "vocab": [
                    {"kana": "みせてください", "romaji": "misete kudasai", "vn": "cho tôi xem với", "emoji": "👀"},
                    {"kana": "これにします", "romaji": "kore ni shimasu", "vn": "tôi lấy cái này", "emoji": "✅"},
                    {"kana": "けっこうです", "romaji": "kekkou desu", "vn": "thôi, không cần ạ", "emoji": "🙅"},
                    {"kana": "ちょっとまってください", "romaji": "chotto matte kudasai", "vn": "đợi tôi một chút"},
                ],
                "sentences": [
                    {"jp": "それをみせてください。", "romaji": "Sore o misete kudasai.",
                     "vn": "Cho tôi xem cái đó với.", "blocks": ["それ", "を", "みせてください"]},
                    {"jp": "これにします。カードでおねがいします。", "romaji": "Kore ni shimasu. Kaado de onegaishimasu.",
                     "vn": "Tôi lấy cái này. Cho tôi trả bằng thẻ.", "blocks": ["これにします", "。", "カード", "で", "おねがいします"]},
                ],
            },
        ],
    },
    # =======================================================================
    {
        "title": "Đi lại và phương tiện",
        "desc": "Tàu điện là mạch máu của đời sống Nhật Bản. Chủ đề này dạy bạn gọi tên phương "
                "tiện, mua vé, tìm đúng lối ra, hỏi đường khi lạc và hỏi đi mất bao lâu — đủ để "
                "tự đi lại một mình mà không cần ai dẫn.",
        "lessons": [
            {
                "title": "Bài 1: Phương tiện đi lại",
                "desc": "Nhóm từ này chia làm hai: từ thuần Nhật (でんしゃ tàu điện, くるま ô tô, "
                        "じてんしゃ xe đạp, ひこうき máy bay) và từ mượn viết Katakana (バス, タクシー). "
                        "しんかんせん là tàu siêu tốc — chữ này ghép từ 3 phần nghĩa đen là tuyến "
                        "đường ray mới.",
                "vocab": [
                    {"kana": "バス", "romaji": "basu", "vn": "xe buýt", "emoji": "🚌"},
                    {"kana": "タクシー", "romaji": "takushii", "vn": "taxi", "emoji": "🚕"},
                    {"kana": "じてんしゃ", "romaji": "jitensha", "vn": "xe đạp", "emoji": "🚲"},
                    {"kana": "くるま", "romaji": "kuruma", "vn": "ô tô", "emoji": "🚗"},
                    {"kana": "ひこうき", "romaji": "hikouki", "vn": "máy bay", "emoji": "✈️"},
                    {"kana": "ちかてつ", "romaji": "chikatetsu", "vn": "tàu điện ngầm", "emoji": "🚇"},
                    {"kana": "しんかんせん", "romaji": "shinkansen", "vn": "tàu siêu tốc", "emoji": "🚄"},
                ],
                "sentences": [
                    {"jp": "まいにちバスでがっこうへいきます。", "romaji": "Mainichi basu de gakkou e ikimasu.",
                     "vn": "Hằng ngày tôi đi học bằng xe buýt.", "blocks": ["まいにち", "バス", "で", "がっこう", "へ", "いきます"]},
                    {"jp": "ひこうきでベトナムへかえります。", "romaji": "Hikouki de Betonamu e kaerimasu.",
                     "vn": "Tôi về Việt Nam bằng máy bay.", "blocks": ["ひこうき", "で", "ベトナム", "へ", "かえります"]},
                ],
            },
            {
                "title": "Bài 2: Ở nhà ga",
                "desc": "Ga tàu Nhật rất lớn, biết 4 chữ này là không lạc: きっぷ (vé), かいさつ (cửa "
                        "soát vé), ホーム (sân ga, mượn từ platform), でぐち (lối ra). Bảng chỉ dẫn "
                        "trong ga hầu như chỗ nào cũng có 4 chữ đó.",
                "vocab": [
                    {"kana": "きっぷ", "romaji": "kippu", "vn": "vé", "emoji": "🎫"},
                    {"kana": "ホーム", "romaji": "hoomu", "vn": "sân ga", "emoji": "🚉"},
                    {"kana": "でぐち", "romaji": "deguchi", "vn": "lối ra", "emoji": "🚪"},
                    {"kana": "かいさつ", "romaji": "kaisatsu", "vn": "cửa soát vé"},
                    {"kana": "いりぐち", "romaji": "iriguchi", "vn": "lối vào"},
                    {"kana": "ばんせん", "romaji": "bansen", "vn": "đường ray số mấy"},
                ],
                "sentences": [
                    {"jp": "えきできっぷをかいます。", "romaji": "Eki de kippu o kaimasu.",
                     "vn": "Tôi mua vé ở nhà ga.", "blocks": ["えき", "で", "きっぷ", "を", "かいます"]},
                    {"jp": "でぐちはどこですか。", "romaji": "Deguchi wa doko desu ka.",
                     "vn": "Lối ra ở đâu ạ?", "blocks": ["でぐち", "は", "どこ", "ですか"]},
                ],
            },
            {
                "title": "Bài 3: Lên xe, xuống xe",
                "desc": "Chú ý trợ từ: lên xe dùng に (でんしゃにのります), còn đi bằng phương tiện gì "
                        "thì dùng で (でんしゃでいきます). Người Việt hay lẫn hai câu này. Riêng đi bộ "
                        "thì không dùng で mà nói あるいて.",
                "vocab": [
                    {"kana": "のります", "romaji": "norimasu", "vn": "lên xe, đi (phương tiện)"},
                    {"kana": "おります", "romaji": "orimasu", "vn": "xuống xe"},
                    {"kana": "のりかえます", "romaji": "norikaemasu", "vn": "đổi tuyến, chuyển tàu"},
                    {"kana": "あるいて", "romaji": "aruite", "vn": "đi bộ", "emoji": "🚶"},
                    {"kana": "つぎ", "romaji": "tsugi", "vn": "tiếp theo"},
                ],
                "sentences": [
                    {"jp": "えきでちかてつにのります。", "romaji": "Eki de chikatetsu ni norimasu.",
                     "vn": "Tôi lên tàu điện ngầm ở ga.", "blocks": ["えき", "で", "ちかてつ", "に", "のります"]},
                    {"jp": "つぎのえきでおります。", "romaji": "Tsugi no eki de orimasu.",
                     "vn": "Tôi xuống ở ga tiếp theo.", "blocks": ["つぎ", "の", "えき", "で", "おります"]},
                ],
            },
            {
                "title": "Bài 4: Hỏi đường",
                "desc": "Bạn đã biết まっすぐ và みぎにまがって ở chủ đề vị trí. Bài này thêm các mốc "
                        "người Nhật hay lấy làm chuẩn khi chỉ đường: しんごう (đèn giao thông), "
                        "こうさてん (ngã tư), かど (góc phố), はし (cây cầu).",
                "vocab": [
                    {"kana": "しんごう", "romaji": "shingou", "vn": "đèn giao thông", "emoji": "🚦"},
                    {"kana": "はし", "romaji": "hashi", "vn": "cây cầu", "emoji": "🌉"},
                    {"kana": "こうさてん", "romaji": "kousaten", "vn": "ngã tư"},
                    {"kana": "かど", "romaji": "kado", "vn": "góc phố"},
                    {"kana": "とおい", "romaji": "tooi", "vn": "xa"},
                    {"kana": "ちかい", "romaji": "chikai", "vn": "gần"},
                ],
                "sentences": [
                    {"jp": "えきはとおいですか。", "romaji": "Eki wa tooi desu ka.",
                     "vn": "Nhà ga có xa không ạ?", "blocks": ["えき", "は", "とおい", "ですか"]},
                    {"jp": "しんごうをみぎにまがってください。", "romaji": "Shingou o migi ni magatte kudasai.",
                     "vn": "Đến đèn giao thông xin rẽ phải.", "blocks": ["しんごう", "を", "みぎにまがって", "ください"]},
                ],
            },
            {
                "title": "Bài 5: Đi mất bao lâu",
                "desc": "Mẫu 〜から〜まで…かかります là cách chuẩn để nói quãng đường mất bao lâu: "
                        "うちからえきまでじゅっぷんかかります = từ nhà đến ga mất 10 phút. どのくらい là "
                        "câu hỏi tương ứng: khoảng bao lâu.",
                "vocab": [
                    {"kana": "かかります", "romaji": "kakarimasu", "vn": "mất (thời gian)", "emoji": "⏳"},
                    {"kana": "じかん", "romaji": "jikan", "vn": "tiếng đồng hồ", "emoji": "⏰"},
                    {"kana": "はやい", "romaji": "hayai", "vn": "nhanh, sớm", "emoji": "💨"},
                    {"kana": "おそい", "romaji": "osoi", "vn": "chậm, muộn", "emoji": "🐌"},
                    {"kana": "どのくらい", "romaji": "dono kurai", "vn": "khoảng bao lâu"},
                ],
                "sentences": [
                    {"jp": "うちからえきまでじゅっぷんかかります。", "romaji": "Uchi kara eki made juppun kakarimasu.",
                     "vn": "Từ nhà đến ga mất mười phút.", "blocks": ["うち", "から", "えき", "まで", "じゅっぷん", "かかります"]},
                    {"jp": "しんかんせんははやいです。", "romaji": "Shinkansen wa hayai desu.",
                     "vn": "Tàu siêu tốc thì nhanh.", "blocks": ["しんかんせん", "は", "はやい", "です"]},
                ],
            },
            {
                "title": "Bài 6: Khi tàu trễ, tàu đông",
                "desc": "Tàu Nhật hiếm khi trễ, nhưng khi trễ thì loa ga báo bằng chữ おくれて "
                        "います. Giờ cao điểm sáng thì tàu こんでいます (đông nghẹt) — hai chữ này "
                        "nghe được là bớt hoang mang hẳn.",
                "vocab": [
                    {"kana": "おくれて", "romaji": "okurete", "vn": "bị trễ, bị muộn"},
                    {"kana": "まちます", "romaji": "machimasu", "vn": "đợi, chờ"},
                    {"kana": "こんでいます", "romaji": "konde imasu", "vn": "đông nghẹt"},
                    {"kana": "すいています", "romaji": "suite imasu", "vn": "vắng người"},
                    {"kana": "さいしゅう", "romaji": "saishuu", "vn": "chuyến cuối cùng"},
                ],
                "sentences": [
                    {"jp": "でんしゃがおくれています。", "romaji": "Densha ga okurete imasu.",
                     "vn": "Tàu đang bị trễ.", "blocks": ["でんしゃ", "が", "おくれて", "います"]},
                    {"jp": "ここでともだちをまちます。", "romaji": "Koko de tomodachi o machimasu.",
                     "vn": "Tôi đợi bạn ở đây.", "blocks": ["ここ", "で", "ともだち", "を", "まちます"]},
                ],
            },
        ],
    },
    # =======================================================================
    {
        "title": "Sở thích và ngày nghỉ",
        "desc": "Câu hỏi しゅみはなんですか (sở thích của bạn là gì?) gần như chắc chắn xuất hiện "
                "trong mọi cuộc làm quen với người Nhật. Chủ đề này cho bạn đủ vốn từ để trả lời, "
                "để nói thích hay không thích, giỏi hay kém, và kể mình làm gì vào cuối tuần.",
        "lessons": [
            {
                "title": "Bài 1: Sở thích của tôi",
                "desc": "Mẫu câu しゅみは〜です dùng đúng một lần là dùng được cả đời: わたしのしゅみは "
                        "りょうりです (sở thích của tôi là nấu ăn). Chỗ trống điền một danh từ chỉ "
                        "hoạt động, không phải động từ.",
                "vocab": [
                    {"kana": "しゅみ", "romaji": "shumi", "vn": "sở thích"},
                    {"kana": "りょこう", "romaji": "ryokou", "vn": "du lịch", "emoji": "🧳"},
                    {"kana": "どくしょ", "romaji": "dokusho", "vn": "đọc sách", "emoji": "📖"},
                    {"kana": "りょうり", "romaji": "ryouri", "vn": "nấu ăn", "emoji": "🍳"},
                    {"kana": "しゃしん", "romaji": "shashin", "vn": "ảnh, chụp ảnh", "emoji": "📷"},
                    {"kana": "かいもの", "romaji": "kaimono", "vn": "mua sắm", "emoji": "🛍️"},
                ],
                "sentences": [
                    {"jp": "わたしのしゅみはりょうりです。", "romaji": "Watashi no shumi wa ryouri desu.",
                     "vn": "Sở thích của tôi là nấu ăn.", "blocks": ["わたし", "の", "しゅみ", "は", "りょうり", "です"]},
                    {"jp": "にちようびにりょこうへいきます。", "romaji": "Nichiyoubi ni ryokou e ikimasu.",
                     "vn": "Chủ nhật tôi đi du lịch.", "blocks": ["にちようび", "に", "りょこう", "へ", "いきます"]},
                ],
            },
            {
                "title": "Bài 2: Thể thao",
                "desc": "Tên môn thể thao đi với động từ します (chơi, làm): サッカーをします = chơi bóng "
                        "đá. Đừng dịch chơi thành あそびます — あそびます chỉ dùng cho chơi đùa nói chung, "
                        "không dùng cho môn thể thao.",
                "vocab": [
                    {"kana": "スポーツ", "romaji": "supootsu", "vn": "thể thao", "emoji": "🏅"},
                    {"kana": "サッカー", "romaji": "sakkaa", "vn": "bóng đá", "emoji": "⚽"},
                    {"kana": "やきゅう", "romaji": "yakyuu", "vn": "bóng chày", "emoji": "⚾"},
                    {"kana": "テニス", "romaji": "tenisu", "vn": "quần vợt", "emoji": "🎾"},
                    {"kana": "すいえい", "romaji": "suiei", "vn": "bơi lội", "emoji": "🏊"},
                    {"kana": "ジョギング", "romaji": "jogingu", "vn": "chạy bộ", "emoji": "🏃"},
                    {"kana": "します", "romaji": "shimasu", "vn": "làm, chơi (môn gì)"},
                ],
                "sentences": [
                    {"jp": "まいあさジョギングをします。", "romaji": "Maiasa jogingu o shimasu.",
                     "vn": "Sáng nào tôi cũng chạy bộ.", "blocks": ["まいあさ", "ジョギング", "を", "します"]},
                    {"jp": "どようびにテニスをします。", "romaji": "Doyoubi ni tenisu o shimasu.",
                     "vn": "Thứ bảy tôi chơi quần vợt.", "blocks": ["どようび", "に", "テニス", "を", "します"]},
                ],
            },
            {
                "title": "Bài 3: Phim ảnh và âm nhạc",
                "desc": "Ba động từ đi kèm khác nhau, nhớ đúng cặp thì câu mới tự nhiên: えいがを "
                        "みます (xem phim), おんがくをききます (nghe nhạc), うたをうたいます (hát bài hát). "
                        "Cụm cuối lặp gốc từ うた nghe hơi lạ nhưng người Nhật nói đúng như vậy.",
                "vocab": [
                    {"kana": "えいが", "romaji": "eiga", "vn": "phim", "emoji": "🎬"},
                    {"kana": "ゲーム", "romaji": "geemu", "vn": "trò chơi điện tử", "emoji": "🎮"},
                    {"kana": "うた", "romaji": "uta", "vn": "bài hát", "emoji": "🎵"},
                    {"kana": "うたいます", "romaji": "utaimasu", "vn": "hát", "emoji": "🎤"},
                    {"kana": "アニメ", "romaji": "anime", "vn": "phim hoạt hình"},
                    {"kana": "まんが", "romaji": "manga", "vn": "truyện tranh"},
                ],
                "sentences": [
                    {"jp": "よるえいがをみます。", "romaji": "Yoru eiga o mimasu.",
                     "vn": "Buổi tối tôi xem phim.", "blocks": ["よる", "えいが", "を", "みます"]},
                    {"jp": "うちでうたをうたいます。", "romaji": "Uchi de uta o utaimasu.",
                     "vn": "Tôi hát ở nhà.", "blocks": ["うち", "で", "うた", "を", "うたいます"]},
                ],
            },
            {
                "title": "Bài 4: Thích và không thích",
                "desc": "Điểm dễ sai nhất: thứ được thích đi với が chứ không phải を — すしがすきです, "
                        "không nói すしをすきです. Lý do là すき không phải động từ mà là tính từ な, nên "
                        "câu có nghĩa đen gần với sushi thì đáng thích đối với tôi.",
                "vocab": [
                    {"kana": "だいすき", "romaji": "daisuki", "vn": "rất thích", "emoji": "❤️"},
                    {"kana": "きらい", "romaji": "kirai", "vn": "ghét, không thích", "emoji": "👎"},
                    {"kana": "だいきらい", "romaji": "daikirai", "vn": "rất ghét", "emoji": "💢"},
                    {"kana": "すきじゃありません", "romaji": "suki ja arimasen", "vn": "không thích"},
                ],
                "sentences": [
                    {"jp": "わたしはすしがだいすきです。", "romaji": "Watashi wa sushi ga daisuki desu.",
                     "vn": "Tôi rất thích sushi.", "blocks": ["わたし", "は", "すし", "が", "だいすき", "です"]},
                    {"jp": "ビールはあまりすきじゃありません。", "romaji": "Biiru wa amari suki ja arimasen.",
                     "vn": "Tôi không thích bia lắm.", "blocks": ["ビール", "は", "あまり", "すきじゃありません"]},
                ],
            },
            {
                "title": "Bài 5: Giỏi và kém",
                "desc": "じょうず và へた cũng đi với が như すき. Một lưu ý về văn hoá: người Nhật gần "
                        "như không tự khen mình じょうず — nói về bản thân thì họ dùng すき hoặc とくい, "
                        "còn じょうず để dành khen người khác.",
                "vocab": [
                    {"kana": "じょうず", "romaji": "jouzu", "vn": "giỏi, khéo", "emoji": "👍"},
                    {"kana": "へた", "romaji": "heta", "vn": "kém, vụng", "emoji": "😅"},
                    {"kana": "とくい", "romaji": "tokui", "vn": "sở trường, làm tốt"},
                    {"kana": "できます", "romaji": "dekimasu", "vn": "làm được, có thể"},
                ],
                "sentences": [
                    {"jp": "あのひとはえいごがじょうずです。", "romaji": "Ano hito wa eigo ga jouzu desu.",
                     "vn": "Người kia giỏi tiếng Anh.", "blocks": ["あのひと", "は", "えいご", "が", "じょうず", "です"]},
                    {"jp": "わたしはりょうりがへたです。", "romaji": "Watashi wa ryouri ga heta desu.",
                     "vn": "Tôi nấu ăn dở.", "blocks": ["わたし", "は", "りょうり", "が", "へた", "です"]},
                ],
            },
            {
                "title": "Bài 6: Cuối tuần bạn làm gì",
                "desc": "Bốn trạng từ tần suất này đặt ngay trước động từ: いつも (luôn luôn) > よく "
                        "(thường xuyên) > ときどき (thỉnh thoảng) > ぜんぜん (hoàn toàn không). Riêng "
                        "ぜんぜん bắt buộc đi với đuôi phủ định ở cuối câu.",
                "vocab": [
                    {"kana": "しゅうまつ", "romaji": "shuumatsu", "vn": "cuối tuần", "emoji": "🗓️"},
                    {"kana": "いつも", "romaji": "itsumo", "vn": "luôn luôn"},
                    {"kana": "よく", "romaji": "yoku", "vn": "thường xuyên"},
                    {"kana": "ときどき", "romaji": "tokidoki", "vn": "thỉnh thoảng"},
                    {"kana": "ぜんぜん", "romaji": "zenzen", "vn": "hoàn toàn không"},
                ],
                "sentences": [
                    {"jp": "しゅうまつはいつもうちにいます。", "romaji": "Shuumatsu wa itsumo uchi ni imasu.",
                     "vn": "Cuối tuần tôi luôn ở nhà.", "blocks": ["しゅうまつ", "は", "いつも", "うち", "に", "います"]},
                    {"jp": "にちようびはよくえいがをみます。", "romaji": "Nichiyoubi wa yoku eiga o mimasu.",
                     "vn": "Chủ nhật tôi hay xem phim.", "blocks": ["にちようび", "は", "よく", "えいが", "を", "みます"]},
                ],
            },
        ],
    },
    # =======================================================================
    {
        "title": "Sức khoẻ và cơ thể",
        "desc": "Chủ đề không ai muốn dùng nhưng lúc cần thì không thay thế được: gọi tên bộ phận "
                "cơ thể, nói đúng chỗ đang đau, mua thuốc và hiểu câu bác sĩ hỏi. Học trước lúc "
                "khoẻ để lúc ốm còn nói được.",
        "lessons": [
            {
                "title": "Bài 1: Các bộ phận cơ thể",
                "desc": "Toàn từ ngắn một đến hai âm, rất dễ nhớ nhưng cũng rất dễ lẫn vì có nhiều "
                        "từ đồng âm: め là mắt, みみ là tai, はな ở đây là mũi (trùng âm với はな hoa). "
                        "Trong câu, ngữ cảnh sẽ cho biết đang nói đến nghĩa nào.",
                "vocab": [
                    {"kana": "め", "romaji": "me", "vn": "mắt", "emoji": "👁️"},
                    {"kana": "はな", "romaji": "hana", "vn": "mũi", "emoji": "👃"},
                    {"kana": "くち", "romaji": "kuchi", "vn": "miệng", "emoji": "👄"},
                    {"kana": "みみ", "romaji": "mimi", "vn": "tai", "emoji": "👂"},
                    {"kana": "て", "romaji": "te", "vn": "tay", "emoji": "✋"},
                    {"kana": "あし", "romaji": "ashi", "vn": "chân", "emoji": "🦵"},
                    {"kana": "あたま", "romaji": "atama", "vn": "đầu"},
                    {"kana": "おなか", "romaji": "onaka", "vn": "bụng"},
                ],
                "sentences": [
                    {"jp": "こどもはめがおおきいです。", "romaji": "Kodomo wa me ga ookii desu.",
                     "vn": "Đứa bé có đôi mắt to.", "blocks": ["こども", "は", "め", "が", "おおきい", "です"]},
                    {"jp": "おとうさんはあしがながいです。", "romaji": "Otousan wa ashi ga nagai desu.",
                     "vn": "Bố tôi chân dài.", "blocks": ["おとうさん", "は", "あし", "が", "ながい", "です"]},
                ],
            },
            {
                "title": "Bài 2: Nói chỗ đang đau",
                "desc": "Công thức đúng một mẫu duy nhất, thay tên bộ phận vào là xong: "
                        "〜がいたいです. あたまがいたいです = tôi đau đầu. Chú ý dùng が chứ không dùng は, "
                        "vì phần đứng trước が chính là chỗ đang có vấn đề.",
                "vocab": [
                    {"kana": "いたい", "romaji": "itai", "vn": "đau", "emoji": "😣"},
                    {"kana": "ねつ", "romaji": "netsu", "vn": "sốt", "emoji": "🤒"},
                    {"kana": "かぜ", "romaji": "kaze", "vn": "cảm, cảm lạnh", "emoji": "🤧"},
                    {"kana": "せき", "romaji": "seki", "vn": "ho"},
                    {"kana": "のど", "romaji": "nodo", "vn": "cổ họng"},
                ],
                "sentences": [
                    {"jp": "あたまがいたいです。", "romaji": "Atama ga itai desu.",
                     "vn": "Tôi bị đau đầu.", "blocks": ["あたま", "が", "いたい", "です"]},
                    {"jp": "きょうはねつがあります。", "romaji": "Kyou wa netsu ga arimasu.",
                     "vn": "Hôm nay tôi bị sốt.", "blocks": ["きょう", "は", "ねつ", "が", "あります"]},
                ],
            },
            {
                "title": "Bài 3: Bệnh viện và thuốc",
                "desc": "Ở Nhật thuốc kê đơn lấy tại くすりや ngay cạnh bệnh viện, còn thuốc thường "
                        "mua ở siêu thị thuốc. Động từ đi với thuốc là のみます (uống) — kể cả thuốc "
                        "viên hay thuốc bột đều dùng のみます.",
                "vocab": [
                    {"kana": "くすり", "romaji": "kusuri", "vn": "thuốc", "emoji": "💊"},
                    {"kana": "ちゅうしゃ", "romaji": "chuusha", "vn": "tiêm", "emoji": "💉"},
                    {"kana": "けんさ", "romaji": "kensa", "vn": "xét nghiệm, kiểm tra", "emoji": "🩺"},
                    {"kana": "びょうき", "romaji": "byouki", "vn": "bệnh, ốm"},
                    {"kana": "しんさつ", "romaji": "shinsatsu", "vn": "việc khám bệnh"},
                ],
                "sentences": [
                    {"jp": "びょういんへいきます。", "romaji": "Byouin e ikimasu.",
                     "vn": "Tôi đi bệnh viện.", "blocks": ["びょういん", "へ", "いきます"]},
                    {"jp": "あさくすりをのみます。", "romaji": "Asa kusuri o nomimasu.",
                     "vn": "Buổi sáng tôi uống thuốc.", "blocks": ["あさ", "くすり", "を", "のみます"]},
                ],
            },
            {
                "title": "Bài 4: Cảm giác trong người",
                "desc": "つかれました đã ở thể quá khứ sẵn — người Nhật nói tôi đã mệt chứ không nói "
                        "tôi đang mệt, vì mệt là kết quả của việc vừa làm xong. Đây là câu bạn sẽ "
                        "nghe đồng nghiệp nói mỗi chiều tan làm.",
                "vocab": [
                    {"kana": "つかれました", "romaji": "tsukaremashita", "vn": "mệt rồi", "emoji": "😫"},
                    {"kana": "ねむい", "romaji": "nemui", "vn": "buồn ngủ", "emoji": "😴"},
                    {"kana": "きぶんがわるい", "romaji": "kibun ga warui", "vn": "thấy trong người khó chịu"},
                    {"kana": "きもちがいい", "romaji": "kimochi ga ii", "vn": "thấy dễ chịu"},
                    {"kana": "げんきになりました", "romaji": "genki ni narimashita", "vn": "đã khoẻ lại"},
                ],
                "sentences": [
                    {"jp": "きょうはとてもつかれました。", "romaji": "Kyou wa totemo tsukaremashita.",
                     "vn": "Hôm nay tôi rất mệt.", "blocks": ["きょう", "は", "とても", "つかれました"]},
                    {"jp": "あさはいつもねむいです。", "romaji": "Asa wa itsumo nemui desu.",
                     "vn": "Buổi sáng lúc nào tôi cũng buồn ngủ.", "blocks": ["あさ", "は", "いつも", "ねむい", "です"]},
                ],
            },
            {
                "title": "Bài 5: Lời khuyên và lời chúc",
                "desc": "おだいじに là câu chia tay chuẩn mực khi người kia đang ốm, dịch thoáng là "
                        "giữ gìn sức khoẻ nhé. Nói câu này lúc bạn bè hay đồng nghiệp Nhật xin nghỉ "
                        "ốm sẽ được đánh giá là rất tinh ý.",
                "vocab": [
                    {"kana": "やすみます", "romaji": "yasumimasu", "vn": "nghỉ, nghỉ làm"},
                    {"kana": "やすんでください", "romaji": "yasunde kudasai", "vn": "hãy nghỉ ngơi đi", "emoji": "🛌"},
                    {"kana": "おだいじに", "romaji": "odaijini", "vn": "giữ gìn sức khoẻ nhé"},
                    {"kana": "むりしないで", "romaji": "muri shinaide", "vn": "đừng gắng sức quá"},
                    {"kana": "ゆっくり", "romaji": "yukkuri", "vn": "từ từ, thong thả"},
                ],
                "sentences": [
                    {"jp": "きょうはうちでやすんでください。", "romaji": "Kyou wa uchi de yasunde kudasai.",
                     "vn": "Hôm nay hãy nghỉ ở nhà nhé.", "blocks": ["きょう", "は", "うち", "で", "やすんでください"]},
                    {"jp": "あしたはしごとをやすみます。", "romaji": "Ashita wa shigoto o yasumimasu.",
                     "vn": "Ngày mai tôi nghỉ làm.", "blocks": ["あした", "は", "しごと", "を", "やすみます"]},
                ],
            },
            {
                "title": "Bài 6: Ở phòng khám",
                "desc": "Bác sĩ Nhật gần như luôn mở đầu bằng どうしましたか (bạn bị làm sao?) rồi hỏi "
                        "いつからですか (từ khi nào?). Chỉ cần trả lời được hai câu đó là buổi khám "
                        "trôi qua được, phần còn lại bác sĩ sẽ dẫn.",
                "vocab": [
                    {"kana": "どうしましたか", "romaji": "dou shimashita ka", "vn": "bạn bị làm sao thế?"},
                    {"kana": "いつからですか", "romaji": "itsu kara desu ka", "vn": "từ khi nào ạ?"},
                    {"kana": "きのうから", "romaji": "kinou kara", "vn": "từ hôm qua"},
                    {"kana": "まいにちのみます", "romaji": "mainichi nomimasu", "vn": "uống mỗi ngày"},
                ],
                "sentences": [
                    {"jp": "どうしましたか。あたまがいたいです。", "romaji": "Dou shimashita ka. Atama ga itai desu.",
                     "vn": "Bạn bị làm sao? Tôi đau đầu.", "blocks": ["どうしましたか", "。", "あたま", "が", "いたい", "です"]},
                    {"jp": "いつからですか。きのうからです。", "romaji": "Itsu kara desu ka. Kinou kara desu.",
                     "vn": "Từ khi nào ạ? Từ hôm qua.", "blocks": ["いつからですか", "。", "きのうから", "です"]},
                ],
            },
        ],
    },
]
