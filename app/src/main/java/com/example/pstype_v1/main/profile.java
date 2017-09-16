package com.example.pstype_v1.main;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.pstype_v1.R;
import com.example.pstype_v1.signin.sign;
import com.example.pstype_v1.useful.Request;
import com.example.pstype_v1.useful.tokenSaver;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class profile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Профиль");
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
                        String buf="Возраст: "+jsonResponse.getString("age");
                        age.setText(buf);
                        if (jsonResponse.getString("sex").equals("true")){
                            sex.setText("Пол: мужской");
                        }
                        else{
                            sex.setText("Пол: женский");
                        }
                        CircleImageView photo = (CircleImageView)findViewById(R.id.avatar);
                        if ((!tokenSaver.getURL(profile.this).equals("URL"))&&(!tokenSaver.getURL(profile.this).equals(""))) {
                            new DownloadImageFromInternet((CircleImageView) findViewById(R.id.avatar))
                                    .execute(tokenSaver.getURL(profile.this));
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
        String url="http://pstype-pstype.1d35.starter-us-east-1.openshiftapps.com/api/v1/change/data";
        Request info = new Request(headers,values,url,responseListener,errorListener);
        RequestQueue queue = Volley.newRequestQueue(profile.this);
        queue.add(info);
    }


    private class DownloadImageFromInternet extends AsyncTask<String, Void, Bitmap> {
        CircleImageView imageView;

        public DownloadImageFromInternet(CircleImageView imageView) {
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
                startActivity(new Intent(this, profile_change.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
