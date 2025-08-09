package com.possessor.loginapi.client;

import com.possessor.loginapi.dto.TokenRequest;
import com.possessor.loginapi.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenClient {
    
    private final WebClient.Builder webClientBuilder;
    
    @Value("${token.service.url:http://localhost:8081}")
    private String tokenServiceUrl;
    
    public Mono<TokenResponse> generateToken(String username, String email) {
        TokenRequest request = new TokenRequest(username, email);
        
        return webClientBuilder.build()
                .post()
                .uri(tokenServiceUrl + "/api/auth/token")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .doOnSuccess(response -> log.info("Token generated for user: {}", username))
                .doOnError(error -> log.error("Failed to generate token for user: {}", username, error));
    }
    
    public Mono<TokenResponse> refreshToken(String refreshToken) {
        return webClientBuilder.build()
                .post()
                .uri(tokenServiceUrl + "/api/auth/token/refresh")
                .bodyValue(refreshToken)
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .doOnSuccess(response -> log.info("Token refreshed successfully"))
                .doOnError(error -> log.error("Failed to refresh token", error));
    }
}