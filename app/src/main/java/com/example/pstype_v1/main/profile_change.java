package com.example.pstype_v1.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.pstype_v1.R;
import com.example.pstype_v1.signin.sign;
import com.example.pstype_v1.useful.getInfo;
import com.example.pstype_v1.useful.pr_change;
import com.example.pstype_v1.useful.tokenSaver;

import org.json.JSONException;
import org.json.JSONObject;

public class profile_change extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_change);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Редактирование");

        final RadioGroup buttonSex = (RadioGroup)findViewById(R.id.RadioGroup);
        final RadioButton male = (RadioButton)findViewById(R.id.radioButton);
        final RadioButton female = (RadioButton)findViewById(R.id.radioButton2);
        final EditText age = (EditText)findViewById(R.id.age);
        final Boolean[] sex = {true};

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    String success = jsonResponse.getString("status");

                    if (success.equals("ok")) {
                        age.setText(jsonResponse.getString("age"));
                        if (jsonResponse.getString("sex").equals("true")){
                            male.setChecked(true);
                        }
                        else{
                            female.setChecked(true);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        Response.ErrorListener errorListener= new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                tokenSaver.clearToken(profile_change.this);
                Intent intent = new Intent(profile_change.this, sign.class);
                profile_change.this.startActivity(intent);
                finish();
            }
        };
        getInfo info = new getInfo(tokenSaver.getToken(profile_change.this), responseListener, errorListener);
        RequestQueue queue = Volley.newRequestQueue(profile_change.this);
        queue.add(info);

        Button cancel = (Button)findViewById(R.id.but_canc);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(profile_change.this, profile.class));
                finish();
            }
        });

        Button accept = (Button)findViewById(R.id.but_ok);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar4);
                progressBar.setVisibility(ProgressBar.VISIBLE);
                buttonSex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                        switch (checkedId){
                            case R.id.radioButton:
                                sex[0]=true;
                                break;
                            case R.id.radioButton2:
                                sex[0]=false;
                                break;
                            default:
                                sex[0] =true;
                                break;
                        }
                    }
                });

                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String success = jsonResponse.getString("status");

                            if (success.equals("ok")) {
                                Toast.makeText(getApplicationContext(), "Ваши изменения сохранены", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(ProgressBar.INVISIBLE);
                                Intent intent = new Intent(profile_change.this, profile.class);
                                profile_change.this.startActivity(intent);
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
                        Toast.makeText(getApplicationContext(), "Ошибка при изменении данных", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                        Intent intent = new Intent(profile_change.this, profile.class);
                        profile_change.this.startActivity(intent);
                        finish();
                    }
                };
                pr_change inf = new pr_change(tokenSaver.getToken(profile_change.this),age.getText().toString(),sex[0], responseListener, errorListener);
                RequestQueue queue = Volley.newRequestQueue(profile_change.this);
                queue.add(inf);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_change, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(new Intent(this, profile.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
