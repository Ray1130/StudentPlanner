package com.example.planner.data.model;

import com.google.gson.annotations.SerializedName;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class Task {
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    public Integer id;

    @SerializedName("title")
    public String title;
    
    @SerializedName("dueDate")
    public long dueDate;
    
    @SerializedName("subjectId")
    public int subjectId;
    
    @SerializedName("completed")
    public boolean isCompleted;

    @SerializedName("timestamp")
    public long timestamp;

    @SerializedName("category")
    @ColumnInfo(name = "category")
    public String category;

    @SerializedName("priority")
    @ColumnInfo(name = "priority")
    public String priority;

    @SerializedName("reminderEnabled")
    @ColumnInfo(name = "reminderEnabled")
    public boolean isReminderEnabled;

    @SerializedName("note")
    @ColumnInfo(name = "note")
    public String note;

    public Task(String title, long dueDate, int subjectId) {
        this.title = title;
        this.dueDate = dueDate;
        this.subjectId = subjectId;
        this.isCompleted = false;
        this.isReminderEnabled = false;
        this.id = null;
        this.category = "";
        this.note = "";
        this.priority = "low";
    }
}
