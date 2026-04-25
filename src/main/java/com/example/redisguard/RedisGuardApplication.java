package com.example.redisguard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RedisGuardApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisGuardApplication.class, args);
    }

}
