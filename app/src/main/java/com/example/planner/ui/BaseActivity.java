package com.example.planner.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.planner.R;
import com.example.planner.ui.main.MainActivity;
import com.example.planner.ui.pomodoro.PomodoroActivity;
import com.example.planner.ui.profile.UserProfileActivity;
import com.example.planner.ui.task.TaskActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.planner.ui.schedule.ScheduleActivity;

public abstract class BaseActivity extends AppCompatActivity {

    protected BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setupBottomNavigation(int selectedItemId) {
        bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(selectedItemId);
            bottomNavigation.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == selectedItemId) {
                    return true;
                }

                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_tasks) {
                    startActivity(new Intent(this, TaskActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_calendar) {
                    startActivity(new Intent(this, ScheduleActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_pomodoro) {
                    startActivity(new Intent(this, PomodoroActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(this, UserProfileActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_notifications) {
                    startActivity(new Intent(this, com.example.planner.ui.notification.NotificationActivity.class));
                    finish();
                    return true;
                }
                return false;
            });
        }
    }
}
