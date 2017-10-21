package com.example.pstype_v1.main;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.pstype_v1.R;
import com.example.pstype_v1.data.Contract.track;
import com.example.pstype_v1.data.DbHelper;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Statistics extends AppCompatActivity {

    String pattern = "##0.0000";
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.debug);
        try {
            getInfo();
        }
        catch(Exception e)
        {

        }
    }

    private void getInfo() {
        ListView data = (ListView) findViewById(R.id.data);
        DbHelper mDbHelper= new DbHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        List<String> list= new ArrayList<String>();
        String[] projection = {
                track.COLUMN_DATE,
                track.COLUMN_TIME,
                track.COLUMN_SPEED,
                track.COLUMN_LAT,
                track.COLUMN_LON};
        Cursor cursor = db.query(track.TABLE_NAME, projection, null, null, null, null, null);
        DecimalFormat decimalFormat = new DecimalFormat(pattern);

        try {
            int dateColumnIndex = cursor.getColumnIndex(track.COLUMN_DATE);
            int timeColumnIndex = cursor.getColumnIndex(track.COLUMN_TIME);
            int speedColumnIndex = cursor.getColumnIndex(track.COLUMN_SPEED);
            int latColumnIndex = cursor.getColumnIndex(track.COLUMN_LAT);
            int lonColumnIndex = cursor.getColumnIndex(track.COLUMN_LON);

            // Проходим через все ряды
            while (cursor.moveToNext()) {
                // Используем индекс для получения строки или числа
                String currentDate = cursor.getString(dateColumnIndex);
                String currentTime = cursor.getString(timeColumnIndex);
                double currentSpeed = cursor.getDouble(speedColumnIndex);
                double currentLat = cursor.getDouble(latColumnIndex);
                double currentLon = cursor.getDouble(lonColumnIndex);

                String str = "Дата: "+currentDate+"\t\tВремя: "+currentTime+"\t\tСкорость(км/ч): "+decimalFormat.format(currentSpeed)+"\t\t\tШирота: "+decimalFormat.format(currentLat)+"\t\t\tДолгота: "+decimalFormat.format(currentLon);
                list.add(str);
            }
        } finally {
            // Всегда закрываем курсор после чтения
            cursor.close();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, list);
        data.setAdapter(adapter);
    }

    int count (){
        int count=0;
        DbHelper mDbHelper= new DbHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                track.TABLE_NAME,
                new String[] {"COUNT("+ track.COLUMN_DATE +") AS Count"},
                null, null, null, null, null);
        try {
            int idCount = cursor.getColumnIndex("Count");
            while (cursor.moveToNext()) {
                count = cursor.getInt(idCount);
            }
        } finally {
            cursor.close();
        }
        return count;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.stat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(new Intent(this, general.class));
                finish();
                return true;
            case R.id.update:
//                finish();
//                this.startActivity(new Intent(this, this.getClass()));
                getInfo();
                String num = "["+count()+"]";
                actionBar.setTitle("Отладка "+num);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
