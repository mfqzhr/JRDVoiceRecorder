package com.tyjradio.jrdvoicerecorder.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.os.Environment;

/**
 * 日志输出类,输出日志至文件中
 *
 * @author yzy
 *
 */

public class LogUtils {
    public static String LOG_PATH = "";
    public static String LOG_NAME = "/debug.log";
    private static String INFO = "info";
    private static String DEBUG = "debug";
    private static String WARN = "warn";
    private static String VERBOSE = "verbose";
    private static String ERROR = "error";
    private static final boolean isDebug = true ;
    static {
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            LOG_PATH = Environment.getExternalStorageDirectory() + "/LogUtils" + "/.log" ;
        }else{
            LOG_PATH = "/LogUtils" + "/.log" ;
        }
    }

    /**
     * 初始化保存日志的文件
     * @param path
     * @param name
     */
    public static void init(String path,String name){
        LOG_PATH = path ;
        LOG_NAME = name ;
    }

    public static void i(String content) {
        saveLog(INFO, content);
    }

    public static void v(String content) {
        saveLog(VERBOSE, content);
    }

    public static void d(String content) {
        saveLog(DEBUG, content);
    }

    public static void e(String content) {
        saveLog(ERROR, content);
    }

    public static void w(String content) {
        saveLog(WARN, content);
    }

    @SuppressLint("SimpleDateFormat")
    public static void saveLog(String type, String content) {
        if(isDebug){//当当前是debug时,才进入保存日志操作
            appendToFile_Third(LOG_PATH + LOG_NAME, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(new Date(
                    System.currentTimeMillis())) + "(" + type + "):" + content + "\r\n");
        }
    }

    /**
     * 第一种方式追加文本至文件中(随机文件流追加)
     *
     * @param filePath
     * @param content
     */
    public static void appendToFile_One(String filePath, String content) {
        RandomAccessFile ranFile = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            ranFile = new RandomAccessFile(file, "rw");
            long fileLength = ranFile.length();// 获取文件文本的长度
            ranFile.seek(fileLength); // 设置游标至文件的尾部
            ranFile.writeBytes(content);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ranFile != null) {
                    ranFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 第二种方式追加文本至文件中(BufferedWriter追加)
     *
     * @param filePath
     * @param content
     */
    public static void appendToFile_Two(String filePath, String content) {
        BufferedWriter bufferedWriter = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file)));
            bufferedWriter.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 第三种方式追加文本至文件中(BufferedWriter追加)
     *
     * @param filePath
     * @param content
     */
    public static void appendToFile_Third(String filePath, String content) {
        FileWriter fileWriter = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            fileWriter = new FileWriter(file, true);// true表示附加
            fileWriter.append(content);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
