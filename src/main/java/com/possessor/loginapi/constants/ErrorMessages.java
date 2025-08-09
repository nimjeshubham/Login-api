package com.possessor.loginapi.constants;

public final class ErrorMessages {
    
    public static final String USERNAME_EXISTS_ERROR = "Username already exists";
    public static final String EMAIL_EXISTS_ERROR = "Email already exists";
    public static final String INVALID_CREDENTIALS_ERROR = "Invalid credentials";
    public static final String INVALID_RESET_TOKEN_ERROR = "Invalid or expired reset token";
    public static final String RESET_TOKEN_EXPIRED_ERROR = "Reset token has expired";
    public static final String INVALID_VERIFICATION_TOKEN_ERROR = "Invalid verification token";
    public static final String USER_NOT_FOUND_ERROR = "User not found";
    public static final String EMAIL_SEND_FAILED_ERROR = "Failed to send email";
    public static final String EMAIL_SERVICE_UNAVAILABLE_ERROR = "Email service temporarily unavailable";
    
    private ErrorMessages() {
        throw new UnsupportedOperationException("Utility class");
    }
}