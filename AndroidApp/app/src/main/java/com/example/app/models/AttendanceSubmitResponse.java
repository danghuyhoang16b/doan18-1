package com.example.app.models;

import com.google.gson.annotations.SerializedName;

public class AttendanceSubmitResponse {
    @SerializedName("message")
    private String message;

    @SerializedName("summary")
    private Summary summary;

    public String getMessage() {
        return message;
    }

    public Summary getSummary() {
        return summary;
    }

    public static class Summary {
        @SerializedName("total")
        private int total;

        @SerializedName("present")
        private int present;

        @SerializedName("absent_excused")
        private int absentExcused;

        @SerializedName("absent_unexcused")
        private int absentUnexcused;

        @SerializedName("late")
        private int late;

        public int getTotal() {
            return total;
        }

        public int getPresent() {
            return present;
        }

        public int getAbsentExcused() {
            return absentExcused;
        }

        public int getAbsentUnexcused() {
            return absentUnexcused;
        }

        public int getLate() {
            return late;
        }

        public String toDisplayString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Tổng: ").append(total).append("\n");
            sb.append("Có mặt: ").append(present).append("\n");
            sb.append("Vắng có phép: ").append(absentExcused).append("\n");
            sb.append("Vắng không phép: ").append(absentUnexcused).append("\n");
            sb.append("Đi muộn: ").append(late);
            return sb.toString();
        }
    }
}
