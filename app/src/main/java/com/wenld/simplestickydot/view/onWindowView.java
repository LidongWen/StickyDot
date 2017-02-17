package com.wenld.simplestickydot.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

/**
 * <p/>
 * Author: 温利东 on 2017/2/17 14:26.
 * blog: http://blog.csdn.net/sinat_15877283
 * github: https://github.com/LidongWen
 */

public class onWindowView extends View {
    /**
     * 拖拽的view
     */
    private View mDragView;
    private WindowManager mWm;

    private int mDragViewHeight;
    private int mDragViewWidth;
    private int mStatusBarHeight;

    private DragStickViewListener dragStickViewListener;

    private WindowManager.LayoutParams mParams;

    private PointF pointF = new PointF();


    public onWindowView(Context context, View mDragView, WindowManager mWm) {
        super(context);
        this.mDragView = mDragView;
        this.mWm = mWm;
        init();
    }


    private void init() {
        mDragView.measure(1, 1);
        mDragViewHeight = mDragView.getMeasuredHeight() / 2;
        mDragViewWidth = mDragView.getMeasuredWidth() / 2;

        mParams = new WindowManager.LayoutParams();
        mParams.format = PixelFormat.TRANSLUCENT;
        mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.gravity = Gravity.TOP | Gravity.LEFT;
    }

    /**
     * 设置状态栏高度，最好外面传进来，当view还没有绑定到窗体的时候是测量不到的
     *
     * @param mStatusBarHeight
     */
    public void setStatusBarHeight(int mStatusBarHeight) {
        this.mStatusBarHeight = mStatusBarHeight;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        //      需要去除状态栏高度偏差
        canvas.translate(0, -mStatusBarHeight);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE: {

                setPointF((int) event.getRawX(), (int) event.getRawY());

//                invalidate();
                break;
            }
            case MotionEvent.ACTION_UP:
                dragStickViewListener.outRangeUp(pointF);
                break;
        }
        return true;
    }

    public void setPointF(int x, int y) {
        pointF.set(x, y);

        mParams.x = (int) (x - mDragViewWidth);
        mParams.y = (int) (y - mDragViewHeight - mStatusBarHeight);
        try {
            mWm.updateViewLayout(mDragView, mParams);
        } catch (Exception e) {

        }
    }

    /**
     * 拖拽过程监听接口
     */
    public interface DragStickViewListener { /**
     * 在范围内移动回调
     * @param dragCanterPoint 拖拽的中心坐标
     */
    void inRangeMove(PointF dragCanterPoint);
        /**
         * 在范围外移动回调
         * @param dragCanterPoint 拖拽的中心坐标
         */
        void outRangeMove(PointF dragCanterPoint);
        /**
         *  当移出了规定范围，最后在范围内松手的回调
         * @param dragCanterPoint
         */
        void out2InRangeUp(PointF dragCanterPoint);
        /**
         * 当移出了规定范围，最后在范围外松手的回调
         * @param dragCanterPoint
         */
        void outRangeUp(PointF dragCanterPoint);
        /**
         * 一直没有移动出范围，在范围内松手的回调
         * @param dragCanterPoint
         */
        void inRangeUp(PointF dragCanterPoint);
    }
    public DragStickViewListener getDragStickViewListener() {
        return dragStickViewListener;
    }

    public void setDragStickViewListener(DragStickViewListener dragStickViewListener) {
        this.dragStickViewListener = dragStickViewListener;
    }
}
