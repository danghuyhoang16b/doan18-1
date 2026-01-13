package com.example.app.models;

public class News {
    private int id;
    private String title;
    private String summary;
    private String image_url;
    private String created_at;

    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public String getImageUrl() { return image_url; }
    public String getCreatedAt() { return created_at; }
}
