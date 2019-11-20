package com.tyjradio.jrdvoicerecorder.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.tyjradio.jrdvoicerecorder.bean.AudioRecorderItemBean;
import com.tyjradio.jrdvoicerecorder.recorder.VoiceRecorderService;

import java.util.ArrayList;
import java.util.Collections;

public class AudioUtils {


    /**
     * Android 音乐播放器应用里，读出的音乐时长为 long 类型以毫秒数为单位，例如：将 234736 转化为分钟和秒应为 03:55 （包含四舍五入）
     *
     * @param duration 音乐时长
     * @return
     */
    public static String timeParse(long duration) {
        String time = "";
        long minute = duration / 60000;
        long seconds = duration % 60000;
        long second = Math.round((float) seconds / 1000);
        long hour = 0;
        if (minute >= 60) {
            hour = minute / 60;
            minute = minute % 60;
        }

        if (hour < 10) {
            time += "0";
        }
        time += hour + ":";
        if (minute < 10) {
            time += "0";
        }
        time += minute + ":";
        if (second < 10) {
            time += "0";
        }
        time += second;
        return time;

    }


    /**
     * 获取sd卡所有的音乐文件
     *
     * @return
     * @throws
     */
    public static ArrayList<AudioRecorderItemBean> getSongs(Context context, String[] projection, String selection) {

        ArrayList<AudioRecorderItemBean> list = new ArrayList<>();
        //把扫描到的录音赋值给list
        Uri uri = Uri.parse("content://com.tyjradio.provider/phonerecorder");
        Cursor cursor = context.getContentResolver()
                .query(uri, projection, selection, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                AudioRecorderItemBean audioRecorderItemBean =
                        new AudioRecorderItemBean();
                audioRecorderItemBean
                        .setId
                                (cursor.getInt(cursor.getColumnIndex("ID")));
                audioRecorderItemBean
                        .setAudioFile
                                (cursor.getString(cursor.getColumnIndex("AudioFile")));
                audioRecorderItemBean
                        .setCallingTime
                                (cursor.getString(cursor.getColumnIndex("CallingTime")));
                audioRecorderItemBean
                        .setDurationTime
                                (cursor.getInt(cursor.getColumnIndex("DurationTime")));
                audioRecorderItemBean
                        .setMachineSerialNum
                                (new String(cursor.getBlob(cursor.getColumnIndex("MachineSerialNum"))));
                audioRecorderItemBean
                        .setSelfPhoneNum
                                (cursor.getString(cursor.getColumnIndex("SelfPhoneNum")));
                audioRecorderItemBean
                        .setCalledPhoneNum
                                (cursor.getString(cursor.getColumnIndex("CalledPhoneNum")));
                audioRecorderItemBean
                        .setZipCode
                                (cursor.getString(cursor.getColumnIndex("ZipCode")));
                audioRecorderItemBean
                        .setChannelCode
                                (cursor.getInt(cursor.getColumnIndex("ChannelCode")));
                audioRecorderItemBean
                        .setChannelType
                                (cursor.getString(cursor.getColumnIndex("ChannelType")));
                audioRecorderItemBean
                        .setRecorderType
                                (cursor.getString(cursor.getColumnIndex("RecorderType")));
                audioRecorderItemBean
                        .setREFrequency
                                (cursor.getDouble(cursor.getColumnIndex("REFrequency")));
                audioRecorderItemBean
                        .setOutOrInput
                                (cursor.getString(cursor.getColumnIndex("OutOrInput")));
                audioRecorderItemBean
                        .setEmFrequency
                                (cursor.getDouble(cursor.getColumnIndex("EmFrequency")));

                audioRecorderItemBean.setStartPosition(cursor.getInt(cursor.getColumnIndex("StartPosition")));
                audioRecorderItemBean.setLength(cursor.getInt(cursor.getColumnIndex("Length")));
                audioRecorderItemBean.setCount(cursor.getInt(cursor.getColumnIndex("Count")));


                list.add(audioRecorderItemBean);

            }
            cursor.close();
        }

