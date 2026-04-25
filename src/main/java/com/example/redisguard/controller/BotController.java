package com.example.redisguard.controller;

import com.example.redisguard.dto.request.CreateBotRequest;
import com.example.redisguard.dto.response.ApiResponse;
import com.example.redisguard.dto.response.BotResponse;
import com.example.redisguard.service.BotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bots")
@RequiredArgsConstructor
public class BotController {

    private final BotService botService;

    @PostMapping
    public ResponseEntity<ApiResponse<BotResponse>> createBot(
            @Valid @RequestBody CreateBotRequest request) {
        BotResponse response = botService.createBot(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bot created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BotResponse>>> getAllBots() {
        List<BotResponse> bots = botService.getAllBots();
        return ResponseEntity
                .ok(ApiResponse.success("Bots retrieved successfully", bots));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BotResponse>> getBotById(@PathVariable Long id) {
        BotResponse response = botService.getBotById(id);
        return ResponseEntity
                .ok(ApiResponse.success("Bot retrieved successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBot(@PathVariable Long id) {
        botService.deleteBot(id);
        return ResponseEntity
                .ok(ApiResponse.success("Bot deleted successfully", null));
    }
}