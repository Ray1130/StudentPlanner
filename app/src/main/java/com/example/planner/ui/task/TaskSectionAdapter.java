package com.example.planner.ui.task;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.planner.R;
import java.util.List;

public class TaskSectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnTaskActionListener {
        void onAddNewTask();
        void onAddNewGroup();
        void onTaskClick(TaskUiModel task);
        void onTaskStatusChanged(TaskUiModel task);
        void onTaskLongClick(TaskUiModel task);
    }

    private List<TaskUiModel> items;
    private OnTaskActionListener listener;

    public TaskSectionAdapter(List<TaskUiModel> items, OnTaskActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getViewType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TaskUiModel.TYPE_GROUP_HEADER:
                return new HeaderViewHolder(inflater.inflate(R.layout.item_task_group_header, parent, false));
            case TaskUiModel.TYPE_TABLE_ROW:
                return new TaskCardViewHolder(inflater.inflate(R.layout.item_task_card, parent, false));
            default:
                return new EmptyViewHolder(new View(parent.getContext()));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TaskUiModel item = items.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).tvGroupName.setText(item.getTitle());
        } else if (holder instanceof TaskCardViewHolder) {
            TaskCardViewHolder cardHolder = (TaskCardViewHolder) holder;
            cardHolder.tvTitle.setText(item.getTitle());
            
            String subtitle = item.getDeadline();
            if (item.getNote() != null && !item.getNote().isEmpty()) {
                subtitle = item.getNote() + " - " + subtitle;
            }
            cardHolder.tvSubtitle.setText(subtitle);

            if (cardHolder.ivBell != null) {
                cardHolder.ivBell.setVisibility(item.isReminderEnabled() ? View.VISIBLE : View.GONE);
            }

            updateCheckIcon(cardHolder.ivCheck, item.isChecked());
            cardHolder.ivCheck.setOnClickListener(v -> {
                item.setChecked(!item.isChecked());
                updateCheckIcon(cardHolder.ivCheck, item.isChecked());
                if (listener != null) listener.onTaskStatusChanged(item);
            });

            cardHolder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onTaskClick(item);
            });

            cardHolder.itemView.setOnLongClickListener(v -> {
                if (listener != null) listener.onTaskLongClick(item);
                return true;
            });
            
            // Dấu chấm ưu tiên và Gạch màu
            int priorityDrawable;
            int priorityColor;
            String priority = item.getPriority() != null ? item.getPriority().toLowerCase() : "low";
            switch (priority) {
                case "high":
                    priorityDrawable = R.drawable.bg_priority_dot_red;
                    priorityColor = holder.itemView.getContext().getColor(R.color.priority_high);
                    break;
                case "medium":
                    priorityDrawable = R.drawable.bg_priority_dot_orange;
                    priorityColor = holder.itemView.getContext().getColor(R.color.priority_medium);
                    break;
                case "low":
                default:
                    priorityDrawable = R.drawable.bg_priority_dot_green;
                    priorityColor = holder.itemView.getContext().getColor(R.color.priority_low);
                    break;
            }
            
            if (cardHolder.viewPriorityDot != null) {
                cardHolder.viewPriorityDot.setBackgroundResource(priorityDrawable);
            }
            if (cardHolder.viewPriorityStrip != null) {
                cardHolder.viewPriorityStrip.setBackgroundColor(priorityColor);
            }
        }
    }

    private void updateCheckIcon(ImageView imageView, boolean isChecked) {
        int iconRes = isChecked ? R.drawable.ic_check_circle_24 : R.drawable.ic_radio_unchecked;
        imageView.setImageResource(iconRes);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public void updateData(List<TaskUiModel> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvGroupName;
        HeaderViewHolder(@NonNull View itemView) { 
            super(itemView); 
            tvGroupName = itemView.findViewById(R.id.tvHeaderName);
        }
    }

    static class TaskCardViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle;
        ImageView ivCheck, ivBell;
        View viewPriorityDot, viewPriorityStrip;
        TaskCardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvSubtitle = itemView.findViewById(R.id.tvTaskSubtitle);
            ivCheck = itemView.findViewById(R.id.ivCheck);
            ivBell = itemView.findViewById(R.id.ivBell);
            viewPriorityDot = itemView.findViewById(R.id.viewPriorityDot);
            viewPriorityStrip = itemView.findViewById(R.id.viewPriorityStrip);
        }
    }

    static class EmptyViewHolder extends RecyclerView.ViewHolder {
        EmptyViewHolder(@NonNull View itemView) { super(itemView); }
    }
}
