package com.example.planner.ui.profile;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.planner.ui.BaseActivity;
import com.example.planner.R;

import java.util.Locale;

public class UserProfileActivity extends BaseActivity {

    private UserProfileViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        viewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);

        setupBottomNavigation(R.id.nav_profile);

        setupOverview();
        observeProfileData();

        setupSettingRow(R.id.settingRingtone, R.drawable.ic_music_note, getString(R.string.setting_ringtone), null);
        setupSettingRow(R.id.settingDefaultReminder, R.drawable.ic_alarm, getString(R.string.setting_default_reminder), getString(R.string.setting_default_reminder_val));
        setupSettingRow(R.id.settingPomodoro, R.drawable.ic_timer, getString(R.string.setting_pomodoro), getString(R.string.setting_pomodoro_val));
        setupSettingRow(R.id.settingFocusSound, R.drawable.ic_library_music, getString(R.string.setting_focus_sound), getString(R.string.setting_focus_sound_val));
        setupSettingRow(R.id.settingDailyReminder, R.drawable.ic_notifications, getString(R.string.setting_daily_reminder), null);
        setupSettingRow(R.id.settingLanguage, R.drawable.ic_language, getString(R.string.setting_language), null);
        setupSettingRow(R.id.settingUserGuide, R.drawable.ic_menu_book, getString(R.string.setting_guide), null);

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }
    }

    private void setupOverview() {
        TextView tvUserName = findViewById(R.id.tvUserName);
        SharedPreferences prefs = getSharedPreferences("USER_FILE", MODE_PRIVATE);
        String username = prefs.getString("username", "admin");
        if (tvUserName != null) tvUserName.setText(username);
    }

    private void observeProfileData() {
        TextView tvCompletedCount = findViewById(R.id.tvCompletedTasksCount);
        TextView tvRemainingCount = findViewById(R.id.tvRemainingTasksCount);
        ProgressBar progressBar = findViewById(R.id.pbTaskProgress);
        TextView tvPercentage = findViewById(R.id.tvProgressPercentage);
        LinearLayout barChartContainer = findViewById(R.id.barChartContainer);

        viewModel.getProfileStats().observe(this, stats -> {
            if (stats == null) return;

            if (tvCompletedCount != null) tvCompletedCount.setText(String.valueOf(stats.getTotalCompleted()));
            if (tvRemainingCount != null) tvRemainingCount.setText(String.valueOf(stats.getTotalRemaining()));
            
            int percentage = stats.getTodayPercentage();
            if (progressBar != null) {
                progressBar.setMax(100);
                progressBar.setProgress(percentage);
            }
            if (tvPercentage != null) {
                tvPercentage.setText(String.format(Locale.getDefault(), "%d%%", percentage));
            }

            updateBarChart(barChartContainer, stats.getLast7DaysCompleted(), stats.getLast7DaysTotal(), stats.getLast7DaysLabels());
        });
    }

    private void updateBarChart(LinearLayout container, int[] completedData, int[] totalData, String[] labels) {
        if (container == null || completedData == null || totalData == null || labels == null) return;
        container.removeAllViews();

        int maxVal = 0;
        for (int val : totalData) if (val > maxVal) maxVal = val;
        if (maxVal < 5) maxVal = 5;

        float density = getResources().getDisplayMetrics().density;
        
        for (int i = 0; i < totalData.length; i++) {
            int completed = completedData[i];
            int total = totalData[i];
            String label = labels[i];
            
            LinearLayout columnLayout = new LinearLayout(this);
            columnLayout.setOrientation(LinearLayout.VERTICAL);
            columnLayout.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams colParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
            columnLayout.setLayoutParams(colParams);

            // Số lượng tổng trên đầu bar
            TextView tvCount = new TextView(this);
            tvCount.setText(String.valueOf(total));
            tvCount.setTextSize(10);
            tvCount.setTextColor(Color.GRAY);
            tvCount.setGravity(Gravity.CENTER);
            columnLayout.addView(tvCount);

            // Thanh biểu đồ (Chiều cao dựa trên số lượng hoàn thành)
            View bar = new View(this);
            int heightPx = (int) ((completed * 120 * density) / maxVal); 
            if (heightPx < (int)(4 * density)) heightPx = (int)(4 * density); 

            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams((int)(18 * density), heightPx);
            barParams.setMargins(0, (int)(2 * density), 0, (int)(4 * density));
            bar.setLayoutParams(barParams);
            bar.setBackgroundColor(Color.parseColor("#5E35B1"));
            columnLayout.addView(bar);

            // Ngày ở dưới bar
            TextView tvLabel = new TextView(this);
            tvLabel.setText(label);
            tvLabel.setTextSize(9);
            tvLabel.setTextColor(Color.BLACK);
            tvLabel.setGravity(Gravity.CENTER);
            columnLayout.addView(tvLabel);

            container.addView(columnLayout);
        }
    }

    private void setupSettingRow(int includeId, int iconResId, String title, String value) {
        View settingView = findViewById(includeId);
        if (settingView == null) return;
        ImageView ivSettingIcon = settingView.findViewById(R.id.ivSettingIcon);
        TextView tvSettingTitle = settingView.findViewById(R.id.tvSettingTitle);
        TextView tvSettingValue = settingView.findViewById(R.id.tvSettingValue);
        ImageView ivSettingArrow = settingView.findViewById(R.id.ivSettingArrow);

        if (ivSettingIcon != null) ivSettingIcon.setImageResource(iconResId);
        if (tvSettingTitle != null) tvSettingTitle.setText(title);

        if (tvSettingValue != null) {
            if (value != null && !value.isEmpty()) {
                tvSettingValue.setText(value);
                tvSettingValue.setVisibility(View.VISIBLE);
            } else {
                tvSettingValue.setVisibility(View.GONE);
            }
        }

        if (ivSettingArrow != null) {
            ivSettingArrow.setImageResource(R.drawable.ic_chevron_right_24);
        }

        settingView.setOnClickListener(v -> {
        });
    }
}
