package com.tyjradio.jrdvoicerecorder;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.tyjradio.jrdvoicerecorder.recorder.VoiceRecorderService;

//设置页面
public class SettingActivity extends AppCompatActivity {
    @BindView(R.id.id_record_open_iv)
    ImageView ivOpen;

    @BindView(R.id.id_record_close_iv)
    ImageView ivClose;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
    }


    @OnClick(R.id.id_back_iv)
    void closeActivity(){
        this.finish();
    }

    @OnClick(R.id.id_record_open_iv)
    void clickOpen(){
        ivOpen.setVisibility(View.GONE);
        ivClose.setVisibility(View.VISIBLE);

        //intent VoiceRecorderService to open EnableRecord
        Intent voiceServiceIntent = new Intent(this, VoiceRecorderService.class);
        startService(voiceServiceIntent);


    }

    @OnClick(R.id.id_record_close_iv)
    void clickClose(){
        ivOpen.setVisibility(View.VISIBLE);
        ivClose.setVisibility(View.GONE);

        //intent VoiceRecorderService to open EnableRecord
        Intent voiceServiceIntent = new Intent(this, VoiceRecorderService.class);
        startService(voiceServiceIntent);

    }

}
