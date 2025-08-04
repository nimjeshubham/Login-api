package com.possessor.loginapi;

import com.possessor.loginapi.dto.LoginRequest;
import com.possessor.loginapi.dto.RegisterRequest;
import com.possessor.loginapi.entity.User;
import com.possessor.loginapi.exception.AuthenticationException;
import com.possessor.loginapi.exception.UserAlreadyExistsException;
import com.possessor.loginapi.repository.UserRepository;
import com.possessor.loginapi.security.JwtUtil;
import com.possessor.loginapi.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtUtil jwtUtil;
    
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtUtil);
    }

    @Test
    void register_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("Password123!");

        User savedUser = new User();
        savedUser.setUsername("testuser");
        savedUser.setEmail("test@example.com");

        when(userRepository.existsByUsername(anyString())).thenReturn(Mono.just(false));
        when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(false));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(savedUser));
        when(jwtUtil.generateToken(anyString())).thenReturn("jwt-token");

        StepVerifier.create(authService.register(request))
                .expectNextMatches(response -> 
                    response.getToken().equals("jwt-token") && 
                    response.getUsername().equals("testuser"))
                .verifyComplete();
    }

    @Test
    void register_UserAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");

        when(userRepository.existsByUsername(anyString())).thenReturn(Mono.just(true));

        StepVerifier.create(authService.register(request))
                .expectError(UserAlreadyExistsException.class)
                .verify();
    }

    @Test
    void login_Success() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password");

        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");

        when(userRepository.findByUsername(anyString())).thenReturn(Mono.just(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString())).thenReturn("jwt-token");

        StepVerifier.create(authService.login(request))
                .expectNextMatches(response -> response.getToken().equals("jwt-token"))
                .verifyComplete();
    }

    @Test
    void login_InvalidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        when(userRepository.findByUsername(anyString())).thenReturn(Mono.empty());

        StepVerifier.create(authService.login(request))
                .expectError(AuthenticationException.class)
                .verify();
    }
}