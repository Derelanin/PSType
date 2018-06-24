package com.example.pstype_v1.main;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;

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
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.dialogs.VKCaptchaDialog;
import com.vk.sdk.util.VKUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class register extends AppCompatActivity {
    int dialog_date=1;
    Calendar calendar = Calendar.getInstance();
    int myYear = calendar.get(Calendar.YEAR);
    int myMonth = calendar.get(Calendar.MONTH);
    int myDay = calendar.get(Calendar.DAY_OF_MONTH);
    String date = myDay+"-"+(myMonth+1)+"-"+myYear;
    String americanDate;
    EditText datePick;
    int[] dbId;
    String[] dbName;
    int[] dbId2;
    String[] dbName2;
    ArrayAdapter<String> adapter, adapter2;
    String[] exp = { "Отсутствует", "Менее 6 месяцев", "От 6 месяцев до 1 года", "1-3 года",
            "3-5 лет", "5-10 лет", "10-15 лет", "15-20 лет", "Более 20 лет" };

    private String appId = "6102430";
    private static String[] vkScope = new String[]{ };
    public static String vkTokenKey = "VK_ACCESS_TOKEN";
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
        }
    };
    private VKRequest currentRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        VKUIHelper.onCreate(register.this);
        HashMap<String, String> why = new HashMap<>();
        why.put("access_token", "07e1eadf07e1eadf07e1eadff007bcf741007e107e1eadf5ebd0b1a4d40a21f56977d15");
        why.put("expires_in", "86400");
        why.put("user_id", ".");
        why.put("secret", ".");
        why.put("email", ".");
        VKSdk.initialize(sdkListener, appId, VKAccessToken.tokenFromParameters(why));
        VKAccessToken hh = VKSdk.getAccessToken();

        final Functions func = new Functions(register.this);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        final EditText etusername=(EditText)findViewById(R.id.editText3);
        final EditText etpassword=(EditText)findViewById(R.id.editText4);
        final EditText etpassword2=(EditText)findViewById(R.id.editText7);
        final Button buttonReg = (Button) findViewById(R.id.button4);
        final RadioGroup buttonSex = (RadioGroup)findViewById(R.id.RadioGroup);
        final int[] sex = {0};
        final Spinner driverExp = (Spinner)findViewById(R.id.spinner);
        final EditText drExp = (EditText)findViewById(R.id.editText6);
        final EditText country = (EditText)findViewById(R.id.editText8);
        final EditText city = (EditText)findViewById(R.id.editText9);
        final TextInputLayout hideCity = (TextInputLayout)findViewById(R.id.textInputLayout8);
        final Spinner database = (Spinner)findViewById(R.id.databases);

        //String[] fingerprints = VKUtil.getCertificateFingerprint(this, this.getPackageName());

        ArrayAdapter<String> adapterExp = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, exp);
        adapterExp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        driverExp.setAdapter(adapterExp);
        driverExp.setSelection(1);
        drExp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(register.this);
                driverExp.performClick();
                driverExp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        drExp.setText(driverExp.getSelectedItem().toString());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
                if (driverExp.getSelectedItemPosition()==0)
                    drExp.setText(driverExp.getSelectedItem().toString());
            }
        });

        etusername.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!etusername.isFocused())
                    hideKeyboard(register.this);
            }
        });

        datePick = (EditText)findViewById(R.id.editText5);
        datePick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(dialog_date);
                datePick.clearFocus();
                hideKeyboard(register.this);
            }
        });

        getDB();
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
                            city.setText("");
                            country.setText(database.getSelectedItem().toString());
                            hideCity.setVisibility(View.VISIBLE);
                        }
                        else{
                            country.setText("");
                            hideCity.setVisibility(View.GONE);
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
                //city.clearFocus();
            }
        });

        etpassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!etpassword.isFocused()) {
                    hideKeyboard(register.this);
                }
            }
        });
        etpassword2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!etpassword.isFocused()) {
                    hideKeyboard(register.this);
                }
            }
        });

        buttonSex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId){
                    case R.id.radioButton:
                        sex[0] =1;
                        break;
                    case R.id.radioButton2:
                        sex[0] =2;
                        break;
                    default:
                        sex[0] =0;
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

                if (username.matches("id\\d+"))
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(register.this);
                    builder.setMessage("Пользователь с таким именем уже существует")
                            .setNegativeButton("Повторить", null)
                            .create()
                            .show();
                    return;
                }
                if (drExp.getText().toString().equals(""))
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(register.this);
                    builder.setMessage("Необходимо выбрать стаж вождения")
                            .setNegativeButton("Повторить", null)
                            .create()
                            .show();
                    return;
                }

                if (!password.equals(password2))
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(register.this);
                    builder.setMessage("Введённые пароли не совпадают")
                            .setNegativeButton("Повторить", null)
                            .create()
                            .show();
                    return;
                }
                if (Age()<14) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(register.this);
                    builder.setMessage("Возраст должен быть больше либо равен 14")
                            .setNegativeButton("Повторить", null)
                            .create()
                            .show();
                    return;
                }
                if (Age()>110) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(register.this);
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
                                Intent intent = new Intent(register.this, sign.class);
                                progressBar.setVisibility(ProgressBar.INVISIBLE);
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
                        String json;
                        NetworkResponse response = error.networkResponse;
                        if(response != null && response.data != null){
                            switch(response.statusCode){
                                case 400:
                                    json = new String(response.data);
                                    json = trimMessage(json, "message");
                                    //displayMessage(json);
                                    displayMessage(json+"");
                                    break;
                                case 503:
                                    json = new String(response.data);
                                    json = trimMessage(json, "message");
                                    displayMessage("Database error");
                                    break;
                            }
                        }
                        if (response==null){
                            displayMessage("Отсутствует подключение к интернету");
                        }
                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                    }
                };
                progressBar.setVisibility(ProgressBar.VISIBLE);
                String[] headers = {"username", "password", "age", "sex", "name", "country", "city", "experience"};
                String[] values = {username, password, americanDate, String.valueOf(sex[0]), username, country.getText().toString(), city.getText().toString(), drExp.getText().toString()};
                Request regReq = new Request(headers,values,getString(R.string.url_signup),responseListener,errorListener);
                RequestQueue queue = Volley.newRequestQueue(register.this);
                int socketTimeout = 30000;//30 seconds - change to what you want
                RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                regReq.setRetryPolicy(policy);
                queue.add(regReq);
            }
        });

    }
    public static String trimMessage(String json, String key){
        String trimmedString;
        try{
            JSONObject obj = new JSONObject(json);
            trimmedString = obj.getString(key);
            trimmedString=Functions.translate(trimmedString);
        } catch(JSONException e){
            e.printStackTrace();
            return null;
        }
        return trimmedString;
    }
    public void displayMessage(String text){
        AlertDialog.Builder builder = new AlertDialog.Builder(register.this);
        builder.setMessage(text)
                .setNegativeButton("Повторить", null)
                .create()
                .show();
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

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
                    dbName = new String[count+1];
                    dbId[0]=0;
                    dbName[0]="(не указано)";
                    for (int i=1; i<=count; i++){
                        dbId[i]=(items.getJSONObject(i-1)).getInt("id");
                        dbName[i]=(items.getJSONObject(i-1)).getString("title");
                    }
                    adapter = new ArrayAdapter<String>(register.this, android.R.layout.simple_spinner_item, dbName);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
                    dbName2=new String[count+1];
                    dbId2[0]=0;
                    dbName2[0]="(не указано)";
                    for (int i=1; i<=count; i++){
                        dbId2[i]=(items.getJSONObject(i-1)).getInt("id");
                        dbName2[i]=(items.getJSONObject(i-1)).getString("title");
                    }
                    adapter2 = new ArrayAdapter<String>(register.this, android.R.layout.simple_spinner_dropdown_item, dbName2);
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
