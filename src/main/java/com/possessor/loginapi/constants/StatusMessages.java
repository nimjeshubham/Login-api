package com.possessor.loginapi.constants;

public final class StatusMessages {
    
    public static final String USERNAME_TAKEN = "Username is already taken";
    public static final String USERNAME_AVAILABLE = "Username is available";
    public static final String EMAIL_REGISTERED = "Email is already registered";
    public static final String EMAIL_AVAILABLE = "Email is available";
    
    private StatusMessages() {
        throw new UnsupportedOperationException("Utility class");
    }
}