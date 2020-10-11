package com.example.myapplication;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class RotationService extends Service {
    private final int MAKE_FRAGMENT = 1;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /*
    블로그에서 나오는 방법으로 진행한다.
    단, 테스트를 진행하기 위해서 여러개의 notification channel이 존재하면 foreground service를 안할 수 있다.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("jms8732", "Service onStartCommand...");

        Notification notification = createNotification();

        startForeground(2, notification);
        autoOrientation(1);
        return super.onStartCommand(intent, flags, startId);
    }

    //포 그라운드를 위한 Notification 생성
    private Notification createNotification() {
        String channelId = "Rotation_ID";
        String channelName = "Rotation_Channel";
        String channelDescription = "Rotation example channel";

        Intent aIntent = new Intent(this, customBottomSheet.class);
        aIntent.putExtra("mode", MAKE_FRAGMENT);
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

    //자동 로테이션 메소드
    private void autoOrientation(int orientation){
        Settings.System.putInt(getContentResolver(),Settings.System.ACCELEROMETER_ROTATION,1); //자동 로테이션 시작
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Settings.System.putInt(getContentResolver(),Settings.System.ACCELEROMETER_ROTATION,0); //자동 로테이션 중지
        SharedPreferences prefs = getSharedPreferences("pref",MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("status",false);
        editor.apply();
    }

}
