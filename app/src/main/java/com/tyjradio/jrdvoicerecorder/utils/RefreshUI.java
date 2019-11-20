package com.tyjradio.jrdvoicerecorder.utils;


//用于刷新界面
public class RefreshUI {
    private static RefreshUI refreshUI;


    //单例模式
    public static RefreshUI getInstance() {
        if (refreshUI == null) {
            refreshUI = new RefreshUI();
        }
        return refreshUI;

    }

    private  OnAudioRecorderCompletionListener onAudioRecorderCompletionListener;


    public interface OnAudioRecorderCompletionListener{
        void onAudioRecorderCompletion();
    }

    public void setOnAudioRecorderCompletionListener(OnAudioRecorderCompletionListener onAudioRecorderCompletionListener) {
        this.onAudioRecorderCompletionListener = onAudioRecorderCompletionListener;
    }

    public void recorderCompletion() {
        onAudioRecorderCompletionListener.onAudioRecorderCompletion();
    }


}
