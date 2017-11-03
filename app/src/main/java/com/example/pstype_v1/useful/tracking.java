package com.example.pstype_v1.useful;

import android.Manifest;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.pstype_v1.R;
import com.example.pstype_v1.data.Contract;
import com.example.pstype_v1.data.Contract.track;
import com.example.pstype_v1.data.DbHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class tracking extends Service {
    int flag=0;
    long trackStart;
    boolean sleepFlag = false;
    int time = 120000;
    final String FILENAME = "PSType-log";
    FileOutputStream outputStream;
    //BufferedWriter bw;
    int id;
    String pattern = "##0.0000";
    DecimalFormat decimalFormat;

    public tracking() {
    }

    private LocationManager locationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SetLogMessage("-------Старт фоновой активности. Включено отслеживание раз в 2 минуты\n");
        decimalFormat = new DecimalFormat(pattern);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return START_STICKY;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, time, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, time, 0, locationListener);
        return Service.START_STICKY;
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            showLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

    };

    private void showLocation(Location location) {
        if (location == null)
            return;
        if ((location.getProvider().equals(LocationManager.GPS_PROVIDER)) || (location.getProvider().equals(LocationManager.NETWORK_PROVIDER))) {
            Date date = new Date(location.getTime());
            String year = date.getYear()+"";
            double speed = (location.getSpeed()*3600.0)/1000.0;

            InputData(date.getDate()+"-"+(date.getMonth()+1)+"-"+year.substring(1),
                    date.getHours()+":"+date.getMinutes()+":"+date.getSeconds(),
                    (location.getSpeed()*3600.0)/1000.0,location.getLatitude(), location.getLongitude());

            //decimalFormat.format(currentLat)
            SetLogMessage("["+date.getHours()+":"+date.getMinutes()+":"+date.getSeconds()+"] Ш: "+decimalFormat.format(location.getLatitude())+"; Д: "+decimalFormat.format(location.getLongitude())+
                           "; С: "+decimalFormat.format(speed)+"\n");

            //Тут оно посылалось на сервер, но я сделаю это в конце.

//            Response.Listener<String> responseListener = new Response.Listener<String>() {
//               @Override
//               public void onResponse(String response) {
//                   try {
//                        JSONObject jsonResponse = new JSONObject(response);
//                        String success = jsonResponse.getString("status");
//                        //Тут, в принципе, я никак на ответы не должна реагировать. Оно всё в фоновом режиме посылается.
//                      } catch (JSONException e) {
//                        e.printStackTrace();
//                   }
//               }
//            };
//           Response.ErrorListener errorListener= new Response.ErrorListener() {
//           @Override
//            public void onErrorResponse(VolleyError error) {
//            //Тут аналогично. Оно всё в фоновом режиме посылается.
//           }
//           };
//
//            String[] headers = {"token", "longitude", "latitude", "speed"};
//            String[] values = {tokenSaver.getToken(tracking.this), String.valueOf(location.getLongitude()), String.valueOf(location.getLatitude()), String.valueOf((location.getSpeed()*3600.0)/1000.0)};
//            Request maps = new Request(headers,values,getString(R.string.url_pos),responseListener,errorListener);
//            RequestQueue queue = Volley.newRequestQueue(tracking.this);
//            int socketTimeout = 30000;//30 seconds - change to what you want
//            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
//            maps.setRetryPolicy(policy);
//            queue.add(maps);

            //Надеюсь, это это работает.
            /*
             * Флаги:
             * 0 - слежка 10 минут.
             * 1 - слежка 1 секунда
             * 2 - отслеживание остановки, 10 минут
             */
            //Если скорость больше 10 км/ч и флаг 0, то запускаем ежесекундную слежку.
            if (speed>10 && flag==0){
                SetLogMessage("-------Обнаружено движение. Включено отслеживание раз в 1 секунду\n");
                SetLogMessage("-------//Если скорость больше 10 км/ч и флаг 0, то запускаем ежесекундную слежку.\n");
                SetLogMessage("-------//flag: 0 -> 1\n");
                flag=1;
                time=1000;
            }
            //Если скорость меньше 10 км/ч и флаг 1, то засекаем время.
            else if (speed<10 && flag==1){
                SetLogMessage("-------Скорость меньше 10 км/ч. Засекаем время и отслеживаем остановку\n");
                SetLogMessage("-------//Если скорость меньше 10 км/ч и флаг 1, то засекаем время.\n");
                SetLogMessage("-------//flag: 1 -> 2\n");
                trackStart = date.getTime();
                flag=2;
            }
            //Если скорость больше 10 км/ч и флаг 2, то движение восстановилось, слежка 1 секунда
            else if (speed>10 && flag==2){
                SetLogMessage("-------Скорость повысилась. Остановки не было\n");
                SetLogMessage("-------//Если скорость больше 10 км/ч и флаг 2, то движение восстановилось, слежка 1 секунда\n");
                SetLogMessage("-------//flag: 2 -> 1\n");
                flag=1;
            }
            //Если скорость меньше 10 км/ч, флаг 2 и прошло 10 минут, то запуск слежки раз в 10 минут.
            else if (speed<10 && flag==2 && (date.getTime()-trackStart)>6000000){
                SetLogMessage("-------Остановка. Включено отслеживание раз в 2 минуты\\n");
                SetLogMessage("-------//Если скорость меньше 10 км/ч, флаг 2 и прошло 10 минут, то запуск слежки раз в 10 минут.\n");
                SetLogMessage("-------//flag: 2 -> 0\n");
                flag=0;
                time=120000;

                SetLogMessage("Отслеживание кончилось, теперь запускаем отправку\n");
                SendTracking sendTracking = new SendTracking();
                sendTracking.execute();
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
    }

    public void InputData (String date, String time, double speed, double lat, double lon) {
        DbHelper mDbHelper = new DbHelper(this);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(track.COLUMN_DATE, date);
        values.put(track.COLUMN_TIME, time);
        values.put(track.COLUMN_SPEED, speed);
        values.put(track.COLUMN_LAT, lat);
        values.put(track.COLUMN_LON, lon);
        long newRowId = db.insert(track.TABLE_NAME, null, values);
        if (newRowId == -1) {

        }
    }

    void SetLogMessage (String message)
    {
        try {
            outputStream = openFileOutput(FILENAME, MODE_APPEND);
            outputStream.write(message.getBytes());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class SendTracking extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            SetLogMessage("Начало отправления\n");
        }

        @Override
        protected Void doInBackground(Void... params) {
            while (test())
            {
                send();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            SetLogMessage("Конец отправления\n");
        }
    }
    boolean test() {
        id=0;
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
        db.close();
        if (id!=0) return true;
        else return false;
    }
    void deleteTrack (String _id){
        try {
            DbHelper mDbHelper = new DbHelper(this);
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            db.delete(Contract.track.TABLE_NAME, "ID = ?", new String[]{_id});
            db.close();
            SetLogMessage("Запись отправлена и удалена\n");
        }
        catch (Exception e)
        {

        }
    }
    void sendSleep()
    {
        //Здесь поток спит, чтобы успел обработаться запрос и не возникало чёртовых циклов, убивающих всё.
        if (sleepFlag)
            try {
                TimeUnit.SECONDS.sleep(30);
                sleepFlag=false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
                Contract.track.COLUMN_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
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
        db.close();

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Если отправлено, то удаляем запись из БД.
                deleteTrack(String.valueOf(id));
            }
        };
        Response.ErrorListener errorListener= new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //При ошибке - ничего не делаем. Запись остаеётся в БД.
                SetLogMessage("Запись не отправлена\n");
            }
        };
        String[] headers = {"token", "longitude", "latitude", "speed"};
        String[] values = {tokenSaver.getToken(com.example.pstype_v1.useful.tracking.this), String.valueOf(currentLon), String.valueOf(currentLat), String.valueOf(currentSpeed)};
        Request maps = new Request(headers,values,getString(R.string.url_pos),responseListener,errorListener);
        RequestQueue queue = Volley.newRequestQueue(com.example.pstype_v1.useful.tracking.this);
        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        maps.setRetryPolicy(policy);
        queue.add(maps);
        sleepFlag=true;
        sendSleep();
    }
}
