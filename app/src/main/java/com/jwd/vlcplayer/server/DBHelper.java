package com.jwd.vlcplayer.server;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by scheet on 2017/12/8.
 */

public class DBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "serverbase.db";// 数据库名称
    public static final int VERSION = 1;
    private Context context;
    public static final String TABLE_CHANNEL = "json_channel";//数据表

    public static final String CACHE = "cache";
    public static final String ID = "_id";
    public static final String TYPE = "url";
    public static final String DATA = "data";
    public static final String TIME = "time";




    public DBHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
        this.context = context;
    }

    public Context getContext(){
        return context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // TODO 创建数据库后，对数据库的操作
        String sql = "CREATE TABLE IF NOT EXISTS "
                + CACHE + " ("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TYPE + " TEXT, "
                + TIME + " TEXT, "
                + DATA + " TEXT)";
        sqLiteDatabase.execSQL(sql);

    }
    //删除数据库
    public void deleteDb(){
        context.deleteDatabase(DB_NAME);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // TODO 更改数据库版本的操作
        onCreate(sqLiteDatabase);
    }
}
