package com.example.planner.ui.main;

public class MainTaskItem {
    public static final int PRIORITY_LOW = 0;
    public static final int PRIORITY_MEDIUM = 1;
    public static final int PRIORITY_HIGH = 2;

    private final String title;
    private final String meta;
    private final int priority;
    private final boolean completed;

    public MainTaskItem(String title, String meta, int priority, boolean completed) {
        this.title = title;
        this.meta = meta;
        this.priority = priority;
        this.completed = completed;
    }

    public String getTitle() {
        return title;
    }

    public String getMeta() {
        return meta;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isCompleted() {
        return completed;
    }
}
