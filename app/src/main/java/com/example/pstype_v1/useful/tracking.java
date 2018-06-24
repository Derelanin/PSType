package com.example.pstype_v1.useful;

import android.Manifest;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
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
import java.util.regex.Pattern;

public class tracking extends Service {
    static int flag = 0;
    long trackStart;
    boolean sleepFlag = false, send, accel, sendA;
    static int time = 120000;
    final String FILENAME = "PSType-log";
    final String FILENAMEL = "PSType-LatLng";
    final String FILENAMEA = "PSType-Accel";
    FileOutputStream outputStream;
    LatLng GPSPoint;
    Timer timer;
    String pattern = "##0.0000";
    DecimalFormat decimalFormat;
    final String SHARED_PREF_NAME = "SHARED_PREF_NAME";
    static SharedPreferences sPref;
    private SensorManager sensorManager;
    Sensor Accelerometer;
    int boundaryZ;
    double[] accelNow = new double[3];


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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boundaryZ = Integer.parseInt(prefs.getString(getString(R.string.refresh), "5"));
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
        sendA = sPref.getBoolean("SENDA", false);
        if (sendA) {
            SendAccel sendAccel = new SendAccel(this);
            sendAccel.execute();
        }
        accel = sPref.getBoolean("ACCEL", false);
        if (accel)
        {
            Accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            sensorManager.registerListener(listener, Accelerometer,  SensorManager.SENSOR_DELAY_NORMAL);
        }


        decimalFormat = new DecimalFormat(pattern);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return START_STICKY;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, time, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, time, 0, locationListener);

        return Service.START_STICKY;
    }

    public void run() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, time, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, time, 0, locationListener);
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
            double x, y, z;
            x = event.values[0];
            y = event.values[1];
            z = event.values[2];
            //Z - торможение и ускорение
            if (z>boundaryZ || z<(-boundaryZ)){
                if (GPSPoint==null) return;
                LatLng point = GPSPoint;
                SetLogAccel(new java.sql.Timestamp(new Date().getTime())+"|"+  x
                        +"|"+ y +"|"+ z
                        +"|"+ point.latitude
                        +"|"+ point.longitude+"\n");
            }
            //X - повороты
            if (x>boundaryZ || x<(-boundaryZ)){
                if (GPSPoint==null) return;
                LatLng point = GPSPoint;
                SetLogAccel(new java.sql.Timestamp(new Date().getTime())+"|"+  x
                        +"|"+ y +"|"+ z
                        +"|"+ point.latitude
                        +"|"+ point.longitude+"\n");
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private void showLocation(Location location) {
        if (location == null)
            return;
        if ((location.getProvider().equals(LocationManager.GPS_PROVIDER)) || (location.getProvider().equals(LocationManager.NETWORK_PROVIDER))) {

            Maps.setSpeed(location.getSpeed() * 3.6+"");
            //Условие на точность результата в пределах 15 метров.
            if (location.getAccuracy()<15) {
                Date date = new Date(location.getTime());
                double speed = location.getSpeed() * 3.6;
                InputData(new java.sql.Timestamp(date.getTime()) + "", speed, location.getLatitude(), location.getLongitude());
                SetLogMessage("[" + date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds() + "] Ш: " + decimalFormat.format(location.getLatitude()) + "; Д: " + decimalFormat.format(location.getLongitude()) +
                        "; С: " + decimalFormat.format(speed) + "\n");
                GPSPoint = new LatLng(location.getLatitude(),location.getLongitude());
                SetLatLng();

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
                    //Удаление текстовых данных, запись трека для отрисовки
                    MyPreferenceActivity.LogDelete(tracking.this);
                    //Отправка начала отслеживания на сервер
                    Maps.trackBegin(tracking.this, getString(R.string.url_startPos));
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
                    //sendTrackEnd();
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
    public static void InputDataAccel (double x, double y, double z, double lat, double lon, String date, String type, Context c) {
        DbHelper mDbHelper = new DbHelper(c);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Contract.accel.COLUMN_X, x);
        values.put(Contract.accel.COLUMN_Y, y);
        values.put(Contract.accel.COLUMN_Z, z);
        values.put(Contract.accel.COLUMN_LAT, lat);
        values.put(Contract.accel.COLUMN_LON, lon);
        values.put(Contract.accel.COLUMN_TYPE, type);
        if (date.substring(0, 1).equals(" ")) date = date.substring(1);
        values.put(Contract.accel.COLUMN_DATE, date.substring(0, 10));
        values.put(Contract.accel.COLUMN_TIME, date.substring(11, date.length()-1));
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

    void sendTrackEnd(){
        String FILENAME = "PSType-LatLng";
        String FILENAMEACCEL = "PSType-Accel";
        String points = "[";
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(openFileInput(FILENAME)));
            String str = " ", latlng="";
            while ((str = br.readLine()) != null) {
                latlng=str;
                String[] sep = latlng.split(Pattern.quote("|"));
                points+="{lat: \""+Double.parseDouble(sep[0])+"\", lon: \""+Double.parseDouble(sep[1])+"\"};";
            }

            if (sPref.getBoolean("ACCEL", false)) {

                points += "[";
                br = new BufferedReader(new InputStreamReader(openFileInput(FILENAMEACCEL)));
                str = " ";
                latlng = "";
                while ((str = br.readLine()) != null) {
                    latlng = str;
                    String[] sep = latlng.split(Pattern.quote("|"));
                    points += "{lat: \"" + Double.parseDouble(sep[4]) + "\", lon: \"" + Double.parseDouble(sep[5]) + "\"}>";
                }
            }

            points = points.substring(0,points.length()-1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (Exception e){

        }
        if (sPref.getBoolean("ACCEL", false)) points+="]]";
        else points+="]";


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
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 10, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        signReq.setRetryPolicy(policy);
        queue.add(signReq);
    }




}
