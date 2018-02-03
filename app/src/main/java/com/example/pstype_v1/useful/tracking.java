package com.example.pstype_v1.useful;

import android.Manifest;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import com.example.pstype_v1.main.Maps;
import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.google.android.gms.internal.zzhl.runOnUiThread;

public class tracking extends Service {
    static int flag = 0;
    long trackStart;
    boolean sleepFlag = false, send;
    static int time = 120000;
    final String FILENAME = "PSType-log";
    final String FILENAMEL = "PSType-LatLng";
    final String FILENAMEA = "PSType-Accel";
    FileOutputStream outputStream;
    LatLng GPSPoint;
    Timer timer;
    static int id;
    String pattern = "##0.0000";
    DecimalFormat decimalFormat;
    final String SHARED_PREF_NAME = "SHARED_PREF_NAME";
    static SharedPreferences sPref;
    private SensorManager sensorManager;
    Sensor Accelerometer;

    public tracking() {
    }

    private LocationManager locationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sPref = this.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        time = sPref.getInt("TIME", 120000);
        if (time == 120000)
            SetLogMessage("-------Включено отслеживание раз в 2 минуты\n");
        else if (time == 1000)
            SetLogMessage("-------Включено отслеживание раз в 1 секунду\n");

        flag = sPref.getInt("FLAG", 0);
        if (flag == 2) trackStart = sPref.getLong("TRACKSTART", 0);
        send = sPref.getBoolean("SEND", false);
        if (send) {
            SendTracking sendTracking = new SendTracking(this);
            sendTracking.execute();
        }

