package com.example.planner.ui.task;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.planner.data.ApiService;
import com.example.planner.data.local.AppDatabase;
import com.example.planner.data.model.Subject;
import com.example.planner.data.model.Task;
import com.example.planner.data.repository.TaskRepository;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TaskViewModel extends AndroidViewModel {
    private TaskRepository repository;
    private LiveData<List<Task>> allTasks;
    private LiveData<List<Task>> pendingTasks;
    private MutableLiveData<List<Subject>> allSubjects = new MutableLiveData<>();
    private ApiService apiService;
    private AppDatabase database;

    public TaskViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
        database = AppDatabase.getDatabase(application);
        allTasks = repository.getAllTasks();
        pendingTasks = repository.getPendingTasks();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        loadSubjects();
    }

    public void loadSubjects() {
        // 1. Load từ Local DB trước
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Subject> localSubjects = database.subjectDao().getAllSubjects();
            if (localSubjects != null) {
                allSubjects.postValue(localSubjects);
            }
        });

        // 2. Load từ Server
        apiService.getAllSubjects().enqueue(new Callback<List<Subject>>() {
            @Override
            public void onResponse(Call<List<Subject>> call, Response<List<Subject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Subject> serverSubjects = response.body();
                    allSubjects.postValue(serverSubjects);
                    
                    // Cập nhật local cache
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        for (Subject s : serverSubjects) {
                            database.subjectDao().insert(s);
                        }
                    });
                } else if (allSubjects.getValue() == null) {
                    allSubjects.postValue(new ArrayList<>());
                }
            }
            @Override
            public void onFailure(Call<List<Subject>> call, Throwable t) {
                if (allSubjects.getValue() == null) {
                    allSubjects.postValue(new ArrayList<>());
                }
            }
        });
    }

    public LiveData<List<Subject>> getAllSubjects() {
        return allSubjects;
    }

    public void insertSubject(Subject subject, OnSubjectCreatedListener listener) {
        // Lưu local trước để đảm bảo mượt mà
        AppDatabase.databaseWriteExecutor.execute(() -> {
            database.subjectDao().insert(subject);
            // Sau khi insert local, chúng ta vẫn cần đẩy lên server
            // Note: Room @Insert trả về rowId nhưng model dùng Integer id từ server.
        });

        apiService.createSubject(subject).enqueue(new Callback<Subject>() {
            @Override
            public void onResponse(Call<Subject> call, Response<Subject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Subject createdSubject = response.body();
                    // Cập nhật lại local với ID từ server
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        database.subjectDao().insert(createdSubject);
                        loadSubjects();
                        if (listener != null) listener.onCreated(createdSubject);
                    });
                } else {
                    if (listener != null) listener.onCreated(null);
                }
            }
            @Override
            public void onFailure(Call<Subject> call, Throwable t) {
                if (listener != null) listener.onCreated(null);
            }
        });
    }

    public interface OnSubjectCreatedListener {
        void onCreated(Subject subject);
    }

    public void saveTask(Task task, Runnable onSuccess) {
        repository.insertTask(task);
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

    public void insert(Task task) {
        repository.insertTask(task);
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
