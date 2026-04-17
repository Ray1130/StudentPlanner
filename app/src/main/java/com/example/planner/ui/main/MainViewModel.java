package com.example.planner.ui.main;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.planner.data.ApiService;
import com.example.planner.ui.task.TaskUiModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainViewModel extends ViewModel {

    private final MutableLiveData<DashboardUiState> dashboardUiState = new MutableLiveData<>();
    private final ApiService apiService;
    private final SimpleDateFormat apiDateFormat = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi", "VN"));

    public MainViewModel() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
        refreshData();
    }

    public LiveData<DashboardUiState> getDashboardUiState() {
        return dashboardUiState;
    }

    public void refreshData() {
        apiService.getAllTasks().enqueue(new Callback<List<TaskUiModel>>() {
            @Override
            public void onResponse(Call<List<TaskUiModel>> call, Response<List<TaskUiModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("MainViewModel", "Nhận được " + response.body().size() + " tasks từ server");
                    processTasks(response.body());
                } else {
                    Log.e("MainViewModel", "Lỗi phản hồi API: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<TaskUiModel>> call, Throwable t) {
                Log.e("MainViewModel", "Lỗi kết nối API: " + t.getMessage());
                dashboardUiState.postValue(new DashboardUiState(0, 0, 0, 0, new ArrayList<>(), new ArrayList<>()));
            }
        });
    }

    private void processTasks(List<TaskUiModel> tasks) {
        List<MainTaskItem> todayTasks = new ArrayList<>();
        List<MainTaskItem> upcomingTasks = new ArrayList<>();

        int todayCount = 0;
        int overdueCount = 0;
        int completedCount = 0;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfToday = cal.getTimeInMillis();
        long endOfToday = startOfToday + (24 * 60 * 60 * 1000);

        for (TaskUiModel task : tasks) {
            // Log thông tin task để debug
            Log.d("MainViewModel", "Processing task: " + task.getTitle() + " | Deadline: " + task.getDeadline());

            if (task.isChecked()) {
                completedCount++;
            }

            long dueDate = 0;
            if (task.getDeadline() != null && !task.getDeadline().isEmpty()) {
                try {
                    Date date = apiDateFormat.parse(task.getDeadline());
                    if (date != null) dueDate = date.getTime();
                } catch (ParseException e) {
                    // Thử parse format khác nếu format chuẩn bị lỗi (vd: yyyy-MM-dd)
                    try {
                        SimpleDateFormat altFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                        Date date = altFormat.parse(task.getDeadline());
                        if (date != null) dueDate = date.getTime();
                    } catch (ParseException e2) {
                        Log.e("MainViewModel", "Không thể parse ngày: " + task.getDeadline());
                    }
                }
            }

            boolean isToday = dueDate >= startOfToday && dueDate < endOfToday;
            boolean isOverdue = dueDate < startOfToday && dueDate > 0;

            int priority = MainTaskItem.PRIORITY_MEDIUM;
            if (isOverdue) {
                overdueCount++;
                priority = MainTaskItem.PRIORITY_HIGH;
            }
            if (isToday) todayCount++;

            String meta = (task.getNote() != null ? task.getNote() : "");
            if (task.getDeadline() != null) {
                meta += (meta.isEmpty() ? "" : " • ") + task.getDeadline();
            }
            if (isOverdue) meta += " • Quá hạn";

            MainTaskItem item = new MainTaskItem(
                    task.getTitle() != null ? task.getTitle() : "Không tiêu đề",
                    meta,
                    priority,
                    task.isChecked()
            );

            if (isToday || isOverdue) {
                todayTasks.add(item);
            } else {
                upcomingTasks.add(item);
            }
        }

        dashboardUiState.postValue(new DashboardUiState(
                todayCount,
                overdueCount,
                0,
                completedCount,
                todayTasks,
                upcomingTasks
        ));
    }

    public static class DashboardUiState {
        private final int todayCount;
        private final int overdueCount;
        private final int highPriorityCount;
        private final int completedCount;
        private final List<MainTaskItem> todayTasks;
        private final List<MainTaskItem> upcomingTasks;

        public DashboardUiState(int todayCount, int overdueCount, int highPriorityCount,
                                int completedCount, List<MainTaskItem> todayTasks,
                                List<MainTaskItem> upcomingTasks) {
            this.todayCount = todayCount;
            this.overdueCount = overdueCount;
            this.highPriorityCount = highPriorityCount;
            this.completedCount = completedCount;
            this.todayTasks = todayTasks;
            this.upcomingTasks = upcomingTasks;
        }

        public int getTodayCount() { return todayCount; }
        public int getOverdueCount() { return overdueCount; }
        public int getHighPriorityCount() { return highPriorityCount; }
        public int getCompletedCount() { return completedCount; }
        public List<MainTaskItem> getTodayTasks() { return todayTasks; }
        public List<MainTaskItem> getUpcomingTasks() { return upcomingTasks; }
    }
}