package com.hetang.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Danfeng on 2018/4/22.
 */

public class DateUtil {
        private static final long ONE_MINUTE = 60000L;
    private static final long ONE_HOUR = 3600000L;
    private static final long ONE_DAY = 86400000L;
    private static final long ONE_WEEK = 604800000L;

    private static final String ONE_SECOND_AGO = "秒前";
    private static final String ONE_MINUTE_AGO = "分钟前";
    private static final String ONE_HOUR_AGO = "小时前";
    private static final String ONE_DAY_AGO = "天前";
    private static final String ONE_MONTH_AGO = "月前";
    private static final String ONE_YEAR_AGO = "年前";
    
     public static String format(Date date) {
        long delta = new Date().getTime() - date.getTime();
        if (delta < 1L * ONE_MINUTE) {
            long seconds = toSeconds(delta);
            return (seconds <= 0 ? 1 : seconds) + ONE_SECOND_AGO;
        }
        if (delta < 45L * ONE_MINUTE) {
            long minutes = toMinutes(delta);
            return (minutes <= 0 ? 1 : minutes) + ONE_MINUTE_AGO;
        }
         if (delta < 24L * ONE_HOUR) {
            long hours = toHours(delta);
            return (hours <= 0 ? 1 : hours) + ONE_HOUR_AGO;
        }
        if (delta < 48L * ONE_HOUR) {
            return "昨天";
        }
        if (delta < 30L * ONE_DAY) {
            long days = toDays(delta);
            return (days <= 0 ? 1 : days) + ONE_DAY_AGO;
        }
         
         if (delta < 12L * 4L * ONE_WEEK) {
            long months = toMonths(delta);
            return (months <= 0 ? 1 : months) + ONE_MONTH_AGO;
        } else {
            long years = toYears(delta);
            return (years <= 0 ? 1 : years) + ONE_YEAR_AGO;
        }
    }

    private static long toSeconds(long date) {
        return date / 1000L;
    }
    
     private static long toMinutes(long date) {
        return toSeconds(date) / 60L;
    }

    private static long toHours(long date) {
        return toMinutes(date) / 60L;
    }

    private static long toDays(long date) {
        return toHours(date) / 24L;
    }
    
    private static long toMonths(long date) {
        return toDays(date) / 30L;
    }

    private static long toYears(long date) {
        return toMonths(date) / 365L;
    }

    //Calendar 转化 String
    public static  String calendarToStr(Calendar calendar,String format) {

//    Calendar calendat = Calendar.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat(format);

        return sdf.format(calendar.getTime());
    }


    //String 转化Calendar
    public static Calendar strToCalendar(String str,String format) {

//    String str = "2012-5-27";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date date = null;
        Calendar calendar = null;
        try {
            date = sdf.parse(str);
            calendar = Calendar.getInstance();
            calendar.setTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return calendar;
    }


    //    Date 转化String
    public static String dateTostr(Date date,String format) {

        SimpleDateFormat sdf = new SimpleDateFormat(format);
//    String dateStr = sdf.format(new Date());
        String dateStr = sdf.format(date);
        return dateStr;
    }


    //  String 转化Date
    public static Date strToDate(String str,String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date date = null;
        try {
            date = sdf.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }


    //Date 转化Calendar
    public static Calendar dateToCalendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }


    //Calendar转化Date
    public static Date calendarToDate(Calendar calendar) {
        return calendar.getTime();
    }


    // String 转成    Timestamp

    public static Timestamp strToTimeStamp(String str) {

//    Timestamp ts = Timestamp.valueOf("2012-1-14 08:11:00");
        return Timestamp.valueOf(str);
    }


    //Date 转 TimeStamp
    public static Timestamp dateToTimeStamp(Date date,String format) {

        SimpleDateFormat df = new SimpleDateFormat(format);

        String time = df.format(new Date());

        Timestamp ts = Timestamp.valueOf(time);
        return ts;
    }
    
    public static String timeStamp2String(Long created) {
        Date date = new Date(created * 1000);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return simpleDateFormat.format(date);
    }
    
    public static String time2Comparison(Long created){
        Date date = new Date(created * 1000);
        return format(date);
    }
}
