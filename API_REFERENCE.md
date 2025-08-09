# Login API Reference

## Base URL
```
https://api.yourdomain.com/api/auth/v1
```

## Authentication
All protected endpoints require a Bearer token in the Authorization header:
```
Authorization: Bearer <access_token>
```

## Rate Limiting
All endpoints are rate limited. Rate limit information is returned in response headers:
- `X-RateLimit-Limit`: Maximum requests allowed
- `X-RateLimit-Remaining`: Remaining requests in current window
- `Retry-After`: Seconds to wait when rate limited

## Endpoints

### POST /register
Register a new user account.

**Request Body:**
```json
{
    "username": "string (3-50 chars, alphanumeric + underscore)",
    "email": "string (valid email, max 100 chars)",
    "password": "string (8-100 chars, must contain uppercase, lowercase, digit, special char)"
}
```

**Response (201):**
```json
{
    "message": "User registered successfully"
}
```

**Errors:**
- `400`: Validation failed
- `409`: Username or email already exists
- `429`: Rate limit exceeded (3/minute)

---

### POST /login
Authenticate user and receive JWT tokens.

**Request Body:**
```json
{
    "username": "string (3-50 chars)",
    "password": "string (6-100 chars)"
}
```

**Response (200):**
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

**Response Headers:**
- `X-Correlation-ID`: Request correlation ID for tracing

**Errors:**
- `400`: Validation failed
- `401`: Invalid credentials
- `429`: Rate limit exceeded (5/minute)

---

### POST /refresh
Refresh access token using refresh token.

**Request Body:**
```json
{
    "refreshToken": "string"
}
```

**Response (200):**
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

**Errors:**
- `401`: Invalid or expired refresh token

---

### POST /logout
Invalidate current access token.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Response (200):**
```json
{
    "message": "Logged out successfully"
}
```

**Errors:**
- `401`: Invalid or missing token

---

### POST /forgot-password
Request password reset email.

**Request Body:**
```json
{
    "email": "string"
}
```

**Response (200):**
```json
{
    "message": "Password reset email sent"
}
```

**Note:** Always returns success for security (no user enumeration).

---

### POST /reset-password
Reset password using reset token.

**Request Body:**
```json
{
    "token": "string",
    "newPassword": "string (8-100 chars, complexity requirements)"
}
```

**Response (200):**
```json
{
    "message": "Password reset successfully"
}
```

**Errors:**
- `400`: Validation failed
- `401`: Invalid or expired reset token

---

### GET /check-username/{username}
Check username availability.

**Parameters:**
- `username`: string (3-50 chars)

**Response (200):**
```json
{
    "available": true,
    "message": "Username is available"
}
```

---

### GET /check-email/{email}
Check email availability.

**Parameters:**
- `email`: string (valid email format)

**Response (200):**
```json
{
    "available": false,
    "message": "Email is already registered"
}
```

---

### POST /verify-email/{token}
Verify email address using verification token.

**Parameters:**
- `token`: string (verification token)

**Response (200):**
```json
{
    "message": "Email verified successfully"
}
```

**Errors:**
- `401`: Invalid or expired verification token

---

### POST /resend-verification
Resend email verification.

**Query Parameters:**
- `email`: string (email address)

**Response (200):**
```json
{
    "message": "Verification email sent"
}
```

---

### GET /health
Service health check.

**Response (200):**
```
Authentication service is healthy
```

## Error Response Format

All error responses follow this format:
```json
{
    "message": "Error description",
    "status": 400,
    "timestamp": "2024-01-15T10:30:00Z",
    "errors": ["Detailed error messages"]
}
```

## HTTP Status Codes

| Code | Description |
|------|-------------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request |
| 401 | Unauthorized |
| 409 | Conflict |
| 429 | Too Many Requests |
| 500 | Internal Server Error |

## SDK Examples

### JavaScript/Node.js
```javascript
const API_BASE = 'https://api.yourdomain.com/api/auth/v1';

// Register user
async function register(username, email, password) {
    const response = await fetch(`${API_BASE}/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, email, password })
    });
    return response.json();
}

// Login user
async function login(username, password) {
    const response = await fetch(`${API_BASE}/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
    });
    return response.json();
}
```

### Python
```python
import requests

API_BASE = 'https://api.yourdomain.com/api/auth/v1'

def register(username, email, password):
    response = requests.post(f'{API_BASE}/register', json={
        'username': username,
        'email': email,
        'password': password
    })
    return response.json()

def login(username, password):
    response = requests.post(f'{API_BASE}/login', json={
        'username': username,
        'password': password
    })
    return response.json()
```

### cURL
```bash
# Register
curl -X POST https://api.yourdomain.com/api/auth/v1/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"Password123!"}'

# Login
curl -X POST https://api.yourdomain.com/api/auth/v1/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"Password123!"}'

# Logout
curl -X POST https://api.yourdomain.com/api/auth/v1/logout \
  -H "Authorization: Bearer <access_token>"
```