package com.example.myapplication;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.view.WindowCallbackWrapper;
import androidx.core.app.NotificationCompat;
import androidx.core.view.ViewCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class RotationService extends Service implements View.OnClickListener {
    private WindowManager manager;
    private View view;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //WindowManager 생성
        manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        view = LayoutInflater.from(this).inflate(R.layout.moving_floating_button, null);


        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);

            params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
            params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR;
            manager.addView(view, params);


        view.findViewById(R.id.test).setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        params.x = (int)Math.abs (event.getRawX() - initialTouchX);
                        params.y = (int)Math.abs (event.getRawY() - initialTouchY);

                        Log.d("jms8732","update X: " + params.x + " Update Y : " + params.y);
                        manager.updateViewLayout(view, params);

                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = createNotification();

        startForeground(2, notification);
        return super.onStartCommand(intent, flags, startId);
    }

    /*
      블로그에서 나오는 방법으로 진행한다.
      단, 테스트를 진행하기 위해서 여러개의 notification channel이 존재하면 foreground Service가 동작을 안할 수 있다.
       */
    private Notification createNotification() {
        String channelId = "Rotation_ID";
        String channelName = "Rotation_Channel";
        String channelDescription = "Rotation example channel";

        Intent aIntent = new Intent(getApplicationContext(), customBottomSheet.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 101, aIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(channelDescription);
            channel.enableLights(true);

            manager.createNotificationChannel(channel);
        } else
            builder = new NotificationCompat.Builder(this);

        builder.setContentTitle("Rotation Service")
                .setContentText("Rotation Running")
                .setSmallIcon(R.drawable.screen_rotation_white)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pIntent)
                .setOngoing(false)
                .setAutoCancel(false)
                .setTicker("Running").build();


        return builder.build();
    }


    @Override
    public void onClick(View v) {
        Log.d("jms8732", "onClick...");
    }

    //최상단 뷰 삭제
    private void destroyLinear() {
        if (ViewCompat.isAttachedToWindow(view)) {
            manager.removeViewImmediate(view);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        destroyLinear();
        SharedPreferences prefs = getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("status", false);
        editor.apply();

        Intent intent = new Intent("com.example.ROTATION_ACTIVITY");
        sendBroadcast(intent);
    }

}
