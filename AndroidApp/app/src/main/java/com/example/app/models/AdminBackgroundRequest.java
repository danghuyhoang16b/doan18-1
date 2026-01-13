package com.example.app.models;

public class AdminBackgroundRequest {
    private String target; // "mobile" or "pc"
    private String image_key; // e.g., "banner_1"
    public AdminBackgroundRequest(String target, String image_key) {
        this.target = target;
        this.image_key = image_key;
    }
}
