package com.example.planner.ui.main;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.planner.data.local.AppDatabase;
import com.example.planner.data.model.Task;
import com.example.planner.data.repository.TaskRepository;
import com.example.planner.utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private final MediatorLiveData<DashboardUiState> dashboardUiState = new MediatorLiveData<>();
    private final TaskRepository repository;

    public MainViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);

        LiveData<List<Task>> tasksFromDb = repository.getAllTasks();
        
        dashboardUiState.addSource(tasksFromDb, tasks -> {
            if (tasks != null) {
                processTasks(tasks);
            }
        });

        // Đồng bộ dữ liệu
        repository.syncTasksFromServer();
    }

    public void updateTaskStatus(int taskId, boolean isCompleted) {
        // Repository đã có hàm toggle hoặc update, dùng chung để đồng nhất logic
        repository.toggleTaskCompletion(taskId);
    }

    public LiveData<DashboardUiState> getDashboardUiState() {
        return dashboardUiState;
    }

    private void processTasks(List<Task> tasks) {
        // Thực hiện dọn dẹp task đã hết hạn khi xử lý dữ liệu
        repository.cleanupExpiredTasks();

        List<MainTaskItem> todayTasks = new ArrayList<>();
        List<MainTaskItem> upcomingTasks = new ArrayList<>();

        int todayCount = 0;
        int overdueCount = 0;
        int highPriorityCount = 0;
        int completedCount = 0;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfToday = cal.getTimeInMillis();
        long endOfToday = startOfToday + (24 * 60 * 60 * 1000);

        for (Task task : tasks) {
            // Chỉ hiển thị task chưa hết hạn trên trang chủ
            boolean isExpired = task.expiryTimestamp > 0 && task.expiryTimestamp <= System.currentTimeMillis();
            if (isExpired) {
                continue;
            }

            if (task.isCompleted) {
                completedCount++;
            }

            long dueDate = task.dueDate;
            boolean isToday = dueDate >= startOfToday && dueDate < endOfToday;
            boolean isOverdue = dueDate < startOfToday && dueDate > 0;

            if (!task.isCompleted) {
                if (isToday) todayCount++;
                if (isOverdue) overdueCount++;
                if ("high".equalsIgnoreCase(task.priority)) highPriorityCount++;
            }

            String dateStr = DateUtils.timestampToString(task.dueDate);
            String categoryOrNote = "";
            if (task.category != null && !task.category.isEmpty() && !task.category.equalsIgnoreCase("Học tập")) {
                categoryOrNote = task.category;
            } else if (task.note != null && !task.note.isEmpty()) {
                categoryOrNote = task.note;
            }

            String meta = categoryOrNote.isEmpty() ? dateStr : categoryOrNote + " • " + dateStr;
            
            int uiPriority = MainTaskItem.PRIORITY_LOW;
            if ("high".equalsIgnoreCase(task.priority)) {
                uiPriority = MainTaskItem.PRIORITY_HIGH;
            } else if ("medium".equalsIgnoreCase(task.priority)) {
                uiPriority = MainTaskItem.PRIORITY_MEDIUM;
            }

            MainTaskItem item = new MainTaskItem(task.id, task.title, meta, uiPriority, task.isCompleted, task.isReminderEnabled);

            if (isToday || isOverdue) todayTasks.add(item);
            else upcomingTasks.add(item);
        }

        dashboardUiState.setValue(new DashboardUiState(
                todayCount, overdueCount, highPriorityCount, completedCount, todayTasks, upcomingTasks
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
