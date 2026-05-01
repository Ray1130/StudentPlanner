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

    public static long stringToTimestamp(String dateString) {
        try {
            if (dateString == null || dateString.isEmpty() || dateString.equals("Chưa có hạn")) return 0;
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date date = formatter.parse(dateString);
            return date != null ? date.getTime() : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    //  Tính xem còn bao nhiêu ngày nữa là tới deadline
    public static int getDaysRemaining(long dueDateTimestamp) {
        long currentTimestamp = System.currentTimeMillis();
        long diff = dueDateTimestamp - currentTimestamp;

        // Trả về số ngày
        return (int) (diff / (1000 * 60 * 60 * 24));
    }
}
