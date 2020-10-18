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
import android.widget.Toast;

import androidx.core.view.ViewCompat;

//스와이프
public class OnSwipeTouchListener implements View.OnTouchListener {
    private final GestureDetector gestureDetector;

    public OnSwipeTouchListener(Context ctx) {
        gestureDetector = new GestureDetector(ctx, new GestureListener());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_UP) {
            invisible_view();
            return true;
        }
        return gestureDetector.onTouchEvent(event);
    }



    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 50;
        private float startX , startY;
        @Override
        public boolean onDown(MotionEvent e) {
            startX = e.getX();
            startY = e.getY();

            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) { boolean result = false;
            try {
                float diffY = e2.getY() - startY;
                float diffX = e2.getX() - startX;

                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                        startX = e2.getX();
                        startY = e2.getY();
                        result = true;
                    }
                } else if (Math.abs(diffY) > SWIPE_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeBottom();
                    } else {
                        onSwipeTop();
                    }
                    startX = e2.getX();
                    startY = e2.getY();
                    result = true;
                }

            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }



    //현재 보여지는 뷰 안보이게
    public void invisible_view(){

    }
    //음량 조절절
    public void onSwipeRight() {
    }

    public void onSwipeLeft() {
    }

    public void onSwipeTop() {
    }

    public void onSwipeBottom() {
    }

}

