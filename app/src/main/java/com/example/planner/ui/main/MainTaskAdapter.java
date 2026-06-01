package com.example.planner.ui.main;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planner.R;

import java.util.ArrayList;
import java.util.List;

public class MainTaskAdapter extends RecyclerView.Adapter<MainTaskAdapter.TaskViewHolder> {

    private final List<MainTaskItem> items = new ArrayList<>();
    private OnTaskStatusChangeListener listener;

    public interface OnTaskStatusChangeListener {
        void onStatusChanged(int taskId, boolean isCompleted);
        void onTaskClick(int taskId);
    }

    public void setOnTaskStatusChangeListener(OnTaskStatusChangeListener listener) {
        this.listener = listener;
    }

    public void submitList(List<MainTaskItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main_task, parent, false);
        return new TaskViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgStatus;
        private final ImageView imgReminder;
        private final TextView tvTaskTitle;
        private final TextView tvTaskMeta;
        private final TextView tvTaskTag;
        private final View viewPriorityStrip;
        private final OnTaskStatusChangeListener listener;

        public TaskViewHolder(@NonNull View itemView, OnTaskStatusChangeListener listener) {
            super(itemView);
            this.listener = listener;
            imgStatus = itemView.findViewById(R.id.imgStatus);
            imgReminder = itemView.findViewById(R.id.imgReminder);
            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvTaskMeta = itemView.findViewById(R.id.tvTaskMeta);
            tvTaskTag = itemView.findViewById(R.id.tvTaskTag);
            viewPriorityStrip = itemView.findViewById(R.id.viewPriorityStrip);
        }

        void bind(MainTaskItem item) {
            tvTaskTitle.setText(item.getTitle());
            tvTaskMeta.setText(item.getMeta());

            if (tvTaskTag != null) {
                if (item.isCourse()) {
                    tvTaskTag.setText("Học phần");
                    tvTaskTag.setBackgroundResource(R.drawable.bg_tag_course);
                    tvTaskTag.setTextColor(itemView.getContext().getColor(R.color.tag_course_text));
                } else {
                    tvTaskTag.setText("Ngoại khóa");
                    tvTaskTag.setBackgroundResource(R.drawable.bg_tag_extracurricular);
                    tvTaskTag.setTextColor(itemView.getContext().getColor(R.color.tag_extracurricular_text));
                }
            }

            if (imgReminder != null) {
                imgReminder.setVisibility(item.isReminderEnabled() ? View.VISIBLE : View.GONE);
            }

            int statusIcon = item.isCompleted() ? R.drawable.ic_check_circle_24 : R.drawable.ic_circle_outline_24;
            int statusTint = item.isCompleted() ? R.color.success : R.color.text_secondary;
            imgStatus.setImageResource(statusIcon);
            imgStatus.setColorFilter(ContextCompat.getColor(itemView.getContext(), statusTint));

            imgStatus.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStatusChanged(item.getId(), !item.isCompleted());
                }
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskClick(item.getId());
                }
            });

            int priorityDrawable;
            // Gạch màu ưu tiên
            int priorityColor;
            switch (item.getPriority()) {
                case MainTaskItem.PRIORITY_HIGH:
                    priorityColor = R.color.priority_high;
                    break;
                case MainTaskItem.PRIORITY_MEDIUM:
                    priorityColor = R.color.priority_medium;
                    break;
                case MainTaskItem.PRIORITY_LOW:
                default:
                    priorityColor = R.color.priority_low;
                    break;
            }

            if (viewPriorityStrip != null) {
                viewPriorityStrip.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), priorityColor));
            }
        }
    }
}
