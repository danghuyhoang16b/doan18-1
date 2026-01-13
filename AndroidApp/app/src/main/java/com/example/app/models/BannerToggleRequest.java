package com.example.app.models;

public class BannerToggleRequest {
    private int id;
    private boolean is_active;

    public BannerToggleRequest(int id, boolean is_active) {
        this.id = id;
        this.is_active = is_active;
    }
}
