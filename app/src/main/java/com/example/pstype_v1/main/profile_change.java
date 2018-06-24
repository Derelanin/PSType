package com.example.pstype_v1.main;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.pstype_v1.R;
import com.example.pstype_v1.useful.Request;
import com.example.pstype_v1.useful.tokenSaver;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKSdkListener;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.dialogs.VKCaptchaDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Pattern;

public class profile_change extends AppCompatActivity {
    int dialog_date=1;
    Calendar calendar = Calendar.getInstance();
    int myYear = calendar.get(Calendar.YEAR);
    int myMonth = calendar.get(Calendar.MONTH);
    int myDay = calendar.get(Calendar.DAY_OF_MONTH);
    String date = myDay+"-"+(myMonth+1)+"-"+myYear;
    String americanDate;
    EditText datePick;
    int[] dbId;
    ArrayList<String> dbName;
    int[] dbId2;
    ArrayList<String> dbName2;
    ArrayAdapter<String> adapter, adapter2;
    private String appId = "6102430";
    private static String[] vkScope = new String[]{ };
    public static String vkTokenKey = "VK_ACCESS_TOKEN";
    private final VKSdkListener sdkListener = new profile_change.VKSdkListener() {
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
        }
    };
    private VKRequest currentRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_change);

        VKUIHelper.onCreate(profile_change.this);
        if (VKSdk.getAccessToken() == null){
            HashMap<String, String> why = new HashMap<>();
            why.put("access_token", "07e1eadf07e1eadf07e1eadff007bcf741007e107e1eadf5ebd0b1a4d40a21f56977d15");
            why.put("expires_in", "86400");
            why.put("user_id", ".");
            why.put("secret", ".");
            why.put("email", ".");
            VKSdk.initialize(sdkListener, appId, VKAccessToken.tokenFromParameters(why));
        }
        else{
            VKSdk.initialize(sdkListener, appId, VKAccessToken.tokenFromSharedPreferences(profile_change.this, vkTokenKey));
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Редактирование");

        final RadioGroup buttonSex = (RadioGroup)findViewById(R.id.RadioGroup);
        final RadioButton male = (RadioButton)findViewById(R.id.radioButton);
        final RadioButton female = (RadioButton)findViewById(R.id.radioButton2);
        final RadioButton not = (RadioButton)findViewById(R.id.radioButton3);
        final EditText country = (EditText)findViewById(R.id.country);
        final EditText city = (EditText)findViewById(R.id.city);
        final EditText age = (EditText)findViewById(R.id.age);
        final int[] sex = {0};
        final EditText excperience = (EditText)findViewById(R.id.experience);
        final Spinner database = (Spinner)findViewById(R.id.db);
        final Spinner driverExp = (Spinner)findViewById(R.id.exp);

        ArrayAdapter<?> adapterExp = ArrayAdapter.createFromResource(this, R.array.exp, android.R.layout.simple_spinner_item);
        adapterExp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        driverExp.setAdapter(adapterExp);
        excperience.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                driverExp.performClick();
                driverExp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        excperience.setText(driverExp.getSelectedItem().toString());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
                if (driverExp.getSelectedItemPosition()==0)
                    excperience.setText(driverExp.getSelectedItem().toString());
            }
        });

        getDB();
       Intent intent = getIntent();
        if (intent.getStringExtra("exp")!=null) excperience.setText(intent.getStringExtra("exp"));
        if (intent.getStringExtra("city")!=null) city.setText(intent.getStringExtra("city"));
        country.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database.setAdapter(adapter);
                database.performClick();
                database.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (id!=0) {
                            getCities(dbId[position]);
                            country.setText(database.getSelectedItem().toString());
                            city.setVisibility(View.VISIBLE);
                        }
                        else{
                            country.setText("");
                            city.setText("");
                            city.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        country.setText("");
                    }
                });
            }
        });
        city.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database.setAdapter(adapter2);
                database.performClick();
                database.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (id!=0)
                            city.setText(database.getSelectedItem().toString());
                        else
                            city.setText("");
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        city.setText("");
                    }
                });
            }
        });

        datePick = (EditText)findViewById(R.id.age);

        String[] bdata = (intent.getStringExtra("age")).split(Pattern.quote("-"));
        Calendar calendar = Calendar.getInstance();
        myDay=Integer.parseInt(bdata[2].substring(0,2));
        myMonth=Integer.parseInt(bdata[1])-1;
        myYear=Integer.parseInt(bdata[0]);
        americanDate = bdata[0]+"-"+bdata[1]+"-"+bdata[2];
        date = bdata[2].substring(0,2)+"-"+bdata[1]+"-"+bdata[0];
        datePick.setText(bdata[2].substring(0,2)+"-"+bdata[1]+"-"+bdata[0]);

        datePick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(dialog_date);
            }
        });


        sex[0]=Integer.parseInt(intent.getStringExtra("sex"));
        if (sex[0]==1){
            male.setChecked(true);
        }
        else if (sex[0]==2){
            female.setChecked(true);
        }
        else{
            not.setChecked(true);
        }

        Button cancel = (Button)findViewById(R.id.but_canc);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(profile_change.this, profile.class));
                finish();
            }
        });
        buttonSex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId){
                    case R.id.radioButton:
                        sex[0]=1;
                        break;
                    case R.id.radioButton2:
                        sex[0]=2;
                        break;
                    case R.id.radioButton3:
                        sex[0] =0;
                        break;
                }
            }
        });
        Button accept = (Button)findViewById(R.id.but_ok);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar4);
                progressBar.setVisibility(ProgressBar.VISIBLE);


                String age = datePick.getText().toString();
                if (Age()<14) {
                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                    AlertDialog.Builder builder = new AlertDialog.Builder(profile_change.this);
                    builder.setMessage("Возраст должен быть больше либо равен 14")
                            .setNegativeButton("Повторить", null)
                            .create()
                            .show();
                    return;
                }
                if (Age()>110) {
                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                    AlertDialog.Builder builder = new AlertDialog.Builder(profile_change.this);
                    builder.setMessage("Возраст должен быть меньше либо равен 110")
                            .setNegativeButton("Повторить", null)
                            .create()
                            .show();
                    return;
                }

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
                String[] headers = {"token","age", "sex"};
                String[] values = {tokenSaver.getToken(profile_change.this), americanDate, String.valueOf(sex[0])};
                Request inf = new Request(headers,values,getString(R.string.url_change),responseListener,errorListener);
                RequestQueue queue = Volley.newRequestQueue(profile_change.this);
                int socketTimeout = 30000;//30 seconds - change to what you want
                RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                inf.setRetryPolicy(policy);
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
                //startActivity(new Intent(this, profile.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected Dialog onCreateDialog(int id) {
        if (id == dialog_date) {
            DatePickerDialog tpd = new DatePickerDialog(this, myCallBack, myYear, myMonth, myDay);
            return tpd;
        }
        return super.onCreateDialog(id);
    }

    DatePickerDialog.OnDateSetListener myCallBack = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            myYear = year;
            myMonth = monthOfYear+1;
            myDay = dayOfMonth;
            String day = myDay+"";
            String month = myMonth+"";
            if (day.length()==1)
                day="0"+day;
            if(month.length()==1)
                month="0"+month;
            date=day+"-"+month+"-"+myYear;
            americanDate = month+"-"+day+"-"+myYear;
            datePick.setText(date);
            datePick.setHint(getString(R.string.age));
        }
    };

    int Age (){
        int age;
        String[] bdata = date.split(Pattern.quote("-"));
        Calendar calendar = Calendar.getInstance();
        int byear=Integer.parseInt(bdata[2]);
        int year=calendar.get(Calendar.YEAR);
        age=year-byear;
        int month= calendar.get(Calendar.MONTH)+1;
        int bmonth=Integer.parseInt(bdata[1]);
        if (bmonth>month)
            age--;
        if (bmonth==month){
            int bday=Integer.parseInt(bdata[0]);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            if (bday>day)
                age--;
        }
        return age;
    }

    private abstract class VKSdkListener extends com.vk.sdk.VKSdkListener {
        public abstract void onCaptchaError(VKError captchaError);

        public abstract void onTokenExpired(VKAccessToken expiredToken);

        public abstract void onAccessDenied(VKError authorizationError);

        public abstract void onReceiveNewToken(VKAccessToken newToken);
    }
    private void getDB(){
        if (currentRequest != null) {
            currentRequest.cancel();
        }
        currentRequest = new VKRequest("database.getCountries", VKParameters.from("need_all", "1","count","1000"));
        currentRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                Log.d("VkDemoApp", "onComplete " + response);
                JSONObject jsonResponse=response.json;
                try {
                    JSONObject responceCountry = jsonResponse.getJSONObject("response");
                    int count = responceCountry.getInt("count");
                    JSONArray items = responceCountry.getJSONArray("items");
                    dbId = new int[count+1];
                    dbId[0]=0;
                    dbName = new ArrayList<>();
                    dbName.add("(не указано)");
                    for (int i=1; i<=count; i++){
                        dbId[i]=(items.getJSONObject(i-1)).getInt("id");
                        dbName.add(items.getJSONObject(i-1).getString("title"));
                    }
                    adapter = new ArrayAdapter<String>(profile_change.this, android.R.layout.simple_spinner_item, dbName);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    Intent intent = getIntent();
                    if (intent.getStringExtra("country")!=null) {
                        EditText country = (EditText)findViewById(R.id.country);
                        country.setText(intent.getStringExtra("country"));
                        int num = dbName.indexOf(intent.getStringExtra("country"));
                        getCities(dbId[num]);
                        EditText city = (EditText)findViewById(R.id.city);
                        city.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                super.attemptFailed(request, attemptNumber, totalAttempts);
                Log.d("VkDemoApp", "attemptFailed " + request + " " + attemptNumber + " " + totalAttempts);
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                Log.d("Error", "onError " + error);
            }

            @Override
            public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded, long bytesTotal) {
                super.onProgress(progressType, bytesLoaded, bytesTotal);
                Log.d("VkDemoApp", "onProgress " + progressType + " " + bytesLoaded + " " + bytesTotal);
            }
        });
    }

    private void getCities(int cityId){
        if (currentRequest != null) {
            currentRequest.cancel();
        }
        currentRequest = new VKRequest("database.getCities", VKParameters.from("need_all", "0","count","1000","country_id", String.valueOf(cityId)));
        currentRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                Log.d("VkDemoApp", "onComplete " + response);
                JSONObject jsonResponse=response.json;
                try {
                    JSONObject responceCountry = jsonResponse.getJSONObject("response");
                    int count = responceCountry.getInt("count");
                    JSONArray items = responceCountry.getJSONArray("items");
                    dbId2=new int[count+1];
                    dbId2[0]=0;
                    dbName2 = new ArrayList<>();
                    dbName2.add("(не указано)");
                    for (int i=1; i<=count; i++){
                        dbId2[i]=(items.getJSONObject(i-1)).getInt("id");
                        dbName2.add((items.getJSONObject(i-1)).getString("title"));
                    }
                    adapter2 = new ArrayAdapter<String>(profile_change.this, android.R.layout.simple_spinner_dropdown_item, dbName2);
                    adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    adapter2.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                super.attemptFailed(request, attemptNumber, totalAttempts);
                Log.d("VkDemoApp", "attemptFailed " + request + " " + attemptNumber + " " + totalAttempts);
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
            }

            @Override
            public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded, long bytesTotal) {
                super.onProgress(progressType, bytesLoaded, bytesTotal);
                Log.d("VkDemoApp", "onProgress " + progressType + " " + bytesLoaded + " " + bytesTotal);
            }
        });
    }
}
