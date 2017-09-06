package com.example.pstype_v1.main;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.pstype_v1.R;
import com.example.pstype_v1.signin.sign;
import com.example.pstype_v1.useful.MyPreferenceActivity;
import com.example.pstype_v1.useful.tokenSaver;
import com.example.pstype_v1.useful.tracking;

import java.io.InputStream;

public class general extends AppCompatActivity {

    SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        String flag = tokenSaver.getToken(general.this);
        if (flag.isEmpty())
        {
            Intent intent = new Intent(general.this, sign.class);
            general.this.startActivity(intent);
            finish();
        }

        if (tokenSaver.getFIRST(general.this).isEmpty()) {
            Intent intent = new Intent(general.this, welcome.class);
            tokenSaver.setFIRST(general.this);
            general.this.startActivity(intent);
        }

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean setting = sp.getBoolean("look", false);

        requestMultiplePermissions();

        if (setting)
            startService(new Intent(this, tracking.class));
        else
            stopService(new Intent(this, tracking.class));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general);
        //Button exit = (Button)findViewById(R.id.button3);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar3);
        ImageView exit = (ImageView) findViewById(R.id.exit);
        ImageView ram = (ImageView) findViewById(R.id.round);
        Button map = (Button)findViewById(R.id.button5);
        Button set = (Button)findViewById(R.id.settings);
        Button stat = (Button)findViewById(R.id.stat);
        //String[] fingerprints = VKUtil.getCertificateFingerprint(this, this.getPackageName());
        Toolbar bar = (Toolbar) findViewById(R.id.toolbar2);
        bar.setTitle(tokenSaver.getName(general.this));
        bar.setLogo(R.mipmap.ic_launcher);
        ram.setVisibility(ImageView.INVISIBLE);
        if ((!tokenSaver.getURL(general.this).equals("URL"))&&(!tokenSaver.getURL(general.this).equals(""))) {
            new DownloadImageFromInternet((ImageView) findViewById(R.id.avatar))
                    .execute(tokenSaver.getURL(general.this));
            ImageView ava = (ImageView) findViewById(R.id.avatar);
            ava.setVisibility(ImageView.VISIBLE);
            ram.setVisibility(ImageView.VISIBLE);
        }

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tokenSaver.clearToken(general.this);
                Intent intent = new Intent(general.this, sign.class);
                general.this.startActivity(intent);
                finish();
            }
        });

        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    progressBar.setVisibility(ProgressBar.VISIBLE);
                    Intent intent = new Intent(general.this, Maps.class);
                    general.this.startActivity(intent);
                    progressBar.setVisibility(ProgressBar.INVISIBLE);
            }
        });

        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(general.this, MyPreferenceActivity.class);
                general.this.startActivity(intent);
            }
        });

        stat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(general.this, Statistics.class);
                general.this.startActivity(intent);
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private boolean isPermissionGranted(String permission) {
        // проверяем разрешение - есть ли оно у нашего приложения
        int permissionCheck = ActivityCompat.checkSelfPermission(this, permission);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }
    protected void onResume() {
        Boolean notif = sp.getBoolean("look", false);
        super.onResume();
    }
    private void requestPermission(String permission, int requestCode) {
        // запрашиваем разрешение
        ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
    }

    public void requestMultiplePermissions() {
        ActivityCompat.requestPermissions(this,
                new String[] {
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                10001);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == 10001) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    private class DownloadImageFromInternet extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;

        public DownloadImageFromInternet(ImageView imageView) {
            this.imageView = imageView;
            //Toast.makeText(getApplicationContext(), "Загрузка изображения профиля", Toast.LENGTH_SHORT).show();
        }

        protected Bitmap doInBackground(String... urls) {
            String imageURL = urls[0];
            Bitmap bimage = null;
            try {
                InputStream in = new java.net.URL(imageURL).openStream();
                bimage = BitmapFactory.decodeStream(in);

            } catch (Exception e) {
                Log.e("Error Message", e.getMessage());
                e.printStackTrace();
            }
            return bimage;
        }

        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }

    public void unbutton(View view){
        Toast.makeText(getApplicationContext(), "Данная функция пока недоступна", Toast.LENGTH_SHORT).show();
    }
}
