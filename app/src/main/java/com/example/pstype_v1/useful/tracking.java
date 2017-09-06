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

import java.util.Date;

public class tracking extends Service {
    public tracking() {
    }

    private LocationManager locationManager;
    StringBuilder sbGPS = new StringBuilder();
    StringBuilder sbNet = new StringBuilder();

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        if ((flags & START_FLAG_RETRY) == 0) {
//            //  Если это повторный запуск, выполнить какие-то действия.
//        }
//        else {
//            // Альтернативные действия в фоновом режиме.
//        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //  Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return START_STICKY;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000 * 10, 10, locationListener);
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 1000 * 10, 10,
                locationListener);
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
            //formatLocation(location);
            Date date = new Date(location.getTime());
            String year = date.getYear()+"";
            InputData(date.getDate()+"-"+(date.getMonth()+1)+"-"+year.substring(1),
                    date.getHours()+":"+date.getMinutes()+":"+date.getSeconds(),
                    location.getSpeed(),location.getLatitude(), location.getLongitude());
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
}
