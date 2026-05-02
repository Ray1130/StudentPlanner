package com.example.planner.data.model;
import com.google.gson.annotations.SerializedName;
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
    
    @SerializedName("isCompleted")
    public boolean isCompleted; // Trạng thái: Đã làm xong chưa?

    public long timestamp; // Thời gian thực hiện
    public String category; // Để phân biệt màu sắc hoặc loại
    public String priority; // low, medium, high
    public boolean isReminderEnabled;
    public String note;

    public Task(String title, long dueDate, int subjectId) {
        this.title = title;
        this.dueDate = dueDate;
        this.subjectId = subjectId;
        this.isCompleted = false; // Mặc định tạo mới là chưa hoàn thành
        this.priority = "low"; // Mặc định là low
        this.isReminderEnabled = false;
        this.id = null;
    }
}