        decimalFormat = new DecimalFormat(pattern);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return START_STICKY;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, time, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, time, 0, locationListener);
        Accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(listener, Accelerometer,  SensorManager.SENSOR_DELAY_NORMAL);

        //Таймер для того, чтобы раз в 10 секунд запихивать в файлик последнюю точку.
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            SetLatLng();
                        }
                        catch (Exception e){

                        }
                    }
                });
            }
        };
        timer.schedule(task, 0, 5000);

        return Service.START_STICKY;
    }

    public void run() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, time, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, time, 0, locationListener);
        sensorManager.registerListener(listener, Accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
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
            if (ActivityCompat.checkSelfPermission(tracking.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(tracking.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //showLocation(locationManager.getLastKnownLocation(provider));
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

    };

    private SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            InputDataAccel(event.values[0], event.values[1], event.values[2]);
            SetLogAccel(new java.sql.Timestamp(new Date().getTime())+" X:"+  decimalFormat.format(event.values[0])
                    +" Y:"+ decimalFormat.format(event.values[1])
                    +" Z:"+ decimalFormat.format(event.values[2])+"\n");
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            String par ="";
            if (accuracy==1)
                par="Низкая точность, необходима калибровка";
            else if (accuracy==2)
                par="Средняя точность";
            else if (accuracy==3)
                par="Максимально возможная точность";
            else
                par="Данные, предоставляемые датчиком, недостоверны. Совсем всё плохо";
            SetLogAccel("\n\n"+par+"\n");
        }
    };

    private void showLocation(Location location) {
        if (location == null)
            return;
        if ((location.getProvider().equals(LocationManager.GPS_PROVIDER)) || (location.getProvider().equals(LocationManager.NETWORK_PROVIDER))) {

            //Условие на точность результата в пределах 30 метров.
            if (location.getAccuracy()<30) {
                Date date = new Date(location.getTime());
                double speed = location.getSpeed() * 3.6;
                InputData(new java.sql.Timestamp(date.getTime()) + "", speed, location.getLatitude(), location.getLongitude());
                SetLogMessage("[" + date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds() + "] Ш: " + decimalFormat.format(location.getLatitude()) + "; Д: " + decimalFormat.format(location.getLongitude()) +
                        "; С: " + decimalFormat.format(speed) + "\n");
                GPSPoint = new LatLng(location.getLatitude(),location.getLongitude());

                //Надеюсь, что это работает.
                /*
                 * Флаги:
                 * 0 - слежка 2 минуты.
                 * 1 - слежка 1 секунда
                 * 2 - отслеживание остановки, 10 минут.
                 */
                //Если скорость больше 10 км/ч и флаг 0, то запускаем ежесекундную слежку.
                if (speed > 10 && flag == 0) {
                    SetLogMessage("-------Обнаружено движение. Включено отслеживание раз в 1 секунду\n");
                    time = 1000;
                    TODO: удаление текстовых данных, запись трека для отрисовки
                    run();
                    flag = 1;
                    SharedPreferences.Editor editor = sPref.edit();
                    editor.putInt("FLAG", flag);
                    editor.putInt("TIME", time);
                    editor.apply();
                }
                //Если скорость меньше 10 км/ч и флаг 1, то засекаем время.
                else if (speed < 10 && flag == 1) {
                    SetLogMessage("-------Скорость меньше 10 км/ч. Засекаем время и отслеживаем остановку\n");
                    trackStart = date.getTime();
                    flag = 2;
                    SharedPreferences.Editor editor = sPref.edit();
                    editor.putInt("FLAG", flag);
                    editor.putLong("TRACKSTART", trackStart);
                    editor.apply();
                }
                //Если скорость больше 10 км/ч и флаг 2, то движение восстановилось, слежка 1 секунда
                else if (speed > 10 && flag == 2) {
                    SetLogMessage("-------Скорость повысилась. Остановки не было\n");
                    flag = 1;
                    SharedPreferences.Editor editor = sPref.edit();
                    editor.putInt("FLAG", flag);
                    editor.apply();
                }
                //Если скорость меньше 10 км/ч, флаг 2 и прошло 10 минут, то запуск слежки раз в 10 минут.
                else if (speed < 10 && flag == 2 && (date.getTime() - trackStart) > 600000) {
                    SetLogMessage("-------Остановка. Включено отслеживание раз в 2 минуты\n");
                    flag = 0;
                    time = 120000;
                    SharedPreferences.Editor editor = sPref.edit();
                    editor.putInt("FLAG", flag);
                    editor.putInt("TIME", time);
                    editor.putBoolean("MAP", false);
                    editor.apply();
                    run();
                    sendObr();
                    SendTracking sendTracking = new SendTracking(this);
                    sendTracking.execute();
                }
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
    }

    void sendObr(){
        String FILENAME = "PSType-LatLng";
        String points = "[";
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(openFileInput(FILENAME)));
            String str = " ", latlng="";
            while ((str = br.readLine()) != null) {
                latlng=str;
                String[] sep = latlng.split(Pattern.quote("|"));
                points+="{lat: \""+Double.parseDouble(sep[0])+"\", lon: \""+Double.parseDouble(sep[1])+"\"};";
            }

            points = points.substring(0,points.length()-1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (Exception e){

        }
        points+="]";


        Date date = new Date();
        String dd = new java.sql.Timestamp(date.getTime()) + "";
        String StopTime = dd.substring(11,19);

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        };
        Response.ErrorListener errorListener= new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        };
        String[] headers = {"token","points", "StopTime"};
        String[] values = {tokenSaver.getToken(tracking.this),points, StopTime};
        Request signReq = new Request(headers,values,getString(R.string.url_obr),responseListener,errorListener);
        RequestQueue queue = Volley.newRequestQueue(tracking.this);
        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        signReq.setRetryPolicy(policy);
        queue.add(signReq);
    }

    public void InputData (String date, double speed, double lat, double lon) {
        DbHelper mDbHelper = new DbHelper(this);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(track.COLUMN_DATE, date);
        values.put(track.COLUMN_SPEED, speed);
        values.put(track.COLUMN_LAT, lat);
        values.put(track.COLUMN_LON, lon);
        long newRowId = db.insert(track.TABLE_NAME, null, values);
        db.close();
        if (newRowId == -1) {

        }
    }
    public void InputDataAccel (double x, double y, double z) {
        DbHelper mDbHelper = new DbHelper(this);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Contract.accel.COLUMN_X, x);
        values.put(Contract.accel.COLUMN_Y, y);
        values.put(Contract.accel.COLUMN_Z, z);
        long newRowId = db.insert(Contract.accel.TABLE_NAME, null, values);
        db.close();
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

    void SetLatLng ()
    {
        String message;
        try {
            message=GPSPoint.latitude + "|" + GPSPoint.longitude + "\n";
            outputStream = openFileOutput(FILENAMEL, MODE_APPEND);
            outputStream.write(message.getBytes());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void SetLogAccel (String message)
    {
        try {
            outputStream = openFileOutput(FILENAMEA, MODE_APPEND);
            outputStream.write(message.getBytes());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static class SendTracking extends AsyncTask<Void, Void, Void> {

        Context context;
        public SendTracking(Context context)
        {
            this.context=context;
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
            }
            catch (Exception e){

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }

    static void Sending(Context context)
    {
        if (test(context))
        {
            send(context);
        }
        else {
            SharedPreferences.Editor editor = sPref.edit();
            editor.putBoolean("SEND", false);
            editor.apply();
        }
    }

    static boolean test(Context context) {
        id=0;
        DbHelper mDbHelper= new DbHelper(context);
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
        if (id!=0) return true;
        else return false;
    }
    static void deleteTrack(String _id, Context context){
        DbHelper mDbHelper = new DbHelper(context);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        db.delete(Contract.track.TABLE_NAME, "ID = ?", new String[]{_id});
        db.close();
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
    static void send(final Context context){
        DbHelper mDbHelper= new DbHelper(context);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = {
                Contract.track.COLUMN_ID,
                track.COLUMN_DATE,
                Contract.track.COLUMN_SPEED,
                Contract.track.COLUMN_LAT,
                Contract.track.COLUMN_LON};
        Cursor cursor = db.query(
                Contract.track.TABLE_NAME,
                projection,
                Contract.track.COLUMN_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        double currentSpeed=0, currentLat=0, currentLon=0;
        String date="";
        try {
            int speedColumnIndex = cursor.getColumnIndex(Contract.track.COLUMN_SPEED);
            int idCount = cursor.getColumnIndex(Contract.track.COLUMN_ID);
            int latColumnIndex = cursor.getColumnIndex(Contract.track.COLUMN_LAT);
            int dateColumnIndex = cursor.getColumnIndex(track.COLUMN_DATE);
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
        Response.ErrorListener errorListener= new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //При ошибке - ничего не делаем. Запись остаеётся в БД.
            }
        };
        String[] headers = {"token", "longitude", "latitude", "speed", "date"};
        String[] values = {tokenSaver.getToken(context), String.valueOf(currentLon), String.valueOf(currentLat), String.valueOf(currentSpeed), date};
        Request maps = new Request(headers,values,context.getString(R.string.url_pos),responseListener,errorListener);
        RequestQueue queue = Volley.newRequestQueue(context);
        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        maps.setRetryPolicy(policy);
        queue.add(maps);
    }
}
