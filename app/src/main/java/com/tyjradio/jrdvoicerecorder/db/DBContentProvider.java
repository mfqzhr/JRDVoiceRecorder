package com.tyjradio.jrdvoicerecorder.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.tyjradio.jrdvoicerecorder.utils.ContentData.PhoneRecorderTblData;

import static com.tyjradio.jrdvoicerecorder.utils.ContentData.PhoneRecorderTblData.PHONERECORDER;
import static com.tyjradio.jrdvoicerecorder.utils.ContentData.PhoneRecorderTblData.PHONERECORDER_P;
import static com.tyjradio.jrdvoicerecorder.utils.ContentData.PhoneRecorderTblData.SQLITE_SEQUENCE;
import static com.tyjradio.jrdvoicerecorder.utils.ContentData.PhoneRecorderTblData.uriMatcher;
import static com.tyjradio.jrdvoicerecorder.utils.ContentData.TABLE_NAME;


public class DBContentProvider extends ContentProvider {
    private DBHelper dbHelper;
    //匹配内容
    //private static final UriMatcher sUriMatcher = new UriMatcher();


    public DBContentProvider() {
    }

    @Override //返回MIME类型对应内容的URI
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        //throw new UnsupportedOperationException("Not yet implemented");
        switch (uriMatcher.match(uri)) {
            case PHONERECORDER: {
                return PhoneRecorderTblData.CONTENT_TYPE;
            }
            case PHONERECORDER_P: {
                return PhoneRecorderTblData.CONTENT_TYPE_ITME;
            }
        }
        return null;
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        dbHelper = new DBHelper(getContext());
        return true;
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.@by db组
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)) {
            case PHONERECORDER:
                return db.delete(TABLE_NAME, selection, selectionArgs);
            case PHONERECORDER_P:
                String id = uri.getPathSegments().get(1);
                return db.delete(TABLE_NAME, "id = ?", new String[]{id});


        }
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int update( Uri uri, ContentValues values, String selection,  String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)) {
            case PHONERECORDER:
                return db.update(TABLE_NAME,values,selection,selectionArgs);
            case PHONERECORDER_P:
                String id = uri.getPathSegments().get(1);
                return db.update(TABLE_NAME, values, "id = ?", new String[]{id});
            case SQLITE_SEQUENCE:
                db.execSQL("UPDATE sqlite_sequence set seq=0 where name='PhoneRecorderTbl'");
                return 1;


        }
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.@by db组
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id;

        switch (uriMatcher.match(uri)) {
            case PHONERECORDER:
                id = db.insert(TABLE_NAME, null, values);
                return ContentUris.withAppendedId(uri, id);
            case PHONERECORDER_P:
                id = db.insert(TABLE_NAME, "ID", values);
                String uriPath = uri.toString();
                String path = uriPath.substring(0, uriPath.lastIndexOf("/")) + id;
                return Uri.parse(path);
        }
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // TODO: Implement this to handle query requests from clients.@by db组
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = null;
        switch (uriMatcher.match(uri)) {
            case PHONERECORDER:
               cursor = db.query(TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
               break;
            case PHONERECORDER_P:
                long id = ContentUris.parseId(uri);
                String where = "ID=" + id;
                if ((selection != null) && (!"".equals(selection))) {
                    where = where + " and " + selection;
                }
                cursor = db.query(TABLE_NAME, projection, where, selectionArgs, null,
                        null, sortOrder);
                break;



        }
        return cursor;
    }
}


