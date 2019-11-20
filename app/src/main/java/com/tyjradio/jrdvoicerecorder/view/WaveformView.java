package com.tyjradio.jrdvoicerecorder.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.tyjradio.jrdvoicerecorder.R;
import com.tyjradio.jrdvoicerecorder.utils.SoundFile;

public class WaveformView extends View {
    public interface WaveformListener {
        public void waveformTouchStart(float x);
        public void waveformTouchMove(float x);
        public void waveformTouchEnd();
        public void waveformFling(float x);
        public void waveformDraw();
        public void waveformZoomIn();
        public void waveformZoomOut();
    };


    private WaveformListener mListener;

    private SoundFile mSoundFile;
    //采样率
    private int mSampleRate;
    private int mSamplesPerFrame;
    private int[] mHeightsAtThisZoomLevel;
    private int[] mLenByZoomLevel;
    private double[][] mValuesByZoomLevel;
    private double[] mZoomFactorByZoomLevel;
    private int mZoomLevel;
    private int mNumZoomLevels;
    private boolean mInitialized;
    private int mOffset;
    private GestureDetector mGestureDetector;

    //画笔
    //规制音轨矩形(播放过的)
    private Paint mLinePaint;
    //没有播放过
    private Paint mLinePaintNo;


    //绘制底部线
    private Paint mBottomLinePaint;
    //进度的线
    private Paint mPlayLinePaint;

    public static final int MARGIN_BOTTOM=72;


    //播放的位置
    private int mPlaybackPos;

    public WaveformView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // We don't want keys, the markers get these
        setFocusable(false);
        Resources res = getResources();

        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(false);
        mLinePaint.setStyle(Paint.Style.FILL);//设置填满
        mLinePaint.setColor(res.getColor(R.color.main_color));


        mLinePaintNo = new Paint();
        mLinePaintNo.setAntiAlias(false);
        mLinePaintNo.setStyle(Paint.Style.FILL);//设置填满
        mLinePaintNo.setColor(res.getColor(R.color.main_gray));

        //设置底部线画笔的属性
        mBottomLinePaint=new Paint();
        mBottomLinePaint.setAntiAlias(false);
        mBottomLinePaint.setColor(res.getColor(R.color.main_gray));
        mBottomLinePaint.setStrokeWidth(2);

        mPlayLinePaint=new Paint();
        mPlayLinePaint.setAntiAlias(false);
        mPlayLinePaint.setColor(res.getColor(R.color.main_color));

        mPlaybackPos = -1;

        mGestureDetector = new GestureDetector(
                context,
                new GestureDetector.SimpleOnGestureListener() {
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
                        mListener.waveformFling(vx);
                        return true;
                    }
                }
        );


    }

    /**
     * 设置soundFile
     * @param soundFile
     */
    public void setSoundFile(SoundFile soundFile) {
        mSoundFile = soundFile;
        mSampleRate = mSoundFile.getSampleRate();
        mSamplesPerFrame = mSoundFile.getSamplesPerFrame();
        computeDoublesForAllZoomLevels();
        mHeightsAtThisZoomLevel = null;
    }



