package com.example.pstype_v1.useful;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.pstype_v1.R;
import com.example.pstype_v1.data.Contract;
import com.example.pstype_v1.data.DbHelper;

/**
 * Created by Derelanin on 21.10.2017.
 */

public class SendTracking extends Service {

    int id;
    public void onCreate() {
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        while (test())
        {
            send();
        }
        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    boolean test() {
        int id=0;
        DbHelper mDbHelper= new DbHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = {
               Contract.track.COLUMN_ID};
        Cursor cursor = db.query(
                Contract.track.TABLE_NAME,
                projection,
                null, null, null, null, Contract.track.COLUMN_ID, "1");
        try {
            int idCount = cursor.getColumnIndex(Contract.track.COLUMN_ID);
            while (cursor.moveToNext()) {
                id = cursor.getInt(idCount);
            }
        } finally {
            cursor.close();
        }

        if (id!=0) return true;
        else return false;
    }

    void send(){
        DbHelper mDbHelper= new DbHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = {
                Contract.track.COLUMN_ID,
                Contract.track.COLUMN_SPEED,
                Contract.track.COLUMN_LAT,
                Contract.track.COLUMN_LON};
        Cursor cursor = db.query(
                Contract.track.TABLE_NAME,
                projection,
                Contract.track.COLUMN_ID + "=?", new String[]{"1"}, null, null, null);
        double currentSpeed=0, currentLat=0, currentLon=0;
        try {
            int speedColumnIndex = cursor.getColumnIndex(Contract.track.COLUMN_SPEED);
            int idCount = cursor.getColumnIndex(Contract.track.COLUMN_ID);
            int latColumnIndex = cursor.getColumnIndex(Contract.track.COLUMN_LAT);
            int lonColumnIndex = cursor.getColumnIndex(Contract.track.COLUMN_LON);
            while (cursor.moveToNext()) {
                id = cursor.getInt(idCount);
                currentSpeed = cursor.getDouble(speedColumnIndex);
                currentLat = cursor.getDouble(latColumnIndex);
                currentLon = cursor.getDouble(lonColumnIndex);
            }
        } finally {
            cursor.close();
        }

           Response.Listener<String> responseListener = new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    //Если отправлено, то удаляем запись из БД.
                    DbHelper mDbHelper= new DbHelper(SendTracking.this);
                    SQLiteDatabase db = mDbHelper.getReadableDatabase();
                    db.delete("track", "id = " + id, null);
                }
            };
            Response.ErrorListener errorListener= new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //При ошибке - ничего не делаем. Запись остаеётся в БД.
                }
            };
            String[] headers = {"token", "longitude", "latitude", "speed"};
            String[] values = {tokenSaver.getToken(SendTracking.this), String.valueOf(currentLon), String.valueOf(currentLat), String.valueOf(currentSpeed)};
            Request maps = new Request(headers,values,getString(R.string.url_pos),responseListener,errorListener);
            RequestQueue queue = Volley.newRequestQueue(SendTracking.this);
            int socketTimeout = 30000;
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            maps.setRetryPolicy(policy);
            queue.add(maps);
    }
}
