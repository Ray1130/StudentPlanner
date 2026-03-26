package com.example.planner.ui.task;
import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.planner.data.model.Task;
import com.example.planner.data.repository.TaskRepository;
import java.util.List;
public class TaskViewModel extends AndroidViewModel {
    private TaskRepository repository;

    // LiveData giúp giao diện tự động cập nhật khi có thay đổi
    private LiveData<List<Task>> allTasks;
    private LiveData<List<Task>> pendingTasks;

    public TaskViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
        allTasks = repository.getAllTasks();
        pendingTasks = repository.getPendingTasks();
    }

    // --- CÁC HÀM ĐỂ GIAO DIỆN (UI) LẤY DỮ LIỆU ĐỂ HIỂN THỊ ---
    public LiveData<List<Task>> getAllTasks() {
        return allTasks;
    }

    public LiveData<List<Task>> getPendingTasks() {
        return pendingTasks;
    }

    //CÁC HÀM ĐỂ GIAO DIỆN GỌI KHI NGƯỜI DÙNG BẤM NÚT THÊM/SỬA/XÓA ---
    public void insert(Task task) {
        repository.insertTask(task);
    }
    public void update(Task task) {
        repository.updateTask(task);
    }
    public void delete(Task task) {
        repository.deleteTask(task);
    }
}
