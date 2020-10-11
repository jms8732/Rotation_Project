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


public class customBottomSheet extends AppCompatActivity {
    Button kill, cancel;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_bottom_sheet);

        kill = (Button) findViewById(R.id.kill);
        cancel = (Button) findViewById(R.id.cancel);

        getWindow().setGravity(Gravity.BOTTOM); //액티비티를 바닥에 표시

    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            int x = (int) ev.getXPrecision();
            int y = (int) ev.getYPrecision();

            if (Build.VERSION.SDK_INT >= 18) {
                Rect kill_rect = new Rect();
                if(kill.getLocalVisibleRect(kill_rect)){ //컴포넌트의 위치를 파악하기 위한 메소드
                    //서비스 종료
                    if(kill_rect.contains(x,y)){
                        Intent  intent = new Intent(this,RotationService.class);
                        stopService(intent); //서비스 종료
                    }
                }
                //컴포넌트 종료
                finish();
            }
        }

        return super.dispatchTouchEvent(ev);
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
