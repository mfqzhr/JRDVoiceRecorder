package com.tyjradio.jrdvoicerecorder;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tyjradio.jrdvoicerecorder.adapter.AudioAdapter;
import com.tyjradio.jrdvoicerecorder.bean.AudioRecorderItemBean;
import com.tyjradio.jrdvoicerecorder.recorder.VoiceRecorderService;
import com.tyjradio.jrdvoicerecorder.utils.RefreshUI;
import com.tyjradio.jrdvoicerecorder.utils.AudioUtils;
import com.tyjradio.jrdvoicerecorder.utils.FileUtils;
import com.tyjradio.jrdvoicerecorder.utils.Mediahelper;
import com.tyjradio.jrdvoicerecorder.utils.PcmToWavUtil;
import com.tyjradio.jrdvoicerecorder.utils.Utils;
import com.tyjradio.jrdvoicerecorder.view.MainPop;
import com.tyjradio.jrdvoicerecorder.view.MorePopWindow;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import static com.tyjradio.jrdvoicerecorder.utils.GlobalConfig.AUDIO_FORMAT;
import static com.tyjradio.jrdvoicerecorder.utils.GlobalConfig.CHANNEL_CONFIG;
import static com.tyjradio.jrdvoicerecorder.utils.GlobalConfig.SAMPLE_RATE_INHZ;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, RefreshUI.OnAudioRecorderCompletionListener {

    @BindView(R.id.id_more_iv)
    ImageView ivMore;


    @BindView(R.id.id_audio_list_view)
    ListView mListView;

    @BindView(R.id.id_search_iv)
    ImageView ivSearch;

    @BindView(R.id.id_play_name)
    TextView tvPlayName;

    @BindView(R.id.id_now_time)
    TextView tvNowTime;

    @BindView(R.id.id_total_time)
    TextView tvTotalTime;

    @BindView(R.id.id_main_stop)
    ImageView ivMainStop;

    @BindView(R.id.id_main_on)
    ImageView ivMainOn;

    @BindView(R.id.id_progress)
    ProgressBar ivProgress;

    @BindView(R.id.id_ll_title)
    LinearLayout llTitle;

    @BindView(R.id.id_title)
    TextView tvTitle;

    @BindView(R.id.id_bottom_play_ll)
    LinearLayout linearLayout;


    private List<AudioRecorderItemBean> audios = new ArrayList<>();
    private AudioAdapter audioAdapter;
    private static final int RC_CAMERA_AND_LOCATION = 444;

    //目前选中的音乐的位置
    private int selectPosition = 0;
    private int playstate;
    public static final int isPlaying = 44;
    public static final int isPouse = 45;
    public static final int isStop = 46;
    private Thread thread;
    private MainPop mainPop;
    private VoiceRecorderService voiceRecorderService;
    //运用Handler中的handleMessage方法接收子线程传递的信息
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            // 将SeekBar位置设置到当前播放位置
            ivProgress.setProgress(msg.what);
            //获得音乐的当前播放时间
            tvNowTime.setText(AudioUtils.timeParse(msg.what));
        }
    };


    private Handler handler1 = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1001) {
                setPlayItem(-1);
                linearLayout.setEnabled(false);
                ivMainStop.setEnabled(false);
                ivMainOn.setEnabled(false);
                audioAdapter = new AudioAdapter(MainActivity.this, audios);
                mListView.setAdapter(audioAdapter);
            }
        }
    };
