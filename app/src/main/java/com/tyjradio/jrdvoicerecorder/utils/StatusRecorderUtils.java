package com.tyjradio.jrdvoicerecorder.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;


public class StatusRecorderUtils {

    private static String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/recorderstatus/";
    private static String fileName = "OperationRecorder.txt";

    public static File getFile() {

        return makeFilePath(filePath, fileName);

    }

    public static void writeRecorderEnable(byte tag) {

        if (tag == 1) {
            String enableRecorder = TimeUtil.getNowTime(System.currentTimeMillis()) +
                    " 窄带开启录音失败";
            writeTxtToFile(enableRecorder);
        }
        if (tag == 0) {
            String enableRecorder = TimeUtil.getNowTime(System.currentTimeMillis()) +
                    " 窄带开启录音成功";
            writeTxtToFile(enableRecorder);
        }

    }

    public static void writeShutdownLog() {


        String shutdownLog = TimeUtil.getNowTime(System.currentTimeMillis()) +
                " 关机";
        writeTxtToFile(shutdownLog);


    }

    public static void writeData(byte[] inforParam) {

        if (inforParam[7] == 1) //开机
        {
            File fileLog = makeFilePath(filePath, fileName);
            try {
                RandomAccessFile accessFile = new RandomAccessFile(fileLog, "rw");
                if (accessFile.length() == 0) {
                    String initData = "**************************" + "\r\n" +
                            "无线列调对讲设备记录文件" + "\r\n" +
                            "设备序号: " + new String(Utils.subByte(inforParam, 40, 16)) + "\r\n" +
                            "生产厂家: " + new String(Utils.subByte(inforParam, 8, 16)) + "\r\n" +
                            "设备版本: " + new String(Utils.subByte(inforParam, 24, 32)) + "\r\n" +
                            "读取时间: " + TimeUtil.getNowTime(System.currentTimeMillis()) + "\r\n" +
                            "**************************" + "\r\n" +
                            TimeUtil.getNowTime(System.currentTimeMillis()) + " 开机" + "\r\n";
                    accessFile.write(initData.getBytes());
                } else {

                    accessFile.seek(0);
                    String firstLine = accessFile.readLine();
                    if (firstLine.equals("**************************")) {
                        accessFile.seek(accessFile.length());
                        String strStartLog = TimeUtil.getNowTime(System.currentTimeMillis()) + " 开机" + "\r\n";
                        accessFile.write(strStartLog.getBytes());
                    } else {

                        String initData = "**************************" + "\r\n" +
                                "无线列调对讲设备记录文件" + "\r\n" +
                                "设备序号: " + new String(Utils.subByte(inforParam, 40, 16)) + "\r\n" +
                                "生产厂家: " + new String(Utils.subByte(inforParam, 8, 16)) + "\r\n" +
                                "设备版本: " + new String(Utils.subByte(inforParam, 24, 32)) + "\r\n" +
                                "读取时间: " + TimeUtil.getNowTime(System.currentTimeMillis()) + "\r\n" +
                                "**************************" + "\r\n";

                        insertIntoFile(accessFile,initData.getBytes().length);
                        accessFile.seek(0);
                        accessFile.write(initData.getBytes());
                        accessFile.seek(accessFile.length());
                        String initData2 = TimeUtil.getNowTime(System.currentTimeMillis()) + " 开机" + "\r\n";
                        accessFile.write(initData2.getBytes());

                    }
                }
                accessFile.close();
            } catch (Exception e) {
                Log.e("TestFile", "Error on write File:" + e);
            }

        } else if (inforParam[7] == 3)  //状态
        {
            String data = TimeUtil.getNowTime(System.currentTimeMillis()) +
                    " 状态: 区域号" + String.valueOf(inforParam[8]) + "," +
                    " 信道号" + String.valueOf(inforParam[9]) + "," +
                    " 电量" + String.valueOf(inforParam[10]) + "%";
            writeTxtToFile(data);

        } else if (inforParam[7] == 6)  //改变信道
        {
            String data = TimeUtil.getNowTime(System.currentTimeMillis()) +
                    " 改变信道, " + String.valueOf(inforParam[8]) + "->" +
                    String.valueOf(inforParam[9]);
            writeTxtToFile(data);

        } else if (inforParam[7] == 7)  //发起呼叫
        {
            if (inforParam[8] == 0) {
                String data = TimeUtil.getNowTime(System.currentTimeMillis()) +
                        " 发起呼叫，平原机车";
                writeTxtToFile(data);
            }
            if (inforParam[8] == 1) {
                String data = TimeUtil.getNowTime(System.currentTimeMillis()) +
                        " 发起呼叫，山区机车";
                writeTxtToFile(data);
            }
            if (inforParam[8] == 2) {
                String data = TimeUtil.getNowTime(System.currentTimeMillis()) +
                        " 发起呼叫，平原车站";
                writeTxtToFile(data);
            }
            if (inforParam[8] == 3) {
                String data = TimeUtil.getNowTime(System.currentTimeMillis()) +
                        " 发起呼叫，山区车站";
                writeTxtToFile(data);
            }
            if (inforParam[8] == 4) {
                String data = TimeUtil.getNowTime(System.currentTimeMillis()) +
                        " 发起呼叫，PTT呼叫开始";
                writeTxtToFile(data);

            }
            if (inforParam[8] == 5) {
                String data = TimeUtil.getNowTime(System.currentTimeMillis()) +
                        " 发起呼叫，PTT呼叫结束";
                writeTxtToFile(data);

            }

        } else if (inforParam[7] == 8)  //改变音量
        {
            String data = TimeUtil.getNowTime(System.currentTimeMillis()) +
                    " 改变音量, " + String.valueOf(inforParam[8]) + "->" +
                    String.valueOf(inforParam[9]);
            writeTxtToFile(data);

        } else if (inforParam[7] == 10)  //低电量报警
        {
            String data = TimeUtil.getNowTime(System.currentTimeMillis()) +
                    " 低电量报警, 电量" + String.valueOf(inforParam[8]) + "%";
            writeTxtToFile(data);
        } else if (inforParam[7] == 11)  //开关窄带网络
        {
            if (inforParam[8] == 1) {
                String data = TimeUtil.getNowTime(System.currentTimeMillis()) +
                        " 开启窄带网络";
                writeTxtToFile(data);

            }
            if (inforParam[8] == 0) {
                String data = TimeUtil.getNowTime(System.currentTimeMillis()) +
                        " 关闭窄带网络";
                writeTxtToFile(data);

            }

        }


    }


