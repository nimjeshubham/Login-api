package com.possessor.loginapi.controller;

import com.possessor.loginapi.constants.ApiEndpoints;
import com.possessor.loginapi.constants.MetricsConstants;
import com.possessor.loginapi.dto.*;
import com.possessor.loginapi.service.AuthService;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.UUID;

@RestController
@RequestMapping(ApiEndpoints.AUTH_BASE + "/v1")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AuthController {
    
    private final AuthService authService;
    private final Counter loginAttempts;
    private final Counter registrationAttempts;
    
    public AuthController(AuthService authService, MeterRegistry meterRegistry) {
        this.authService = authService;
        this.loginAttempts = Counter.builder("auth.login.attempts")
            .description("Total login attempts")
            .register(meterRegistry);
        this.registrationAttempts = Counter.builder("auth.registration.attempts")
            .description("Total registration attempts")
            .register(meterRegistry);
    }
    
    @PostMapping(ApiEndpoints.AUTH_REGISTER)
    @Timed(value = MetricsConstants.AUTH_REGISTER, description = MetricsConstants.AUTH_REGISTER_DESC)
    public Mono<ResponseEntity<MessageResponse>> register(@Valid @RequestBody RegisterRequest request) {
        String correlationId = UUID.randomUUID().toString();
        registrationAttempts.increment();
        
        // Sanitize inputs
        request.setUsername(request.getUsername().trim().toLowerCase());
        request.setEmail(request.getEmail().trim().toLowerCase());
        
        return authService.register(request)
                .contextWrite(Context.of("correlationId", correlationId))
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .doOnSuccess(response -> log.info("Registration successful for user: {} [{}]", 
                    request.getUsername(), correlationId))
                .doOnError(error -> log.error("Registration failed for user: {} [{}] - {}", 
                    request.getUsername(), correlationId, error.getMessage()));
    }
    
    @PostMapping(ApiEndpoints.AUTH_LOGIN)
    @Timed(value = MetricsConstants.AUTH_LOGIN, description = MetricsConstants.AUTH_LOGIN_DESC)
    public Mono<ResponseEntity<AuthResponse>> login(@Valid @RequestBody LoginRequest request,
                                                   @RequestHeader(value = "User-Agent", required = false) String userAgent,
                                                   @RequestHeader(value = "X-Forwarded-For", required = false) String clientIp) {
        String correlationId = UUID.randomUUID().toString();
        loginAttempts.increment();
        
        // Sanitize input to prevent injection attacks
        request.setUsername(request.getUsername().trim().toLowerCase());
        
        log.info("Login attempt for user: {} from IP: {} [{}]", 
            request.getUsername(), clientIp != null ? clientIp : "unknown", correlationId);
        
        return authService.login(request)
                .contextWrite(Context.of("correlationId", correlationId))
                .map(response -> {
                    log.info("Successful login for user: {} [{}]", request.getUsername(), correlationId);
                    return ResponseEntity.ok()
                        .header("X-Correlation-ID", correlationId)
                        .body(response);
                })
                .doOnError(error -> {
                    log.warn("Failed login attempt for user: {} from IP: {} [{}] - {}", 
                        request.getUsername(), clientIp != null ? clientIp : "unknown", 
                        correlationId, error.getMessage());
                });
    }
    
    @GetMapping(ApiEndpoints.AUTH_HEALTH)
    public Mono<ResponseEntity<String>> health() {
        return Mono.just(ResponseEntity.ok("Authentication service is healthy"));
    }
    
    @PostMapping(ApiEndpoints.AUTH_FORGOT_PASSWORD)
    @Timed(value = MetricsConstants.AUTH_FORGOT_PASSWORD, description = MetricsConstants.AUTH_FORGOT_PASSWORD_DESC)
    public Mono<ResponseEntity<MessageResponse>> forgotPassword(@Valid @RequestBody PasswordResetRequest request) {
        return authService.requestPasswordReset(request.getEmail())
                .map(ResponseEntity::ok);
    }
    
    @PostMapping(ApiEndpoints.AUTH_RESET_PASSWORD)
    @Timed(value = MetricsConstants.AUTH_RESET_PASSWORD, description = MetricsConstants.AUTH_RESET_PASSWORD_DESC)
    public Mono<ResponseEntity<MessageResponse>> resetPassword(@Valid @RequestBody PasswordResetConfirmRequest request) {
        return authService.resetPassword(request.getToken(), request.getNewPassword())
                .map(ResponseEntity::ok);
    }
    
    @PostMapping(ApiEndpoints.AUTH_REFRESH)
    @Timed(value = MetricsConstants.AUTH_REFRESH, description = MetricsConstants.AUTH_REFRESH_DESC)
    public Mono<ResponseEntity<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refreshToken(request.getRefreshToken())
                .map(ResponseEntity::ok);
    }
    
    @PostMapping(ApiEndpoints.AUTH_LOGOUT)
    @Timed(value = MetricsConstants.AUTH_LOGOUT, description = MetricsConstants.AUTH_LOGOUT_DESC)
    public Mono<ResponseEntity<MessageResponse>> logout(@RequestHeader("Authorization") String token) {
        return authService.logout(token.replace("Bearer ", ""))
                .map(ResponseEntity::ok);
    }
    
    @GetMapping(ApiEndpoints.AUTH_CHECK_USERNAME)
    @Timed(value = MetricsConstants.AUTH_CHECK_USERNAME, description = MetricsConstants.AUTH_CHECK_USERNAME_DESC)
    public Mono<ResponseEntity<AvailabilityResponse>> checkUsername(
            @PathVariable @Size(min = 3, max = 50) String username) {
        return authService.checkUsernameAvailability(username.trim().toLowerCase())
                .map(ResponseEntity::ok);
    }
    
    @GetMapping(ApiEndpoints.AUTH_CHECK_EMAIL)
    @Timed(value = MetricsConstants.AUTH_CHECK_EMAIL, description = MetricsConstants.AUTH_CHECK_EMAIL_DESC)
    public Mono<ResponseEntity<AvailabilityResponse>> checkEmail(
            @PathVariable @Email @Size(max = 100) String email) {
        return authService.checkEmailAvailability(email.trim().toLowerCase())
                .map(ResponseEntity::ok);
    }
    
    @PostMapping(ApiEndpoints.AUTH_VERIFY_EMAIL)
    @Timed(value = MetricsConstants.AUTH_VERIFY_EMAIL, description = MetricsConstants.AUTH_VERIFY_EMAIL_DESC)
    public Mono<ResponseEntity<MessageResponse>> verifyEmail(@PathVariable String token) {
        return authService.verifyEmail(token)
                .map(ResponseEntity::ok);
    }
    
    @PostMapping(ApiEndpoints.AUTH_RESEND_VERIFICATION)
    @Timed(value = MetricsConstants.AUTH_RESEND_VERIFICATION, description = MetricsConstants.AUTH_RESEND_VERIFICATION_DESC)
    public Mono<ResponseEntity<MessageResponse>> resendVerification(@RequestParam String email) {
        return authService.resendVerificationEmail(email)
                .map(ResponseEntity::ok);
    }
}