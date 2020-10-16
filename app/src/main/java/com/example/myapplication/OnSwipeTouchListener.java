package com.example.myapplication;

import android.content.Context;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.TextView;

import androidx.core.view.ViewCompat;

//스와이프
public class OnSwipeTouchListener implements View.OnTouchListener {
    private final GestureDetector gestureDetector;
    private Context context;
    private Display display;

    public OnSwipeTouchListener(Context ctx) {
        context = ctx;
        gestureDetector = new GestureDetector(ctx, new GestureListener());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > 0) {
                        int speed = speed(Math.abs(diffX));
                        if (diffX > 0) {
                            onSwipeRight(speed);
                        } else {
                            onSwipeLeft(speed);
                        }
                    }
                    result = true;
                } else if (Math.abs(diffY) > 0) {
                    int speed = speed(Math.abs(diffY));
                    if (diffY > 0) {
                        onSwipeBottom(speed);
                    } else {
                        onSwipeTop(speed);
                    }
                }
                result = true;

            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }

    //가속도에 따라 반환되는 스피드
    private int speed(float diff) {
        Log.d("jms8732","Diff : " + diff);
        int ret =0 ;
        if(diff >= 100){
            ret = (int)(diff/100);

            if(ret >= 10)
                ret = 10;
        }

        return ret;
    }

    public void onSwipeRight(int speed) {
    }

    public void onSwipeLeft(int speed) {
    }

    public void onSwipeTop(int speed) {
    }

    public void onSwipeBottom(int speed) {
    }

    //TextView에 보여지는 화면
    public void showView(final TextView textView, int speed, boolean plus){
        if(plus) {
            new AsyncTask<Integer, Integer, Integer>() {

                @Override
                protected void onPostExecute(Integer integer) {
                    textView.setText(String.valueOf(integer));
                    Settings.System.putInt(context.getContentResolver(),Settings.System.SCREEN_BRIGHTNESS,integer);
                }

                @Override
                protected void onProgressUpdate(Integer... values) {
                    int val = values[0];
                    Settings.System.putInt(context.getContentResolver(),Settings.System.SCREEN_BRIGHTNESS,val);
                    textView.setText(String.valueOf(val));
                }

                @Override
                protected Integer doInBackground(Integer... integers) {
                    int temp = integers[0];
                    int start = Integer.parseInt(textView.getText().toString()); //초기 시작
                    for (int i = 0; i < temp; i++) {
                        publishProgress(start++);
                        SystemClock.sleep(100);
                    }

                    return start;
                }
            }.execute(speed);
        }else{
            new AsyncTask<Integer, Integer, Integer>() {

                @Override
                protected void onPostExecute(Integer integer) {
                    textView.setText(String.valueOf(integer));
                    Settings.System.putInt(context.getContentResolver(),Settings.System.SCREEN_BRIGHTNESS,integer);
                }

                @Override
                protected void onProgressUpdate(Integer... values) {
                    int val = values[0];
                    Settings.System.putInt(context.getContentResolver(),Settings.System.SCREEN_BRIGHTNESS,val);
                    textView.setText(String.valueOf(val));
                }

                @Override
                protected Integer doInBackground(Integer... integers) {
                    int temp = integers[0];
                    int start = Integer.parseInt(textView.getText().toString()); //초기 시작
                    for (int i = 0; i < temp; i++) {
                        publishProgress(start--);
                        SystemClock.sleep(100);
                    }

                    return start;
                }
            }.execute(speed);
        }
    }
}

