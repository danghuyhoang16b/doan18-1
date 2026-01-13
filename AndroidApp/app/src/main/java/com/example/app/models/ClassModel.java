package com.example.app.models;

import com.google.gson.annotations.SerializedName;

public class ClassModel {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    public ClassModel() {}
    public ClassModel(int id, String name) { this.id = id; this.name = name; }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return name;
    }
}
