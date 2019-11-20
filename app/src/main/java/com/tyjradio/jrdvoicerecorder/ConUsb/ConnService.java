package com.tyjradio.jrdvoicerecorder.ConUsb;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.tyjradio.jrdvoicerecorder.bean.AudioRecorderItemBean;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import java.util.Map;
import java.util.HashMap;


/**
 * Created by yangjian on 2018/12/24.
 *  由于移动设备的网络的复杂性，经常会出现网络断开，如果没有心跳包的检测，
 *  客户端只会在需要发送数据的时候才知道自己已经断线，会延误，甚至丢失服务器发送过来的数据。
 */


public class ConnService extends Service {

    /**心跳频率*/
    private static final long HEART_BEAT_RATE = 3 ;
    /**服务器ip地址*/
    public static final String HOST = "127.0.0.1";
    /**心跳发送的时间*/
    private long sendTime = 0L;
    /**socketserver*/
    ServerSocket m_serverSocket = null;
    /**服务器端口号*/
    final int SERVER_PORT = 10086;
    /**是否保持连接*/
    public static Boolean mainThreadFlag = true;
    /**是否停止所有的线程*/
    //public volatile static Boolean m_bIOThreadFlag = false;
    /**保存客户端线程Key，客户端线程*/
    Map<Integer,Thread> m_mapSocketThread = new HashMap<>(); //保存所有客户端线程 key 为客户端唯一的id
    Map<Integer,Integer> m_mapClientHeart = new HashMap<>(); //保存所有客户端心跳状态 key 为客户端唯一的id


    /**For heart Beat*/
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            switch(msg.what){
                case 1:
                    for(Map.Entry<Integer,Thread> entry : m_mapSocketThread.entrySet()){
                        if(entry.getValue().getId() == (long)msg.obj){
                            m_mapClientHeart.put(entry.getKey(),0);
                            Log.d("test","mapClientHeart心跳数："+String.valueOf(m_mapSocketThread.size()));
                            break;
                        }
                    }

                    break;
                default:
                    break;

            }
        }
    };

    /**心跳检测，通过handler的线程重复调用自己*/
    private Runnable heartBeatRunnable = new Runnable(){
        @Override
        public void run() {

            int nHeartBeatNum = 0;
            for(Map.Entry<Integer,Integer> entry : m_mapClientHeart.entrySet()){
                Log.v("test","检查所有的客户端，释放断线的客户资源"+entry.getValue()+"综述"+m_mapClientHeart.size());
                nHeartBeatNum = entry.getValue();
                nHeartBeatNum++;
                entry.setValue(nHeartBeatNum);

                if(nHeartBeatNum > HEART_BEAT_RATE){
                    m_mapSocketThread.get(entry.getKey()).interrupt();
                    m_mapClientHeart.remove(entry.getKey());
                    m_mapSocketThread.remove(entry.getKey());
                }
            }
            mHandler.postDelayed(this, 1000);
        }
    };


    private void initSocket(){
        try {

              mHandler.postDelayed(heartBeatRunnable, 1000);//初始化成功后，就准备发送心跳包
              m_serverSocket = new ServerSocket(SERVER_PORT);
              Log.v("test","intsocket,10086");

              while(mainThreadFlag){
                  Socket socket  = m_serverSocket.accept();
                  Log.d("test","socket to string " + socket.toString());
                  Thread rNewThread = new Thread(new ThreadReadWriterIOSocket(this,socket, mHandler));
                  //存储thread.id

                  for (int i = 0; i <= m_mapSocketThread.size();i++){
                      if(!m_mapSocketThread.containsKey(i)){
                          m_mapSocketThread.put(i,rNewThread);
                          m_mapClientHeart.put(i,0);
                          break;
                      }
                  }

                  rNewThread.start();
              }

        } catch (UnknownHostException e) {
            e.printStackTrace();
            Log.v("test","connserverice " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Log.v("test","connserverice " + e.getMessage());
        }
    }

    class InitSocketThread extends Thread {
        @Override
        public void run() {
            super.run();
            initSocket();
        }
    }

    public ConnService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mainThreadFlag = true;
        new InitSocketThread().start();
        Log.v("test","connservice--------------开机启动服务------------- onCreate");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.v("test","connservice startcommand");

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("test","on ConnService onDestroy()");
        for(Map.Entry<Integer,Thread> entry : m_mapSocketThread.entrySet()){
            entry.getValue().interrupt();
        }
        mainThreadFlag = false;
        mHandler.removeCallbacks(heartBeatRunnable);
        try{
            if(m_serverSocket != null)
                m_serverSocket.close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }











}
