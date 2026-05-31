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
    private String subtitle;
    private boolean checked;
    private String priority; // low, medium, high
    private int subjectId;
    private String category;
    private boolean isReminderEnabled;
    private long expiryTimestamp;

    public TaskUiModel(int viewType, String title, String deadline, String note, boolean checked, String priority) {
        this.viewType = viewType;
        this.title = title;
        this.deadline = deadline;
        this.note = note;
        this.checked = checked;
        this.priority = priority;
        this.isReminderEnabled = false;
        this.category = "";
        this.subtitle = "";
    }

    public TaskUiModel(int id, int viewType, String title, String deadline, String note, boolean checked,
            String priority, int subjectId, boolean isReminderEnabled, String category, String subtitle) {
        this.id = id;
        this.viewType = viewType;
        this.title = title;
        this.deadline = deadline;
        this.note = note;
        this.checked = checked;
        this.priority = priority;
        this.subjectId = subjectId;
        this.isReminderEnabled = isReminderEnabled;
        this.category = category;
        this.subtitle = subtitle;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public int getViewType() {
        return viewType;
    }

    public String getTitle() {
        return title;
    }

    public String getDeadline() {
        return deadline;
    }

    public String getNote() {
        return note;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isReminderEnabled() {
        return isReminderEnabled;
    }

    public void setReminderEnabled(boolean reminderEnabled) {
        isReminderEnabled = reminderEnabled;
    }

    public long getExpiryTimestamp() {
        return expiryTimestamp;
    }

    public void setExpiryTimestamp(long expiryTimestamp) {
        this.expiryTimestamp = expiryTimestamp;
    }
}
