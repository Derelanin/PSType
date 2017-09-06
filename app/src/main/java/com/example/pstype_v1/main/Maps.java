package com.example.pstype_v1.main;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.pstype_v1.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class Maps extends AppCompatActivity {
    GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        createMapView();

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

    private void createMapView() {
        try {
            if (null == googleMap) {
                googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                        R.id.mapView)).getMap();
                CameraPosition cameraPosition;
                CameraUpdate cameraUpdate;
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setZoomControlsEnabled(true);

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
}
