package com.tyjradio.jrdvoicerecorder.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.tyjradio.jrdvoicerecorder.R;
import com.tyjradio.jrdvoicerecorder.utils.Utils;

public class MainPop extends PopupWindow implements View.OnClickListener {
    private ChangeSelectListener changeSelectListener;

    @Override
    public void onClick(View v) {
        tv1.setBackgroundColor(context.getResources().getColor(R.color.color_4FADCB));
        tv2.setBackgroundColor(context.getResources().getColor(R.color.color_4FADCB));
        tv3.setBackgroundColor(context.getResources().getColor(R.color.color_4FADCB));
/*        tv4.setBackgroundColor(context.getResources().getColor(R.color.color_4FADCB));
        tv5.setBackgroundColor(context.getResources().getColor(R.color.color_4FADCB));
        tv6.setBackgroundColor(context.getResources().getColor(R.color.color_4FADCB));*/
        switch (v.getId()){
            case R.id.id_select_1:
                tv1.setBackgroundColor(context.getResources().getColor(R.color.color_FF379BBC));
                changeSelectListener.setChange("全部录音");
                break;
            case R.id.id_select_2:
                tv2.setBackgroundColor(context.getResources().getColor(R.color.color_FF379BBC));
                changeSelectListener.setChange("发射方");
                break;
            case R.id.id_select_3:
                tv3.setBackgroundColor(context.getResources().getColor(R.color.color_FF379BBC));
                changeSelectListener.setChange("接收方");
                break;
/*            case R.id.id_select_4:
                tv4.setBackgroundColor(context.getResources().getColor(R.color.color_FF379BBC));
                changeSelectListener.setChange("电话");
                break;
            case R.id.id_select_5:
                tv5.setBackgroundColor(context.getResources().getColor(R.color.color_FF379BBC));
                changeSelectListener.setChange("窄带对讲");
                break;
            case R.id.id_select_6:
                tv6.setBackgroundColor(context.getResources().getColor(R.color.color_FF379BBC));
                changeSelectListener.setChange("POC对讲");
                break;*/
        }
    }

    public interface ChangeSelectListener{
       void setChange(String name);
    }


    private Context context;
    private View mainView;
    private TextView tv1;
    private TextView tv2;
    private TextView tv3;
/*    private TextView tv4;
    private TextView tv5;
    private TextView tv6;*/
    public MainPop(Context context,ChangeSelectListener changeSelectListener){
        super(context);
        this.changeSelectListener=changeSelectListener;
        this.context=context;
        mainView=LayoutInflater.from(context).inflate(R.layout.main_view,null);
        setContentView(mainView);
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        //设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(Utils.dip2px(context, 143));
        //  设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(false);
        //   设置背景透明
        this.setBackgroundDrawable(new ColorDrawable(0x00000000));
        tv1=mainView.findViewById(R.id.id_select_1);
        tv2=mainView.findViewById(R.id.id_select_2);
        tv3=mainView.findViewById(R.id.id_select_3);
/*        tv4=mainView.findViewById(R.id.id_select_4);
        tv5=mainView.findViewById(R.id.id_select_5);
        tv6=mainView.findViewById(R.id.id_select_6);*/
        tv1.setOnClickListener(this);
        tv2.setOnClickListener(this);
        tv3.setOnClickListener(this);
/*        tv4.setOnClickListener(this);
        tv5.setOnClickListener(this);
        tv6.setOnClickListener(this);*/


    }
}
