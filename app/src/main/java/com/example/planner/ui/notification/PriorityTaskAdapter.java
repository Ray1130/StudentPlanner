package com.example.planner.ui.notification;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.planner.R;
import com.example.planner.data.model.Subject;
import com.example.planner.data.model.Task;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PriorityTaskAdapter extends RecyclerView.Adapter<PriorityTaskAdapter.ViewHolder> {

    private List<Task> tasks = new ArrayList<>();
    private Map<Integer, String> subjectMap = new HashMap<>();

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    public void setSubjects(List<Subject> subjects) {
        subjectMap.clear();
        if (subjects != null) {
            for (Subject subject : subjects) {
                subjectMap.put(subject.id, subject.name);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task_card, parent, false);
        return new ViewHolder(view);
    }

    public interface OnTaskStatusChangeListener {
        void onTaskStatusChanged(Task task);
    }

    private OnTaskStatusChangeListener listener;

    public void setOnTaskStatusChangeListener(OnTaskStatusChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.tvTitle.setText(task.title);
        
        String subjectName = subjectMap.get(task.subjectId);
        String remainingText = getRemainingTimeText(task.dueDate);
        
        if (subjectName != null && !subjectName.isEmpty()) {
            holder.tvSubtitle.setText(subjectName + " • " + remainingText);
        } else {
            holder.tvSubtitle.setText(remainingText);
        }

        int iconRes = task.isCompleted ? R.drawable.ic_check_circle_24 : R.drawable.ic_circle_outline_24;
        holder.ivCheck.setImageResource(iconRes);
        int tintColor = task.isCompleted ? R.color.success : R.color.text_secondary;
        holder.ivCheck.setColorFilter(androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(), tintColor));
        holder.ivCheck.setAlpha(1.0f);
        holder.tvTitle.setAlpha(task.isCompleted ? 0.5f : 1.0f);

        holder.ivCheck.setOnClickListener(v -> {
            task.isCompleted = !task.isCompleted;
            notifyItemChanged(position);
            if (listener != null) {
                listener.onTaskStatusChanged(task);
            }
        });

        holder.ivBell.setVisibility(task.isReminderEnabled ? View.VISIBLE : View.GONE);
        
        // Priority UI consistency
        int priorityColor;
        String priority = task.priority != null ? task.priority.toLowerCase() : "low";
        switch (priority) {
            case "high":
                priorityColor = holder.itemView.getContext().getColor(R.color.priority_high);
                break;
            case "medium":
                priorityColor = holder.itemView.getContext().getColor(R.color.priority_medium);
                break;
            default:
                priorityColor = holder.itemView.getContext().getColor(R.color.priority_low);
                break;
        }
        holder.viewPriorityStrip.setBackgroundColor(priorityColor);
    }

    private String getRemainingTimeText(long dueDate) {
        if (dueDate <= 0) return "Không có hạn";
        
        long now = System.currentTimeMillis();
        long diff = dueDate - now;
        
        if (diff <= 0) return "Quá hạn";
        
        long hours = diff / (1000 * 60 * 60);
        if (hours < 24) {
            if (hours == 0) {
                long minutes = diff / (1000 * 60);
                return "Còn " + minutes + " phút";
            }
            return "Còn " + hours + " giờ";
        } else {
            long days = hours / 24;
            return "Còn " + days + " ngày";
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle;
        ImageView ivCheck, ivBell;
        View viewPriorityStrip;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvSubtitle = itemView.findViewById(R.id.tvTaskSubtitle);
            ivCheck = itemView.findViewById(R.id.ivCheck);
            ivBell = itemView.findViewById(R.id.ivBell);
            viewPriorityStrip = itemView.findViewById(R.id.viewPriorityStrip);
        }
    }
}
