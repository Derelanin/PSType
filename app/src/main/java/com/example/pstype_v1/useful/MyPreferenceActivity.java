package com.example.pstype_v1.useful;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.app.AlertDialog;

import com.example.pstype_v1.R;
import com.example.pstype_v1.data.DbHelper;

/**
 * Created by Derelanin on 06.09.2017.
 */

public class MyPreferenceActivity extends PreferenceActivity{
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
}
