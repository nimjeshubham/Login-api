# Login API - Postman Collection Guide

## Overview
This guide provides comprehensive instructions for using the Login API Postman collection to test all authentication endpoints.

## Collection Import

### 1. Import Collection
1. Open Postman
2. Click **Import** button
3. Select **File** tab
4. Choose `Login-API-Postman-Collection.json`
5. Click **Import**

### 2. Environment Setup
Create a new environment with these variables:

| Variable | Value | Description |
|----------|-------|-------------|
| `baseUrl` | `http://localhost:8080` | API base URL |
| `username` | `testuser123` | Test username |
| `email` | `test@example.com` | Test email |
| `password` | `Password123!` | Test password |
| `newPassword` | `NewPassword123!` | New password for reset |
| `checkUsername` | `newuser456` | Username to check availability |
| `checkEmail` | `check@example.com` | Email to check availability |
| `resetToken` | `sample-reset-token-uuid` | Password reset token |
| `verificationToken` | `sample-verification-token-uuid` | Email verification token |
| `accessToken` | `` | Auto-populated after login |
| `refreshToken` | `` | Auto-populated after login |

## Collection Structure

### 📁 Authentication
Core authentication endpoints for user management.

#### 1. Register User
- **Method**: `POST`
- **Endpoint**: `/api/auth/v1/register`
- **Purpose**: Create new user account
- **Rate Limit**: 3 requests/minute

**Request Body:**
```json
{
    "username": "{{username}}",
    "email": "{{email}}",
    "password": "{{password}}"
}
```

**Tests:**
- ✅ Status code is 201
- ✅ Response contains success message

#### 2. Login User
- **Method**: `POST`
- **Endpoint**: `/api/auth/v1/login`
- **Purpose**: Authenticate user and get JWT tokens
- **Rate Limit**: 5 requests/minute

**Request Body:**
```json
{
    "username": "{{username}}",
    "password": "{{password}}"
}
```

**Auto-Actions:**
- Saves `accessToken` to environment
- Saves `refreshToken` to environment

**Tests:**
- ✅ Status code is 200
- ✅ Response contains access token
- ✅ X-Correlation-ID header present

#### 3. Refresh Token
- **Method**: `POST`
- **Endpoint**: `/api/auth/v1/refresh`
- **Purpose**: Get new access token using refresh token

**Request Body:**
```json
{
    "refreshToken": "{{refreshToken}}"
}
```

**Auto-Actions:**
- Updates `accessToken` in environment

#### 4. Logout User
- **Method**: `POST`
- **Endpoint**: `/api/auth/v1/logout`
- **Purpose**: Invalidate current access token
- **Auth**: Bearer Token Required

**Headers:**
```
Authorization: Bearer {{accessToken}}
```

### 📁 Password Management
Password reset and recovery endpoints.

#### 1. Forgot Password
- **Method**: `POST`
- **Endpoint**: `/api/auth/v1/forgot-password`
- **Purpose**: Request password reset email

**Request Body:**
```json
{
    "email": "{{email}}"
}
```

#### 2. Reset Password
- **Method**: `POST`
- **Endpoint**: `/api/auth/v1/reset-password`
- **Purpose**: Reset password using reset token

**Request Body:**
```json
{
    "token": "{{resetToken}}",
    "newPassword": "{{newPassword}}"
}
```

### 📁 User Validation
Username and email availability checking.

#### 1. Check Username Availability
- **Method**: `GET`
- **Endpoint**: `/api/auth/v1/check-username/{{checkUsername}}`
- **Purpose**: Verify if username is available

**Response:**
```json
{
    "available": true,
    "message": "Username is available"
}
```

#### 2. Check Email Availability
- **Method**: `GET`
- **Endpoint**: `/api/auth/v1/check-email/{{checkEmail}}`
- **Purpose**: Verify if email is available

### 📁 Email Verification
Email verification and resend functionality.

#### 1. Verify Email
- **Method**: `POST`
- **Endpoint**: `/api/auth/v1/verify-email/{{verificationToken}}`
- **Purpose**: Verify email address using token

#### 2. Resend Verification Email
- **Method**: `POST`
- **Endpoint**: `/api/auth/v1/resend-verification?email={{email}}`
- **Purpose**: Resend verification email

### 📁 Health & Monitoring
System health and monitoring endpoints.

#### 1. Auth Health Check
- **Method**: `GET`
- **Endpoint**: `/api/auth/v1/health`
- **Purpose**: Check authentication service health

#### 2. Actuator Health
- **Method**: `GET`
- **Endpoint**: `/actuator/health`
- **Purpose**: System health status

#### 3. Actuator Metrics
- **Method**: `GET`
- **Endpoint**: `/actuator/metrics`
- **Purpose**: Application metrics

