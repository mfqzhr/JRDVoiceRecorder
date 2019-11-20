package com.tyjradio.jrdvoicerecorder.view;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.IDNA;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.tyjradio.jrdvoicerecorder.InfoActivity;
import com.tyjradio.jrdvoicerecorder.R;


public class MorePopWindow extends PopupWindow {
    private View conentView;
    private String userId;
    public MorePopWindow(final Activity context) {
        final LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        conentView = inflater.inflate(R.layout.popup_window_more, null);
        int h = context.getWindowManager().getDefaultDisplay().getHeight();
        int w = context.getWindowManager().getDefaultDisplay().getWidth();
        this.setContentView(conentView);
        this.setWidth(LayoutParams.WRAP_CONTENT);
        this.setHeight(LayoutParams.WRAP_CONTENT);
        //设置点击隐藏的属性
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        this.update();
        ColorDrawable dw = new ColorDrawable(0000000000);
        this.setBackgroundDrawable(dw);
        RelativeLayout getMore = (RelativeLayout) conentView.findViewById(R.id.getmore);
        getMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击弹出按钮的操作
                Intent intent=new Intent(context, InfoActivity.class);
                context.startActivity(intent);
                MorePopWindow.this.dismiss();
            }
        });

    }

    public void showPopupWindow(View parent) {
        if (!this.isShowing()) {
            this.showAsDropDown(parent, parent.getLayoutParams().width / 2, 10);
        } else {
            this.dismiss();
        }
    }
}