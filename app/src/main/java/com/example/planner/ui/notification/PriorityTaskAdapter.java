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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

    @NonNull
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
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dateStr = (task.dueDate > 0) ? sdf.format(new Date(task.dueDate)) : "Không có hạn";
        
        if (subjectName != null && !subjectName.isEmpty()) {
            holder.tvSubtitle.setText(subjectName + " • " + dateStr);
        } else {
            holder.tvSubtitle.setText(dateStr);
        }

        holder.ivCheck.setImageResource(task.isCompleted ? R.drawable.ic_check_circle_24 : R.drawable.ic_radio_unchecked);
        holder.ivCheck.setAlpha(task.isCompleted ? 0.5f : 1.0f);
        holder.tvTitle.setAlpha(task.isCompleted ? 0.5f : 1.0f);

        holder.ivCheck.setOnClickListener(v -> {
            task.isCompleted = !task.isCompleted;
            notifyItemChanged(position);
            if (listener != null) {
                listener.onTaskStatusChanged(task);
            }
        });

        holder.ivBell.setVisibility(task.isReminderEnabled ? View.VISIBLE : View.GONE);
        holder.viewPriorityStrip.setBackgroundColor(holder.itemView.getContext().getColor(R.color.priority_high));
        holder.viewPriorityDot.setBackgroundResource(R.drawable.bg_priority_dot_red);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle;
        ImageView ivCheck, ivBell;
        View viewPriorityDot, viewPriorityStrip;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvSubtitle = itemView.findViewById(R.id.tvTaskSubtitle);
            ivCheck = itemView.findViewById(R.id.ivCheck);
            ivBell = itemView.findViewById(R.id.ivBell);
            viewPriorityDot = itemView.findViewById(R.id.viewPriorityDot);
            viewPriorityStrip = itemView.findViewById(R.id.viewPriorityStrip);
        }
    }
}
