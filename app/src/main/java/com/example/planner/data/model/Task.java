package com.example.planner.data.model;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
@Entity(tableName = "tasks")
public class Task {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public long dueDate;
    public int subjectId;
    public boolean isCompleted; // Trạng thái: Đã làm xong chưa?

    public Task(String title, long dueDate, int subjectId) {
        this.title = title;
        this.dueDate = dueDate;
        this.subjectId = subjectId;
        this.isCompleted = false; // Mặc định tạo mới là chưa hoàn thành
    }
}
