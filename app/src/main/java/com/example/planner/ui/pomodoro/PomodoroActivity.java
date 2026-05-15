package com.example.planner.ui.pomodoro;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.planner.R;
import com.example.planner.ui.BaseActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.Locale;

public class PomodoroActivity extends BaseActivity {

    private TextView tvTimer;
    private CircularProgressIndicator timerProgress;
    private MaterialButton btnAction;
    private ImageView ivSettings;
    
    private TextView btnModePomodoro, btnModeShortBreak, btnModeLongBreak;
    
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = 1500000;
    private long initialTimeInMillis = 1500000;
    private boolean timerRunning = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pomodoro);
        setupBottomNavigation(R.id.nav_pomodoro);

        initViews();
        setupListeners();
        updateCountDownText();
        updateTabFocus(btnModePomodoro);
    }

    private void initViews() {
        tvTimer = findViewById(R.id.tvTimer);
        timerProgress = findViewById(R.id.timerProgress);
        btnAction = findViewById(R.id.btnAction);
        ivSettings = findViewById(R.id.ivSettings);
        
        btnModePomodoro = findViewById(R.id.btnModePomodoro);
        btnModeShortBreak = findViewById(R.id.btnModeShortBreak);
        btnModeLongBreak = findViewById(R.id.btnModeLongBreak);
    }

    private void setupListeners() {
        btnAction.setOnClickListener(v -> {
            if (timerRunning) {
                pauseTimer();
            } else {
                startTimer();
            }
        });

        ivSettings.setOnClickListener(v -> showSettingsDialog());

        btnModePomodoro.setOnClickListener(v -> {
            updateTabFocus(btnModePomodoro);
            resetTimer(25);
        });
        btnModeShortBreak.setOnClickListener(v -> {
            updateTabFocus(btnModeShortBreak);
            resetTimer(5);
        });
        btnModeLongBreak.setOnClickListener(v -> {
            updateTabFocus(btnModeLongBreak);
            resetTimer(15);
        });
    }

    private void updateTabFocus(TextView selectedTab) {
        resetTabStyle(btnModePomodoro);
        resetTabStyle(btnModeShortBreak);
        resetTabStyle(btnModeLongBreak);
        selectedTab.setBackgroundResource(R.drawable.bg_mode_selected);
        selectedTab.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        selectedTab.setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private void resetTabStyle(TextView tab) {
        tab.setBackground(null);
        tab.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        tab.setTypeface(null, android.graphics.Typeface.NORMAL);
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
                updateProgressBar();
            }

            @Override
            public void onFinish() {
                timerRunning = false;
                btnAction.setText("START");
                Toast.makeText(PomodoroActivity.this, "Hoàn thành!", Toast.LENGTH_SHORT).show();
            }
        }.start();

        timerRunning = true;
        btnAction.setText("PAUSE");
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        timerRunning = false;
        btnAction.setText("START");
    }

    private void resetTimer(int minutes) {
        pauseTimer();
        initialTimeInMillis = minutes * 60000L;
        timeLeftInMillis = initialTimeInMillis;
        updateCountDownText();
        timerProgress.setProgress(100);
    }

    private void updateCountDownText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        tvTimer.setText(timeLeftFormatted);
    }

    private void updateProgressBar() {
        int progress = (int) ((timeLeftInMillis * 100) / initialTimeInMillis);
        timerProgress.setProgress(progress, true);
    }

    private void showSettingsDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_pomodoro_settings, null);
        dialog.setContentView(dialogView);

        NumberPicker npFocus = dialogView.findViewById(R.id.npFocusTime);
        NumberPicker npShort = dialogView.findViewById(R.id.npShortBreak);
        NumberPicker npLong = dialogView.findViewById(R.id.npLongBreak);
        NumberPicker npAfter = dialogView.findViewById(R.id.npLongBreakAfter);

        setupNumberPicker(npFocus, 1, 60, 25);
        setupNumberPicker(npShort, 1, 30, 5);
        setupNumberPicker(npLong, 1, 45, 15);
        setupNumberPicker(npAfter, 1, 10, 4);

        dialogView.findViewById(R.id.btnSaveSettings).setOnClickListener(v -> {
            resetTimer(npFocus.getValue());
            dialog.dismiss();
            Toast.makeText(this, "Đã cập nhật thời gian!", Toast.LENGTH_SHORT).show();
        });

        dialogView.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void setupNumberPicker(NumberPicker np, int min, int max, int def) {
        np.setMinValue(min);
        np.setMaxValue(max);
        np.setValue(def);
    }
}
