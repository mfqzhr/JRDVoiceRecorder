/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 ZhongChuangHuaYing.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.tyjradio.jrdvoicerecorder.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author tianyingsu
 *	关于播放记录的数据库
 */
public class SearchHistoryDBHelper extends SQLiteOpenHelper{

	private static String DATABASE_NAME="user.db";
	private static int DATABASE_VERSION=1;

	/**
	 * @param context
	 */
	public SearchHistoryDBHelper(Context context) {
		//CursorFactory设置为null,使用默认值  
        super(context, DATABASE_NAME, null, DATABASE_VERSION); 
	}
	
	//数据库第一次创建时oncreate会被调用
	@Override
	public void onCreate(SQLiteDatabase db) {
		//创建表的语句(创建的表的名字为searchHistory)
		db.execSQL("CREATE TABLE IF NOT EXISTS searchHistory" +  
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT,name VARCHAR)"); 
	}

	//数据库升级时此方法会被调用
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}
