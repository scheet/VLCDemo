package com.jwd.vlcplayer.server;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by scheet on 2017/12/8.
 */

public class DBManage {

    private static final String TAG = "DBManage";
    public Context context;
    private DBHelper mDBHelper;
    private SQLiteDatabase db;

    public DBManage(Context context) throws SQLException {
        if(null==mDBHelper){
            mDBHelper = new DBHelper(context);
        }

    }


    /**
     * 插入缓存，没有就插入，有就替换
     *
     * @param type 标签类型
     * @param data json数据
     */
    public synchronized void insertData(String type, String data) {
        Log.e(TAG,"insertData data = " + data);
        db = mDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.TYPE, type);
        values.put(DBHelper.DATA, data);
        values.put(DBHelper.TIME, System.currentTimeMillis());
        db.replace(DBHelper.CACHE, null, values);
        db.close();
    }


    /**
     * 根据标签获取到数据库储存的内容
     *
     * @param type 标签类型
     * @return 数据
     */
    public synchronized String getData(String type) {
        String result = "";
        db = mDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DBHelper.CACHE + " WHERE URL = ?", new String[]{type});
        while (cursor.moveToNext()) {
            result = cursor.getString(cursor.getColumnIndex(DBHelper.DATA));
        }
        cursor.close();
        db.close();
        return result;
    }

}
