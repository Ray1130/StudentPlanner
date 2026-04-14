package com.example.planner.ui.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planner.R;
import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private MainViewModel viewModel;
    private MainTaskAdapter todayAdapter;
    private MainTaskAdapter upcomingAdapter;

    private TextView tvGreeting;
    private TextView tvDate;
    private TextView tvTodayCount;
    private TextView tvOverdueCount;
    private TextView tvPriorityCount;
    private TextView tvCompletedCount;
    private ImageView ivMenu;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawerLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupToolbarDrawer();
        setupRecyclerView();
        setupViewModel();
        bindStaticData();
        observeUi();

        NavigationView navigationView = findViewById(R.id.navigationView);

        navigationView.setNavigationItemSelectedListener(this);
    }

    private void initViews() {
        tvGreeting = findViewById(R.id.tvGreeting);
        tvDate = findViewById(R.id.tvDate);
        tvTodayCount = findViewById(R.id.tvTodayCount);
        tvOverdueCount = findViewById(R.id.tvOverdueCount);
        tvPriorityCount = findViewById(R.id.tvPriorityCount);
        tvCompletedCount = findViewById(R.id.tvCompletedCount);
        ivMenu = findViewById(R.id.ivMenu);
        drawerLayout = findViewById(R.id.drawerLayout);

        // Nút "Công việc" trong Grid
        findViewById(R.id.btn_nav_tasks).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, com.example.planner.ui.task.TaskActivity.class));
        });
    }

    private void setupToolbarDrawer() {
        NavigationView navigationView = findViewById(R.id.navigationView);

        if (ivMenu != null && drawerLayout != null) {
            ivMenu.setOnClickListener(v -> {
                drawerLayout.openDrawer(GravityCompat.START);
            });
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.open_drawer,
                R.string.close_drawer
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                drawerLayout.closeDrawers();
                return true;
            });
        }
    }

    private void setupRecyclerView() {
        RecyclerView rvTodayTasks = findViewById(R.id.rvTodayTasks);
        RecyclerView rvUpcomingTasks = findViewById(R.id.rvUpcomingTasks);

        if (rvTodayTasks != null) {
            rvTodayTasks.setLayoutManager(new LinearLayoutManager(this));
            todayAdapter = new MainTaskAdapter();
            rvTodayTasks.setAdapter(todayAdapter);
        }

        if (rvUpcomingTasks != null) {
            rvUpcomingTasks.setLayoutManager(new LinearLayoutManager(this));
            upcomingAdapter = new MainTaskAdapter();
            rvUpcomingTasks.setAdapter(upcomingAdapter);
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
    }

    private void bindStaticData() {
        String dateText = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi", "VN"))
                .format(new Date());
        if (tvDate != null) tvDate.setText(capitalizeFirstLetter(dateText));
        if (tvGreeting != null) tvGreeting.setText(getString(R.string.welcome_user, "Nhân"));
    }

    private void observeUi() {
        viewModel.getDashboardUiState().observe(this, state -> {
            if (state == null) return;

            if (tvTodayCount != null) tvTodayCount.setText(String.valueOf(state.getTodayCount()));
            if (tvOverdueCount != null) tvOverdueCount.setText(String.valueOf(state.getOverdueCount()));
            if (tvPriorityCount != null) tvPriorityCount.setText(String.valueOf(state.getHighPriorityCount()));
            if (tvCompletedCount != null) tvCompletedCount.setText(String.valueOf(state.getCompletedCount()));

            if (todayAdapter != null) todayAdapter.submitList(state.getTodayTasks());
            if (upcomingAdapter != null) upcomingAdapter.submitList(state.getUpcomingTasks());
        });
    }

    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) return "";
        return input.substring(0, 1).toUpperCase(new Locale("vi", "VN")) + input.substring(1);
    }

    // Xử lý sự kiện logout khi bấm Đăng xuất từ giao diện trang chủ
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.nav_logout) {

            SharedPreferences preferences = getSharedPreferences("USER_FILE", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            finish();

            return true;
        }

        return false;
    }
}
