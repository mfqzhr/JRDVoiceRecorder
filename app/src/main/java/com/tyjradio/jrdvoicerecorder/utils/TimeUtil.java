package com.tyjradio.jrdvoicerecorder.utils;

import java.text.SimpleDateFormat;

import java.util.Date;

public class TimeUtil {


    /**
     * 把当前的时间转成年月日时分秒的格式
     * @param startRecorderTime
     * @return
     */

    public static String getNowTime(long startRecorderTime) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(startRecorderTime);
        String retStrFormatNowDate = formatter.format(date);
        return retStrFormatNowDate;
    }


}
