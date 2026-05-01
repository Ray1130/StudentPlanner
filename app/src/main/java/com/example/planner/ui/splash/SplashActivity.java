package com.example.planner.ui.splash;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.example.planner.R;
import com.example.planner.ui.main.LoginActivity;
import com.example.planner.ui.main.MainActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Dòng này giúp kết nối màn hình hệ thống với trang của bạn, xóa bỏ icon Android mặc định
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.ivSplashLogo);
        TextView appName = findViewById(R.id.tvAppName);

        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeIn.setDuration(1500);
        
        if (logo != null) logo.startAnimation(fadeIn);
        if (appName != null) appName.startAnimation(fadeIn);

        new Handler(Looper.getMainLooper()).postDelayed(this::checkLoginStatus, 2500);
    }

    private void checkLoginStatus() {
        SharedPreferences preferences = getSharedPreferences("USER_FILE", MODE_PRIVATE);
        boolean isLoggedIn = preferences.getBoolean("isLoggedIn", false);

        Intent intent;
        if (isLoggedIn) {
            intent = new Intent(SplashActivity.this, MainActivity.class);
        } else {
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }
        
        startActivity(intent);
        finish();
    }
}
