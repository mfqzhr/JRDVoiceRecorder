package com.tyjradio.jrdvoicerecorder.ConUsb;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tyjradio.jrdvoicerecorder.bean.AudioFileBean;
import com.tyjradio.jrdvoicerecorder.bean.AudioRecorderItemBean;
import com.tyjradio.jrdvoicerecorder.bean.MessageBean;
import com.tyjradio.jrdvoicerecorder.bean.QueryInfoBean;
import com.tyjradio.jrdvoicerecorder.bean.TimeBean;
import com.tyjradio.jrdvoicerecorder.utils.AudioUtils;
import com.tyjradio.jrdvoicerecorder.utils.ContentData;
import com.tyjradio.jrdvoicerecorder.utils.FileUtils;
import com.tyjradio.jrdvoicerecorder.utils.LogUtils;
import com.tyjradio.jrdvoicerecorder.utils.PcmToWavUtil;
import com.tyjradio.jrdvoicerecorder.utils.RefreshUI;
import com.tyjradio.jrdvoicerecorder.utils.StatusRecorderUtils;
import com.tyjradio.jrdvoicerecorder.utils.TimeUtil;
import com.tyjradio.jrdvoicerecorder.utils.Utils;

import static com.tyjradio.jrdvoicerecorder.utils.GlobalConfig.AUDIO_FORMAT;
import static com.tyjradio.jrdvoicerecorder.utils.GlobalConfig.CHANNEL_CONFIG;
import static com.tyjradio.jrdvoicerecorder.utils.GlobalConfig.SAMPLE_RATE_INHZ;
import static java.lang.Integer.getInteger;


public class ThreadReadWriterIOSocket implements Runnable {
    private Socket m_clientSocket;
    private Context m_context;
    private Handler m_Handler;
    private long m_idThread;



    public ThreadReadWriterIOSocket(Context context, Socket client, Handler mHandler){
        this.m_clientSocket = client;
        this.m_context = context;
        this.m_Handler = mHandler;

    }

