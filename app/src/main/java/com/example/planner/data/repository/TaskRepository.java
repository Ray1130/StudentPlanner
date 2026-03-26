package com.example.planner.data.repository;
import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import androidx.lifecycle.LiveData;
import com.example.planner.data.local.AppDatabase;
import com.example.planner.data.local.TaskDao;
import com.example.planner.data.model.Task;
public class TaskRepository {
    private TaskDao taskDao;
    private ExecutorService executorService;

    public TaskRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        taskDao = db.taskDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insertTask(Task task) {
        executorService.execute(() -> taskDao.insert(task));
    }

    public void updateTask(Task task) {
        executorService.execute(() -> taskDao.update(task));
    }

    public void deleteTask(Task task) {
        executorService.execute(() -> taskDao.delete(task));
    }
    public LiveData<List<Task>> getAllTasks() {
        return taskDao.getAllTasks();
    }

    public LiveData<List<Task>> getTasksBySubject(int subjectId) {
        return taskDao.getTasksBySubject(subjectId);
    }

    public LiveData<List<Task>> getPendingTasks() {
        return taskDao.getPendingTasks();
    }
}