/*    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event)) {
            return true;
        }

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mListener.waveformTouchStart(event.getX());
                break;
            case MotionEvent.ACTION_MOVE:
                mListener.waveformTouchMove(event.getX());
                break;
            case MotionEvent.ACTION_UP:
                mListener.waveformTouchEnd();
                break;
        }
        return true;
    }*/

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mSoundFile == null)
            return;

        if (mHeightsAtThisZoomLevel == null)
            computeIntsForThisZoomLevel();


        // Draw waveform
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        int start = mOffset;
        Log.e("TAG","offset:"+mOffset);
        int width = mHeightsAtThisZoomLevel.length - start;
        Log.e("TAG","start:"+start);
        Log.e("TAG","width:"+width);
        Log.e("TAG","mHeightsAtThisZoomLevel.length:"+mHeightsAtThisZoomLevel.length);
        Log.e("TAG","mPlaybackPos"+mPlaybackPos);
        int ctr = measuredHeight / 2;

        if (width > measuredWidth)
            width = measuredWidth;

        int i = 0;


        //绘制底部的基准线
        canvas.drawLine(0,measuredHeight-MARGIN_BOTTOM,
                measuredWidth,measuredHeight-MARGIN_BOTTOM,mBottomLinePaint);

        // Draw waveform
        for (i = 0; i < width; i++) {
            //绘制矩形
            Paint paint=null;
            if(i+start<=mPlaybackPos){
                paint=mLinePaint;
            }else {
                paint=mLinePaintNo;
            }
            canvas.drawRect(i*16+30,measuredHeight-2*mHeightsAtThisZoomLevel[start + i]-MARGIN_BOTTOM,
                    i*16+8+30,measuredHeight-MARGIN_BOTTOM,paint);

            if(i+start==mPlaybackPos){
                //绘制播放进度条线
                //底部的圆
                canvas.drawCircle(i*16+30+2,getMeasuredHeight()-16,16,mPlayLinePaint);
                //线条
                mPlayLinePaint.setStrokeWidth(4);
                canvas.drawLine(i*16+30,0,i*16+30+4,getMeasuredHeight()-16,mPlayLinePaint);
            }

        }

        if (mListener != null) {
            mListener.waveformDraw();
        }

    }

    public void setListener(WaveformListener listener) {
        mListener = listener;
    }


    public int millisecsToPixels(int msecs) {
        double z = mZoomFactorByZoomLevel[mZoomLevel];
        return (int)((msecs * 1.0 * mSampleRate * z) /
                (1000.0 * mSamplesPerFrame) + 0.5);
    }

    public int secondsToPixels(double seconds) {
        if(mZoomFactorByZoomLevel != null){
            double z = mZoomFactorByZoomLevel[mZoomLevel];
            return (int)(z * seconds * mSampleRate / mSamplesPerFrame + 0.5);
        }
        else{
            return -1;
        }

    }

    public int pixelsToMillisecs(int pixels) {
        double z = mZoomFactorByZoomLevel[mZoomLevel];
        return (int)(pixels * (1000.0 * mSamplesPerFrame) /
                (mSampleRate * z) + 0.5);
    }


    public int maxPos() {
        return mLenByZoomLevel[mZoomLevel];
    }

    public void setPlayback(int pos) {
        mPlaybackPos = pos;
    }

    public int getPlayback(){
        return mPlaybackPos;
    }

    public void setParameters(int offset) {
        mOffset = offset;
    }


    /**
     * Called once when a new sound file is added
     */
    private void computeDoublesForAllZoomLevels() {
        int numFrames = mSoundFile.getNumFrames();
        int[] frameGains = mSoundFile.getFrameGains();
        double[] smoothedGains = new double[numFrames];
        if (numFrames == 1) {
            smoothedGains[0] = frameGains[0];
        } else if (numFrames == 2) {
            smoothedGains[0] = frameGains[0];
            smoothedGains[1] = frameGains[1];
        } else if (numFrames > 2) {
            smoothedGains[0] = (double)(
                    (frameGains[0] / 2.0) +
                            (frameGains[1] / 2.0));
            for (int i = 1; i < numFrames - 1; i++) {
                smoothedGains[i] = (double)(
                        (frameGains[i - 1] / 3.0) +
                                (frameGains[i    ] / 3.0) +
                                (frameGains[i + 1] / 3.0));
            }
            smoothedGains[numFrames - 1] = (double)(
                    (frameGains[numFrames - 2] / 2.0) +
                            (frameGains[numFrames - 1] / 2.0));
        }

        // Make sure the range is no more than 0 - 255
        double maxGain = 1.0;
        for (int i = 0; i < numFrames; i++) {
            if (smoothedGains[i] > maxGain) {
                maxGain = smoothedGains[i];
            }
        }
        double scaleFactor = 1.0;
        if (maxGain > 255.0) {
            scaleFactor = 255 / maxGain;
        }

        // Build histogram of 256 bins and figure out the new scaled max
        maxGain = 0;
        int gainHist[] = new int[256];
        for (int i = 0; i < numFrames; i++) {
            int smoothedGain = (int)(smoothedGains[i] * scaleFactor);
            if (smoothedGain < 0)
                smoothedGain = 0;
            if (smoothedGain > 255)
                smoothedGain = 255;

            if (smoothedGain > maxGain)
                maxGain = smoothedGain;

            gainHist[smoothedGain]++;
        }

        // Re-calibrate the min to be 5%
        double minGain = 0;
        int sum = 0;
        while (minGain < 255 && sum < numFrames / 20) {
            sum += gainHist[(int)minGain];
            minGain++;
        }

        // Re-calibrate the max to be 99%
        sum = 0;
        while (maxGain > 2 && sum < numFrames / 100) {
            sum += gainHist[(int)maxGain];
            maxGain--;
        }

        // Compute the heights
        double[] heights = new double[numFrames];
        double range = maxGain - minGain;
        for (int i = 0; i < numFrames; i++) {
            double value = (smoothedGains[i] * scaleFactor - minGain) / range;
            if (value < 0.0)
                value = 0.0;
            if (value > 1.0)
                value = 1.0;
            heights[i] = value * value;
        }

        mNumZoomLevels = 5;
        mLenByZoomLevel = new int[5];
        mZoomFactorByZoomLevel = new double[5];
        mValuesByZoomLevel = new double[5][];

        // Level 0 is doubled, with interpolated values
        mLenByZoomLevel[0] = numFrames * 2;
        mZoomFactorByZoomLevel[0] = 2.0;
        mValuesByZoomLevel[0] = new double[mLenByZoomLevel[0]];
        if (numFrames > 0) {
            mValuesByZoomLevel[0][0] = 0.5 * heights[0];
            mValuesByZoomLevel[0][1] = heights[0];
        }
        for (int i = 1; i < numFrames; i++) {
            mValuesByZoomLevel[0][2 * i] = 0.5 * (heights[i - 1] + heights[i]);
            mValuesByZoomLevel[0][2 * i + 1] = heights[i];
        }

        // Level 1 is normal
        mLenByZoomLevel[1] = numFrames;
        mValuesByZoomLevel[1] = new double[mLenByZoomLevel[1]];
        mZoomFactorByZoomLevel[1] = 1.0;
        for (int i = 0; i < mLenByZoomLevel[1]; i++) {
            mValuesByZoomLevel[1][i] = heights[i];
        }

        // 3 more levels are each halved
        for (int j = 2; j < 5; j++) {
            mLenByZoomLevel[j] = mLenByZoomLevel[j - 1] / 2;
            mValuesByZoomLevel[j] = new double[mLenByZoomLevel[j]];
            mZoomFactorByZoomLevel[j] = mZoomFactorByZoomLevel[j - 1] / 2.0;
            for (int i = 0; i < mLenByZoomLevel[j]; i++) {
                mValuesByZoomLevel[j][i] =
                        0.5 * (mValuesByZoomLevel[j - 1][2 * i] +
                                mValuesByZoomLevel[j - 1][2 * i + 1]);
            }
        }

        if (numFrames > 5000) {
            mZoomLevel = 3;
        } else if (numFrames > 1000) {
            mZoomLevel = 2;
        } else if (numFrames > 300) {
            mZoomLevel = 1;
        } else {
            mZoomLevel = 0;
        }

        mInitialized = true;
    }

    /**
     * Called the first time we need to draw when the zoom level has changed
     * or the screen is resized
     */
    private void computeIntsForThisZoomLevel() {
        int halfHeight = (getMeasuredHeight() / 2) - 1;
        mHeightsAtThisZoomLevel = new int[mLenByZoomLevel[mZoomLevel]];
        for (int i = 0; i < mLenByZoomLevel[mZoomLevel]; i++) {
            mHeightsAtThisZoomLevel[i] =
                    (int)(mValuesByZoomLevel[mZoomLevel][i] * halfHeight);
        }
    }



}
