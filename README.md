# Login API - Production Grade

A production-ready reactive Spring Boot application providing secure authentication services with JWT tokens.

## Features

- **Security**: JWT authentication, BCrypt password hashing, input validation
- **Performance**: Reactive programming, connection pooling, caching
- **Monitoring**: Actuator endpoints, Prometheus metrics, health checks
- **Resilience**: Rate limiting, retry logic, proper error handling
- **Testing**: Comprehensive unit tests with Testcontainers
- **Deployment**: Docker support, environment-based configuration

## Prerequisites

- Java 21
- PostgreSQL 15+
- Docker & Docker Compose (optional)
- Gradle 8+

## Quick Start with Docker

```bash
docker-compose up -d
```

## Manual Setup

1. **Database Setup**:
```sql
CREATE DATABASE logindb;
```

2. **Environment Variables**:
```bash
export DB_URL=r2dbc:postgresql://localhost:5432/logindb
export DB_USERNAME=postgres
export DB_PASSWORD=your_password
export JWT_SECRET=your_jwt_secret_key_minimum_32_characters
export JWT_EXPIRATION=86400000
```

3. **Run Application**:
```bash
./gradlew bootRun
```

## API Endpoints

### Authentication

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "Password123!"
}
```

**Password Requirements**: 8+ chars, uppercase, lowercase, digit, special character

#### Login User
```http
POST /api/auth/login
Content-Type: application/json

{
    "username": "john_doe",
    "password": "Password123!"
}
```

#### Health Check
```http
GET /api/auth/health
```

### Protected Endpoints

#### User Profile
```http
GET /api/user/profile
Authorization: Bearer <jwt_token>
```

### Monitoring

- **Health**: `/actuator/health`
- **Metrics**: `/actuator/metrics`
- **Prometheus**: `/actuator/prometheus`

## Production Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|----------|
| `DB_URL` | Database connection URL | - |
| `DB_USERNAME` | Database username | - |
| `DB_PASSWORD` | Database password | - |
| `JWT_SECRET` | JWT signing secret (32+ chars) | - |
| `JWT_EXPIRATION` | Token expiration (ms) | 86400000 |
| `PORT` | Server port | 8080 |

### Security Features

- **Rate Limiting**: 100 requests/minute per IP
- **Password Policy**: Strong password requirements
- **JWT Security**: HS256 algorithm, configurable expiration
- **Input Validation**: Comprehensive request validation
- **Error Handling**: Secure error responses (no sensitive data)

### Performance Features

- **Connection Pooling**: R2DBC connection pool (20-50 connections)
- **Caching**: User data caching with Caffeine
- **Reactive**: Non-blocking I/O with WebFlux
- **Metrics**: Performance monitoring with Micrometer

## Testing

```bash
# Run all tests
./gradlew test

# Run with coverage
./gradlew test jacocoTestReport
```

## Deployment

### Docker Build
```bash
./gradlew build
docker build -t login-api .
```

### Kubernetes
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
    metadata:
      labels:
        app: login-api
    spec:
      containers:
      - name: login-api
        image: login-api:latest
        ports:
        - containerPort: 8080
        env:
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
          periodSeconds: 10
```

## Database Schema

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
```

## Monitoring & Observability

- **Logs**: Structured JSON logging
- **Metrics**: Custom business metrics + JVM metrics
- **Health Checks**: Database connectivity, disk space
- **Tracing**: Request correlation IDs

## Security Considerations

- Secrets managed via environment variables
- No sensitive data in logs or error responses
- Rate limiting to prevent abuse
- Strong password policies
- JWT tokens with reasonable expiration
- HTTPS recommended for production