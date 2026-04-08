package com.example.planner.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class MainViewModel extends ViewModel {

    private final MutableLiveData<DashboardUiState> dashboardUiState = new MutableLiveData<>();

    public MainViewModel() {
        loadFakeData();
    }

    public LiveData<DashboardUiState> getDashboardUiState() {
        return dashboardUiState;
    }

    private void loadFakeData() {
        List<MainTaskItem> todayTasks = new ArrayList<>();
        todayTasks.add(new MainTaskItem("Train model KTDL", "KTDL • Quá hạn", MainTaskItem.PRIORITY_HIGH, false));

        List<MainTaskItem> upcomingTasks = new ArrayList<>();
        upcomingTasks.add(new MainTaskItem("Train model KTDL", "KTDL • 31/3", MainTaskItem.PRIORITY_MEDIUM, false));
        upcomingTasks.add(new MainTaskItem("Train model KTDL", "KTDL • 31/3", MainTaskItem.PRIORITY_HIGH, false));
        upcomingTasks.add(new MainTaskItem("Train model KTDL", "KTDL • 31/3", MainTaskItem.PRIORITY_MEDIUM, false));

        DashboardUiState state = new DashboardUiState(
                0,
                0,
                0,
                0,
                todayTasks,
                upcomingTasks
        );
        dashboardUiState.setValue(state);
    }

    public static class DashboardUiState {
        private final int todayCount;
        private final int overdueCount;
        private final int highPriorityCount;
        private final int completedCount;
        private final List<MainTaskItem> todayTasks;
        private final List<MainTaskItem> upcomingTasks;

        public DashboardUiState(int todayCount,
                                int overdueCount,
                                int highPriorityCount,
                                int completedCount,
                                List<MainTaskItem> todayTasks,
                                List<MainTaskItem> upcomingTasks) {
            this.todayCount = todayCount;
            this.overdueCount = overdueCount;
            this.highPriorityCount = highPriorityCount;
            this.completedCount = completedCount;
            this.todayTasks = todayTasks;
            this.upcomingTasks = upcomingTasks;
        }

        public int getTodayCount() {
            return todayCount;
        }

        public int getOverdueCount() {
            return overdueCount;
        }

        public int getHighPriorityCount() {
            return highPriorityCount;
        }

        public int getCompletedCount() {
            return completedCount;
        }

        public List<MainTaskItem> getTodayTasks() {
            return todayTasks;
        }

        public List<MainTaskItem> getUpcomingTasks() {
            return upcomingTasks;
        }
    }
}
