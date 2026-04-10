package com.example.planner.ui.task;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planner.R;
import java.util.ArrayList;
import java.util.List;

public class TaskActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        // 1. Gán số lượng biến chữ cho Title
        TextView tvCount = findViewById(R.id.tv_task_count);
        if (tvCount != null) {
            tvCount.setText(getString(R.string.task_count_format, 10));
        }

        // 2. Thiết lập dữ liệu Fake (Dummy Data)
        List<TaskUiModel> dummyData = new ArrayList<>();
        
        // --- Môn Mobile ---
        dummyData.add(new TaskUiModel(TaskUiModel.TYPE_HEADER, "Môn Mobile", "", "", false));
        dummyData.add(new TaskUiModel(TaskUiModel.TYPE_TABLE_HEADER, "", "", "", false));
        dummyData.add(new TaskUiModel(TaskUiModel.TYPE_TABLE_ROW, "Giới thiệu bài toán", "10/3/2026", "", true));
        dummyData.add(new TaskUiModel(TaskUiModel.TYPE_TABLE_ROW, "Phác thảo figma", "20/3/2026", "", true));
        dummyData.add(new TaskUiModel(TaskUiModel.TYPE_TABLE_ROW, "Code front-end", "10/4/2026", "", false));
        dummyData.add(new TaskUiModel(TaskUiModel.TYPE_TABLE_ROW, "Code back-end", "20/4/2026", "", false));
        dummyData.add(new TaskUiModel(TaskUiModel.TYPE_ACTION_NEW_PAGE, "", "", "", false));
        
        // --- Môn CSDL Phân tán ---
        dummyData.add(new TaskUiModel(TaskUiModel.TYPE_HEADER, "Môn CSDL phân tán", "", "", false));
        dummyData.add(new TaskUiModel(TaskUiModel.TYPE_TABLE_HEADER, "", "", "", false));
        dummyData.add(new TaskUiModel(TaskUiModel.TYPE_TABLE_ROW, "Giới thiệu bài toán", "10/3/2026", "", true));
        dummyData.add(new TaskUiModel(TaskUiModel.TYPE_TABLE_ROW, "Phác thảo figma", "20/3/2026", "", true));
        dummyData.add(new TaskUiModel(TaskUiModel.TYPE_TABLE_ROW, "Code front-end", "10/4/2026", "", false));
        dummyData.add(new TaskUiModel(TaskUiModel.TYPE_TABLE_ROW, "Code back-end", "20/4/2026", "", false));
        dummyData.add(new TaskUiModel(TaskUiModel.TYPE_ACTION_NEW_PAGE, "", "", "", false));
        
        // --- Nút thêm nhóm cuối cùng ---
        dummyData.add(new TaskUiModel(TaskUiModel.TYPE_ACTION_NEW_GROUP, "", "", "", false));

        // 3. Set Adapter
        RecyclerView rvTasks = findViewById(R.id.rv_tasks);
        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        rvTasks.setAdapter(new TaskSectionAdapter(dummyData));
    }
}