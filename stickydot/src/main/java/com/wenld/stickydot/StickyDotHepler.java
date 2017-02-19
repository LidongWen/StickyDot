package com.wenld.stickydot;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.drawable.AnimationDrawable;
import android.support.v4.view.MotionEventCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * Created by wenld on 2017/2/17.
 */

public class StickyDotHepler implements View.OnTouchListener {
    private Context mContext;
    StickyDotView mStickyView;
    private View mShowView;
    private View mDragView;
    private int dragViewLayoutID;

    private WindowManager mWm;
    private WindowManager.LayoutParams paramsStickyView;
    private WindowManager.LayoutParams paramsDragView;


    private int mStatusBarHeight;
    private int mDragViewHeight;
    private int mDragViewWidth;

    private StickyListener outListener;


    /**
     * 是否可以被拖拽
     */
    private boolean isDraged = true;

    public StickyDotHepler(Context mContext, View mShowView, View mDragView) {

        this.mContext = mContext;
        this.mShowView = mShowView;
        this.mDragView = mDragView;
        init();

    }

    private void init() {

        mShowView.setOnTouchListener(this);

        mWm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mStickyView = new StickyDotView(mContext);

        if (mStatusBarHeight == 0)
            mStatusBarHeight = getStatusBarHeight(mContext);
        if (paramsStickyView == null) {
            paramsStickyView = new WindowManager.LayoutParams();
            paramsStickyView.format = PixelFormat.TRANSLUCENT;
        }
        if (paramsDragView == null) {
            paramsDragView = new WindowManager.LayoutParams();
            paramsDragView.format = PixelFormat.TRANSLUCENT;
            paramsDragView.height = WindowManager.LayoutParams.WRAP_CONTENT;
            paramsDragView.width = WindowManager.LayoutParams.WRAP_CONTENT;
            paramsDragView.gravity = Gravity.TOP | Gravity.LEFT;
        }
    }

    /**
     * 拖拽布局仅仅是文本 可直接初始化
     *
     * @param mContext
     * @param mShowView
     * @param dragViewLayoutID
     */
    public StickyDotHepler(Context mContext, View mShowView, int dragViewLayoutID) {

        this.mContext = mContext;
        this.mShowView = mShowView;
        this.dragViewLayoutID = dragViewLayoutID;

        mDragView = LayoutInflater.from(mContext).inflate(dragViewLayoutID, null, false);
        init();
        copyText();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);

