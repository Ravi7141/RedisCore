package com.example.redisguard.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LikeResponse {
    private Long postId;
    private Long likeCount;
    private String message;
}
