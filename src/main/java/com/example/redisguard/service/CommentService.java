package com.example.redisguard.service;

import com.example.redisguard.dto.request.CreateCommentRequest;
import com.example.redisguard.dto.response.CommentResponse;
import com.example.redisguard.entity.Bot;
import com.example.redisguard.entity.Comment;
import com.example.redisguard.entity.Post;
import com.example.redisguard.exception.ResourceNotFoundException;
import com.example.redisguard.exception.TooManyRequestsException;
import com.example.redisguard.repo.BotRepository;
import com.example.redisguard.repo.CommentRepository;
import com.example.redisguard.repo.PostRepository;
import com.example.redisguard.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final BotRepository botRepository;
    private final GuardrailService guardrailService;
    private final ViralityService viralityService;
    private final NotificationService notificationService;

    @Transactional
    public CommentResponse addComment(Long postId, CreateCommentRequest request) {
        // Validate post exists
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", postId));

        // Validate author exists
        if (request.getAuthorType() == Post.AuthorType.USER) {
            if (!userRepository.existsById(request.getAuthorId())) {
                throw new ResourceNotFoundException("User", request.getAuthorId());
            }
        } else {
            if (!botRepository.existsById(request.getAuthorId())) {
                throw new ResourceNotFoundException("Bot", request.getAuthorId());
            }
        }

        int depthLevel = 1; // default - direct reply to post
        if (request.getParentCommentId() != null) {
            Comment parent = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Comment", request.getParentCommentId()));
            depthLevel = parent.getDepthLevel() + 1;
        }

        if (request.getAuthorType() == Post.AuthorType.BOT) {
            // Vertical Cap check
            if (depthLevel > 20) {
                throw new TooManyRequestsException("Bot reply depth limit of 20 reached");
            }

            // Horizontal Cap check
            if (!guardrailService.checkAndIncrementBotCount(postId)) {
                throw new TooManyRequestsException("Bot reply limit of 100 reached for post " + postId);
            }

//             Cooldown Cap check — only if post belongs to a human
            if (post.getAuthorType() == Post.AuthorType.USER) {
                if (!guardrailService.checkAndSetCooldown(request.getAuthorId(), post.getAuthorId())) {
                    throw new TooManyRequestsException(
                            "Bot " + request.getAuthorId() + " is on cooldown for human " + post.getAuthorId()
                    );
                }
            }
        }

        Comment comment = Comment.builder()
                .post(post)
                .authorType(request.getAuthorType())
                .authorId(request.getAuthorId())
                .content(request.getContent())
                .depthLevel(depthLevel)
                .build();

        Comment saved = commentRepository.save(comment);
        log.info("Added comment id={} to post id={}", saved.getId(), postId);

        if (request.getAuthorType() == Post.AuthorType.USER) {
            viralityService.incrementHumanComment(postId);
        } else {
            viralityService.incrementBotReply(postId);
        }

        // Notification (only when bot comments on human's post)
        if (request.getAuthorType() == Post.AuthorType.BOT
                && post.getAuthorType() == Post.AuthorType.USER) {
            Bot bot = botRepository.findById(request.getAuthorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bot", request.getAuthorId()));
            notificationService.handleBotInteractionNotification(post.getAuthorId(), bot.getName());
        }

        log.info("Added comment id={} to post id={} at depthLevel={}", saved.getId(), postId, depthLevel);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPost(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post", postId);
        }
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CommentResponse getCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));
        return toResponse(comment);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new ResourceNotFoundException("Comment", commentId);
        }
        commentRepository.deleteById(commentId);
        log.info("Deleted comment id={}", commentId);
    }

    private CommentResponse toResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .authorType(comment.getAuthorType())
                .authorId(comment.getAuthorId())
                .content(comment.getContent())
                .depthLevel(comment.getDepthLevel())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
