package com.example.pstype_v1.main;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
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

public class profile extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {

    String Age, Sex, dateAge;
    private AppBarLayout mAppBarLayout;
    private Toolbar mToolbar;
    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR  = 0.9f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS     = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION              = 200;
    private boolean mIsTheTitleVisible          = false;
    private boolean mIsTheTitleContainerVisible = true;
    private TextView mTitle;
    String Name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile2);

        mToolbar = (Toolbar)findViewById(R.id.toolbar4);
        mAppBarLayout = (AppBarLayout)findViewById(R.id.appbar);
        mTitle = (TextView)findViewById(R.id.textView6);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setElevation(0);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(" ");
        actionBar.hide();
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar3);
        final AppBarLayout layout1 = (AppBarLayout) findViewById(R.id.appbar);
        final NestedScrollView layout2 = (NestedScrollView)findViewById(R.id.nsv);
        mAppBarLayout.addOnOffsetChangedListener(this);
        //startAlphaAnimation(mTitle, 0, View.INVISIBLE);

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
                        Name = (String) name.getText();
                        TextView age = (TextView)findViewById(R.id.textView7);
                        TextView sex = (TextView)findViewById(R.id.textView8);

                        Age=jsonResponse.getString("age");

                        //String buf="Возраст: "+Age;
                        age.setText(Age);

                        Sex=jsonResponse.getString("sex");
                        switch (Integer.parseInt(Sex)){
                            case 1:
                                sex.setText("мужской");;
                                break;
                            case 2:
                                sex.setText("женский");
                                break;
                            case 0:
                                sex.setText("не указан");
                                break;
                        }
                        actionBar.show();
                        CircleImageView photo = (CircleImageView)findViewById(R.id.avatar);
                        if (tokenSaver.getURL(profile.this).equals("VK")) {
                            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "Android/data/com.example.pstype_v1"+ File.separator,"myBitmap.jpg");
                            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                            CircleImageView ava = (CircleImageView) findViewById(R.id.avatar);
                            ava.setImageBitmap(bitmap);
                        }

                        TextView ps = (TextView)findViewById(R.id.textView9);
                       // buf="Психотип: "+jsonResponse.getString("type");
                        ps.setText(jsonResponse.getString("type"));
                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                        layout1.setVisibility(ConstraintLayout.VISIBLE);
                        layout2.setVisibility(ConstraintLayout.VISIBLE);
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
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(offset) / (float) maxScroll;

        handleAlphaOnTitle(percentage);
        handleToolbarTitleVisibility(percentage);
    }

    private void handleToolbarTitleVisibility(float percentage) {
        if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {

            if(!mIsTheTitleVisible) {
                startAlphaAnimation(mTitle, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                ActionBar actionBar = getSupportActionBar();
                actionBar.setTitle(Name);
                mIsTheTitleVisible = true;
            }

        } else {

            if (mIsTheTitleVisible) {
                startAlphaAnimation(mTitle, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                ActionBar actionBar = getSupportActionBar();
                actionBar.setTitle("");
                mIsTheTitleVisible = false;
            }
        }
    }
    public static void startAlphaAnimation (View v, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }
    private void handleAlphaOnTitle(float percentage) {
        if (percentage >= PERCENTAGE_TO_HIDE_TITLE_DETAILS) {
            if(mIsTheTitleContainerVisible) {
                startAlphaAnimation(mTitle, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleContainerVisible = false;
            }

        } else {

            if (!mIsTheTitleContainerVisible) {
                startAlphaAnimation(mTitle, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleContainerVisible = true;
            }
        }
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
