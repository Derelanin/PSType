package com.example.pstype_v1.main;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

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

import java.io.File;
import java.util.Calendar;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class profile extends AppCompatActivity {

    String Age, Sex, dateAge;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Профиль");
        actionBar.hide();
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar4);
        final ConstraintLayout layout = (ConstraintLayout)findViewById(R.id.all);


        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    String success = jsonResponse.getString("status");

                    if (success.equals("ok")) {
                        TextView name = (TextView)findViewById(R.id.textView6);
                        //name.setText(tokenSaver.getName(profile.this));
                        name.setText(jsonResponse.getString("username"));
                        TextView age = (TextView)findViewById(R.id.textView7);
                        TextView sex = (TextView)findViewById(R.id.textView8);

                        Age=jsonResponse.getString("age");

                        String buf="Возраст: "+Age;
                        age.setText(buf);

                        Sex=jsonResponse.getString("sex");
                        switch (Integer.parseInt(Sex)){
                            case 1:
                                sex.setText("Пол: мужской");;
                                break;
                            case 2:
                                sex.setText("Пол: женский");
                                break;
                            case 0:
                                sex.setText("Пол: не указан");
                                break;
                        }
                        actionBar.show();
                        CircleImageView photo = (CircleImageView)findViewById(R.id.avatar);
                        if (tokenSaver.getURL(profile.this).equals("VK")) {
                            File file = new File(Environment.
                                    getExternalStorageDirectory()+ File.separator,"myBitmap.jpg");
                            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                            CircleImageView ava = (CircleImageView) findViewById(R.id.avatar);
                            ava.setImageBitmap(bitmap);
                        }

                        TextView ps = (TextView)findViewById(R.id.textView9);
                        buf="Психотип: "+jsonResponse.getString("type");
                        ps.setText(buf);
                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                        layout.setVisibility(ConstraintLayout.VISIBLE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        Response.ErrorListener errorListener= new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                tokenSaver.clearToken(profile.this);
                Intent intent = new Intent(profile.this, sign.class);
                profile.this.startActivity(intent);
                finish();
            }
        };
        String[] headers = {"token"};
        String[] values = {tokenSaver.getToken(profile.this)};
        Request info = new Request(headers,values,getString(R.string.url_data),responseListener,errorListener);
        RequestQueue queue = Volley.newRequestQueue(profile.this);
        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        info.setRetryPolicy(policy);
        queue.add(info);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(new Intent(this, general.class));
                finish();
                return true;
            case R.id.item2:
                //startActivity(new Intent(this, profile_change.class));
                Intent intent = new Intent(this, profile_change.class);
                intent.putExtra("age", dateAge);
                intent.putExtra("sex", Sex);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    int AgeProfile (String date){
        int age;
        String[] bdata = date.split(Pattern.quote("-"));
        Calendar calendar = Calendar.getInstance();
        int byear=Integer.parseInt(bdata[0]);
        int year=calendar.get(Calendar.YEAR);
        age=year-byear;
        int month= calendar.get(Calendar.MONTH)+1;
        int bmonth=Integer.parseInt(bdata[1]);
        if (bmonth>month)
            age--;
        if (bmonth==month){
            int bday=Integer.parseInt(bdata[2]);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            if (bday>day)
                age--;
        }
        return age;
    }
    String parseAge(String Age){
        String[] date = Age.split(Pattern.quote("-"));
        return (date[2]+"-"+date[1]+"-"+date[0]);
    }
}
