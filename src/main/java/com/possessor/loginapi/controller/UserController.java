package com.possessor.loginapi.controller;

import com.possessor.loginapi.dto.ChangePasswordRequest;
import com.possessor.loginapi.dto.MessageResponse;
import com.possessor.loginapi.dto.UpdateProfileRequest;
import com.possessor.loginapi.entity.User;
import com.possessor.loginapi.service.AuthService;
import com.possessor.loginapi.service.UserService;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    
    private final AuthService authService;
    private final UserService userService;
    
    @GetMapping("/profile")
    @Timed(value = "user.profile", description = "Time taken to get user profile")
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
    
    @PutMapping("/profile")
    @Timed(value = "user.update.profile", description = "Time taken to update user profile")
    public Mono<ResponseEntity<MessageResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        return userService.updateProfile(username, request)
                .map(ResponseEntity::ok);
    }
    
    @PutMapping("/password")
    @Timed(value = "user.change.password", description = "Time taken to change password")
    public Mono<ResponseEntity<MessageResponse>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        return userService.changePassword(username, request)
                .map(ResponseEntity::ok);
    }
    
    @DeleteMapping("/account")
    @Timed(value = "user.delete.account", description = "Time taken to delete account")
    public Mono<ResponseEntity<MessageResponse>> deleteAccount(Authentication authentication) {
        String username = authentication.getName();
        return userService.deleteAccount(username)
                .map(ResponseEntity::ok);
    }
}