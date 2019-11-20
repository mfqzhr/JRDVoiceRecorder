/**
 * -----------------------------------------------------------------------
 * Copyright (C) 2015 ZhongChuangHuaYing.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.tyjradio.jrdvoicerecorder.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * @author tianyingsu
 * 用于管理播放记录的数据库，执行一些操作
 */
public class SearchHistoryDBManager {
    private SearchHistoryDBHelper helper;
    private SQLiteDatabase database;
    private static final String TABLE_NAME = "searchHistory";

    //构造方法
    public SearchHistoryDBManager(Context context) {
        helper = new SearchHistoryDBHelper(context);
        database = helper.getWritableDatabase();
    }

    //查询表中所有的数据
    public ArrayList<String> getAllSearchHistory() {
        ArrayList<String> historyData = new ArrayList<String>();
        Cursor cursor = database.query(TABLE_NAME, new String[]{"name"}, null, null, null, null, null);
        while (cursor.moveToNext()) {
            historyData.add(cursor.getString(cursor.getColumnIndex("name")));
        }
        //关闭cursor对象
        cursor.close();
        return historyData;
    }

    //向表中插入数据
    public void insertSearchHistoryData(String content) {
        //插入的方法是先从数据库中查询到所有数据，然后只取前三条，再加上新增的数据一同存入数据库
        ArrayList<String> oldData = new ArrayList<String>();
        ArrayList<String> newData = new ArrayList<String>();
        oldData = getAllSearchHistory();
        for (int i = 0; i < oldData.size(); i++) {
            if (oldData.get(i).equals(content)) {
                oldData.remove(i);
            }
        }
        newData.add(content);
        newData.addAll(oldData);
        insertData(newData);
    }

    /**
     * 根据搜索内容删除表中的某一条数据
     *
     * @param searchText
     */
    public void deleteOneSearch(String searchText) {
        database.delete(TABLE_NAME, "name=?", new String[]{searchText});

    }

    //向数据库中插入新的数据
    public void insertData(ArrayList<String> newData) {
        //首先将数据库中的数据清空
        deletAll();
        for (int i = 0; i < newData.size(); i++) {
            ContentValues values = new ContentValues();
            values.put("_id", i);
            values.put("name", newData.get(i));
            database.insert(TABLE_NAME, null, values);
            /*database.execSQL("insert into "+TABLE_NAME+"(_id,name) values("+i+","+newData.get(i)+")");*/
        }
    }

    //用于清空所有额数据
    public void deletAll() {
        String sql = "DELETE FROM " + TABLE_NAME + ";";
        database.execSQL(sql);
    }

}
