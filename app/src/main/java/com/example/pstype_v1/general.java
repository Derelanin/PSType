package com.example.pstype_v1;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general);
        Button exit = (Button)findViewById(R.id.button3);
        Button map = (Button)findViewById(R.id.button5);
        TextView hello =(TextView)findViewById(R.id.textView7);
        hello.setText("Пользователь: "+ tokenSaver.getName(general.this));

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
                Intent intent = new Intent(general.this, Maps.class);
                general.this.startActivity(intent);
            }
        });
    }
}
