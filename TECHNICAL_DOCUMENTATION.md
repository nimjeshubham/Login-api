# Login API - Technical Documentation

## Architecture Overview

### Technology Stack
- **Framework**: Spring Boot 3.5.4 with WebFlux (Reactive)
- **Language**: Java 21
- **Database**: PostgreSQL 15+ with R2DBC
- **Cache**: Redis 7 for distributed caching
- **Security**: JWT with BCrypt password hashing
- **Monitoring**: Micrometer + Prometheus
- **Build**: Gradle 8+

### System Architecture
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Load Balancer │────│   Login API     │────│   PostgreSQL    │
│                 │    │   (Multiple     │    │   Database      │
│                 │    │   Instances)    │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                       ┌─────────────────┐
                       │     Redis       │
                       │  (Distributed   │
                       │   Caching)      │
                       └─────────────────┘
```

## API Specification

### Base URL
```
Production: https://api.yourdomain.com/api/auth/v1
Development: http://localhost:8080/api/auth/v1
```

### Authentication Endpoints

#### 1. User Registration
```http
POST /register
Content-Type: application/json

{
    "username": "john_doe",
    "email": "john@example.com", 
    "password": "Password123!"
}
```

**Response (201 Created):**
```json
{
    "message": "User registered successfully"
}
```

**Rate Limit**: 3 requests/minute per IP

#### 2. User Login
```http
POST /login
Content-Type: application/json

{
    "username": "john_doe",
    "password": "Password123!"
}
```

**Response (200 OK):**
```json
{
    "username": "john_doe",
    "email": "john@example.com",
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "tokenType": "Bearer",
    "expiresIn": 86400000
}
```

**Rate Limit**: 5 requests/minute per IP

#### 3. Token Refresh
```http
POST /refresh
Content-Type: application/json

