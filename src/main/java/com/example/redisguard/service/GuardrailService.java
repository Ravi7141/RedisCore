package com.example.redisguard.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class GuardrailService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String BOT_COUNT_KEY = "post:%d:bot_count";
    private static final String COOLDOWN_KEY = "cooldown:bot_%d:human_%d";

    /**
     * Atomically increments bot count and checks if it exceeds 100.
     * Returns true if allowed, false if cap reached.
     */
    public boolean checkAndIncrementBotCount(Long postId) {
        String key = String.format(BOT_COUNT_KEY, postId);
        Long count = redisTemplate.opsForValue().increment(key);

        if (count > 100) {
            // Decrement back since we are rejecting this request
            redisTemplate.opsForValue().decrement(key);
            log.warn("Horizontal cap reached for post {}", postId);
            return false;
        }
        return true;
    }


    public boolean checkAndSetCooldown(Long botId, Long humanId) {
        String key = String.format(COOLDOWN_KEY, botId, humanId);

        // SETNX - Set if Not Exists, returns true if key was set (not exists before)
        Boolean wasSet = redisTemplate.opsForValue()
                .setIfAbsent(key, "1", Duration.ofMinutes(10));

        if (Boolean.FALSE.equals(wasSet)) {
            // Key already exists → bot is on cooldown
            log.warn("Cooldown active for bot {} interacting with human {}", botId, humanId);
            return false;
        }
        return true;
    }
}
