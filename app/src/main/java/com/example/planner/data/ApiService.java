package com.example.planner.data;

import com.example.planner.data.model.Subject;
import com.example.planner.data.model.Task;
import com.example.planner.ui.task.TaskUiModel;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @GET("api/tasks")
    Call<List<Task>> getAllTasks();

    @POST("api/tasks")
    Call<Task> createTask(@Body Task task);

    @GET("api/subjects")
    Call<List<Subject>> getAllSubjects();

    @POST("api/subjects")
    Call<Subject> createSubject(@Body Subject subject);
}
