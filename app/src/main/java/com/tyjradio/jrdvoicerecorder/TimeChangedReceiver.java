package com.tyjradio.jrdvoicerecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.tyjradio.jrdvoicerecorder.utils.StatusRecorderUtils;


public class TimeChangedReceiver extends BroadcastReceiver {

    private static final String ACTION = "android.intent.action.TIME_SET";


    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(ACTION)){
            Log.d("timechange", "onReceive: " + intent.getAction());
            StatusRecorderUtils.writeTimeChangedLog(context);
        }

    }



}
