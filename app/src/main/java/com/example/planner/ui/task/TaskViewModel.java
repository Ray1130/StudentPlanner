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

public class TaskViewModel extends AndroidViewModel {
    private final TaskRepository repository;
    private final LiveData<List<Task>> allTasks;
    private final LiveData<List<Subject>> allSubjects;
    private final AppDatabase database;

    public TaskViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
        database = AppDatabase.getDatabase(application);

        allTasks = repository.getAllTasks();
        allSubjects = database.subjectDao().getAllSubjectsLiveData();
    }

    public void loadSubjects() {
        repository.getApiService().getAllSubjects().enqueue(new Callback<List<Subject>>() {
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

    public void loadTasks() {
        repository.cleanupExpiredTasks();
        repository.syncTasksFromServer();
    }

    public LiveData<List<Subject>> getAllSubjects() {
        return allSubjects;
    }

    public void insertSubject(Subject subject, OnSubjectCreatedListener listener) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            database.subjectDao().insert(subject);
        });

        repository.getApiService().createSubject(subject).enqueue(new Callback<Subject>() {
            @Override
            public void onResponse(Call<Subject> call, Response<Subject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Subject created = response.body();
                    AppDatabase.databaseWriteExecutor.execute(() -> database.subjectDao().insert(created));
                    if (listener != null)
                        listener.onCreated(created);
                } else {
                    if (listener != null)
                        listener.onCreated(subject);
                }
            }

            @Override
            public void onFailure(Call<Subject> call, Throwable t) {
                if (listener != null)
                    listener.onCreated(subject);
            }
        });
    }

    public void updateSubject(Subject subject) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            database.subjectDao().update(subject);
        });
    }

    public void deleteSubject(Subject subject) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Xóa tất cả task thuộc môn học này
            List<Task> tasks = database.taskDao().getTasksBySubjectSync(subject.id);
            for (Task t : tasks) {
                database.taskDao().delete(t);
            }
            database.subjectDao().delete(subject);
        });
    }

    public void deleteCategory(String category) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Lấy tất cả task thuộc category ngoại khóa
            List<Task> allTasksList = database.taskDao().getAllTasksSync();
            for (Task t : allTasksList) {
                if (category.equals(t.category) && t.subjectId <= 0) {
                    database.taskDao().delete(t);
                }
            }
        });
    }

    public void updateCategory(String oldCategory, String newCategory) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Task> allTasksList = database.taskDao().getAllTasksSync();
            for (Task t : allTasksList) {
                if (oldCategory.equals(t.category) && t.subjectId <= 0) {
                    t.category = newCategory;
                    database.taskDao().update(t);
                }
            }
        });
    }

    public interface OnSubjectCreatedListener {
        void onCreated(Subject subject);
    }

    public void saveTask(Task task, Runnable onSuccess) {
        repository.getApiService().createTask(task).enqueue(new Callback<Task>() {
            @Override
            public void onResponse(Call<Task> call, Response<Task> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Task serverTask = response.body();
                    if ((serverTask.category == null || serverTask.category.isEmpty()) && task.category != null) {
                        serverTask.category = task.category;
                    }
                    if (serverTask.expiryTimestamp <= 0 && task.expiryTimestamp > 0) {
                        serverTask.expiryTimestamp = task.expiryTimestamp;
                    }
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        database.taskDao().insert(serverTask);
                    });
                } else {
                    repository.insertTask(task);
                }
                if (onSuccess != null)
                    onSuccess.run();
            }

            @Override
            public void onFailure(Call<Task> call, Throwable t) {
                repository.insertTask(task);
                if (onSuccess != null)
                    onSuccess.run();
            }
        });
    }

    public LiveData<List<Task>> getAllTasks() {
        return allTasks;
    }

    public void toggleTaskCompletion(int taskId) {
        repository.toggleTaskCompletion(taskId);
    }

    public void update(Task task, Runnable onSuccess) {
        repository.updateTask(task);
        repository.getApiService().updateTask(task).enqueue(new Callback<Task>() {
            @Override
            public void onResponse(Call<Task> call, Response<Task> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Task updatedTask = response.body();
                    // Preserve category from local task if server didn't return it
                    if ((updatedTask.category == null || updatedTask.category.isEmpty()) && task.category != null) {
                        updatedTask.category = task.category;
                    }
                    // Preserve expiry if server returned 0 but we sent a value
                    if (updatedTask.expiryTimestamp <= 0 && task.expiryTimestamp > 0) {
                        updatedTask.expiryTimestamp = task.expiryTimestamp;
                    }
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        database.taskDao().update(updatedTask);
                    });
                }
                if (onSuccess != null)
                    onSuccess.run();
            }

            @Override
            public void onFailure(Call<Task> call, Throwable t) {
                if (onSuccess != null)
                    onSuccess.run();
            }
        });
    }

    public void delete(int taskId, Runnable onSuccess) {
        repository.deleteById(taskId);

        repository.getApiService().deleteTask(taskId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (onSuccess != null)
                    onSuccess.run();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("TaskViewModel", "Failed to delete task on server: " + t.getMessage());
                if (onSuccess != null)
                    onSuccess.run();
            }
        });
    }
}
