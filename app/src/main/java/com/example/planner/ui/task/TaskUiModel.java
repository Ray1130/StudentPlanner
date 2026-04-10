package com.example.planner.ui.task;

public class TaskUiModel {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_TABLE_HEADER = 1;
    public static final int TYPE_TABLE_ROW = 2;
    public static final int TYPE_ACTION_NEW_PAGE = 3;
    public static final int TYPE_ACTION_NEW_GROUP = 4;

    private int viewType;
    private String title; // Dùng cho Header, Button, Task name
    private String deadline;
    private String note;
    private boolean isChecked;

    public TaskUiModel(int viewType, String title, String deadline, String note, boolean isChecked) {
        this.viewType = viewType;
        this.title = title;
        this.deadline = deadline;
        this.note = note;
        this.isChecked = isChecked;
    }

    public int getViewType() { return viewType; }
    public String getTitle() { return title; }
    public String getDeadline() { return deadline; }
    public String getNote() { return note; }
    public boolean isChecked() { return isChecked; }
}