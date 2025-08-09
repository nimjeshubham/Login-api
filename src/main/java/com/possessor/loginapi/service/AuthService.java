package com.possessor.loginapi.service;

import com.possessor.loginapi.client.TokenClient;
import com.possessor.loginapi.dto.*;
import com.possessor.loginapi.entity.User;
import com.possessor.loginapi.exception.AuthenticationException;
import com.possessor.loginapi.exception.UserAlreadyExistsException;
import com.possessor.loginapi.repository.UserRepository;
import com.possessor.loginapi.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final TokenClient tokenClient;
    
    public Mono<MessageResponse> register(RegisterRequest request) {
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
                    log.info("User registered successfully: {}", user.getUsername());
                    return new MessageResponse("User created successfully");
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
                        log.info("User credentials validated successfully: {}", user.getUsername());
                        return tokenClient.generateToken(user.getUsername(), user.getEmail())
                                .map(tokenResponse -> new AuthResponse(
                                    user.getUsername(),
                                    user.getEmail(),
                                    tokenResponse.getAccessToken(),
                                    tokenResponse.getRefreshToken(),
                                    tokenResponse.getTokenType(),
                                    tokenResponse.getExpiresIn()
                                ));
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
    
    public Mono<MessageResponse> requestPasswordReset(String email) {
        return userRepository.findByEmail(email.toLowerCase())
                .flatMap(user -> {
                    String resetToken = UUID.randomUUID().toString();
                    user.setResetToken(resetToken);
                    user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
                    
                    return userRepository.save(user)
                            .then(emailService.sendPasswordResetEmail(user.getEmail(), resetToken));
                })
                .then(Mono.just(new MessageResponse("Password reset email sent")))
                .onErrorReturn(new MessageResponse("If email exists, reset link will be sent"))
                .doOnSuccess(response -> log.info("Password reset requested for email: {}", email));
    }
    
    @CacheEvict(value = "users", allEntries = true)
    public Mono<MessageResponse> resetPassword(String token, String newPassword) {
        return userRepository.findByResetToken(token)
                .switchIfEmpty(Mono.error(new AuthenticationException("Invalid or expired reset token")))
                .flatMap(user -> {
                    if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
                        return Mono.error(new AuthenticationException("Reset token has expired"));
                    }
                    
                    user.setPassword(passwordEncoder.encode(newPassword));
                    user.setResetToken(null);
                    user.setResetTokenExpiry(null);
                    user.setUpdatedAt(LocalDateTime.now());
                    
                    return userRepository.save(user);
                })
                .map(user -> new MessageResponse("Password reset successfully"))
                .doOnSuccess(response -> log.info("Password reset completed for token: {}", token));
    }
    
    public Mono<AuthResponse> refreshToken(String refreshToken) {
        return tokenClient.refreshToken(refreshToken)
                .flatMap(tokenResponse -> {
                    String username = jwtUtil.getUsernameFromToken(refreshToken);
                    return findByUsername(username)
                            .map(user -> new AuthResponse(
                                user.getUsername(),
                                user.getEmail(),
                                tokenResponse.getAccessToken(),
                                tokenResponse.getRefreshToken(),
                                tokenResponse.getTokenType(),
                                tokenResponse.getExpiresIn()
                            ));
                })
                .switchIfEmpty(Mono.error(new AuthenticationException("User not found")));
    }
    
    public Mono<MessageResponse> logout(String token) {
        // In a real implementation, you would add the token to a blacklist
        return Mono.just(new MessageResponse("Logged out successfully"));
    }
    
    public Mono<AvailabilityResponse> checkUsernameAvailability(String username) {
        return userRepository.existsByUsername(username.toLowerCase())
                .map(exists -> new AvailabilityResponse(!exists, 
                    exists ? "Username is already taken" : "Username is available"));
    }
    
    public Mono<AvailabilityResponse> checkEmailAvailability(String email) {
        return userRepository.existsByEmail(email.toLowerCase())
                .map(exists -> new AvailabilityResponse(!exists,
                    exists ? "Email is already registered" : "Email is available"));
    }
    
    @CacheEvict(value = "users", allEntries = true)
    public Mono<MessageResponse> verifyEmail(String token) {
        return userRepository.findByVerificationToken(token)
                .switchIfEmpty(Mono.error(new AuthenticationException("Invalid verification token")))
                .flatMap(user -> {
                    user.setEmailVerified(true);
                    user.setVerificationToken(null);
                    user.setUpdatedAt(LocalDateTime.now());
                    
                    return userRepository.save(user);
                })
                .map(user -> new MessageResponse("Email verified successfully"))
                .doOnSuccess(response -> log.info("Email verified for token: {}", token));
    }
    
    public Mono<MessageResponse> resendVerificationEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase())
                .switchIfEmpty(Mono.error(new AuthenticationException("Email not found")))
                .flatMap(user -> {
                    if (user.isEmailVerified()) {
                        return Mono.just(new MessageResponse("Email is already verified"));
                    }
                    
                    String verificationToken = UUID.randomUUID().toString();
                    user.setVerificationToken(verificationToken);
                    
                    return userRepository.save(user)
                            .then(Mono.just(new MessageResponse("Verification email sent")));
                })
                .doOnSuccess(response -> log.info("Verification email resent for: {}", email));
    }
}