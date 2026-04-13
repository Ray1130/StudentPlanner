package com.example.planner.data;

import com.example.planner.ui.task.TaskUiModel;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("api/tasks")
    Call<List<TaskUiModel>> getAllTasks();
}
