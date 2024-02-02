package com.shuking.pairBackend.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Dateutils {
    /**
     * 字符串转date
     *
     * @param str 时间
     */
    public static Date str2Date(String str, String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        try {
            return dateFormat.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 获取现在时间
     */
    public static String getNowDate(String pattern) {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        return dateFormat.format(date);
    }

}
