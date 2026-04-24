package com.example.planner.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.planner.data.model.PomodoroSession;
import java.util.List;

@Dao
public interface PomodoroDao {
    @Insert
    void insertSession(PomodoroSession session);

    @Query("SELECT * FROM pomodoro_sessions WHERE taskId = :taskId ORDER BY startTime DESC")
    List<PomodoroSession> getSessionsForTask(int taskId);

    @Query("SELECT SUM(duration) FROM pomodoro_sessions WHERE taskId = :taskId AND completed = 1")
    int getTotalFocusTimeForTask(int taskId);
}
