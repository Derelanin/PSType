package com.example.pstype_v1.useful;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.pstype_v1.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class debugging_log extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    SwipeRefreshLayout mSwipeRefreshLayout;
    boolean accel = true;
    ScrollView scrollView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debugging_log);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Лог отладки");

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        scrollView = (ScrollView)findViewById(R.id.scroll);

        ShowLogGPS();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_debug, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.bottom:
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                return true;
            case R.id.top:
                scrollView.fullScroll(ScrollView.FOCUS_UP);
                return true;
            case R.id.gps:
                accel=true;
                ShowLogGPS();
                invalidateOptionsMenu();
                return true;
            case R.id.accel:
                accel=false;
                ShowLogAccel();
                invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        MenuItem it1 = menu.findItem(R.id.gps);
        MenuItem it2 = menu.findItem(R.id.accel);
        if (accel)
        {
            it1.setVisible(false);
            it2.setVisible(true);
        }
        else
        {
            it2.setVisible(false);
            it1.setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
                if (accel)
                    ShowLogGPS();
                else
                    ShowLogAccel();
            }
        }, 1000);
    }

    void ShowLogGPS(){
        String FILENAME = "PSType-log";
        TextView textView = (TextView)findViewById(R.id.log);
        textView.setText("");
        textView.setText("Лог отладки GPS:\n\n");
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(openFileInput(FILENAME)));
            String str = "";
            while ((str = br.readLine()) != null) {
                textView.append(str);
                textView.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    void ShowLogAccel(){
        String FILENAME = "PSType-Accel";
        TextView textView = (TextView)findViewById(R.id.log);
        textView.setText("");
        textView.setText("Лог отладки акселерометра:\n\n");
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(openFileInput(FILENAME)));
            String str = "";
            while ((str = br.readLine()) != null) {
                textView.append(str);
                textView.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//    private static final int SWIPE_MIN_DISTANCE = 120;
//    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
//
//    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
//        @Override
//        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//            if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//                return false; // справа налево
//            }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//                return false; // слева направо
//            }
//
//            if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
//                down.setVisibility(View.VISIBLE);
//                return false; // снизу вверх
//            }  else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
//                return false; // сверху вниз
//            }
//            return false;
//        }
//    }
}
