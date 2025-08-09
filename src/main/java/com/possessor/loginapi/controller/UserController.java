package com.possessor.loginapi.controller;

import com.possessor.loginapi.constants.ApiEndpoints;
import com.possessor.loginapi.constants.MetricsConstants;
import com.possessor.loginapi.dto.ChangePasswordRequest;
import com.possessor.loginapi.dto.MessageResponse;
import com.possessor.loginapi.dto.UpdateProfileRequest;
import com.possessor.loginapi.entity.User;
import com.possessor.loginapi.service.AuthService;
import com.possessor.loginapi.service.UserService;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(ApiEndpoints.USER_BASE)
@RequiredArgsConstructor
@Slf4j
public class UserController {
    
    private final AuthService authService;
    private final UserService userService;
    
    @GetMapping(ApiEndpoints.USER_PROFILE)
    @Timed(value = MetricsConstants.USER_PROFILE, description = MetricsConstants.USER_PROFILE_DESC)
    public Mono<ResponseEntity<User>> getProfile(Authentication authentication) {
        String username = authentication.getName();
        return authService.findByUsername(username)
                .map(user -> {
                    user.setPassword(null);
                    user.setResetToken(null);
                    user.setVerificationToken(null);
                    return ResponseEntity.ok(user);
                })
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }
    
    @PutMapping(ApiEndpoints.USER_PROFILE)
    @Timed(value = MetricsConstants.USER_UPDATE_PROFILE, description = MetricsConstants.USER_UPDATE_PROFILE_DESC)
    public Mono<ResponseEntity<MessageResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        return userService.updateProfile(username, request)
                .map(ResponseEntity::ok);
    }
    
    @PutMapping(ApiEndpoints.USER_PASSWORD)
    @Timed(value = MetricsConstants.USER_CHANGE_PASSWORD, description = MetricsConstants.USER_CHANGE_PASSWORD_DESC)
    public Mono<ResponseEntity<MessageResponse>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        return userService.changePassword(username, request)
                .map(ResponseEntity::ok);
    }
    
    @DeleteMapping(ApiEndpoints.USER_ACCOUNT)
    @Timed(value = MetricsConstants.USER_DELETE_ACCOUNT, description = MetricsConstants.USER_DELETE_ACCOUNT_DESC)
    public Mono<ResponseEntity<MessageResponse>> deleteAccount(Authentication authentication) {
        String username = authentication.getName();
        return userService.deleteAccount(username)
                .map(ResponseEntity::ok);
    }
}