package com.example.pstype_v1;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class Maps extends AppCompatActivity {

    GoogleMap googleMap;
    private boolean mPermissionDenied = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final double TARGET_LATITUDE = 17.893366;
    private static final double TARGET_LONGITUDE = 19.511868;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MyLocationListener myLocationListener = new MyLocationListener(this);
        MyLocationListener.SetUpLocationListener(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //googleMap.setMyLocationEnabled(true);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                createMapView(0, 0, 12);
                addMarker();
            }
        });
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.mapView);
        //mapFragment.getMapAsync(this);
        //onMapReady(googleMap);
        double mylat = 55.7531432;
        double mylon = 37.6198181;
        int zoom = 12;
        if (MyLocationListener.imHere!=null)
        {
            mylat=MyLocationListener.imHere.getLatitude();
            mylon=MyLocationListener.imHere.getLongitude();
            zoom=17;
        }
        createMapView(mylat, mylon, zoom);
    }

    private void addMarker(){

        /** Make sure that the map has been initialised **/
        if(null != googleMap){
            googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(0, 0))
                    .title("Marker")
                    .draggable(true)
            );
        }
    }

    private void createMapView(double lat, double lon, int zoom){
        /**
         * Catch the null pointer exception that
         * may be thrown when initialising the map
         */

        try {
            if(null == googleMap){
                googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                        R.id.mapView)).getMap();
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(lat, lon))
                        .zoom(zoom)
                        .bearing(0)
                        .tilt(0)
                        .build();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                googleMap.animateCamera(cameraUpdate);
                /**
                 * If the map is still null after attempted initialisation,
                 * show an error to the user
                 */
                if(null == googleMap) {
                    Toast.makeText(getApplicationContext(),
                            "Error creating map",Toast.LENGTH_SHORT).show();
                }
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
}
