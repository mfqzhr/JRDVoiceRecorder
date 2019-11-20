package com.tyjradio.jrdvoicerecorder.recorder;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.dsp.DspManager;
import android.dsp.common.CommonManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.tyjradio.jrdvoicerecorder.R;
import com.tyjradio.jrdvoicerecorder.recorder.recording.AudioChunk;
import com.tyjradio.jrdvoicerecorder.recorder.recording.AudioRecordConfig;
import com.tyjradio.jrdvoicerecorder.recorder.recording.MsRecorder;
import com.tyjradio.jrdvoicerecorder.recorder.recording.PullTransport;
import com.tyjradio.jrdvoicerecorder.recorder.recording.Recorder;
import com.tyjradio.jrdvoicerecorder.utils.RefreshUI;
import com.tyjradio.jrdvoicerecorder.utils.AudioUtils;
import com.tyjradio.jrdvoicerecorder.utils.FileUtils;
import com.tyjradio.jrdvoicerecorder.utils.PcmToWavUtil;
import com.tyjradio.jrdvoicerecorder.utils.SharePreferencesHelper;
import com.tyjradio.jrdvoicerecorder.utils.StatusRecorderUtils;
import com.tyjradio.jrdvoicerecorder.utils.TimeUtil;
import com.tyjradio.jrdvoicerecorder.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutorService;

import static com.tyjradio.jrdvoicerecorder.utils.GlobalConfig.AUDIO_FORMAT;
import static com.tyjradio.jrdvoicerecorder.utils.GlobalConfig.CHANNEL_CONFIG;
import static com.tyjradio.jrdvoicerecorder.utils.GlobalConfig.SAMPLE_RATE_INHZ;

/**
 * 用于播放的服务 可以后台运行
 */

public class VoiceRecorderService extends Service {

    private int recordEnableNum = 0; //通知窄带recordEnable的次数
    private int nbRecordEnable = 1;
    private static final String TAG = "TestHyt";
    //命令管理
    private CommonManager commonManager = null;
    //音效管理
    private DspManager dspManager = null;


    //实现通信
    public final IBinder binder = new VoiceBinder();
    //结束时间
    public long stopRecorderTime;
    Uri newUri;
    //获取内容共享器
    Uri uri = Uri.parse("content://com.tyjradio.provider/phonerecorder");
    //向数据库存入数据
    ContentValues values;
    public String newId;

    private String fileName = "";

    //ui刷新
    RefreshUI refreshUI;
    //录音对象
    Recorder recorder;
    //录音参数
    String ZipCode;
    String ChannelCode;
    String ChannelType;
    String RecorderType;
    String OutOrInput;
    byte[] MachineSerialNum;
    String REFrequency;
    String EmFrequency;
    long CallingTime;
    int SelfPhoneNum, CalledPhoneNum;

    public Handler handler = new Handler();


    public VoiceRecorderService() {
    }

    protected byte makeChecksum(byte[] buffer, int size) {
        byte xor8;
        xor8 = 0x00;
        for (int i = 0; i < size; i++) {
            xor8 ^= buffer[i];

        }
        return xor8;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        /* 注册hytera 的监听器 **/
        registerHyteraListener();

        Log.v("test", "--------------开机启动服务-------------");


    }

