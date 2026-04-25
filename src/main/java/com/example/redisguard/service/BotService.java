package com.example.redisguard.service;

import com.example.redisguard.dto.request.CreateBotRequest;
import com.example.redisguard.dto.response.BotResponse;
import com.example.redisguard.entity.Bot;
import com.example.redisguard.exception.ResourceNotFoundException;
import com.example.redisguard.repo.BotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BotService {

    private final BotRepository botRepository;

    @Transactional
    public BotResponse createBot(CreateBotRequest request) {
        Bot bot = Bot.builder()
                .name(request.getName().trim())
                .personaDescription(request.getPersonaDescription())
                .build();

        Bot saved = botRepository.save(bot);
        log.info("Created bot id={} name={}", saved.getId(), saved.getName());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<BotResponse> getAllBots() {
        return botRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BotResponse getBotById(Long id) {
        Bot bot = botRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bot", id));
        return toResponse(bot);
    }

    @Transactional
    public void deleteBot(Long id) {
        if (!botRepository.existsById(id)) {
            throw new ResourceNotFoundException("Bot", id);
        }
        botRepository.deleteById(id);
        log.info("Deleted bot id={}", id);
    }

    private BotResponse toResponse(Bot bot) {
        return BotResponse.builder()
                .id(bot.getId())
                .name(bot.getName())
                .personaDescription(bot.getPersonaDescription())
                .createdAt(bot.getCreatedAt())
                .build();
    }
}
