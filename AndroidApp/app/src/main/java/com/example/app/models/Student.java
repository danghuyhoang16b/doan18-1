package com.example.app.models;

import com.google.gson.annotations.SerializedName;

public class Student {
    @SerializedName("id")
    private int id;

    @SerializedName("full_name")
    private String fullName;

    @SerializedName("code")
    private String code;
    @SerializedName("avatar")
    private String avatar;

    // Local state for attendance
    private String status = "present"; // present, absent, late
    private String note = "";

    public Student() { }

    public Student(int id, String fullName, String code) {
        this.id = id;
        this.fullName = fullName;
        this.code = code;
    }

    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getCode() {
        return code;
    }
    public String getAvatar() { return avatar; }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
