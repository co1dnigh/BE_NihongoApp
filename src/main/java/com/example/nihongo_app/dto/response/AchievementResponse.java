package com.example.nihongo_app.dto.response;

import lombok.Builder;
import lombok.Value;

/**
 * Trạng thái 1 achievement đối với user: đã unlock hay chưa, tiến độ hiện tại.
 *
 * <p>Dùng để FE vẽ grid huy chương: achievement đã unlock → hiện icon, achievement
 * đang progress → hiện thanh tiến độ, achievement ẩn (secret, chưa unlock) → hiện
 * dấu hỏi.</p>
 */
@Value
@Builder
public class AchievementResponse {

    Long achievementId;
    String code;
    String name;
    String description;
    String icon;
    String type;
    Integer threshold;

    /** true = achievement này là secret (ẩn cho tới khi unlock). */
    Boolean secret;

    /** true = user đã unlock achievement này. */
    Boolean unlocked;

    /** Tiến độ hiện tại (so với threshold). */
    Integer progress;

    /** true = achievement này đang active. */
    Boolean active;
}