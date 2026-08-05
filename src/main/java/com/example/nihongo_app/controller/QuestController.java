package com.example.nihongo_app.controller;

import com.example.nihongo_app.dto.response.QuestResponse;
import com.example.nihongo_app.entity.UserDailyQuest;
import com.example.nihongo_app.security.AppUserPrincipal;
import com.example.nihongo_app.service.DailyQuestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * API xem Daily Quest hôm nay của user hiện tại. Quest được gán "lười" (lazy):
 * lần gọi đầu tiên trong ngày sẽ tự tạo 3 quest mới nếu chưa có.
 */
@RestController
@RequestMapping("/api/v1/users/me/quests")
@RequiredArgsConstructor
@Tag(name = "Daily Quest", description = "3 nhiệm vụ hàng ngày, điều kiện để mở Rương thưởng")
public class QuestController {

    private final DailyQuestService dailyQuestService;

    @GetMapping
    @Operation(summary = "Xem 3 Daily Quest hôm nay (tự động gán ngẫu nhiên nếu chưa có)")
    public ResponseEntity<List<QuestResponse>> getTodayQuests(Authentication authentication) {
        Long userId = resolveUserId(authentication);
        List<UserDailyQuest> quests = dailyQuestService.getTodayQuests(userId);
        List<QuestResponse> response = quests.stream()
                .map(q -> QuestResponse.builder()
                        .questId(q.getId())
                        .title(q.getTitle())
                        .questType(q.getQuestType())
                        .currentProgress(q.getCurrentProgress())
                        .targetValue(q.getTargetValue())
                        .completed(q.getCompleted())
                        .build())
                .toList();
        return ResponseEntity.ok(response);
    }

    private Long resolveUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserPrincipal principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authenticated principal");
        }
        return principal.getUserId();
    }
}
