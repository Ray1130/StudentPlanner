package com.example.planner.ui.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.planner.data.local.AppDatabase;
import com.example.planner.data.model.Task;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class UserProfileViewModel extends AndroidViewModel {

    private final MediatorLiveData<ProfileStats> profileStats = new MediatorLiveData<>();
    private final AppDatabase database;

    public UserProfileViewModel(@NonNull Application application) {
        super(application);
        database = AppDatabase.getDatabase(application);

        LiveData<List<Task>> allTasks = database.taskDao().getAllTasks();
        profileStats.addSource(allTasks, tasks -> {
            if (tasks != null) {
                calculateStats(tasks);
            }
        });
    }

    public LiveData<ProfileStats> getProfileStats() {
        return profileStats;
    }

    private void calculateStats(List<Task> tasks) {
        int totalCompleted = 0;
        int totalRemaining = 0;
        int completedToday = 0;
        int remainingToday = 0;
        
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfToday = cal.getTimeInMillis();
        long endOfToday = startOfToday + (24 * 60 * 60 * 1000);

        int[] last7DaysCompleted = new int[7];
        int[] last7DaysTotal = new int[7];
        String[] last7DaysLabels = new String[7];
        long[] dayStarts = new long[7];
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
        for (int i = 0; i < 7; i++) {
            Calendar dayCal = (Calendar) cal.clone();
            dayCal.add(Calendar.DAY_OF_YEAR, -(6 - i));
            dayStarts[i] = dayCal.getTimeInMillis();
            last7DaysLabels[i] = sdf.format(dayCal.getTime());
        }

        for (Task task : tasks) {
            // Thống kê tổng quát (tất cả các ngày)
            if (task.isCompleted) {
                totalCompleted++;
            } else {
                totalRemaining++;
            }

            // Thống kê cho hôm nay (Donut Chart)
            if (task.dueDate >= startOfToday && task.dueDate < endOfToday) {
                if (task.isCompleted) {
                    completedToday++;
                } else {
                    remainingToday++;
                }
            }

            // Thống kê 7 ngày qua (Bar Chart)
            for (int i = 0; i < 7; i++) {
                long dStart = dayStarts[i];
                long dEnd = dStart + 24 * 60 * 60 * 1000;
                if (task.dueDate >= dStart && task.dueDate < dEnd) {
                    last7DaysTotal[i]++;
                    if (task.isCompleted) {
                        last7DaysCompleted[i]++;
                    }
                }
            }
        }

        profileStats.setValue(new ProfileStats(totalCompleted, totalRemaining, completedToday, remainingToday, last7DaysCompleted, last7DaysTotal, last7DaysLabels));
    }

    public static class ProfileStats {
        private final int totalCompleted;
        private final int totalRemaining;
        private final int completedToday;
        private final int remainingToday;
        private final int[] last7DaysCompleted;
        private final int[] last7DaysTotal;
        private final String[] last7DaysLabels;

        public ProfileStats(int totalCompleted, int totalRemaining, int completedToday, int remainingToday, int[] last7DaysCompleted, int[] last7DaysTotal, String[] last7DaysLabels) {
            this.totalCompleted = totalCompleted;
            this.totalRemaining = totalRemaining;
            this.completedToday = completedToday;
            this.remainingToday = remainingToday;
            this.last7DaysCompleted = last7DaysCompleted;
            this.last7DaysTotal = last7DaysTotal;
            this.last7DaysLabels = last7DaysLabels;
        }

        public int getTotalCompleted() { return totalCompleted; }
        public int getTotalRemaining() { return totalRemaining; }
        public int getCompletedToday() { return completedToday; }
        public int getRemainingToday() { return remainingToday; }
        public int[] getLast7DaysCompleted() { return last7DaysCompleted; }
        public int[] getLast7DaysTotal() { return last7DaysTotal; }
        public String[] getLast7DaysLabels() { return last7DaysLabels; }
        
        public int getTodayTotal() { return completedToday + remainingToday; }
        public int getTodayPercentage() {
            int total = getTodayTotal();
            if (total == 0) return 0;
            // Tính toán % hoàn thành dựa trên số task hoàn thành / tổng số task của hôm nay
            return (int) (((float) completedToday / total) * 100);
        }
    }
}
