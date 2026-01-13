package com.example.app.models;

import com.google.gson.annotations.SerializedName;

public class BannerIdRequest {
    @SerializedName("token")
    private String token;
    
    @SerializedName("id")
    private int id;

    public BannerIdRequest(String token, int id) {
        this.token = token;
        this.id = id;
    }
}
