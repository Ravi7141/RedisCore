package com.example.redisguard.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserRequest {

    @NotBlank(message = "username must not be blank")
    @Size(max = 100, message = "username must be at most 100 characters")
    private String username;

    private Boolean isPremium = false;
}