    @Override
    public void run() {
        Log.v("test", "rwiosocket run");
        BufferedOutputStream out;
        BufferedInputStream in;
        String strMsg;
        byte[] outByte;

        try{
            /*PC端发来的数据msg*/
            out = new BufferedOutputStream(m_clientSocket.getOutputStream());
            in = new BufferedInputStream(m_clientSocket.getInputStream());
            //ConnService.m_bIOThreadFlag
            while(!Thread.currentThread().isInterrupted()){
                if(!m_clientSocket.isConnected())
                {
                    break;
                }
                Log.v("test", Thread.currentThread().getName() + "--->"+"will read...");
               // Thread.sleep(20);

                /* 读操作命令 */
                strMsg = readCMDFromSocket(in);
                Log.v("test","this is message" + strMsg);

                if(strMsg==null)
                {
                    Log.v("test","message is null");
                }
                else{

                    JSONObject jsonObjIn = JSON.parseObject(strMsg);
                    int nMsgType = jsonObjIn.getInteger("MessageType");

                    //如果是心跳包，则向handlemessage 发送Message，更新心跳计数，并给client端回复一个心跳包
                    switch (nMsgType){
                        case ContentData.MESSAGETYPE_HEARTBEAT: //心跳包
                            //更新 心跳计数
                            Message message = m_Handler.obtainMessage(1,Thread.currentThread().getId());
                            m_Handler.sendMessage(message);
                            //String strHeartBeatContent = "<HEAD><MCODE>0X01</MCODE><DATA>heart</DATA></HEAD>";

                            MessageBean msgBeanRes = new MessageBean();

                            msgBeanRes.setMessageType(ContentData.MESSAGETYPE_HEARTBEAT);
                            msgBeanRes.setDirection(1);

                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date date = new Date(System.currentTimeMillis());
                            TimeBean timeBean = new TimeBean();
                            timeBean.setCurrentTime(simpleDateFormat.format(date));
                            msgBeanRes.setMessageBody(timeBean);
                            String strHeartBeatContent = JSON.toJSONString(msgBeanRes);
                            String strHeartResponse = String.format("%09d",strHeartBeatContent.length()) + strHeartBeatContent;
                            byte[] bHeartResponse = strHeartResponse.getBytes("UTF-8");
                            out.write(bHeartResponse,0,bHeartResponse.length);
                            out.flush();
                            break;
                        case ContentData.MESSAGETYPE_QUERY:
                            Message message2 = m_Handler.obtainMessage(1,Thread.currentThread().getId());
                            m_Handler.sendMessage(message2);

                            String strMsgValue = jsonObjIn.getString("MessageBody");
                            //从json中，解析出查询参数
                            JSONObject jsonObjectSub = JSON.parseObject(strMsgValue);

                            String strSelection = null;
                            if(jsonObjectSub.containsKey("Condition")){
                                strSelection = jsonObjectSub.getString("Condition");
                            }

//                            if(jsonObjectSub.getBooleanValue("EnableStartTime") == true && jsonObjectSub.getBooleanValue("EnableEndTime") == true)
//                            {
//                                strSelection = "CallingTime >= '" + jsonObjectSub.getString("StartTime") + "' and CallingTime <= '"+ jsonObjectSub.getString("EndTime") +"'";
//                            }
//
//                            if(jsonObjectSub.getBooleanValue("EnableStartTime") == true && !(jsonObjectSub.getBooleanValue("EnableEndTime") == true))
//                            {
//                                strSelection = "CallingTime >= '" + jsonObjectSub.getString("StartTime") +"'";
//                            }
//                            if(!(jsonObjectSub.getBooleanValue("EnableStartTime") == true) && jsonObjectSub.getBooleanValue("EnableEndTime") == true)
//                            {
//                                strSelection = "CallingTime <= '" + jsonObjectSub.getString("EndTime") +"'";
//                            }

                            //查询contentprovider
                            Uri uri = Uri.parse("content://com.tyjradio.provider/phonerecorder");
                            //定义list对象

                            String tag = "'";
                            Cursor cursor = m_context.getContentResolver().query(uri,new String[]{"*"}, strSelection, null,null,null);

                            if(cursor != null)
                            {
                                if(! ContentData.arrayList.isEmpty())
                                {
                                    ContentData.arrayList.clear();
                                }
                                while(cursor.moveToNext()){
                                    Log.d("test","list----->"+ String.valueOf(cursor.getCount()));
                                    AudioRecorderItemBean itemBean = new AudioRecorderItemBean();

                                    itemBean.setId(cursor.getInt(cursor.getColumnIndex("ID")));
                                    Log.d("test","id"+ itemBean.getId());
                                    //itemBean.setGroupID(cursor.getInt(cursor.getColumnIndex("GroupID")));
                                    itemBean.setZipCode(cursor.getString(cursor.getColumnIndex("ZipCode")));
                                    itemBean.setSelfPhoneNum(cursor.getString(cursor.getColumnIndex("SelfPhoneNum")));
                                    itemBean.setCalledPhoneNum(cursor.getString(cursor.getColumnIndex("CalledPhoneNum")));
                                    itemBean.setCallingTime(cursor.getString(cursor.getColumnIndex("CallingTime")));
                                    itemBean.setDurationTime(cursor.getInt(cursor.getColumnIndex("DurationTime")));
                                    itemBean.setChannelCode(cursor.getInt(cursor.getColumnIndex("ChannelCode")));
                                    itemBean.setChannelType(cursor.getString(cursor.getColumnIndex("ChannelType")));
                                    itemBean.setRecorderType(cursor.getString(cursor.getColumnIndex("RecorderType")));
                                    itemBean.setREFrequency(cursor.getDouble(cursor.getColumnIndex("REFrequency")));
                                    itemBean.setEmFrequency(cursor.getDouble(cursor.getColumnIndex("EmFrequency")));
                                    itemBean.setOutOrInput(cursor.getString(cursor.getColumnIndex("OutOrInput")));
                                    itemBean.setMachineSerialNum(new String(cursor.getBlob(cursor.getColumnIndex("MachineSerialNum"))));
                                    //itemBean.setMachineSerialNum(Utils.unicodeToString(cursor.getBlob(cursor.getColumnIndex("MachineSerialNum"))));
                                    //itemBean.setRecorderFilePath(cursor.getString(cursor.getColumnIndex("RecorderFilePath")));
                                    itemBean.setAudioFile(cursor.getString(cursor.getColumnIndex("AudioFile")));
                                    itemBean.setStartPosition(cursor.getLong(cursor.getColumnIndex("StartPosition")));
                                    itemBean.setLength(cursor.getInt(cursor.getColumnIndex("Length")));
                                    itemBean.setCount(cursor.getInt(cursor.getColumnIndex("Count")));
                                    ContentData.arrayList.add(itemBean);

                            }
                                cursor.close();

                            }
                            MessageBean msgQueryR = new MessageBean();
                            msgQueryR.setMessageType(ContentData.MESSAGETYPE_QUERY);
                            msgQueryR.setDirection(1);

                            QueryInfoBean qb = new QueryInfoBean();
                            qb.setSize(ContentData.arrayList.size());
                            Log.v("test","size is --------------->"+String.valueOf(ContentData.arrayList.size()));
                            qb.setCurrentP(0);

                            msgQueryR.setMessageBody(qb);

                            String strQueryinfo = JSON.toJSONString(msgQueryR);
                            String strQueryinfo2 = String.format("%09d",strQueryinfo.length())+strQueryinfo;
                            byte[] bQueryinfo2 = strQueryinfo2.getBytes("UTF-8");
                            out.write(bQueryinfo2,0,bQueryinfo2.length);
                            out.flush();

                            break;
                        case ContentData.MESSAGETYPE_DOWNLOAD:

                            Message message3 = m_Handler.obtainMessage(1,Thread.currentThread().getId());
                            m_Handler.sendMessage(message3);

                            String strDownMsgBody = jsonObjIn.getString("MessageBody");
                            //从json中，解析出查询参数
                            JSONObject jsonDownObj = JSON.parseObject(strDownMsgBody);
                            //查询contentprovider
//                            Uri uri3 = Uri.parse("content://com.tyjradio.provider/"+jsonDownObj.getInteger("ID").toString());
//                            Cursor cursor3 = m_context.getContentResolver().query(uri3,new String[]{"RecorderFilePath"},null,null,null,null);
//                            String fileName = null;
//
//                            if(cursor3 != null) {
//                                while (cursor3.moveToNext()) {
//                                    fileName = cursor3.getString(cursor3.getColumnIndex("RecorderFilePath"));
//                                }
//                                cursor3.close();
//                            }
                            String fileNameD = jsonDownObj.getString("AudioFile");
                            long fileLength = jsonDownObj.getLong("Length");
                            int start3Position = jsonDownObj.getIntValue("StartPosition");
                            String fileWAV3Path = FileUtils.getWAVFileAbsolutePath(fileNameD);
                            if (!FileUtils.isFileCreate(fileWAV3Path)) {
                                //从数据库查询数据

                                //先从大文件中读取pcm文件
                                FileUtils.getFile(start3Position,fileLength, fileNameD);
                                PcmToWavUtil pcmToWavUtil = new PcmToWavUtil(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT);
                                //将录音附加信息写入wav中
                                pcmToWavUtil.pcmToWav( FileUtils.getPcmFileAbsolutePath(fileNameD), fileWAV3Path, AudioUtils.beanToBytes(m_context, fileNameD));
                                //转换完成，将pcm文件删除
                                File file = new File(FileUtils.getPcmFileAbsolutePath(fileNameD));
                                file.delete();
                            }
                            try{
                                File audioFile = new File(fileWAV3Path);
                                FileInputStream fis = new FileInputStream(audioFile);
                                int fileLen = fis.available(); // file.length
                                fis.close();

                                MessageBean audioBean = new MessageBean();
                                audioBean.setMessageType(3);
                                audioBean.setDirection(1);
                                AudioFileBean fileBean = new AudioFileBean();
                                fileBean.setSum(fileLen);
                                fileBean.setCurrentPoint(0);
                                fileBean.setPath(fileWAV3Path);
                                audioBean.setMessageBody(fileBean);

                                String strAudioFileInfo = JSON.toJSONString(audioBean);
                                String strAudioInfoResponse = String.format("%09d",strAudioFileInfo.length()) + strAudioFileInfo;
                                byte[] bAudioInfoResponse = strAudioInfoResponse.getBytes("UTF-8");
                                out.write(bAudioInfoResponse,0,bAudioInfoResponse.length);
                                out.flush();
                                break;
                            }
                            catch (IOException e){
                                e.printStackTrace();
                                break;
                            }


                        case ContentData.MESSAGETYPE_QUERYDOWN:
                            Message message4 = m_Handler.obtainMessage(1,Thread.currentThread().getId());
                            m_Handler.sendMessage(message4);

                            String strQueryDownBody = jsonObjIn.getString("MessageBody");
                            //从json中，解析出查询参数
                            JSONObject jsnObjectQueryDown = JSON.parseObject(strQueryDownBody);

                            int size = jsnObjectQueryDown.getIntValue("Size");
                            int currentPoint = jsnObjectQueryDown.getIntValue("CurrentPoint");

                            MessageBean mbQueryDown = new MessageBean();

                            mbQueryDown.setMessageType(ContentData.MESSAGETYPE_QUERYDOWN);
                            mbQueryDown.setDirection(1);
                            QueryInfoBean infoBean = new QueryInfoBean();
                            infoBean.setCurrentP(currentPoint);
                            infoBean.setSize(size);
                            if(currentPoint < ContentData.arrayList.size()){
                                infoBean.setAudioRecorderBean( ContentData.arrayList.get(currentPoint));
                            }

                            mbQueryDown.setMessageBody(infoBean);
                            Log.v("test","size is --------------->"+String.valueOf(size));
                            Log.v("test","point is --------------->"+String.valueOf(currentPoint));
                            String strQueryDown = JSON.toJSONString(mbQueryDown);
                            String strQueryDownL = String.format("%09d",strQueryDown.length())+strQueryDown;
                            byte[] bQueryDown = strQueryDownL.getBytes("UTF-8");
                            out.write(bQueryDown,0,bQueryDown.length);
                            out.flush();

                            break;
                        case ContentData.MESSAGETYPE_DATADOWN:
                            Message message5 = m_Handler.obtainMessage(1,Thread.currentThread().getId());
                            m_Handler.sendMessage(message5);

                            String strAudioDownBody = jsonObjIn.getString("MessageBody");
                            Log.v("test1","MessageBody:"+strAudioDownBody);
                            //从json中，解析出查询参数
                            JSONObject jsnObjectAudioDown = JSON.parseObject(strAudioDownBody);
                            int sum = jsnObjectAudioDown.getIntValue("Sum");
                            int currentP = jsnObjectAudioDown.getIntValue("CurrentPoint");
                            String path =jsnObjectAudioDown.getString("Path");
                            Log.v("test1","path:"+path);

                            try{
                                File audioFile = new File(path);

                                Log.v("test1","file:"+audioFile);

                                FileInputStream fis = new FileInputStream(audioFile);
                                Log.v("test1","file size " + String.valueOf(fis.available()));

                                byte[] audioData = new byte[1024];
                                int offset = currentP;
                                while(offset > 0)
                                {
                                    long amt = fis.skip(offset);
                                    if(amt == -1){
                                        throw new RuntimeException(audioFile + ": unexpected EOF");
                                    }
                                    offset -= amt;
                                }
                                int readLen = fis.read(audioData);
                                //fis.close();
                                if(readLen != -1){
                                    MessageBean audioDataMsgBean = new MessageBean();
                                    audioDataMsgBean.setMessageType(5);
                                    audioDataMsgBean.setDirection(1);
                                    AudioFileBean audioFileBean = new AudioFileBean();
                                    audioFileBean.setSum(sum);
                                    audioFileBean.setPath(path);
                                    byte[] audioReadedData = new byte[readLen];
                                    System.arraycopy(audioData,0,audioReadedData,0,readLen);
                                    String strReaderData = Base64.encodeToString(audioReadedData,Base64.DEFAULT);
                                    audioFileBean.setCurrentPoint(currentP+readLen);
                                    audioFileBean.setData(strReaderData);
                                    audioDataMsgBean.setMessageBody(audioFileBean);

                                    String strAudioData = JSON.toJSONString(audioDataMsgBean);
                                    String strAudioDataL = String.format("%09d",strAudioData.length())+strAudioData;
                                    Log.v("test1","out str:"+strAudioDataL);
                                    byte[] bAudioData = strAudioDataL.getBytes("UTF-8");
                                    out.write(bAudioData,0,bAudioData.length);
                                    out.flush();
                                }
                                else{
                                    fis.close();
                                    audioFile.delete();

                                }

                                break;

                            }catch (IOException e){
                                e.printStackTrace();
                                break;
                            }
                        case ContentData.MESSAGETYPE_QUERYLOG:
                        {
                            Message message6 = m_Handler.obtainMessage(1,Thread.currentThread().getId());
                            m_Handler.sendMessage(message6);

                            String strLogQueryBody = jsonObjIn.getString("MessageBody");
                            JSONObject logInfoObj = JSON.parseObject(strLogQueryBody);
                            String logPath = logInfoObj.getString("Path");

                            File logFile = StatusRecorderUtils.getFile();
                            RandomAccessFile accessFile = new RandomAccessFile(logFile,"rw");
//                            accessFile.seek(0);
//                            while (accessFile.getFilePointer() != accessFile.length()){
//                                //String strCurr = new String(accessFile.readLine().getBytes("ISO-8859-1"),"GBK");
//                                String strCurr =accessFile.readLine();
//                                String newString = new String(Utils.getBytes(strCurr.toCharArray()));
//                                Log.v("test3",newString);
//                                String[] strlist = newString.split(":");
//                                if(strlist[0].equals("读取时间")){
//                                    accessFile.seek(accessFile.getFilePointer()-21);
//                                    accessFile.write((TimeUtil.getNowTime(System.currentTimeMillis()) + "\r\n").getBytes());
//                                    break;
//                                }
//                            }


                            long logFileLen = accessFile.length(); // file.length
                            accessFile.close();

                            MessageBean logInfoBean = new MessageBean();
                            logInfoBean.setMessageType(6);
                            logInfoBean.setDirection(1);
                            AudioFileBean logFileBean = new AudioFileBean();
                            logFileBean.setSum(logFileLen);
                            logFileBean.setCurrentPoint(0);
                            logFileBean.setPath(logPath);
                            logInfoBean.setMessageBody(logFileBean);

                            String strLogInfo = JSON.toJSONString(logInfoBean);
                            String strLogInfoL = String.format("%09d",strLogInfo.length())+strLogInfo;
                            Log.v("test1","out str:"+strLogInfoL);
                            byte[] bLogFileData = strLogInfoL.getBytes("UTF-8");
                            out.write(bLogFileData,0,bLogFileData.length);
                            out.flush();
                        }
                        break;
                        case ContentData.MESSAGETYPE_LOGDOWN:
                        {
                            Message message8 = m_Handler.obtainMessage(1,Thread.currentThread().getId());
                            m_Handler.sendMessage(message8);
                            String strLogDown= jsonObjIn.getString("MessageBody");
                            //从json中，解析出查询参数
                            JSONObject strLogDownBody = JSON.parseObject(strLogDown);
                            //int sumLog = strLogDownBody.getIntValue("Sum");
                            int currentPLog = strLogDownBody.getIntValue("CurrentPoint");
                            String pathLog =strLogDownBody.getString("Path");
                            try {
                                File logFile = StatusRecorderUtils.getFile();

                                Log.v("test1", "file:" + logFile);

                                FileInputStream fisLog = new FileInputStream(logFile);
                                int sumLog = fisLog.available();
                                Log.v("test1", "file size " + String.valueOf(fisLog.available()));

                                byte[] logFileData = new byte[1024];
                                int offset = currentPLog;
                                while(offset > 0)
                                {
                                    long amt = fisLog.skip(offset);
                                    if(amt == -1){
                                        throw new RuntimeException(logFile + ": unexpected EOF");
                                    }
                                    offset -= amt;
                                }
                                int readLen = fisLog.read(logFileData);
                                Log.v("test","shit3 shit3 " + logFileData.length);
                                //fis.close();
                                if(readLen != -1){
                                    MessageBean logDataMsgBean = new MessageBean();
                                    logDataMsgBean.setMessageType(8);
                                    logDataMsgBean.setDirection(1);
                                    AudioFileBean logFileBean = new AudioFileBean();
                                    logFileBean.setSum(sumLog);
                                    logFileBean.setPath(pathLog);
                                    byte[] logReadedData = new byte[readLen];
                                    System.arraycopy(logFileData,0,logReadedData,0,readLen);
                                    Log.v("test","shit shit " + logReadedData.length);
                                    String strReaderData = Base64.encodeToString(logReadedData,Base64.DEFAULT);
                                    //String strReaderData = logReadedData.toString();
                                    logFileBean.setCurrentPoint(currentPLog+readLen);
                                    logFileBean.setData(strReaderData);
                                    logDataMsgBean.setMessageBody(logFileBean);

                                    String strAudioData = JSON.toJSONString(logDataMsgBean);
                                    String strAudioDataL = String.format("%09d",strAudioData.length())+strAudioData;
                                    Log.v("test1","out str:"+strAudioDataL);
                                    byte[] bAudioData = strAudioDataL.getBytes("UTF-8");
                                    out.write(bAudioData,0,bAudioData.length);
                                    out.flush();
                                }

                                break;
                            }catch (IOException e){
                                e.printStackTrace();
                                break;
                            }

                        }
                        case ContentData.MESSAGETYPE_COMMAND:

                            //数据库，count 置零
                            AudioUtils.deleteAllRecorders(m_context);

                            RefreshUI.getInstance().recorderCompletion();



                        default:
                                break;

                    }

                }

            }

            out.close();
            in.close();

        }catch (Exception e)
        {
            e.printStackTrace();
//            MessageBean errorInfoBean = new MessageBean();
//            errorInfoBean.setMessageType(6);
//            errorInfoBean.setDirection(1);
//            AudioFileBean logFileBean = new AudioFileBean();
//            logFileBean.setSum(logFileLen);
//            logFileBean.setCurrentPoint(0);
//            logFileBean.setPath(logPath);
//            logInfoBean.setMessageBody(logFileBean);
//
//            String strLogInfo = JSON.toJSONString(logInfoBean);
//            String strLogInfoL = String.format("%09d",strLogInfo.length())+strLogInfo;
//            Log.v("test1","out str:"+strLogInfoL);
//            byte[] bLogFileData = strLogInfoL.getBytes("UTF-8");
//            out.write(bLogFileData,0,bLogFileData.length);
//            out.flush();
        }finally {
            try{
                if(m_clientSocket != null)
                {
                    Log.v("test", Thread.currentThread().getName() + "---> clientsocket close");
                    m_clientSocket.close();
                }

            }catch (IOException e){
                Log.v("test",Thread.currentThread().getName()+"--->" + "read write error333333");
                e.printStackTrace();
            }

        }

    }

