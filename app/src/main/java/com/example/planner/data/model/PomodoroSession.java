package com.example.planner.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pomodoro_sessions")
public class PomodoroSession {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int taskId;
    public long startTime;
    public int duration;     //Số phút tập trung
    public boolean completed;

    public PomodoroSession(int taskId, long startTime, int duration, boolean completed) {
        this.taskId = taskId;
        this.startTime = startTime;
        this.duration = duration;
        this.completed = completed;
    }
}
