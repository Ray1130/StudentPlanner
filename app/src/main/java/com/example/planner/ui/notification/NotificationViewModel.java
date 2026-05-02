package com.example.planner.ui.notification;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.planner.data.ApiService;
import com.example.planner.data.model.Task;
import com.example.planner.data.repository.TaskRepository;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NotificationViewModel extends AndroidViewModel {
    private static final String TAG = "NotificationVM";
    private final MutableLiveData<List<Task>> highPriorityTasks = new MutableLiveData<>();
    private final MutableLiveData<List<com.example.planner.data.model.Subject>> subjects = new MutableLiveData<>();
    private final ApiService apiService;
    private final TaskRepository repository;

    public NotificationViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
        String BASE_URL = "http://10.0.2.2:8080/"; 
        
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    public LiveData<List<Task>> getHighPriorityTasks() {
        return highPriorityTasks;
    }

    public LiveData<List<com.example.planner.data.model.Subject>> getSubjects() {
        return subjects;
    }

    public void fetchSubjects() {
        apiService.getAllSubjects().enqueue(new Callback<List<com.example.planner.data.model.Subject>>() {
            @Override
            public void onResponse(Call<List<com.example.planner.data.model.Subject>> call, Response<List<com.example.planner.data.model.Subject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    subjects.setValue(response.body());
                    fetchHighPriorityTasks();
                } else {
                    Log.e(TAG, "Failed to fetch subjects: " + response.code());
                    fetchHighPriorityTasks();
                }
            }

            @Override
            public void onFailure(Call<List<com.example.planner.data.model.Subject>> call, Throwable t) {
                Log.e(TAG, "Network failure (subjects): " + t.getMessage());
                fetchHighPriorityTasks();
            }
        });
    }

    public void fetchHighPriorityTasks() {
        Log.d(TAG, "Fetching high priority tasks from: " + apiService.getHighPriorityTasks().request().url());
        apiService.getHighPriorityTasks().enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Task> tasks = response.body();
                    Log.d(TAG, "Successfully fetched " + tasks.size() + " tasks");
                    highPriorityTasks.setValue(tasks);
                } else {
                    Log.e(TAG, "Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Task>> call, Throwable t) {
                Log.e(TAG, "Network failure: " + t.getMessage());
            }
        });
    }

    public void updateTask(Task task) {
        apiService.updateTask(task).enqueue(new Callback<Task>() {
            @Override
            public void onResponse(Call<Task> call, Response<Task> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Task updated successfully on server");
                    repository.updateTask(task);
                    fetchHighPriorityTasks();
                } else {
                    Log.e(TAG, "Failed to update task: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Task> call, Throwable t) {
                Log.e(TAG, "Network failure (update): " + t.getMessage());
            }
        });
    }
}
