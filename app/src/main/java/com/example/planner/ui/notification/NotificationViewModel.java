package com.example.planner.ui.notification;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.planner.data.local.AppDatabase;
import com.example.planner.data.model.Subject;
import com.example.planner.data.model.Task;
import com.example.planner.data.repository.TaskRepository;

import java.util.ArrayList;
import java.util.List;

public class NotificationViewModel extends AndroidViewModel {
    private final MediatorLiveData<List<Task>> highPriorityTasks = new MediatorLiveData<>();
    private final MediatorLiveData<List<Task>> reminders = new MediatorLiveData<>();
    private final LiveData<List<Subject>> allSubjects;
    private final TaskRepository repository;

    public NotificationViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
        allSubjects = AppDatabase.getDatabase(application).subjectDao().getAllSubjectsLiveData();

        LiveData<List<Task>> tasksFromDb = repository.getAllTasks();
        
        highPriorityTasks.addSource(tasksFromDb, this::processTasks);
        reminders.addSource(tasksFromDb, this::processTasks);
        
        repository.syncTasksFromServer();
    }

    public LiveData<List<Task>> getHighPriorityTasks() {
        return highPriorityTasks;
    }

    public LiveData<List<Subject>> getSubjects() {
        return allSubjects;
    }

    public LiveData<List<Task>> getReminders() {
        return reminders;
    }

    public void fetchSubjects() {
        // Subjects are now observed via LiveData from DB
    }

    private void processTasks(List<Task> allTasks) {
        if (allTasks == null) return;
        
        List<Task> highPriority = new ArrayList<>();
        List<Task> reminderList = new ArrayList<>();
        long now = System.currentTimeMillis();
        long next24h = now + (24 * 60 * 60 * 1000);

        for (Task task : allTasks) {
            // Only show uncompleted tasks in Notification/Reminders
            if (task.isCompleted) continue;

            // High Priority Tasks
            if ("high".equalsIgnoreCase(task.priority)) {
                highPriority.add(task);
            }

            // Reminders: Due within 24h OR explicit reminder enabled
            if (task.dueDate > 0 && task.dueDate <= next24h && task.dueDate > now - 3600000) { 
                reminderList.add(task);
            } else if (task.isReminderEnabled) {
                reminderList.add(task);
            }
        }
        highPriorityTasks.setValue(highPriority);
        reminders.setValue(reminderList);
    }
}
