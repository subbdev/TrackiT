package com.subbu.trackit.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseHelper  extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    public static final String DATABASE_NAME = "TransFlo";
    private static final int DATABASE_VERSION = 1;
    Context mContext;


    private static final String DATABASE_CREATE_TABLE_TRUCK_STOP = "create table if not exists "
            + DatabaseAdapter.DATABASE_TABLE_TRUCK_STOP
            + " ("
            + DatabaseAdapter.COLUMN_NAME+" text , "
            + DatabaseAdapter.COLUMN_CITY + " text , "
            + DatabaseAdapter.COLUMN_STATE + " text , "
            + DatabaseAdapter.COLUMN_COUNTRY + " text , "
            + DatabaseAdapter.COLUMN_ZIP + " text , "
            + DatabaseAdapter.COLUMN_LATITUDE + " REAL , "
            + DatabaseAdapter.COLUMN_LONGITUDE + " REAL , "
            + DatabaseAdapter.COLUMN_RAW_LINE1 + " text , "
            + DatabaseAdapter.COLUMN_RAW_LINE2 + " text , "
            + DatabaseAdapter.COLUMN_RAW_LINE3 + " text);";




    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_TABLE_TRUCK_STOP);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
