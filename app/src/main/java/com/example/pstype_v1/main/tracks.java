package com.example.pstype_v1.main;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.SimpleExpandableListAdapter;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.pstype_v1.R;
import com.example.pstype_v1.useful.Request;
import com.example.pstype_v1.useful.tokenSaver;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class tracks extends AppCompatActivity {

    ArrayList<Map<String, String>> groupData;
    ArrayList<Map<String, String>> childDataItem;
    ArrayList<ArrayList<Map<String, String>>> childData;
    Map<String, String> m;
    ExpandableListView tracks;
    SimpleExpandableListAdapter adapter;
    ActionBar actionBar;

    final String ATTR_GROUP_NAME= "dateName";
    final String ATTR_PHONE_NAME= "dataName2";

    final String SHARED_PREF_NAME = "SHARED_PREF_NAME";
    static SharedPreferences sPref;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracks);

        actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.tracks);

        dialog = new Dialog(tracks.this);
        dialog.setContentView(R.layout.dialog_track);

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    String success = jsonResponse.getString("str");
                    success = success.substring(1,success.length()-1);
                    String hh = success.substring(0,25);
                    if (hh.contains("undefined"))
                    {
                        ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar5);
                        pb.setVisibility(View.INVISIBLE);
                        ConstraintLayout no_track = (ConstraintLayout)findViewById(R.id.no_track);
                        no_track.setVisibility(View.VISIBLE);
                    }
                    else {
                        String[] dates = success.split(Pattern.quote(";"));
                        String date = "";
                        groupData = new ArrayList<Map<String, String>>();
                        String groupFrom[] = new String[]{"dateName"};
                        int groupTo[] = new int[]{android.R.id.text1};
                        childData = new ArrayList<ArrayList<Map<String, String>>>();
                        boolean flag = false;

                        jsonResponse = new JSONObject(dates[0]);
                        if (!jsonResponse.getString("StopTime").equals("undefined")) {
                            if (!jsonResponse.getString("dateTrack").equals(date)) {
                                if (flag) {
                                    childData.add(childDataItem);
                                }
                                childDataItem = new ArrayList<Map<String, String>>();
                                date = jsonResponse.getString("dateTrack");
                                m = new HashMap<String, String>();
                                m.put("dateName", date);
                                groupData.add(m);
                            }
                            m = new HashMap<String, String>();
                            m.put("dataName2", jsonResponse.getString("StartTime") + " - " + jsonResponse.getString("StopTime"));
                            childDataItem.add(m);
                            if (!flag)
                                flag = true;
                        }

                        for (int i = 1; i < dates.length; i++) {
                            jsonResponse = new JSONObject(dates[i]);
                            if (!jsonResponse.getString("dateTrack").equals(date)) {
                                if (flag) {
                                    childData.add(childDataItem);
                                }
                                childDataItem = new ArrayList<Map<String, String>>();
                                date = jsonResponse.getString("dateTrack");
                                m = new HashMap<String, String>();
                                m.put("dateName", date);
                                groupData.add(m);
                            }
                            m = new HashMap<String, String>();
                            m.put("dataName2", jsonResponse.getString("StartTime") + " - " + jsonResponse.getString("StopTime"));
                            childDataItem.add(m);
                            if (!flag)
                                flag = true;
                        }

                        childData.add(childDataItem);

                        //childData.add(childDataItem);


                        // список атрибутов элементов для чтения
                        String childFrom[] = new String[]{"dataName2"};
                        // список ID view-элементов, в которые будет помещены атрибуты элементов
                        int childTo[] = new int[]{android.R.id.text1};
                        adapter = new SimpleExpandableListAdapter(
                                tracks.this,
                                groupData,
                                android.R.layout.simple_expandable_list_item_1,
                                groupFrom,
                                groupTo,
                                childData,
                                android.R.layout.simple_list_item_1,
                                childFrom,
                                childTo);

                        tracks = (ExpandableListView) findViewById(R.id.tracks);
                        tracks.setAdapter(adapter);

                        ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar5);
                        pb.setVisibility(View.INVISIBLE);

                        tracks.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                            public boolean onChildClick(ExpandableListView parent, View v,
                                                        int groupPosition, int childPosition, long id) {
                                dialog.show();
                                Response.Listener<String> responseListener = new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        try {
                                            JSONObject jsonResponse = new JSONObject(response);
                                            sPref = tracks.this.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sPref.edit();
                                            editor.putBoolean("POINTS", true);
                                            editor.apply();

                                            Intent intent = new Intent(tracks.this, Maps.class);
                                            intent.putExtra("points", jsonResponse.getString("points"));
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
                                String[] values = {tokenSaver.getToken(tracks.this), getGroupText(groupPosition), getChildText(groupPosition, childPosition)};
                                Request signReq = new Request(headers, values, getString(R.string.url_getPoints), responseListener, errorListener);
                                RequestQueue queue = Volley.newRequestQueue(tracks.this);
                                int socketTimeout = 30000;//30 seconds - change to what you want
                                RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                                signReq.setRetryPolicy(policy);
                                queue.add(signReq);
                                return false;
                            }
                        });
                    }
                } catch (JSONException e) {
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
    String getGroupText(int groupPos) {
        //String nn = groupData.get(groupPos).get(ATTR_GROUP_NAME);
        return ((Map<String,String>)(adapter.getGroup(groupPos))).get(ATTR_GROUP_NAME);
    }

    String getChildText(int groupPos, int childPos) {
        return (((Map<String,String>)(adapter.getChild(groupPos, childPos))).get(ATTR_PHONE_NAME)).substring(0,8);
    }

    String getGroupChildText(int groupPos, int childPos) {
        return getGroupText(groupPos) + " " +  getChildText(groupPos, childPos);
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
