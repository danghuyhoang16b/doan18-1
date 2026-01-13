package com.example.app.models;

import com.google.gson.annotations.SerializedName;

public class CreateNotificationRequest {
    @SerializedName("token")
    private String token;

    @SerializedName("title")
    private String title;

    @SerializedName("content")
    private String content;

    @SerializedName("priority")
    private String priority;

    public CreateNotificationRequest(String token, String title, String content, String priority) {
        this.token = token;
        this.title = title;
        this.content = content;
        this.priority = priority;
    }
}
