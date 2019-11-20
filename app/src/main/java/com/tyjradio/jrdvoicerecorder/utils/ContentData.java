package com.tyjradio.jrdvoicerecorder.utils;

import android.content.UriMatcher;
import android.net.Uri;
import android.provider.BaseColumns;

import com.tyjradio.jrdvoicerecorder.bean.AudioRecorderItemBean;

import java.util.ArrayList;

public final class ContentData {
    public static final int MESSAGETYPE_HEARTBEAT = 1;
    public static final int MESSAGETYPE_QUERY = 2;
    public static final int MESSAGETYPE_DOWNLOAD = 3;
    public static final int MESSAGETYPE_QUERYDOWN = 4;
    public static final int MESSAGETYPE_DATADOWN = 5;
    public static final int MESSAGETYPE_QUERYLOG = 6;
    public static final int MESSAGETYPE_LOGDOWN = 8;
    public static final int MESSAGETYPE_COMMAND = 9;
    public static final String AUTHORITY = "com.tyjradio.provider";
    //public static byte[] audioFile;
    /**保存信息*/
    public static ArrayList<AudioRecorderItemBean> arrayList = new ArrayList<>();


    //数据库名称常量
    public static final String DATABASE_NAME = "VoiceData.db";
    //数据库版本常量创建 数据库的时候，都必须加上版本信息；并且必须大于4
    public static final int DATABASE_VERSION = 4;
    //表名称常量
    public static final String TABLE_NAME = "PhoneRecorderTbl";

    public static final class PhoneRecorderTblData implements BaseColumns {
        //Uri，外部程序需要访问就是通过这个Uri访问的，这个Uri必须的唯一的。
        public static final Uri CONTENT_URI = Uri.parse("content://"+ AUTHORITY + "/phonerecorder");
        // 数据集的MIME类型字符串则应该以vnd.android.cursor.dir/开头
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/com.tyjradio.phonerecorder";
        // 单一数据的MIME类型字符串应该以vnd.android.cursor.item/开头
        public static final String CONTENT_TYPE_ITME = "vnd.android.cursor.item/com.tyjradio.phonerecorderbypara";
        /* 自定义匹配码 */
        public static final int PHONERECORDER = 1;
        /* 自定义匹配码 */
        public static final int PHONERECORDER_P = 2;
        public static final int SQLITE_SEQUENCE = 3;

        /*请完成phonerecordertbl中字段的常量定义，查看设计文档中数据设计，phonerecordertbl @by db组
        public static final String ZIPCODE = "title";
        public static final String SELFPHONENUM = "name";
        public static final String CALLEDPHONENUM = "date_added";
        public static final String TIME = "SEX";
        public static final String DURATION = "_id desc";


        public static final String ZIPCODE = "title";

         */

        public static final UriMatcher uriMatcher;


        static {
            // 常量UriMatcher.NO_MATCH表示不匹配任何路径的返回码
            uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
            // 如果match()方法匹配content://com.tyjradio.provider/phonerecorder路径,PHONERECORDER
            uriMatcher.addURI(ContentData.AUTHORITY, "phonerecorder", PHONERECORDER);
            // 如果match()方法匹配content://com.tyjradio.provider/phonerecorder/230,路径，PHONERECORDER_P
            uriMatcher.addURI(ContentData.AUTHORITY, "phonerecorder/#", PHONERECORDER_P);
            uriMatcher.addURI(ContentData.AUTHORITY, "sqlite_sequence", SQLITE_SEQUENCE);
        }
    }



//    public static void setAudioFile(int size){
//        audioFile = new byte[size];
//
//    }
//    public static byte[] getAudioFile(){
//        return audioFile;
//    }
//
//    public  static void clearAudioFile(){
//        if(audioFile.length !=0){
//            java.util.Arrays.fill(audioFile, (byte) 0);
//            audioFile = null;
//        }
//
//    }
}
