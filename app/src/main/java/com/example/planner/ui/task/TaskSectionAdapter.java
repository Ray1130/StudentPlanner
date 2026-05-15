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
            
            // UI MOCKUP - DELETE WHEN INTEGRATING REAL LOGIC
            String subtitleTemplate = item.getNote() != null && !item.getNote().isEmpty() ? item.getNote() : "Kinh tế vĩ mô • KTN201";
            if (subtitleTemplate.contains(" • ")) {
                int dotIndex = subtitleTemplate.indexOf(" • ");
                android.text.SpannableString spannable = new android.text.SpannableString(subtitleTemplate);
                spannable.setSpan(new android.text.style.ForegroundColorSpan(cardHolder.itemView.getContext().getColor(R.color.primary_purple)),
                        dotIndex + 1, dotIndex + 2, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                cardHolder.tvSubtitle.setText(spannable);
            } else {
                cardHolder.tvSubtitle.setText(subtitleTemplate);
            }
            
            if (cardHolder.tvTag != null) {
                // Giả lập thẻ Tag: Học phần vs Ngoại khoá 
                if ("high".equalsIgnoreCase(item.getPriority())) {
                    cardHolder.tvTag.setText("Ngoại khoá");
                    cardHolder.tvTag.setBackgroundResource(R.drawable.bg_tag_extracurricular);
                    cardHolder.tvTag.setTextColor(cardHolder.itemView.getContext().getColor(R.color.tag_extracurricular_text));
                } else {
                    cardHolder.tvTag.setText("Học phần");
                    cardHolder.tvTag.setBackgroundResource(R.drawable.bg_tag_course);
                    cardHolder.tvTag.setTextColor(cardHolder.itemView.getContext().getColor(R.color.tag_course_text));
                }
                
                // Logic hiển thị icon theo Tag
                if ("Học phần".equals(cardHolder.tvTag.getText().toString())) {
                    if (cardHolder.ivBell != null) cardHolder.ivBell.setVisibility(View.VISIBLE);
                    if (cardHolder.ivLocation != null) cardHolder.ivLocation.setVisibility(View.GONE);
                } else {
                    if (cardHolder.ivBell != null) cardHolder.ivBell.setVisibility(View.GONE);
                    if (cardHolder.ivLocation != null) cardHolder.ivLocation.setVisibility(View.VISIBLE);
                }
            }
            
            if (cardHolder.tvTime != null) {
                String time = item.getDeadline();
                cardHolder.tvTime.setText(time != null && !time.isEmpty() ? time : "Hôm nay, 17:00");
                cardHolder.tvTime.setTextColor(cardHolder.itemView.getContext().getColor(R.color.danger));
            }
            
            if (cardHolder.tvCommentCount != null) {
                // Hardcode comment count
                cardHolder.tvCommentCount.setText(String.valueOf((int)(Math.random() * 5) + 1));
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
            
            // Dấu chấm ưu tiên và Gạch màu
            int priorityDrawable;
            int priorityColor;
            String priorityText; // UI MOCKUP
            String priority = item.getPriority() != null ? item.getPriority().toLowerCase() : "low";
            switch (priority) {
                case "high":
                    priorityDrawable = R.drawable.bg_priority_dot_red;
                    priorityColor = holder.itemView.getContext().getColor(R.color.priority_high);
                    priorityText = "Cao";
                    break;
                case "medium":
                    priorityDrawable = R.drawable.bg_priority_dot_orange;
                    priorityColor = holder.itemView.getContext().getColor(R.color.priority_medium);
                    priorityText = "Sắp đến hạn";
                    break;
                case "low":
                default:
                    priorityDrawable = R.drawable.bg_priority_dot_green;
                    priorityColor = holder.itemView.getContext().getColor(R.color.priority_low);
                    priorityText = "Đã lên lịch";
                    break;
            }
            
            if (cardHolder.viewPriorityDot != null) {
                cardHolder.viewPriorityDot.setBackgroundResource(priorityDrawable);
            }
            if (cardHolder.viewPriorityStrip != null) {
                cardHolder.viewPriorityStrip.setBackgroundColor(priorityColor); // Deprecated in UI but safely keep
            }

            // UI MOCKUP - DELETE WHEN INTEGRATING REAL LOGIC
            if (cardHolder.tvStatus != null) {
                cardHolder.tvStatus.setText(priorityText);
                cardHolder.tvStatus.setTextColor(priorityColor);
            }
        }
    }

    private void updateCheckIcon(ImageView imageView, boolean isChecked) {
        int iconRes = isChecked ? R.drawable.ic_check_circle_24 : R.drawable.ic_circle_outline_24;
        imageView.setImageResource(iconRes);
        int tintColor = isChecked ? R.color.success : R.color.text_secondary;
        imageView.setColorFilter(androidx.core.content.ContextCompat.getColor(imageView.getContext(), tintColor));
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
        TextView tvTitle, tvSubtitle, tvTag, tvTime, tvStatus, tvCommentCount;
        ImageView ivCheck, ivBell, ivLocation;
        View viewPriorityDot, viewPriorityStrip;
        TaskCardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvSubtitle = itemView.findViewById(R.id.tvTaskSubtitle);
            ivCheck = itemView.findViewById(R.id.ivCheck);
            ivBell = itemView.findViewById(R.id.ivBell);
            ivLocation = itemView.findViewById(R.id.ivLocation);
            viewPriorityDot = itemView.findViewById(R.id.viewPriorityDot);
            viewPriorityStrip = itemView.findViewById(R.id.viewPriorityStrip);
            
            // UI MOCKUP - DELETE WHEN INTEGRATING REAL LOGIC
            tvTag = itemView.findViewById(R.id.tvTag);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvCommentCount = itemView.findViewById(R.id.tvCommentCount);
        }
    }

    static class EmptyViewHolder extends RecyclerView.ViewHolder {
        EmptyViewHolder(@NonNull View itemView) { super(itemView); }
    }
}
