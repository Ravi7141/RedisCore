package com.example.redisguard.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final RedisTemplate<String, String> redisTemplate;

    // Runs every 5 minutes
    @Scheduled(fixedRate = 5 * 60 * 1000)
//    @Scheduled(fixedRate = 10 * 1000) // for testing use this 10 second
    public void sweepPendingNotifications() {
        log.info("CRON Sweeper started - scanning pending notifications");

        // Scan all keys matching user:*:pending_notifs
        Set<String> keys = redisTemplate.keys("user:*:pending_notifs");

        if (keys == null || keys.isEmpty()) {
            log.info("CRON Sweeper - no pending notifications found");
            return;
        }

        for (String key : keys) {
            // Pop all messages from the list
            List<String> messages = redisTemplate.opsForList()
                    .range(key, 0, -1);

            if (messages == null || messages.isEmpty()) continue;

            // Delete the list after reading
            redisTemplate.delete(key);

            // Parse first bot name and count
            String firstMessage = messages.get(0);
            int othersCount = messages.size() - 1;

            // Extract userId from key (user:{id}:pending_notifs)
            String userId = key.split(":")[1];

            if (othersCount == 0) {
                log.info("Summarized Push Notification to user {}: {}",
                        userId, firstMessage);
            } else {
                log.info("Summarized Push Notification to user {}: {} and {} others interacted with your posts",
                        userId, firstMessage, othersCount);
            }
        }

        log.info("CRON Sweeper finished");
    }
}