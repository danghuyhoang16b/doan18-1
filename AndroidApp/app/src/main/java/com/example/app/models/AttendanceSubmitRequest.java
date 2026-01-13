package com.example.app.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AttendanceSubmitRequest {
    @SerializedName("token")
    private String token;

    @SerializedName("class_id")
    private int classId;

    @SerializedName("date")
    private String date;

    @SerializedName("attendance_list")
    private List<AttendanceItem> attendanceList;

    public AttendanceSubmitRequest(String token, int classId, String date, List<AttendanceItem> attendanceList) {
        this.token = token;
        this.classId = classId;
        this.date = date;
        this.attendanceList = attendanceList;
    }

    public static class AttendanceItem {
        @SerializedName("student_id")
        private int studentId;

        @SerializedName("status")
        private String status;

        @SerializedName("note")
        private String note;

        public AttendanceItem(int studentId, String status, String note) {
            this.studentId = studentId;
            this.status = status;
            this.note = note;
        }
    }
}
