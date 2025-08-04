package com.possessor.loginapi.service;

import com.possessor.loginapi.dto.AuthResponse;
import com.possessor.loginapi.dto.LoginRequest;
import com.possessor.loginapi.dto.RegisterRequest;
import com.possessor.loginapi.entity.User;
import com.possessor.loginapi.exception.AuthenticationException;
import com.possessor.loginapi.exception.UserAlreadyExistsException;
import com.possessor.loginapi.repository.UserRepository;
import com.possessor.loginapi.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    public Mono<AuthResponse> register(RegisterRequest request) {
        log.info("Registration attempt for username: {}", request.getUsername());
        
        return userRepository.existsByUsername(request.getUsername())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new UserAlreadyExistsException("Username already exists"));
                    }
                    return userRepository.existsByEmail(request.getEmail());
                })
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new UserAlreadyExistsException("Email already exists"));
                    }
                    
                    User user = new User();
                    user.setUsername(request.getUsername().toLowerCase());
                    user.setEmail(request.getEmail().toLowerCase());
                    user.setPassword(passwordEncoder.encode(request.getPassword()));
                    user.setCreatedAt(LocalDateTime.now());
                    user.setUpdatedAt(LocalDateTime.now());
                    
                    return userRepository.save(user);
                })
                .map(user -> {
                    String token = jwtUtil.generateToken(user.getUsername());
                    log.info("User registered successfully: {}", user.getUsername());
                    return new AuthResponse(token, user.getUsername(), user.getEmail());
                })
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100)))
                .doOnError(error -> log.error("Registration failed for username: {}", request.getUsername(), error));
    }
    
    public Mono<AuthResponse> login(LoginRequest request) {
        log.info("Login attempt for username: {}", request.getUsername());
        
        return userRepository.findByUsername(request.getUsername().toLowerCase())
                .switchIfEmpty(Mono.error(new AuthenticationException("Invalid credentials")))
                .flatMap(user -> {
                    if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        String token = jwtUtil.generateToken(user.getUsername());
                        log.info("User logged in successfully: {}", user.getUsername());
                        return Mono.just(new AuthResponse(token, user.getUsername(), user.getEmail()));
                    }
                    log.warn("Invalid password for username: {}", request.getUsername());
                    return Mono.error(new AuthenticationException("Invalid credentials"));
                })
                .doOnError(error -> log.error("Login failed for username: {}", request.getUsername(), error.getMessage()));
    }
    
    @Cacheable("users")
    public Mono<User> findByUsername(String username) {
        return userRepository.findByUsername(username.toLowerCase());
    }
}