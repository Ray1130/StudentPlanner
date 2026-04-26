package com.example.planner.ui.schedule;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.planner.R;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import com.example.planner.ui.main.MainTaskItem;
import com.example.planner.ui.main.MainTaskAdapter;

import com.example.planner.ui.BaseActivity;
import java.util.List;

public class ScheduleActivity extends BaseActivity {

    private TextView tvCurrentMonth;
    private RecyclerView rvCalendar;
    private ImageButton btnPrevMonth, btnNextMonth;
    private CalendarAdapter adapter;
    private TextView tvTaskCount;
    private LocalDate selectedDate;
    private MainTaskAdapter taskAdapter;
    private RecyclerView rvTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 1. Nạp giao diện XML vào Activity
        setContentView(R.layout.activity_schedule);

        // 2. Khởi tạo dữ liệu
        selectedDate = LocalDate.now();

        initWidgets();

        setMonthView();

        showTasksForSelectedDate();

        btnPrevMonth.setOnClickListener(v -> {
            selectedDate = selectedDate.minusMonths(1);
            setMonthView();
        });

        btnNextMonth.setOnClickListener(v -> {
            selectedDate = selectedDate.plusMonths(1);
            setMonthView();
        });

        setupBottomNavigation(R.id.nav_calendar);
    }

    private void initWidgets() {
        // Trong Activity, gọi trực tiếp findViewById
        rvCalendar = findViewById(R.id.rvCalendar);
        tvCurrentMonth = findViewById(R.id.tvCurrentMonth);
        rvTasks = findViewById(R.id.rv_tasks);
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        tvTaskCount = findViewById(R.id.tvTaskCount);
    }

    private void updateTaskCount(int count) {
        if (tvTaskCount != null) {
            tvTaskCount.setText(count + " công việc hiện tại");
        }
    }

    private void showTasksForSelectedDate() {
        // 1. Dùng đúng Model MainTaskItem thay vì Task
        List<MainTaskItem> list = new ArrayList<>();

        // Giả sử constructor của MainTaskItem là: (Title, Meta, IsCompleted, Priority)
        // Bạn hãy kiểm tra lại file MainTaskItem.java để truyền đúng tham số nhé
        list.add(new MainTaskItem("Design figma", "Mobile - 1/4", MainTaskItem.PRIORITY_HIGH, false));
        list.add(new MainTaskItem("Viết latex", "Mobile - 1/4", MainTaskItem.PRIORITY_MEDIUM, false));

        // 2. Khởi tạo adapter (MainTaskAdapter không dùng list trong constructor)
        if (taskAdapter == null) {
            taskAdapter = new MainTaskAdapter();
            rvTasks.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
            rvTasks.setAdapter(taskAdapter);
        }

        // 3. Sử dụng hàm submitList() của MainTaskAdapter để đổ dữ liệu
        taskAdapter.submitList(list);

        updateTaskCount(list.size());
    }

    private void setMonthView() {
        // 1. Cập nhật tiêu đề tháng năm (ví dụ: Tháng 4, 2026)
        tvCurrentMonth.setText(monthYearFromDate(selectedDate));

        // 2. Lấy danh sách 42 ngày (bao gồm cả ngày bù của tháng trước/sau)
        ArrayList<LocalDate> daysInMonth = daysInMonthList(selectedDate);

        // 3. Khởi tạo Adapter với ĐẦY ĐỦ 3 tham số:
        // Tham số 1: List ngày
        // Tham số 2: selectedDate (để adapter biết tháng nào cần làm rõ, tháng nào làm mờ)
        // Tham số 3: Listener xử lý sự kiện click
        adapter = new CalendarAdapter(daysInMonth, selectedDate, (position, date) -> {
            if (date != null) {
                // Nếu click vào một ngày, cập nhật selectedDate và vẽ lại giao diện
                selectedDate = date;
                setMonthView();

                showTasksForSelectedDate();
                // Log hoặc Toast để kiểm tra
                // Toast.makeText(this, "Bạn chọn ngày: " + date, Toast.LENGTH_SHORT).show();
            }
        });

        // 4. Thiết lập LayoutManager (7 cột) và gắn Adapter
        GridLayoutManager layoutManager = new GridLayoutManager(this, 7);
        rvCalendar.setLayoutManager(layoutManager);
        rvCalendar.setAdapter(adapter);

        // 5. Cập nhật số lượng công việc (giả sử lấy từ database hoặc mock 10)
        updateTaskCount(10);
    }

    private String monthYearFromDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        return date.format(formatter);
    }

    private ArrayList<LocalDate> daysInMonthList(LocalDate date) {
        ArrayList<LocalDate> days = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);
        int daysInMonth = yearMonth.lengthOfMonth();

        LocalDate firstOfMonth = date.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue(); // T2=1, ..., CN=7

        // 1. Lấy ngày của tháng trước để lấp đầy hàng đầu tiên
        // dayOfWeek = 1 (T2) thì không cần bù, 2 (T3) bù 1 ngày...
        int daysToBefore = dayOfWeek - 1;
        LocalDate prevMonth = date.minusMonths(1);
        int daysInPrevMonth = YearMonth.from(prevMonth).lengthOfMonth();

        for (int i = daysToBefore - 1; i >= 0; i--) {
            days.add(prevMonth.withDayOfMonth(daysInPrevMonth - i));
        }

        // 2. Thêm ngày của tháng hiện tại
        for (int i = 1; i <= daysInMonth; i++) {
            days.add(date.withDayOfMonth(i));
        }

        // 3. Lấp đầy các ô trống còn lại bằng ngày tháng sau (tổng 42 ô)
        int nextMonthDays = 35 - days.size();
        for (int i = 1; i <= nextMonthDays; i++) {
            days.add(date.plusMonths(1).withDayOfMonth(i));
        }

        return days;
    }
}