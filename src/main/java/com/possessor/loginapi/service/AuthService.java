package com.possessor.loginapi.service;

import com.possessor.loginapi.client.TokenClient;
import com.possessor.loginapi.constants.AuthConstants;
import com.possessor.loginapi.constants.ErrorMessages;
import com.possessor.loginapi.constants.LogMessages;
import com.possessor.loginapi.constants.StatusMessages;
import com.possessor.loginapi.constants.SuccessMessages;
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
    private final SessionService sessionService;
    
    public Mono<MessageResponse> register(RegisterRequest request) {
        log.info(LogMessages.REGISTRATION_ATTEMPT, request.getUsername());
        
        return userRepository.existsByUsername(request.getUsername())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new UserAlreadyExistsException(ErrorMessages.USERNAME_EXISTS_ERROR));
                    }
                    return userRepository.existsByEmail(request.getEmail());
                })
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new UserAlreadyExistsException(ErrorMessages.EMAIL_EXISTS_ERROR));
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
                    log.info(LogMessages.USER_REGISTERED_SUCCESS, user.getUsername());
                    return new MessageResponse(SuccessMessages.USER_CREATED_SUCCESS);
                })
                .retryWhen(Retry.backoff(AuthConstants.MAX_RETRY_ATTEMPTS, Duration.ofMillis(AuthConstants.RETRY_BACKOFF_MILLIS)))
                .doOnError(error -> log.error(LogMessages.REGISTRATION_FAILED, request.getUsername(), error));
    }
    
    public Mono<AuthResponse> login(LoginRequest request) {
        log.info(LogMessages.LOGIN_ATTEMPT, request.getUsername());
        
        return userRepository.findByUsername(request.getUsername().toLowerCase())
                .switchIfEmpty(Mono.error(new AuthenticationException(ErrorMessages.INVALID_CREDENTIALS_ERROR)))
                .flatMap(user -> {
                    if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        log.info(LogMessages.CREDENTIALS_VALIDATED, user.getUsername());
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
                    log.warn(LogMessages.INVALID_PASSWORD, request.getUsername());
                    return Mono.error(new AuthenticationException(ErrorMessages.INVALID_CREDENTIALS_ERROR));
                })
                .doOnError(error -> log.error(LogMessages.LOGIN_FAILED, request.getUsername(), error.getMessage()));
    }
    
    @Cacheable(AuthConstants.USERS_CACHE)
    public Mono<User> findByUsername(String username) {
        return userRepository.findByUsername(username.toLowerCase());
    }
    
    public Mono<MessageResponse> requestPasswordReset(String email) {
        return userRepository.findByEmail(email.toLowerCase())
                .flatMap(user -> {
                    String resetToken = UUID.randomUUID().toString();
                    user.setResetToken(resetToken);
                    user.setResetTokenExpiry(LocalDateTime.now().plusHours(AuthConstants.RESET_TOKEN_EXPIRY_HOURS));
                    
                    return userRepository.save(user)
                            .then(emailService.sendPasswordResetEmail(user.getEmail(), resetToken));
                })
                .then(Mono.just(new MessageResponse(SuccessMessages.PASSWORD_RESET_EMAIL_SENT)))
                .onErrorReturn(new MessageResponse(SuccessMessages.PASSWORD_RESET_EMAIL_FALLBACK))
                .doOnSuccess(response -> log.info(LogMessages.PASSWORD_RESET_REQUESTED, email));
    }
    
    @CacheEvict(value = AuthConstants.USERS_CACHE, allEntries = true)
    public Mono<MessageResponse> resetPassword(String token, String newPassword) {
        return userRepository.findByResetToken(token)
                .switchIfEmpty(Mono.error(new AuthenticationException(ErrorMessages.INVALID_RESET_TOKEN_ERROR)))
                .flatMap(user -> {
                    if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
                        return Mono.error(new AuthenticationException(ErrorMessages.RESET_TOKEN_EXPIRED_ERROR));
                    }
                    
                    user.setPassword(passwordEncoder.encode(newPassword));
                    user.setResetToken(null);
                    user.setResetTokenExpiry(null);
                    user.setUpdatedAt(LocalDateTime.now());
                    
                    return userRepository.save(user);
                })
                .map(user -> new MessageResponse(SuccessMessages.PASSWORD_RESET_SUCCESS))
                .doOnSuccess(response -> log.info(LogMessages.PASSWORD_RESET_COMPLETED, token));
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
                .switchIfEmpty(Mono.error(new AuthenticationException(ErrorMessages.USER_NOT_FOUND_ERROR)));
    }
    
    public Mono<MessageResponse> logout(String token) {
        return sessionService.blacklistToken(token, Duration.ofHours(24))
            .then(Mono.fromCallable(() -> {
                String username = jwtUtil.getUsernameFromToken(token);
                return sessionService.invalidateUserSession(username);
            }).flatMap(mono -> mono))
            .then(Mono.just(new MessageResponse(SuccessMessages.LOGOUT_SUCCESS)))
            .doOnSuccess(response -> log.info("User logged out successfully"))
            .onErrorReturn(new MessageResponse(SuccessMessages.LOGOUT_SUCCESS));
    }
    
    public Mono<AvailabilityResponse> checkUsernameAvailability(String username) {
        return userRepository.existsByUsername(username.toLowerCase())
                .map(exists -> new AvailabilityResponse(!exists,
                        Boolean.TRUE.equals(exists) ? StatusMessages.USERNAME_TAKEN : StatusMessages.USERNAME_AVAILABLE));
    }
    
    public Mono<AvailabilityResponse> checkEmailAvailability(String email) {
        return userRepository.existsByEmail(email.toLowerCase())
                .map(exists -> new AvailabilityResponse(!exists,
                        Boolean.TRUE.equals(exists) ? StatusMessages.EMAIL_REGISTERED : StatusMessages.EMAIL_AVAILABLE));
    }
    
    @CacheEvict(value = AuthConstants.USERS_CACHE, allEntries = true)
    public Mono<MessageResponse> verifyEmail(String token) {
        return userRepository.findByVerificationToken(token)
                .switchIfEmpty(Mono.error(new AuthenticationException(ErrorMessages.INVALID_VERIFICATION_TOKEN_ERROR)))
                .flatMap(user -> {
                    user.setEmailVerified(true);
                    user.setVerificationToken(null);
                    user.setUpdatedAt(LocalDateTime.now());
                    
                    return userRepository.save(user);
                })
                .map(user -> new MessageResponse(SuccessMessages.EMAIL_VERIFIED_SUCCESS))
                .doOnSuccess(response -> log.info(LogMessages.EMAIL_VERIFIED, token));
    }
    
    public Mono<MessageResponse> resendVerificationEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase())
                .switchIfEmpty(Mono.error(new AuthenticationException(AuthConstants.USER_NOT_FOUND_ERROR)))
                .flatMap(user -> {
                    if (user.isEmailVerified()) {
                        return Mono.just(new MessageResponse("Email is already verified"));
                    }
                    
                    String verificationToken = UUID.randomUUID().toString();
                    user.setVerificationToken(verificationToken);
                    user.setUpdatedAt(LocalDateTime.now());
                    
                    return userRepository.save(user)
                            .then(emailService.sendVerificationEmail(user.getEmail(), verificationToken));
                })
                .then(Mono.just(new MessageResponse(AuthConstants.VERIFICATION_EMAIL_SENT)))
                .onErrorReturn(new MessageResponse(AuthConstants.VERIFICATION_EMAIL_SENT))
                .doOnSuccess(response -> log.info("Verification email resent for: {}", email));
    }
}