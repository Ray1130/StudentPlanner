package com.example.planner.ui.task;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

    private TextView chipAll;
    private TextView chipCourse;
    private TextView chipExtracurricular;
    private TextView chipUrgent;

    private Handler refreshHandler = new Handler(Looper.getMainLooper());
    private Runnable refreshRunnable;

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

            @Override
            public void onHeaderLongClick(TaskUiModel headerItem) {
                showDeleteGroupDialog(headerItem);
            }

            @Override
            public void onHeaderEditClick(TaskUiModel headerItem) {
                showEditGroupDialog(headerItem);
            }
        });
        rvTasks.setAdapter(adapter);

        findViewById(R.id.fabAddTask).setOnClickListener(v -> showCreateSheet());

        setupFilterChips();

        observeData();
        viewModel.loadSubjects();
        viewModel.loadTasks(); // Kích hoạt đồng bộ
        setupBottomNavigation(R.id.nav_tasks);
        startAutoRefresh();
    }

    private void showDeleteGroupDialog(TaskUiModel headerItem) {
        String title = "Xóa nhóm";
        String message = "Bạn có chắc muốn xóa nhóm \"" + headerItem.getTitle() + "\" và tất cả công việc liên quan?";
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    if (headerItem.getSubjectId() > 0) {
                        // Xóa môn học
                        Subject subject = findSubjectById(viewModel.getAllSubjects().getValue(), headerItem.getSubjectId());
                        if (subject != null) {
                            viewModel.deleteSubject(subject);
                        }
                    } else {
                        // Xóa category ngoại khóa
                        String cleanCategory = headerItem.getTitle();
                        // Header title contains count like "CLB (2)", need to strip it
                        if (cleanCategory.contains(" (")) {
                            cleanCategory = cleanCategory.substring(0, cleanCategory.lastIndexOf(" ("));
                        }
                        viewModel.deleteCategory(cleanCategory);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showEditGroupDialog(TaskUiModel headerItem) {
        String currentName = headerItem.getTitle();
        if (currentName.contains(" (")) {
            currentName = currentName.substring(0, currentName.lastIndexOf(" ("));
        }

        android.widget.EditText input = new android.widget.EditText(this);
        input.setText(currentName);
        input.setSelection(currentName.length());
        
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        android.widget.FrameLayout container = new android.widget.FrameLayout(this);
        android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = padding;
        params.rightMargin = padding;
        input.setLayoutParams(params);
        container.addView(input);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Đổi tên nhóm")
                .setView(container)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        if (headerItem.getSubjectId() > 0) {
                            Subject subject = findSubjectById(viewModel.getAllSubjects().getValue(), headerItem.getSubjectId());
                            if (subject != null) {
                                subject.name = newName;
                                viewModel.updateSubject(subject);
                            }
                        } else {
                            String oldCategory = headerItem.getTitle();
                            if (oldCategory.contains(" (")) {
                                oldCategory = oldCategory.substring(0, oldCategory.lastIndexOf(" ("));
                            }
                            viewModel.updateCategory(oldCategory, newName);
                        }
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void startAutoRefresh() {
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                // Kiểm tra và cập nhật UI nếu có task hết hạn
                List<Subject> subjects = viewModel.getAllSubjects().getValue();
                List<Task> tasks = viewModel.getAllTasks().getValue();
                if (subjects != null && tasks != null) {
                    processAndDisplayTasks(subjects, tasks);
                }
                refreshHandler.postDelayed(this, 5000); // Mỗi 5 giây
            }
        };
        refreshHandler.postDelayed(refreshRunnable, 5000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
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
        viewModel.loadTasks();
    }

    private void processAndDisplayTasks(List<Subject> subjects, List<Task> tasks) {
        taskList.clear();
        int pendingCount = 0;

        // Lọc task: Task Activity chỉ hiện các task chưa hết hạn (isTaskExpired = false)
        List<Task> activeTaskList = new ArrayList<>();
        for (Task task : tasks) {
            if (!isTaskExpired(task)) {
                activeTaskList.add(task);
                if (!task.isCompleted)
                    pendingCount++;
            }
        }

        // Sort: Uncompleted first, then by due date. Completed tasks go to the bottom.
        activeTaskList.sort((t1, t2) -> {
            if (t1.isCompleted != t2.isCompleted) {
                return t1.isCompleted ? 1 : -1;
            }
            return Long.compare(t1.dueDate, t2.dueDate);
        });

        if (currentFilter.equals("ALL")) {
            // Trang "Tất cả" hiển thị danh sách task liền mạch, có đầy đủ nhãn và thông tin môn học
            for (Task task : activeTaskList) {
                Subject subject = findSubjectById(subjects, task.subjectId);
                addTaskToUiList(task, subject, false);
            }

        } else if (currentFilter.equals("COURSE")) {
            Map<Integer, List<Task>> groupedTasks = new HashMap<>();
            List<Task> nonSubjectStudyTasks = new ArrayList<>();

            for (Task task : activeTaskList) {
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
                    // Đếm số task chưa hoàn thành để hiển thị ở tiêu đề
                    int groupPendingCount = 0;
                    for (Task t : subjectTasks) {
                        if (!t.isCompleted) groupPendingCount++;
                    }
                    
                    String headerTitle = subject.name + (groupPendingCount > 0 ? " (" + groupPendingCount + ")" : "");
                    taskList.add(new TaskUiModel(0, TaskUiModel.TYPE_GROUP_HEADER, headerTitle, "", "", false, "low",
                            subject.id, false, "Học tập", "", 0));
                    for (Task task : subjectTasks)
                        addTaskToUiList(task, subject, false);
                }
            }

            if (!nonSubjectStudyTasks.isEmpty()) {
                int groupPendingCount = 0;
                for (Task t : nonSubjectStudyTasks) {
                    if (!t.isCompleted) groupPendingCount++;
                }
                
                String headerTitle = "Tự học / Khác" + (groupPendingCount > 0 ? " (" + groupPendingCount + ")" : "");
                taskList.add(new TaskUiModel(0, TaskUiModel.TYPE_GROUP_HEADER, headerTitle, "", "", false, "low", 0,
                        false, "Học tập", "", 0));
                for (Task task : nonSubjectStudyTasks)
                    addTaskToUiList(task, null, false);
            }

        } else if (currentFilter.equals("EXTRA")) {
            Map<String, List<Task>> groupedTasks = new HashMap<>();

            for (Task task : activeTaskList) {
                if (!"Học tập".equals(task.category) && task.subjectId <= 0) {
                    String cat = (task.category == null || task.category.isEmpty()) ? "Cá nhân" : task.category;
                    if (!groupedTasks.containsKey(cat))
                        groupedTasks.put(cat, new ArrayList<>());
                    groupedTasks.get(cat).add(task);
                }
            }
            for (Map.Entry<String, List<Task>> entry : groupedTasks.entrySet()) {
                List<Task> groupTasks = entry.getValue();
                if (!groupTasks.isEmpty()) {
                    int groupPendingCount = 0;
                    for (Task t : groupTasks) {
                        if (!t.isCompleted) groupPendingCount++;
                    }

                    String headerTitle = entry.getKey() + (groupPendingCount > 0 ? " (" + groupPendingCount + ")" : "");
                    taskList.add(new TaskUiModel(0, TaskUiModel.TYPE_GROUP_HEADER, headerTitle, "", "", false, "low", 0,
                            false, "Ngoại khóa", "", 0));
                    for (Task t : groupTasks)
                        addTaskToUiList(t, null, false);
                }
            }
        } else if (currentFilter.equals("URGENT")) {
            // Show only tasks due in next 7 days and not completed
            long currentTime = System.currentTimeMillis();
            long sevenDaysMs = 7 * 24 * 60 * 60 * 1000L;

            List<Task> urgentTasks = new ArrayList<>();
            for (Task task : activeTaskList) {
                if (!task.isCompleted && task.dueDate > currentTime && task.dueDate <= currentTime + sevenDaysMs) {
                    urgentTasks.add(task);
                }
            }

            // Sort by due date (already sorted above but filtering might change order if we don't re-sort, though here we want only uncompleted)
            urgentTasks.sort((t1, t2) -> Long.compare(t1.dueDate, t2.dueDate));

            if (!urgentTasks.isEmpty()) {
                taskList.add(
                        new TaskUiModel(0, TaskUiModel.TYPE_GROUP_HEADER, "Sắp đến hạn", "", "", false, "low", 0, false,
                                "Sắp đến hạn", "", 0));
                for (Task task : urgentTasks) {
                    Subject subject = findSubjectById(subjects, task.subjectId);
                    addTaskToUiList(task, subject, false);
                }
            }
        }

        adapter.updateData(taskList);

        TextView tvCount = findViewById(R.id.tv_task_count);
        if (tvCount != null) {
            tvCount.setText(getString(R.string.task_count_format, pendingCount));
        }
    }

    private boolean isTaskExpired(Task task) {
        if (task.expiryTimestamp <= 0) {
            return false; // No expiry set
        }
        return System.currentTimeMillis() > task.expiryTimestamp; // Return true if expired
    }

    private void addTaskToUiList(Task task, Subject subject, boolean hideTag) {
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

        TaskUiModel model = new TaskUiModel(
                task.id != null ? task.id : 0,
                TaskUiModel.TYPE_TABLE_ROW,
                task.title,
                DateUtils.timestampToFormattedString(task.dueDate, task.isReminderEnabled),
                task.note,
                task.isCompleted,
                task.priority != null ? task.priority.toLowerCase() : "low",
                task.subjectId,
                task.isReminderEnabled,
                task.category,
                displaySubtitle,
                task.expiryTimestamp);
        model.setHideTag(hideTag);
        taskList.add(model);
    }

    private Subject findSubjectById(List<Subject> subjects, int subjectId) {
        if (subjects == null || subjectId <= 0)
            return null;
        for (Subject subject : subjects) {
            if (subject.id == subjectId)
                return subject;
        }
        return null;
    }

    private void setupFilterChips() {
        chipAll = findViewById(R.id.chipAll);
        chipCourse = findViewById(R.id.chipCourse);
        chipExtracurricular = findViewById(R.id.chipExtracurricular);
        chipUrgent = findViewById(R.id.chipUrgent);

        chipAll.setOnClickListener(v -> setFilter("ALL"));
        chipCourse.setOnClickListener(v -> setFilter("COURSE"));
        chipExtracurricular.setOnClickListener(v -> setFilter("EXTRA"));
        chipUrgent.setOnClickListener(v -> setFilter("URGENT"));
    }

    private void setFilter(String filterType) {
        currentFilter = filterType;
        updateFilterChipUI();

        // Trigger data refresh with new filter
        List<Subject> subjects = viewModel.getAllSubjects().getValue();
        List<Task> tasks = viewModel.getAllTasks().getValue();
        if (subjects != null && tasks != null) {
            processAndDisplayTasks(subjects, tasks);
        }
    }

    private void updateFilterChipUI() {
        // Reset all chips to unselected state
        chipAll.setBackgroundResource(R.drawable.bg_chip_unselected);
        chipAll.setTextColor(getColor(R.color.chip_unselected_text));

        chipCourse.setBackgroundResource(R.drawable.bg_chip_unselected);
        chipCourse.setTextColor(getColor(R.color.chip_unselected_text));

        chipExtracurricular.setBackgroundResource(R.drawable.bg_chip_unselected);
        chipExtracurricular.setTextColor(getColor(R.color.chip_unselected_text));

        chipUrgent.setBackgroundResource(R.drawable.bg_chip_unselected);
        chipUrgent.setTextColor(getColor(R.color.chip_unselected_text));

        // Set selected chip based on current filter
        switch (currentFilter) {
            case "COURSE":
                chipCourse.setBackgroundResource(R.drawable.bg_chip_selected);
                chipCourse.setTextColor(getColor(R.color.chip_selected_text));
                break;
            case "EXTRA":
                chipExtracurricular.setBackgroundResource(R.drawable.bg_chip_selected);
                chipExtracurricular.setTextColor(getColor(R.color.chip_selected_text));
                break;
            case "URGENT":
                chipUrgent.setBackgroundResource(R.drawable.bg_chip_selected);
                chipUrgent.setTextColor(getColor(R.color.chip_selected_text));
                break;
            default: // ALL
                chipAll.setBackgroundResource(R.drawable.bg_chip_selected);
                chipAll.setTextColor(getColor(R.color.chip_selected_text));
                break;
        }
    }
}
