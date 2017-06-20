package com.example.pstype_v1;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class general extends AppCompatActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        String flag = tokenSaver.getToken(general.this);
        if (flag.isEmpty())
        {
            Intent intent = new Intent(general.this, sign.class);
            general.this.startActivity(intent);
            finish();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general);
        //Button exit = (Button)findViewById(R.id.button3);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar3);
        ImageView exit = (ImageView) findViewById(R.id.exit);
        Button map = (Button)findViewById(R.id.button5);
        Toolbar bar = (Toolbar) findViewById(R.id.toolbar2);
        bar.setTitle(tokenSaver.getName(general.this));
        bar.setLogo(R.mipmap.ic_launcher);

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

}
