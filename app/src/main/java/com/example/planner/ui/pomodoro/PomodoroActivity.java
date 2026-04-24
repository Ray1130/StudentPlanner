package com.example.planner.ui.pomodoro;

import android.os.Bundle;
import androidx.annotation.Nullable;
import com.example.planner.R;
import com.example.planner.ui.BaseActivity;

public class PomodoroActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pomodoro);
        setupBottomNavigation(R.id.nav_pomodoro);
    }
}
