package com.example.planner.ui.schedule;

public class CalendarTaskInfo {
    private final String taskTitle;
    private final String subjectName;

    public CalendarTaskInfo(String taskTitle, String subjectName) {
        this.taskTitle = taskTitle;
        this.subjectName = subjectName;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public String getSubjectName() {
        return subjectName;
    }
}
