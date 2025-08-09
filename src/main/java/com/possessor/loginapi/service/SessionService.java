package com.possessor.loginapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {
    
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    
    public Mono<Void> blacklistToken(String token, Duration expiration) {
        String key = "blacklist:" + token;
        return redisTemplate.opsForValue()
            .set(key, "true", expiration)
            .doOnSuccess(result -> log.info("Token blacklisted: {}", token.substring(0, 10) + "..."))
            .then();
    }
    
    public Mono<Boolean> isTokenBlacklisted(String token) {
        String key = "blacklist:" + token;
        return redisTemplate.hasKey(key)
            .onErrorReturn(false);
    }
    
    public Mono<Void> storeUserSession(String userId, String sessionData, Duration expiration) {
        String key = "session:" + userId;
        return redisTemplate.opsForValue()
            .set(key, sessionData, expiration)
            .then();
    }
    
    public Mono<String> getUserSession(String userId) {
        String key = "session:" + userId;
        return redisTemplate.opsForValue()
            .get(key)
            .onErrorReturn("");
    }
    
    public Mono<Void> invalidateUserSession(String userId) {
        String key = "session:" + userId;
        return redisTemplate.delete(key)
            .then();
    }
}