package com.possessor.loginapi.constants;

public final class ApiEndpoints {
    
    private ApiEndpoints() {}
    
    // Base paths
    public static final String API_BASE = "/api";
    public static final String AUTH_BASE = API_BASE + "/auth";
    public static final String USER_BASE = API_BASE + "/user";
    public static final String ACTUATOR_BASE = "/actuator";
    
    // Auth endpoints
    public static final String AUTH_REGISTER = "/register";
    public static final String AUTH_LOGIN = "/login";
    public static final String AUTH_HEALTH = "/health";
    public static final String AUTH_FORGOT_PASSWORD = "/forgot-password";
    public static final String AUTH_RESET_PASSWORD = "/reset-password";
    public static final String AUTH_REFRESH = "/refresh";
    public static final String AUTH_LOGOUT = "/logout";
    public static final String AUTH_CHECK_USERNAME = "/check-username/{username}";
    public static final String AUTH_CHECK_EMAIL = "/check-email/{email}";
    public static final String AUTH_VERIFY_EMAIL = "/verify-email/{token}";
    public static final String AUTH_RESEND_VERIFICATION = "/resend-verification";
    
    // User endpoints
    public static final String USER_PROFILE = "/profile";
    public static final String USER_PASSWORD = "/password";
    public static final String USER_ACCOUNT = "/account";
    
    // Actuator endpoints
    public static final String ACTUATOR_HEALTH = "/health";
    public static final String ACTUATOR_INFO = "/info";
    public static final String ACTUATOR_METRICS = "/metrics";
    public static final String ACTUATOR_PROMETHEUS = "/prometheus";
    
    // Full endpoint paths
    public static final String FULL_AUTH_REGISTER = AUTH_BASE + AUTH_REGISTER;
    public static final String FULL_AUTH_LOGIN = AUTH_BASE + AUTH_LOGIN;
    public static final String FULL_AUTH_HEALTH = AUTH_BASE + AUTH_HEALTH;
    public static final String FULL_AUTH_FORGOT_PASSWORD = AUTH_BASE + AUTH_FORGOT_PASSWORD;
    public static final String FULL_AUTH_RESET_PASSWORD = AUTH_BASE + AUTH_RESET_PASSWORD;
    public static final String FULL_AUTH_REFRESH = AUTH_BASE + AUTH_REFRESH;
    public static final String FULL_AUTH_LOGOUT = AUTH_BASE + AUTH_LOGOUT;
    public static final String FULL_AUTH_CHECK_USERNAME = AUTH_BASE + AUTH_CHECK_USERNAME;
    public static final String FULL_AUTH_CHECK_EMAIL = AUTH_BASE + AUTH_CHECK_EMAIL;
    public static final String FULL_AUTH_VERIFY_EMAIL = AUTH_BASE + AUTH_VERIFY_EMAIL;
    public static final String FULL_AUTH_RESEND_VERIFICATION = AUTH_BASE + AUTH_RESEND_VERIFICATION;
    
    public static final String FULL_USER_PROFILE = USER_BASE + USER_PROFILE;
    public static final String FULL_USER_PASSWORD = USER_BASE + USER_PASSWORD;
    public static final String FULL_USER_ACCOUNT = USER_BASE + USER_ACCOUNT;
    
    public static final String FULL_ACTUATOR_HEALTH = ACTUATOR_BASE + ACTUATOR_HEALTH;
    public static final String FULL_ACTUATOR_INFO = ACTUATOR_BASE + ACTUATOR_INFO;
    public static final String FULL_ACTUATOR_METRICS = ACTUATOR_BASE + ACTUATOR_METRICS;
    public static final String FULL_ACTUATOR_PROMETHEUS = ACTUATOR_BASE + ACTUATOR_PROMETHEUS;
}