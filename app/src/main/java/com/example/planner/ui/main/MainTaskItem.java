package com.example.planner.ui.main;

public class MainTaskItem {
    public static final int PRIORITY_LOW = 0;
    public static final int PRIORITY_MEDIUM = 1;
    public static final int PRIORITY_HIGH = 2;

    private final int id;
    private final String title;
    private final String meta;
    private final int priority;
    private final boolean completed;
    private final boolean reminderEnabled;

    public MainTaskItem(int id, String title, String meta, int priority, boolean completed, boolean reminderEnabled) {
        this.id = id;
        this.title = title;
        this.meta = meta;
        this.priority = priority;
        this.completed = completed;
        this.reminderEnabled = reminderEnabled;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getMeta() { return meta; }
    public int getPriority() { return priority; }
    public boolean isCompleted() { return completed; }
    public boolean isReminderEnabled() { return reminderEnabled; }
}
