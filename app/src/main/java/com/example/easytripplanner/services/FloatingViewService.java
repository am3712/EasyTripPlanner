package com.example.easytripplanner.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.easytripplanner.R;

public class FloatingViewService extends Service {
    private WindowManager mWindowManager;
    private View mFloatingView;
    private int dpHeight;
    private int dpWidth;
    private int viewWidth;
    private int viewHeight;
    private static final String TAG = "FloatingViewService";

    public FloatingViewService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("InflateParams")
    @Override
    public void onCreate() {
        super.onCreate();
        //Inflate the floating view layout we created
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null);

        int layoutParams;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            layoutParams = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams = WindowManager.LayoutParams.TYPE_PHONE;
        }

        //Add the view to the window.
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutParams,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        //Specify the view position
        params.gravity = Gravity.TOP | Gravity.START;        //Initially view will be added to top-left corner
        params.x = 0;
        params.y = 100;


        //Add the view to the window
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mFloatingView, params);


        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        dpHeight = displayMetrics.heightPixels;
        dpWidth = displayMetrics.widthPixels;

        ViewTreeObserver vto = mFloatingView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mFloatingView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                viewWidth = mFloatingView.getMeasuredWidth();
                viewHeight = mFloatingView.getMeasuredHeight();
            }
        });


        //The root element of the expanded view layout
        final View expandedView = mFloatingView.findViewById(R.id.expanded_container);


        //Set the close button
        ImageView closeButtonCollapsed = (ImageView) mFloatingView.findViewById(R.id.close_btn);
        closeButtonCollapsed.setOnClickListener(view -> {
            //close the service and remove the from from the window
            stopSelf();
        });


        //Drag and move floating view using user's touch action.
        mFloatingView.findViewById(R.id.collapse_view).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private boolean isMoving;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.performClick();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //remember the initial position.
                        initialX = params.x;
                        initialY = params.y;

                        //get the touch location
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();

                        return true;

                    case MotionEvent.ACTION_MOVE:
                        //Calculate the X and Y coordinates of the view.
                        int newX = initialX + (int) (event.getRawX() - initialTouchX);
                        int newY = initialY + (int) (event.getRawY() - initialTouchY);

                        if (newX + viewWidth > dpWidth)
                            newX = dpWidth - viewWidth;
                        else if (newX < 0) {
                            newX = 0;
                        }

                        if (newY + viewHeight > dpHeight)
                            newY = dpHeight - viewHeight;
                        else if (newY < 0) {
                            newY = 0;
                        }

                        params.x = newX;
                        params.y = newY;

                        //Update the layout with new X & Y coordinate
                        mWindowManager.updateViewLayout(mFloatingView, params);
                        isMoving = true;
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (!isMoving) {
                            if (isViewCollapsed())
                                expandedView.setVisibility(View.VISIBLE);
                            else
                                expandedView.setVisibility(View.GONE);
                            return true;
                        }
                        isMoving = false;
                }
                return false;
            }
        });
    }

    /**
     * Detect if the floating view is collapsed or expanded.
     *
     * @return true if the floating view is collapsed.
     */
    private boolean isViewCollapsed() {
        return mFloatingView == null || mFloatingView.findViewById(R.id.expanded_container).getVisibility() == View.GONE;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatingView != null) mWindowManager.removeView(mFloatingView);
    }
}