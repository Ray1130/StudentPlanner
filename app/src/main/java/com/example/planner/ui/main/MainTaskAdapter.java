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
        return new TaskViewHolder(view);
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
        private final View viewPriorityDot;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            imgStatus = itemView.findViewById(R.id.imgStatus);
            imgReminder = itemView.findViewById(R.id.imgReminder);
            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvTaskMeta = itemView.findViewById(R.id.tvTaskMeta);
            viewPriorityDot = itemView.findViewById(R.id.viewPriorityDot);
        }

        void bind(MainTaskItem item) {
            tvTaskTitle.setText(item.getTitle());
            tvTaskMeta.setText(item.getMeta());

            // Nhắc nhở
            if (imgReminder != null) {
                imgReminder.setVisibility(item.isReminderEnabled() ? View.VISIBLE : View.GONE);
            }

            // Icon trạng thái
            int statusIcon = item.isCompleted() ? R.drawable.ic_check_circle_24 : R.drawable.ic_circle_outline_24;
            int statusTint = item.isCompleted() ? R.color.success : R.color.text_secondary;
            imgStatus.setImageResource(statusIcon);
            imgStatus.setColorFilter(ContextCompat.getColor(itemView.getContext(), statusTint));

            // Dấu chấm ưu tiên
            int priorityDrawable;
            if (item.getPriority() == MainTaskItem.PRIORITY_HIGH) {
                priorityDrawable = R.drawable.bg_priority_dot_red;
            } else if (item.getPriority() == MainTaskItem.PRIORITY_MEDIUM) {
                priorityDrawable = R.drawable.bg_priority_dot_orange;
            } else {
                priorityDrawable = R.drawable.bg_priority_dot_green;
            }

            Drawable background = ContextCompat.getDrawable(itemView.getContext(), priorityDrawable);
            if (viewPriorityDot != null) {
                viewPriorityDot.setBackground(background);
            }
        }
    }
}
