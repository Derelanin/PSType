package com.example.pstype_v1.main;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.example.pstype_v1.useful.tokenSaver;

import java.io.InputStream;

public class general extends AppCompatActivity {

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

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general);
        //Button exit = (Button)findViewById(R.id.button3);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar3);
        ImageView exit = (ImageView) findViewById(R.id.exit);
        ImageView ram = (ImageView) findViewById(R.id.round);
        Button map = (Button)findViewById(R.id.button5);
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
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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
