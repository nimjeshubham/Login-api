package com.possessor.loginapi.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class RequestSizeLimitFilter implements WebFilter, Ordered {
    
    private static final long MAX_REQUEST_SIZE = 1024 * 1024; // 1MB
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String contentLength = exchange.getRequest().getHeaders().getFirst("Content-Length");
        
        if (contentLength != null) {
            try {
                long size = Long.parseLong(contentLength);
                if (size > MAX_REQUEST_SIZE) {
                    log.warn("Request size {} exceeds limit {}", size, MAX_REQUEST_SIZE);
                    exchange.getResponse().setStatusCode(HttpStatus.PAYLOAD_TOO_LARGE);
                    return exchange.getResponse().setComplete();
                }
            } catch (NumberFormatException e) {
                log.warn("Invalid Content-Length header: {}", contentLength);
            }
        }
        
        return chain.filter(exchange);
    }
    
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}