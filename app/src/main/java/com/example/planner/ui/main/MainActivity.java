package com.example.planner.ui.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planner.R;
import com.example.planner.ui.BaseActivity;
import com.example.planner.ui.task.TaskActivity;
import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private MainViewModel viewModel;
    private MainTaskAdapter todayAdapter;
    private MainTaskAdapter upcomingAdapter;

    private TextView tvGreeting;
    private TextView tvDate;
    private TextView tvTodayCount;
    private TextView tvOverdueCount;
    private TextView tvPriorityCount;
    private TextView tvCompletedCount;
    private ImageView ivMenu;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawerLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupToolbarDrawer();
        setupRecyclerView();
        setupViewModel();
        bindStaticData();
        setupWeeklyOverview();
        observeUi();
        setupBottomNavigation(R.id.nav_home);

        NavigationView navigationView = findViewById(R.id.navigationView);

        navigationView.setNavigationItemSelectedListener(this);
    }

    private void initViews() {
        tvGreeting = findViewById(R.id.tvGreeting);
        tvDate = findViewById(R.id.tvDate);
        tvTodayCount = findViewById(R.id.tvTodayCount);
        tvOverdueCount = findViewById(R.id.tvOverdueCount);
        tvPriorityCount = findViewById(R.id.tvPriorityCount);
        tvCompletedCount = findViewById(R.id.tvCompletedCount);
        ivMenu = findViewById(R.id.ivMenu);
        drawerLayout = findViewById(R.id.drawerLayout);

        findViewById(R.id.btn_nav_tasks).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, com.example.planner.ui.task.TaskActivity.class));
        });

        findViewById(R.id.btn_nav_calendar).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, com.example.planner.ui.schedule.ScheduleActivity.class));
        });

        findViewById(R.id.btn_nav_reminders).setOnClickListener(v -> {
            startActivity(
                    new Intent(MainActivity.this, com.example.planner.ui.notification.NotificationActivity.class));
        });

        findViewById(R.id.btn_nav_pomodoro).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, com.example.planner.ui.pomodoro.PomodoroActivity.class));
        });

        findViewById(R.id.tvViewAllToday).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, com.example.planner.ui.task.TaskActivity.class));
        });

        findViewById(R.id.tvViewAllUpcoming).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, com.example.planner.ui.task.TaskActivity.class));
        });
    }

    private void setupToolbarDrawer() {
        if (ivMenu != null && drawerLayout != null) {
            ivMenu.setOnClickListener(v -> {
                drawerLayout.openDrawer(GravityCompat.START);
            });
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.open_drawer,
                R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void setupRecyclerView() {
        RecyclerView rvTodayTasks = findViewById(R.id.rvTodayTasks);
        RecyclerView rvUpcomingTasks = findViewById(R.id.rvUpcomingTasks);

        MainTaskAdapter.OnTaskStatusChangeListener listener = new MainTaskAdapter.OnTaskStatusChangeListener() {
            @Override
            public void onStatusChanged(int taskId, boolean isCompleted) {
                viewModel.updateTaskStatus(taskId, isCompleted);
            }

            @Override
            public void onTaskClick(int taskId) {
                // Trang chủ không cho phép xem chi tiết/sửa/xóa task
                // Chức năng này chỉ dành cho màn hình Task và Schedule
            }
        };

        if (rvTodayTasks != null) {
            rvTodayTasks.setLayoutManager(new LinearLayoutManager(this));
            todayAdapter = new MainTaskAdapter();
            todayAdapter.setOnTaskStatusChangeListener(listener);
            rvTodayTasks.setAdapter(todayAdapter);
        }

        if (rvUpcomingTasks != null) {
            rvUpcomingTasks.setLayoutManager(new LinearLayoutManager(this));
            upcomingAdapter = new MainTaskAdapter();
            upcomingAdapter.setOnTaskStatusChangeListener(listener);
            rvUpcomingTasks.setAdapter(upcomingAdapter);
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
    }

    private void bindStaticData() {
        String dateText = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi", "VN"))
                .format(new Date());
        if (tvDate != null)
            tvDate.setText(capitalizeFirstLetter(dateText));
        if (tvGreeting != null)
            tvGreeting.setText(getString(R.string.welcome_back));
    }

    private void setupWeeklyOverview() {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        Calendar today = Calendar.getInstance();
        int currentDayOfMonth = today.get(Calendar.DAY_OF_MONTH);
        int currentMonth = today.get(Calendar.MONTH);
        int currentYear = today.get(Calendar.YEAR);

        int[] dateIds = { R.id.tvDay1Date, R.id.tvDay2Date, R.id.tvDay3Date, R.id.tvDay4Date, R.id.tvDay5Date,
                R.id.tvDay6Date, R.id.tvDay7Date };
        int[] cardIds = { R.id.cardDay1, R.id.cardDay2, R.id.cardDay3, R.id.cardDay4, R.id.cardDay5, R.id.cardDay6,
                R.id.cardDay7 };

        for (int i = 0; i < 7; i++) {
            TextView tvDateValue = findViewById(dateIds[i]);
            com.google.android.material.card.MaterialCardView card = findViewById(cardIds[i]);

            if (tvDateValue != null) {
                tvDateValue.setText(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
            }

            if (card != null) {
                boolean isToday = calendar.get(Calendar.DAY_OF_MONTH) == currentDayOfMonth &&
                        calendar.get(Calendar.MONTH) == currentMonth &&
                        calendar.get(Calendar.YEAR) == currentYear;

                if (isToday) {
                    card.setStrokeWidth(3);
                    card.setStrokeColor(getColor(R.color.pastel_indigo));
                    card.setCardBackgroundColor(getColor(R.color.bottom_nav_selected_bg));
                } else {
                    card.setStrokeWidth(2);
                    card.setStrokeColor(getColor(R.color.card_stroke));
                    card.setCardBackgroundColor(getColor(R.color.card_bg_default));
                }
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private void observeUi() {
        viewModel.getDashboardUiState().observe(this, state -> {
            if (state == null)
                return;

            if (tvTodayCount != null)
                tvTodayCount.setText(String.valueOf(state.getTodayCount()));
            if (tvOverdueCount != null)
                tvOverdueCount.setText(String.valueOf(state.getOverdueCount()));
            if (tvPriorityCount != null)
                tvPriorityCount.setText(String.valueOf(state.getHighPriorityCount()));
            if (tvCompletedCount != null)
                tvCompletedCount.setText(String.valueOf(state.getCompletedCount()));

            if (todayAdapter != null)
                todayAdapter.submitList(state.getTodayTasks());
            if (upcomingAdapter != null)
                upcomingAdapter.submitList(state.getUpcomingTasks());
        });
    }

    private void showEditSheet(int taskId) {
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

    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty())
            return "";
        return input.substring(0, 1).toUpperCase(new Locale("vi", "VN")) + input.substring(1);
    }

    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_logout) {
            SharedPreferences preferences = getSharedPreferences("USER_FILE", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        } else if (id == R.id.nav_home) {
            drawerLayout.closeDrawers();
            return true;
        } else if (id == R.id.nav_calendar) {
            startActivity(new Intent(MainActivity.this, com.example.planner.ui.schedule.ScheduleActivity.class));
            drawerLayout.closeDrawers();
            return true;
        } else if (id == R.id.nav_tasks) {
            startActivity(new Intent(MainActivity.this, com.example.planner.ui.task.TaskActivity.class));
            drawerLayout.closeDrawers();
            return true;
        } else if (id == R.id.nav_pomodoro) {
            startActivity(new Intent(MainActivity.this, com.example.planner.ui.pomodoro.PomodoroActivity.class));
            drawerLayout.closeDrawers();
            return true;
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(MainActivity.this, com.example.planner.ui.profile.UserProfileActivity.class));
            drawerLayout.closeDrawers();
            return true;
        }
        return false;
    }
}
