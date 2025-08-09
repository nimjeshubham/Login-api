package com.possessor.loginapi.constants;

public final class LogMessages {
    
    // Registration Log Messages
    public static final String REGISTRATION_ATTEMPT = "Registration attempt for username: {}";
    public static final String USER_REGISTERED_SUCCESS = "User registered successfully: {}";
    public static final String REGISTRATION_FAILED = "Registration failed for username: {}";
    
    // Login Log Messages
    public static final String LOGIN_ATTEMPT = "Login attempt for username: {}";
    public static final String CREDENTIALS_VALIDATED = "User credentials validated successfully: {}";
    public static final String INVALID_PASSWORD = "Invalid password for username: {}";
    public static final String LOGIN_FAILED = "Login failed for username: {}";
    
    // Password Reset Log Messages
    public static final String PASSWORD_RESET_REQUESTED = "Password reset requested for email: {}";
    public static final String PASSWORD_RESET_COMPLETED = "Password reset completed for token: {}";
    
    // Email Verification Log Messages
    public static final String EMAIL_VERIFIED = "Email verified for token: {}";
    
    private LogMessages() {
        throw new UnsupportedOperationException("Utility class");
    }
}