package com.example.app.models;

import com.google.gson.annotations.SerializedName;

public class Banner {
    @SerializedName("id")
    private int id;
    
    @SerializedName("image_url")
    private String imageUrl;
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("cta_text")
    private String ctaText;
    
    @SerializedName("link_url")
    private String linkUrl;

    @SerializedName("is_active")
    private int isActive;

    @SerializedName("priority")
    private int priority;

    public Banner(int id, String imageUrl, String title, String ctaText, String linkUrl, int isActive, int priority) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.title = title;
        this.ctaText = ctaText;
        this.linkUrl = linkUrl;
        this.isActive = isActive;
        this.priority = priority;
    }

    public Banner(String imageUrl, String title, String ctaText, String linkUrl) {
        this.imageUrl = imageUrl;
        this.title = title;
        this.ctaText = ctaText;
        this.linkUrl = linkUrl;
        this.isActive = 1;
        this.priority = 0;
    }

    public int getId() { return id; }
    public String getImageUrl() { return imageUrl; }
    public String getTitle() { return title; }
    public String getCtaText() { return ctaText; }
    public String getLinkUrl() { return linkUrl; }
    public boolean isActive() { return isActive == 1; }
    public void setActive(boolean active) { this.isActive = active ? 1 : 0; }
    public int getPriority() { return priority; }
}