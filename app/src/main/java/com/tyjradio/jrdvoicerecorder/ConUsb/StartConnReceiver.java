package com.tyjradio.jrdvoicerecorder.ConUsb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;


import com.tyjradio.jrdvoicerecorder.utils.Utils;

public class StartConnReceiver extends BroadcastReceiver {
    public static String COM_START_CONNECTION_ACTION = "com.JRDBroadcastReceiver.StartConnectionAction";
    public static final int port = 10086;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //throw new UnsupportedOperationException("Not yet implemented");
        //Toast.makeText(context, "oooo", Toast.LENGTH_LONG).show();
        //启动ConnService
        /**
         * 这里要首先判断connService服务是否开启，如果没有则开启，通知Notify告知USB已经连接
         */
        if(intent.getBooleanExtra("state",true)== true){

            boolean isRunning = Utils.isServiceRunning(context,"com.tyjradio.jrdvoicerecorder.ConUsb.ConnService");
            if (isRunning == false){
                Intent ConnServiceIntent = new Intent(context,ConnService.class);
                context.startService(ConnServiceIntent);
            }

        }
        else if (intent.getBooleanExtra("state",true) == false){
            boolean isRunning = Utils.isServiceRunning(context,"com.tyjradio.jrdvoicerecorder.ConUsb.ConnService");
            if (isRunning == true){
                Intent ConnServiceIntent = new Intent(context,ConnService.class);
                context.stopService(ConnServiceIntent);
            }
        }
    }
}
