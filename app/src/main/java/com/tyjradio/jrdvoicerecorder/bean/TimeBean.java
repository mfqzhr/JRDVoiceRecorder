package com.tyjradio.jrdvoicerecorder.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

public class TimeBean implements MessageBody, Serializable {

    private String currentTime;

    @JSONField(name="Time")
    public String getCurrentTime() {
        return currentTime;
    }

    @JSONField(name="Time")
    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }



}
