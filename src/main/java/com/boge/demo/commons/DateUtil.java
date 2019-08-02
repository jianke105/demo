package com.boge.demo.commons;


import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {


    //获取现在日期时间
    public static Date getNow() {
        Date datenow = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = dateFormat.format(datenow);
        Date date = Str2Date(dateStr);

        return date;
    }

    //将Date类型转成String类型
    public static String Date2Str(Date date) {
        // Date now=new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = dateFormat.format(date);
        return dateStr;
    }


    //将Date类型转成String类型
    public static String Date2Str2(Date date) {
        // Date now=new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = dateFormat.format(date);
        return dateStr;
    }


    //将String类型转成Date类型
    // String dateStr = "2014-02-02 17:02:12";
    public static Date Str2Date(String dateStr) {

        Date date = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            date = sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;

    }

    public static long getDateSpace(String begindate, String enddate) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");

        long daysBetween = 0;
        try {
            Date d1 = sdf.parse(begindate);
            Date d2 = sdf.parse(enddate);
            daysBetween = (d2.getTime() - d1.getTime() + 1000000) / (60 * 60 * 24 * 1000);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return daysBetween;
    }

    //算出两个天数差，yyyy-MM-dd HH:mm:ss
    public static long getDateTimeSpace(String begindate, String enddate) {
        long day = 0;

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        long l = 0;
        try {
            java.util.Date nowtime = df.parse(enddate);
            java.util.Date date = df.parse(begindate);
            l = nowtime.getTime() - date.getTime();
            day = l / (24 * 60 * 60 * 1000);
            long hour = (l / (60 * 60 * 1000) - day * 24);
            long min = ((l / (60 * 1000)) - day * 24 * 60 - hour * 60) + hour * 60;
            long s = (l / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);

            // String timelong = hour+":"+min+":"+s;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return day;
    }


    //获取当前日期
    public static Date getNowDateShort() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = formatter.format(currentTime);

        Date currentTime_2 = strToDate(dateString);
        return currentTime_2;
    }

    //讲短时间格式转成Date格式
    public static Date strToDate(String strDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        ParsePosition pos = new ParsePosition(0);
        Date strtodate = formatter.parse(strDate, pos);
        return strtodate;
    }
}
