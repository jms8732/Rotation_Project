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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.view.WindowCallbackWrapper;
import androidx.core.app.NotificationCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ExtraService extends Service implements View.OnClickListener {
    private WindowManager manager;
    private View view_floating, view_adjust;
    private boolean isOpen = false;
    private FloatingActionButton menu;
    private RelativeLayout relativeLayout;
    private TextView number;
    private AudioManager audioManager;
    private ImageView imageView;
    private int init_volume = 0, init_bright = 0;

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
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        setTheme(R.style.AppTheme);

        view_adjust = LayoutInflater.from(this).inflate(R.layout.adjust_background, null);

        //초기 밝기와 볼륨을 얻는다.
        init_volume = convert_volume(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        init_bright = convert_bright(Settings.System.getInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS,0));

        //밝기, 볼륨을 나타내는 TextView 설정
        number = (TextView)view_adjust.findViewById(R.id.number);
        number.setVisibility(View.INVISIBLE);

        //밝기, 볼륨을 나타내는 이미즤
        imageView = (ImageView)view_adjust.findViewById(R.id.image);
        imageView.setVisibility(View.INVISIBLE);

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
                number.setVisibility(View.VISIBLE);
                number.setText(init_volume+"%");
                String vol = number.getText().toString().replace("%", "");

                imageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.sound_icon_white,null));
                imageView.setVisibility(View.VISIBLE);

                int current_vol = Integer.parseInt(vol);
                if (current_vol + 1 <= 100) {
                    number.setText(++current_vol + "%");
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (current_vol * 0.15), AudioManager.FLAG_PLAY_SOUND);
                    init_volume = current_vol;
                }
            }

            @Override
            public void onSwipeLeft() {
                number.setVisibility(View.VISIBLE);
                number.setText(init_volume+"%");
                String vol = number.getText().toString().replace("%", "");

                imageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.sound_icon_white,null));
                imageView.setVisibility(View.VISIBLE);

                int current_vol = Integer.parseInt(vol);
                if (current_vol - 1 >= 0) {
                    number.setText(--current_vol + "%");
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (current_vol * 0.15), AudioManager.FLAG_PLAY_SOUND);
                    init_volume = current_vol;
                }
            }

            @Override
            public void onSwipeTop() {
                number.setVisibility(View.VISIBLE);
                number.setText(String.valueOf(init_bright));
                int current_bright = Integer.parseInt(number.getText().toString());

                imageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.white_sun,null));
                imageView.setVisibility(View.VISIBLE);

                if (current_bright + 1 <= 100) {
                    Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, (int) (++current_bright * 2.55f));
                    number.setText(String.valueOf(current_bright));
                    init_bright = current_bright;
                }

            }

            @Override
            public void onSwipeBottom() {
                number.setVisibility(View.VISIBLE);
                number.setText(String.valueOf(init_bright));
                int current_bright = Integer.parseInt(number.getText().toString());

                imageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.white_sun,null));
                imageView.setVisibility(View.VISIBLE);

                if (current_bright - 1 >= 0) {
                    Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, (int) (--current_bright * 2.55f));
                    number.setText(String.valueOf(current_bright));
                    init_bright = current_bright;
                }
            }

            @Override
            public void invisible_view() {
                if(number.getVisibility() == View.VISIBLE){
                    number.setVisibility(View.INVISIBLE);
                }
                imageView.setVisibility(View.INVISIBLE);
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

    //초기 음량 변환
    private int convert_volume(int vol) {
        return (int) (vol * 100 / 15);
    }

    //초기 밝기 변환
    private int convert_bright(float bright) {
        return (int) (bright * 100 / 255);
    }

    private void open() {
        isOpen = true;
        relativeLayout.setVisibility(View.VISIBLE);

        manager.updateViewLayout(view_adjust, layoutParams);
    }

    private void close() {
        isOpen = false;
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