    // 将字符串写入到文本文件中
    private static void writeTxtToFile(String strcontent) {
        //生成文件夹之后，再生成文件，不然会出错
        makeFilePath(filePath, fileName);
        String strFilePath = filePath + fileName;
        // 每次写入时，都换行写
        String strContent = strcontent + "\r\n";
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                Log.d("TestFile", "Create the file:" + strFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("TestFile", "Error on write File:" + e);
        }
    }


//生成文件

    private static File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                //假如文件不存在的话，创建文件
                file.createNewFile();
                //并对文件进行初始化
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

//生成文件夹

    private static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            Log.i("error:", e + "");
        }
    }

    public static void writeTimeChangedLog(Context context) {
        String nowTime = TimeUtil.getNowTime(System.currentTimeMillis());
/*
        String lastTime = SharePreferencesHelper.getInstance(context).getString(SharePreferencesHelper.TIME,"");
*/
        String timeChangeLog = nowTime +
                " 人工校时 ";
                //+ lastTime + "->" + nowTime;
        writeTxtToFile(timeChangeLog);

    }

    //用于在文件的头部插入空格
    public static void insertIntoFile(RandomAccessFile accessFile, int insertLength){
        try {
            //RandomAccessFile accessFile = new RandomAccessFile(file, "rw");
            long fileLength = accessFile.length();
            for(int i =0; i<fileLength; i++){
                accessFile.seek(fileLength-i-1);
                byte rb = accessFile.readByte();
                accessFile.seek(fileLength+insertLength-i-1);
                accessFile.writeByte(rb);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
