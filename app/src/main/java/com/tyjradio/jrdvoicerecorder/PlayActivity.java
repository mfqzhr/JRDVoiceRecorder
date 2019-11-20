package com.tyjradio.jrdvoicerecorder;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import com.tyjradio.jrdvoicerecorder.bean.AudioRecorderItemBean;
import com.tyjradio.jrdvoicerecorder.utils.AudioUtils;
import com.tyjradio.jrdvoicerecorder.utils.FileUtils;
import com.tyjradio.jrdvoicerecorder.utils.PcmToWavUtil;
import com.tyjradio.jrdvoicerecorder.utils.SamplePlayer;
import com.tyjradio.jrdvoicerecorder.utils.SoundFile;
import com.tyjradio.jrdvoicerecorder.view.WaveformView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;

import static com.tyjradio.jrdvoicerecorder.utils.GlobalConfig.AUDIO_FORMAT;
import static com.tyjradio.jrdvoicerecorder.utils.GlobalConfig.CHANNEL_CONFIG;
import static com.tyjradio.jrdvoicerecorder.utils.GlobalConfig.SAMPLE_RATE_INHZ;

public class PlayActivity extends AppCompatActivity implements WaveformView.WaveformListener {
    //public static final String fileName="/system/media/audio/ringtones/AcousticGuitar.ogg";
    private String fileName;
    private File mFile;
    //加载声音的线程
    private Thread mLoadSoundFileThread;
    private SoundFile mSoundFile;

    private long mLoadingLastUpdateTime;
    private boolean mLoadingKeepGoing;

    private SamplePlayer mPlayer;
    private Handler mHandler;


    private WaveformView mWaveformView;


    private boolean mIsPlaying;


    private int mPlayStartMsec;
    private int mPlayEndMsec;
    private int mMaxPos;
    private int mStartPos;


    private int mWidth;
    private boolean mTouchDragging;
    private int mOffsetGoal;
    private int mFlingVelocity;
    private int mOffset;
    private float mTouchStart;
    private int mTouchInitialOffset;
    private long mWaveformTouchStartMsec;

    @BindView(R.id.id_play_on_iv)
    ImageView ivPlayOn;

    @BindView(R.id.id_play_stop_iv)
    ImageView ivPlayStop;

    @BindView(R.id.id_title_tv)
    TextView tvTitle;

    @BindView(R.id.id_now_time_tv)
    TextView tvNowTime;

    @BindView(R.id.id_total_time_tv)
    TextView tvTotalTime;


    //加载的dialog
    private SpotsDialog loadingDialog;

    private AudioRecorderItemBean audio;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        ButterKnife.bind(this);
        loadingDialog = new SpotsDialog(this, "音频解析中");
        loadingDialog.setCancelable(false);
        loadingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                //弹框显示的回调
                mLoadingKeepGoing = false;
            }
        });
        audio = (AudioRecorderItemBean) getIntent().getSerializableExtra("audio");
        String filePath = FileUtils.getWAVFileAbsolutePath((audio.getAudioFile()));
        Log.d("filePath", "filtepath: " + filePath);
        if (!FileUtils.isFileCreate(filePath)) {
            FileUtils.getFile(audio.getStartPosition(),
                    audio.getLength(), audio.getAudioFile());
            pcmToWav(filePath);
            //转换完成，将pcm文件删除
            File file = new File(FileUtils.getPcmFileAbsolutePath(audio.getAudioFile()));
            file.delete();
        }
        fileName = filePath;
        tvTitle.setText(audio.getAudioFile());
        tvTotalTime.setText(AudioUtils.timeParse(audio.getDurationTime()));
        mHandler = new Handler();
        init();
        loadFromFile();
    }

    private void init() {
        mWaveformView = findViewById(R.id.wave_form_view);
        mWaveformView.setListener(this);
    }


    private long getCurrentTime() {
        return System.nanoTime() / 1000000;
    }


    /**
     * 加载音频文件
     */
    public void loadFromFile() {
        mFile = new File(fileName);
        Log.v("filePath", "load file " + mFile.getName());
        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
        }
        mLoadingKeepGoing = true;
        final SoundFile.ProgressListener listener =
                new SoundFile.ProgressListener() {
                    public boolean reportProgress(double fractionComplete) {
                        long now = getCurrentTime();
                        if (now - mLoadingLastUpdateTime > 100) {
                       /* mProgressDialog.setProgress(
                                (int) (mProgressDialog.getMax() * fractionComplete));*/
                            mLoadingLastUpdateTime = now;
                        }
                        return mLoadingKeepGoing;
                    }
                };
        mLoadSoundFileThread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    mSoundFile = SoundFile.create(mFile.getAbsolutePath(), listener);

                    if (mSoundFile != null) {
                        if (loadingDialog.isShowing()) {
                            loadingDialog.cancel();
                        }
                    }
                    mPlayer = new SamplePlayer(mSoundFile);
                    Runnable runnable = new Runnable() {
                        public void run() {
                            finishOpeningSoundFile();

                        }
                    };
                    mHandler.post(runnable);
