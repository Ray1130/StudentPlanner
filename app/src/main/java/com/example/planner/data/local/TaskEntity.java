package com.example.planner.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class TaskEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private int viewType; // 2 cho Table Row
    private String title;
    private String deadline;
    private String note;
    private boolean isChecked;

    public TaskEntity(int viewType, String title, String deadline, String note, boolean isChecked) {
        this.viewType = viewType;
        this.title = title;
        this.deadline = deadline;
        this.note = note;
        this.isChecked = isChecked;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getViewType() { return viewType; }
    public void setViewType(int viewType) { this.viewType = viewType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public boolean isChecked() { return isChecked; }
    public void setChecked(boolean checked) { isChecked = checked; }
}