        Collections.reverse(list);
        return list;
    }


    /**
     * 删除指定的录音
     */

    public static void deleteRecorder(Context context, int count, long startPosition) {

        //把扫描到的录音赋值给list
        Uri uri = Uri.parse("content://com.tyjradio.provider/phonerecorder");
        context.getContentResolver()
                .delete(uri, "startposition < ? and count < ?", new String[]{String.valueOf(startPosition), String.valueOf(count)});

    }

    /**
     * 删除全部的录音
     */

    public static void deleteAllRecorders(Context context) {

        //把扫描到的录音赋值给list
        Uri uri = Uri.parse("content://com.tyjradio.provider/phonerecorder");
        context.getContentResolver()
                .delete(uri, null, null);
        Uri uri1 = Uri.parse("content://com.tyjradio.provider/sqlite_sequence");
        context.getContentResolver().update(uri1,null,null,null);
        SharePreferencesHelper.getInstance(context).putInt(
                SharePreferencesHelper.COUNT, 0);




    }


    /**
     * 将模型转化成原始数据
     *
     * @param context
     * @param audioFile
     * @return
     */
    public static byte[] beanToBytes(Context context, String audioFile) {

        ArrayList<AudioRecorderItemBean> audios = AudioUtils.getSongs(context, new String[]{"*"}, "AudioFile = '" + audioFile + "'");
        AudioRecorderItemBean audio = audios.get(0);
        Log.d("转换", "beanToBytes: " + audio);
        byte[] data = new byte[42];

        //接收还是发送

        if (audio.getOutOrInput().equals("发送")) {
            data[0] = (byte) 13;
        }
        if (audio.getOutOrInput().equals("接收")) {
            data[0] = (byte) 14;
        }
        data[1] = 0;

        //本机id
        byte[] selfPhoneNum = Utils.intToByte(Integer.valueOf(audio.getSelfPhoneNum()));
        data[2] = selfPhoneNum[0];
        data[3] = selfPhoneNum[1];
        data[4] = selfPhoneNum[2];
        data[5] = selfPhoneNum[3];

        //远端id
        byte[] calledPhoneNum = Utils.intToByte(Integer.valueOf(audio.getCalledPhoneNum()));
        data[6] = calledPhoneNum[0];
        data[7] = calledPhoneNum[1];
        data[8] = calledPhoneNum[2];
        data[9] = calledPhoneNum[3];

        //区号
        data[10] = (byte) Integer.parseInt(audio.getZipCode());


        //信道号
        data[11] = (byte) audio.getChannelCode();

        if (audio.getChannelType().equals("数字")) {
            //信道类型
            data[12] = (byte) 0;
            if (audio.getRecorderType().equals("个呼"))
                //录音类型
                data[13] = (byte) 0;
            if (audio.getRecorderType().equals("组呼"))
                data[13] = (byte) 1;
            if (audio.getRecorderType().equals("全呼"))
                data[13] = (byte) 2;

        }
        if (audio.getChannelType().equals("模拟")) {
            data[12] = (byte) 1;
            if (audio.getRecorderType().equals("普通"))
                data[13] = (byte) 0;
            if (audio.getRecorderType().equals("列调"))
                data[13] = (byte) 1;
        }


        //tx频率
        byte[] emFrequency = Utils.intToByte((int) (audio.getEmFrequency() * 1000000));
        data[14] = emFrequency[0];
        data[15] = emFrequency[1];
        data[16] = emFrequency[2];
        data[17] = emFrequency[3];

        //rx频率
        byte[] reFrequency = Utils.intToByte((int) (audio.getREFrequency() * 1000000));
        data[18] = reFrequency[0];
        data[19] = reFrequency[1];
        data[20] = reFrequency[2];
        data[21] = reFrequency[3];

        //序列号
        byte[] machineSerialNum = audio.getMachineSerialNum().getBytes();
        for (int i = 0; i < machineSerialNum.length; i++) {
            data[22 + i] = machineSerialNum[i];
        }
        byte[] durationTime = Utils.intToByte((int)audio.getDurationTime());
        data[38] = durationTime[0];
        data[39] = durationTime[1];
        data[40] = durationTime[2];
        data[41] = durationTime[3];


        return data;
    }


}
