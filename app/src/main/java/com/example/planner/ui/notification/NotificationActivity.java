package com.example.planner.ui.notification;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.planner.R;
import com.example.planner.ui.BaseActivity;

public class NotificationActivity extends BaseActivity {

    private NotificationViewModel viewModel;
    private PriorityTaskAdapter priorityAdapter;
    private PriorityTaskAdapter reminderAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        setupBottomNavigation(R.id.nav_notifications);
        setupRecyclerViews();
        setupViewModel();
    }

    private void setupRecyclerViews() {
        RecyclerView rvPriority = findViewById(R.id.rv_Tasks);
        if (rvPriority != null) {
            rvPriority.setLayoutManager(new LinearLayoutManager(this));
            priorityAdapter = new PriorityTaskAdapter();
            priorityAdapter.setOnTaskStatusChangeListener(task -> {
                if (viewModel != null) viewModel.updateTask(task);
            });
            rvPriority.setAdapter(priorityAdapter);
            rvPriority.setNestedScrollingEnabled(false);
        }

        RecyclerView rvReminders = findViewById(R.id.rv_Reminders);
        if (rvReminders != null) {
            rvReminders.setLayoutManager(new LinearLayoutManager(this));
            reminderAdapter = new PriorityTaskAdapter();
            reminderAdapter.setOnTaskStatusChangeListener(task -> {
                if (viewModel != null) viewModel.updateTask(task);
            });
            rvReminders.setAdapter(reminderAdapter);
            rvReminders.setNestedScrollingEnabled(false);
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(NotificationViewModel.class);
        
        viewModel.getHighPriorityTasks().observe(this, tasks -> {
            if (tasks != null) priorityAdapter.setTasks(tasks);
        });

        viewModel.getReminders().observe(this, tasks -> {
            if (tasks != null) reminderAdapter.setTasks(tasks);
        });

        viewModel.getSubjects().observe(this, subjects -> {
            if (subjects != null) {
                priorityAdapter.setSubjects(subjects);
                reminderAdapter.setSubjects(subjects);
            }
        });

        viewModel.fetchSubjects();
    }
}
