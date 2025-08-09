package com.possessor.loginapi.controller;

import com.possessor.loginapi.dto.*;
import com.possessor.loginapi.dto.AuthResponse;
import com.possessor.loginapi.dto.LoginRequest;
import com.possessor.loginapi.dto.MessageResponse;
import com.possessor.loginapi.dto.RegisterRequest;
import com.possessor.loginapi.service.AuthService;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/register")
    @Timed(value = "auth.register", description = "Time taken to register user")
    public Mono<ResponseEntity<MessageResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }
    
    @PostMapping("/login")
    @Timed(value = "auth.login", description = "Time taken to login user")
    public Mono<ResponseEntity<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request)
                .map(ResponseEntity::ok);
    }
    
    @GetMapping("/health")
    public Mono<ResponseEntity<String>> health() {
        return Mono.just(ResponseEntity.ok("Authentication service is healthy"));
    }
    
    @PostMapping("/forgot-password")
    @Timed(value = "auth.forgot.password", description = "Time taken to process forgot password")
    public Mono<ResponseEntity<MessageResponse>> forgotPassword(@Valid @RequestBody PasswordResetRequest request) {
        return authService.requestPasswordReset(request.getEmail())
                .map(ResponseEntity::ok);
    }
    
    @PostMapping("/reset-password")
    @Timed(value = "auth.reset.password", description = "Time taken to reset password")
    public Mono<ResponseEntity<MessageResponse>> resetPassword(@Valid @RequestBody PasswordResetConfirmRequest request) {
        return authService.resetPassword(request.getToken(), request.getNewPassword())
                .map(ResponseEntity::ok);
    }
    
    @PostMapping("/refresh")
    @Timed(value = "auth.refresh", description = "Time taken to refresh token")
    public Mono<ResponseEntity<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refreshToken(request.getRefreshToken())
                .map(ResponseEntity::ok);
    }
    
    @PostMapping("/logout")
    @Timed(value = "auth.logout", description = "Time taken to logout")
    public Mono<ResponseEntity<MessageResponse>> logout(@RequestHeader("Authorization") String token) {
        return authService.logout(token.replace("Bearer ", ""))
                .map(ResponseEntity::ok);
    }
    
    @GetMapping("/check-username/{username}")
    @Timed(value = "auth.check.username", description = "Time taken to check username availability")
    public Mono<ResponseEntity<AvailabilityResponse>> checkUsername(@PathVariable String username) {
        return authService.checkUsernameAvailability(username)
                .map(ResponseEntity::ok);
    }
    
    @GetMapping("/check-email/{email}")
    @Timed(value = "auth.check.email", description = "Time taken to check email availability")
    public Mono<ResponseEntity<AvailabilityResponse>> checkEmail(@PathVariable String email) {
        return authService.checkEmailAvailability(email)
                .map(ResponseEntity::ok);
    }
    
    @PostMapping("/verify-email/{token}")
    @Timed(value = "auth.verify.email", description = "Time taken to verify email")
    public Mono<ResponseEntity<MessageResponse>> verifyEmail(@PathVariable String token) {
        return authService.verifyEmail(token)
                .map(ResponseEntity::ok);
    }
    
    @PostMapping("/resend-verification")
    @Timed(value = "auth.resend.verification", description = "Time taken to resend verification")
    public Mono<ResponseEntity<MessageResponse>> resendVerification(@RequestParam String email) {
        return authService.resendVerificationEmail(email)
                .map(ResponseEntity::ok);
    }
}