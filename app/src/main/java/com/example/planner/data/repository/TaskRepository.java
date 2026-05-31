package com.example.planner.data.repository;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.lifecycle.LiveData;
import com.example.planner.data.ApiService;
import com.example.planner.data.local.AppDatabase;
import com.example.planner.data.local.TaskDao;
import com.example.planner.data.local.SubjectDao;
import com.example.planner.data.model.Task;
import com.example.planner.receiver.ReminderReceiver;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TaskRepository {
    private TaskDao taskDao;
    private SubjectDao subjectDao;
    private ApiService apiService;
    private ExecutorService executorService;
    private Context context;

    public TaskRepository(Application application) {
        this.context = application.getApplicationContext();
        AppDatabase db = AppDatabase.getDatabase(application);
        taskDao = db.taskDao();
        subjectDao = db.subjectDao();
        executorService = Executors.newSingleThreadExecutor();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        cleanupExpiredTasks();
    }

    public void cleanupExpiredTasks() {
        executorService.execute(() -> {
            long now = System.currentTimeMillis();
            taskDao.deleteExpiredTasks(now);
            Log.d("TaskRepository", "Cleaned up expired tasks at " + now);
        });
    }

    public void toggleTaskCompletion(int taskId) {
        executorService.execute(() -> {
            Task task = taskDao.getTaskByIdSync(taskId);
            if (task != null) {
                task.isCompleted = !task.isCompleted;
                if (task.isCompleted) {
                    // Tự động ẩn sau 2 ngày (2 * 24 * 60 * 60 * 1000 ms)
                    task.expiryTimestamp = System.currentTimeMillis() + (2 * 24 * 60 * 60 * 1000L);
                } else {
                    task.expiryTimestamp = 0;
                }
                taskDao.update(task);
                Log.d("TaskRepository", "Toggled task " + taskId + " locally to " + task.isCompleted + " with expiry "
                        + task.expiryTimestamp);

                apiService.updateTask(task).enqueue(new Callback<Task>() {
                    @Override
                    public void onResponse(Call<Task> call, Response<Task> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Log.d("TaskRepository", "Sync successful for task " + taskId);
                            executorService.execute(() -> taskDao.update(response.body()));
                        } else {
                            Log.e("TaskRepository", "Sync failed for task " + taskId + ": " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Task> call, Throwable t) {
                        Log.e("TaskRepository", "Sync error for task " + taskId + ": " + t.getMessage());
                    }
                });
            } else {
                Log.e("TaskRepository", "Task not found: " + taskId);
            }
        });
    }

    public void syncTasksFromServer() {
        apiService.getAllTasks().enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Task> serverTasks = response.body();
                    executorService.execute(() -> {
                        // Không dùng deleteAll() để tránh mất dữ liệu local chưa kịp sync
                        for (Task serverTask : serverTasks) {
                            // Get existing task from local DB to preserve category
                            Task localTask = taskDao.getTaskByIdSync(serverTask.id);

                            // Preserve category from local if server doesn't have it
                            if ((serverTask.category == null || serverTask.category.isEmpty()) &&
                                    localTask != null && localTask.category != null) {
                                serverTask.category = localTask.category;
                                Log.d("TaskRepository",
                                        "Preserved category '" + serverTask.category + "' for task " + serverTask.id);
                            }

                            // Use update if task exists, insert if new
                            if (localTask != null) {
                                taskDao.update(serverTask);
                            } else {
                                taskDao.insert(serverTask);
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Task>> call, Throwable t) {
                Log.e("TaskRepository", "Failed to sync tasks: " + t.getMessage());
            }
        });
    }

    public void insertTask(Task task) {
        executorService.execute(() -> {
            long id = taskDao.insert(task);
            task.id = (int) id;
            if (task.isReminderEnabled && !task.isCompleted) {
                scheduleReminder(task);
            }
        });
    }

    public void updateTask(Task task) {
        executorService.execute(() -> {
            taskDao.update(task);
            if (task.isCompleted) {
                cancelReminder(task);
            } else if (task.isReminderEnabled) {
                scheduleReminder(task);
            } else {
                cancelReminder(task);
            }
        });
    }

    public void deleteTask(Task task) {
        executorService.execute(() -> {
            cancelReminder(task);
            taskDao.delete(task);
        });
    }

    public void deleteById(int taskId) {
        executorService.execute(() -> {
            Task task = taskDao.getTaskByIdSync(taskId);
            if (task != null) {
                cancelReminder(task);
            }
            taskDao.deleteById(taskId);
        });
    }

    private void scheduleReminder(Task task) {
        if (task.dueDate <= 0)
            return;

        long reminderTime = task.dueDate - (10 * 60 * 1000); // 10 phút trước
        if (reminderTime <= System.currentTimeMillis())
            return;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("task_title", task.title);
        intent.putExtra("task_id", task.id);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, task.id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
            }
            Log.d("TaskRepository", "Scheduled reminder for task: " + task.title + " at " + reminderTime);
        }
    }

    private void cancelReminder(Task task) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, task.id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            Log.d("TaskRepository", "Canceled reminder for task: " + task.title);
        }
    }

    public LiveData<List<Task>> getAllTasks() {
        return taskDao.getAllTasks();
    }

    public LiveData<List<Task>> getTasksBySubject(int subjectId) {
        return taskDao.getTasksBySubject(subjectId);
    }

    public LiveData<List<Task>> getPendingTasks() {
        return taskDao.getPendingTasks();
    }

    public LiveData<List<Task>> getTasksByDate(long startOfDay, long endOfDay) {
        return taskDao.getTasksByDate(startOfDay, endOfDay);
    }

    public ApiService getApiService() {
        return apiService;
    }

    public String getSubjectNameByIdSync(int subjectId) {
        try {
            return subjectDao.getSubjectNameById(subjectId);
        } catch (Exception e) {
            Log.e("TaskRepository", "Failed to get subject name for ID " + subjectId, e);
            return null;
        }
    }
}