    /**
     * 功能：从socket流中读取完整文件数据
     *
     * InputStream in：socket输入流
     *
     * byte[] filelength: 流的前4个字节存储要转送的文件的字节数
     *
     * byte[] fileformat：流的前5-8字节存储要转送的文件的格式（如.apk）
     *
     * */
    public static byte[] receiveFileFromSocket(InputStream in, OutputStream out, byte[] filelength, byte[] fileformat)
    {
        byte[] filebytes = null;// 文件数据
        try
        {
            in.read(filelength);// 读文件长度
            int filelen = Utils.bytesToInt(filelength);// 文件长度从4字节byte[]转成Int
            String strtmp = "read file length ok:" + filelen;
            out.write(strtmp.getBytes("utf-8"));
            out.flush();

            filebytes = new byte[filelen];
            int pos = 0;
            int rcvLen = 0;
            while ((rcvLen = in.read(filebytes, pos, filelen - pos)) > 0)
            {
                pos += rcvLen;
            }
            Log.v("test", Thread.currentThread().getName() + "---->" + "read file OK:file size="
                    + filebytes.length);
            out.write("read file ok".getBytes("utf-8"));
            out.flush();
        } catch (Exception e)
        {
            Log.v("test",Thread.currentThread().getName() + "---->" + "receiveFileFromSocket error");
            e.printStackTrace();
        }
        return filebytes;
    }

