package com.example.pstype_v1.testing;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.pstype_v1.R;
import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

public class accel_test extends AppCompatActivity {
    SensorManager sensorManager;
    Sensor Accelerometer;
    TextView info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accel_test);
        info = (TextView) findViewById(R.id.textView36);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(listener, Accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            double x, y, z;
            x = event.values[0];
            y = event.values[1];
            z = event.values[2];
            String temp = "X: "+x+"\nY: "+y+"\nZ: "+z;
            info.setText(temp);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
}
