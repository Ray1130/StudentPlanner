package com.example.planner.ui.task;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.planner.data.ApiService;
import com.example.planner.data.local.AppDatabase;
import com.example.planner.data.model.Subject;
import com.example.planner.data.model.Task;
import com.example.planner.data.repository.TaskRepository;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TaskViewModel extends AndroidViewModel {
    private final TaskRepository repository;
    private final LiveData<List<Task>> allTasks;
    private final LiveData<List<Task>> pendingTasks;
    private final LiveData<List<Subject>> allSubjects;
    private final ApiService apiService;
    private final AppDatabase database;

    public TaskViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
        database = AppDatabase.getDatabase(application);
        
        // Luôn ưu tiên hiển thị dữ liệu từ Local Database
        allTasks = repository.getAllTasks();
        pendingTasks = repository.getPendingTasks();
        allSubjects = database.subjectDao().getAllSubjectsLiveData();

        // Cấu hình Retrofit cho việc đồng bộ Online
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/") //
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        loadSubjects();
        syncTasksFromServer();
    }

    public void loadSubjects() {
        apiService.getAllSubjects().enqueue(new Callback<List<Subject>>() {
            @Override
            public void onResponse(Call<List<Subject>> call, Response<List<Subject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Subject> serverSubjects = response.body();
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        database.subjectDao().deleteAll();
                        for (Subject s : serverSubjects) {
                            database.subjectDao().insert(s);
                        }
                    });
                }
            }
            @Override
            public void onFailure(Call<List<Subject>> call, Throwable t) {
                Log.e("Sync", "Cannot connect to server, using local data only.");
            }
        });
    }

    public LiveData<List<Subject>> getAllSubjects() {
        return allSubjects;
    }

    public void insertSubject(Subject subject, OnSubjectCreatedListener listener) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            database.subjectDao().insert(subject);
        });

        apiService.createSubject(subject).enqueue(new Callback<Subject>() {
            @Override
            public void onResponse(Call<Subject> call, Response<Subject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Subject created = response.body();
                    AppDatabase.databaseWriteExecutor.execute(() -> database.subjectDao().insert(created));
                    if (listener != null) listener.onCreated(created);
                } else {
                    if (listener != null) listener.onCreated(subject);
                }
            }
            @Override
            public void onFailure(Call<Subject> call, Throwable t) {
                if (listener != null) listener.onCreated(subject);
            }
        });
    }

    public interface OnSubjectCreatedListener {
        void onCreated(Subject subject);
    }

    public void saveTask(Task task, Runnable onSuccess) {
        // 1. Gửi lên Server trước để lấy ID thật
        apiService.createTask(task).enqueue(new Callback<Task>() {
            @Override
            public void onResponse(Call<Task> call, Response<Task> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Task serverTask = response.body();
                    // 2. Lưu vào Local với ID từ Server
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        database.taskDao().insert(serverTask);
                    });
                } else {
                    // Nếu server lỗi, lưu tạm vào local (id sẽ tự sinh)
                    repository.insertTask(task);
                }
                if (onSuccess != null) onSuccess.run();
            }
            @Override
            public void onFailure(Call<Task> call, Throwable t) {
                repository.insertTask(task);
                if (onSuccess != null) onSuccess.run();
            }
        });
    }

    public void syncTasksFromServer() {
        apiService.getAllTasks().enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Task> serverTasks = response.body();
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        // Xóa sạch local và thay bằng data từ server để đồng bộ ID
                        database.taskDao().deleteAll();
                        for (Task t : serverTasks) {
                            database.taskDao().insert(t);
                        }
                    });
                }
            }
            @Override
            public void onFailure(Call<List<Task>> call, Throwable t) {
                Log.e("Sync", "Failed to sync tasks: " + t.getMessage());
            }
        });
    }

    public LiveData<List<Task>> getAllTasks() {
        return allTasks;
    }

    public LiveData<List<Task>> getPendingTasks() {
        return pendingTasks;
    }

    public void toggleTaskCompletion(int taskId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Task task = database.taskDao().getTaskByIdSync(taskId);
            if (task != null) {
                task.isCompleted = !task.isCompleted;
                // 1. Cập nhật Local
                database.taskDao().update(task);

                // 2. Gửi lên Server
                apiService.updateTask(task).enqueue(new Callback<Task>() {
                    @Override
                    public void onResponse(Call<Task> call, Response<Task> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Log.d("TaskViewModel", "Server updated completion for task: " + taskId);
                            AppDatabase.databaseWriteExecutor.execute(() -> {
                                database.taskDao().update(response.body());
                            });
                        }
                    }
                    @Override
                    public void onFailure(Call<Task> call, Throwable t) {
                        Log.e("TaskViewModel", "Failed to sync completion to server: " + t.getMessage());
                    }
                });
            }
        });
    }

    public void update(Task task, Runnable onSuccess) {
        repository.updateTask(task);
        apiService.updateTask(task).enqueue(new Callback<Task>() {
            @Override
            public void onResponse(Call<Task> call, Response<Task> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Cập nhật lại local với dữ liệu chuẩn từ server (nếu cần)
                    Task updatedTask = response.body();
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        database.taskDao().update(updatedTask);
                    });
                }
                if (onSuccess != null) onSuccess.run();
            }
            @Override
            public void onFailure(Call<Task> call, Throwable t) {
                if (onSuccess != null) onSuccess.run();
            }
        });
    }

    public void delete(int taskId, Runnable onSuccess) {
        repository.deleteById(taskId);

        apiService.deleteTask(taskId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (onSuccess != null) onSuccess.run();
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("TaskViewModel", "Failed to delete task on server: " + t.getMessage());
                if (onSuccess != null) onSuccess.run();
            }
        });
    }
}
