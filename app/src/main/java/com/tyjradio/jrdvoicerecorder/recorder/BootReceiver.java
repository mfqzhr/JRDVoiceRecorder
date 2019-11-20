package com.tyjradio.jrdvoicerecorder.recorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;


public class BootReceiver extends BroadcastReceiver {

    private static final String ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //throw new UnsupportedOperationException("Not yet implemented");

        if(intent.getAction().equals(ACTION)){
            Log.v("test1","service" + Build.VERSION.SDK_INT);

            if (Build.VERSION.SDK_INT >=26) {
                context.startForegroundService(new Intent(context, VoiceRecorderService.class));
            } else {
                context.startService(new Intent(context, VoiceRecorderService.class));
            }
        }

    }
}
