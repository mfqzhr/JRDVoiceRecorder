package com.tyjradio.jrdvoicerecorder.utils;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.crypto.spec.OAEPParameterSpec;

import static com.tyjradio.jrdvoicerecorder.utils.GlobalConfig.AUDIO_FORMAT;
import static com.tyjradio.jrdvoicerecorder.utils.GlobalConfig.CHANNEL_CONFIG;
import static com.tyjradio.jrdvoicerecorder.utils.GlobalConfig.SAMPLE_RATE_INHZ;

//文件的工具类
public class FileUtils {
    //固定大小文件的文件大小值(10MB)
    public static final long LENGTH = 1024 * 1024 * 1000;
    private static String rootPath = "Yplaer";
    public static String recordName = "record.pcm";
    public static String WAV_FILE_NAME = "yplay.wav";
    //原始文件(不能播放)
    private final static String AUDIO_PCM_BASEPATH = "/" + rootPath + "/pcm/";
    //wav文件
    private final static String AUDIO_WAV_BASEPATH = "/" + rootPath + "/wav/";

    public static String getPcmFileAbsolutePath(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            throw new NullPointerException("fileName isEmpty");
        }
        if (!isSdcardExit()) {
            throw new IllegalStateException("sd card no found");
        }
        String mAudioRawPath = "";
        if (isSdcardExit()) {
            if (!fileName.endsWith(".pcm")) {
                fileName = fileName + ".pcm";
            }
            String fileBasePath = Environment.getExternalStorageDirectory().getAbsolutePath() + AUDIO_PCM_BASEPATH;
            File file = new File(fileBasePath);
            //创建目录
            if (!file.exists()) {
                file.mkdirs();
            }
            mAudioRawPath = fileBasePath + fileName;
        }

        return mAudioRawPath;
    }


    public static String getWAVFileAbsolutePath(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            throw new NullPointerException("fileName isEmpty");
        }
        if (!isSdcardExit()) {
            throw new IllegalStateException("sd card no found");
        }
        String mAudioRawPath = "";
        if (isSdcardExit()) {
            if (!fileName.endsWith(".wav")) {
                fileName = fileName + ".wav";
            }
            String fileBasePath = Environment.getExternalStorageDirectory().getAbsolutePath() + AUDIO_WAV_BASEPATH;
            File file = new File(fileBasePath);
            //创建目录
            if (!file.exists()) {
                file.mkdirs();
            }
            mAudioRawPath = fileBasePath + fileName;
        }

        return mAudioRawPath;
    }

    /**
     * 判断是否有外部存储设备sdcard
     *
     * @return true | false
     */
    public static boolean isSdcardExit() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return true;
        else
            return false;
    }


    /**
     * 创建固定大小的文件
     *
     * @param file
     * @param length
     * @throws IOException
     */
    public static void createFixedFile(File file, long length) throws IOException {
        long start = System.currentTimeMillis();
        RandomAccessFile r = null;
        try {
            r = new RandomAccessFile(file, "rw");
            r.setLength(length);
        } finally {
            if (r != null) {
                try {
                    r.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        long end = System.currentTimeMillis();
        System.out.println(end - start);
    }

    //判断文件是否存在
    public static Boolean isFileCreate(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }


    public static void getFile(long startPosition, long length, String fileName) {
        try {
            File file = new File(FileUtils.getPcmFileAbsolutePath(fileName));
            FileOutputStream out = new FileOutputStream(file);
            RandomAccessFile in = new RandomAccessFile(getPcmFileAbsolutePath(FileUtils.recordName), "rw");
            OpusUtils opusUtils = new OpusUtils();
            long createDecoder = opusUtils.createDecoder(SAMPLE_RATE_INHZ, 1);
            in.seek(startPosition);
            byte[] total = new byte[(int) length];
            //byte[] b;
            int i = 0;
            long position = startPosition;
            Log.e("TAG", "length:" + length);
            while (i < length) {
                byte[] b = new byte[80];
                int n = in.read(b,0,80);
                position += n;
                in.seek(position);
                i = i + n;
               /* if (i + 80 > length) {
                    return;
                }
                b = Utils.subByte(total, i, 80);
                i = i + 80;*/
                short[] shortArray = new short[b.length * 4];
                int decodeSize = opusUtils.decode(createDecoder, b, shortArray);
                if (decodeSize > 0) {
                    short[] decodeArray = new short[decodeSize];
                    System.arraycopy(shortArray, 0, decodeArray, 0, decodeSize);
                    out.write(Utils.shortArrayToByteArray(decodeArray));
                }
                Log.e("TAG", "decodeSize:" + decodeSize);

            }
            //Log.e("TAG", "length:" + length);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void copyFile(long startPosition, String fileName, Context context) {
        try {
            FileInputStream fileInputStream = new FileInputStream(fileName);
            int n = 0;
            RandomAccessFile out = new RandomAccessFile(getPcmFileAbsolutePath(recordName), "rw");
            OpusUtils opusUtils = new OpusUtils();
            long createEncoder = opusUtils.createEncoder(SAMPLE_RATE_INHZ, 1, 3);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            byte[] b = new byte[640];
            int length = 0,total = 0;
            out.seek(startPosition);
            while ((n = bufferedInputStream.read(b,0,b.length)) != -1) {
                total += n;
                if (total + 19200 > fileInputStream.getChannel().size()) {
                    break;
                }
                byte[] readByte  = new byte[n];
                System.arraycopy(b, 0, readByte, 0, n);


                byte[] byteArray = new byte[readByte.length / 8];
                int encodeSize = opusUtils.encode(createEncoder, Utils.byteArrayToShortArray(readByte), 0, byteArray);
                if (encodeSize > 0) {
                    length +=encodeSize;

                    byte[] decodeArray = new byte[encodeSize];
                    System.arraycopy(byteArray, 0, decodeArray, 0, encodeSize);
                    out.write(decodeArray);
                }
            }
            Log.e("TAG", "length:" + length);
            SharePreferencesHelper.getInstance(context).putLong(SharePreferencesHelper.LENGTH,length);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}