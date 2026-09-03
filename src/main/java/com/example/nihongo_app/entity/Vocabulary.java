package com.example.nihongo_app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Một đơn vị kiến thức ôn được: từ vựng, kanji hoặc một chữ kana.
 *
 * <p>Đây là thực thể mà toàn bộ tính năng tra nghĩa và ôn tập ngắt quãng xoay quanh.
 * Điểm quan trọng: nó tách khỏi {@link LessonQuestion}. Cùng một từ 「せんせい」 xuất hiện
 * ở 5 câu hỏi khác nhau vẫn chỉ là MỘT row ở đây, nên trí nhớ của người học cộng dồn
 * qua mọi lần gặp thay vì bị chia lẻ theo từng câu — và họ không thể học vẹt đáp án
 * của riêng một câu để coi như đã thuộc từ.</p>
 *
 * <p>Cột {@code surface} dùng collation {@code utf8mb4_bin}: collation mặc định của
 * schema coi hiragana bằng katakana và bỏ qua dakuten, xem V36/V40.</p>
 */
@Entity
@Table(name = "vocabulary")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vocabulary {

    public enum ItemType {
        VOCAB, KANJI, KANA, PHRASE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private ItemType itemType;

    /** Mặt chữ hiển thị cho người học: 「おちゃ」, 「先生」. */
    @Column(name = "surface", nullable = false, length = 100)
    private String surface;

    /** Cách đọc kana, chỉ có khi {@code surface} chứa kanji. */
    @Column(name = "reading", length = 100)
    private String reading;

    @Column(name = "romaji", length = 100)
    private String romaji;

    @Column(name = "meaning_vn", nullable = false, length = 255)
    private String meaningVn;

    @Column(name = "jlpt_level", length = 2)
    private String jlptLevel;

    @Column(name = "audio_url", length = 255)
    private String audioUrl;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
