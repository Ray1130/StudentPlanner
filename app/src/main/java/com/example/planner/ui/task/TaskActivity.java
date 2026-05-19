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
        taskList.add(new TaskUiModel(1, TaskUiModel.TYPE_TABLE_ROW, "Bài tập chương 3", "Hôm nay, 17:00", "Kinh tế vĩ mô • KTN201", false, "high", 1, true, "Học tập", ""));
        taskList.add(new TaskUiModel(2, TaskUiModel.TYPE_TABLE_ROW, "Thuyết trình giữa kỳ", "Mai, 10:00", "Marketing căn bản • MAR301", false, "medium", 1, true, "Học tập", ""));
        taskList.add(new TaskUiModel(3, TaskUiModel.TYPE_TABLE_ROW, "Đọc tài liệu tuần 5", "Thứ 5, 25/05", "Tiếng Anh học thuật • ENG102", false, "low", 1, true, "Học tập", ""));
        taskList.add(new TaskUiModel(4, TaskUiModel.TYPE_TABLE_ROW, "Họp CLB Truyền thông", "Thứ 6, 26/05 • 19:30", "Phòng B203", false, "medium", 0, false, "CLB", "CLB Truyền thông"));
        taskList.add(new TaskUiModel(5, TaskUiModel.TYPE_TABLE_ROW, "Chuẩn bị kế hoạch Mùa hè xanh", "Thứ 7, 27/05 • Online", "", false, "low", 0, false, "Tình nguyện", "Tình nguyện"));
        taskList.add(new TaskUiModel(6, TaskUiModel.TYPE_TABLE_ROW, "Nộp đề cương tiểu luận", "Chủ nhật, 28/05, 23:59", "Phương pháp nghiên cứu • RES201", false, "medium", 1, true, "Học tập", ""));
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
        viewModel.syncTasksFromServer();
    }

    private void processAndDisplayTasks(List<Subject> subjects, List<Task> tasks) {
        taskList.clear();

        if (currentFilter.equals("ALL")) {
            // Hiển thị Header "Học tập"
            taskList.add(new TaskUiModel(0, TaskUiModel.TYPE_GROUP_HEADER, "Học tập", "", "", false, "low", 0, false, "Học tập", ""));
            
            // Nhóm Học tập theo môn học
            Map<Integer, List<Task>> courseGroups = new HashMap<>();
            List<Task> otherStudy = new ArrayList<>();
            for (Task task : tasks) {
                if (task.subjectId > 0 || "Học tập".equals(task.category)) {
                    if (task.subjectId > 0) {
                        if (!courseGroups.containsKey(task.subjectId)) courseGroups.put(task.subjectId, new ArrayList<>());
                        courseGroups.get(task.subjectId).add(task);
                    } else {
                        otherStudy.add(task);
                    }
                }
            }
            
            for (Subject s : subjects) {
                List<Task> group = courseGroups.get(s.id);
                if (group != null) {
                    for (Task t : group) addTaskToUiList(t, s);
                }
            }
            for (Task t : otherStudy) addTaskToUiList(t, null);

            // Hiển thị Header "Ngoại khóa"
            taskList.add(new TaskUiModel(0, TaskUiModel.TYPE_GROUP_HEADER, "Ngoại khóa", "", "", false, "low", 0, false, "Ngoại khóa", ""));
            
            // Nhóm Ngoại khóa theo category
            Map<String, List<Task>> extraGroups = new HashMap<>();
            for (Task task : tasks) {
                if (!"Học tập".equals(task.category) && task.subjectId <= 0) {
                    String cat = (task.category == null || task.category.isEmpty()) ? "Cá nhân" : task.category;
                    if (!extraGroups.containsKey(cat)) extraGroups.put(cat, new ArrayList<>());
                    extraGroups.get(cat).add(task);
                }
            }
            for (Map.Entry<String, List<Task>> entry : extraGroups.entrySet()) {
                for (Task t : entry.getValue()) addTaskToUiList(t, null);
            }

        } else if (currentFilter.equals("COURSE")) {
            Map<Integer, List<Task>> groupedTasks = new HashMap<>();
            List<Task> nonSubjectStudyTasks = new ArrayList<>();

            for (Task task : tasks) {
                if (task.subjectId > 0 || "Học tập".equals(task.category)) {
                    if (task.subjectId > 0) {
                        if (!groupedTasks.containsKey(task.subjectId)) groupedTasks.put(task.subjectId, new ArrayList<>());
                        groupedTasks.get(task.subjectId).add(task);
                    } else {
                        nonSubjectStudyTasks.add(task);
                    }
                }
            }

            for (Subject subject : subjects) {
                List<Task> subjectTasks = groupedTasks.get(subject.id);
                if (subjectTasks != null && !subjectTasks.isEmpty()) {
                    taskList.add(new TaskUiModel(0, TaskUiModel.TYPE_GROUP_HEADER, subject.name, "", "", false, "low", subject.id, false, "Học tập", ""));
                    for (Task task : subjectTasks) addTaskToUiList(task, subject);
                }
            }

            if (!nonSubjectStudyTasks.isEmpty()) {
                taskList.add(new TaskUiModel(0, TaskUiModel.TYPE_GROUP_HEADER, "Tự học / Khác", "", "", false, "low", 0, false, "Học tập", ""));
                for (Task task : nonSubjectStudyTasks) addTaskToUiList(task, null);
            }

        } else if (currentFilter.equals("EXTRA")) {
            Map<String, List<Task>> groupedTasks = new HashMap<>();

            for (Task task : tasks) {
                if (!"Học tập".equals(task.category) && task.subjectId <= 0) {
                    String cat = (task.category == null || task.category.isEmpty()) ? "Cá nhân" : task.category;
                    if (!groupedTasks.containsKey(cat)) groupedTasks.put(cat, new ArrayList<>());
                    groupedTasks.get(cat).add(task);
                }
            }

            for (Map.Entry<String, List<Task>> entry : groupedTasks.entrySet()) {
                taskList.add(new TaskUiModel(0, TaskUiModel.TYPE_GROUP_HEADER, entry.getKey(), "", "", false, "low", 0, false, entry.getKey(), ""));
                for (Task task : entry.getValue()) addTaskToUiList(task, null);
            }
        }

        adapter.updateData(taskList);
        TextView tvCount = findViewById(R.id.tv_task_count);
        if (tvCount != null) tvCount.setText(taskList.size() + " công việc");
    }

    private void addTaskToUiList(Task task, Subject subject) {
        String displaySubtitle = "";
        if (subject != null) {
            displaySubtitle = subject.name + (subject.code != null ? " • " + subject.code : "");
        } else {
            // Hiển thị Category (CLB, Tình nguyện, Hiến máu...)
            if (task.category != null && !task.category.isEmpty() && !task.category.equals("Học tập")) {
                displaySubtitle = task.category;
            } else if (task.note != null && !task.note.isEmpty()) {
                displaySubtitle = task.note;
            } else {
                // Mặc định cho các hoạt động ngoại khóa khác là Cá nhân
                displaySubtitle = "Cá nhân";
            }
        }

        taskList.add(new TaskUiModel(
                task.id != null ? task.id : 0,
                TaskUiModel.TYPE_TABLE_ROW,
                task.title,
                DateUtils.timestampToString(task.dueDate),
                task.note,
                task.isCompleted,
                task.priority != null ? task.priority.toLowerCase() : "low",
                task.subjectId,
                task.isReminderEnabled,
                task.category != null ? task.category : "",
                displaySubtitle
        ));
    }
}
