package com.example.myapplication;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.net.Inet4Address;


public class customBottomSheet extends AppCompatActivity implements View.OnClickListener{
    Button kill, cancel;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_bottom_sheet);

        kill = (Button) findViewById(R.id.kill);
        kill.setOnClickListener(this);
        cancel = (Button) findViewById(R.id.cancel);
        cancel.setOnClickListener(this);
        getWindow().setGravity(Gravity.BOTTOM); //액티비티를 바닥에 표시

    }

    @Override
    public void onClick(View v) {
        if(v == kill){
            //서비스 종료
            Intent intent = new Intent(this,RotationService.class);
            stopService(intent);
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}
