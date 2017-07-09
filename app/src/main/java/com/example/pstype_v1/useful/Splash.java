package com.example.pstype_v1.useful;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.pstype_v1.R;
import com.example.pstype_v1.main.general;


public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Intent intent = new Intent(this, general.class);
        startActivity(intent);
        finish();
    }
}
