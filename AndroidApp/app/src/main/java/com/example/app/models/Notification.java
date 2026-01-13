package com.example.app.models;

import com.google.gson.annotations.SerializedName;

public class Notification {
    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("content")
    private String content;

    @SerializedName("priority")
    private String priority;

    @SerializedName("created_at")
    private String createdAt;

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getPriority() { return priority; }
    public String getCreatedAt() { return createdAt; }
}
