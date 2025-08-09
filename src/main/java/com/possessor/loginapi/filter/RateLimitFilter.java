package com.possessor.loginapi.filter;

import com.possessor.loginapi.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter implements WebFilter, Ordered {
    
    private final RateLimitService rateLimitService;
    
    @Value("${app.rate-limit.login-requests:5}")
    private int loginMaxRequests;
    
    @Value("${app.rate-limit.register-requests:3}")
    private int registerMaxRequests;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String clientIp = getClientIp(exchange);
        
        if (path.contains("/auth/v1/login")) {
            return checkRateLimit(exchange, chain, "login:" + clientIp, loginMaxRequests);
        } else if (path.contains("/auth/v1/register")) {
            return checkRateLimit(exchange, chain, "register:" + clientIp, registerMaxRequests);
        }
        
        return chain.filter(exchange);
    }
    
    private Mono<Void> checkRateLimit(ServerWebExchange exchange, WebFilterChain chain, 
                                     String key, int maxRequests) {
        return rateLimitService.isAllowed(key, maxRequests, Duration.ofMinutes(1))
            .flatMap(allowed -> {
                if (!allowed) {
                    log.warn("Rate limit exceeded for key: {}", key);
                    exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                    exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(maxRequests));
                    exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", "0");
                    exchange.getResponse().getHeaders().add("Retry-After", "60");
                    return exchange.getResponse().setComplete();
                }
                
                return rateLimitService.getRemainingRequests(key, maxRequests)
                    .doOnNext(remaining -> {
                        exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(maxRequests));
                        exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", String.valueOf(remaining));
                    })
                    .then(chain.filter(exchange));
            });
    }
    
    private String getClientIp(ServerWebExchange exchange) {
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return exchange.getRequest().getRemoteAddress() != null ? 
            exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }
    
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 2;
    }
}