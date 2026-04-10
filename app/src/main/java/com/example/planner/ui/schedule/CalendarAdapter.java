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
    private final LocalDate selectedDate;

    public CalendarAdapter(ArrayList<LocalDate> days, LocalDate selectedDate, OnItemListener onItemListener) {
        this.days = days;
        this.selectedDate = selectedDate;
        this.onItemListener = onItemListener;
    }


    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_cell, parent, false);

        // Ép chiều cao mỗi ô là một con số cụ thể nếu nó không hiện (ví dụ 150px)
        // Hoặc tính toán dựa trên chiều cao RecyclerView
        int parentHeight = parent.getHeight();
        if (parentHeight <= 0) {
            parentHeight = 800; // Giá trị mặc định nếu chưa đo được height
        }
        view.getLayoutParams().height = parentHeight / 6;

        return new CalendarViewHolder(view, onItemListener, days);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        LocalDate date = days.get(position);

        if (date == null) {
            holder.tvDayOfMonth.setText("");
        } else {
            holder.tvDayOfMonth.setText(String.valueOf(date.getDayOfMonth()));

            // RESET các trạng thái mặc định để tránh lỗi khi RecyclerView tái sử dụng (Recycle) view
            holder.tvDayOfMonth.setBackground(null);
            holder.tvTaskNote.setVisibility(View.GONE);

            // LOGIC CHÍNH: Kiểm tra ngày thuộc tháng hiện tại hay tháng trước/sau
            if (date.getMonthValue() != selectedDate.getMonthValue()) {
                // Nếu KHÔNG thuộc tháng hiện tại -> Làm mờ
                holder.tvDayOfMonth.setTextColor(Color.LTGRAY);
            } else {
                // Nếu THUỘC tháng hiện tại -> Để màu đen hoặc highlight
                holder.tvDayOfMonth.setTextColor(Color.BLACK);

                // Highlight ngày hôm nay (Vòng tròn tím)
                if (date.equals(LocalDate.now())) {
                    holder.tvDayOfMonth.setBackgroundResource(R.drawable.bg_circle_purple);
                    holder.tvDayOfMonth.setTextColor(Color.WHITE);
                }
            }

            // Logic hiển thị task dummy (Giữ nguyên hoặc tối ưu theo tháng)
            if (date.getDayOfMonth() == 30 && date.getMonthValue() == 3) {
                holder.tvTaskNote.setVisibility(View.VISIBLE);
                holder.tvTaskNote.setText("Training");
                holder.tvTaskNote.setTextColor(Color.parseColor("#E9C46A"));
            }

            if (date.getDayOfMonth() == 2 && date.getMonthValue() == 4) {
                holder.tvTaskNote.setVisibility(View.VISIBLE);
                holder.tvTaskNote.setText("Des fig");
                holder.tvTaskNote.setTextColor(Color.parseColor("#4CAF50"));
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
        private final TextView tvTaskNote;
        private final OnItemListener onItemListener;
        private final ArrayList<LocalDate> days;

        public CalendarViewHolder(@NonNull View itemView, OnItemListener onItemListener, ArrayList<LocalDate> days) {
            super(itemView);
            tvDayOfMonth = itemView.findViewById(R.id.tvDayOfMonth);
            tvTaskNote = itemView.findViewById(R.id.tvTaskNote);
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