package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int viewType; // 0: HEADER, 1: TABLE_HEADER, 2: TABLE_ROW, v.v.
    private String title;
    private String deadline;
    private String note;
    private boolean isChecked;

    // Default constructor
    public Task() {}

    // Constructor để tạo nhanh dữ liệu mẫu
    public Task(int viewType, String title, String deadline, String note, boolean isChecked) {
        this.viewType = viewType;
        this.title = title;
        this.deadline = deadline;
        this.note = note;
        this.isChecked = isChecked;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
