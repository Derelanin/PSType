package com.example.pstype_v1.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.pstype_v1.R;
import com.example.pstype_v1.useful.MyPreferenceActivity;
import com.example.pstype_v1.useful.tracking;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;


public class Maps extends AppCompatActivity {
    GoogleMap googleMap;
    ArrayList<LatLng> list = new ArrayList<LatLng>();
    PolylineOptions polylineOptions;
    com.example.pstype_v1.useful.Notification not;
    final String SHARED_PREF_NAME = "SHARED_PREF_NAME";
    static SharedPreferences sPref, sp;
    Timer timer;
    TimerTask task;
    LatLng loc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        createMapView();

        final FloatingActionButton gps = (FloatingActionButton)findViewById(R.id.gps);
        gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getView();
            }
        });

        final FloatingActionButton start = (FloatingActionButton)findViewById(R.id.start);
        final FloatingActionButton stop = (FloatingActionButton)findViewById(R.id.stop);

        sPref = this.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (sPref.getBoolean("SCREEN", false))
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        SetTimer();

        boolean showButton = sPref.getBoolean("MAP", false);
        if (showButton) {
            start.setVisibility(View.INVISIBLE);
            stop.setVisibility(View.VISIBLE);
            timer.schedule(task, 0, 5000);
        }
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gps.callOnClick();
                googleMap.clear();
                polylineOptions = new PolylineOptions()
                        .add(loc)
                        .color(Color.RED).width(3);
                //googleMap.addPolyline(polylineOptions);
                //addTrack();

                MyPreferenceActivity.LogDelete(Maps.this);
                SharedPreferences.Editor editor = sPref.edit();
                editor.putBoolean("MAP", true);
                editor.apply();

                not=new com.example.pstype_v1.useful.Notification(Maps.this);
                not.Show();
                sPref = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
                editor = sPref.edit();
                editor.putInt("FLAG", 1);
                editor.putInt("TIME", 1000);
                editor.apply();
                editor = sp.edit();
                editor.putBoolean("look", true);
                editor.apply();
                startService(new Intent(Maps.this, tracking.class));

                SetTimer();
                timer.schedule(task, 2000, 5000);

                start.setVisibility(View.INVISIBLE);
                stop.setVisibility(View.VISIBLE);
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sPref.edit();
                editor.putBoolean("MAP", false);
                editor.apply();
                not=new com.example.pstype_v1.useful.Notification(Maps.this);
                not.NotShow();

                sPref = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
                editor = sPref.edit();
                editor.putInt("FLAG", 0);
                editor.putInt("TIME", 120000);
                editor.apply();
                editor = sp.edit();
                editor.putBoolean("look", false);
                editor.apply();
                stopService(new Intent(Maps.this, tracking.class));
                tracking.SendTracking sendTracking = new tracking.SendTracking(Maps.this);
                sendTracking.execute();
                timer.cancel();

                start.setVisibility(View.VISIBLE);
                stop.setVisibility(View.INVISIBLE);
            }
        });
    }

    void DrawTrack(){
        String FILENAME = "PSType-LatLng";
        LatLng point;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(openFileInput(FILENAME)));
            String str = " ", latlng="";
            while ((str = br.readLine()) != null) {
                latlng=str;
            }
            String[] sep = latlng.split(Pattern.quote("|"));
            point = new LatLng(Double.parseDouble(sep[0]),Double.parseDouble(sep[1]));
            polylineOptions
                    .add(point)
                    .color(Color.RED).width(3);
            googleMap.addPolyline(polylineOptions);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void SetTimer(){
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            DrawTrack();
                        }
                        catch (Exception e){

                        }
                    }
                });
            }
        };
    }

    private void addMarker() {

        /** Make sure that the map has been initialised **/
        if (null != googleMap) {
            googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(0, 0))
                    .title("Marker")
                    .draggable(true)
            );
        }
    }

    void getView()
    {
        CameraPosition cameraPosition;
        CameraUpdate cameraUpdate;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        double latitude, longitude;
        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, true));

        if (location!=null)
        {
            latitude=location.getLatitude();
            longitude=location.getLongitude();
        }
        else
        {
            latitude=55.7531432;
            longitude=37.6198181;
        }

        loc = new LatLng(latitude,longitude);
        cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latitude, longitude))
                .zoom(15)
                .bearing(0)
                .tilt(0)
                .build();
        cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        googleMap.animateCamera(cameraUpdate);


        if(null == googleMap) {
            Toast.makeText(getApplicationContext(),
                    "Ошибка создания карты",Toast.LENGTH_SHORT).show();
        }
    }
    private void createMapView() {
        try {
            if (null == googleMap) {
                googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                        R.id.mapView)).getMap();
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                googleMap.setMyLocationEnabled(true);
               // googleMap.getUiSettings().setZoomControlsEnabled(true);

                getView();
                addTrack();
            }
        } catch (NullPointerException exception){
            Log.e("mapApp", exception.toString());
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
        timer.cancel();
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void addTrack()
    {
        String FILENAME = "PSType-LatLng";
        try {
            // открываем поток для чтения
            BufferedReader br = new BufferedReader(new InputStreamReader(openFileInput(FILENAME)));
            String str = " ";
            while ((str = br.readLine()) != null) {
                String[] sep = str.split(Pattern.quote("|"));
                try{
                list.add(new LatLng(Double.parseDouble(sep[0]),Double.parseDouble(sep[1])));
                }
                catch (Exception e){

                }
            }
            polylineOptions = new PolylineOptions()
                    .addAll(list)
                    .color(Color.RED).width(3);
            googleMap.addPolyline(polylineOptions);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
