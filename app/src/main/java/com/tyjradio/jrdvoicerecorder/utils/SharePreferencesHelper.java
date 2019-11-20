/*
 * Copyright 2016 Freelander
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tyjradio.jrdvoicerecorder.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Tian on 2016/10/25.
 * sp的工具类（单例模式）
 */
public class SharePreferencesHelper {

    private static final String TAG = "SharePreferencesHelper";

    //sp文件的名字
    private static final String SP_NAME = "yang";
    //存储大文件继续插入的起始位置
    public static final String START_POSITION = "start_position";
    //记录大文件被重写的次数
    public static final String COUNT="count";
    //版本信息
    //每次录制的长度
    public static final String LENGTH = "length";



    public static final String VERSION_INFO = "version_info";



    private SharedPreferences mPreferences;

    private SharedPreferences.Editor mEditor;

    private static SharePreferencesHelper mSPHelper;

    private Context context;

    public static SharePreferencesHelper getInstance(Context context) {
        if (mSPHelper == null)
            synchronized (SharePreferencesHelper.class) {
                if (mSPHelper == null) {
                    mSPHelper = new SharePreferencesHelper(context);
                }
            }

        return mSPHelper;
    }

    private SharePreferencesHelper(Context context) {
        this.context = context;
        mPreferences = context.getSharedPreferences(SP_NAME, Context.MODE_APPEND);
    }


    public boolean putString(String key, String value) {
        mEditor = mPreferences.edit();
        mEditor.putString(key, value);
        return mEditor.commit();
    }

    public String getString(String key) {
        return mPreferences.getString(key, "");
    }

    public String getString(String key, String defValue) {
        return mPreferences.getString(key, defValue);
    }

    public boolean removeString(String key) {
        mEditor = mPreferences.edit();
        mEditor.remove(key);
        return mEditor.commit();
    }


    public boolean putInt(String key, int value) {
        mEditor = mPreferences.edit();
        mEditor.putInt(key, value);
        return mEditor.commit();
    }

    public int getInt(String key) {
        return mPreferences.getInt(key, 0);
    }

    public int getInt(String key, int defValue) {
        return mPreferences.getInt(key, defValue);
    }


    public boolean putLong(String key, long value) {
        mEditor = mPreferences.edit();
        mEditor.putLong(key, value);
        return mEditor.commit();
    }

    public long getLong(String key) {
        return mPreferences.getLong(key, 0);
    }

    public long getLong(String key, long defValue) {
        return mPreferences.getLong(key, defValue);
    }


    public boolean putBoolean(String key, boolean value) {
        mEditor = mPreferences.edit();
        mEditor.putBoolean(key, value);
        return mEditor.commit();
    }




    public boolean getBoolean(String key) {
        return mPreferences.getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defValue) {
        return mPreferences.getBoolean(key, defValue);
    }

    public void clearData() {
        mPreferences = context.getSharedPreferences(SharePreferencesHelper.SP_NAME, 0);
        mPreferences.edit().clear().commit();
    }



}
