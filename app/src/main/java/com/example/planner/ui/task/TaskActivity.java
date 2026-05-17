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
    private String currentFilter = "ALL"; // ALL, COURSE, EXTRA

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
                viewModel.toggleTaskCompletion(task.getId());
            }

            @Override
            public void onTaskLongClick(TaskUiModel task) {
                showEditSheet(task);
            }
        });
        rvTasks.setAdapter(adapter);

        findViewById(R.id.fabAddTask).setOnClickListener(v -> showCreateSheet());
        
        setupFilters();
        observeData();
        viewModel.loadSubjects();
        setupBottomNavigation(R.id.nav_tasks);
    }

    private void setupFilters() {
        TextView chipAll = findViewById(R.id.chipAll);
        TextView chipCourse = findViewById(R.id.chipCourse);
        TextView chipExtracurricular = findViewById(R.id.chipExtracurricular);

        chipAll.setOnClickListener(v -> {
            currentFilter = "ALL";
            updateFilterUi();
            refreshTaskDisplay();
        });

        chipCourse.setOnClickListener(v -> {
            currentFilter = "COURSE";
            updateFilterUi();
            refreshTaskDisplay();
        });

        chipExtracurricular.setOnClickListener(v -> {
            currentFilter = "EXTRA";
            updateFilterUi();
            refreshTaskDisplay();
        });
    }

    private void updateFilterUi() {
        TextView chipAll = findViewById(R.id.chipAll);
        TextView chipCourse = findViewById(R.id.chipCourse);
        TextView chipExtracurricular = findViewById(R.id.chipExtracurricular);

        chipAll.setBackgroundResource(currentFilter.equals("ALL") ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        chipAll.setTextColor(getColor(currentFilter.equals("ALL") ? R.color.white : R.color.text_secondary));

        chipCourse.setBackgroundResource(currentFilter.equals("COURSE") ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        chipCourse.setTextColor(getColor(currentFilter.equals("COURSE") ? R.color.white : R.color.text_secondary));

        chipExtracurricular.setBackgroundResource(currentFilter.equals("EXTRA") ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        chipExtracurricular.setTextColor(getColor(currentFilter.equals("EXTRA") ? R.color.white : R.color.text_secondary));
    }

    private void refreshTaskDisplay() {
        List<Subject> subjects = viewModel.getAllSubjects().getValue();
        List<Task> tasks = viewModel.getAllTasks().getValue();
        if (tasks != null) {
            processAndDisplayTasks(subjects != null ? subjects : new ArrayList<>(), tasks);
        }
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

        if (currentFilter.equals("ALL")) {
            for (Task task : tasks) {
                Subject s = null;
                for (Subject sub : subjects) {
                    if (sub.id != null && sub.id.equals(task.subjectId)) {
                        s = sub;
                        break;
                    }
                }
                addTaskToUiList(task, s);
            }
        } else if (currentFilter.equals("COURSE")) {
            Map<Integer, List<Task>> groupedTasks = new HashMap<>();
            Map<Integer, Subject> subjectMap = new HashMap<>();
            for (Subject s : subjects) {
                if (s.id != null) subjectMap.put(s.id, s);
            }

            for (Task task : tasks) {
                if (task.subjectId > 0) {
                    List<Task> group = groupedTasks.get(task.subjectId);
                    if (group == null) {
                        group = new ArrayList<>();
                        groupedTasks.put(task.subjectId, group);
                    }
                    group.add(task);
                }
            }

            for (Subject subject : subjects) {
                List<Task> subjectTasks = groupedTasks.get(subject.id);
                if (subjectTasks != null && !subjectTasks.isEmpty()) {
                    taskList.add(new TaskUiModel(0, TaskUiModel.TYPE_GROUP_HEADER, subject.name, "", "", false, "low", subject.id, false));
                    for (Task task : subjectTasks) {
                        addTaskToUiList(task, subject);
                    }
                }
            }
        } else if (currentFilter.equals("EXTRA")) {
            for (Task task : tasks) {
                if (task.subjectId <= 0) {
                    addTaskToUiList(task, null);
                }
            }
        }

        adapter.updateData(taskList);

        TextView tvCount = findViewById(R.id.tv_task_count);
        if (tvCount != null) {
            tvCount.setText(taskList.size() + " công việc hiện tại");
        }
    }

    private void addTaskToUiList(Task task, Subject subject) {
        String subtitle = "";
        if (subject != null) {
            subtitle = subject.name + (subject.code != null ? " • " + subject.code : "");
        } else if (task.category != null) {
            subtitle = task.category;
        }

        taskList.add(new TaskUiModel(
                task.id != null ? task.id : 0,
                TaskUiModel.TYPE_TABLE_ROW,
                task.title,
                DateUtils.timestampToString(task.dueDate),
                subtitle,
                task.isCompleted,
                task.priority != null ? task.priority.toLowerCase() : "low",
                task.subjectId,
                task.isReminderEnabled
        ));
    }
}
