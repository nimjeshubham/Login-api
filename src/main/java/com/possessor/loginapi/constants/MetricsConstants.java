package com.possessor.loginapi.constants;

public final class MetricsConstants {
    
    private MetricsConstants() {}
    
    // User metrics
    public static final String USER_PROFILE = "user.profile";
    public static final String USER_UPDATE_PROFILE = "user.update.profile";
    public static final String USER_CHANGE_PASSWORD = "user.change.password";
    public static final String USER_DELETE_ACCOUNT = "user.delete.account";
    
    // Auth metrics
    public static final String AUTH_LOGIN = "auth.login";
    public static final String AUTH_REGISTER = "auth.register";
    public static final String AUTH_FORGOT_PASSWORD = "auth.forgot.password";
    public static final String AUTH_RESET_PASSWORD = "auth.reset.password";
    public static final String AUTH_REFRESH = "auth.refresh";
    public static final String AUTH_LOGOUT = "auth.logout";
    public static final String AUTH_CHECK_USERNAME = "auth.check.username";
    public static final String AUTH_CHECK_EMAIL = "auth.check.email";
    public static final String AUTH_VERIFY_EMAIL = "auth.verify.email";
    public static final String AUTH_RESEND_VERIFICATION = "auth.resend.verification";
    
    // Descriptions
    public static final String USER_PROFILE_DESC = "Time taken to get user profile";
    public static final String USER_UPDATE_PROFILE_DESC = "Time taken to update user profile";
    public static final String USER_CHANGE_PASSWORD_DESC = "Time taken to change password";
    public static final String USER_DELETE_ACCOUNT_DESC = "Time taken to delete account";
    public static final String AUTH_LOGIN_DESC = "Time taken to login user";
    public static final String AUTH_REGISTER_DESC = "Time taken to register user";
    public static final String AUTH_FORGOT_PASSWORD_DESC = "Time taken to process forgot password";
    public static final String AUTH_RESET_PASSWORD_DESC = "Time taken to reset password";
    public static final String AUTH_REFRESH_DESC = "Time taken to refresh token";
    public static final String AUTH_LOGOUT_DESC = "Time taken to logout";
    public static final String AUTH_CHECK_USERNAME_DESC = "Time taken to check username availability";
    public static final String AUTH_CHECK_EMAIL_DESC = "Time taken to check email availability";
    public static final String AUTH_VERIFY_EMAIL_DESC = "Time taken to verify email";
    public static final String AUTH_RESEND_VERIFICATION_DESC = "Time taken to resend verification";
}