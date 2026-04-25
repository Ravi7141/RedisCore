package com.example.redisguard.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String NOTIF_COOLDOWN_KEY = "notif:cooldown:user_%d";
    private static final String PENDING_NOTIFS_KEY = "user:%d:pending_notifs";

    public void handleBotInteractionNotification(Long userId, String botName) {
        String cooldownKey = String.format(NOTIF_COOLDOWN_KEY, userId);
        String pendingKey = String.format(PENDING_NOTIFS_KEY, userId);
        String message = "Bot " + botName + " replied to your post";

        Boolean isFirstNotif = redisTemplate.opsForValue()
                .setIfAbsent(cooldownKey, "1", Duration.ofMinutes(15));

        if (Boolean.FALSE.equals(isFirstNotif)) {
            // User already got notification in last 15 min  push to pending list
            redisTemplate.opsForList().rightPush(pendingKey, message);
            log.info("Notification queued for user {}: {}", userId, message);
        } else {
            // First notification → send immediately
            log.info("Push Notification Sent to User {}: {}", userId, message);
        }
    }
}

