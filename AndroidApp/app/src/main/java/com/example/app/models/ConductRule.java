package com.example.app.models;

public class ConductRule {
    private int id;
    private String rule_name;
    private int points;
    private String description;

    public int getId() { return id; }
    public String getRuleName() { return rule_name; }
    public int getPoints() { return points; }
    
    @Override
    public String toString() {
        return rule_name + " (-" + points + "đ)";
    }
}