{
    "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

#### 4. Logout
```http
POST /logout
Authorization: Bearer <access_token>
```

**Response (200 OK):**
```json
{
    "message": "Logged out successfully"
}
```

### Utility Endpoints

#### Username Availability
```http
GET /check-username/{username}
```

#### Email Availability  
```http
GET /check-email/{email}
```

#### Health Check
```http
GET /health
```

## Security Implementation

### Authentication Flow
1. **Registration**: BCrypt password hashing (strength 12)
2. **Login**: JWT token generation with refresh token
3. **Authorization**: Bearer token validation
4. **Logout**: Token blacklisting in Redis

### Security Features
- **Rate Limiting**: Distributed Redis-based rate limiting
- **Input Validation**: Bean Validation with sanitization
- **CORS Protection**: Configurable origin restrictions
- **Security Headers**: HSTS, frame options, content type protection
- **Request Size Limits**: 1MB maximum payload
- **Token Blacklisting**: Immediate token invalidation

### Password Requirements
- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter  
- At least one digit
- At least one special character (@$!%*?&)

## Distributed Systems

### Redis Integration
```yaml
# Rate Limiting Keys
rate_limit:login:{ip} -> request_count (TTL: 1 minute)
rate_limit:register:{ip} -> request_count (TTL: 1 minute)

# Session Management
blacklist:{token} -> "true" (TTL: token_expiration)
session:{userId} -> session_data (TTL: configurable)
```

### Connection Pooling
- **Database**: R2DBC pool (20-50 connections)
- **Redis**: Lettuce pool (5-20 connections)
- **Connection Validation**: Health checks every 10s

## Monitoring & Observability

### Metrics
```yaml
# Custom Metrics
auth.login.attempts -> Counter
auth.registration.attempts -> Counter
auth.login -> Timer
auth.register -> Timer

# System Metrics
jvm.memory.used
jvm.gc.pause
http.server.requests
```

### Health Checks
- **Database**: PostgreSQL connectivity
- **Cache**: Redis connectivity  
- **Application**: Service health status

### Logging
```yaml
# Log Format
%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level [%X{correlationId}] %logger{36} - %msg%n

# Log Levels (Production)
com.possessor.loginapi: WARN
org.springframework.security: ERROR
```

### Correlation IDs
Every request generates a UUID correlation ID for distributed tracing.

## Performance Characteristics

### Throughput
- **Login**: ~1000 requests/second
- **Registration**: ~500 requests/second
- **Token Validation**: ~2000 requests/second

### Response Times (P95)
- **Login**: <100ms
- **Registration**: <150ms
- **Token Refresh**: <50ms

### Rate Limits
| Endpoint | Limit | Window |
|----------|-------|--------|
| Login | 5 requests | 1 minute |
| Register | 3 requests | 1 minute |
| General | 100 requests | 1 minute |

## Error Handling

### HTTP Status Codes
- **200**: Success
- **201**: Created (registration)
- **400**: Bad Request (validation errors)
- **401**: Unauthorized (invalid credentials)
- **409**: Conflict (user exists)
- **429**: Too Many Requests (rate limited)
- **500**: Internal Server Error

### Error Response Format
```json
{
    "message": "Validation failed",
    "status": 400,
    "timestamp": "2024-01-15T10:30:00",
    "errors": ["Username is required", "Password too short"]
}
```

## Deployment

### Environment Variables
```bash
# Database
DB_URL=r2dbc:postgresql://localhost:5432/logindb
DB_USERNAME=postgres
DB_PASSWORD=your_password

# JWT
JWT_SECRET=your_jwt_secret_key_minimum_32_characters
JWT_EXPIRATION=86400000

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password

# CORS
CORS_ORIGINS=https://yourdomain.com
```

### Docker Deployment
```bash
# Start all services
docker-compose up -d

# Scale API instances
docker-compose up -d --scale login-api=3
```

### Kubernetes Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: login-api
spec:
  replicas: 3
  selector:
    matchLabels:
      app: login-api
  template:
    spec:
      containers:
      - name: login-api
        image: login-api:latest
        ports:
        - containerPort: 8080
        env:
        - name: REDIS_HOST
          value: "redis-service"
        - name: DB_URL
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: url
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 10
```

## Security Considerations

### Production Checklist
- [x] HTTPS enforcement
- [x] Rate limiting enabled
- [x] Input validation
- [x] SQL injection prevention
- [x] XSS protection
- [x] CSRF protection
- [x] Secure headers
- [x] Token blacklisting
- [x] Password hashing
- [x] Error message sanitization

### Security Headers
```yaml
Strict-Transport-Security: max-age=31536000; includeSubDomains
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
```

## Troubleshooting

### Common Issues

#### High Memory Usage
```bash
# Check JVM metrics
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Adjust heap size
export JAVA_OPTS="-Xmx2g -Xms1g"
```

#### Redis Connection Issues
```bash
# Check Redis connectivity
curl http://localhost:8080/actuator/health

# Verify Redis configuration
redis-cli -h localhost -p 6379 ping
```

#### Database Connection Pool Exhaustion
```yaml
# Increase pool size
spring.r2dbc.pool.max-size=100
spring.r2dbc.pool.initial-size=20
```

### Monitoring Queries
```bash
# Check rate limit status
redis-cli KEYS "rate_limit:*"

# Monitor active sessions
redis-cli KEYS "session:*"

# Check blacklisted tokens
redis-cli KEYS "blacklist:*"
```

## API Testing

### cURL Examples
```bash
# Register user
curl -X POST http://localhost:8080/api/auth/v1/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"Password123!"}'

# Login user
curl -X POST http://localhost:8080/api/auth/v1/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"Password123!"}'

# Check health
curl http://localhost:8080/api/auth/v1/health
```

### Load Testing
```bash
# Using Apache Bench
ab -n 1000 -c 10 -H "Content-Type: application/json" \
   -p login.json http://localhost:8080/api/auth/v1/login
```

## Maintenance

### Database Migrations
```sql
-- Add index for performance
CREATE INDEX CONCURRENTLY idx_users_email_verified ON users(email) WHERE email_verified = true;

-- Clean up expired tokens (run periodically)
DELETE FROM password_reset_tokens WHERE expires_at < NOW();
```

### Redis Maintenance
```bash
# Monitor memory usage
redis-cli INFO memory

# Clean expired keys
redis-cli --scan --pattern "rate_limit:*" | xargs redis-cli DEL
```

### Log Rotation
```yaml
# logback-spring.xml
<rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
    <fileNamePattern>logs/login-api.%d{yyyy-MM-dd}.log</fileNamePattern>
    <maxHistory>30</maxHistory>
    <totalSizeCap>1GB</totalSizeCap>
</rollingPolicy>
```

## Performance Tuning

### JVM Tuning
```bash
export JAVA_OPTS="-Xmx2g -Xms2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

### Database Tuning
```sql
-- PostgreSQL configuration
shared_buffers = 256MB
effective_cache_size = 1GB
work_mem = 4MB
maintenance_work_mem = 64MB
```

### Redis Tuning
```bash
# redis.conf
maxmemory 512mb
maxmemory-policy allkeys-lru
tcp-keepalive 300
```