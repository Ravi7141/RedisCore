package com.example.redisguard.service;

import com.example.redisguard.dto.request.CreatePostRequest;
import com.example.redisguard.dto.response.LikeResponse;
import com.example.redisguard.dto.response.PostResponse;
import com.example.redisguard.entity.Post;
import com.example.redisguard.exception.ResourceNotFoundException;
import com.example.redisguard.repo.BotRepository;
import com.example.redisguard.repo.PostRepository;
import com.example.redisguard.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final BotRepository botRepository;
    private final ViralityService viralityService;

    private static final String LIKE_KEY_PREFIX = "post:likes:";
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public PostResponse createPost(CreatePostRequest request) {
        validateAuthorExists(request.getAuthorType(), request.getAuthorId());

        Post post = Post.builder()
                .authorType(request.getAuthorType())
                .authorId(request.getAuthorId())
                .content(request.getContent())
                .likeCount(0L)
                .build();

        Post saved = postRepository.save(post);
        log.info("Created post id={} by {}:{}", saved.getId(), saved.getAuthorType(), saved.getAuthorId());
        return toResponse(saved);
    }

    private void validateAuthorExists(Post.AuthorType type, Long authorId) {
        if (type == Post.AuthorType.USER) {
            if (!userRepository.existsById(authorId)) {
                throw new ResourceNotFoundException("User", authorId);
            }
        } else {
            if (!botRepository.existsById(authorId)) {
                throw new ResourceNotFoundException("Bot", authorId);
            }
        }
    }

    private PostResponse toResponse(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .authorType(post.getAuthorType())
                .authorId(post.getAuthorId())
                .content(post.getContent())
                .likeCount(post.getLikeCount())
                .createdAt(post.getCreatedAt())
                .build();
    }
    @Transactional
    public LikeResponse likePost(Long postId) {
        // Validate post exists
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", postId));

        // Atomically increment in Redis (fast-path)
        String redisKey = LIKE_KEY_PREFIX + postId;
        Long redisCount = redisTemplate.opsForValue().increment(redisKey);

        // Also persist the increment in PostgreSQL (durable)
        postRepository.incrementLikeCount(postId);

        long likeCount = redisCount != null ? redisCount : post.getLikeCount() + 1;
        log.info("Post {} liked. Redis count={}", postId, likeCount);
        // After like is saved
        viralityService.incrementHumanLike(postId);

        return LikeResponse.builder()
                .postId(postId)
                .likeCount(likeCount)
                .message("Post liked successfully")
                .build();
    }
}
