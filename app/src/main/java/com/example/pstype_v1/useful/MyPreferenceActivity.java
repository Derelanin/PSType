package com.example.pstype_v1.useful;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

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
                AlertDialog.Builder builder = new AlertDialog.Builder(MyPreferenceActivity.this);
                builder.setMessage("Вы действительно хотите удалить записи из БД?")
                        .setCancelable(false)
                        .setPositiveButton("Да",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        delfun();
                                        Toast.makeText(getApplicationContext(), "База данных успешно очищена", Toast.LENGTH_SHORT).show();
                                    }
                                })
                        .setNegativeButton("Нет",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.cancel();
                                    }
                                }).create().show();
                return true;
            }
        });

        final CheckBoxPreference screenOn = (CheckBoxPreference)findPreference("screenOn");
        screenOn.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String SHARED_PREF_NAME = "SHARED_PREF_NAME";
                SharedPreferences sPref = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sPref.edit();
                if (screenOn.isChecked())
                {
                    editor.putBoolean("SCREEN", true);
                }
                else
                {
                    editor.putBoolean("SCREEN", false);
                }
                editor.apply();
                return false;
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

        Preference log_read = findPreference(getString(R.string.log_read));
        log_read.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                readLog();
                return true;
            }
        });

        Preference log_copy = findPreference(getString(R.string.log_copy));
        log_copy.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String FILENAME = "PSType-log";
                String FILENAME2 = "PSType-LatLng";

                String DIR_SD = "PSType";
                String FILENAME_SD = "PSType-log(2)";
                String FILENAME_SD2 = "PSType-LtdLng";

                if (!Environment.getExternalStorageState().equals(
                        Environment.MEDIA_MOUNTED)) {
                    Toast.makeText(getApplicationContext(), "Нет доступа к SD", Toast.LENGTH_SHORT).show();
                }
                else {
                    File sdPath = Environment.getExternalStorageDirectory();
                    sdPath = new File(sdPath.getAbsolutePath() + "/" + DIR_SD);
                    sdPath.mkdirs();
                    File sdFile = new File(sdPath, FILENAME_SD);

                    try {
                        BufferedWriter bw = new BufferedWriter(new FileWriter(sdFile));
                        BufferedReader br = new BufferedReader(new InputStreamReader(openFileInput(FILENAME)));
                        String str = "";
                        while ((str = br.readLine()) != null) {
                            bw.append(str);
                            bw.append("\n");
                        }
                        bw.close();
                        //Toast.makeText(getApplicationContext(), "Вроде успешно", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    sdFile = new File(sdPath, FILENAME_SD2);
                    try {
                        BufferedWriter bw = new BufferedWriter(new FileWriter(sdFile));
                        BufferedReader br = new BufferedReader(new InputStreamReader(openFileInput(FILENAME2)));
                        String str = "";
                        while ((str = br.readLine()) != null) {
                            bw.append(str);
                            bw.append("\n");
                        }
                        bw.close();
                        Toast.makeText(getApplicationContext(), "Вроде успешно", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }



                return true;
            }
        });

        Preference log_clear = findPreference(getString(R.string.log_clear));
        log_clear.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MyPreferenceActivity.this);
                builder.setMessage("Вы действительно хотите удалить записи из лога?")
                        .setCancelable(false)
                        .setPositiveButton("Да",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                       LogDelete(MyPreferenceActivity.this);
                                        Toast.makeText(getApplicationContext(), "Логи очищены", Toast.LENGTH_SHORT).show();
                                    }
                                })
                        .setNegativeButton("Нет",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.cancel();
                                    }
                                }).create().show();

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

    public static void LogDelete(Context c){
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(c.openFileOutput("PSType-log", MODE_PRIVATE)));
            bw.write(" ");
            bw.close();
            bw = new BufferedWriter(new OutputStreamWriter(c.openFileOutput("PSType-LatLng", MODE_PRIVATE)));
            bw.write(" ");
            bw.close();
            bw = new BufferedWriter(new OutputStreamWriter(c.openFileOutput("PSType-Accel", MODE_PRIVATE)));
            bw.write(" ");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    void readLog(){
        Intent intent = new Intent(this,debugging_log.class);
        startActivity(intent);
    }

    public void not (){
        SharedPreferences sp;
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean setting = sp.getBoolean("look", false);
        com.example.pstype_v1.useful.Notification not;

        SharedPreferences sPref = this.getSharedPreferences("SHARED_PREF_NAME", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        if (!setting) {
            startService(new Intent(this, tracking.class));
            not=new com.example.pstype_v1.useful.Notification(MyPreferenceActivity.this);
            not.Show();
            editor.putBoolean("MAP", true);
        }
        else {
            stopService(new Intent(this, tracking.class));
            not=new com.example.pstype_v1.useful.Notification(MyPreferenceActivity.this);
            not.NotShow();
            editor.putBoolean("MAP", false);
        }
        editor.apply();
    }
}
