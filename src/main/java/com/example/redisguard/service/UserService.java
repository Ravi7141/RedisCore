package com.example.redisguard.service;

import com.example.redisguard.dto.request.CreateUserRequest;
import com.example.redisguard.dto.response.UserResponse;
import com.example.redisguard.entity.User;
import com.example.redisguard.exception.ResourceNotFoundException;
import com.example.redisguard.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new IllegalArgumentException(
                    "Username '" + request.getUsername() + "' is already taken");
        }

        User user = User.builder()
                .username(request.getUsername().trim())
                .isPremium(request.getIsPremium() != null ? request.getIsPremium() : false)
                .build();

        User saved = userRepository.save(user);
        log.info("Created user id={} username={}", saved.getId(), saved.getUsername());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return toResponse(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", id);
        }
        userRepository.deleteById(id);
        log.info("Deleted user id={}", id);
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .isPremium(user.getIsPremium())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
