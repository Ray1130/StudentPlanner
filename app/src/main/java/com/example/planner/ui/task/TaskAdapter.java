package com.example.planner.ui.task;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.planner.R;
import com.example.planner.data.model.Task;
import com.example.planner.utils.DateUtils;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> tasks;

    public TaskAdapter(List<Task> tasks) {
        this.tasks = tasks;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task_table_row, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.tvName.setText(task.title);
        holder.tvDeadline.setText(DateUtils.timestampToString(task.dueDate));
        holder.tvNote.setText(""); // Hoặc task.description nếu có
        holder.checkBox.setChecked(task.isCompleted);
    }

    @Override
    public int getItemCount() {
        return tasks != null ? tasks.size() : 0;
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDeadline, tvNote;
        CheckBox checkBox;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_task_name);
            tvDeadline = itemView.findViewById(R.id.tv_task_deadline);
            tvNote = itemView.findViewById(R.id.tv_task_note);
            checkBox = itemView.findViewById(R.id.cb_task_done);
        }
    }
}