        if (action == MotionEvent.ACTION_DOWN) {
            if (!isDraged)
                return false;
            ViewParent parent = v.getParent();
            if (parent == null) {
                return false;
            }
            parent.requestDisallowInterceptTouchEvent(true);

            initWindowsViewData();
            mShowView.setVisibility(View.GONE);
            //          开始添加的窗体让其显示

            mWm.addView(mStickyView, paramsStickyView);
            mWm.addView(mDragView, paramsDragView);
            mDragView.measure(1, 1);
            mDragViewHeight = mDragView.getMeasuredHeight() / 2;
            mDragViewWidth = mDragView.getMeasuredWidth() / 2;
            mStickyView.setDragStickViewListener(listener);
        }
        mStickyView.onTouchEvent(event);
        return true;
    }

    private void initWindowsViewData() {
        int[] points = new int[2];
        mShowView.getLocationInWindow(points);
        int x = points[0] + mShowView.getWidth() / 2;
        int y = points[1] + mShowView.getHeight() / 2;
        mStickyView.setStatusBarHeight(mStatusBarHeight);
        //初始化开始点
        mStickyView.setCenterPoint(x, y);
    }

    private DragStickViewListener listener = new DragStickViewListener() {
        @Override
        public void inRangeMove(PointF dragCanterPoint) {
            paramsDragView.x = (int) (dragCanterPoint.x - mDragViewWidth);
            paramsDragView.y = (int) (dragCanterPoint.y - mDragViewHeight - mStatusBarHeight);
            try {
                mWm.updateViewLayout(mDragView, paramsDragView);
            } catch (Exception e) {

            }
        }

        @Override
        public void outRangeUp(PointF dragCanterPoint) {
            if (mWm != null && mStickyView.getParent() != null && mDragView.getParent() != null) {
                mWm.removeView(mStickyView);
                mWm.removeView(mDragView);
            }
            //做其他的事情
            playAnim(dragCanterPoint);
        }

        @Override
        public void inRangeUp(PointF dragCanterPoint) {
            if (mWm != null && mStickyView.getParent() != null && mDragView.getParent() != null) {
                mWm.removeView(mStickyView);
                mWm.removeView(mDragView);
            }
            mShowView.setVisibility(View.VISIBLE);
        }
    };

    /**
     * 复制文本内容
     */
    private void copyText() {
        if (mShowView instanceof TextView && mDragView instanceof TextView) {
            ((TextView) mDragView).setText((((TextView) mShowView).getText().toString()));
        }
    }

    /**
     * 播放移除动画(帧动画)，这个过程根据个人喜好
     *
     * @param dragCanterPoint
     */
    private void playAnim(final PointF dragCanterPoint) {
        final ImageView imageView = new ImageView(mContext);
        imageView.setImageResource(R.drawable.out_anim);
        final AnimationDrawable mAnimDrawable = (AnimationDrawable) imageView
                .getDrawable();
//        这里得到的是其真实的大小，因为此时还得不到其测量值
        int intrinsicWidth = imageView.getDrawable().getIntrinsicWidth();
        int intrinsicHeight = imageView.getDrawable().getIntrinsicHeight();

        paramsDragView.x = (int) dragCanterPoint.x - intrinsicWidth / 2;
        paramsDragView.y = (int) dragCanterPoint.y - intrinsicHeight / 2 - mStatusBarHeight;
//      获取播放一次帧动画的总时长
        long duration = getAnimDuration(mAnimDrawable);

        mWm.addView(imageView, paramsDragView);
        mAnimDrawable.start();
//        由于帧动画不能定时停止，只能采用这种办法
        imageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mAnimDrawable.stop();
                imageView.clearAnimation();
                mWm.removeView(imageView);
                if (outListener != null)
                    outListener.outRangeUp(dragCanterPoint);
//                if (viewOutRangeUpRun != null) {
//                    viewOutRangeUpRun.run();
//                }
            }
        }, duration);
    }

    /**
     * 得到帧动画的摧毁时间
     *
     * @param mAnimDrawable
     * @return
     */
    private long getAnimDuration(AnimationDrawable mAnimDrawable) {
        long duration = 0;
        for (int i = 0; i < mAnimDrawable.getNumberOfFrames(); i++) {
            duration += mAnimDrawable.getDuration(i);
        }
        return duration;
    }

    /**
     * 获取状态栏高度
     *
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }

        return result;

    }


    public interface StickyListener {
        void outRangeUp(PointF dragCanterPoint);
    }

    public StickyDotHepler setOutListener(StickyListener outListener) {
        this.outListener = outListener;
        return this;
    }

    /**
     * 关闭
     */
    public void dismiss() {

        int[] points = new int[2];
        mShowView.getLocationInWindow(points);
        int x = points[0] + mShowView.getWidth() / 2;
        int y = points[1] + mShowView.getHeight() / 2;

        mShowView.setVisibility(View.GONE);
        listener.outRangeUp(new PointF(points[0], points[1]));
    }

    /**
     * 设置拖拽最大距离
     *
     * @param mFarthestDistance
     * @return
     */
    public StickyDotHepler setMaxDragDistance(float mFarthestDistance) {
        mStickyView.setmFarthestDistance(mFarthestDistance);
        return this;
    }

    /**
     * 设置颜色
     *
     * @param color
     * @return
     */
    public StickyDotHepler setColor(int color) {
        mStickyView.setPaintColor(color);
        return this;
    }

    /**
     * 是否可以被拖拽
     *
     * @param draged
     * @return
     */
    public StickyDotHepler setDraged(boolean draged) {
        isDraged = draged;
        return this;
    }
}
