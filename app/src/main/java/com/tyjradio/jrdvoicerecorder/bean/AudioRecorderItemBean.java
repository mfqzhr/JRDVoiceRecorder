package com.tyjradio.jrdvoicerecorder.bean;

import java.io.Serializable;
import java.util.Objects;

public class AudioRecorderItemBean  implements Serializable {
    //录音编号
    private int ID;
    //区号
    private String ZipCode;
    //本机ID
    private String SelfPhoneNum;
    //远端ID
    private  String CalledPhoneNum;
    //录音时间
    private String CallingTime;
    //录音持续时间
    private  long DurationTime;
    //信道号
    private int ChannelCode;
    //信道类型
    private String ChannelType;
    //录音类型
    private String RecorderType;
    //接收频率
    private double REFrequency;
    //发射频率
    private double EmFrequency;
    //收发
    private String OutOrInput;
    //机器序列号
    private String MachineSerialNum;
    //录音文件名称
    private String AudioFile;
    //是否被选中
    private boolean isSelect;
    //是否正在播放
    private boolean isPlaying;
    //开始的位置
    private long StartPosition;
    //文件的长度
    private long Length;

    //大文件被重写的次数（用于重写删除文件用）
    private int Count;


    public int getChannelCode() {
        return ChannelCode;
    }

    public void setChannelCode(int channelCode) {
        ChannelCode = channelCode;
    }



    public String getAudioFile() {
        return AudioFile;
    }

    public void setAudioFile(String audioFile) {
        AudioFile = audioFile;
    }

    public long getStartPosition() {
        return StartPosition;
    }

    public void setStartPosition(long startPosition) {
        this.StartPosition = startPosition;
    }

    public long getLength() {
        return Length;
    }

    public void setLength(long length) {
        this.Length = length;
    }

    public int getCount() {
        return Count;
    }

    public void setCount(int count) {
        this.Count = count;
    }

    public int getId() {
        return ID;
    }
    public void setId(int id) {
        this.ID = id;
    }

    public String getZipCode() {
        return ZipCode;
    }

    public String getSelfPhoneNum() {
        return SelfPhoneNum;
    }

    public String getCalledPhoneNum() {
        return CalledPhoneNum;
    }

    public String getCallingTime() {
        return CallingTime;
    }

    public long getDurationTime() {
        return DurationTime;
    }

    public String getChannelType() {
        return ChannelType;
    }

    public String getRecorderType() {
        return RecorderType;
    }

    public double getREFrequency() {
        return REFrequency;
    }

    public double getEmFrequency() {
        return EmFrequency;
    }

    public String getOutOrInput() {
        return OutOrInput;
    }

    public String getMachineSerialNum() {
        return MachineSerialNum;
    }


    public void setZipCode(String zipCode) {
        ZipCode = zipCode;
    }

    public void setSelfPhoneNum(String selfPhoneNum) {
        SelfPhoneNum = selfPhoneNum;
    }

    public void setCalledPhoneNum(String calledPhoneNum) {
        CalledPhoneNum = calledPhoneNum;
    }

    public void setCallingTime(String callingTime) {
        CallingTime = callingTime;
    }

    public void setDurationTime(long durationTime) {
        DurationTime = durationTime;
    }

    public void setChannelType(String channelType) {
        ChannelType = channelType;
    }

    public void setRecorderType(String recorderType) {
        RecorderType = recorderType;
    }

    public void setREFrequency(double REFrequency) {
        this.REFrequency = REFrequency;
    }

    public void setEmFrequency(double emFrequency) {
        EmFrequency = emFrequency;
    }

    public void setOutOrInput(String outOrInput) {
        OutOrInput = outOrInput;
    }

    public void setMachineSerialNum(String machineSerialNum) {
        MachineSerialNum = machineSerialNum;
    }


    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }


    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AudioRecorderItemBean that = (AudioRecorderItemBean) o;
        return ID == that.ID &&
                DurationTime == that.DurationTime &&
                ChannelCode == that.ChannelCode &&
                Double.compare(that.REFrequency, REFrequency) == 0 &&
                Double.compare(that.EmFrequency, EmFrequency) == 0 &&
                isSelect == that.isSelect &&
                isPlaying == that.isPlaying &&
                StartPosition == that.StartPosition &&
                Length == that.Length &&
                Count == that.Count &&
                Objects.equals(ZipCode, that.ZipCode) &&
                Objects.equals(SelfPhoneNum, that.SelfPhoneNum) &&
                Objects.equals(CalledPhoneNum, that.CalledPhoneNum) &&
                Objects.equals(CallingTime, that.CallingTime) &&
                Objects.equals(ChannelType, that.ChannelType) &&
                Objects.equals(RecorderType, that.RecorderType) &&
                Objects.equals(OutOrInput, that.OutOrInput) &&
                Objects.equals(MachineSerialNum, that.MachineSerialNum) &&
                Objects.equals(AudioFile, that.AudioFile);
    }

    @Override
    public int hashCode() {

        return Objects.hash(ID, ZipCode, SelfPhoneNum, CalledPhoneNum, CallingTime, DurationTime, ChannelCode, ChannelType, RecorderType, REFrequency, EmFrequency, OutOrInput, MachineSerialNum, AudioFile, isSelect, isPlaying, StartPosition, Length, Count);
    }

    @Override
    public String toString() {
        return "AudioRecorderItemBean{" +
                "ID=" + ID +
                ", ZipCode='" + ZipCode + '\'' +
                ", SelfPhoneNum='" + SelfPhoneNum + '\'' +
                ", CalledPhoneNum='" + CalledPhoneNum + '\'' +
                ", CallingTime='" + CallingTime + '\'' +
                ", DurationTime=" + DurationTime +
                ", ChannelCode=" + ChannelCode +
                ", ChannelType='" + ChannelType + '\'' +
                ", RecorderType='" + RecorderType + '\'' +
                ", REFrequency=" + REFrequency +
                ", EmFrequency=" + EmFrequency +
                ", OutOrInput='" + OutOrInput + '\'' +
                ", MachineSerialNum='" + MachineSerialNum + '\'' +
                ", AudioFile='" + AudioFile + '\'' +
                ", isSelect=" + isSelect +
                ", isPlaying=" + isPlaying +
                ", startPosition=" + StartPosition +
                ", length=" + Length +
                ", count=" + Count +
                '}';
    }
}
