package com.example.pstype_v1.useful;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.pstype_v1.R;
import com.example.pstype_v1.data.DbHelper;
import com.example.pstype_v1.main.general;

import static android.app.PendingIntent.getActivity;

/**
 * Created by Derelanin on 06.09.2017.
 */

public class MyPreferenceActivity extends PreferenceActivity {
    private static final int NOTIFY_ID = 101;


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        LinearLayout root = (LinearLayout)findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        root.addView(bar, 0); // insert at top
        Drawable drawable = bar.getNavigationIcon();
        drawable.setColorFilter(ContextCompat.getColor(this, R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        Preference del = findPreference(getString(R.string.del));
        del.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                delfun();
                Toast.makeText(getApplicationContext(), "База данных успешно очищена", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        Preference about = findPreference(getString(R.string.about));
        about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                message();
                return true;
            }
        });

        Preference look = findPreference(getString(R.string.look));
        look.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                not();
                return true;
            }
        });
    }

    void delfun (){
        DbHelper mDbHelper= new DbHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        db.delete("track", null, null);
    }

    void message(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Определение психотипа водителя.\n\nАвторы:\nИванникова Валентина\nКозлова Анна\nСукач Елизавета")
                .setTitle("PSType")
                .setIcon(R.mipmap.ic_launcher)
                .create()
                .show();
    }

    public void not (){
        SharedPreferences sp;
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean setting = sp.getBoolean("look", false);
        Context context = getApplicationContext();
        Intent notificationIntent = new Intent(context, general.class);
        PendingIntent contentIntent = getActivity(context,
                0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        Resources res = context.getResources();
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentIntent(contentIntent)
                //.setTicker(res.getString(R.string.warning)) // текст в строке состояния
                .setTicker("Кажется, что за вами следят")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(false)
                //.setContentTitle(res.getString(R.string.notifytitle)) // Заголовок уведомления
                .setContentTitle("Слежка")
                //.setContentText(res.getString(R.string.notifytext))
                .setContentText("Включено отслеживание местоположения"); // Текст уведомления

        // Notification notification = builder.getNotification(); // до API 16
        Notification notification = builder.build();
        notification.flags = notification.flags | Notification.FLAG_INSISTENT;
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (!setting) {
            startService(new Intent(this, tracking.class));
            notificationManager.notify(NOTIFY_ID, notification);
        }
        else {
            stopService(new Intent(this, tracking.class));
            notificationManager.cancel(NOTIFY_ID);
        }
    }


}
