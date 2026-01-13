package com.example.app.models;

public class VerifyOtpRequest {
    private String phone;
    private String code;
    public VerifyOtpRequest(String phone, String code) {
        this.phone = phone;
        this.code = code;
    }
}
