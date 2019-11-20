package com.tyjradio.jrdvoicerecorder.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.util.TypedValue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class Utils {

    //dp转px
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
    //px转dp
    public static int px2dip(Context context, int pxValue) {
        return ((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                pxValue, context.getResources().getDisplayMetrics()));

    }

    public static String unicodeToString(byte[] bytes){
        StringBuffer strbuffer = new StringBuffer();

        for (int i = 0; i < bytes.length/2; i++){
            int addr = bytes[i*2] & 0xFF;
            addr |= ((bytes[i*2+1] << 8) & 0xFF00);
            strbuffer.append((char)addr);
        }

        return strbuffer.toString();

    }

    public static byte[] getBytes(char[] chars) {
        byte [] result = new byte[chars.length];
        for(int i=0;i<chars.length;i++){
            result[i] = (byte) chars[i];
        }
        return result;
    }



    /* byte[]转Int */
    public static int bytesToInt(byte[] bytes) {
        int addr = bytes[0] & 0xFF;
        addr |= ((bytes[1] << 8) & 0xFF00);
        addr |= ((bytes[2] << 16) & 0xFF0000);
        addr |= ((bytes[3] << 24) & 0xFF000000);
        return addr;

    }

    /**
     * 以大端模式将byte[]转成int
     */
    public static int bytesToIntBig(byte[] src, int offset) {
        int value;
        value = (int) (((src[offset] & 0xFF) << 24)
                | ((src[offset + 1] & 0xFF) << 16)
                | ((src[offset + 2] & 0xFF) << 8)
                | (src[offset + 3] & 0xFF));
        return value;
    }


    /* Int转byte[] */
    public static byte[] intToByte(int i) {
        byte[] abyte0 = new byte[4];
        abyte0[0] = (byte) (0xff & i);
        abyte0[1] = (byte) ((0xff00 & i) >> 8);
        abyte0[2] = (byte) ((0xff0000 & i) >> 16);
        abyte0[3] = (byte) ((0xff000000 & i) >> 24);
        return abyte0;
    }


    /**
     * 以大端模式将int转成byte[]
     */
    public static byte[] intToBytesBig(int value) {
        byte[] src = new byte[4];
        src[0] = (byte) ((value >> 24) & 0xFF);
        src[1] = (byte) ((value >> 16) & 0xFF);
        src[2] = (byte) ((value >> 8) & 0xFF);
        src[3] = (byte) (value & 0xFF);
        return src;
    }

    /**
     * 组成新的字符
     */
    public static String Stringinsert(String src, String dec, int position){
        StringBuffer stringBuffer = new StringBuffer(src);

        return stringBuffer.insert(position, dec).toString();
    }


    /**
     * 截取byte数组   不改变原数组
     * @param b 原数组
     * @param off 偏差值（索引）
     * @param length 长度
     * @return 截取后的数组
     */
    public static byte[] subByte(byte[] b,int off,int length){
        byte[] b1 = new byte[length];
        System.arraycopy(b, off, b1, 0, length);
        return b1;
    }

    /**
     * 判断后台服务是否运行
     */
    public static boolean isServiceRunning(Context context, String ServiceName) {
        if (("").equals(ServiceName) || ServiceName == null)
            return false;
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) activityManager.getRunningServices(50);
        for (int i=0; i<runningService.size(); i++){
            if (runningService.get(i).service.getClassName().toString().equals(ServiceName))
                return true;
        }
        return false;

    }



    public static short[] byteArrayToShortArray(byte[] byteArray) {
        short[] shortArray = new short[byteArray.length / 2];
        ByteBuffer.wrap(byteArray).order(ByteOrder.nativeOrder()).asShortBuffer().get(shortArray);
        return shortArray;
    }

    public static byte[] shortArrayToByteArray( short[] shortArray) {
        int count = shortArray.length;
        byte[] dest = new byte[count << 1];
        int i = 0;

        for(int var5 = count; i < var5; ++i) {
            int var10001 = i * 2;
            short var6 = shortArray[i];
            short var7 = (short)'\uffff';
            int var9 = var10001;
            short var10 = (short)(var6 & var7);
            dest[var9] = (byte)((int)((long)var10 >> 0));
            var10001 = i * 2 + 1;
            var6 = shortArray[i];
            var7 = (short)'\uffff';
            var9 = var10001;
            var10 = (short)(var6 & var7);
            dest[var9] = (byte)((int)((long)var10 >> 8));
        }

        return dest;
    }



}
