package com.example.app.models;

import com.google.gson.annotations.SerializedName;

public class Score {
    @SerializedName("subject_name")
    private String subjectName;

    @SerializedName("score_15m")
    private float score15m;

    @SerializedName("score_45m")
    private float score45m;

    @SerializedName("score_final")
    private float scoreFinal;

    public String getSubjectName() {
        return subjectName;
    }

    public float getScore15m() {
        return score15m;
    }

    public float getScore45m() {
        return score45m;
    }

    public float getScoreFinal() {
        return scoreFinal;
    }
}
