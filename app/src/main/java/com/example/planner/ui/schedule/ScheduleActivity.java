package com.example.planner.ui.schedule;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.planner.R;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.ViewModelProvider;
import com.example.planner.ui.main.MainTaskItem;
import com.example.planner.ui.main.MainTaskAdapter;
import com.example.planner.ui.BaseActivity;

public class ScheduleActivity extends BaseActivity {

    private TextView tvCurrentMonth;
    private RecyclerView rvCalendar;
    private ImageButton btnPrevMonth, btnNextMonth, btnAddTask;
    private CalendarAdapter adapter;
    private TextView tvTaskCount;
    private LocalDate selectedDate;
    private MainTaskAdapter taskAdapter;
    private RecyclerView rvTasks;
    private ScheduleViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        viewModel = new ViewModelProvider(this).get(ScheduleViewModel.class);
        selectedDate = LocalDate.now();

        initWidgets();
        initCalendarRecyclerView(); // Khởi tạo cố định LayoutManager và Adapter
        setupObservers();

        setMonthView();

        btnPrevMonth.setOnClickListener(v -> {
            selectedDate = selectedDate.minusMonths(1);
            setMonthView();
        });

        btnNextMonth.setOnClickListener(v -> {
            selectedDate = selectedDate.plusMonths(1);
            setMonthView();
        });

        btnAddTask.setOnClickListener(v -> {
            com.example.planner.ui.task.TaskCreateSheetFragment sheet = new com.example.planner.ui.task.TaskCreateSheetFragment();
            Bundle bundle = new Bundle();
            long timestamp = selectedDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
            bundle.putLong("default_date", timestamp);
            sheet.setArguments(bundle);
            sheet.show(getSupportFragmentManager(), "TaskCreateSheet");
        });

        setupBottomNavigation(R.id.nav_calendar);
    }

    private void initCalendarRecyclerView() {
        rvCalendar.setLayoutManager(new GridLayoutManager(this, 7));
        adapter = new CalendarAdapter(new ArrayList<>(), selectedDate, (position, date) -> {
            if (date != null) {
                selectedDate = date;
                viewModel.setSelectedDate(date);
                adapter.updateSelectedDate(date);
                // Nếu click vào ngày của tháng khác thì chuyển tháng
                if (date.getMonthValue() != selectedDate.getMonthValue()) {
                    setMonthView();
                }
            }
        });
        rvCalendar.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getTasksForSelectedDate().observe(this, tasks -> {
            if (taskAdapter == null) {
                taskAdapter = new MainTaskAdapter();
                taskAdapter.setOnTaskStatusChangeListener(new MainTaskAdapter.OnTaskStatusChangeListener() {
                    @Override
                    public void onStatusChanged(int taskId, boolean isCompleted) {
                        // Gọi repo hoặc viewmodel để update nhanh
                        com.example.planner.data.repository.TaskRepository repo = new com.example.planner.data.repository.TaskRepository(getApplication());
                        repo.toggleTaskCompletion(taskId);
                    }

                    @Override
                    public void onTaskClick(int taskId) {
                        showEditSheet(taskId);
                    }
                });
                rvTasks.setLayoutManager(new LinearLayoutManager(this));
                rvTasks.setAdapter(taskAdapter);
            }
            taskAdapter.submitList(tasks);
            updateTaskCount(tasks.size());
        });

        viewModel.getMonthTaskData().observe(this, data -> {
            if (adapter != null) {
                adapter.setTaskData(data);
            }
        });
    }

    private void showEditSheet(int taskId) {
        // Fetch task details to create TaskUiModel
        com.example.planner.data.local.AppDatabase db = com.example.planner.data.local.AppDatabase.getDatabase(this);
        com.example.planner.data.local.AppDatabase.databaseWriteExecutor.execute(() -> {
            com.example.planner.data.model.Task task = db.taskDao().getTaskByIdSync(taskId);
            if (task != null) {
                runOnUiThread(() -> {
                    com.example.planner.ui.task.TaskUiModel uiModel = new com.example.planner.ui.task.TaskUiModel(
                            task.id,
                            com.example.planner.ui.task.TaskUiModel.TYPE_TABLE_ROW,
                            task.title,
                            com.example.planner.utils.DateUtils.timestampToFormattedString(task.dueDate, task.isReminderEnabled),
                            task.note,
                            task.isCompleted,
                            task.priority != null ? task.priority.toLowerCase() : "low",
                            task.subjectId,
                            task.isReminderEnabled,
                            task.category,
                            ""
                    );
                    com.example.planner.ui.task.TaskCreateSheetFragment sheet = com.example.planner.ui.task.TaskCreateSheetFragment.newInstance(uiModel);
                    sheet.show(getSupportFragmentManager(), "TaskEditSheet");
                });
            }
        });
    }

    private void initWidgets() {
        rvCalendar = findViewById(R.id.rvCalendar);
        tvCurrentMonth = findViewById(R.id.tvCurrentMonth);
        rvTasks = findViewById(R.id.rv_tasks);
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        tvTaskCount = findViewById(R.id.tvTaskCount);
        btnAddTask = findViewById(R.id.btnAddTask);
    }

    private void updateTaskCount(int count) {
        if (tvTaskCount != null) {
            tvTaskCount.setText(count + " công việc hiện tại");
        }
    }

    private void setMonthView() {
        tvCurrentMonth.setText(monthYearFromDate(selectedDate));
        ArrayList<LocalDate> daysInMonth = daysInMonthList(selectedDate);
        
        viewModel.loadTaskCountsForMonth(daysInMonth);

        if (adapter != null) {
            adapter.updateDays(daysInMonth, selectedDate);
        }
    }

    private String monthYearFromDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        return date.format(formatter);
    }

    private ArrayList<LocalDate> daysInMonthList(LocalDate date) {
        ArrayList<LocalDate> days = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);
        int daysInMonth = yearMonth.lengthOfMonth();

        LocalDate firstOfMonth = date.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        int daysToBefore = dayOfWeek - 1;
        LocalDate prevMonth = date.minusMonths(1);
        int daysInPrevMonth = YearMonth.from(prevMonth).lengthOfMonth();

        for (int i = daysToBefore - 1; i >= 0; i--) {
            days.add(prevMonth.withDayOfMonth(daysInPrevMonth - i));
        }

        // Thêm ngày của tháng hiện tại
        for (int i = 1; i <= daysInMonth; i++) {
            days.add(date.withDayOfMonth(i));
        }

        int nextMonthDays = 35 - days.size();
        for (int i = 1; i <= nextMonthDays; i++) {
            days.add(date.plusMonths(1).withDayOfMonth(i));
        }

        return days;
    }
}