package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button button;
    private boolean check;
    private ActivityManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (manager == null)
            manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        //현재 Foreground 서비스가 진행되고 있는 지 확인하는 반복문
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ExtraService.class.getName().equals(service.service.getClassName())) {
                check = true;
            }
        }

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);

        if(!check){
            button.setText("Start Service");
        }else
            button.setText("Stop Service");

        //화면 전환을 permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //오버레이 권한 설정
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + this.getPackageName()));
                startActivity(intent);

            }

            //시스템에 쓰기 위한
            if (!Settings.System.canWrite(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + this.getPackageName()));
                startActivity(intent);
            }
        } else {
            Intent intent1 = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + this.getPackageName()));
            startActivity(intent1);

            //시스템에 쓰기 위한
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + this.getPackageName()));
            startActivity(intent);

        }

    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, ExtraService.class);
        if (!check) {
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(intent);
                finishAndRemoveTask();
            } else {
                startService(intent);
                finishAndRemoveTask();
            }
        }else {
            stopService(intent);
            button.setText("Start Service");
            check = false;
        }
    }

}