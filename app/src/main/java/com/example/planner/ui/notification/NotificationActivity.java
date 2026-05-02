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
    private PriorityTaskAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        setupBottomNavigation(R.id.nav_notifications);
        setupRecyclerView();
        setupViewModel();
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.rv_Tasks);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new PriorityTaskAdapter();
            adapter.setOnTaskStatusChangeListener(task -> {
                if (viewModel != null) {
                    viewModel.updateTask(task);
                }
            });
            recyclerView.setAdapter(adapter);
            recyclerView.setNestedScrollingEnabled(false);
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(NotificationViewModel.class);
        viewModel.getHighPriorityTasks().observe(this, tasks -> {
            android.util.Log.d("NotificationActivity", "Received tasks: " + (tasks != null ? tasks.size() : "null"));
            if (tasks != null) {
                adapter.setTasks(tasks);
            }
        });
        viewModel.getSubjects().observe(this, subjects -> {
            if (subjects != null) {
                adapter.setSubjects(subjects);
            }
        });
        viewModel.fetchSubjects();
    }
}
