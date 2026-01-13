package com.example.app.models;

public class CaptchaResponse {
    private String captcha_question;
    private String captcha_token;
    public String getCaptchaQuestion() { return captcha_question; }
    public String getCaptchaToken() { return captcha_token; }
}
