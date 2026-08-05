package com.example.nihongo_app.entity;

import com.example.nihongo_app.entity.QuestDefinition.QuestType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 1 quest cụ thể đã được gán cho 1 user trong 1 ngày.
 * Bảng vật lý: {@code user_daily_quests} (tạo bởi V20__create_quest_tables.sql).
 *
 * <p>{@code questType}/{@code targetValue} được copy từ {@link QuestDefinition} tại thời
 * điểm gán, để sau này admin đổi definition không ảnh hưởng ngược tới quest đã gán cho
 * user trong quá khứ.</p>
 */
@Entity
@Table(name = "user_daily_quests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDailyQuest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "quest_definition_id", nullable = false)
    private Long questDefinitionId;

    @Column(name = "quest_date", nullable = false)
    private LocalDate questDate;

    @Column(nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "quest_type", nullable = false)
    private QuestType questType;

    @Column(name = "target_value", nullable = false)
    private Integer targetValue;

    @Column(name = "current_progress", nullable = false)
    private Integer currentProgress;

    @Column(nullable = false)
    private Boolean completed;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
