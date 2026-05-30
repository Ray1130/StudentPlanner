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
            public void onAddNewTask() {
                showCreateSheet();
            }

            @Override
            public void onAddNewGroup() {
                showCreateSheet();
            }

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

        observeData();
        viewModel.loadSubjects();
        viewModel.loadTasks(); // Kích hoạt đồng bộ
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
        viewModel.loadTasks();
    }

    private void processAndDisplayTasks(List<Subject> subjects, List<Task> tasks) {
        taskList.clear();

        if (currentFilter.equals("ALL")) {
            // Hiển thị Header "Học tập"
            taskList.add(new TaskUiModel(0, TaskUiModel.TYPE_GROUP_HEADER, "Học tập", "", "", false, "low", 0, false,
                    "Học tập", ""));

            // Nhóm Học tập theo môn học
            Map<Integer, List<Task>> courseGroups = new HashMap<>();
            List<Task> otherStudy = new ArrayList<>();
            for (Task task : tasks) {
                if (task.subjectId > 0 || "Học tập".equals(task.category)) {
                    if (task.subjectId > 0) {
                        if (!courseGroups.containsKey(task.subjectId))
                            courseGroups.put(task.subjectId, new ArrayList<>());
                        courseGroups.get(task.subjectId).add(task);
                    } else {
                        otherStudy.add(task);
                    }
                }
            }

        int pendingCount = 0;
        for (Task task : tasks) {
            if (!task.isCompleted) pendingCount++;

            boolean found = false;
            for (Subject s : subjects) {
                List<Task> group = courseGroups.get(s.id);
                if (group != null) {
                    for (Task t : group)
                        addTaskToUiList(t, s);
                }
            }
            for (Task t : otherStudy)
                addTaskToUiList(t, null);

            // Hiển thị Header "Ngoại khóa"
            taskList.add(new TaskUiModel(0, TaskUiModel.TYPE_GROUP_HEADER, "Ngoại khóa", "", "", false, "low", 0, false,
                    "Ngoại khóa", ""));

            // Nhóm Ngoại khóa theo category
            Map<String, List<Task>> extraGroups = new HashMap<>();
            for (Task task : tasks) {
                if (!"Học tập".equals(task.category) && task.subjectId <= 0) {
                    String cat = (task.category == null || task.category.isEmpty()) ? "Cá nhân" : task.category;
                    if (!extraGroups.containsKey(cat))
                        extraGroups.put(cat, new ArrayList<>());
                    extraGroups.get(cat).add(task);
                }
            }
            for (Map.Entry<String, List<Task>> entry : extraGroups.entrySet()) {
                for (Task t : entry.getValue())
                    addTaskToUiList(t, null);
            }

        } else if (currentFilter.equals("COURSE")) {
            Map<Integer, List<Task>> groupedTasks = new HashMap<>();
            List<Task> nonSubjectStudyTasks = new ArrayList<>();

            for (Task task : tasks) {
                if (task.subjectId > 0 || "Học tập".equals(task.category)) {
                    if (task.subjectId > 0) {
                        if (!groupedTasks.containsKey(task.subjectId))
                            groupedTasks.put(task.subjectId, new ArrayList<>());
                        groupedTasks.get(task.subjectId).add(task);
                    } else {
                        nonSubjectStudyTasks.add(task);
                    }
                }
            }

            for (Subject subject : subjects) {
                List<Task> subjectTasks = groupedTasks.get(subject.id);
                if (subjectTasks != null && !subjectTasks.isEmpty()) {
                    taskList.add(new TaskUiModel(0, TaskUiModel.TYPE_GROUP_HEADER, subject.name, "", "", false, "low",
                            subject.id, false, "Học tập", ""));
                    for (Task task : subjectTasks)
                        addTaskToUiList(task, subject);
                }
            }

            if (!nonSubjectStudyTasks.isEmpty()) {
                taskList.add(new TaskUiModel(0, TaskUiModel.TYPE_GROUP_HEADER, "Tự học / Khác", "", "", false, "low", 0,
                        false, "Học tập", ""));
                for (Task task : nonSubjectStudyTasks)
                    addTaskToUiList(task, null);
            }

        } else if (currentFilter.equals("EXTRA")) {
            Map<String, List<Task>> groupedTasks = new HashMap<>();

            for (Task task : tasks) {
                if (!"Học tập".equals(task.category) && task.subjectId <= 0) {
                    String cat = (task.category == null || task.category.isEmpty()) ? "Cá nhân" : task.category;
                    if (!groupedTasks.containsKey(cat))
                        groupedTasks.put(cat, new ArrayList<>());
                    groupedTasks.get(cat).add(task);
                }
            }

        if (!orphanTasks.isEmpty()) {
            taskList.add(new TaskUiModel(0, TaskUiModel.TYPE_GROUP_HEADER, "Chưa phân loại", "", "", false, "low", 0, false, 0));
            for (Task task : orphanTasks) {
                addTaskToUiList(task);
            }
        }

        adapter.updateData(taskList);
        TextView tvCount = findViewById(R.id.tv_task_count);
        if (tvCount != null) {
            tvCount.setText(getString(R.string.task_count_format, pendingCount));
        }
    }

    private void addTaskToUiList(Task task, Subject subject) {
        String displaySubtitle = "";
        if (subject != null) {
            displaySubtitle = subject.name + (subject.code != null ? " • " + subject.code : "");
        } else {
            // Hiển thị Category (CLB, Tình nguyện, Hiến máu...)
            if (task.category != null && !task.category.isEmpty()) {
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
                task.expiryTimestamp
        ));
    }
}
