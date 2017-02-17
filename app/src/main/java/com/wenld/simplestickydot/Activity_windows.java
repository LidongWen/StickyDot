package com.wenld.simplestickydot;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.TextView;

import com.wenld.commontools.ScreenUtils;
import com.wenld.simplestickydot.view.onWindowView;

/**
 * <p/>
 * Author: 温利东 on 2017/2/17 14:39.
 * blog: http://blog.csdn.net/sinat_15877283
 * github: https://github.com/LidongWen
 */

public class Activity_windows extends Activity {
    private TextView tv_activity_windows;

    onWindowView mStickyView;
    private View mDragView;

    private WindowManager mWm;
    private WindowManager.LayoutParams mParams;

    private int mStatusBarHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_windows);
        initView();
        listener();
    }

    private void initView() {
        tv_activity_windows = (TextView) findViewById(R.id.tv_activity_windows);

        mWm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mStatusBarHeight = ScreenUtils.getStatusBarHeight(this);
        mDragView = LayoutInflater.from(this).inflate(R.layout.include_view, null, false);
        mStickyView = new onWindowView(this, mDragView, mWm);
        mParams = new WindowManager.LayoutParams();
        mParams.format = PixelFormat.TRANSLUCENT;
    }

    private void listener() {
        tv_activity_windows.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = MotionEventCompat.getActionMasked(event);

                if (action == MotionEvent.ACTION_DOWN) {
                    ViewParent parent = v.getParent();
                    if (parent == null) {
                        return false;
                    }
//                    parent.requestDisallowInterceptTouchEvent(true);

                    initWindowsViewData();
                    mStickyView.setDragStickViewListener(new onWindowView.DragStickViewListener() {
                        @Override
                        public void inRangeMove(PointF dragCanterPoint) {

                        }

                        @Override
                        public void outRangeMove(PointF dragCanterPoint) {

                        }

                        @Override
                        public void out2InRangeUp(PointF dragCanterPoint) {

                        }

                        @Override
                        public void outRangeUp(PointF dragCanterPoint) {
                            if (mWm != null && mStickyView.getParent() != null && mDragView.getParent() != null) {
                                mWm.removeView(mStickyView);
                                mWm.removeView(mDragView);
                            }
                        }

                        @Override
                        public void inRangeUp(PointF dragCanterPoint) {

                        }
                    });
                    //          开始添加的窗体让其显示
                    mWm.addView(mStickyView, mParams);
                    mWm.addView(mDragView, mParams);
                }
                mStickyView.onTouchEvent(event);
                return true;
            }
        });
    }

    private void initWindowsViewData() {
        int[] points = new int[2];
        tv_activity_windows.getLocationInWindow(points);
        int x = points[0] + tv_activity_windows.getWidth() / 2;
        int y = points[1] + tv_activity_windows.getHeight() / 2;
        mStickyView.setStatusBarHeight(mStatusBarHeight);
        //初始化开始点
        mStickyView.setPointF(x, y);
    }


}
