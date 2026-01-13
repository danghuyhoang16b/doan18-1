package com.example.app.models;

public class ScheduleItem {
    private int day_of_week;
    private int period;
    private String subject_name;
    private String teacher_name;
    private String class_name;

    public int getDayOfWeek() { return day_of_week; }
    public int getPeriod() { return period; }
    public String getSubjectName() { return subject_name; }
    public String getTeacherName() { return teacher_name; }
    public String getClassName() { return class_name; }
}
