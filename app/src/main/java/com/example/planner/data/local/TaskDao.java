package com.example.planner.data.local;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import com.example.planner.data.model.Task;
@Dao
public interface TaskDao {
    @Insert
    void insert(Task task); // Thêm deadline mới

    @Update
    void update(Task task); // Cập nhật (ví dụ: đánh dấu tick đã hoàn thành)

    @Delete
    void delete(Task task); // Xóa deadline

    // Lấy toàn bộ deadline, sắp xếp theo hạn nộp (cái nào gấp lên đầu)
    @Query("SELECT * FROM tasks ORDER BY dueDate ASC")
    LiveData<List<Task>> getAllTasks();

    @Query("SELECT * FROM tasks WHERE subjectId = :subjectId ORDER BY dueDate ASC")
    LiveData<List<Task>> getTasksBySubject(int subjectId);

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY dueDate ASC")
    LiveData<List<Task>> getPendingTasks();
}
