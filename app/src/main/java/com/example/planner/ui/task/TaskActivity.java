package com.example.planner.ui.task;

import android.os.Bundle;
import android.widget.TextView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planner.R;
import com.example.planner.data.model.Subject;
import com.example.planner.data.model.Task;
import com.example.planner.utils.DateUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.planner.ui.BaseActivity;

public class TaskActivity extends BaseActivity {

    private RecyclerView rvTasks;
    private List<TaskUiModel> taskList = new ArrayList<>();
    private TaskViewModel viewModel;
    private TaskSectionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        viewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        rvTasks = findViewById(R.id.rv_tasks);
        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        
        // Khởi tạo adapter với listener
        adapter = new TaskSectionAdapter(new ArrayList<>(), new TaskSectionAdapter.OnTaskActionListener() {
            @Override
            public void onAddNewTask() { showCreateSheet(); }
            @Override
            public void onAddNewGroup() { showCreateSheet(); }
            @Override
            public void onTaskStatusChanged(TaskUiModel task) {
                // Xử lý cập nhật trạng thái
            }
            @Override
            public void onTaskLongClick(TaskUiModel task) {
                showEditSheet(task);
            }
        });
        rvTasks.setAdapter(adapter);

        findViewById(R.id.fabAddTask).setOnClickListener(v -> showCreateSheet());

        // Quan sát dữ liệu - Đây là phần quan trọng nhất để sửa lỗi không hiện task
        observeData();
        
        // Load dữ liệu ban đầu từ server
        viewModel.loadSubjects();

        setupBottomNavigation(R.id.nav_tasks);
    }

    private void showCreateSheet() {
        TaskCreateSheetFragment sheet = new TaskCreateSheetFragment();
        sheet.show(getSupportFragmentManager(), "TaskCreateSheet");
    }

    private void showEditSheet(TaskUiModel task) {
        TaskCreateSheetFragment sheet = TaskCreateSheetFragment.newInstance(task);
        sheet.show(getSupportFragmentManager(), "TaskEditSheet");
    }

    private void observeData() {
        // Tự động cập nhật khi có môn học hoặc task mới
        viewModel.getAllSubjects().observe(this, subjects -> {
            List<Task> tasks = viewModel.getAllTasks().getValue();
            if (tasks != null) {
                processAndDisplayTasks(subjects != null ? subjects : new ArrayList<>(), tasks);
            }
        });

        viewModel.getAllTasks().observe(this, tasks -> {
            if (tasks != null) {
                List<Subject> subjects = viewModel.getAllSubjects().getValue();
                processAndDisplayTasks(subjects != null ? subjects : new ArrayList<>(), tasks);
            }
        });
    }

    // Được gọi từ TaskCreateSheetFragment sau khi lưu thành công
    public void fetchTasksFromServer() {
        viewModel.loadSubjects();
    }

    private void processAndDisplayTasks(List<Subject> subjects, List<Task> tasks) {
        taskList.clear();

        // Nhóm task theo subjectId
        Map<Integer, List<Task>> groupedTasks = new HashMap<>();
        List<Task> orphanTasks = new ArrayList<>();

        for (Task task : tasks) {
            boolean found = false;
            for (Subject s : subjects) {
                if (s.id != null && s.id.equals(task.subjectId)) {
                    List<Task> group = groupedTasks.get(task.subjectId);
                    if (group == null) {
                        group = new ArrayList<>();
                        groupedTasks.put(task.subjectId, group);
                    }
                    group.add(task);
                    found = true;
                    break;
                }
            }
            if (!found) {
                orphanTasks.add(task);
            }
        }

        // 1. Duyệt qua từng môn học để tạo Header và danh sách Card
        for (Subject subject : subjects) {
            List<Task> subjectTasks = groupedTasks.get(subject.id);
            if (subjectTasks != null && !subjectTasks.isEmpty()) {
                taskList.add(new TaskUiModel(TaskUiModel.TYPE_GROUP_HEADER, subject.name, "", "", false, "low"));
                for (Task task : subjectTasks) {
                    addTaskToUiList(task);
                }
            }
        }

        // 2. Thêm các công việc không thuộc môn học nào (hoặc môn học chưa load kịp)
        if (!orphanTasks.isEmpty()) {
            taskList.add(new TaskUiModel(TaskUiModel.TYPE_GROUP_HEADER, "Chưa phân loại / Đang tải...", "", "", false, "low"));
            for (Task task : orphanTasks) {
                addTaskToUiList(task);
            }
        }

        adapter.updateData(taskList);

        TextView tvCount = findViewById(R.id.tv_task_count);
        if (tvCount != null) {
            tvCount.setText(getString(R.string.task_count_format, tasks.size()));
        }
    }

    private void addTaskToUiList(Task task) {
        taskList.add(new TaskUiModel(
                task.id != null ? task.id : 0,
                TaskUiModel.TYPE_TABLE_ROW,
                task.title,
                DateUtils.timestampToString(task.dueDate),
                "",
                task.isCompleted,
                task.priority != null ? task.priority : "low",
                task.subjectId
        ));
    }
}
