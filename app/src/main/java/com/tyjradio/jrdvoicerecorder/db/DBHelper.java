package com.tyjradio.jrdvoicerecorder.db;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;

import com.tyjradio.jrdvoicerecorder.utils.ContentData;


public class DBHelper extends SQLiteOpenHelper {
    /*
    定义建表语句 组号 区号 本机ID 远端ID 时间 持续时间 信道号 录音类型
    接收频率 发射频率 发送/接收 信道类型 机器序列号 录音文件路径
    录音文件名 录音开始时间
    */
    public static final String CREATE_PHONERECORDERTBL
            = "create table PhoneRecorderTbl ( "
            + "ID integer primary key autoincrement, "
            + "ZipCode varchar(20), "
            + "SelfPhoneNum varchar(30), "
            + "CalledPhoneNum varchar(30), "
            + "CallingTime  datetime, "
            + "DurationTime  integer, "
            + "ChannelCode  numeric, "
            + "ChannelType  varchar(10), "
            + "RecorderType  varchar(10), "
            + "REFrequency  double, "
            + "EmFrequency double, "
            + "OutOrInput  varchar(50), "
            + "MachineSerialNum  blob, "
            + "AudioFile  varchar(50), "
            + "StartPosition INTEGER, "
            + "Length INTEGER, "
            + "Count INTEGER)";

    //parameters：参数1:上下文对象，参数2:数据库的名称，参数3:创建Cursor的工厂类,参数4:数据库的版本
    public DBHelper(Context context){
        super(context, ContentData.DATABASE_NAME,null,ContentData.DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_PHONERECORDERTBL);


    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        String sqlstr = "DROP TABLE IF EXISTS " + ContentData.TABLE_NAME;
        sqLiteDatabase.execSQL(sqlstr);
        onCreate(sqLiteDatabase);

    }


}
