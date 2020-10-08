package com.example.myapplication;

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

        getWindow().setGravity(Gravity.BOTTOM);

    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            int x = (int) ev.getXPrecision();
            int y = (int) ev.getYPrecision();

            if (Build.VERSION.SDK_INT >= 18) {
                Rect kill_rect = new Rect();
                if(kill.getGlobalVisibleRect(kill_rect)){
                    //서비스 종료
                    if(kill_rect.contains(x,y)){
                        Toast.makeText(this, "Kill...", Toast.LENGTH_SHORT).show();
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
        Log.d("jms8732","dialog destroy...");


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}
