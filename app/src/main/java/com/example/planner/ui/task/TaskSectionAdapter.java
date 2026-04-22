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
        void onTaskStatusChanged(TaskUiModel task);
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
            case TaskUiModel.TYPE_TABLE_HEADER:
            case TaskUiModel.TYPE_ACTION_NEW_PAGE:
            case TaskUiModel.TYPE_ACTION_NEW_GROUP:
            default:
                // Trả về một View trống cho các loại không còn sử dụng để tránh lỗi
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
            
            // Subtitle kết hợp Deadline và Note (ví dụ: Mobile - 5/5)
            String subtitle = item.getDeadline();
            if (item.getNote() != null && !item.getNote().isEmpty()) {
                subtitle = item.getNote() + " - " + subtitle;
            }
            cardHolder.tvSubtitle.setText(subtitle);

            // Cập nhật icon check
            updateCheckIcon(cardHolder.ivCheck, item.isChecked());
            cardHolder.ivCheck.setOnClickListener(v -> {
                item.setChecked(!item.isChecked());
                updateCheckIcon(cardHolder.ivCheck, item.isChecked());
                if (listener != null) listener.onTaskStatusChanged(item);
            });
            
            // Hiển thị màu ưu tiên
            int color;
            switch (item.getPriority() != null ? item.getPriority() : "low") {
                case "high":
                    color = holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_light);
                    break;
                case "medium":
                    color = holder.itemView.getContext().getResources().getColor(android.R.color.holo_orange_light);
                    break;
                case "low":
                default:
                    color = holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_light);
                    break;
            }
            cardHolder.viewPriorityStrip.setBackgroundColor(color);
            if (cardHolder.viewPriorityDot != null) {
                cardHolder.viewPriorityDot.setBackgroundColor(color);
            }
            cardHolder.viewPriorityStrip.setVisibility(View.VISIBLE);
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

    // --- Các ViewHolders ---
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvGroupName;
        HeaderViewHolder(@NonNull View itemView) { 
            super(itemView); 
            tvGroupName = itemView.findViewById(R.id.tvHeaderName);
        }
    }

    static class TaskCardViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle;
        ImageView ivCheck;
        View viewPriorityStrip, viewPriorityDot;
        TaskCardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvSubtitle = itemView.findViewById(R.id.tvTaskSubtitle);
            ivCheck = itemView.findViewById(R.id.ivCheck);
            viewPriorityStrip = itemView.findViewById(R.id.viewPriorityStrip);
            viewPriorityDot = itemView.findViewById(R.id.viewPriorityDot);
        }
    }

    static class EmptyViewHolder extends RecyclerView.ViewHolder {
        EmptyViewHolder(@NonNull View itemView) { super(itemView); }
    }
}
