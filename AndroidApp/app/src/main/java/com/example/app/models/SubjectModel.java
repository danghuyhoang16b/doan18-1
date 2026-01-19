package com.example.app.models;

import com.google.gson.annotations.SerializedName;

public class SubjectModel {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    public SubjectModel() {}
    public SubjectModel(int id, String name) { this.id = id; this.name = name; }

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
