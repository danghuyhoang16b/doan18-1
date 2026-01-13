package com.example.app.models;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("id")
    private int id;

    @SerializedName("username")
    private String username;

    @SerializedName("full_name")
    private String fullName;

    @SerializedName("role")
    private String role;

    @SerializedName("avatar")
    private String avatar;

    @SerializedName("email")
    private String email;

    @SerializedName("phone")
    private String phone;

    @SerializedName("is_locked")
    private int isLocked;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("password")
    private String password;

    @SerializedName("is_red_star")
    private int isRedStar;

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
    public String getAvatar() { return avatar; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public boolean isLocked() { return isLocked == 1; }
    public String getCreatedAt() { return createdAt; }
    public String getPassword() { return password; }

    public void setId(int id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setRole(String role) { this.role = role; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setIsLocked(int isLocked) { this.isLocked = isLocked; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setPassword(String password) { this.password = password; }
    public boolean isRedStar() { return isRedStar == 1; }
    public void setIsRedStar(int isRedStar) { this.isRedStar = isRedStar; }
}
