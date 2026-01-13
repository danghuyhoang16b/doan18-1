package com.example.app.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class StatisticsResponse {
    @SerializedName("scores")
    private List<ScoreStat> scores;

    @SerializedName("attendance")
    private List<AttendanceStat> attendance;

    public List<ScoreStat> getScores() { return scores; }
    public List<AttendanceStat> getAttendance() { return attendance; }

    public static class ScoreStat {
        @SerializedName("subject_name")
        private String subjectName;

        @SerializedName("score")
        private float score;

        public String getSubjectName() { return subjectName; }
        public float getScore() { return score; }
    }

    public static class AttendanceStat {
        @SerializedName("status")
        private String status;

        @SerializedName("count")
        private int count;

        public String getStatus() { return status; }
        public int getCount() { return count; }
    }
}
