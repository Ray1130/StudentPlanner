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
        int completedThisMonth = 0;
        int remainingThisMonth = 0;

        Calendar cal = Calendar.getInstance();

        // Start of current month
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfMonth = cal.getTimeInMillis();

        // End of current month
        cal.add(Calendar.MONTH, 1);
        long startOfNextMonth = cal.getTimeInMillis();

        // Weekly Stats (Current Week - Fixed 7 days Monday to Sunday)
        int[] weeklyCompleted = new int[7];
        int[] weeklyTotal = new int[7];
        String[] dayNames = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        String[] weeklyLabels = new String[7];
        long[] weekDayStarts = new long[7];

        Calendar weekCal = Calendar.getInstance();
        weekCal.setFirstDayOfWeek(Calendar.MONDAY);
        weekCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        weekCal.set(Calendar.HOUR_OF_DAY, 0);
        weekCal.set(Calendar.MINUTE, 0);
        weekCal.set(Calendar.SECOND, 0);
        weekCal.set(Calendar.MILLISECOND, 0);

        SimpleDateFormat daySdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
        for (int i = 0; i < 7; i++) {
            weekDayStarts[i] = weekCal.getTimeInMillis();
            weeklyLabels[i] = dayNames[i] + "\n" + daySdf.format(weekCal.getTime());
            weekCal.add(Calendar.DAY_OF_YEAR, 1);
        }

        for (Task task : tasks) {
            // Chỉ tính task chưa hết hạn HOẶC đã hoàn thành (không tính task hết hạn mà chưa xong)
            boolean isExpired = task.expiryTimestamp > 0 && task.expiryTimestamp < System.currentTimeMillis();
            if (isExpired && !task.isCompleted) continue;

            // Monthly Stats
            if (task.dueDate >= startOfMonth && task.dueDate < startOfNextMonth) {
                if (task.isCompleted) {
                    completedThisMonth++;
                } else {
                    remainingThisMonth++;
                }
            }

            // Weekly Stats
            for (int i = 0; i < 7; i++) {
                long dStart = weekDayStarts[i];
                long dEnd = dStart + 24 * 60 * 60 * 1000;
                if (task.dueDate >= dStart && task.dueDate < dEnd) {
                    weeklyTotal[i]++;
                    if (task.isCompleted) {
                        weeklyCompleted[i]++;
                    }
                }
            }
        }

        profileStats.setValue(new ProfileStats(0, 0, completedThisMonth, remainingThisMonth, weeklyCompleted, weeklyTotal, weeklyLabels));
    }

    public List<String> getMonthlyHistory(List<Task> tasks) {
        java.util.Map<String, Integer> history = new java.util.TreeMap<>(java.util.Collections.reverseOrder());
        SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        for (Task task : tasks) {
            if (task.isCompleted) {
                cal.setTimeInMillis(task.dueDate);
                String monthKey = sdf.format(cal.getTime());
                history.put(monthKey, history.getOrDefault(monthKey, 0) + 1);
            }
        }

        List<String> historyList = new java.util.ArrayList<>();
        for (java.util.Map.Entry<String, Integer> entry : history.entrySet()) {
            historyList.add("Tháng " + entry.getKey() + ": " + entry.getValue() + " nhiệm vụ");
        }
        return historyList;
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
