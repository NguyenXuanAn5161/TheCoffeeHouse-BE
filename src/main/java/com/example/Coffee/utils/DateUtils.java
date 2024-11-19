package com.example.Coffee.utils;

import java.util.Calendar;
import java.util.Date;

public class DateUtils {
    public static Date addHoursToDate(Date date, int hours) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR, hours);
        return calendar.getTime();
    }
}