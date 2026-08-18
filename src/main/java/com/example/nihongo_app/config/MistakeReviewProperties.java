package com.example.nihongo_app.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Các số cấu hình cho phiên ôn lỗi sai (Mistake Review), đọc từ {@code app.mistake-review}
 * trong application.yml — không hardcode trong service.
 */
@Component
@ConfigurationProperties(prefix = "app.mistake-review")
@Getter
@Setter
public class MistakeReviewProperties {

    /** So mistake ACTIVE toi da duoc dua vao 1 phien on (mac dinh 10). */
    private int sessionSize = 10;

    /** So lan dung lien tiep can de "xoa no" mot mistake (mac dinh 2). */
    private int resolveStreak = 2;

    /** So ngay toi thieu giua 2 lan dung gan nhat de lan dung moi nhat duoc tinh vao streak xoa no (mac dinh 1). */
    private int resolveMinGapDays = 1;

    /** Nang luong thuong khi hoan thanh 1 phien on hop le (mac dinh 5). */
    private int energyReward = 5;

    /** So phien on toi da duoc thuong nang luong moi ngay (mac dinh 2). */
    private int rewardedSessionsPerDay = 2;
}
