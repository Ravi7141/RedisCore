package com.example.redisguard.dto.response;

import com.example.redisguard.entity.Post;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PostResponse {
    private Long id;
    private Post.AuthorType authorType;
    private Long authorId;
    private String content;
    private Long likeCount;
    private LocalDateTime createdAt;
}
