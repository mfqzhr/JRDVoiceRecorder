package com.tyjradio.jrdvoicerecorder.bean;

import com.alibaba.fastjson.annotation.JSONField;
import java.io.Serializable;


public class AudioFileBean implements MessageBody, Serializable {



    private long sum = 0;
    private int currentPoint = 0;

    private String path;
    private String data;

    @JSONField(name="Sum")
    public long getSum() {
        return sum;
    }
    @JSONField(name="Sum")
    public void setSum(long sum) {
        this.sum = sum;
    }

    @JSONField(name="CurrentPoint")
    public int getCurrentPoint() {
        return currentPoint;
    }

    @JSONField(name="CurrentPoint")
    public void setCurrentPoint(int currentPoint) {
        this.currentPoint = currentPoint;
    }
    @JSONField(name="Data")
    public String getData() {
        return data;
    }
    @JSONField(name="Data")
    public void setData(String data) {
        this.data = data;
    }
    @JSONField(name="Path")
    public String getPath() {
        return path;
    }
    @JSONField(name="Path")
    public void setPath(String path) {
        this.path = path;
    }

}
