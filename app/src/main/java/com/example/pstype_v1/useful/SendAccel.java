package com.example.pstype_v1.useful;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.pstype_v1.R;
import com.example.pstype_v1.data.Contract;
import com.example.pstype_v1.data.DbHelper;

public class SendAccel extends AsyncTask<Void, Void, Void> {

    Context context;
    static int id;
    final String SHARED_PREF_NAME = "SHARED_PREF_NAME";
    static SharedPreferences sPref;

    public SendAccel(Context context) {
        this.context = context;
        sPref = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            SharedPreferences.Editor editor = sPref.edit();
            editor.putBoolean("SENDA", true);
            editor.apply();
            Sending(context);
        } catch (Exception e) {

        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
    }

    static void Sending(Context context) {
        if (test(context)) {
            send(context);
        } else {
            SharedPreferences.Editor editor = sPref.edit();
            editor.putBoolean("SENDA", false);
            editor.apply();
        }
    }

    static boolean test(Context context) {
        id = 0;
        DbHelper mDbHelper = new DbHelper(context);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = {
                Contract.accel.COLUMN_ID};
        Cursor cursor = db.query(
                Contract.accel.TABLE_NAME,
                projection,
                null, null, null, null, Contract.accel.COLUMN_ID, "1");
        try {
            int idCount = cursor.getColumnIndex(Contract.accel.COLUMN_ID);
            while (cursor.moveToNext()) {
                id = cursor.getInt(idCount);
            }
        } finally {
            cursor.close();
            db.close();
        }
        if (id != 0) return true;
        else return false;
    }

    static void deleteTrack(String _id, Context context) {
        DbHelper mDbHelper = new DbHelper(context);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        db.delete(Contract.accel.TABLE_NAME, "ID = ?", new String[]{_id});
        db.close();
    }

    static void send(final Context context) {
        DbHelper mDbHelper = new DbHelper(context);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = {
                Contract.accel.COLUMN_ID,
                Contract.accel.COLUMN_DATE,
                Contract.accel.COLUMN_TIME,
                Contract.accel.COLUMN_TYPE,
                Contract.accel.COLUMN_X,
                Contract.accel.COLUMN_Y,
                Contract.accel.COLUMN_Z,
                Contract.accel.COLUMN_LAT,
                Contract.accel.COLUMN_LON};
        Cursor cursor = db.query(
                Contract.accel.TABLE_NAME,
                projection,
                Contract.accel.COLUMN_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        double currentLat = 0, currentLon = 0, curX = 0, curY = 0, curZ = 0;
        String date = "", time = "", type = "";
        try {
            int dateColumnIndex = cursor.getColumnIndex(Contract.accel.COLUMN_DATE);
            int idCount = cursor.getColumnIndex(Contract.accel.COLUMN_ID);
            int latColumnIndex = cursor.getColumnIndex(Contract.accel.COLUMN_LAT);
            int timeColumnIndex = cursor.getColumnIndex(Contract.accel.COLUMN_TIME);
            int typeColumnIndex = cursor.getColumnIndex(Contract.accel.COLUMN_TYPE);
            int lonColumnIndex = cursor.getColumnIndex(Contract.accel.COLUMN_LON);
            int xColumnIndex = cursor.getColumnIndex(Contract.accel.COLUMN_X);
            int yColumnIndex = cursor.getColumnIndex(Contract.accel.COLUMN_Y);
            int zColumnIndex = cursor.getColumnIndex(Contract.accel.COLUMN_Z);
            while (cursor.moveToNext()) {
                id = cursor.getInt(idCount);
                date = cursor.getString(dateColumnIndex);
                time = cursor.getString(timeColumnIndex);
                type = cursor.getString(typeColumnIndex);
                currentLat = cursor.getDouble(latColumnIndex);
                currentLon = cursor.getDouble(lonColumnIndex);
                curX = cursor.getDouble(xColumnIndex);
                curY = cursor.getDouble(yColumnIndex);
                curZ = cursor.getDouble(zColumnIndex);
            }
        } finally {
            cursor.close();
            db.close();
        }

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Если отправлено, то удаляем запись из БД.
                deleteTrack(String.valueOf(id), context);
                Sending(context);
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //При ошибке - ничего не делаем. Запись остаеётся в БД.
            }
        };
        String[] headers = {"token", "x", "y", "z", "lon", "lat", "time", "date", "type"};
        String[] values = {tokenSaver.getToken(context), String.valueOf(curX), String.valueOf(curY), String.valueOf(curZ),
                String.valueOf(currentLon), String.valueOf(currentLat), time, date, type};
        Request maps = new Request(headers, values, context.getString(R.string.url_setAccel), responseListener, errorListener);
        RequestQueue queue = Volley.newRequestQueue(context);
        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        maps.setRetryPolicy(policy);
        queue.add(maps);
    }
}