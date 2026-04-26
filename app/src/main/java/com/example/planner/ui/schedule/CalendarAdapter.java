package com.example.planner.ui.schedule;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.planner.R;
import java.time.LocalDate;
import java.util.ArrayList;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    private final ArrayList<LocalDate> days;
    private final OnItemListener onItemListener;
    private LocalDate selectedDate;
    private java.util.Map<LocalDate, CalendarTaskInfo> taskData = new java.util.HashMap<>();

    public CalendarAdapter(ArrayList<LocalDate> days, LocalDate selectedDate, OnItemListener onItemListener) {
        this.days = days;
        this.selectedDate = selectedDate;
        this.onItemListener = onItemListener;
    }

    public void updateSelectedDate(LocalDate date) {
        this.selectedDate = date;
        notifyDataSetChanged();
    }

    public void setTaskData(java.util.Map<LocalDate, CalendarTaskInfo> data) {
        this.taskData = data;
        notifyDataSetChanged();
    }

    public void updateDays(ArrayList<LocalDate> newDays, LocalDate selectedDate) {
        this.days.clear();
        this.days.addAll(newDays);
        this.selectedDate = selectedDate;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_cell, parent, false);
        return new CalendarViewHolder(view, onItemListener, days);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        LocalDate date = days.get(position);

        if (date == null) {
            holder.tvDayOfMonth.setText("");
            holder.dotIndicator.setVisibility(View.GONE);
        } else {
            holder.tvDayOfMonth.setText(String.valueOf(date.getDayOfMonth()));

            // RESET các trạng thái mặc định
            holder.tvDayOfMonth.setBackground(null);
            holder.dotIndicator.setVisibility(View.GONE);

            // LOGIC CHÍNH: Kiểm tra ngày thuộc tháng hiện tại hay tháng trước/sau
            if (date.getMonthValue() != selectedDate.getMonthValue()) {
                holder.tvDayOfMonth.setTextColor(Color.LTGRAY);
            } else {
                holder.tvDayOfMonth.setTextColor(Color.BLACK);

                // Highlight ngày hôm nay
                if (date.equals(LocalDate.now())) {
                    holder.tvDayOfMonth.setBackgroundResource(R.drawable.bg_circle_purple);
                    holder.tvDayOfMonth.setTextColor(Color.WHITE);
                }
            }

            // Hiển thị chấm tím nếu ngày đó có task
            if (taskData != null && taskData.containsKey(date)) {
                holder.dotIndicator.setVisibility(View.VISIBLE);
            }
        }
    }


    @Override
    public int getItemCount() {
        return days.size();
    }

    public interface OnItemListener {
        void onItemClick(int position, LocalDate date);
    }

    public static class CalendarViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView tvDayOfMonth;
        private final View dotIndicator;
        private final OnItemListener onItemListener;
        private final ArrayList<LocalDate> days;

        public CalendarViewHolder(@NonNull View itemView, OnItemListener onItemListener, ArrayList<LocalDate> days) {
            super(itemView);
            tvDayOfMonth = itemView.findViewById(R.id.tvDayOfMonth);
            dotIndicator = itemView.findViewById(R.id.dotIndicator);
            this.onItemListener = onItemListener;
            this.days = days;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemListener.onItemClick(getAdapterPosition(), days.get(getAdapterPosition()));
        }
    }
}