package com.example.pstype_v1.main;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.pstype_v1.R;
import com.example.pstype_v1.useful.Functions;
import com.example.pstype_v1.useful.Request;
import com.example.pstype_v1.useful.tokenSaver;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUser;
import com.vk.sdk.api.model.VKList;
import com.vk.sdk.dialogs.VKCaptchaDialog;
import com.vk.sdk.util.VKUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.regex.Pattern;

public class sign extends AppCompatActivity {
    private String appId = "6102430";
    public static String vkTokenKey = "VK_ACCESS_TOKEN";
    private static int flag = 1;
    private static String[] vkScope = new String[]{ };
    private final VKSdkListener sdkListener = new VKSdkListener() {
        @Override
        public void onAcceptUserToken(VKAccessToken token) {
            Log.d("VkDemoApp", "onAcceptUserToken " + token);
            //startLoading();
        }
        @Override
        public void onReceiveNewToken(VKAccessToken newToken) {
            Log.d("VkDemoApp", "onReceiveNewToken " + newToken);
            newToken.saveTokenToSharedPreferences(getApplicationContext(), vkTokenKey);
        }
        @Override
        public void onRenewAccessToken(VKAccessToken token) {
            Log.d("VkDemoApp", "onRenewAccessToken " + token);
            //startLoading();
        }
        @Override
        public void onCaptchaError(VKError captchaError) {
            Log.d("VkDemoApp", "onCaptchaError " + captchaError);
            new VKCaptchaDialog(captchaError).show();
        }
        @Override
        public void onTokenExpired(VKAccessToken expiredToken) {
            Log.d("VkDemoApp", "onTokenExpired " + expiredToken);
            VKSdk.authorize(vkScope, true, false);
        }
        @Override
        public void onAccessDenied(VKError authorizationError) {
            Log.d("VkDemoApp", "onAccessDenied " + authorizationError);
//            new AlertDialog.Builder(sign.this)
//                    .setMessage(authorizationError.errorMessage)
//                    .show();
        }
    };
    private VKRequest currentRequest, currentRequest2;

