package com.possessor.loginapi.service;

import com.possessor.loginapi.dto.ChangePasswordRequest;
import com.possessor.loginapi.dto.MessageResponse;
import com.possessor.loginapi.dto.UpdateProfileRequest;
import com.possessor.loginapi.exception.AuthenticationException;
import com.possessor.loginapi.exception.UserAlreadyExistsException;
import com.possessor.loginapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @CacheEvict(value = "users", key = "#username")
    public Mono<MessageResponse> updateProfile(String username, UpdateProfileRequest request) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new AuthenticationException("User not found")))
                .flatMap(user -> {
                    Mono<Void> validationMono = Mono.empty();
                    
                    if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
                        validationMono = userRepository.existsByUsername(request.getUsername())
                                .flatMap(exists -> exists ? 
                                    Mono.error(new UserAlreadyExistsException("Username already exists")) : 
                                    Mono.empty());
                        user.setUsername(request.getUsername().toLowerCase());
                    }
                    
                    if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
                        validationMono = validationMono.then(
                            userRepository.existsByEmail(request.getEmail())
                                .flatMap(exists -> exists ? 
                                    Mono.error(new UserAlreadyExistsException("Email already exists")) : 
                                    Mono.empty())
                        );
                        user.setEmail(request.getEmail().toLowerCase());
                        user.setEmailVerified(false);
                    }
                    
                    user.setUpdatedAt(LocalDateTime.now());
                    
                    return validationMono.then(userRepository.save(user));
                })
                .map(user -> new MessageResponse("Profile updated successfully"))
                .doOnSuccess(response -> log.info("Profile updated for user: {}", username))
                .doOnError(error -> log.error("Profile update failed for user: {}", username, error));
    }
    
    @CacheEvict(value = "users", key = "#username")
    public Mono<MessageResponse> changePassword(String username, ChangePasswordRequest request) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new AuthenticationException("User not found")))
                .flatMap(user -> {
                    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                        return Mono.error(new AuthenticationException("Current password is incorrect"));
                    }
                    
                    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                    user.setUpdatedAt(LocalDateTime.now());
                    
                    return userRepository.save(user);
                })
                .map(user -> new MessageResponse("Password changed successfully"))
                .doOnSuccess(response -> log.info("Password changed for user: {}", username))
                .doOnError(error -> log.error("Password change failed for user: {}", username, error));
    }
    
    @CacheEvict(value = "users", key = "#username")
    public Mono<MessageResponse> deleteAccount(String username) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new AuthenticationException("User not found")))
                .flatMap(user -> userRepository.delete(user))
                .then(Mono.just(new MessageResponse("Account deleted successfully")))
                .doOnSuccess(response -> log.info("Account deleted for user: {}", username))
                .doOnError(error -> log.error("Account deletion failed for user: {}", username, error));
    }
}