//                    if (mLoadingKeepGoing) {
//                        Runnable runnable = new Runnable() {
//                            public void run() {
//                                finishOpeningSoundFile();
//                                Log.v("filePath","load file3 finishOpeningSoundFile();");
//                            }
//                        };
//                        mHandler.post(runnable);
//                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                }


            }
        });
        mLoadSoundFileThread.start();
    }


    private synchronized void onPlay(int startPosition) {
        if (mIsPlaying) {
            handlePause();
            return;
        }
        if (mPlayer == null) {
            // Not initialized yet
            return;
        }
        try {
            mPlayStartMsec = mWaveformView.pixelsToMillisecs(startPosition);
            mPlayEndMsec = mWaveformView.pixelsToMillisecs(mMaxPos);
            mPlayer.setOnCompletionListener(new SamplePlayer.OnCompletionListener() {
                @Override
                public void onCompletion() {

                    handlePause();

                }
            });
            mIsPlaying = true;

            mPlayer.seekTo(mPlayStartMsec);
            mPlayer.start();
            updateDisplay();
            enableDisableButtons();
        } catch (Exception e) {
            e.getStackTrace();
            return;
        }
    }


    private void enableDisableButtons() {
        if (mIsPlaying) {
            ivPlayOn.setVisibility(View.VISIBLE);
            ivPlayStop.setVisibility(View.GONE);
        } else {
            ivPlayOn.setVisibility(View.GONE);
            ivPlayStop.setVisibility(View.VISIBLE);
        }
    }


    private synchronized void handlePause() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.pause();
        }
        // mWaveformView.setPlayback(-1);
        mIsPlaying = false;
        enableDisableButtons();
    }

    public void pcmToWav(String wavPath) {
        PcmToWavUtil pcmToWavUtil = new PcmToWavUtil(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT);
        pcmToWavUtil.pcmToWav(FileUtils.getPcmFileAbsolutePath(audio.getAudioFile())
                , wavPath);
    }


    //解码音频文件成功
    private void finishOpeningSoundFile() {
        mWaveformView.setSoundFile(mSoundFile);
        mMaxPos = mWaveformView.maxPos();
        updateDisplay();
    }


    private synchronized void updateDisplay() {
        if (mIsPlaying) {
            tvNowTime.setText(AudioUtils.timeParse(mPlayer.getAudioTimeUs() / 1000));
            int now = mPlayer.getCurrentPosition();
            int frames = mWaveformView.millisecsToPixels(now);
            mWaveformView.setPlayback(frames);
            Log.e("TAG", "frames:" + frames);
            setOffsetGoalNoUpdate(frames - 20);
            if (now >= mPlayEndMsec) {
                handlePause();
            }
        }


        if (!mTouchDragging) {
            int offsetDelta;

            if (mFlingVelocity != 0) {
                offsetDelta = mFlingVelocity / 30;
                if (mFlingVelocity > 80) {
                    mFlingVelocity -= 80;
                } else if (mFlingVelocity < -80) {
                    mFlingVelocity += 80;
                } else {
                    mFlingVelocity = 0;
                }

                mOffset += offsetDelta;

                if (mOffset + mWidth / 2 > mMaxPos) {
                    mOffset = mMaxPos - mWidth / 2;
                    mFlingVelocity = 0;
                }
                if (mOffset < 0) {
                    mOffset = 0;
                    mFlingVelocity = 0;
                }
                mOffsetGoal = mOffset;
            } else {
                offsetDelta = mOffsetGoal - mOffset;

                if (offsetDelta > 10)
                    offsetDelta = offsetDelta / 10;
                else if (offsetDelta > 0)
                    offsetDelta = 1;
                else if (offsetDelta < -10)
                    offsetDelta = offsetDelta / 10;
                else if (offsetDelta < 0)
                    offsetDelta = -1;
                else
                    offsetDelta = 0;

                mOffset += offsetDelta;
            }
        }

        mWaveformView.setParameters(mOffset);


        mWaveformView.invalidate();

    }


    private void setOffsetGoalNoUpdate(int offset) {
        if (mTouchDragging) {
            return;
        }

        mOffsetGoal = offset;
        Log.e("TAG", "mofsetGoal:" + mOffsetGoal);
        Log.e("TAG", "mMaxPos:" + mMaxPos);
        if (mOffsetGoal + 20 > mMaxPos)
            mOffsetGoal = mMaxPos - 20;
        if (mOffsetGoal < 0)
            mOffsetGoal = 0;
    }


    @OnClick(R.id.id_before_iv)
    void goBefore() {
        if (mIsPlaying) {
            int newPos = mPlayer.getCurrentPosition() - 5000;
            if (newPos < mPlayStartMsec)
                newPos = mPlayStartMsec;
            mPlayer.seekTo(newPos);
        }
    }


    @OnClick(R.id.id_after_iv)
    void goAfter() {
        if (mIsPlaying) {
            int newPos = 5000 + mPlayer.getCurrentPosition();
            if (newPos > mPlayEndMsec)
                newPos = mPlayEndMsec;
            mPlayer.seekTo(newPos);
        }

    }

    /**
     * 点击停止按钮，则开始播放
     */
    @OnClick(R.id.id_play_stop_iv)
    void clickStop() {
        mStartPos = mWaveformView.secondsToPixels(0.0);
        if (mStartPos != -1) {
            onPlay(mStartPos);
        }


    }

    /**
     * 点击正在播放的按钮，则停止
     */
    @OnClick(R.id.id_play_on_iv)
    void clickOn() {
        mStartPos = mWaveformView.secondsToPixels(0.0);
        if (mStartPos != -1) {
            onPlay(mStartPos);
        }


    }

    @OnClick(R.id.id_back_iv)
    void closeActivity() {
        //File file = new File(FileUtils.getPcmFileAbsolutePath(audio.getAudioFile()));
        //file.delete();
        File file1 = new File(FileUtils.getWAVFileAbsolutePath(audio.getAudioFile()));
        file1.delete();
        this.finish();
    }


    /**
     * WaveformView的监听方法
     *
     * @param x
     */
    @Override
    public void waveformTouchStart(float x) {
        mTouchDragging = true;
        mTouchStart = x;
        mTouchInitialOffset = mOffset;
        mFlingVelocity = 0;
        mWaveformTouchStartMsec = getCurrentTime();
    }

    @Override
    public void waveformTouchMove(float x) {
        mOffset = trap((int) (mTouchInitialOffset + (mTouchStart - x)));
        updateDisplay();
    }

    private int trap(int pos) {
        if (pos < 0)
            return 0;
        if (pos > mMaxPos)
            return mMaxPos;
        return pos;
    }

    @Override
    public void waveformTouchEnd() {
        mTouchDragging = false;
        mOffsetGoal = mOffset;

        long elapsedMsec = getCurrentTime() - mWaveformTouchStartMsec;
        if (elapsedMsec < 300) {
            if (mIsPlaying) {
                int seekMsec = mWaveformView.pixelsToMillisecs(
                        (int) (mTouchStart + mOffset));
                if (seekMsec >= mPlayStartMsec &&
                        seekMsec < mPlayEndMsec) {
                    mPlayer.seekTo(seekMsec);
                } else {
                    handlePause();
                }
            } else {
                onPlay((int) (mTouchStart + mOffset));
            }
        }
    }

    @Override
    public void waveformFling(float x) {
        mTouchDragging = false;
        mOffsetGoal = mOffset;
        mFlingVelocity = (int) (-x);
        updateDisplay();
    }

    private void closeThread(Thread thread) {
        if (thread != null && thread.isAlive()) {
            try {
                thread.join();
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    protected void onStop() {

        File file1 = new File(FileUtils.getWAVFileAbsolutePath(audio.getAudioFile()));
        if (file1.exists()) {
            file1.delete();

        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mLoadingKeepGoing = false;
        closeThread(mLoadSoundFileThread);
        mLoadSoundFileThread = null;

        if (mPlayer != null) {
            if (mPlayer.isPlaying() || mPlayer.isPaused()) {
                mPlayer.stop();
            }
            mPlayer.release();
            mPlayer = null;
        }
        File file1 = new File(FileUtils.getWAVFileAbsolutePath(audio.getAudioFile()));
        file1.delete();

        super.onDestroy();

    }

    /**
     * 每次绘制都会调用此方法
     */
    @Override
    public void waveformDraw() {
        mWidth = mWaveformView.getMeasuredWidth();
        Log.e("TAG", "mWidth：" + mWidth);
        if (mIsPlaying) {
            updateDisplay();
        }

    }

    @Override
    public void waveformZoomIn() {

    }

    @Override
    public void waveformZoomOut() {

    }
}
