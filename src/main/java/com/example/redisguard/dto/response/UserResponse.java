package com.example.redisguard.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private Boolean isPremium;
    private LocalDateTime createdAt;
}
