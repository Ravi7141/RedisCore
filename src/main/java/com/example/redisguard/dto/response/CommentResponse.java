package com.example.redisguard.dto.response;

import com.example.redisguard.entity.Post;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentResponse {
    private Long id;
    private Long postId;
    private Post.AuthorType authorType;
    private Long authorId;
    private String content;
    private int depthLevel;
    private LocalDateTime createdAt;
}
