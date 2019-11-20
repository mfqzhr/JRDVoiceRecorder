package com.tyjradio.jrdvoicerecorder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.view.View;
import android.widget.ImageView;


/**
 * @ author 穆樊强
 * @ date:
 */

public class SplashActivity extends Activity implements ViewPropertyAnimatorListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initData();
    }

    protected void initData() {
        // ViewCompat萎缩动画效果,监听动画结束后跳转到onAnimationEnd
        ImageView imageView = (ImageView) findViewById(R.id.imageView_splash);
        ViewCompat.animate(imageView).scaleX(1.0f).scaleY(1.0f)
                .setListener(this).setDuration(2000);

    }

    @Override
    public void onAnimationStart(View view) {

    }

    @Override
    public void onAnimationEnd(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();

    }

    @Override
    public void onAnimationCancel(View view) {

    }
}
