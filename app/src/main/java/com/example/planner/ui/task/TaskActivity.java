package com.example.planner.ui.task;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planner.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.widget.Toast;
import com.example.planner.data.ApiService;
import com.example.planner.data.model.Subject;
import com.example.planner.data.model.Task;
import com.example.planner.utils.DateUtils;
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

        findViewById(R.id.tv_task_count);
        rvTasks = findViewById(R.id.rv_tasks);
        rvTasks.setLayoutManager(new LinearLayoutManager(this));

        // Gán sự kiện cho nút "Tạo mới" ở trên cùng
        findViewById(R.id.btn_create_task).setOnClickListener(v -> {
            TaskCreateSheetFragment sheet = new TaskCreateSheetFragment();
            sheet.show(getSupportFragmentManager(), "TaskCreateSheet");
        });

        // Gọi API lấy dữ liệu từ Spring Boot
        fetchTasksFromServer();
    }

    public void fetchTasksFromServer() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/") // IP máy tính khi dùng emulator
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        // 1. Lấy danh sách Môn học trước
        apiService.getAllSubjects().enqueue(new Callback<List<Subject>>() {
            @Override
            public void onResponse(Call<List<Subject>> call, Response<List<Subject>> responseSubjects) {
                if (responseSubjects.isSuccessful() && responseSubjects.body() != null) {
                    List<Subject> subjects = responseSubjects.body();

                    // 2. Sau đó lấy danh sách Task
                    apiService.getAllTasks().enqueue(new Callback<List<Task>>() {
                        @Override
                        public void onResponse(Call<List<Task>> call, Response<List<Task>> responseTasks) {
                            if (responseTasks.isSuccessful() && responseTasks.body() != null) {
                                List<Task> tasks = responseTasks.body();
                                processAndDisplayTasks(subjects, tasks);
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Task>> call, Throwable t) {
                            Toast.makeText(TaskActivity.this, "Lỗi lấy Task: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Subject>> call, Throwable t) {
                Toast.makeText(TaskActivity.this, "Lỗi lấy Môn học: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processAndDisplayTasks(List<Subject> subjects, List<Task> tasks) {
        taskList.clear();

        // Nhóm task theo subjectId
        Map<Integer, List<Task>> groupedTasks = new HashMap<>();
        for (Task task : tasks) {
            List<Task> group = groupedTasks.get(task.subjectId);
            if (group == null) {
                group = new ArrayList<>();
                groupedTasks.put(task.subjectId, group);
            }
            group.add(task);
        }

        // Duyệt qua từng môn học để tạo Header và Table
        for (Subject subject : subjects) {
            // Luôn thêm Header tên môn học
            taskList.add(new TaskUiModel(TaskUiModel.TYPE_GROUP_HEADER, subject.name, "", "", false));
            
            // Header của bảng bài tập
            taskList.add(new TaskUiModel(TaskUiModel.TYPE_TABLE_HEADER, "Tên bài tập", "Hạn chót", "Ghi chú", false));

            List<Task> subjectTasks = groupedTasks.get(subject.id);
            if (subjectTasks != null) {
                // Danh sách bài tập trong môn này
                for (Task task : subjectTasks) {
                    taskList.add(new TaskUiModel(
                            TaskUiModel.TYPE_TABLE_ROW,
                            task.title,
                            DateUtils.timestampToString(task.dueDate),
                            task.category,
                            task.isCompleted
                    ));
                }
            }
        }

        // Thêm các nút chức năng ở cuối danh sách
        taskList.add(new TaskUiModel(TaskUiModel.TYPE_ACTION_NEW_PAGE, "", "", "", false));
        taskList.add(new TaskUiModel(TaskUiModel.TYPE_ACTION_NEW_GROUP, "", "", "", false));

        rvTasks.setAdapter(new TaskSectionAdapter(taskList, new TaskSectionAdapter.OnTaskActionListener() {
            @Override
            public void onAddNewTask() {
                TaskCreateSheetFragment sheet = new TaskCreateSheetFragment();
                sheet.show(getSupportFragmentManager(), "TaskCreateSheet");
            }

            @Override
            public void onAddNewGroup() {
                // Có thể mở dialog tạo môn học nhanh ở đây hoặc dùng chung Sheet
                TaskCreateSheetFragment sheet = new TaskCreateSheetFragment();
                sheet.show(getSupportFragmentManager(), "TaskCreateSheet");
            }

            @Override
            public void onTaskStatusChanged(TaskUiModel task) {
                // Xử lý khi checkbox thay đổi (nếu cần)
            }
        }));

        TextView tvCount = findViewById(R.id.tv_task_count);
        if (tvCount != null) {
            tvCount.setText(getString(R.string.task_count_format, tasks.size()));
        }
    }
}
