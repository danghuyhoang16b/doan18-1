package com.example.app.models;

import com.google.gson.annotations.SerializedName;

public class SendMessageRequest {
    @SerializedName("token")
    private String token;

    @SerializedName("receiver_id")
    private int receiverId;

    @SerializedName("content")
    private String content;

    public SendMessageRequest(String token, int receiverId, String content) {
        this.token = token;
        this.receiverId = receiverId;
        this.content = content;
    }
}
