# Login API - Concepts and Design Patterns

## Table of Contents
1. [Architectural Patterns](#architectural-patterns)
2. [Design Patterns](#design-patterns)
3. [Security Concepts](#security-concepts)
4. [Reactive Programming](#reactive-programming)
5. [Distributed Systems](#distributed-systems)
6. [Data Management](#data-management)
7. [Monitoring & Observability](#monitoring--observability)
8. [Performance Optimization](#performance-optimization)

## Architectural Patterns

### 1. Layered Architecture
```
┌─────────────────────────────────────┐
│           Controller Layer          │ ← REST API endpoints
├─────────────────────────────────────┤
│            Service Layer            │ ← Business logic
├─────────────────────────────────────┤
│          Repository Layer           │ ← Data access
├─────────────────────────────────────┤
│           Database Layer            │ ← PostgreSQL
└─────────────────────────────────────┘
```

**Implementation:**
- `AuthController` - REST endpoints
- `AuthService` - Business logic
- `UserRepository` - Data access
- `PostgreSQL` - Data persistence

### 2. Microservices Architecture
- **Single Responsibility**: Authentication service only
- **API Gateway Ready**: Versioned endpoints (`/v1`)
- **Service Discovery**: Health check endpoints
- **Independent Deployment**: Docker containerization

### 3. Hexagonal Architecture (Ports & Adapters)
```
┌─────────────────────────────────────┐
│              Core Domain            │
│         (AuthService)               │
├─────────────────────────────────────┤
│  Ports (Interfaces)                 │
│  - UserRepository                   │
│  - EmailService                     │
│  - TokenClient                      │
├─────────────────────────────────────┤
│  Adapters (Implementations)         │
│  - R2DBC Repository                 │
│  - SMTP Email Service               │
│  - JWT Token Service                │
└─────────────────────────────────────┘
```

## Design Patterns

### 1. Dependency Injection (IoC)
```java
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
}
```

**Benefits:**
- Loose coupling
- Testability
- Configuration externalization

### 2. Repository Pattern
```java
public interface UserRepository extends ReactiveCrudRepository<User, Long> {
    Mono<User> findByUsername(String username);
    Mono<Boolean> existsByEmail(String email);
}
```

**Benefits:**
- Data access abstraction
- Testability with mocks
- Database technology independence

### 3. DTO Pattern (Data Transfer Object)
```java
@Data
public class LoginRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}
```

**Benefits:**
- API contract definition
- Input validation
- Data encapsulation

### 4. Builder Pattern
```java
Counter.builder("auth.login.attempts")
    .description("Total login attempts")
    .register(meterRegistry);
```

**Benefits:**
- Fluent API
- Optional parameters
- Immutable objects

### 5. Strategy Pattern
```java
// Different rate limiting strategies
public interface RateLimitStrategy {
    Mono<Boolean> isAllowed(String key, int maxRequests, Duration window);
}

// Redis-based implementation
@Service
public class RedisRateLimitStrategy implements RateLimitStrategy {
    // Implementation
}
```

### 6. Chain of Responsibility Pattern
```java
// Security filter chain
.addFilterBefore(requestSizeLimitFilter, SecurityWebFiltersOrder.AUTHORIZATION)
.addFilterBefore(rateLimitFilter, SecurityWebFiltersOrder.AUTHENTICATION)
.addFilterBefore(tokenBlacklistFilter, SecurityWebFiltersOrder.AUTHENTICATION)
.addFilterBefore(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
```

### 7. Factory Pattern
```java
@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
```

## Security Concepts

### 1. Authentication vs Authorization
- **Authentication**: "Who are you?" (Login process)
- **Authorization**: "What can you do?" (Permission checking)

### 2. JWT (JSON Web Token)
```
Header.Payload.Signature
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

**Components:**
- **Header**: Algorithm and token type
- **Payload**: Claims (user data)
- **Signature**: Verification hash

### 3. BCrypt Password Hashing
```java
// Strength 12 = 2^12 iterations
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
String hashedPassword = encoder.encode("plaintext");
```

**Benefits:**
- Salt generation
- Adaptive hashing
- Brute force resistance

### 4. CORS (Cross-Origin Resource Sharing)
```java
corsConfig.setAllowedOriginPatterns(Arrays.asList(allowedOrigins));
corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
corsConfig.setAllowCredentials(true);
```

### 5. Security Headers
- **HSTS**: Force HTTPS connections
- **X-Frame-Options**: Prevent clickjacking
- **X-Content-Type-Options**: Prevent MIME sniffing

### 6. Rate Limiting
```java
// Sliding window rate limiting
public Mono<Boolean> isAllowed(String key, int maxRequests, Duration window) {
    // Redis-based distributed rate limiting
}
```

**Types:**
- **Fixed Window**: Reset at fixed intervals
- **Sliding Window**: Continuous time window
- **Token Bucket**: Burst capacity with refill rate

## Reactive Programming

### 1. Reactive Streams
```java
public Mono<AuthResponse> login(LoginRequest request) {
    return userRepository.findByUsername(request.getUsername())
        .switchIfEmpty(Mono.error(new AuthenticationException()))
        .flatMap(user -> validateAndGenerateToken(user, request));
}
```

**Key Types:**
- **Mono**: 0 or 1 element
- **Flux**: 0 to N elements

### 2. Non-Blocking I/O
- **WebFlux**: Reactive web framework
- **R2DBC**: Reactive database connectivity
- **Netty**: Non-blocking server

### 3. Backpressure Handling
```java
.retryWhen(Retry.backoff(3, Duration.ofMillis(500)))
.timeout(Duration.ofSeconds(30))
```

### 4. Context Propagation
```java
.contextWrite(Context.of("correlationId", correlationId))
```

## Distributed Systems

### 1. CAP Theorem
- **Consistency**: All nodes see same data
- **Availability**: System remains operational
- **Partition Tolerance**: System continues despite network failures

**Our Choice**: AP (Availability + Partition Tolerance)

### 2. Distributed Caching
```java
// Redis for distributed rate limiting
String redisKey = "rate_limit:" + clientIp;
return redisTemplate.opsForValue().increment(redisKey);
```

### 3. Session Management
```java
// Distributed token blacklisting
public Mono<Void> blacklistToken(String token, Duration expiration) {
    return redisTemplate.opsForValue().set("blacklist:" + token, "true", expiration);
}
```

### 4. Circuit Breaker Pattern
```java
@Bean
public CircuitBreaker authCircuitBreaker() {
    return CircuitBreaker.of("auth-service", CircuitBreakerConfig.custom()
        .failureRateThreshold(50)
        .waitDurationInOpenState(Duration.ofSeconds(30))
        .build());
}
```

**States:**
- **Closed**: Normal operation
- **Open**: Failing fast
- **Half-Open**: Testing recovery

### 5. Retry Pattern
```java
.retryWhen(Retry.backoff(3, Duration.ofMillis(500))
    .filter(throwable -> throwable instanceof TransientException))
```

## Data Management

### 1. ACID Properties
- **Atomicity**: All or nothing transactions
- **Consistency**: Data integrity maintained
- **Isolation**: Concurrent transaction isolation
- **Durability**: Committed data persists

### 2. Connection Pooling
```yaml
spring.r2dbc.pool.initial-size=20
spring.r2dbc.pool.max-size=50
spring.r2dbc.pool.max-idle-time=10m
```

### 3. Database Indexing
```sql
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
```

### 4. Data Validation
```java
@NotBlank(message = "Username is required")
@Size(min = 3, max = 50)
@Pattern(regexp = "^[a-zA-Z0-9_]+$")
private String username;
```

**Validation Levels:**
- **Client-side**: User experience
- **Server-side**: Security and integrity
- **Database**: Constraints and triggers

## Monitoring & Observability

### 1. Three Pillars of Observability
- **Metrics**: Quantitative measurements
- **Logs**: Event records
- **Traces**: Request flow tracking

### 2. Metrics Types
```java
// Counter - monotonically increasing
Counter loginAttempts = Counter.builder("auth.login.attempts").register(registry);

// Timer - duration and frequency
@Timed(value = "auth.login", description = "Login duration")

// Gauge - current value
Gauge.builder("active.sessions").register(registry, this::getActiveSessions);
```

### 3. Structured Logging
```yaml
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level [%X{correlationId}] %logger{36} - %msg%n
```

### 4. Health Checks
```java
@Component
public class DatabaseHealthIndicator implements ReactiveHealthIndicator {
    public Mono<Health> health() {
        return userRepository.count()
            .map(count -> Health.up().withDetail("users", count).build())
            .onErrorReturn(Health.down().build());
    }
}
```

### 5. Correlation IDs
```java
String correlationId = UUID.randomUUID().toString();
MDC.put("correlationId", correlationId);
```

## Performance Optimization

### 1. Caching Strategies
```java
@Cacheable("users")
public Mono<User> findByUsername(String username) {
    return userRepository.findByUsername(username);
}
```

**Cache Patterns:**
- **Cache-Aside**: Application manages cache
- **Write-Through**: Write to cache and database
- **Write-Behind**: Asynchronous database writes

### 2. Database Optimization
```sql
-- Query optimization
EXPLAIN ANALYZE SELECT * FROM users WHERE username = 'john_doe';

-- Index usage
CREATE INDEX CONCURRENTLY idx_users_email_verified ON users(email) WHERE email_verified = true;
```

### 3. Connection Pool Tuning
```yaml
# R2DBC Pool
spring.r2dbc.pool.initial-size=20
spring.r2dbc.pool.max-size=50
spring.r2dbc.pool.max-acquire-time=60s

# Redis Pool
spring.redis.lettuce.pool.max-active=20
spring.redis.lettuce.pool.max-idle=10
```

### 4. JVM Tuning
```bash
# G1 Garbage Collector
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:G1HeapRegionSize=16m

# Memory settings
-Xmx2g -Xms2g
-XX:MetaspaceSize=256m
```

### 5. Reactive Optimization
```java
// Parallel processing
.flatMap(user -> processUser(user), 10) // Concurrency of 10

// Batching
.buffer(100) // Process in batches of 100

// Prefetching
.publishOn(Schedulers.parallel(), 32) // Prefetch 32 items
```

## Enterprise Patterns

### 1. API Versioning
```java
@RequestMapping(ApiEndpoints.AUTH_BASE + "/v1")
```

**Strategies:**
- **URL Versioning**: `/v1/users`
- **Header Versioning**: `Accept: application/vnd.api.v1+json`
- **Parameter Versioning**: `?version=1`

### 2. Error Handling
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ValidationException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidation(ValidationException ex) {
        // Centralized error handling
    }
}
```

### 3. Configuration Management
```yaml
# Environment-specific configuration
spring.profiles.active=${SPRING_PROFILES_ACTIVE:dev}

# External configuration
app.jwt.secret=${JWT_SECRET:default-secret}
```

### 4. Service Discovery
```yaml
# Health check endpoint
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized
```

### 5. Graceful Shutdown
```yaml
# Graceful shutdown configuration
server.shutdown=graceful
spring.lifecycle.timeout-per-shutdown-phase=30s
```

## Testing Patterns

### 1. Test Pyramid
```
    ┌─────────────┐
    │   E2E Tests │ ← Few, expensive
    ├─────────────┤
    │ Integration │ ← Some, moderate cost
    │    Tests    │
    ├─────────────┤
    │ Unit Tests  │ ← Many, fast, cheap
    └─────────────┘
```

### 2. Test Containers
```java
@Testcontainers
class AuthServiceTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
}
```

### 3. Reactive Testing
```java
@Test
void shouldLoginSuccessfully() {
    StepVerifier.create(authService.login(loginRequest))
        .expectNextMatches(response -> response.getAccessToken() != null)
        .verifyComplete();
}
```

This comprehensive guide covers all the key concepts, patterns, and architectural decisions implemented in the Login API, providing a complete reference for understanding the system's design and implementation choices.