package com.example.planner.data.repository;

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.LiveData;
import com.example.planner.data.ApiService;
import com.example.planner.data.local.AppDatabase;
import com.example.planner.data.local.TaskDao;
import com.example.planner.data.model.Task;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TaskRepository {
    private TaskDao taskDao;
    private ApiService apiService;
    private ExecutorService executorService;

    public TaskRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        taskDao = db.taskDao();
        executorService = Executors.newSingleThreadExecutor();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    public void toggleTaskCompletion(int taskId) {
        executorService.execute(() -> {
            Task task = taskDao.getTaskByIdSync(taskId);
            if (task != null) {
                task.isCompleted = !task.isCompleted;
                taskDao.update(task);
                Log.d("TaskRepository", "Toggled task " + taskId + " locally to " + task.isCompleted);

                apiService.updateTask(task).enqueue(new Callback<Task>() {
                    @Override
                    public void onResponse(Call<Task> call, Response<Task> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Log.d("TaskRepository", "Sync successful for task " + taskId);
                            executorService.execute(() -> taskDao.update(response.body()));
                        } else {
                            Log.e("TaskRepository", "Sync failed for task " + taskId + ": " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Task> call, Throwable t) {
                        Log.e("TaskRepository", "Sync error for task " + taskId + ": " + t.getMessage());
                    }
                });
            } else {
                Log.e("TaskRepository", "Task not found: " + taskId);
            }
        });
    }

    public void syncTasksFromServer() {
        apiService.getAllTasks().enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Task> serverTasks = response.body();
                    executorService.execute(() -> {
                        // Không dùng deleteAll() để tránh mất dữ liệu local chưa kịp sync
                        for (Task t : serverTasks) {
                            taskDao.insert(t);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Task>> call, Throwable t) {
                Log.e("TaskRepository", "Failed to sync tasks: " + t.getMessage());
            }
        });
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

    public void deleteById(int taskId) {
        executorService.execute(() -> taskDao.deleteById(taskId));
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

    public LiveData<List<Task>> getTasksByDate(long startOfDay, long endOfDay) {
        return taskDao.getTasksByDate(startOfDay, endOfDay);
    }

    public ApiService getApiService() {
        return apiService;
    }
}
