package com.example.pstype_v1.main;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.pstype_v1.R;
import com.example.pstype_v1.useful.Request;

import java.util.Calendar;
import java.util.regex.Pattern;

public class addSignup extends AppCompatActivity {

    int dialog_date=1;
    Calendar calendar = Calendar.getInstance();
    int myYear = calendar.get(Calendar.YEAR);
    int myMonth = calendar.get(Calendar.MONTH);
    int myDay = calendar.get(Calendar.DAY_OF_MONTH);
    String date = myDay+"-"+(myMonth+1)+"-"+myYear;
    String americanDate;
    EditText datePick;
    String[] exp = { "Отсутствует", "Менее 6 месяцев", "От 6 месяцев до 1 года", "1-3 года",
            "3-5 лет", "5-10 лет", "10-15 лет", "15-20 лет", "Более 20 лет" };
    Intent intent;
    boolean flAge = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_signup);
        final Button buttonReg = (Button) findViewById(R.id.button4);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar2);


        datePick = (EditText)findViewById(R.id.editText5);
        datePick.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (datePick.isFocused()){
                    showDialog(dialog_date);
                    datePick.clearFocus();
                }
            }
        });
        intent = getIntent();
        if (intent.getIntExtra("age", 0)!=0) {
            datePick.setVisibility(View.GONE);
            flAge=false;
        }

        final Spinner driverExp = (Spinner)findViewById(R.id.spinner);
        final EditText drExp = (EditText)findViewById(R.id.editText6);
        ArrayAdapter<String> adapterExp = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, exp);
        adapterExp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        driverExp.setAdapter(adapterExp);
        driverExp.setSelection(1);
        drExp.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (drExp.isFocused()){
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
                    drExp.clearFocus();
                }
            }
        });

        buttonReg.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                if (flAge) {
                    if (Age() < 14) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(addSignup.this);
                        builder.setMessage("Возраст должен быть больше либо равен 14")
                                .setNegativeButton("Повторить", null)
                                .create()
                                .show();
                        return;
                    }
                    if (Age() > 110) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(addSignup.this);
                        builder.setMessage("Возраст должен быть меньше либо равен 110")
                                .setNegativeButton("Повторить", null)
                                .create()
                                .show();
                        return;
                    }
                }

                if (flAge) {
                    if (datePick.getText().toString().equals("") || drExp.getText().toString().equals("")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(addSignup.this);
                        builder.setMessage("Необходимо заполнить все поля")
                                .setNegativeButton("Повторить", null)
                                .create()
                                .show();
                        return;
                    }
                }
                else
                {
                    if (drExp.getText().toString().equals("")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(addSignup.this);
                        builder.setMessage("Необходимо заполнить все поля")
                                .setNegativeButton("Повторить", null)
                                .create()
                                .show();
                        return;
                    }
                }


                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Intent intent = new Intent(addSignup.this, sign.class);
                        intent.putExtra("add", true);
                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                        addSignup.this.startActivity(intent);
                        finish();
                    }
                };
                Response.ErrorListener errorListener= new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error != null) {
                            Log.e("Volley", "Error. HTTP Status Code:"+error.networkResponse.statusCode);
                        }
                        if (error instanceof TimeoutError) {
                            Log.e("Volley", "TimeoutError");
                        }else if(error instanceof NoConnectionError){
                            Log.e("Volley", "NoConnectionError");
                        } else if (error instanceof AuthFailureError) {
                            Log.e("Volley", "AuthFailureError");
                        } else if (error instanceof ServerError) {
                            Log.e("Volley", "ServerError");
                        } else if (error instanceof NetworkError) {
                            Log.e("Volley", "NetworkError");
                        } else if (error instanceof ParseError) {
                            Log.e("Volley", "ParseError");
                        }
                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                    }
                };
                progressBar.setVisibility(ProgressBar.VISIBLE);
                String[] headers, values;
                if (flAge)
                {
                    headers = new String[] {"token", "experience", "age"};
                    values = new String[] {intent.getStringExtra("token"), drExp.getText().toString(), americanDate};
                }
                else {
                    headers = new String[] {"token", "experience"};
                    values = new String[] {intent.getStringExtra("token"), drExp.getText().toString()};
                }
                Request regReq = new Request(headers,values,getString(R.string.url_change),responseListener,errorListener);
                RequestQueue queue = Volley.newRequestQueue(addSignup.this);
                int socketTimeout = 40000;//30 seconds - change to what you want
                RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                regReq.setRetryPolicy(policy);
                queue.getCache().clear();
                queue.add(regReq);
            }
        });
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
