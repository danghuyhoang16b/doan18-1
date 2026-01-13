package com.example.app.models;

public class LoginResponse {
    private String message;
    private String token;
    private boolean require_change;
    private boolean captcha_required;
    private String captcha_question;
    private String captcha_token;
    private String phone;
    private String dev_code;
    private User user;
    public String getMessage() { return message; }
    public String getToken() { return token; }
    public boolean isRequireChange() { return require_change; }
    public boolean isCaptchaRequired() { return captcha_required; }
    public String getCaptchaQuestion() { return captcha_question; }
    public String getCaptchaToken() { return captcha_token; }
    public String getPhone() { return phone; }
    public String getDevCode() { return dev_code; }
    public User getUser() { return user; }
}
