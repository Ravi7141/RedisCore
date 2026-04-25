package com.example.redisguard.controller;

import com.example.redisguard.dto.request.CreatePostRequest;
import com.example.redisguard.dto.response.ApiResponse;
import com.example.redisguard.dto.response.LikeResponse;
import com.example.redisguard.dto.response.PostResponse;
import com.example.redisguard.service.PostService;
import com.example.redisguard.service.ViralityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final ViralityService viralityService;

    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @Valid @RequestBody CreatePostRequest request) {
        PostResponse response = postService.createPost(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Post created successfully", response));
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<ApiResponse<LikeResponse>> likePost(@PathVariable Long postId) {
        LikeResponse response = postService.likePost(postId);
        return ResponseEntity
                .ok(ApiResponse.success("Post liked successfully", response));
    }

    @GetMapping("/{postId}/virality")
    public ResponseEntity<?> getViralityScore(@PathVariable Long postId) {
        Long score = viralityService.getViralityScore(postId);
        return ResponseEntity.ok(score);
    }
}
