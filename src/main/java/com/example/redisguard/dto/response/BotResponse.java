package com.example.redisguard.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BotResponse {

    private Long id;
    private String name;
    private String personaDescription;
    private LocalDateTime createdAt;
}
