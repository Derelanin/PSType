package com.example.pstype_v1.main;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
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
import com.example.pstype_v1.useful.Request;
import com.example.pstype_v1.useful.tokenSaver;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
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
        final RadioButton not = (RadioButton)findViewById(R.id.radioButton3);
        //final EditText age = (EditText)findViewById(R.id.age);
        final int[] sex = {0};

        datePick = (EditText)findViewById(R.id.age);
        Intent intent = getIntent();
        datePick.setText(intent.getStringExtra("age"));
        date = intent.getStringExtra("age");

        String[] bdata = (intent.getStringExtra("age")).split(Pattern.quote("-"));
        Calendar calendar = Calendar.getInstance();
        myDay=Integer.parseInt(bdata[0]);
        myMonth=Integer.parseInt(bdata[1])-1;
        myYear=Integer.parseInt(bdata[2]);
        americanDate = (myMonth+1)+"-"+myDay+"-"+myYear;

        Button bdate = (Button)findViewById(R.id.date);
        bdate.setOnClickListener(new View.OnClickListener() {
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
