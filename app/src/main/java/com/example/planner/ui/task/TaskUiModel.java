package com.example.planner.ui.task;

public class TaskUiModel {
    public static final int TYPE_GROUP_HEADER = 0;
    public static final int TYPE_TABLE_HEADER = 1;
    public static final int TYPE_TABLE_ROW = 2;
    public static final int TYPE_ACTION_NEW_PAGE = 3;
    public static final int TYPE_ACTION_NEW_GROUP = 4;

    private int id;
    private int viewType;
    private String title; // Dùng cho Header, Button, Task name
    private String deadline;
    private String note;
    private boolean checked;
    private String priority; // low, medium, high

    public TaskUiModel(int viewType, String title, String deadline, String note, boolean checked, String priority) {
        this.viewType = viewType;
        this.title = title;
        this.deadline = deadline;
        this.note = note;
        this.checked = checked;
        this.priority = priority;
    }

    public TaskUiModel(int id, int viewType, String title, String deadline, String note, boolean checked, String priority) {
        this.id = id;
        this.viewType = viewType;
        this.title = title;
        this.deadline = deadline;
        this.note = note;
        this.checked = checked;
        this.priority = priority;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getViewType() { return viewType; }
    public String getTitle() { return title; }
    public String getDeadline() { return deadline; }
    public String getNote() { return note; }
    public boolean isChecked() { return checked; }
    public void setChecked(boolean checked) { this.checked = checked; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}