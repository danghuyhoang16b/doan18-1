package com.example.app.models;

import com.google.gson.annotations.SerializedName;

public class ConversationRequest {
    @SerializedName("token")
    private String token;

    @SerializedName("contact_id")
    private int contactId;

    public ConversationRequest(String token, int contactId) {
        this.token = token;
        this.contactId = contactId;
    }
}