    /* 读取命令 */
    public String readCMDFromSocket(InputStream in)
    {
        int MAX_BUFFER_BYTES = 1024;
        try{
            byte[] msgBuffer = new byte[MAX_BUFFER_BYTES];

            int numReadedBytes = in.read(msgBuffer,0,msgBuffer.length);

            String msg = new String(msgBuffer, 0, numReadedBytes, "utf-8"); //byte[],offset,length,charset;
            Log.v("test","msg with length---->"+msg);
           // System.arraycopy(logFileData,0,logReadedData,0,readLen);

            if(msg.length()>8){
                String strMsgLength = msg.substring(0,8);
                int msgLength = Integer.parseInt(strMsgLength);

                byte[] by_msg = Utils.subByte(msgBuffer,8,msgLength);
                String strMsg = new String(by_msg,"utf-8");
                Log.v("test","message-->"+strMsg);
                return  strMsg;
            }

            return null;



        }
        catch (IOException e){
            Log.v("test","readCMDFromSocket error!");
            e.printStackTrace();
            return null;
        }

    }

    /** 输出函数 */
    private void outFromSocket(OutputStream out,byte[] outBytes){

        try{

            if(outBytes.length>8192) {


            }

            else{
                out.write(outBytes,0,outBytes.length);
                out.flush();
            }

        }catch (IOException e){
            e.printStackTrace();
        }


    }









}
