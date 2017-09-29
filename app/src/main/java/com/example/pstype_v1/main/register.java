package com.example.pstype_v1.main;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        final Functions func = new Functions(register.this);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        final EditText etusername=(EditText)findViewById(R.id.editText3);
        final EditText etpassword=(EditText)findViewById(R.id.editText4);
        final EditText etpassword2=(EditText)findViewById(R.id.editText7);
        final Button buttonReg = (Button) findViewById(R.id.button4);
        final RadioGroup buttonSex = (RadioGroup)findViewById(R.id.RadioGroup);
        final int[] sex = {0};

        etpassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    func.hideKeyboard(v);
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
        datePick = (EditText)findViewById(R.id.editText5);
        Button bdate = (Button)findViewById(R.id.date);
        bdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(dialog_date);
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
                String age = datePick.getText().toString();
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
                String[] headers = {"username", "password", "age", "sex"};
                String[] values = {username, password, americanDate, String.valueOf(sex[0])};
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
}
