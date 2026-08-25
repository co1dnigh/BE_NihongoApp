package com.example.nihongo_app.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/** Một từ trong kho, kèm trạng thái ôn tập của người đang đăng nhập (nếu có). */
@Data
@Builder
public class VocabularyItemResponse {
    private Long id;
    private String itemType;
    /** Mặt chữ: 「おちゃ」. */
    private String surface;
    private String reading;
    private String romaji;
    private String meaningVn;
    private String audioUrl;

    /** {@code null} khi người học chưa từng gặp từ này. */
    private LocalDateTime firstLearnedAt;
    private LocalDateTime nextDueAt;
    private Integer repetitions;
    /** Đã tới hạn ôn hay chưa, tính sẵn ở server để client khỏi so giờ. */
    private Boolean due;
}
