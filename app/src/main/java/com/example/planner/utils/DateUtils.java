package com.example.planner.utils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
public class DateUtils {
    public static String timestampToString(long timestamp) {
        if (timestamp == 0) return "Chưa có hạn";
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return formatter.format(new Date(timestamp));
    }

    //  Tính xem còn bao nhiêu ngày nữa là tới deadline
    public static int getDaysRemaining(long dueDateTimestamp) {
        long currentTimestamp = System.currentTimeMillis();
        long diff = dueDateTimestamp - currentTimestamp;

        // Trả về số ngày (đổi từ milliseconds sang ngày)
        return (int) (diff / (1000 * 60 * 60 * 24));
    }
}
