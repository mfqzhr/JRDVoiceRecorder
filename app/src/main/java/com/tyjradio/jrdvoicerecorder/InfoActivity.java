package com.tyjradio.jrdvoicerecorder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;


import com.tyjradio.jrdvoicerecorder.utils.SharePreferencesHelper;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class InfoActivity extends AppCompatActivity {

    @BindView(R.id.textInfo)
    TextView textInfo;
    @BindView(R.id.image_info)
    ImageView imageInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        ButterKnife.bind(this);
        String text = "1.1.1.20190730_beta";
        Log.d("version", text);
        textInfo.setText("版本号: " + text);
    }

    @OnClick(R.id.id_back_iv1)
    void closeActivity(){
        this.finish();
    }
}
