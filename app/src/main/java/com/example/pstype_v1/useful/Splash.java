package com.example.pstype_v1.useful;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.pstype_v1.R;
import com.example.pstype_v1.main.general;
import com.example.pstype_v1.main.sign;
import com.example.pstype_v1.main.welcome;


public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (tokenSaver.getFIRST(Splash.this).isEmpty()) {
            Intent intent = new Intent(Splash.this, welcome.class);
            tokenSaver.setFIRST(Splash.this);
            Splash.this.startActivity(intent);
            finish();
        }
        else {
            if (tokenSaver.getToken(Splash.this).isEmpty()) {
                Intent intent = new Intent(Splash.this, sign.class);
                Splash.this.startActivity(intent);
                finish();
            } else {
                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Intent intent = new Intent(Splash.this, general.class);
                        Splash.this.startActivity(intent);
                        finish();
                    }
                };
                Response.ErrorListener errorListener = new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        tokenSaver.clearToken(Splash.this);
                        Intent intent = new Intent(Splash.this, sign.class);
                        Splash.this.startActivity(intent);
                        finish();
                    }
                };
                String[] headers = {"token"};
                String[] values = {tokenSaver.getToken(Splash.this)};
                Request info = new Request(headers, values, getString(R.string.url_data), responseListener, errorListener);
                RequestQueue queue = Volley.newRequestQueue(Splash.this);
                queue.add(info);
            }
        }


//        Intent intent = new Intent(this, general.class);
//        startActivity(intent);
//        finish();
    }
}
