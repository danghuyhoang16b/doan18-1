package com.example.app.models;

public class TeacherLoginRequest {
    private String username;
    private String password;
    private String captcha_token;
    private String captcha_answer;
    public TeacherLoginRequest(String username, String password, String captcha_token, String captcha_answer) {
        this.username = username;
        this.password = password;
        this.captcha_token = captcha_token;
        this.captcha_answer = captcha_answer;
    }
}
