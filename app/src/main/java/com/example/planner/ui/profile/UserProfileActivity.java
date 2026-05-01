package com.example.planner.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import com.example.planner.ui.BaseActivity;
import com.example.planner.R;

public class UserProfileActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        setupBottomNavigation(R.id.nav_profile);

        setupOverview();
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
        TextView tvCompletedCount = findViewById(R.id.tvCompletedTasksCount);
        TextView tvRemainingCount = findViewById(R.id.tvRemainingTasksCount);

        if (tvUserName != null) tvUserName.setText(R.string.user_name_default);
        if (tvCompletedCount != null) tvCompletedCount.setText("5");
        if (tvRemainingCount != null) tvRemainingCount.setText("2");
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
