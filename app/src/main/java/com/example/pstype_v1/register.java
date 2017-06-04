package com.example.pstype_v1;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;

public class register extends AppCompatActivity {

    //private EditText name;
    //private EditText password;
    //private EditText age;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();
    Callback callback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        final EditText etusername=(EditText)findViewById(R.id.editText3);
        final EditText etpassword=(EditText)findViewById(R.id.editText4);
        final EditText etpassword2=(EditText)findViewById(R.id.editText7);
        final EditText etage=(EditText)findViewById(R.id.editText5);
        final Button buttonReg = (Button) findViewById(R.id.button4);
        final RadioGroup buttonSex = (RadioGroup)findViewById(R.id.RadioGroup);
        final Boolean[] sex = {true};

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

        buttonReg.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String username = etusername.getText().toString();
                String password = etpassword.getText().toString();
                String password2=etpassword2.getText().toString();

                if (!password.equals(password2))
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(register.this);
                    builder.setMessage("Введённые пароли не совпадают")
                            .setNegativeButton("Повторить", null)
                            .create()
                            .show();
                    return;
                }
                String age = etage.getText().toString();
                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String success = jsonResponse.getString("status");
                            if (success.equals("ok")) {
                                Intent intent = new Intent(register.this, sign.class);
                                register.this.startActivity(intent);
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
                        AlertDialog.Builder builder = new AlertDialog.Builder(register.this);
                        builder.setMessage("Ошибка регистрации. Данные некорректны или имя пользователя уже занято.")
                                .setNegativeButton("Повторить", null)
                                .create()
                                .show();

                    }
                };
                RegReq regReq = new RegReq(username, password, age, sex[0], responseListener, errorListener);
                RequestQueue queue = Volley.newRequestQueue(register.this);
                queue.add(regReq);
            }
        });

    }


}
