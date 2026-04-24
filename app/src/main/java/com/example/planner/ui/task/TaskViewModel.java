package com.example.planner.ui.task;

import android.app.Application;
import android.util.Log; // Đã thêm import Log để bắt bệnh

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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
        // 1. Thử load từ Local DB trước để UI có dữ liệu ngay
        new Thread(() -> {
            List<Subject> localSubjects = database.subjectDao().getAllSubjects();
            if (localSubjects != null && !localSubjects.isEmpty()) {
                allSubjects.postValue(localSubjects);
            }
        }).start();

        // 2. Load từ Server để cập nhật mới nhất
        apiService.getAllSubjects().enqueue(new Callback<List<Subject>>() {
            @Override
            public void onResponse(Call<List<Subject>> call, Response<List<Subject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Subject> serverSubjects = response.body();
                    allSubjects.postValue(serverSubjects);
                    
                    // Cập nhật lại cache local
                    new Thread(() -> {
                        for (Subject s : serverSubjects) {
                            database.subjectDao().insert(s); // Có thể dùng @Insert(onConflict = OnConflictStrategy.REPLACE)
                        }
                    }).start();
                } else {
                    Log.e("TaskViewModel", "Lỗi tải môn học từ Server code: " + response.code());
                    // Đảm bảo không bị null để không hiện "đang tải" mãi
                    if (allSubjects.getValue() == null) {
                        allSubjects.postValue(new java.util.ArrayList<>());
                    }
                }
            }
            @Override
            public void onFailure(Call<List<Subject>> call, Throwable t) {
                Log.e("TaskViewModel", "Lỗi mạng khi tải môn học: " + t.getMessage());
                // Đảm bảo không bị null
                if (allSubjects.getValue() == null) {
                    allSubjects.postValue(new java.util.ArrayList<>());
                }
            }
        });
    }

    public LiveData<List<Subject>> getAllSubjects() {
        return allSubjects;
    }

    public interface OnSubjectCreatedListener {
        void onCreated(Subject subject);
    }

    public void insertSubject(Subject subject, OnSubjectCreatedListener listener) {
        apiService.createSubject(subject).enqueue(new Callback<Subject>() {
            @Override
            public void onResponse(Call<Subject> call, Response<Subject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Subject createdSubject = response.body();
                    Log.d("TaskViewModel", "Tạo môn học server thành công! ID: " + createdSubject.id);
                    loadSubjects();
                    if (listener != null) listener.onCreated(createdSubject);
                } else {
                    Log.e("TaskViewModel", "Lỗi tạo môn học Server code: " + response.code());
                    if (listener != null) listener.onCreated(null);
                }
            }
            @Override
            public void onFailure(Call<Subject> call, Throwable t) {
                Log.e("TaskViewModel", "Lỗi mạng tạo môn học: " + t.getMessage());
                if (listener != null) listener.onCreated(null);
            }
        });
    }

    public void saveTask(Task task, Runnable onSuccess) {
        // Lưu local trước
        repository.insertTask(task);
        Log.d("TaskViewModel", "Đã lưu Task vào Room DB local");

        // Đẩy lên backend
        apiService.createTask(task).enqueue(new Callback<Task>() {
            @Override
            public void onResponse(Call<Task> call, Response<Task> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("TaskViewModel", "Lưu Task lên server thành công!");
                    // Cập nhật lại Task từ server (có ID thật) vào Room nếu cần
                    // Ở đây đơn giản là báo thành công
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                } else {
                    Log.e("TaskViewModel", "Lưu Task thất bại. Server trả về code: " + response.code());
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                }
            }
            @Override
            public void onFailure(Call<Task> call, Throwable t) {
                Log.e("TaskViewModel", "Lỗi mạng hoặc Crash Retrofit khi lưu Task: " + t.getMessage());
                if (onSuccess != null) {
                    onSuccess.run();
                }
            }
        });
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
        // Tìm và xóa trong Room trước
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