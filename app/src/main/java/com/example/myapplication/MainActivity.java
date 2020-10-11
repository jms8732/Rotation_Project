package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.KeyEventDispatcher;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Switch plug;
    SharedPreferences prefs;
    boolean checked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("jms8732", "Activity onCreate...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        plug = (Switch) findViewById(R.id.plug);
        plug.setOnClickListener(this);
        prefs = getSharedPreferences("pref",MODE_PRIVATE);

        Log.d("jms8732", "Toggle: " + checked);

        checked = prefs.getBoolean("status", false);

        plug.setChecked(checked);

        //화면 전환을 permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!Settings.System.canWrite(this)) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }

    }

    @Override
    public void onClick(View v) {
        if (v == plug) {
            SharedPreferences.Editor editor = prefs.edit();
            Intent intent = new Intent(this, RotationService.class);

            checked = prefs.getBoolean("status", false);

            if (!checked) { //스위치를 키는 경우
                checked = true;
                if (Build.VERSION.SDK_INT >= 26)
                    startForegroundService(intent);
                else
                    startService(intent);
            } else {
                checked = false;
                stopService(intent);
            }

            plug.setChecked(checked);
            editor.putBoolean("status", checked);
            editor.apply();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}