//    private ServiceConnection conn = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            voiceRecorderService = ((VoiceRecorderService.VoiceBinder) service).getService();
//            //绑定监听器
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            voiceRecorderService = null;
//        }
//    };
    //z


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //获取数据，需要先判断有没有权限
        getPermission();
        Log.d("刷新", "onCreate: 刷新了");

      //Intent intent = new Intent(this, VoiceRecorderService.class);//z
        //开启服务
       // startService(intent);//z
        //绑定服务

       // bindService(intent, conn, BIND_AUTO_CREATE); //z


        init();
        initData();
        checkService();

    }

    private void checkService() {
        boolean isRunning = Utils.isServiceRunning(this,"com.tyjradio.jrdvoicerecorder.recorder.VoiceRecorderService");
        if (isRunning == false){
            Intent recorderServiceIntent = new Intent(this,VoiceRecorderService.class);
            startService(recorderServiceIntent);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        String name = tvTitle.getText().toString();
        if (name.equals("发射方")) {
            audios = AudioUtils.getSongs(MainActivity.this, new String[]{"*"}, "OutOrInput='发送'");
            audioAdapter.refreshData(audios);
        }
        if (name.equals("接收方")) {
            audios = AudioUtils.getSongs(MainActivity.this, new String[]{"*"}, "OutOrInput='接收'");
            audioAdapter.refreshData(audios);
        }
        if (name.equals("全部录音")) {
            audios = AudioUtils.getSongs(MainActivity.this, null, null);
            audioAdapter.refreshData(audios);
        }
        if (mListView.getCount() > 0) {
            setPlayItem(0);
            linearLayout.setEnabled(true);
        }

    }

    @Override
    protected void onStop() {

        if (audios.size() > 0) {

            deleteWav();
        }

        super.onStop();
    }

    public void deleteWav() {
        File file = new File(FileUtils.getWAVFileAbsolutePath(audios.get(selectPosition).getAudioFile()));
        if (file.exists()) {
            file.delete();

        }
    }


    public void showPopMenu(View view) {
        MorePopWindow morePopWindow = new MorePopWindow(this);
        morePopWindow.showPopupWindow(view);

    }


    private void init() {
        //ivMore = findViewById(R.id.id_more_iv);
        RefreshUI refreshUI = RefreshUI.getInstance();
        refreshUI.setOnAudioRecorderCompletionListener(this);
        if (mListView.getCount() == 0)
            linearLayout.setEnabled(false);
        ivMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showPopMenu(v);
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //点击某个条目，则被选中，放到底部播放栏中
                setPlayItem(position);
            }
        });

    }

    private void initData() {
        audios = AudioUtils.getSongs(this, null, null);
        audioAdapter = new AudioAdapter(this, audios);
        mListView.setAdapter(audioAdapter);
        if (mListView.getCount() > 0) {
            setPlayItem(0);
            linearLayout.setEnabled(true);
            ivMainStop.setEnabled(true);
            ivMainOn.setEnabled(true);
        } else {
            linearLayout.setEnabled(false);
            ivMainStop.setEnabled(false);
            ivMainOn.setEnabled(false);
        }
    }

    //设置底部播放的音乐信息
    public void setPlayItem(int position) {

        if (playstate == isPlaying || playstate == isPouse) {
            stop();
        }
        if (position == -1) {
            tvPlayName.setText(" ");
            tvNowTime.setText("00:00:00");
            tvTotalTime.setText("00:00:00");
            ivProgress.setProgress(0);
        } else {

            selectPosition = position;
            AudioRecorderItemBean audio = audios.get(position);
            tvPlayName.setText(audio.getAudioFile());
            tvNowTime.setText("00:00:00");
            tvTotalTime.setText(AudioUtils.timeParse(audio.getDurationTime()));
            ivMainStop.setVisibility(View.VISIBLE);
            ivMainOn.setVisibility(View.GONE);
            ivProgress.setProgress(0);
            for (int i = 0; i < audios.size(); i++) {
                if (i == position) {
                    audios.get(i).setSelect(true);
                    if (playstate == isPlaying) {
                        audios.get(i).setPlaying(true);
                    } else {
                        audios.get(i).setPlaying(false);
                    }
                } else {
                    audios.get(i).setSelect(false);
                    audios.get(i).setPlaying(false);
                }
            }
            audioAdapter.refreshData(audios);
        }
        playstate = isStop;
    }

    @Override
    protected void onPause() {
        super.onPause();
        //当离开此页面，则停止播放
        if (playstate == isPlaying) {
            pouse();
        }
    }


    //进行一些权限的请求
    public void getPermission() {

        //申请两个权限，录音和文件读写
        //1、首先声明一个数组permissions，将需要的权限都放在里面
        String[] permissions = new String[]{
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECEIVE_BOOT_COMPLETED};
        //2、创建一个mPermissionList，逐个判断哪些权限未授予，未授予的权限存储到mPerrrmissionList中
        List<String> mPermissionList = new ArrayList<>();

        final int mRequestCode = 100;//权限请求码


        //权限判断和申请

        mPermissionList.clear();//清空没有通过的权限

        //逐个判断你要的权限是否已经通过
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);//添加还未授予的权限
            }
        }

        //申请权限
        if (mPermissionList.size() > 0) {//有权限没有通过，需要申请
            ActivityCompat.requestPermissions(this, permissions, mRequestCode);
        } else {
            //说明权限都已经通过，可以做你想做的事情去
        }
    }


    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        //权限已经被授权
        Log.e("TAG", "权限已经被授权");
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.e("TAG", "权限已经被拒绝");
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {

        }
    }

    public void pcmToWav(String wavPath) {
        PcmToWavUtil pcmToWavUtil = new PcmToWavUtil(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT);
        pcmToWavUtil.pcmToWav(FileUtils.getPcmFileAbsolutePath(audios.get(selectPosition).getAudioFile())
                , wavPath);
    }

    /**
     * 播放音乐，此方法为从头开始播放
     */
    public void play() {
        //判断要播放的WAV文件是否存在
        String filePath = FileUtils.getWAVFileAbsolutePath((audios.get(selectPosition).getAudioFile()));
        if (!FileUtils.isFileCreate(filePath)) {
            //先从大文件中读取pcm文件

            FileUtils.getFile(audios.get(selectPosition).getStartPosition(),
                    audios.get(selectPosition).getLength(), audios.get(selectPosition).getAudioFile());


            pcmToWav(filePath);
            //转换完成，将pcm文件删除
            File file = new File(FileUtils.getPcmFileAbsolutePath(audios.get(selectPosition).getAudioFile()));
            file.delete();
        }
        if (FileUtils.isFileCreate(filePath)) {
            Mediahelper.playSound(filePath, new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    //播放完成了，则修改状态
                    if (playstate == isPlaying) {
                        stop();
                    }
                }
            });
            //修改页面
            playstate = isPlaying;
            //修改状态
            playstate = isPlaying;
            ivProgress.setMax((int) audios.get(selectPosition).getDurationTime());
            // 创建一个线程
            thread = new Thread(new MuiscThread());
            // 启动线程
            thread.start();
            setAni();
        }
    }

    public void setAni() {
        for (int i = 0; i < audios.size(); i++) {
            if (i == selectPosition) {
                if (playstate == isPlaying) {
                    audios.get(i).setPlaying(true);
                } else {
                    audios.get(i).setPlaying(false);
                }
            } else {
                audios.get(i).setPlaying(false);
            }
        }
        audioAdapter.refreshData(audios);
    }


    public void stop() {
        //停止播放了
        thread.interrupt();
        playstate = isStop;
        Mediahelper.release();
        deleteWav();
        ivMainStop.setVisibility(View.VISIBLE);
        ivMainOn.setVisibility(View.GONE);
        setAni();
    }

    public void start() {
        //重新开始播放
        Mediahelper.resume();
        playstate = isPlaying;
        setAni();
    }

    public void pouse() {
        Mediahelper.pause();
        playstate = isPouse;
        ivMainStop.setVisibility(View.VISIBLE);
        ivMainOn.setVisibility(View.GONE);
        setAni();
    }

    //点击了暂停按钮，开始播放
    @OnClick(R.id.id_main_stop)
    void clickStop() {
        ivMainStop.setVisibility(View.GONE);
        ivMainOn.setVisibility(View.VISIBLE);
        if (playstate == isStop) {
            play();
        } else if (playstate == isPouse) {
            //继续播放
            start();
        }
    }

    //点击了播放按钮，则暂停
    @OnClick(R.id.id_main_on)
    void clickOn() {
        ivMainStop.setVisibility(View.VISIBLE);
        ivMainOn.setVisibility(View.GONE);
        if (playstate == isPlaying) {
            //正在播放，则暂停
            pouse();
        }
    }


    @OnClick(R.id.id_search_iv)
    void goSearchActivity() {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("all", (Serializable) audios);
        intent.putExtra("bundle", bundle);
        intent.setClass(this, SearchActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.id_open_pop)
    void openMainPop() {
        if (mainPop == null) {
            mainPop = new MainPop(this, new MainPop.ChangeSelectListener() {
                @Override
                public void setChange(String name) {
                    if (playstate == isPlaying) {
                        stop();
                    }
                    tvTitle.setText(name);
                    selectPosition = 0;
                    if (name.equals("发射方")) {
                        audios = AudioUtils.getSongs(MainActivity.this, new String[]{"*"}, "OutOrInput='发送'");
                        audioAdapter.refreshData(audios);
                    }
                    if (name.equals("接收方")) {
                        audios = AudioUtils.getSongs(MainActivity.this, new String[]{"*"}, "OutOrInput='接收'");
                        audioAdapter.refreshData(audios);
                    }
                    if (name.equals("全部录音")) {
                        audios = AudioUtils.getSongs(MainActivity.this, null, null);
                        audioAdapter.refreshData(audios);
                    }
                    if (mainPop != null && mainPop.isShowing()) {
                        mainPop.dismiss();
                    }
                    if (mListView.getCount() > 0) {
                        linearLayout.setEnabled(true);
                        ivMainStop.setEnabled(true);
                        ivMainOn.setEnabled(true);
                        setPlayItem(0);
                    } else {
                        tvPlayName.setText(" ");
                        tvNowTime.setText("00:00:00");
                        tvTotalTime.setText("00:00:00");
                        ivProgress.setProgress(0);
                        linearLayout.setEnabled(false);
                        ivMainStop.setEnabled(false);
                        ivMainOn.setEnabled(false);
                    }

                }
            });
        }
        if (mainPop.isShowing()) {
            mainPop.dismiss();
        } else {
            mainPop.showAtLocation(findViewById(R.id.id_main), Gravity.TOP, 0, Utils.dip2px(this, 75));
        }
    }

    @OnClick(R.id.id_bottom_play_ll)
    void goPlayActivity() {
        if (playstate == isPlaying) {
            pouse();
        }
        Intent intent = new Intent();
        intent.setClass(this, PlayActivity.class);
        intent.putExtra("audio", audios.get(selectPosition));
        startActivity(intent);
    }

    @Override
    public void onAudioRecorderCompletion() {
        Log.d("回调", "onAudioRecorderCompletion: ");
        String name = tvTitle.getText().toString();
        if (name.equals("发射方")) {
            audios = AudioUtils.getSongs(MainActivity.this, new String[]{"*"}, "OutOrInput='发送'");
        }
        if (name.equals("接收方")) {
            audios = AudioUtils.getSongs(MainActivity.this, new String[]{"*"}, "OutOrInput='接收'");

        }
        if (name.equals("全部录音")) {
            audios = AudioUtils.getSongs(MainActivity.this, null, null);

        }

        if (audios.size() > 0) {
            setPlayItem(0);
            linearLayout.setEnabled(true);
            ivMainStop.setEnabled(true);
            ivMainOn.setEnabled(true);
            audioAdapter.refreshData(audios);
        } else {
            handler1.sendEmptyMessage(1001);

        }


    }


    //建立一个子线程实现Runnable接口
    class MuiscThread implements Runnable {
        @Override
        //实现run方法
        public void run() {
            //判断音乐的状态，在不停止与不暂停的情况下向总线程发出信息
            while (playstate == isPlaying || playstate == isPouse) {
                try {
                    // 每100毫秒更新一次位置
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
                //发出的信息
                handler.sendEmptyMessage(Mediahelper.playPosition());
            }

        }


    }


}
