package com.possessor.loginapi.constants;

public final class AuthConstants {
    
    // Cache Names
    public static final String USERS_CACHE = "users";
    
    // Retry Parameters
    public static final int MAX_RETRY_ATTEMPTS = 3;
    public static final long RETRY_BACKOFF_MILLIS = 100L;
    
    // Token Expiry
    public static final int RESET_TOKEN_EXPIRY_HOURS = 1;
    
    // Error Messages
    public static final String USERNAME_EXISTS_ERROR = "Username already exists";
    public static final String EMAIL_EXISTS_ERROR = "Email already exists";
    public static final String INVALID_CREDENTIALS_ERROR = "Invalid credentials";
    public static final String INVALID_RESET_TOKEN_ERROR = "Invalid or expired reset token";
    public static final String RESET_TOKEN_EXPIRED_ERROR = "Reset token has expired";
    public static final String INVALID_VERIFICATION_TOKEN_ERROR = "Invalid verification token";
    public static final String USER_NOT_FOUND_ERROR = "User not found";
    
    // Success Messages
    public static final String USER_CREATED_SUCCESS = "User created successfully";
    public static final String PASSWORD_RESET_EMAIL_SENT = "Password reset email sent";
    public static final String PASSWORD_RESET_EMAIL_FALLBACK = "If email exists, reset link will be sent";
    public static final String PASSWORD_RESET_SUCCESS = "Password reset successfully";
    public static final String LOGOUT_SUCCESS = "Logged out successfully";
    public static final String EMAIL_VERIFIED_SUCCESS = "Email verified successfully";
    public static final String VERIFICATION_EMAIL_SENT = "Verification email sent";
    
    // Availability Messages
    public static final String USERNAME_TAKEN = "Username is already taken";
    public static final String USERNAME_AVAILABLE = "Username is available";
    public static final String EMAIL_REGISTERED = "Email is already registered";
    public static final String EMAIL_AVAILABLE = "Email is available";
    
    private AuthConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
}