package com.example.pstype_v1.useful;

import android.Manifest;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
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
import android.support.v4.app.ActivityCompat;

import com.example.pstype_v1.data.Contract;
import com.example.pstype_v1.data.DbHelper;
import com.google.android.gms.maps.model.LatLng;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static com.google.android.gms.internal.zzhl.runOnUiThread;

/**
 * Created by Derelanin on 29.11.2017.
 */

public class FastTracking extends Service {
    static int time = 1000;
    final String FILENAME = "PSType-log";
    final String FILENAMEL = "PSType-LatLng";
    final String FILENAMEA = "PSType-Accel";
    FileOutputStream outputStream;
    LatLng GPSPoint;
    Timer timer;
    String pattern = "##0.0000";
    DecimalFormat decimalFormat;
    private SensorManager sensorManager;
    Sensor Accelerometer;

    public FastTracking() {
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
            if (ActivityCompat.checkSelfPermission(FastTracking.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(FastTracking.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        values.put(Contract.track.COLUMN_DATE, date);
        values.put(Contract.track.COLUMN_SPEED, speed);
        values.put(Contract.track.COLUMN_LAT, lat);
        values.put(Contract.track.COLUMN_LON, lon);
        long newRowId = db.insert(Contract.track.TABLE_NAME, null, values);
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

}