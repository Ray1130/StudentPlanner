package com.example.planner.ui.schedule;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.planner.data.local.AppDatabase;
import com.example.planner.data.model.Subject;
import com.example.planner.data.model.Task;
import com.example.planner.ui.main.MainTaskItem;
import com.example.planner.utils.DateUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScheduleViewModel extends AndroidViewModel {

    private final AppDatabase database;
    private final MutableLiveData<LocalDate> selectedDate = new MutableLiveData<>(LocalDate.now());
    private final LiveData<List<MainTaskItem>> tasksForSelectedDate;
    private final MutableLiveData<Map<LocalDate, CalendarTaskInfo>> monthTaskData = new MutableLiveData<>(new HashMap<>());
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final MutableLiveData<Map<Integer, String>> subjectsMap = new MutableLiveData<>(new HashMap<>());

    public ScheduleViewModel(@NonNull Application application) {
        super(application);
        database = AppDatabase.getDatabase(application);
        loadSubjects();

        tasksForSelectedDate = Transformations.switchMap(selectedDate, date -> {
            long startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1;
            
            return Transformations.map(database.taskDao().getTasksByDate(startOfDay, endOfDay), tasks -> {
                List<MainTaskItem> items = new ArrayList<>();
                Map<Integer, String> subjects = subjectsMap.getValue();
                
                for (Task task : tasks) {
                    // Filter out expired tasks (completed > 2 days)
                    boolean isExpired = task.expiryTimestamp > 0 && task.expiryTimestamp <= System.currentTimeMillis();
                    if (isExpired) continue;

                    String subjectName = subjects != null && subjects.containsKey(task.subjectId) 
                            ? subjects.get(task.subjectId) : null;
                    
                    String displaySubtitle = "";
                    if (subjectName != null) {
                        displaySubtitle = subjectName;
                    } else {
                        if (task.category != null && !task.category.isEmpty()) {
                            displaySubtitle = task.category;
                        } else if (task.note != null && !task.note.isEmpty()) {
                            displaySubtitle = task.note;
                        } else {
                            displaySubtitle = "Cá nhân";
                        }
                    }

                    String formattedDate = DateUtils.timestampToFormattedString(task.dueDate, task.isReminderEnabled);
                    String meta = displaySubtitle + " • " + formattedDate;
                    
                    int uiPriority = MainTaskItem.PRIORITY_LOW;
                    if ("high".equalsIgnoreCase(task.priority)) {
                        uiPriority = MainTaskItem.PRIORITY_HIGH;
                    } else if ("medium".equalsIgnoreCase(task.priority)) {
                        uiPriority = MainTaskItem.PRIORITY_MEDIUM;
                    }
                    
                    boolean isCourse = (task.subjectId > 0);
                    items.add(new MainTaskItem(task.id, task.title, meta, uiPriority, task.isCompleted, task.isReminderEnabled, isCourse));
                }
                return items;
            });
        });
    }

    private void loadSubjects() {
        executorService.execute(() -> {
            List<Subject> subjects = database.subjectDao().getAllSubjects();
            Map<Integer, String> map = new HashMap<>();
            for (Subject s : subjects) {
                map.put(s.id, s.name);
            }
            subjectsMap.postValue(map);
        });
    }

    public void loadTaskCountsForMonth(List<LocalDate> days) {
        if (days == null || days.isEmpty()) return;
        
        long start = days.get(0).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long end = days.get(days.size() - 1).plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

        executorService.execute(() -> {
            // Lấy danh sách môn học
            List<Subject> subjectsList = database.subjectDao().getAllSubjects();
            Map<Integer, String> sMap = new HashMap<>();
            for (Subject s : subjectsList) {
                sMap.put(s.id, s.name);
            }

            // Lấy tất cả task trong tháng
            List<Task> tasks = database.taskDao().getTasksByDateSync(start, end);
            Map<LocalDate, List<Task>> tasksByDate = new HashMap<>();
            for (Task t : tasks) {
                // Lọc task đã hết hạn (ẩn sau 2 ngày hoàn thành)
                boolean isExpired = t.expiryTimestamp > 0 && t.expiryTimestamp <= System.currentTimeMillis();
                if (isExpired) continue;

                LocalDate d = Instant.ofEpochMilli(t.dueDate).atZone(ZoneId.systemDefault()).toLocalDate();
                tasksByDate.computeIfAbsent(d, k -> new ArrayList<>()).add(t);
            }

            // Gom nhóm thông tin cho từng ngày
            Map<LocalDate, CalendarTaskInfo> dataMap = new HashMap<>();
            for (Map.Entry<LocalDate, List<Task>> entry : tasksByDate.entrySet()) {
                List<Task> dayTasks = entry.getValue();
                
                StringBuilder titles = new StringBuilder();
                java.util.Set<String> seenSubjects = new java.util.LinkedHashSet<>();

                for (int i = 0; i < dayTasks.size(); i++) {
                    Task t = dayTasks.get(i);
                    if (i < 2) {
                        if (titles.length() > 0) titles.append(", ");
                        titles.append(t.title);
                    }
                    
                    // Lấy tên môn học
                    String subName = sMap.getOrDefault(t.subjectId, "Nhiệm vụ");
                    seenSubjects.add(subName);
                }
                
                if (dayTasks.size() > 2) titles.append("...");
                String subjectsStr = String.join(", ", seenSubjects);
                dataMap.put(entry.getKey(), new CalendarTaskInfo(titles.toString(), subjectsStr));
            }
            monthTaskData.postValue(dataMap);
        });
    }

    public void setSelectedDate(LocalDate date) {
        selectedDate.setValue(date);
    }

    public LiveData<List<MainTaskItem>> getTasksForSelectedDate() {
        return tasksForSelectedDate;
    }

    public LiveData<LocalDate> getSelectedDate() {
        return selectedDate;
    }

    public LiveData<Map<LocalDate, CalendarTaskInfo>> getMonthTaskData() {
        return monthTaskData;
    }
}
