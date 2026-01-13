package com.example.app.models;

import com.google.gson.annotations.SerializedName;

public class Contact {
    @SerializedName("id")
    private int id;

    @SerializedName("full_name")
    private String fullName;

    @SerializedName("role")
    private String role;

    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRole() {
        return role;
    }
}
