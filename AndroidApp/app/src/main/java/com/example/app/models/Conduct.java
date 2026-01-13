package com.example.app.models;

import com.google.gson.annotations.SerializedName;

public class Conduct {
    @SerializedName("type")
    private String type; // reward, discipline

    @SerializedName("content")
    private String content;

    @SerializedName("date")
    private String date;

    public String getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public String getDate() {
        return date;
    }
}
