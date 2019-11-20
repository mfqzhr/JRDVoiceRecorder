package com.tyjradio.jrdvoicerecorder;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.tyjradio.jrdvoicerecorder.bean.AudioRecorderItemBean;
import com.tyjradio.jrdvoicerecorder.utils.AudioUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

//详情页面
public class DetailActivity extends AppCompatActivity {
    private String fileUrl="";
    private AudioRecorderItemBean audio;

    @BindView(R.id.id_title_tv)
    TextView tvTitle;

    @BindView(R.id.CallingTime)
    TextView tvCallingTime;

    @BindView(R.id.DurationTime)
    TextView tvDurationTime;

    @BindView(R.id.MachineSerialNum)
    TextView tvMachineSerialNum;

    @BindView(R.id.SelfPhoneNum)
    TextView tvSelfPhoneNum;

    @BindView(R.id.CalledPhoneNum)
    TextView tvCalledPhoneNum;

    @BindView(R.id.ZipCode)
    TextView tvZipCode;

    @BindView(R.id.ChannelCode)
    TextView tvChannelCode;

    @BindView(R.id.ChannelType)
    TextView tvChannelType;

    @BindView(R.id.RecorderType)
    TextView tvRecorderType;

    @BindView(R.id.REFrequency)
    TextView tvREFrequency;

    @BindView(R.id.EmFrequency)
    TextView tvEmFrequency;




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        audio= (AudioRecorderItemBean) getIntent().getSerializableExtra("audio");
        tvTitle.setText(audio.getAudioFile());
        tvDurationTime.setText(AudioUtils.timeParse(audio.getDurationTime()));
        tvCallingTime.setText(audio.getCallingTime());
        tvMachineSerialNum.setText(audio.getMachineSerialNum());
        tvSelfPhoneNum.setText(audio.getSelfPhoneNum());
        tvCalledPhoneNum.setText(audio.getCalledPhoneNum());
        tvZipCode.setText(audio.getZipCode());
        tvChannelCode.setText(audio.getChannelCode() + "");
        tvChannelType.setText(audio.getChannelType());
        tvRecorderType.setText(audio.getRecorderType());
        tvREFrequency.setText(audio.getREFrequency() + "");
        tvEmFrequency.setText(audio.getEmFrequency() + "");

    }

    @OnClick(R.id.id_play_ll)
    void goPlayActivity(){
        Intent intent=new Intent();
        intent.setClass(this,PlayActivity.class);
        intent.putExtra("audio",audio);
        startActivity(intent);
    }

    @OnClick(R.id.id_back_iv)
    void closeActivity(){
        this.finish();
    }
}
