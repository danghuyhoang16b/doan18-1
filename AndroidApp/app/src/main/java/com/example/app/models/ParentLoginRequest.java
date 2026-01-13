package com.example.app.models;

import com.google.gson.annotations.SerializedName;

public class ParentLoginRequest {
    @SerializedName("student_code")
    private String studentCode;
    @SerializedName("password")
    private String password;

    public ParentLoginRequest(String studentCode, String password) {
        this.studentCode = studentCode;
        this.password = password;
    }
}
