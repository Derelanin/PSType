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

public class SendTracking extends AsyncTask<Void, Void, Void> {

    Context context;
    static int id;
    final String SHARED_PREF_NAME = "SHARED_PREF_NAME";
    static SharedPreferences sPref;

    public SendTracking(Context context) {
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
            editor.putBoolean("SEND", true);
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
            editor.putBoolean("SEND", false);
            editor.apply();
        }
    }

    static boolean test(Context context) {
        id = 0;
        DbHelper mDbHelper = new DbHelper(context);
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
            db.close();
        }
        if (id != 0) return true;
        else return false;
    }

    static void deleteTrack(String _id, Context context) {
        DbHelper mDbHelper = new DbHelper(context);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        db.delete(Contract.track.TABLE_NAME, "ID = ?", new String[]{_id});
        db.close();
    }

    static void send(final Context context) {
        DbHelper mDbHelper = new DbHelper(context);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = {
                Contract.track.COLUMN_ID,
                Contract.track.COLUMN_DATE,
                Contract.track.COLUMN_SPEED,
                Contract.track.COLUMN_LAT,
                Contract.track.COLUMN_LON};
        Cursor cursor = db.query(
                Contract.track.TABLE_NAME,
                projection,
                Contract.track.COLUMN_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        double currentSpeed = 0, currentLat = 0, currentLon = 0;
        String date = "";
        try {
            int speedColumnIndex = cursor.getColumnIndex(Contract.track.COLUMN_SPEED);
            int idCount = cursor.getColumnIndex(Contract.track.COLUMN_ID);
            int latColumnIndex = cursor.getColumnIndex(Contract.track.COLUMN_LAT);
            int dateColumnIndex = cursor.getColumnIndex(Contract.track.COLUMN_DATE);
            int lonColumnIndex = cursor.getColumnIndex(Contract.track.COLUMN_LON);
            while (cursor.moveToNext()) {
                id = cursor.getInt(idCount);
                currentSpeed = cursor.getDouble(speedColumnIndex);
                date = cursor.getString(dateColumnIndex);
                currentLat = cursor.getDouble(latColumnIndex);
                currentLon = cursor.getDouble(lonColumnIndex);
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
        String[] headers = {"token", "longitude", "latitude", "speed", "date"};
        String[] values = {tokenSaver.getToken(context), String.valueOf(currentLon), String.valueOf(currentLat), String.valueOf(currentSpeed), date};
        Request maps = new Request(headers, values, context.getString(R.string.url_pos), responseListener, errorListener);
        RequestQueue queue = Volley.newRequestQueue(context);
        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        maps.setRetryPolicy(policy);
        queue.add(maps);
    }
}