    private void registerHyteraListener() {
        CommonManager.CommonManagerListener commonManagerListener = new CommonManager.CommonManagerListener() {

            @Override
            public void HRCPP_CT_C_Request_RecordInfor(byte[] InforParam) {
                super.HRCPP_CT_C_Request_RecordInfor(InforParam);
                // 窄带请求，总是对录音使能的答复
                Log.d("InforParam", "InforParam: " + Arrays.toString(InforParam));
                byte[] enable = Utils.intToByte(nbRecordEnable);
                byte[] reply_InforParam = {0x02, 'R', 0x13, 0x0a, 0x00, (byte) 0x91, 0x00, enable[0],
                        0x00, 0x03};//HRCPP_CT_C_Request_RecordInfor_Reply
                reply_InforParam[8] = makeChecksum(reply_InforParam, 10);
                commonManager.HRCPP_CT_C_Request_RecordInfor_Reply(reply_InforParam);
            }

            @Override
            public void HRCPP_CT_C_Broadcast_RecordInfor(byte[] InforParam) {
                super.HRCPP_CT_C_Broadcast_RecordInfor(InforParam);
                if (InforParam[5] == -112) {
                    StatusRecorderUtils.writeData(InforParam);
                }
                if (InforParam[5] == 13 || InforParam[5] == 14) {
                    doStart();
                    SelfPhoneNum = Utils.bytesToInt(new byte[]{InforParam[7], InforParam[8], InforParam[9],
                            InforParam[10]});
                    CalledPhoneNum = Utils.bytesToInt(new byte[]{InforParam[11], InforParam[12], InforParam[13],
                            InforParam[14]});
                    ZipCode = new String(String.valueOf(InforParam[15]));  // 区
                    ChannelCode = new String(String.valueOf(InforParam[16]));
                    ChannelType = new String(String.valueOf(InforParam[17]));
                    RecorderType = new String(String.valueOf(InforParam[18]));
                    REFrequency = String.valueOf(Utils.bytesToInt(new byte[]{InforParam[23], InforParam[24], InforParam[25],
                            InforParam[26]}));
                    EmFrequency = String.valueOf(Utils.bytesToInt(new byte[]{InforParam[19], InforParam[20], InforParam[21],
                            InforParam[22]}));
                    OutOrInput = new String(String.valueOf(InforParam[5]));

                    byte[] MachineSerialNum1 = new byte[16];
                    for (int i = 27; i < 43; i++) {
                        MachineSerialNum1[i - 27] = InforParam[i];
                    }
                    MachineSerialNum = MachineSerialNum1;
                    Log.d(TAG, ZipCode + "|" + SelfPhoneNum + "|" + CalledPhoneNum + "|" + ChannelCode + "|" + ChannelType + "|" +
                            RecorderType + "|" + REFrequency + "|" + EmFrequency + "|" + OutOrInput + "|" +
                            MachineSerialNum);

                }

                if (InforParam[5] == 17) {
                    Log.v("test", "start of stop17");
                    doStop();
                    Log.v("test", "end of stop17");

                }

                //Log.d(TAG, inforParam);
            }

            @Override
            public void HRCPP_CT_C_Command_RecordInfor_Reply(byte[] InforParam) {
                super.HRCPP_CT_C_Command_RecordInfor_Reply(InforParam);
                Log.d("InforParam", "HRCPP_CT_C_Command_RecordInfor_Reply InforParam: "
                        + Arrays.toString(InforParam));

                if (InforParam[5] == -111) {
                    //通知窄带开启录音成功
                    if (InforParam[7] == 0) {
                        handler.removeCallbacks(recordEnableRunnable);
                    }
                    StatusRecorderUtils.writeRecorderEnable(InforParam[7]);
                }
            }

        };
        dspManager = (DspManager) getSystemService("dsp_service");
        if (dspManager != null) {
            commonManager = dspManager.getCommonManager();
            commonManager.registerCommonManagerListener(commonManagerListener);
            byte[] command_InforParam = {0x02, 'R', 0x23, 0x0a, 0x00, (byte) 0x91, 0x00, 0x00, 0x00, 0x03};
            command_InforParam[8] = makeChecksum(command_InforParam, 10);
            commonManager.HRCPP_CT_C_Command_RecordInfor(command_InforParam);
        }

        values = new ContentValues();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences sharedPreferences = getSharedPreferences("jrdConf", Context.MODE_PRIVATE);
        int nbRecordSetup = sharedPreferences.getInt("nbRecordSetup", 1);
        nbRecordEnable = nbRecordSetup;

        //通知窄带nbRecordEnable
        recordEnableNum = 0;
        //handler.post(recordEnableRunnable);
        refreshUI = RefreshUI.getInstance();
        //如果service停止，当前资源允许情况下，重启service by yangjian
        return START_STICKY;

        // return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 向窄带发送nbRecordEnable
     */
    private Runnable recordEnableRunnable = new Runnable() {
        @Override
        public void run() {

            if (recordEnableNum < 15) {
                byte[] bEnableREcord = Utils.intToByte(nbRecordEnable);
                byte[] command_InforParam = {0x02, 'R', 0x23, 0x0a, 0x00, (byte) 0x91, 0x00, bEnableREcord[0], 0x00, 0x03};
                command_InforParam[8] = makeChecksum(command_InforParam, 10);
                commonManager.HRCPP_CT_C_Command_RecordInfor(command_InforParam);
                handler.postDelayed(this, 2000);

            } else {
                Handler toastHandler = new Handler(Looper.getMainLooper());
                toastHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "后台窄带断连！", Toast.LENGTH_LONG).show();
                    }
                });

            }
            recordEnableNum++;
        }

    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        // 如果Service被杀死，干掉通知
        //handler.removeCallbacks(recordEnableRunnable);
        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(new Intent(getApplicationContext(), VoiceRecorderService.class));
        } else {
            startService(new Intent(getApplicationContext(), VoiceRecorderService.class));
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return binder;

        //throw new UnsupportedOperationException("Not yet implemented");
    }

    public class VoiceBinder extends Binder {
        //返回service对象
        public VoiceRecorderService getService() {
            return VoiceRecorderService.this;
        }

    }


    /**
     * 启动录音
     *
     * @return
     */
    public void doStart() {

        try {

            //初始化录音
            CallingTime = System.currentTimeMillis();
            fileName = new SimpleDateFormat("yyyyMMdd_hhmmss").format(new Date());
            String fileNamePath = FileUtils.getPcmFileAbsolutePath(fileName);
            Log.e("TAG", fileNamePath);
            recorder = getRecorder(fileNamePath);
            recorder.startRecording();

        } catch (IllegalStateException e) {

        }

    }


    public void doStop() {
        recorder.stopRecording();
        File pcmFile = new File(FileUtils.getPcmFileAbsolutePath(fileName));
        PcmToWavUtil pcmToWavUtil = new PcmToWavUtil(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT);
        stopRecorderTime = System.currentTimeMillis();
        if (stopRecorderTime - CallingTime < 2000 || pcmToWavUtil.getDuration(FileUtils.getPcmFileAbsolutePath(fileName)) < 500) {
            pcmFile.delete();
            return;
        }
        //录音结束，将数据写入大文件中，并且将数据存入数据库
        //判断大文件是否存在
        if (!FileUtils.isFileCreate(FileUtils.getPcmFileAbsolutePath(FileUtils.recordName))) {
            File file = new File(FileUtils.getPcmFileAbsolutePath(FileUtils.recordName));
            try {
                FileUtils.createFixedFile(file, FileUtils.LENGTH);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.e("TAG", "文件已经存在了");
        }
        //1.将文件写入大文件
        String name = FileUtils.getPcmFileAbsolutePath(fileName);
        long startPosition = SharePreferencesHelper.getInstance(this).getLong(SharePreferencesHelper.START_POSITION, 0);
        FileUtils.copyFile(startPosition, name, this);
        long length = SharePreferencesHelper.getInstance(this).getLong(SharePreferencesHelper.LENGTH, 0);

        //(需要判断空间是否可以存储，空间不足则覆盖最开始存入的信息)
        if (startPosition + length > FileUtils.LENGTH) {
            //从头再开始写入，并且修改记录的覆盖次数
            startPosition = 0;
            int count = SharePreferencesHelper.getInstance(VoiceRecorderService.this).getInt(
                    SharePreferencesHelper.COUNT, 0);
            count++;
            SharePreferencesHelper.getInstance(VoiceRecorderService.this).putInt(
                    SharePreferencesHelper.COUNT, count);
        }

        //2.创建实体类，将信息存到数据库
        values.put("DurationTime", pcmToWavUtil.getDuration(FileUtils.getPcmFileAbsolutePath(fileName)));
        values.put("CallingTime", TimeUtil.getNowTime(CallingTime));
        values.put("ZipCode", ZipCode);
        values.put("ChannelCode", ChannelCode);
        if ("0".equals(ChannelType)) {
            values.put("ChannelType", "数字");
            if ("0".equals(RecorderType))
                values.put("RecorderType", "个呼");
            if ("1".equals(RecorderType))
                values.put("RecorderType", "组呼");
            if ("2".equals(RecorderType))
                values.put("RecorderType", "全呼");

        }
        if ("1".equals(ChannelType)) {
            values.put("ChannelType", "模拟");
            if ("0".equals(RecorderType))
                values.put("RecorderType", "普通");
            if ("1".equals(RecorderType))
                values.put("RecorderType", "列调");
        }

        if ("13".equals(OutOrInput)) {
            values.put("OutOrInput", "发送");
        }

        if ("14".equals(OutOrInput)) {
            values.put("OutOrInput", "接收");
        }
        values.put("MachineSerialNum", MachineSerialNum);
        values.put("SelfPhoneNum", SelfPhoneNum);
        values.put("CalledPhoneNum", CalledPhoneNum);
        String newREFrequency = Utils.Stringinsert(REFrequency, ".", 3);
        values.put("REFrequency", newREFrequency);
        String newEmFrequency = Utils.Stringinsert(EmFrequency, ".", 3);
        values.put("EmFrequency", newEmFrequency);
        values.put("AudioFile", fileName);
        values.put("StartPosition", startPosition);
        values.put("Length", length);
        int count = SharePreferencesHelper.getInstance(this).getInt(SharePreferencesHelper.COUNT, 0);
        values.put("Count", count);
        newUri = getContentResolver().insert(uri, values);
        newId = newUri.getPathSegments().get(1);

        //修改大文件存储的起始位置
        SharePreferencesHelper.getInstance(this).
                putLong(SharePreferencesHelper.START_POSITION, startPosition + length);
        //删除数据库中的已经被覆盖的音频信息
        AudioUtils.deleteRecorder(this, count, startPosition + length);
        //复制完成，删除文件
        pcmFile.delete();

        //录音完成,去刷新界面
        ActivityManager mAm = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        String activity_name = mAm.getRunningTasks(1).get(0).topActivity.getClassName();
        if ("com.tyjradio.jrdvoicerecorder.MainActivity".equals(activity_name)) {
            refreshUI.recorderCompletion();
        }

    }


    // 获取降噪录音机，跳过沉默区，只录"有声音"的部分
    private Recorder getNoiseRecorder(String fileNamePath) {

        return MsRecorder.pcm(
                new File(fileNamePath),
                new AudioRecordConfig.Default(1, 2, 16, 16000),
                new PullTransport.Noise()
                        .setOnAudioChunkPulledListener(new PullTransport.OnAudioChunkPulledListener() {
                            @Override
                            public void onAudioChunkPulled(AudioChunk audioChunk) {
                                Log.e("max", "amplitude: " + audioChunk.maxAmplitude());
                            }
                        }).setOnSilenceListener(new PullTransport.OnSilenceListener() {
                    @Override
                    public void onSilence(long l, long l1) {

                    }
                })

        );
    }

    private Recorder getRecorder(String fileNamePath) {

        return MsRecorder.pcm(
                new File(fileNamePath),
                new AudioRecordConfig.Default(1, 2, 16, 16000),
                new PullTransport.Default()

        );
    }



    /**
     * 创建通知栏
     */
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("001", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(channel);
            Notification notification = new Notification.Builder(getApplicationContext(), "001").build();
            startForeground(1, notification);

        }
    }


}
