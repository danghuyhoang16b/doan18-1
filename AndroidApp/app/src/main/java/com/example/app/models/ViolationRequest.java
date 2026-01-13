package com.example.app.models;

public class ViolationRequest {
    private int student_id;
    private int rule_id;
    private String note;

    public ViolationRequest(int student_id, int rule_id, String note) {
        this.student_id = student_id;
        this.rule_id = rule_id;
        this.note = note;
    }
}
