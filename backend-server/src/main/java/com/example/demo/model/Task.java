package com.example.demo.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "view_type")
    @JsonProperty("viewType")
    private int viewType;

    private String title;
    private String deadline;
    private String note;

    @Column(name = "is_checked")
    @JsonProperty("checked")
    private boolean checked;

    public Task() {}

    public Task(int viewType, String title, String deadline, String note, boolean checked) {
        this.viewType = viewType;
        this.title = title;
        this.deadline = deadline;
        this.note = note;
        this.checked = checked;
    }

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

    public boolean isChecked() { return checked; }
    public void setChecked(boolean checked) { this.checked = checked; }
}
