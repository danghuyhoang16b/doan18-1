package com.example.app.models;

import com.google.gson.annotations.SerializedName;

public class ClassRequest {
    @SerializedName("token")
    private String token;

    @SerializedName("class_id")
    private int classId;

    public ClassRequest(String token, int classId) {
        this.token = token;
        this.classId = classId;
    }
}
