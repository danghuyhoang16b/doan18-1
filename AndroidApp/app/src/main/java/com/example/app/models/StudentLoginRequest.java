package com.example.app.models;

public class StudentLoginRequest {
    private String identifier;
    private String password;
    private String captcha_token;
    private String captcha_answer;
    public StudentLoginRequest(String identifier, String password, String captcha_token, String captcha_answer) {
        this.identifier = identifier;
        this.password = password;
        this.captcha_token = captcha_token;
        this.captcha_answer = captcha_answer;
    }
}
