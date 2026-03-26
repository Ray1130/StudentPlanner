package com.example.planner.data.local;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import com.example.planner.data.model.Subject;
@Dao
public interface SubjectDao {
    @Insert
    void insert(Subject subject); // Lệnh thêm môn học mới

    @Update
    void update(Subject subject); // Lệnh sửa tên/mã môn học

    @Delete
    void delete(Subject subject); // Lệnh xóa môn học

    @Query("SELECT * FROM subjects")
    List<Subject> getAllSubjects(); // Lệnh lấy danh sách toàn bộ môn học
}
