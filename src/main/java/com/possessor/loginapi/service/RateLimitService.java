package com.possessor.loginapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    
    public Mono<Boolean> isAllowed(String key, int maxRequests, Duration window) {
        String redisKey = "rate_limit:" + key;
        
        return redisTemplate.opsForValue()
            .get(redisKey)
            .cast(String.class)
            .map(Integer::parseInt)
            .defaultIfEmpty(0)
            .flatMap(currentCount -> {
                if (currentCount >= maxRequests) {
                    return Mono.just(false);
                }
                
                return redisTemplate.opsForValue()
                    .increment(redisKey)
                    .flatMap(newCount -> {
                        if (newCount == 1) {
                            return redisTemplate.expire(redisKey, window)
                                .then(Mono.just(true));
                        }
                        return Mono.just(true);
                    });
            })
            .doOnError(error -> log.error("Redis rate limiting error for key: {}", key, error))
            .onErrorReturn(true); // Fail open on Redis errors
    }
    
    public Mono<Long> getRemainingRequests(String key, int maxRequests) {
        String redisKey = "rate_limit:" + key;
        
        return redisTemplate.opsForValue()
            .get(redisKey)
            .cast(String.class)
            .map(Integer::parseInt)
            .defaultIfEmpty(0)
            .map(currentCount -> (long) Math.max(0, maxRequests - currentCount))
            .onErrorReturn((long) maxRequests);
    }
}