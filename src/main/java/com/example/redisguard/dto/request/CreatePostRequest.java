package com.example.redisguard.dto.request;

import com.example.redisguard.entity.Post;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePostRequest {

    @NotNull(message = "authorType is required (USER or BOT)")
    private Post.AuthorType authorType;

    @NotNull(message = "authorId is required")
    private Long authorId;

    @NotBlank(message = "content must not be blank")
    private String content;
}
