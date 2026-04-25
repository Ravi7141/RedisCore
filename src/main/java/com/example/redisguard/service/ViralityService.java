package com.example.redisguard.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ViralityService {
    private final RedisTemplate<String, String> redisTemplate;

    private static final String VIRALITY_KEY = "post:%d:virality_score";

    public void incrementBotReply(Long postId) {
        increment(postId, 1);
    }

    public void incrementHumanLike(Long postId) {
        increment(postId, 20);
    }

    public void incrementHumanComment(Long postId) {
        increment(postId, 50);
    }

    private void increment(Long postId, long points) {
        String key = String.format(VIRALITY_KEY, postId);
        redisTemplate.opsForValue().increment(key, points);
        log.info("Virality score for post {} incremented by {}", postId, points);
    }

    public Long getViralityScore(Long postId) {
        String key = String.format(VIRALITY_KEY, postId);
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value) : 0L;
    }
}
