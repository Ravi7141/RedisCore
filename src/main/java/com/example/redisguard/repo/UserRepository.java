package com.example.redisguard.repo;

import com.example.redisguard.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);

    boolean existsByUsernameIgnoreCase(@NotBlank(message = "username must not be blank") @Size(max = 100, message = "username must be at most 100 characters") String username);
}