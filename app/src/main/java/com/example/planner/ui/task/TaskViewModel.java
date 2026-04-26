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
        
        // Luôn ưu tiên hiển thị dữ liệu từ Local Database (Room) để UI mượt mà
        allTasks = repository.getAllTasks();
        pendingTasks = repository.getPendingTasks();
        allSubjects = database.subjectDao().getAllSubjectsLiveData();

        // Cấu hình Retrofit cho việc đồng bộ Online
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/") // IP mặc định để emulator kết nối localhost máy tính
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        // Mỗi lần mở app, thử fetch dữ liệu mới nhất từ server về local
        loadSubjects();
    }

    public void loadSubjects() {
        apiService.getAllSubjects().enqueue(new Callback<List<Subject>>() {
            @Override
            public void onResponse(Call<List<Subject>> call, Response<List<Subject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Subject> serverSubjects = response.body();
                    AppDatabase.databaseWriteExecutor.execute(() -> {
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
        // 1. Lưu Local trước
        AppDatabase.databaseWriteExecutor.execute(() -> {
            database.subjectDao().insert(subject);
        });

        // 2. Đẩy lên Server
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
        // Lưu Local
        repository.insertTask(task);
        
        // Đồng bộ lên Server
        apiService.createTask(task).enqueue(new Callback<Task>() {
            @Override
            public void onResponse(Call<Task> call, Response<Task> response) {
                if (onSuccess != null) onSuccess.run();
            }
            @Override
            public void onFailure(Call<Task> call, Throwable t) {
                if (onSuccess != null) onSuccess.run();
            }
        });
    }

    public LiveData<List<Task>> getAllTasks() {
        return allTasks;
    }

    public LiveData<List<Task>> getPendingTasks() {
        return pendingTasks;
    }

    public void update(Task task, Runnable onSuccess) {
        repository.updateTask(task);
        apiService.updateTask(task).enqueue(new Callback<Task>() {
            @Override
            public void onResponse(Call<Task> call, Response<Task> response) {
                if (onSuccess != null) onSuccess.run();
            }
            @Override
            public void onFailure(Call<Task> call, Throwable t) {
                if (onSuccess != null) onSuccess.run();
            }
        });
    }

    public void delete(int taskId, Runnable onSuccess) {
        // Xóa local qua repository (giả định repository có hàm delete bằng ID hoặc object)
        // Lưu ý: Cần tìm object Task từ ID để xóa
        List<Task> currentTasks = allTasks.getValue();
        if (currentTasks != null) {
            for (Task t : currentTasks) {
                if (t.id != null && t.id == taskId) {
                    repository.deleteTask(t);
                    break;
                }
            }
        }
        
        apiService.deleteTask(taskId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (onSuccess != null) onSuccess.run();
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (onSuccess != null) onSuccess.run();
            }
        });
    }
}
