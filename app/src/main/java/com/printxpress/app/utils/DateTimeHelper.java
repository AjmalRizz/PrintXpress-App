package com.printxpress.app.utils;

import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTimeHelper {

    public static String getTimeAgo(Timestamp timestamp) {
        if (timestamp == null) return "Just now";
        
        long diff = Timestamp.now().getSeconds() - timestamp.getSeconds();
        if (diff < 0) diff = 0; // Guard against minor time drift
        
        if (diff < 60) {
            return "Just now";
        } else if (diff < 3600) {
            long mins = diff / 60;
            return mins + (mins == 1 ? " minute ago" : " minutes ago");
        } else if (diff < 86400) {
            long hours = diff / 3600;
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        } else {
            long days = diff / 86400;
            return days + (days == 1 ? " day ago" : " days ago");
        }
    }

    public static String formatDate(Timestamp timestamp) {
        if (timestamp == null) return "";
        Date date = timestamp.toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        return sdf.format(date);
    }

    public static Timestamp getCurrentTimestamp() {
        return Timestamp.now();
    }
}
