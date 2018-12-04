package com.abilix.brain.data;

import com.abilix.brain.utils.LogMgr;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * 数据提供类，提供文件传输表的信息。
 */
public class BrainDatabaseProvider extends ContentProvider {

    public static final int TRANSFERRING_FILE = 0;

    public static final String AUTHORITY = "com.abilix.brain.provider";
    private static UriMatcher uriMatcher;

    private BrainDatabaseHelper mBrainDatabaseHelper;
    private SQLiteDatabase mSqLiteDatabase;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, BrainDatabaseHelper.TABLE_NAME_TRANSFERRING_FILE, TRANSFERRING_FILE);
    }

    @Override
    public boolean onCreate() {
        mBrainDatabaseHelper = new BrainDatabaseHelper(getContext());
        mSqLiteDatabase = mBrainDatabaseHelper.getWritableDatabase();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        LogMgr.i("BrainDatabaseProvider query()");
        //查询数据
        Cursor cursor = null;
        switch (uriMatcher.match(uri)) {
            case TRANSFERRING_FILE:
                cursor = mSqLiteDatabase.query(BrainDatabaseHelper.TABLE_NAME_TRANSFERRING_FILE, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                break;
        }

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        LogMgr.i("BrainDatabaseProvider getType()");
        switch (uriMatcher.match(uri)) {
            case TRANSFERRING_FILE:
                return "vnd.android.cursor.dir/vnd.com.abilix.brain.provider.transferring_file";
        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        LogMgr.i("BrainDatabaseProvider delete()");
        int deteledRows = 0;
        switch (uriMatcher.match(uri)) {
            case TRANSFERRING_FILE:
                deteledRows = mSqLiteDatabase.delete(BrainDatabaseHelper.TABLE_NAME_TRANSFERRING_FILE, selection, selectionArgs);
                break;
            default:
                break;
        }
        return deteledRows;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

}
