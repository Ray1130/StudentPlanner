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
        
        adapter = new TaskSectionAdapter(new ArrayList<>(), new TaskSectionAdapter.OnTaskActionListener() {
            @Override
            public void onAddNewTask() { showCreateSheet(); }
            @Override
            public void onAddNewGroup() { showCreateSheet(); }
            
            @Override
            public void onTaskClick(TaskUiModel task) {
                showEditSheet(task);
            }

            @Override
            public void onTaskStatusChanged(TaskUiModel task) {
                // Sử dụng hàm toggle chuyên dụng để lấy data gốc từ DB và đổi trạng thái
                viewModel.toggleTaskCompletion(task.getId());
            }

            @Override
            public void onTaskLongClick(TaskUiModel task) {
                showEditSheet(task);
            }
        });
        rvTasks.setAdapter(adapter);

        findViewById(R.id.fabAddTask).setOnClickListener(v -> showCreateSheet());
        
        // UI MOCKUP - DELETE WHEN INTEGRATING REAL LOGIC
        initMockData();

        // observeData();
        // viewModel.loadSubjects();
        setupBottomNavigation(R.id.nav_tasks);
    }

    // UI MOCKUP - DELETE WHEN INTEGRATING REAL LOGIC
    private void initMockData() {
        taskList.clear();
        taskList.add(new TaskUiModel(1, TaskUiModel.TYPE_TABLE_ROW, "Bài tập chương 3", "Hôm nay, 17:00", "Kinh tế vĩ mô • KTN201", false, "high", 1, true));
        taskList.add(new TaskUiModel(2, TaskUiModel.TYPE_TABLE_ROW, "Thuyết trình giữa kỳ", "Mai, 10:00", "Marketing căn bản • MAR301", false, "medium", 1, true));
        taskList.add(new TaskUiModel(3, TaskUiModel.TYPE_TABLE_ROW, "Đọc tài liệu tuần 5", "Thứ 5, 25/05", "Tiếng Anh học thuật • ENG102", false, "low", 1, true));
        taskList.add(new TaskUiModel(4, TaskUiModel.TYPE_TABLE_ROW, "Họp CLB Truyền thông", "Thứ 6, 26/05 • 19:30", "CLB Truyền thông • Phòng B203", false, "medium", 2, false));
        taskList.add(new TaskUiModel(5, TaskUiModel.TYPE_TABLE_ROW, "Chuẩn bị kế hoạch Mùa hè xanh", "Thứ 7, 27/05 • Online", "Tình nguyện", false, "low", 2, false));
        taskList.add(new TaskUiModel(6, TaskUiModel.TYPE_TABLE_ROW, "Nộp đề cương tiểu luận", "Chủ nhật, 28/05, 23:59", "Phương pháp nghiên cứu • RES201", false, "medium", 1, true));
        adapter.updateData(taskList);
        
        TextView tvCount = findViewById(R.id.tv_task_count);
        if (tvCount != null) {
            tvCount.setText(taskList.size() + " công việc hiện tại");
        }
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

    public void fetchTasksFromServer() {
        viewModel.loadSubjects();
    }

    private void processAndDisplayTasks(List<Subject> subjects, List<Task> tasks) {
        taskList.clear();

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

        for (Subject subject : subjects) {
            List<Task> subjectTasks = groupedTasks.get(subject.id);
            if (subjectTasks != null && !subjectTasks.isEmpty()) {
                taskList.add(new TaskUiModel(TaskUiModel.TYPE_GROUP_HEADER, subject.name, "", "", false, "low"));
                for (Task task : subjectTasks) {
                    addTaskToUiList(task);
                }
            }
        }

        if (!orphanTasks.isEmpty()) {
            taskList.add(new TaskUiModel(0, TaskUiModel.TYPE_GROUP_HEADER, "Chưa phân loại", "", "", false, "low", 0, false));
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
                task.note != null ? task.note : "",
                task.isCompleted,
                task.priority != null ? task.priority.toLowerCase() : "low",
                task.subjectId,
                task.isReminderEnabled
        ));
    }
}
