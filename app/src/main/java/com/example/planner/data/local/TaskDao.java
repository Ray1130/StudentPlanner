package com.example.planner.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.OnConflictStrategy;

import com.example.planner.data.model.Task;

import java.util.List;

@Dao
public interface TaskDao {
    @Query("SELECT * FROM tasks")
    LiveData<List<Task>> getAllTasks();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);

    @Query("DELETE FROM tasks")
    void deleteAll();

    @Query("SELECT * FROM tasks LIMIT 1")
    Task getAnyTask();

    @Query("SELECT * FROM tasks WHERE subjectId = :subjectId")
    LiveData<List<Task>> getTasksBySubject(int subjectId);

    @Query("SELECT * FROM tasks WHERE isCompleted = 0")
    LiveData<List<Task>> getPendingTasks();

    @Query("SELECT * FROM tasks WHERE dueDate >= :startOfDay AND dueDate <= :endOfDay")
    LiveData<List<Task>> getTasksByDate(long startOfDay, long endOfDay);

    @Query("SELECT * FROM tasks WHERE dueDate >= :startOfDay AND dueDate <= :endOfDay")
    List<Task> getTasksByDateSync(long startOfDay, long endOfDay);
}
