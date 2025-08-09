package com.possessor.loginapi.config;

import com.possessor.loginapi.constants.ApiEndpoints;
import com.possessor.loginapi.filter.RateLimitFilter;
import com.possessor.loginapi.filter.RequestSizeLimitFilter;
import com.possessor.loginapi.filter.TokenBlacklistFilter;
import com.possessor.loginapi.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;



@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitFilter rateLimitFilter;
    private final RequestSizeLimitFilter requestSizeLimitFilter;
    private final TokenBlacklistFilter tokenBlacklistFilter;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
    
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(ApiEndpoints.AUTH_BASE + "/v1/**", ApiEndpoints.FULL_ACTUATOR_HEALTH, ApiEndpoints.FULL_ACTUATOR_INFO).permitAll()
                        .pathMatchers(ApiEndpoints.ACTUATOR_BASE + "/**").hasRole("ADMIN")
                        .anyExchange().authenticated()
                )
                .addFilterBefore(requestSizeLimitFilter, SecurityWebFiltersOrder.AUTHORIZATION)
                .addFilterBefore(rateLimitFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .addFilterBefore(tokenBlacklistFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .addFilterBefore(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .headers(ServerHttpSecurity.HeaderSpec::disable)
                .build();
    }
}