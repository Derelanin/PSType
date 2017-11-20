package com.example.pstype_v1.useful;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

import com.example.pstype_v1.R;
import com.example.pstype_v1.main.general;

import static android.app.PendingIntent.getActivity;

/**
 * Created by Derelanin on 19.11.2017.
 */

public class Notification {
    NotificationManager notificationManager;
    android.app.Notification notification;

    public Notification(Context context){
        //Context context = getApplicationContext();
        Intent notificationIntent = new Intent(context, general.class);
        PendingIntent contentIntent = getActivity(context,
                0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        Resources res = context.getResources();
        android.app.Notification.Builder builder = new android.app.Notification.Builder(context);
        builder.setContentIntent(contentIntent)
                .setTicker("Кажется, что за вами следят")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(false)
                .setContentTitle("Слежка")
                .setContentText("Включено отслеживание местоположения");
        notification = builder.build();
        notification.flags = notification.flags | android.app.Notification.FLAG_INSISTENT;
        notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
    }
    public void Show(){
        notificationManager.notify(101, notification);
    }
    public void NotShow(){
        notificationManager.cancel(101);
    }
}
