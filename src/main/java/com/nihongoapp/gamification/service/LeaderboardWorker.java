package com.nihongoapp.gamification.service;

import com.nihongoapp.gamification.model.OutboxEvent;
import com.nihongoapp.gamification.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LeaderboardWorker {

    private static final Logger log = LoggerFactory.getLogger(LeaderboardWorker.class);
    private static final int MAX_RETRIES = 10;

    private final OutboxEventRepository outboxRepo;
    private final RedisTemplate<String, String> redisStringTemplate;

    public LeaderboardWorker(
            OutboxEventRepository outboxRepo,
            @Qualifier("redisStringTemplate") RedisTemplate<String, String> redisStringTemplate) {
        this.outboxRepo = outboxRepo;
        this.redisStringTemplate = redisStringTemplate;
    }

    @Scheduled(fixedRate = 3000)
    public void processOutbox() {
        List<OutboxEvent> events = outboxRepo.findTop100ByStatusOrderByCreatedAt("pending");
        if (events.isEmpty()) return;

        for (OutboxEvent event : events) {
            processEvent(event);
        }
    }

    private void processEvent(OutboxEvent event) {
        try {
            String key = "leaderboard:weekly:default";
            // Parse payload to get xp and league_id
            String payload = event.getPayload();
            // Simple JSON parse for xp and league_id
            long xp = parseXp(payload);
            String leagueId = parseLeagueId(payload);

            if (leagueId != null) {
                key = "leaderboard:weekly:" + leagueId;
            }

            redisStringTemplate.opsForZSet().incrementScore(key, String.valueOf(parseUserId(payload)), (double) xp);

            event.setStatus(OutboxEvent.Status.done);
            event.setProcessedAt(java.time.LocalDateTime.now());
            outboxRepo.save(event);
            log.debug("Outbox event {} processed", event.getId());
        } catch (Exception e) {
            log.warn("Outbox event {} failed: {}", event.getId(), e.getMessage());
            int retries = event.getRetryCount() + 1;
            event.setRetryCount(retries);
            if (retries >= MAX_RETRIES) {
                event.setStatus(OutboxEvent.Status.failed);
                log.error("Outbox event {} marked FAILED after {} retries", event.getId(), MAX_RETRIES);
            }
            outboxRepo.save(event);
        }
    }

    private long parseUserId(String payload) {
        try {
            return Long.parseLong(payload.split("\"user_id\":")[1].split("[,\\}]")[0].trim());
        } catch (Exception e) { return 0; }
    }

    private long parseXp(String payload) {
        try {
            return Long.parseLong(payload.split("\"xp_earned\":")[1].split("[,\\}]")[0].trim());
        } catch (Exception e) { return 0; }
    }

    private String parseLeagueId(String payload) {
        try {
            return payload.split("\"league_id\":\"")[1].split("\"")[0];
        } catch (Exception e) { return null; }
    }
}
