package com.possessor.loginapi.constants;

public final class SuccessMessages {
    
    public static final String USER_CREATED_SUCCESS = "User created successfully";
    public static final String PASSWORD_RESET_EMAIL_SENT = "Password reset email sent";
    public static final String PASSWORD_RESET_EMAIL_FALLBACK = "If email exists, reset link will be sent";
    public static final String PASSWORD_RESET_SUCCESS = "Password reset successfully";
    public static final String LOGOUT_SUCCESS = "Logged out successfully";
    public static final String EMAIL_VERIFIED_SUCCESS = "Email verified successfully";
    public static final String VERIFICATION_EMAIL_SENT = "Verification email sent";
    public static final String EMAIL_ALREADY_VERIFIED = "Email is already verified";
    public static final String APP_HEALTH = "Authentication service is healthy";
    
    private SuccessMessages() {
        throw new UnsupportedOperationException("Utility class");
    }
}