package com.example.pstype_v1.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.pstype_v1.data.Contract.track;

/**
 * Created by Derelanin on 06.09.2017.
 */

public class DbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "track.db";
    private static final int DATABASE_VERSION = 1;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_TRACK_TABLE = "CREATE TABLE " + track.TABLE_NAME + " ("
                + track.COLUMN_DATE + " TEXT NOT NULL, "
                + track.COLUMN_TIME + " TEXT NOT NULL, "
                + track.COLUMN_SPEED + " DOUBLE NOT NULL, "
                + track.COLUMN_LAT + " DOUBLE NOT NULL, "
                + track.COLUMN_LON + " DOUBLE NOT NULL "
                 + ");";
        db.execSQL(SQL_CREATE_TRACK_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
