package com.example.nihongo_app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Danh mục quest có thể được gán ngẫu nhiên làm Daily Quest cho user.
 * Bảng vật lý: {@code quest_definitions} (tạo bởi V20__create_quest_tables.sql).
 *
 * <p>Mỗi ngày, {@code DailyQuestService} chọn ngẫu nhiên 3 definition đang
 * {@code active} để tạo {@link UserDailyQuest} cho từng user.</p>
 */
@Entity
@Table(name = "quest_definitions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestDefinition {

    public enum QuestType {
        COMPLETE_LESSONS, CORRECT_ANSWERS, PERFECT_LESSON
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "quest_type", nullable = false)
    private QuestType questType;

    @Column(name = "target_value", nullable = false)
    private Integer targetValue;

    @Column(nullable = false)
    private Boolean active;
}