### 📁 Rate Limiting Tests
Test rate limiting functionality.

#### 1. Test Rate Limit - Login
- **Method**: `POST`
- **Endpoint**: `/api/auth/v1/login`
- **Purpose**: Test rate limiting with invalid credentials

**Tests:**
- ✅ Rate limit headers present
- ✅ Status code 200 or 429
- ✅ Retry-After header when rate limited

## Testing Workflows

### 🔄 Complete User Journey
1. **Check Username Availability** → Verify username is free
2. **Check Email Availability** → Verify email is free
3. **Register User** → Create new account
4. **Login User** → Get access tokens
5. **Logout User** → Invalidate tokens

### 🔄 Password Reset Flow
1. **Forgot Password** → Request reset email
2. **Reset Password** → Use reset token (requires email token)

### 🔄 Email Verification Flow
1. **Register User** → Account created
2. **Resend Verification** → Request verification email
3. **Verify Email** → Confirm email (requires email token)

### 🔄 Token Management Flow
1. **Login User** → Get initial tokens
2. **Refresh Token** → Get new access token
3. **Logout User** → Invalidate all tokens

## Rate Limiting Testing

### Login Endpoint (5 requests/minute)
1. Run **Test Rate Limit - Login** 6 times quickly
2. First 5 should return 200/401
3. 6th request should return 429 (Too Many Requests)
4. Check rate limit headers:
   - `X-RateLimit-Limit: 5`
   - `X-RateLimit-Remaining: 0`
   - `Retry-After: 60`

### Register Endpoint (3 requests/minute)
1. Run **Register User** 4 times quickly
2. First 3 should return 201/409
3. 4th request should return 429

## Environment Variables

### Production Environment
```json
{
  "baseUrl": "https://api.yourdomain.com",
  "username": "produser",
  "email": "prod@yourdomain.com",
  "password": "SecurePassword123!"
}
```

### Staging Environment
```json
{
  "baseUrl": "https://staging-api.yourdomain.com",
  "username": "stageuser",
  "email": "stage@yourdomain.com",
  "password": "StagePassword123!"
}
```

## Automated Testing

### Collection Runner
1. Select **Login API Collection**
2. Choose environment
3. Set iterations: 1
4. Set delay: 1000ms
5. Click **Run Login API Collection**

### Newman CLI
```bash
# Install Newman
npm install -g newman

# Run collection
newman run Login-API-Postman-Collection.json \
  --environment Login-API-Environment.json \
  --reporters cli,html \
  --reporter-html-export results.html

# Run with specific folder
newman run Login-API-Postman-Collection.json \
  --folder "Authentication" \
  --environment Login-API-Environment.json
```

## Troubleshooting

### Common Issues

#### 1. 401 Unauthorized
- **Cause**: Invalid or expired access token
- **Solution**: Run **Login User** to get new token

#### 2. 429 Too Many Requests
- **Cause**: Rate limit exceeded
- **Solution**: Wait for rate limit window to reset (1 minute)

#### 3. 400 Bad Request
- **Cause**: Invalid request body or validation errors
- **Solution**: Check request format and required fields

#### 4. Connection Refused
- **Cause**: API server not running
- **Solution**: Start the application with `./gradlew bootRun`

### Debug Headers
Add these headers for debugging:
```
X-Debug: true
X-Request-ID: {{$guid}}
```

## Security Testing

### Invalid Credentials Test
```json
{
    "username": "invaliduser",
    "password": "wrongpassword"
}
```
Expected: 401 Unauthorized

### SQL Injection Test
```json
{
    "username": "admin'; DROP TABLE users; --",
    "password": "password"
}
```
Expected: 400 Bad Request (validation error)

### XSS Test
```json
{
    "username": "<script>alert('xss')</script>",
    "password": "password"
}
```
Expected: 400 Bad Request (validation error)

## Performance Testing

### Load Testing with Newman
```bash
# Run 100 iterations with 10ms delay
newman run Login-API-Postman-Collection.json \
  --iteration-count 100 \
  --delay-request 10 \
  --folder "Authentication"
```

### Concurrent Testing
```bash
# Run 5 concurrent instances
for i in {1..5}; do
  newman run Login-API-Postman-Collection.json &
done
wait
```

## Monitoring Integration

### Response Time Monitoring
```javascript
// Add to test scripts
pm.test("Response time is less than 200ms", function () {
    pm.expect(pm.response.responseTime).to.be.below(200);
});
```

### Custom Metrics
```javascript
// Track login success rate
if (pm.response.code === 200) {
    pm.globals.set("loginSuccessCount", 
        (pm.globals.get("loginSuccessCount") || 0) + 1);
}
```

This comprehensive guide provides everything needed to effectively test the Login API using Postman, from basic functionality to advanced security and performance testing.