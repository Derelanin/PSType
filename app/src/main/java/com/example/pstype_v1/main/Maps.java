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
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.pstype_v1.R;
import com.example.pstype_v1.useful.MyPreferenceActivity;
import com.example.pstype_v1.useful.Request;
import com.example.pstype_v1.useful.SendAccel;
import com.example.pstype_v1.useful.SendTracking;
import com.example.pstype_v1.useful.tokenSaver;
import com.example.pstype_v1.useful.tracking;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;


public class Maps extends AppCompatActivity  implements OnMapReadyCallback {
    private GoogleMap gMap;
    GoogleMap googleMap;
    ArrayList<LatLng> list = new ArrayList<LatLng>();
    PolylineOptions polylineOptions;
    com.example.pstype_v1.useful.Notification not;
    final String SHARED_PREF_NAME = "SHARED_PREF_NAME";
    static SharedPreferences sPref, sp;
    Timer timer;
    TimerTask task;
    Timer timerSpeed;
    TimerTask taskSpeed;
    LatLng loc;
    CameraPosition cameraPosition;
    static String speedMap = "0";
    TextView speedView = (TextView)findViewById(R.id.textView35);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        final FloatingActionButton gps = (FloatingActionButton) findViewById(R.id.gps);
        gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getView();
            }
        });

        final FloatingActionButton start = (FloatingActionButton) findViewById(R.id.start);
        final FloatingActionButton stop = (FloatingActionButton) findViewById(R.id.stop);
        final ConstraintLayout howFast = (ConstraintLayout)findViewById(R.id.howFast);

        sPref = this.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (sPref.getBoolean("SCREEN", false))
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (sPref.getBoolean("POINTS", false)) {
            start.setVisibility(View.INVISIBLE);
            gps.setVisibility(View.INVISIBLE);
            SharedPreferences.Editor editor = sPref.edit();
            editor.putBoolean("POINTS", false);
            editor.apply();
            DrawPoints();
        } else
            createMapView();

        //Не знаю почему, но если его убрать, то всё к чёрту крашится
        SetTimer();
        //SetSpeedTimer();

        boolean showButton = sPref.getBoolean("MAP", false);
        if (showButton) {
            start.setVisibility(View.INVISIBLE);
            stop.setVisibility(View.VISIBLE);
            timer.schedule(task, 0, 5000);
        }
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Карта центруется на пользователе, очищается, начинает прокладку маршрута
                gps.callOnClick();
                googleMap.clear();
                polylineOptions = new PolylineOptions()
                        .add(loc)
                        .color(Color.RED).width(5);
                //googleMap.addPolyline(polylineOptions);
                //addTrack();

                //Отправление на сервер данных о начале поездки
                trackBegin(Maps.this, getString(R.string.url_startPos));

                //Удаление лога старой поездки
                MyPreferenceActivity.LogDelete(Maps.this);

                //Установка флага карты (зачем?)
                SharedPreferences.Editor editor = sPref.edit();
                editor.putBoolean("MAP", true);
                editor.apply();

                //Уведомление
                not = new com.example.pstype_v1.useful.Notification(Maps.this);
                not.Show();
                sPref = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
                editor = sPref.edit();
                //Время и режим отслеживания
                editor.putInt("FLAG", 1);
                editor.putInt("TIME", 1000);
                editor.apply();
                editor = sp.edit();
                //Включение отслеживания в настройках, если не включено
                editor.putBoolean("look", true);
                editor.apply();
                //Запуск отслеживания
                startService(new Intent(Maps.this, tracking.class));

                //Таймер для отрисовки на карте
                SetTimer();
                SetSpeedTimer();
                timer.schedule(task, 2000, 5000);
                timerSpeed.schedule(taskSpeed, 0, 500);

                start.setVisibility(View.INVISIBLE);
                stop.setVisibility(View.VISIBLE);
                howFast.setVisibility(View.VISIBLE);
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sPref.edit();
                editor.putBoolean("MAP", false);
                editor.apply();
                not = new com.example.pstype_v1.useful.Notification(Maps.this);
                not.NotShow();

                stopService(new Intent(Maps.this, tracking.class));
                sPref = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
                editor = sPref.edit();
                editor.putInt("FLAG", 0);
                editor.putInt("TIME", 120000);
                //editor.putBoolean("ACCEL", false);
                editor.apply();
                editor = sp.edit();
                editor.putBoolean("look", false);
                editor.apply();
                sendObr();
                SendTracking sendTracking = new SendTracking(Maps.this);
                sendTracking.execute();
                timer.cancel();
                timerSpeed.cancel();

                start.setVisibility(View.VISIBLE);
                stop.setVisibility(View.INVISIBLE);
                howFast.setVisibility(View.INVISIBLE);
            }
        });
    }

    public static void trackBegin(Context c, String url) {
        Date date = new Date();
        String dd = new java.sql.Timestamp(date.getTime()) + "";
        String dateTrack = dd.substring(0, 10);
        String StartTime = dd.substring(11, 19);
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    String success = jsonResponse.getString("status");
                    if (success.equals("ok")) {

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                int k = 0;
            }
        };
        String[] headers = {"token", "dateTrack", "StartTime"};
        String[] values = {tokenSaver.getToken(c), dateTrack, StartTime};
        Request signReq = new Request(headers, values, url, responseListener, errorListener);
        RequestQueue queue = Volley.newRequestQueue(c);
        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 10, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        signReq.setRetryPolicy(policy);
        queue.add(signReq);
    }

    void DrawTrack() {
        String FILENAME = "PSType-LatLng";
        LatLng point;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(openFileInput(FILENAME)));
            String str = " ", latlng = "";
            while ((str = br.readLine()) != null) {
                latlng = str;
            }
            String[] sep = latlng.split(Pattern.quote("|"));
            point = new LatLng(Double.parseDouble(sep[0]), Double.parseDouble(sep[1]));
            polylineOptions
                    .add(point)
                    .color(Color.RED).width(5);
            googleMap.addPolyline(polylineOptions);

            cameraPosition = new CameraPosition.Builder()
                    .target(point)
                    .bearing(0)
                    .tilt(0)
                    .build();
            getView();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void sendObr() {
        String FILENAME = "PSType-LatLng";
        String FILENAMEACCEL = "PSType-Accel";
        JSONObject pointsObj = new JSONObject();
        String points = "[";
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(openFileInput(FILENAME)));
            String str = " ";
            int trackNum = 0;
            JSONArray tempTrack = new JSONArray();
            while ((str = br.readLine()) != null) {
                String[] sep = str.split(Pattern.quote("|"));
                JSONObject tracksObj = new JSONObject();
                tracksObj.put("lat", Double.parseDouble(sep[0]));
                tracksObj.put("lon", Double.parseDouble(sep[1]));
                tempTrack.put(trackNum,tracksObj);
                trackNum++;
            }
            pointsObj.put("track", tempTrack);

            if (sPref.getBoolean("ACCEL", false)) {

//                points += "[";
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                int boundaryZ = Integer.parseInt(prefs.getString(getString(R.string.refresh), "5"));
                double tempZ = 0;
                br = new BufferedReader(new InputStreamReader(openFileInput(FILENAMEACCEL)));
                trackNum = 0;
                tempTrack = new JSONArray();
                while ((str = br.readLine()) != null) {
                    String[] sep = str.split(Pattern.quote("|"));

                    String type = "";
                    if (Double.parseDouble(sep[3]) != tempZ) {
                        tempZ = Double.parseDouble(sep[3]);
                        if (Double.parseDouble(sep[1]) > boundaryZ) type = "Резкий поворот влево. ";
                        else if (Double.parseDouble(sep[1]) < (-boundaryZ))
                            type = "Резкий поворот вправо. ";
                        if (Double.parseDouble(sep[3]) > boundaryZ) type += "Резкое торможение. ";
                        else if (Double.parseDouble(sep[3]) < (-boundaryZ))
                            type += "Резкое ускорение. ";
                    }
                    if (type == "") break;
                    tracking.InputDataAccel(Double.parseDouble(sep[1]), Double.parseDouble(sep[2]), Double.parseDouble(sep[3]), Double.parseDouble(sep[4]), Double.parseDouble(sep[5]), sep[0], type, this);

                    JSONObject tracksObj = new JSONObject();
                    tracksObj.put("lat", Double.parseDouble(sep[4]));
                    tracksObj.put("lon", Double.parseDouble(sep[5]));
                    tracksObj.put("type", type);
                    tempTrack.put(trackNum, tracksObj);
                    trackNum++;
                }
                pointsObj.put("accel", tempTrack);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


        Date date = new Date();
        String dd = new java.sql.Timestamp(date.getTime()) + "";
        String StopTime = dd.substring(11, 19);

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        };
        String[] headers = {"token", "StopTime", "points"};
        String[] values = {tokenSaver.getToken(Maps.this), StopTime, points};
        Request signReq = new Request(headers, values, pointsObj, getString(R.string.url_obr), responseListener, errorListener);
        RequestQueue queue = Volley.newRequestQueue(Maps.this);
        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 10, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        signReq.setRetryPolicy(policy);
        queue.add(signReq);

        SendAccel sendAccel = new SendAccel(this);
        sendAccel.execute();
        SendTracking sendTracking = new SendTracking(this);
        sendTracking.execute();
    }

    void SetTimer() {
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            DrawTrack();
                        } catch (Exception e) {

                        }
                    }
                });
            }
        };
    }

    void SetSpeedTimer() {
        timerSpeed = new Timer();
        taskSpeed = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            speedView.setText(speedMap);
                        } catch (Exception e) {

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

    void getView() {
        CameraUpdate cameraUpdate;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        double latitude, longitude;
        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, true));

        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        } else {
            latitude = 55.7531432;
            longitude = 37.6198181;
        }

        loc = new LatLng(latitude, longitude);
        cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latitude, longitude))
                .zoom(15)
                .bearing(0)
                .tilt(0)
                .build();
        cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        googleMap.animateCamera(cameraUpdate);


        if (null == googleMap) {
            Toast.makeText(getApplicationContext(),
                    "Ошибка создания карты", Toast.LENGTH_SHORT).show();
        }
    }

    private void createMapView() {
        try {
            if (null == googleMap) {
                googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapView)).getMap();
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
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

    public static void setSpeed(String speed){
        speedMap=speed;
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
                    .color(Color.RED).width(5);
            googleMap.addPolyline(polylineOptions);
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (Exception e){}
    }

    void DrawPoints(){
        Intent intent = getIntent();
        String point = intent.getStringExtra("points");
        try {
            if (null == googleMap) {
                googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapView)).getMap();
            }
        } catch (NullPointerException exception){
            Log.e("mapApp", exception.toString());
        }
        try {

            list=new ArrayList<LatLng>();
            JSONObject pointsJSON = new JSONObject(point);
            JSONArray tracksJSON = pointsJSON.getJSONArray("points");
            pointsJSON = new JSONObject(tracksJSON.getString(0));
            JSONArray trackJSON = pointsJSON.getJSONArray("track");

            LatLng start = null;
            LatLng stop = null;
            for (int i = 0; i<trackJSON.length(); i++){
                JSONObject pointJSON = trackJSON.getJSONObject(i);
                if (i==0){
                    start = new LatLng(pointJSON.getDouble("lat"),pointJSON.getDouble("lon"));
                }
                list.add(new LatLng(pointJSON.getDouble("lat"),pointJSON.getDouble("lon")));
                stop = new LatLng(pointJSON.getDouble("lat"),pointJSON.getDouble("lon"));
            }
            polylineOptions = new PolylineOptions()
                    .addAll(list)
                    .color(Color.RED).width(5);
            googleMap.addPolyline(polylineOptions);

            if (pointsJSON.names().length()==2){
                JSONArray accelJSON = pointsJSON.getJSONArray("accel");
                for (int i = 0; i<accelJSON.length(); i++){
                    JSONObject pointJSON = accelJSON.getJSONObject(i);
                    googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(pointJSON.getDouble("lat"),pointJSON.getDouble("lon")))
                            .draggable(false)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                            .title(pointJSON.getString("type"))
                    );
                }
            }

            googleMap.addMarker(new MarkerOptions()
                    .position(start)
                    .draggable(false)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            );
            googleMap.addMarker(new MarkerOptions()
                    .position(stop)
                    .draggable(false)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            );


            LatLngBounds.Builder bounds = new LatLngBounds.Builder();
            for (LatLng p : list)
                bounds.include(p);
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            int padding = (int) (width * 0.12);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(),  width, height, padding));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap gMap) {
        googleMap = gMap;
        //getView();
    }
}
