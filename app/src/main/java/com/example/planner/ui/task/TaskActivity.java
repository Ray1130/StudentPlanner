package com.example.planner.ui.task;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planner.R;
import java.util.ArrayList;
import java.util.List;

import android.widget.Toast;
import com.example.planner.data.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TaskActivity extends AppCompatActivity {

    private RecyclerView rvTasks;
    private List<TaskUiModel> taskList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        TextView tvCount = findViewById(R.id.tv_task_count);
        rvTasks = findViewById(R.id.rv_tasks);
        rvTasks.setLayoutManager(new LinearLayoutManager(this));

        // Gọi API lấy dữ liệu từ Spring Boot
        fetchTasksFromServer();
    }

    private void fetchTasksFromServer() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/") // IP máy tính khi dùng emulator
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        apiService.getAllTasks().enqueue(new Callback<List<TaskUiModel>>() {
            @Override
            public void onResponse(Call<List<TaskUiModel>> call, Response<List<TaskUiModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    taskList = response.body();
                    rvTasks.setAdapter(new TaskSectionAdapter(taskList, new TaskSectionAdapter.OnTaskActionListener() {
                        @Override
                        public void onAddNewTask() {}

                        @Override
                        public void onAddNewGroup() {}

                        @Override
                        public void onTaskStatusChanged(TaskUiModel task) {}
                    }));
                    
                    TextView tvCount = findViewById(R.id.tv_task_count);
                    if (tvCount != null) {
                        tvCount.setText(getString(R.string.task_count_format, taskList.size()));
                    }
                }
            }

            @Override
            public void onFailure(Call<List<TaskUiModel>> call, Throwable t) {
                Toast.makeText(TaskActivity.this, "Lỗi kết nối Server: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
