package com.example.planner.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "subjects")
public class Subject {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String code; // mã môn
    public String name;

    public Subject(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
