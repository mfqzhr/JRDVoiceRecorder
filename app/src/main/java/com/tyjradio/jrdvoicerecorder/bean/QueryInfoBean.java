package com.tyjradio.jrdvoicerecorder.bean;

import java.io.Serializable;
import com.alibaba.fastjson.annotation.JSONField;

public class QueryInfoBean  implements MessageBody, Serializable {

    private int currentP;
    private int size;
    private AudioRecorderItemBean audioRecorderBean;

    @JSONField(name="CurrentPoint")
    public int getCurrentP() {
        return currentP;
    }

    @JSONField(name="CurrentPoint")
    public void setCurrentP(int currentP) {
        this.currentP = currentP;
    }

    @JSONField(name="Size")
    public int getSize() {
        return size;
    }

    @JSONField(name="Size")
    public void setSize(int size) {
        this.size = size;
    }
    @JSONField(name="Data")
    public AudioRecorderItemBean getAudioRecorderBean() {
        return audioRecorderBean;
    }
    @JSONField(name="Data")
    public void setAudioRecorderBean(AudioRecorderItemBean audioRecorderBean) {
        this.audioRecorderBean = audioRecorderBean;
    }


}
