package com.example.nihongo_app.dto.response;

import lombok.Builder;
import lombok.Value;

/**
 * Toàn bộ trạng thái gamification của user hiện tại, trong 1 lần gọi duy nhất.
 *
 * <p>Dùng để FE hydrate lại UI ngay sau khi có JWT (login xong, hoặc mỗi lần mở lại app) —
 * trước đây không có endpoint nào gộp exp/level/coins/energy/streak, khiến FE phải chờ user
 * làm gì đó (submit bài...) mới có dữ liệu thật, còn lại thì hiện giá trị mặc định hardcode.</p>
 */
@Value
@Builder
public class UserStatsResponse {

    Long id;
    String email;
    String displayName;
    String username;
    String role;

    Integer level;
    Integer exp;
    Long rankId;
    String rankName;

    Integer coins;

    Integer currentEnergy;
    Integer maxEnergy;

    Integer currentStreak;
    Integer longestStreak;
    Integer streakFreezeCount;

    java.util.List<ActiveEffectResponse> activeEffects;
}
