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

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.pstype_v1.R;
import com.example.pstype_v1.useful.MyPreferenceActivity;
import com.example.pstype_v1.useful.Request;
import com.example.pstype_v1.useful.tokenSaver;
import com.example.pstype_v1.useful.tracking;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
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
    CameraPosition cameraPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //stopService(new Intent(Maps.this, FastTracking.class));
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

        if (sPref.getBoolean("POINTS", false)) {
            start.setVisibility(View.INVISIBLE);
            gps.setVisibility(View.INVISIBLE);
            SharedPreferences.Editor editor = sPref.edit();
            editor.putBoolean("POINTS", false);
            editor.apply();
            DrawPoints();
        }
        else
            createMapView();

        //Не знаю почему, но если его убрать, то всё к чёрту крашится
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
                not=new com.example.pstype_v1.useful.Notification(Maps.this);
                not.Show();
                sPref = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
                editor = sPref.edit();
                //Время и режим отслеживания
                editor.putInt("FLAG", 1);
                editor.putInt("TIME", 1000);
                //editor.putBoolean("ACCEL", true);
                editor.apply();
                editor = sp.edit();
                //Включение отслеживания в настройках, если не включено
                editor.putBoolean("look", true);
                editor.apply();
                //Запуск отслеживания
                startService(new Intent(Maps.this, tracking.class));

                //Таймер для отрисовки на карте
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
                tracking.SendTracking sendTracking = new tracking.SendTracking(Maps.this);
                sendTracking.execute();
                timer.cancel();

                start.setVisibility(View.VISIBLE);
                stop.setVisibility(View.INVISIBLE);
            }
        });
    }

    public static void trackBegin(Context c, String url){
        Date date = new Date();
        String dd = new java.sql.Timestamp(date.getTime()) + "";
        String dateTrack = dd.substring(0,10);
        String StartTime = dd.substring(11,19);
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
        Response.ErrorListener errorListener= new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                int k=0;
            }
        };
        String[] headers = {"token","dateTrack", "StartTime"};
        String[] values = {tokenSaver.getToken(c),dateTrack, StartTime};
        Request signReq = new Request(headers,values,url,responseListener,errorListener);
        RequestQueue queue = Volley.newRequestQueue(c);
        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 10, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        signReq.setRetryPolicy(policy);
        queue.add(signReq);
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
                    .color(Color.RED).width(5);
            googleMap.addPolyline(polylineOptions);

            cameraPosition = new CameraPosition.Builder()
                    .target(point)
                    .zoom(15)
                    .bearing(0)
                    .tilt(0)
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void sendObr(){
        //Получается длинная-длинная строка вида:
        //[{lat:"", lon:""};{lat:"", lon:""};{lat:"", lon:""};{lat:"", lon:""};[{lat:"", lon:"", type""}>{lat:"", lon:"", type""}]]
        //Здесь сначала идут точки отрисовки маршрута
        //А во вторых кавычках - точки опасных участков
        //[точки маршрута;[опасные участки]]

        String FILENAME = "PSType-LatLng";
        String FILENAMEACCEL = "PSType-Accel";
        JSONObject pointsObj = new JSONObject();
        String points = "[";
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(openFileInput(FILENAME)));
            JSONObject tracksObj = new JSONObject();
            String str = " ";
            int trackNum=0;
            while ((str = br.readLine()) != null) {
                String[] sep = str.split(Pattern.quote("|"));
                JSONObject tempJson = new JSONObject();
                tempJson.put("lat", Double.parseDouble(sep[0]));
                tempJson.put("lon", Double.parseDouble(sep[1]));
                tracksObj.put("p"+trackNum, tempJson);
//                points+="{lat: \""+Double.parseDouble(sep[0])+"\", lon: \""+Double.parseDouble(sep[1])+"\"};";
                trackNum++;
            }
            pointsObj.put("track", tracksObj);

            if (sPref.getBoolean("ACCEL", false)) {

//                points += "[";
                br = new BufferedReader(new InputStreamReader(openFileInput(FILENAMEACCEL)));
                JSONObject accelObj;
                trackNum=0;
                while ((str = br.readLine()) != null) {
                    String[] sep = str.split(Pattern.quote("|"));
                    JSONObject tempJson = new JSONObject();
                    tempJson.put("lat", Double.parseDouble(sep[4]));
                    tempJson.put("lon", Double.parseDouble(sep[5]));
                    tempJson.put("type", Double.parseDouble(sep[6]));
                    tracksObj.put("a"+trackNum, tempJson);
                    trackNum++;
//                    points += "{lat: \"" + Double.parseDouble(sep[4]) + "\", lon: \"" + Double.parseDouble(sep[5]) + "\", type: \"" + Double.parseDouble(sep[6]) + "\"}>";
                }
            }

//            points = points.substring(0,points.length()-1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (Exception e){

        }
//        if (sPref.getBoolean("ACCEL", false)) points+="]]";
//        else points+="]";


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
        String[] values = {tokenSaver.getToken(Maps.this),points, StopTime};
        Request signReq = new Request(headers,values,getString(R.string.url_obr),responseListener,errorListener);
        RequestQueue queue = Volley.newRequestQueue(Maps.this);
        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 10, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        signReq.setRetryPolicy(policy);
        queue.add(signReq);
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
                googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                        R.id.mapView)).getMap();
            }
        } catch (NullPointerException exception){
            Log.e("mapApp", exception.toString());
        }
        point = point.substring(1,point.length()-1);
        try {
            String points[] = point.split(Pattern.quote(";"));
            list=new ArrayList<LatLng>();
            JSONObject jsonResponse = new JSONObject(points[0]);
            LatLng start = new LatLng(jsonResponse.getDouble("lat"),jsonResponse.getDouble("lon"));
            LatLng stop = new LatLng(jsonResponse.getDouble("lat"),jsonResponse.getDouble("lon"));
            for (int i=0; i< points.length-1; i++) {
                jsonResponse = new JSONObject(points[i]);
                list.add(new LatLng(jsonResponse.getDouble("lat"),jsonResponse.getDouble("lon")));
                stop = new LatLng(jsonResponse.getDouble("lat"),jsonResponse.getDouble("lon"));
            }
            polylineOptions = new PolylineOptions()
                    .addAll(list)
                    .color(Color.RED).width(5);
            googleMap.addPolyline(polylineOptions);

            //Обёрнуто для согласования со старыми данными
            try{
                points[points.length-1] = points[points.length-1].substring(1,points[points.length-1].length()-1);
                String warning[] = points[points.length-1].split(Pattern.quote(">"));
                for (int i=0; i< warning.length; i++) {
                    jsonResponse = new JSONObject(warning[i]);
                    //list.add(new LatLng(jsonResponse.getDouble("lat"),jsonResponse.getDouble("lon")));
                    //stop = new LatLng(jsonResponse.getDouble("lat"),jsonResponse.getDouble("lon"));
                    googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(jsonResponse.getDouble("lat"),jsonResponse.getDouble("lon")))
                            .draggable(false)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                            .title(jsonResponse.getString("type"))
                    );
                }
            }
            catch (Exception exp) {}

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
}