    String VkCountry, VkCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);
        VKUIHelper.onCreate(sign.this);
        VKSdk.initialize(sdkListener, appId, VKAccessToken.tokenFromSharedPreferences(sign.this, vkTokenKey));
        final Functions fun = new Functions(this);
        tokenSaver.clearToken(this);
        String[] fingerprints = VKUtil.getCertificateFingerprint(this, this.getPackageName());


        requestMultiplePermissions();



        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        final EditText etUsername = (EditText) findViewById(R.id.editText);
        final EditText etPassword = (EditText) findViewById(R.id.editText2);
        final Button sign = (Button) findViewById(R.id.button);
        final Button reg = (Button) findViewById(R.id.Reg_but);
        final Button auth_vk = (Button) findViewById(R.id.auth_vk);
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
                    eye.setImageResource(R.drawable.visibility);
                    etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    eyeAv[0] =false;
                }
                else {
                    eye.setImageResource(R.drawable.visibility_off);
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
        if (!VKSdk.wakeUpSession()){
            auth_vk.setText("Регистрация через вк");
        }
        auth_vk.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                findViewById(R.id.progressBar).setVisibility(ProgressBar.VISIBLE);
                if (VKSdk.wakeUpSession()) {
                    startLoading();
                    auth_vk.setText("Войти через вк");
                } else {
                    VKSdk.authorize(vkScope, true, false);
                    progressBar.setVisibility(View.INVISIBLE);
                    auth_vk.setText("Войти через вк");
                }
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
                        AlertDialog.Builder builder = new AlertDialog.Builder(sign.this);
                        if(response != null && response.data != null){

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
                        if(response==null){
                            builder.setMessage("Отсутствует подключение к интернету")
                                    .setNegativeButton("Повторить", null)
                                    .create()
                                    .show();
                        }
                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                    }
                };
                progressBar.setVisibility(ProgressBar.VISIBLE);
                String[] headers = {"username","password"};
                String[] values = {username,password};
                Request signReq = new Request(headers,values,getString(R.string.url_signin),responseListener,errorListener);
                RequestQueue queue = Volley.newRequestQueue(sign.this);
                int socketTimeout = 30000;//30 seconds - change to what you want
                RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                signReq.setRetryPolicy(policy);
                queue.add(signReq);
            }
        });
    }

    private abstract class VKSdkListener extends com.vk.sdk.VKSdkListener {
        public abstract void onCaptchaError(VKError captchaError);

        public abstract void onTokenExpired(VKAccessToken expiredToken);

        public abstract void onAccessDenied(VKError authorizationError);

        public abstract void onReceiveNewToken(VKAccessToken newToken);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        VKUIHelper.onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VKUIHelper.onDestroy(this);
        if (currentRequest != null) {
            currentRequest.cancel();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        VKUIHelper.onResume(this);
    }

    private void startLoading() {

        if (currentRequest != null) {
            currentRequest.cancel();
        }
//        if ((currentRequest == null)&&(flag<3) ) {
//            return;
//        }
        currentRequest = VKApi.users().get(VKParameters.from(VKApiConst.FIELDS, "sex,bdate,first_name,last_name,photo_200,id, city, country"));
        currentRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                Log.d("VkDemoApp", "onComplete " + response);
                final VKApiUser user = ((VKList<VKApiUser>) response.parsedModel).get(0);
                String bdate="";
                int sex1=0;
                try {
                    JSONObject jsonResponse=response.json;
                    String res = jsonResponse.getString("response");
                    res=res.substring(1,res.length());
                    JSONObject jsonVK=new JSONObject(res);
                    bdate = jsonVK.getString("bdate");
                    sex1=jsonVK.getInt("sex");
                    if (sex1==1) sex1=2;
                    else if (sex1==2) sex1=1;
                    VkCity = jsonVK.getString("city");
                    VkCountry = jsonVK.getString("country");
                    jsonVK=new JSONObject(VkCity);
                    VkCity = jsonVK.getString("title");
                    jsonVK=new JSONObject(VkCountry);
                    VkCountry = jsonVK.getString("title");
                    sex1 = Integer.parseInt(jsonVK.getString("sex"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                final String username= user.first_name +" "+ user.last_name;
                final String id="id"+user.id;
                final String photo=user.photo_200;

                String americanDate = "";
                int age;
                if (bdate.length()<6){
                    age=0;
                }
                else{
                    String[] bdata = bdate.split(Pattern.quote("."));
                    Calendar calendar = Calendar.getInstance();
                    int byear=Integer.parseInt(bdata[2]);
                    int year=calendar.get(Calendar.YEAR);
                    int month= calendar.get(Calendar.MONTH);
                    int bmonth=Integer.parseInt(bdata[1])+1;
                    age=year-byear;
                    if (bmonth>month)
                        age--;
                    if (bmonth==month){
                        int bday=Integer.parseInt(bdata[0]);
                        int day = calendar.get(Calendar.DAY_OF_MONTH);
                        if (bday>day)
                            age--;
                    }
                    if (age<14 || age>100) age=0;
                    americanDate = bdata[0]+"-"+bdata[1]+"-"+bdata[2];
                }

                final int Age = age;
                Response.Listener<String> responseListener2 = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject jsonResponse = null;
                        try {
                            jsonResponse = new JSONObject(response);
                            String success = jsonResponse.getString("status");
                            final String token = jsonResponse.getString("token");
                            if (success.equals("ok")) {
                                Toast.makeText(getApplicationContext(), "Пользователь добавлен", Toast.LENGTH_SHORT).show();

                                AlertDialog.Builder builder = new AlertDialog.Builder(sign.this);
                                builder.setMessage("Для завершения регистрации необходимо дополнить информацию о себе")
                                        .setNegativeButton("ОК",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog,
                                                                        int id) {
                                                        tokenSaver.clearToken(sign.this);
                                                        Intent intent = new Intent(sign.this, addSignup.class);
                                                        intent.putExtra("age", Age);
                                                        intent.putExtra("token", token);
                                                        sign.this.startActivity(intent);
                                                    }
                                                })
                                        .create()
                                        .show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                Response.ErrorListener errorListener2= new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        findViewById(R.id.progressBar).setVisibility(ProgressBar.INVISIBLE);
                        NetworkResponse response = error.networkResponse;
                        if (response==null){
                            AlertDialog.Builder builder = new AlertDialog.Builder(sign.this);
                            builder.setMessage("Отсутствует подключение к интернету")
                                    .setNegativeButton("Повторить", null)
                                    .create()
                                    .show();
                        }
                        Response.Listener<String> responseListener3 = new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonResponse = new JSONObject(response);
                                    String success = jsonResponse.getString("status");
                                    if (success.equals("ok")) {
                                        tokenSaver.setName(sign.this, username);
                                        tokenSaver.setURL(sign.this, photo);
                                        tokenSaver.setToken(sign.this,jsonResponse.getString("token"));


                                        Intent genIntent = new Intent(sign.this, general.class);
                                        sign.this.startActivity(genIntent);
                                        findViewById(R.id.progressBar).setVisibility(ProgressBar.INVISIBLE);
                                        finish();
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        Response.ErrorListener errorListener3= new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                NetworkResponse response = error.networkResponse;
                                findViewById(R.id.progressBar).setVisibility(ProgressBar.INVISIBLE);
                                if(response==null){
                                    AlertDialog.Builder builder3 = new AlertDialog.Builder(sign.this);
                                    builder3.setMessage("Отсутствует подключение к интернету")
                                            .setNegativeButton("Повторить", null)
                                            .create()
                                            .show();
                                }
                            }
                        };

                        String[] headers2 = {"username", "password"};
                        String[] values2 = {id, id};
                        Request vksignReq = new Request(headers2,values2,getString(R.string.url_signin),responseListener3,errorListener3);
                        RequestQueue queue3 = Volley.newRequestQueue(sign.this);
                        int socketTimeout = 30000;//30 seconds - change to what you want
                        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                        vksignReq.setRetryPolicy(policy);
                        queue3.add(vksignReq);
                    }
                };

                bdate.replace(Pattern.quote("."), "-");
                String[] headers, values;
                if (Age==0) {
                    headers = new String[]{"username", "password", "sex", "country", "city", "name"};
                    values = new String[] {id, id, String.valueOf(sex1), VkCountry, VkCity, username};
                }
                else
                {
                    headers =  new String[]{"username", "password", "sex", "age", "country", "city", "name"};
                    values =  new String[]{id, id, String.valueOf(sex1), americanDate, VkCountry, VkCity, username};
                }
                Request VKregReq = new Request(headers,values,getString(R.string.url_signup),responseListener2,errorListener2);
                RequestQueue queue2 = Volley.newRequestQueue(sign.this);
                int socketTimeout = 30000;//30 seconds - change to what you want
                RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                VKregReq.setRetryPolicy(policy);
                queue2.add(VKregReq);


            }

            @Override
            public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                super.attemptFailed(request, attemptNumber, totalAttempts);
                AlertDialog.Builder builder = new AlertDialog.Builder(sign.this);
                builder.setMessage("attemptFailed")
                        .setNegativeButton("Повторить", null)
                        .create()
                        .show();
                Log.d("VkDemoApp", "attemptFailed " + request + " " + attemptNumber + " " + totalAttempts);
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                findViewById(R.id.progressBar).setVisibility(ProgressBar.INVISIBLE);
                if (flag == 1) {
                    flag++;
                    startLoading();
                }
            }

            @Override
            public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded, long bytesTotal) {
                super.onProgress(progressType, bytesLoaded, bytesTotal);
                Log.d("VkDemoApp", "onProgress " + progressType + " " + bytesLoaded + " " + bytesTotal);
            }
        });

    }
    public void requestMultiplePermissions() {
        ActivityCompat.requestPermissions(this,
                new String[] {
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                10001);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == 10001) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
