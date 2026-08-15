package com.example.nihongo_app.controller;

import com.example.nihongo_app.dto.response.LeaderboardResponse;
import com.example.nihongo_app.dto.response.RankResponse;
import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.repository.UserRepository;
import com.example.nihongo_app.security.AppUserPrincipal;
import com.example.nihongo_app.service.RankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Rank", description = "Danh sách hạng và leaderboard")
public class RankController {

    private final RankService rankService;
    private final UserRepository userRepository;

    @GetMapping("/ranks")
    @Operation(summary = "Lấy danh sách hạng đang có trong hệ thống, sắp xếp theo order_index tăng dần")
    public ResponseEntity<List<RankResponse>> getRanks() {
        return ResponseEntity.ok(rankService.getAllRanks());
    }

    @GetMapping("/leaderboard")
    @Operation(summary = "Lấy leaderboard top 15 theo hạng; nếu không truyền rankId thì dùng hạng của user đang đăng nhập")
    public ResponseEntity<LeaderboardResponse> getLeaderboard(@RequestParam(value = "rankId", required = false) Long rankId,
                                                            Principal principal) {
        Long currentUserId = resolveUserId(principal);
        Long targetRankId = rankId != null ? rankId : resolveCurrentUserRankId(currentUserId);
        return ResponseEntity.ok(rankService.getLeaderboard(targetRankId, currentUserId));
    }

    private Long resolveCurrentUserRankId(Long currentUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));
        if (currentUser.getRank() == null || currentUser.getRank().getId() == null) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "User has no rank assigned");
        }
        return currentUser.getRank().getId();
    }

    private Long resolveUserId(Principal principal) {
        if (principal == null || !(principal instanceof Authentication authentication)
                || !(authentication.getPrincipal() instanceof AppUserPrincipal appUserPrincipal)) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Missing authenticated principal");
        }
        return appUserPrincipal.getUserId();
    }
}
