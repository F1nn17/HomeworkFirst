package com.shiraku.weatherconsumer.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateFormatter {
    public static String formatTimestamp(int[] timestamp) {
        int year = timestamp[0];
        int month = timestamp[1];
        int day = timestamp[2];
        int hour = timestamp[3];
        int minute = timestamp[4];
        int second = timestamp[5];

        LocalDateTime dateTime = LocalDateTime.of(year, month, day, hour, minute, second);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm:ss");
        return dateTime.format(formatter);
    }
}
