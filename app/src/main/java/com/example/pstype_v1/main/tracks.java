package com.example.pstype_v1.main;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.pstype_v1.R;
import com.example.pstype_v1.useful.EventDecorator;
import com.example.pstype_v1.useful.Request;
import com.example.pstype_v1.useful.tokenSaver;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.R.layout.simple_list_item_1;

public class tracks extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracks);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.tracks);

        final MaterialCalendarView mvc = (MaterialCalendarView)findViewById(R.id.calendarView);
        final ArrayList<CalendarDay> dates = new ArrayList<>();
        final ProgressBar progressBarMain = (ProgressBar)findViewById(R.id.progressBar8);
        final ProgressBar progressBarLittle = (ProgressBar)findViewById(R.id.progressBar11);
        final ArrayAdapter[] adapter = new ArrayAdapter[1];
        final ConstraintLayout no_track = (ConstraintLayout)findViewById(R.id.no_track);
        final ListView tracksList = (ListView)findViewById(R.id.tracks);
        final JSONArray[] tracksArray = new JSONArray[1];
        final String[] tempDate = new String[1];
        final Dialog dialog;
        dialog = new Dialog(tracks.this);
        dialog.setContentView(R.layout.dialog_track);

        tracksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {
                dialog.show();
                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String SHARED_PREF_NAME = "SHARED_PREF_NAME";
                            SharedPreferences sPref = tracks.this.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sPref.edit();
                            editor.putBoolean("POINTS", true);
                            editor.apply();

                            Intent intent = new Intent(tracks.this, Maps.class);
                            intent.putExtra("points", jsonResponse.toString());
                            dialog.cancel();
                            startActivity(intent);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                Response.ErrorListener errorListener = new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                };
                String[] headers = {"token", "dateTrack", "StartTime"};
                String[] values = {tokenSaver.getToken(tracks.this), tempDate[0], ((TextView) itemClicked).getText().toString().substring(0,8)};
                Request signReq = new Request(headers, values, getString(R.string.url_getPoints), responseListener, errorListener);
                RequestQueue queue = Volley.newRequestQueue(tracks.this);
                int socketTimeout = 30000;//30 seconds - change to what you want
                RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                signReq.setRetryPolicy(policy);
                queue.add(signReq);
            }
        });

        mvc.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                Date selectedDate = new Date(date.getYear()-1900,date.getMonth(),date.getDay());
                tempDate[0] = String.format("%tY-%tm-%td",selectedDate, selectedDate, selectedDate);
                no_track.setVisibility(View.INVISIBLE);
                progressBarLittle.setVisibility(View.VISIBLE);
                ArrayList<String> times = new ArrayList<>();
                try {
                    for (int i = 0; i< tracksArray[0].length(); i++){
                        String dateStr = null;
                        dateStr = tracksArray[0].getJSONObject(i).getString("dateTrack");
                        if (dateStr.equals(tempDate[0])) {
                            times.add(tracksArray[0].getJSONObject(i).getString("StartTime")+" - "+tracksArray[0].getJSONObject(i).getString("StopTime"));
                        }
                    }
                    progressBarLittle.setVisibility(View.INVISIBLE);
                    if (times.isEmpty()) no_track.setVisibility(View.VISIBLE);
                    adapter[0] = new ArrayAdapter(tracks.this, simple_list_item_1, times);
                    tracksList.setAdapter(adapter[0]);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    tracksArray[0] = jsonResponse.getJSONArray("str");
                    if (tracksArray[0].length()==0)
                    {
                        mvc.setVisibility(View.VISIBLE);
                        no_track.setVisibility(View.VISIBLE);
                        progressBarMain.setVisibility(View.INVISIBLE);
                    }
                    else {
                        String temp = "";
                        for (int i = 0; i< tracksArray[0].length(); i++){
                            String dateStr = tracksArray[0].getJSONObject(i).getString("dateTrack");
                            if (!dateStr.equals(temp)) {
                                temp = dateStr;
                                DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                                Date date = format.parse(dateStr);
                                dates.add(new CalendarDay(date));
                            }
                        }
                        mvc.addDecorator(new EventDecorator(Color.rgb(0xaf, 0x55, 0x39), dates));
                        mvc.refreshDrawableState();
                        mvc.setVisibility(View.VISIBLE);
                        mvc.setSelectedDate(new Date());
                        Date selectedDate = new Date();
                        tempDate[0] = String.format("%tY-%tm-%td",selectedDate, selectedDate, selectedDate);
                        no_track.setVisibility(View.INVISIBLE);
                        progressBarLittle.setVisibility(View.VISIBLE);
                        ArrayList<String> times = new ArrayList<>();
                        try {
                            for (int i = 0; i< tracksArray[0].length(); i++){
                                String dateStr = null;
                                dateStr = tracksArray[0].getJSONObject(i).getString("dateTrack");
                                if (dateStr.equals(tempDate[0])) {
                                    times.add(tracksArray[0].getJSONObject(i).getString("StartTime")+" - "+tracksArray[0].getJSONObject(i).getString("StopTime"));
                                }
                            }
                            progressBarLittle.setVisibility(View.INVISIBLE);
                            if (times.isEmpty()) no_track.setVisibility(View.VISIBLE);
                            adapter[0] = new ArrayAdapter(tracks.this, simple_list_item_1, times);
                            tracksList.setAdapter(adapter[0]);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        progressBarMain.setVisibility(View.INVISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        };
        Response.ErrorListener errorListener= new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        };
        String[] headers = {"token"};
        String[] values = {tokenSaver.getToken(tracks.this)};
        Request signReq = new Request(headers,values,getString(R.string.url_getDate),responseListener,errorListener);
        RequestQueue queue = Volley.newRequestQueue(tracks.this);
        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        signReq.setRetryPolicy(policy);
        queue.add(signReq);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_track, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(new Intent(this, general.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

