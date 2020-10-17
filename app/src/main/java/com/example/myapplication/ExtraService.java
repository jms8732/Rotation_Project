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
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.view.WindowCallbackWrapper;
import androidx.core.app.NotificationCompat;
import androidx.core.view.ViewCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ExtraService extends Service implements View.OnClickListener {
    private WindowManager manager;
    private View view_floating, view_adjust;
    private boolean isOpen = false;
    private FloatingActionButton menu;
    private RelativeLayout relativeLayout;
    private TextView title, bright;

    private WindowManager.LayoutParams params = null, layoutParams = null;

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

        setTheme(R.style.AppTheme);

        view_adjust = LayoutInflater.from(this).inflate(R.layout.adjust_background, null);
        title = (TextView) view_adjust.findViewById(R.id.title);
        bright = (TextView) view_adjust.findViewById(R.id.bright);

        bright.setText(String.valueOf(convert_bright(Settings.System.getFloat(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 0.0f))));
        layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        relativeLayout = (RelativeLayout) view_adjust.findViewById(R.id.adjust_background);
        relativeLayout.setVisibility(View.INVISIBLE);

        relativeLayout.setOnTouchListener(new OnSwipeTouchListener(getApplicationContext()) {
            @Override
            public void onSwipeRight() {
            }

            @Override
            public void onSwipeLeft() {
            }

            @Override
            public void onSwipeTop() {
                int current_bright = Integer.parseInt(bright.getText().toString());
                if(current_bright + 1 <= 100) {
                    Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, (int) (++current_bright * 2.55f));
                    bright.setText(String.valueOf(current_bright));
                }

            }

            @Override
            public void onSwipeBottom() {
                int current_bright = Integer.parseInt(bright.getText().toString());
                if(current_bright -1 >= 0 ) {
                    Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, (int) (--current_bright * 2.55f));
                    bright.setText(String.valueOf(current_bright));
                }
            }

        });


        manager.addView(relativeLayout, layoutParams); //조절 백그라운드 붙이기

        view_floating = LayoutInflater.from(this).inflate(R.layout.moving_floating_button, null);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR;
        menu = (FloatingActionButton) view_floating.findViewById(R.id.menu);

        menu.setOnClickListener(this);
        menu.setOnTouchListener(new View.OnTouchListener() { //floating 버튼 움직임
            private int initX, initY;
            private float rawX, rawY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initX = params.x;
                        initY = params.y;
                        rawX = event.getRawX();
                        rawY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_UP:
                        break;

                    case MotionEvent.ACTION_MOVE:
                        params.x = initX + (int) (rawX - event.getRawX());
                        params.y = initY + (int) (rawY - event.getRawY());

                        manager.updateViewLayout(view_floating, params);
                        break;
                }

                return false;
            }
        });

        manager.addView(view_floating, params);
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
        if (v == menu) {
            if (!isOpen) {
                open();
            } else
                close();
        }
    }

    private int convert_bright(float bright) {
        return (int) (bright * 100 / 255);
    }

    private void open() {
        isOpen = true;
        Log.d("jms8732", "Open..");
        relativeLayout.setVisibility(View.VISIBLE);

        manager.updateViewLayout(view_adjust, layoutParams);
    }

    private void close() {
        isOpen = false;
        Log.d("jms8732", "Close...");
        relativeLayout.setVisibility(View.INVISIBLE);

        manager.updateViewLayout(view_adjust, layoutParams);
    }

    //최상단 뷰 삭제
    private void destroyLinear() {
        if (ViewCompat.isAttachedToWindow(view_floating)) {
            manager.removeViewImmediate(view_floating);
        }

        if (ViewCompat.isAttachedToWindow(view_adjust))
            manager.removeViewImmediate(view_adjust);

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
