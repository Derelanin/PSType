package com.example.pstype_v1.useful;

import android.Manifest;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;

import com.example.pstype_v1.data.Contract.track;
import com.example.pstype_v1.data.DbHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;

public class tracking extends Service {
    int flag=0;
    long trackStart;
    int time = 120000;
    final String FILENAME = "PSType-log";
    //BufferedWriter bw;
    String pattern = "##0.0000";
    DecimalFormat decimalFormat;
    FileOutputStream outputStream;

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
        SetLogMessage("-------(Это сообщение должно появиться только один раз)\n");
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

            if (speed>10) {
                InputData(date.getDate() + "-" + (date.getMonth() + 1) + "-" + year.substring(1),
                        date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds(),
                        speed, location.getLatitude(), location.getLongitude());
                //decimalFormat.format(currentLat)
                SetLogMessage("["+date.getHours()+":"+date.getMinutes()+":"+date.getSeconds()+"] Ш: "+decimalFormat.format(location.getLatitude())+"; Д: "+decimalFormat.format(location.getLongitude())+
                        "; С: "+decimalFormat.format(speed)+"\n");
            }


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
                startService(new Intent(this, SendTracking.class));
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
}
