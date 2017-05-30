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
import android.widget.TextView;

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

        final EditText etUsername = (EditText) findViewById(R.id.editText);
        final EditText etPassword = (EditText) findViewById(R.id.editText2);
        final TextView err = (TextView) findViewById(R.id.textView2);
        final Button sign = (Button) findViewById(R.id.button);
        final Button reg = (Button) findViewById(R.id.button2);
        final Button eye = (Button) findViewById(R.id.button6);
        final boolean[] eyeAv = {false};

        eye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (eyeAv[0]) {
                    etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    eyeAv[0] =false;
                }
                else {
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
                                //int age = jsonResponse.getInt("age");
                                String token = jsonResponse.getString("token");
                                tokenSaver.setToken(sign.this,token);
                                tokenSaver.setName(sign.this,username);
                                Intent intent = new Intent(sign.this, general.class);
                                //intent.putExtra("username", username);
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
                         AlertDialog.Builder builder = new AlertDialog.Builder(sign.this);
                                builder.setMessage("Ошибка входа. Проверьте правильность введённых вами данных.")
                                        .setNegativeButton("Повторить", null)
                                        .create()
                                        .show();

                    }
                };
                SignReq signReq = new SignReq(username, password, responseListener, errorListener);
                RequestQueue queue = Volley.newRequestQueue(sign.this);
                queue.add(signReq);
            }
        });

    }
}
