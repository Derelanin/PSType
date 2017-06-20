package com.example.pstype_v1;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;


public class sign extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);
        final Functions fun = new Functions(this);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        final EditText etUsername = (EditText) findViewById(R.id.editText);
        final EditText etPassword = (EditText) findViewById(R.id.editText2);
        final Button sign = (Button) findViewById(R.id.button);
        final Button reg = (Button) findViewById(R.id.Reg_but);
        final ImageButton eye = (ImageButton) findViewById(R.id.view_but);
        final boolean[] eyeAv = {false};

        etPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    fun.hideKeyboard(v);
                }
            }
        });

        eye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (eyeAv[0]) {
                    eye.setImageResource(R.drawable.ic_visibility_black_24dp);
                    etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    eyeAv[0] =false;
                }
                else {
                    eye.setImageResource(R.drawable.ic_visibility_off_black_24dp);
                    etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    eyeAv[0]=true;
                }

            }
        });
        reg.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(sign.this, register.class);
                sign.this.startActivity(registerIntent);
            }
        });

        sign.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                final String username = etUsername.getText().toString();
                final String password = etPassword.getText().toString();
                            Response.Listener<String> responseListener = new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String success = jsonResponse.getString("status");

                            if (success.equals("ok")) {
                                String token = jsonResponse.getString("token");
                                tokenSaver.setToken(sign.this,token);
                                tokenSaver.setName(sign.this,username);
                                Intent intent = new Intent(sign.this, general.class);
                                progressBar.setVisibility(ProgressBar.INVISIBLE);
                                sign.this.startActivity(intent);
                                finish();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                Response.ErrorListener errorListener= new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse response = error.networkResponse;
                        if(response != null && response.data != null){
                            AlertDialog.Builder builder = new AlertDialog.Builder(sign.this);
                            switch(response.statusCode){
                                case 404:
                                    builder.setMessage("Пользователь с данным именем не найден")
                                            .setNegativeButton("Повторить", null)
                                            .create()
                                            .show();
                                    break;
                                case 400:
                                    builder.setMessage("Введён неправильный пароль")
                                            .setNegativeButton("Повторить", null)
                                            .create()
                                            .show();
                                    break;
                                case 503:
                                builder.setMessage("Database error")
                                        .setNegativeButton("Повторить", null)
                                        .create()
                                        .show();
                                break;
                            }
                        }
                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                    }
                };
                progressBar.setVisibility(ProgressBar.VISIBLE);
                SignReq signReq = new SignReq(username, password, responseListener, errorListener);
                RequestQueue queue = Volley.newRequestQueue(sign.this);
                queue.add(signReq);
            }
        });
    }
}
