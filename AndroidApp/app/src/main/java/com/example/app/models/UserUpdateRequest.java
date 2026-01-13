package com.example.app.models;

public class UserUpdateRequest {
    public Integer user_id;
    public String full_name;
    public String email;
    public String phone;
    public String avatar_url;
    public UserUpdateRequest(Integer user_id, String full_name, String email, String phone, String avatar_url) {
        this.user_id = user_id;
        this.full_name = full_name;
        this.email = email;
        this.phone = phone;
        this.avatar_url = avatar_url;
    }
}
