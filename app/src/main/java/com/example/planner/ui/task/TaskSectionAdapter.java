package com.example.planner.ui.task;

import android.graphics.Paint;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.tvGroupName.setText(item.getTitle());
            if (headerHolder.ivHeaderAdd != null) {
                headerHolder.ivHeaderAdd.setVisibility(View.GONE);
            }
        } else if (holder instanceof TaskCardViewHolder) {
            TaskCardViewHolder cardHolder = (TaskCardViewHolder) holder;
            cardHolder.tvTitle.setText(item.getTitle());
            
            // Handle completed state: strike-through and dimming
            if (item.isChecked()) {
                cardHolder.tvTitle.setPaintFlags(cardHolder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                cardHolder.itemView.setAlpha(0.6f);
            } else {
                cardHolder.tvTitle.setPaintFlags(cardHolder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                cardHolder.itemView.setAlpha(1.0f);
            }

            if (item.isHideTag()) {
                if (cardHolder.tvTag != null) cardHolder.tvTag.setVisibility(View.GONE);
                cardHolder.tvSubtitle.setVisibility(View.VISIBLE);
                
                String subtitle = item.getSubtitle();
                cardHolder.tvSubtitle.setText(subtitle != null ? subtitle : "");
            } else {
                if (cardHolder.tvTag != null) cardHolder.tvTag.setVisibility(View.VISIBLE);
                cardHolder.tvSubtitle.setVisibility(View.VISIBLE);
                
                String subtitle = item.getSubtitle();
                if (subtitle != null && subtitle.contains(" • ")) {
                    int dotIndex = subtitle.indexOf(" • ");
                    SpannableString spannable = new SpannableString(subtitle);
                    spannable.setSpan(new ForegroundColorSpan(cardHolder.itemView.getContext().getColor(R.color.primary_purple)),
                            dotIndex + 1, dotIndex + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    cardHolder.tvSubtitle.setText(spannable);
                } else {
                    cardHolder.tvSubtitle.setText(subtitle != null ? subtitle : "");
                }

                // Phân loại dựa trên Subject hoặc Category
                if (cardHolder.tvTag != null) {
                    if (item.getSubjectId() > 0) {
                        cardHolder.tvTag.setText("Học phần");
                        cardHolder.tvTag.setBackgroundResource(R.drawable.bg_tag_course);
                        cardHolder.tvTag.setTextColor(cardHolder.itemView.getContext().getColor(R.color.tag_course_text));
                        if (cardHolder.ivBell != null)
                            cardHolder.ivBell.setVisibility(item.isReminderEnabled() ? View.VISIBLE : View.GONE);
                    } else {
                        cardHolder.tvTag.setText("Ngoại khóa");
                        cardHolder.tvTag.setBackgroundResource(R.drawable.bg_tag_extracurricular);
                        cardHolder.tvTag.setTextColor(cardHolder.itemView.getContext().getColor(R.color.tag_extracurricular_text));
                        if (cardHolder.ivBell != null)
                            cardHolder.ivBell.setVisibility(View.GONE);
                    }
                }
            }
            
            if (cardHolder.tvTime != null) {
                cardHolder.tvTime.setText(item.getDeadline());
                // Highlight red if overdue
                try {
                    // Giả định getDeadline() trả về chuỗi có thể so sánh hoặc đã format
                    // Logic đơn giản: nếu là "Hôm nay" hoặc quá hạn thì màu đỏ
                    cardHolder.tvTime.setTextColor(cardHolder.itemView.getContext().getColor(R.color.danger));
                } catch (Exception e) {
                    cardHolder.tvTime.setTextColor(cardHolder.itemView.getContext().getColor(R.color.text_secondary));
                }
            }
            
            updateCheckIcon(cardHolder.ivCheck, item.isChecked());
            cardHolder.ivCheck.setOnClickListener(v -> {
                // 1. Phản hồi UI ngay lập tức
                boolean newCheckedState = !item.isChecked();
                item.setChecked(newCheckedState);
                updateCheckIcon(cardHolder.ivCheck, newCheckedState);
                
                // 2. Thông báo cho ViewModel để lưu vào DB
                if (listener != null) listener.onTaskStatusChanged(item);
            });

            cardHolder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onTaskClick(item);
            });

            cardHolder.itemView.setOnLongClickListener(v -> {
                if (listener != null) listener.onTaskLongClick(item);
                return true;
            });

            // Xử lý Task đã quá hạn
            boolean isExpired = item.getExpiryTimestamp() > 0 && item.getExpiryTimestamp() <= System.currentTimeMillis();
            if (isExpired) {
                cardHolder.tvTitle.setAlpha(0.5f);
                if (cardHolder.tvStatus != null) {
                    cardHolder.tvStatus.setVisibility(View.VISIBLE);
                    cardHolder.tvStatus.setText("Đã hết hạn");
                    cardHolder.tvStatus.setTextColor(holder.itemView.getContext().getColor(R.color.danger));
                }
            } else {
                cardHolder.tvTitle.setAlpha(1.0f);
                if (cardHolder.tvStatus != null) {
                    cardHolder.tvStatus.setVisibility(View.GONE);
                }
            }
            
            // Dấu chấm ưu tiên và Gạch màu
            int priorityColor;
            String priority = item.getPriority() != null ? item.getPriority().toLowerCase() : "low";
            switch (priority) {
                case "high":
                    priorityColor = holder.itemView.getContext().getColor(R.color.priority_high);
                    break;
                case "medium":
                    priorityColor = holder.itemView.getContext().getColor(R.color.priority_medium);
                    break;
                case "low":
                default:
                    priorityColor = holder.itemView.getContext().getColor(R.color.priority_low);
                    break;
            }
            
            if (cardHolder.viewPriorityStrip != null) {
                cardHolder.viewPriorityStrip.setBackgroundColor(priorityColor);
            }
        }
    }

    private void updateCheckIcon(ImageView imageView, boolean isChecked) {
        int iconRes = isChecked ? R.drawable.ic_check_circle_24 : R.drawable.ic_circle_outline_24;
        imageView.setImageResource(iconRes);
        int tintColor = isChecked ? R.color.success : R.color.text_secondary;
        imageView.setColorFilter(ContextCompat.getColor(imageView.getContext(), tintColor));
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
        ImageView ivHeaderAdd;
        HeaderViewHolder(@NonNull View itemView) { 
            super(itemView); 
            tvGroupName = itemView.findViewById(R.id.tvHeaderName);
            ivHeaderAdd = itemView.findViewById(R.id.ivHeaderAdd);
        }
    }

    static class TaskCardViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle, tvTag, tvTime, tvStatus;
        ImageView ivCheck, ivBell;
        View viewPriorityStrip;
        TaskCardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvSubtitle = itemView.findViewById(R.id.tvTaskSubtitle);
            ivCheck = itemView.findViewById(R.id.ivCheck);
            ivBell = itemView.findViewById(R.id.ivBell);
            viewPriorityStrip = itemView.findViewById(R.id.viewPriorityStrip);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            
            // UI MOCKUP - DELETE WHEN INTEGRATING REAL LOGIC
            tvTag = itemView.findViewById(R.id.tvTag);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }

    static class EmptyViewHolder extends RecyclerView.ViewHolder {
        EmptyViewHolder(@NonNull View itemView) { super(itemView); }
    }
}
