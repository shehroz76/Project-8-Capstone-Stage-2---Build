package com.pradeep.myreddit.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;


public class MyRedditProvider extends ContentProvider {

    DbHelper helper;
    private SQLiteDatabase sqlDB;
    public static String AUTHORITY = "com.pradeep.myreddit.myprovider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    @Override
    public boolean onCreate() {
        helper = new DbHelper(getContext());
        sqlDB = helper.getWritableDatabase();

        if (sqlDB == null)
            return false;
        else
            return true;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Cursor cursor = sqlDB.query(DbHelper.TABLE_NAME, null, null, null, null, null, null);
        Log.d("No. of rows ret", Integer.toString(cursor.getCount()));
        return cursor;

    }


    @Override
    public String getType(Uri uri) {
        return null;
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long row = sqlDB.insert("my_subs", null, values);
        return uri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        sqlDB.execSQL("delete from " + DbHelper.TABLE_NAME);
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
