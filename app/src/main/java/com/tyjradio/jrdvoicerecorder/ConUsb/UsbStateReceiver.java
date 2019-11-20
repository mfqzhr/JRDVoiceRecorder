package com.tyjradio.jrdvoicerecorder.ConUsb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.Toast;

import com.tyjradio.jrdvoicerecorder.recorder.VoiceRecorderService;
import com.tyjradio.jrdvoicerecorder.utils.Utils;

/*
**这个service的目的是为了，在插入usb的时候开启ConnService

 */

public class UsbStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //if(intent.getAction().equals("android.hardware.usb.action.USB_STATE")){
        Log.v("test",intent.toString());
        if(intent.getAction().equals("android.hardware.usb.action.USB_STATE")){
            if (intent.getExtras().getBoolean("connected")){
                //Toast.makeText(context, "插入", Toast.LENGTH_LONG).show();
                // 海能达授权后，在此处完成对开发模式的打开，可以进行adb调试，连接
                boolean isRunning = Utils.isServiceRunning(context,"com.tyjradio.jrdvoicerecorder.ConUsb.ConnService");
                if (isRunning == false){
                    Intent ConnServiceIntent = new Intent(context,ConnService.class);
                    context.startService(ConnServiceIntent);
                }
            }
            else {
                //Toast.makeText(context, "拔出", Toast.LENGTH_LONG).show();
                boolean isRunning = Utils.isServiceRunning(context,"com.tyjradio.jrdvoicerecorder.ConUsb.ConnService");
                if (isRunning == true){
                    Intent ConnServiceIntent = new Intent(context,ConnService.class);
                    context.stopService(ConnServiceIntent);
                }

            }
        }

    }
}
