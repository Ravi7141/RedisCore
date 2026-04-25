package com.example.redisguard.service;

import com.example.redisguard.dto.request.CreateCommentRequest;
import com.example.redisguard.dto.response.CommentResponse;
import com.example.redisguard.entity.Bot;
import com.example.redisguard.entity.Comment;
import com.example.redisguard.entity.Post;
import com.example.redisguard.exception.ResourceNotFoundException;
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


        Comment comment = Comment.builder()
                .post(post)
                .authorType(request.getAuthorType())
                .authorId(request.getAuthorId())
                .content(request.getContent())
                .depthLevel(depthLevel)
                .build();

        Comment saved = commentRepository.save(comment);
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
