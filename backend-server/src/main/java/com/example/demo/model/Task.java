package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private Long dueDate; // Sử dụng Long để lưu timestamp
    private Integer subjectId;
    private boolean isCompleted;
    private String priority;
    private boolean isReminderEnabled;

    // Default constructor
    public Task() {}

    // Constructor matching DemoApplication usage
    public Task(String title, Long dueDate, Integer subjectId) {
        this.title = title;
        this.dueDate = dueDate;
        this.subjectId = subjectId;
        this.isCompleted = false;
        this.priority = "low";
        this.isReminderEnabled = false;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Long getDueDate() { return dueDate; }
    public void setDueDate(Long dueDate) { this.dueDate = dueDate; }
    public Integer getSubjectId() { return subjectId; }
    public void setSubjectId(Integer subjectId) { this.subjectId = subjectId; }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public boolean isReminderEnabled() { return isReminderEnabled; }
    public void setReminderEnabled(boolean reminderEnabled) { isReminderEnabled = reminderEnabled; }
}
