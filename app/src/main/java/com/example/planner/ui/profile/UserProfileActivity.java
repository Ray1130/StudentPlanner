package com.example.planner.ui.profile;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.example.planner.data.local.AppDatabase;
import com.example.planner.ui.BaseActivity;
import com.example.planner.R;
import com.example.planner.utils.SettingsHelper;

import java.util.Calendar;
import java.util.Locale;

public class UserProfileActivity extends BaseActivity {

    private UserProfileViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        viewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);

        setupBottomNavigation(R.id.nav_profile);

        observeProfileData();

        // Setup Auto Delete Spinner
        Spinner spinnerAutoDelete = findViewById(R.id.spinnerAutoDelete);
        if (spinnerAutoDelete != null) {
            String[] autoDeleteOptions = new String[] { "Không tự động xóa", "Sau 1 kỳ học" };
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                    autoDeleteOptions);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerAutoDelete.setAdapter(adapter);

            // Load saved preference
            int savedOption = SettingsHelper.getAutoDeleteOption(this);
            spinnerAutoDelete.setSelection(savedOption);

            // Listen for changes
            spinnerAutoDelete.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    SettingsHelper.setAutoDeleteOption(UserProfileActivity.this, position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Do nothing
                }
            });
        }

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        ImageView btnMonthHistory = findViewById(R.id.btnMonthHistory);
        if (btnMonthHistory != null) {
            btnMonthHistory.setOnClickListener(v -> showMonthlyHistoryDialog());
        }
    }

    private void showMonthlyHistoryDialog() {
        viewModel.getProfileStats().observe(this, stats -> {
            // This is a bit recursive, better to use the latest tasks from the ViewModel or DAO.
            // For simplicity, we'll fetch from ViewModel's list if available.
        });
        
        // Actually, let's just trigger a dialog that observes the data once.
        AppDatabase db = AppDatabase.getDatabase(this);
        db.taskDao().getAllTasks().observe(this, tasks -> {
            if (tasks != null) {
                java.util.List<String> history = viewModel.getMonthlyHistory(tasks);
                if (history.isEmpty()) {
                    Toast.makeText(this, "Chưa có dữ liệu hoàn thành tháng nào.", Toast.LENGTH_SHORT).show();
                    return;
                }
                new AlertDialog.Builder(this)
                        .setTitle("Lịch sử hoàn thành các tháng")
                        .setItems(history.toArray(new String[0]), null)
                        .setPositiveButton("Đóng", null)
                        .show();
            }
        });
    }

    private void observeProfileData() {
        TextView tvFocusTitle = findViewById(R.id.tvFocusTitle);
        TextView tvCompletedCount = findViewById(R.id.tvCompletedTasksCount);
        TextView tvRemainingCount = findViewById(R.id.tvRemainingTasksCount);
        LinearLayout barChartContainer = findViewById(R.id.barChartContainer);

        // Update dynamic monthly title
        if (tvFocusTitle != null) {
            Calendar cal = Calendar.getInstance();
            int month = cal.get(Calendar.MONTH) + 1;
            int year = cal.get(Calendar.YEAR);
            tvFocusTitle.setText(String.format(Locale.getDefault(), "Nhiệm vụ trong tháng %d năm %d", month, year));
        }

        viewModel.getProfileStats().observe(this, stats -> {
            if (stats == null)
                return;

            if (tvCompletedCount != null)
                tvCompletedCount.setText(String.valueOf(stats.getCompletedToday()));
            if (tvRemainingCount != null)
                tvRemainingCount.setText(String.valueOf(stats.getRemainingToday()));

            updateBarChart(barChartContainer, stats.getLast7DaysCompleted(), stats.getLast7DaysTotal(),
                    stats.getLast7DaysLabels());
        });
    }

    private void updateBarChart(LinearLayout container, int[] completedData, int[] totalData, String[] labels) {
        if (container == null || completedData == null || totalData == null || labels == null)
            return;
        container.removeAllViews();

        int maxVal = 0;
        for (int val : totalData)
            if (val > maxVal)
                maxVal = val;
        if (maxVal < 5)
            maxVal = 5;

        float density = getResources().getDisplayMetrics().density;

        for (int i = 0; i < totalData.length; i++) {
            int completed = completedData[i];
            int total = totalData[i];
            String label = labels[i];

            LinearLayout columnLayout = new LinearLayout(this);
            columnLayout.setOrientation(LinearLayout.VERTICAL);
            columnLayout.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams colParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT,
                    1f);
            columnLayout.setLayoutParams(colParams);

            // Số lượng tổng trên đầu bar
            TextView tvCount = new TextView(this);
            tvCount.setText(String.valueOf(total));
            tvCount.setTextSize(10);
            tvCount.setTextColor(getColor(R.color.text_secondary));
            tvCount.setGravity(Gravity.CENTER);
            columnLayout.addView(tvCount);

            // Thanh biểu đồ (Chiều cao dựa trên số lượng hoàn thành)
            View bar = new View(this);
            int heightPx = (int) ((completed * 120 * density) / maxVal);
            if (heightPx < (int) (4 * density))
                heightPx = (int) (4 * density);

            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams((int) (18 * density), heightPx);
            barParams.setMargins(0, (int) (2 * density), 0, (int) (4 * density));
            bar.setLayoutParams(barParams);
            bar.setBackgroundColor(getColor(R.color.user_profile_primary));
            columnLayout.addView(bar);

            // Ngày ở dưới bar
            TextView tvLabel = new TextView(this);
            tvLabel.setText(label);
            tvLabel.setTextSize(8);
            tvLabel.setTextColor(getColor(R.color.text_primary));
            tvLabel.setGravity(Gravity.CENTER);
            tvLabel.setLineSpacing(0, 0.8f);
            columnLayout.addView(tvLabel);

            container.addView(columnLayout);
        }
    }
}
