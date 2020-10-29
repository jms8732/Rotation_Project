package com.example.myapplication;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.PathInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ExtraService extends Service {
    private WindowManager manager;
    private View mBackground, mfloating, mContent;
    private boolean isOpen = false, isRotation, onPoint = false;
    private RelativeLayout aBackground, aContent;
    private TextView number;
    private AudioManager audioManager;
    private ImageView imageView, fab1,fab2;
    private int init_volume = 0, init_bright = 0;
    private WindowManager.LayoutParams bParams, fParams, cParams;
    private Display display;
    private Point size;
    private int startX, startY;
    private int LAYOUT_FLAG;
    private AnimationDrawable animationDrawable, animationDrawable1; //Floating button 애니메이션
    private SharedPreferences prefs;

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        prefs = getSharedPreferences("pref", MODE_PRIVATE);
        isRotation = prefs.getBoolean("rotation",false);

        size = new Point();
        getSize();
        init_mBackground();
        init_mFloating();
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

    //조절 layout을 생성하는 메소드
    private void init_mBackground() {
        mBackground = LayoutInflater.from(this).inflate(R.layout.adjust_background, null);

        //초기 밝기와 볼륨을 얻는다.
        init_volume = convert_volume(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        init_bright = convert_bright(Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 0));

        //밝기, 볼륨을 나타내는 TextView 설정
        number = (TextView) mBackground.findViewById(R.id.number);
        number.setVisibility(View.INVISIBLE);

        //밝기, 볼륨을 나타내는 이미즤
        imageView = (ImageView) mBackground.findViewById(R.id.image);
        imageView.setVisibility(View.INVISIBLE);

        bParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        aBackground = (RelativeLayout) mBackground.findViewById(R.id.adjust_background);
        aBackground.setVisibility(View.INVISIBLE);

        aBackground.setOnTouchListener(new OnSwipeTouchListener(getApplicationContext()) {
            @Override
            public void onSwipeRight() {
                number.setVisibility(View.VISIBLE);
                number.setText(init_volume + "%");
                String vol = number.getText().toString().replace("%", "");

                imageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.sound_icon_white, null));
                imageView.setVisibility(View.VISIBLE);

                int current_vol = Integer.parseInt(vol);
                if (current_vol + 1 <= 100) {
                    number.setText(++current_vol + "%");
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) Math.round(current_vol * 0.15), AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_PLAY_SOUND);
                    init_volume = current_vol;
                }
            }

            @Override
            public void onSwipeLeft() {
                number.setVisibility(View.VISIBLE);
                number.setText(init_volume + "%");
                String vol = number.getText().toString().replace("%", "");

                imageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.sound_icon_white, null));
                imageView.setVisibility(View.VISIBLE);

                int current_vol = Integer.parseInt(vol);
                if (current_vol - 1 >= 0) {
                    number.setText(--current_vol + "%");
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) Math.round(current_vol * 0.15), AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_PLAY_SOUND);
                    init_volume = current_vol;
                }
            }

            @Override
            public void onSwipeTop() {
                number.setVisibility(View.VISIBLE);
                number.setText(String.valueOf(init_bright));
                int current_bright = Integer.parseInt(number.getText().toString());

                imageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.white_sun, null));
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

                imageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.white_sun, null));
                imageView.setVisibility(View.VISIBLE);

                if (current_bright - 1 >= 0) {
                    Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, (int) (--current_bright * 2.55f));
                    number.setText(String.valueOf(current_bright));
                    init_bright = current_bright;
                }
            }

            @Override
            public void invisible_view() {
                if (number.getVisibility() == View.VISIBLE) {
                    number.setVisibility(View.INVISIBLE);
                }
                imageView.setVisibility(View.INVISIBLE);
            }
        });

        manager.addView(mBackground, bParams); //조절 백그라운드 붙이기
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getSize();

        if (onPoint) {
            //Floating button이 Top | left에 있는 경우
            fParams.x = -size.x / 2;
            fParams.y = -size.y / 2;
            manager.updateViewLayout(mfloating, fParams);
        }
    }


    private void init_mFloating() {
        //floating button
        mfloating = LayoutInflater.from(this).inflate(R.layout.moving_floating_button, null);
        fParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT
                , WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        fParams.x = size.x / 2;
        fParams.y = size.y / 2;

        //floating 내용물 레이아웃
        cParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        //cParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;

        mContent = LayoutInflater.from(this).inflate(R.layout.moving_button_content, null);
        aContent = (RelativeLayout) mContent.findViewById(R.id.test);
        fab1 = (ImageView)mContent.findViewById(R.id.rotation);
        ((View)(fab1)).setAlpha(0);

        if(isRotation){
            fab1.setBackgroundResource(R.drawable.floating_button_rotation);
            fParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR;
        }else{
            fab1.setBackgroundResource(R.drawable.floating_button_rotation_lock);
            fParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }

        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isRotation){
                    //현재 로테이션인 경우
                    fParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR;
                    fab1.setBackgroundResource(R.drawable.floating_button_rotation_unlock_ani);
                }else{
                    fParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    fab1.setBackgroundResource(R.drawable.floating_button_rotation_lock_ani);
                }

                animationDrawable1 = (AnimationDrawable)fab1.getBackground();
                animationDrawable1.start();
                manager.updateViewLayout(mfloating,fParams);
                isRotation = !isRotation;
            }
        });

        fab2 = (ImageView)mContent.findViewById(R.id.settings);
        fab2.setBackgroundResource(R.drawable.floating_button_settings);
        ((View)(fab2)).setAlpha(0);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isOpen){
                    aBackground.setVisibility(View.VISIBLE);
                }else{
                    aBackground.setVisibility(View.GONE);
                }
                isOpen = !isOpen;
            }
        });

        final ImageView fab = (ImageView) mfloating.findViewById(R.id.fabIcon);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!onPoint) {
                    onPoint = true;
                    //4방향의 꼭지점 중 가까운 방향으로 이동
                    fab.setBackgroundResource(R.drawable.floating_button_expand);
                    animationDrawable = (AnimationDrawable)fab.getBackground();
                    animationDrawable.start();
                    int[] end_point = direction(fParams.x, fParams.y);

                    setGravity(end_point[0], end_point[1]);
                    move2Dimension(fParams.x, fParams.y, end_point[0], end_point[1], 650);
                } else {
                    onPoint = false;
                    moveFab(0, 0);

                    if(isOpen){
                        isOpen= false;
                        aBackground.setVisibility(View.GONE);
                    }

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            fab.setBackgroundResource(R.drawable.floating_button_collapse);
                            animationDrawable = (AnimationDrawable)fab.getBackground();
                            animationDrawable.start();
                            move2Dimension(fParams.x, fParams.y, startX, startY, 650);
                        }
                    }, 500);
                }
            }
        });

        //floating button 이동 리스너
        fab.setOnTouchListener(new View.OnTouchListener() {
            private int initX, initY;
            private float rawX, rawY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!onPoint) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            initX = fParams.x;
                            initY = fParams.y;
                            rawX = event.getRawX();
                            rawY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_UP:
                            break;

                        case MotionEvent.ACTION_MOVE:
                            fParams.x = initX + (int) (event.getRawX() - rawX);
                            fParams.y = initY + (int) (event.getRawY() - rawY);

                            manager.updateViewLayout(mfloating, fParams);
                            break;
                    }
                }
                return false;
            }
        });

        manager.addView(mContent, cParams);
        manager.addView(mfloating, fParams);
    }


    //방향을 결정하는 메소드
    private int[] direction(float startX, float startY) {
        int[] ret = new int[2];

        if (startY < 0 && startX < 0) { // TOP || LEFT
            ret[0] = -size.x / 2;
            ret[1] = -size.y / 2;
        } else if (startX > 0 && startY < 0) { //TOP || RIGHT
            ret[0] = size.x / 2;
            ret[1] = -size.y / 2;
        } else if (startX < 0 && startY > 0) {
            ret[0] = -size.x / 2;
            ret[1] = size.y / 2;
        } else {
            ret[0] = size.x / 2;
            ret[1] = size.y / 2;
        }

        return ret;
    }

    private void setGravity(float x, float y) {
        if (x < 0 && y < 0) {
            cParams.gravity = Gravity.TOP | Gravity.LEFT;
        } else if (x > 0 && y < 0) {
            cParams.gravity = Gravity.TOP | Gravity.RIGHT;
        } else if (x < 0 && y > 0) {
            cParams.gravity = Gravity.BOTTOM | Gravity.LEFT;
        } else {
            cParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        }

        aContent.setGravity(cParams.gravity);
        manager.updateViewLayout(mContent,cParams);
    }

    //floating button을 누른 후, 나오는 floating button들
    private void moveFab(int x, int y) {
        int g = aContent.getGravity();

        int size = aContent.getChildCount();
        final View[] f_array = new View[size];
        for (int i = 0; i < size; i++) {
            f_array[i] = aContent.getChildAt(i);
        }

        if (g == (Gravity.TOP | Gravity.LEFT)) {
        } else if (g == (Gravity.TOP | Gravity.RIGHT)) {
            x *= -1;
        } else if (g == (Gravity.BOTTOM | Gravity.LEFT)) {
            y *= -1;
        } else {
            y *= -1;
            x *= -1;
        }

        if (onPoint) {
            f_array[0].animate().setDuration(550).alpha(1).start();
            f_array[1].animate().setDuration(500).alpha(1).start();
        } else {
            f_array[0].animate().setDuration(550).alpha(0).start();
            f_array[1].animate().setDuration(500).alpha(0).start();
        }

        f_array[0].animate().setDuration(500).translationX(x).start();
        f_array[0].animate().setDuration(500).translationY(y / 3).start();
        f_array[1].animate().setDuration(500).translationX(x / 3).start();
        f_array[1].animate().setDuration(500).translationY(y).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if(!onPoint) {
                    aContent.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).start();
    }

    private void move2Dimension(int sx, int sy, final int ex, int ey, int duration) {
        startX = sx;
        startY = sy;

        //x좌표
        ValueAnimator x_ani = ValueAnimator.ofInt(sx, ex);
        x_ani.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int x = (int) animation.getAnimatedValue();
                fParams.x = x;
                manager.updateViewLayout(mfloating, fParams);

            }
        });

        //y좌표
        ValueAnimator y_ani = ValueAnimator.ofInt(sy, ey);
        y_ani.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int y = (int) animation.getAnimatedValue();
                fParams.y = y;
                manager.updateViewLayout(mfloating, fParams);
            }
        });

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (onPoint) { //각각의 꼭지점에 Floating Button이 존재하는 경우
                    aContent.setVisibility(View.VISIBLE);
                    moveFab(200,200);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });


        animatorSet.playTogether(x_ani, y_ani);
        animatorSet.setDuration(duration);

        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.start();
    }


    //초기 음량 변환
    private int convert_volume(int vol) {
        return (int) (vol * 100 / 15);
    }

    //초기 밝기 변환
    private int convert_bright(float bright) {
        return (int) (bright * 100 / 255);
    }

    //최상단 뷰 삭제
    private void destroyLinear() {
        if (ViewCompat.isAttachedToWindow(mfloating))
            manager.removeViewImmediate(mfloating);

        if (ViewCompat.isAttachedToWindow(mBackground))
            manager.removeViewImmediate(mBackground);

        if (ViewCompat.isAttachedToWindow(mContent))
            manager.removeViewImmediate(mContent);
    }

    //핸드폰의 크기를 확인하는 메소드
    private void getSize() {
        if (display == null) {
            display = manager.getDefaultDisplay();
        }

        if (Build.VERSION.SDK_INT >= 17)
            display.getRealSize(size);
        else
            display.getSize(size);

        Log.d("jms8732", "width: " + size.x + " height: " + size.y);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        destroyLinear();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("rotation", isRotation);
        editor.apply();

    }

}