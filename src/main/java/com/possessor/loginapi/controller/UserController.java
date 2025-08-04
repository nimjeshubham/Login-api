package com.possessor.loginapi.controller;

import com.possessor.loginapi.entity.User;
import com.possessor.loginapi.service.AuthService;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    
    private final AuthService authService;
    
    @GetMapping("/profile")
    @Timed(name = "user.profile", description = "Time taken to get user profile")
    public Mono<ResponseEntity<User>> getProfile(Authentication authentication) {
        String username = authentication.getName();
        return authService.findByUsername(username)
                .map(user -> {
                    user.setPassword(null); // Don't expose password
                    return ResponseEntity.ok(user);
                })
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }
}