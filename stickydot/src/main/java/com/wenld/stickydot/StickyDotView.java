package com.wenld.stickydot;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


/**
 * <p/>
 * Author: 温利东 on 2017/2/16 16:55.
 * blog: http://blog.csdn.net/sinat_15877283
 * github: https://github.com/LidongWen
 * <p>
 * 动画分析：
 * 1、由三部分组成  固定圆、拖拽圆、中间填充部分
 * 2、拖拽得越远 固定圆越小
 * 3、拖拽半径曾有超过一个阈值时 固定圆、贝塞尔区间消失
 * 4、松手：松手时拖拽半径超过阈值，显示一个爆炸效果
 * 5、松手：松手时拖拽半径不超过阈值 且 拖拽半径未曾超过阈值，出现弹效果
 * 6、松手：松手时拖拽半径不超过阈值 且 拖拽半径有曾超过阈值，还原位置
 *
 *
 */

public class StickyDotView extends View {
    private String TAG = "StickyDots";

    /**
     * 拖拽圆的圆心
     */
    private PointF pointFDragCenter = new PointF();
    /**
     * 固定圆的圆心
     */
    private PointF pointfFixCenter = new PointF();
    /**
     * 控制点
     */
    private PointF pointControl = new PointF();
    /**
     * 固定圆的切点
     */
    private PointF[] pointfsFixTangent = new PointF[]{new PointF(), new PointF()};

    /**
     * 拖拽圆的切点
     */
    private PointF[] pointFsDragTangent = new PointF[]{new PointF(), new PointF()};
    /**
     * 最大拖拽范围
     */
    private float mFarthestDistance;
    /**
     * 动画中固定员的最小半径
     */
    private float mMinFixRadius;
    /**
     * 动画中固定员的默认/最大半径
     */
    private float mMaxFixRadius;
    /**
     * 动画中固定圆半径的可伸缩的长度
     */
    private float mChangeFixRadius;
    /**
     * 拖拽圆半径
     */
    private float mDragRadius;
    /**
     * 拖拽距离
     */
    private float mDraglength;
    /**
     * 固定圆半径
     */
    private float mFixRadius;
    /**
     * 超出范围
     */
    private boolean isOut;
    private boolean isOutUp;
    /**
     * 是否touch
     */
    private boolean isTouch;


    Paint paint;
    Path pathBezier;
    private int mStatusBarHeight;

    StickyAnimUtil stickyAnimUtil;

    public void setDragStickViewListener(DragStickViewListener dragStickViewListener) {
        this.dragStickViewListener = dragStickViewListener;
    }

    private DragStickViewListener dragStickViewListener;

    public StickyDotView(Context context) {
        this(context, null);
    }

    public StickyDotView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initValue();
    }


    public void initValue() {
        paint = new Paint();
        paint.setColor(Color.parseColor("#FF4081"));
        paint.setAntiAlias(true);

        pathBezier = new Path();

        mMaxFixRadius = mFixRadius = DensityUtils.dip2px(getContext(), 8);
        mMinFixRadius = DensityUtils.dip2px(getContext(), 3);
        mChangeFixRadius = mMaxFixRadius - mMinFixRadius;

        mFarthestDistance = DensityUtils.dip2px(getContext(), 80);

        mDragRadius = DensityUtils.dip2px(getContext(), 8);
        pointFDragCenter = new PointF();

        pointfFixCenter = new PointF();

        stickyAnimUtil = new StickyAnimUtil(animatorUpdateListener, animatorListener);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(0, -mStatusBarHeight);
        if (isTouch) {
            if (!isOut) {
                //画固定圆
                canvas.drawCircle(pointfFixCenter.x, pointfFixCenter.y, mFixRadius, paint);
                //画贝塞尔曲线
                pathBezier.reset();
                pathBezier.moveTo(pointfsFixTangent[0].x, pointfsFixTangent[0].y);
                pathBezier.quadTo(pointControl.x, pointControl.y, pointFsDragTangent[0].x, pointFsDragTangent[0].y);
                pathBezier.lineTo(pointFsDragTangent[1].x, pointFsDragTangent[1].y);
                pathBezier.quadTo(pointControl.x, pointControl.y, pointfsFixTangent[1].x, pointfsFixTangent[1].y);
                pathBezier.close();
                canvas.drawPath(pathBezier, paint);
            }
        }
        if (!isOutUp) {
            //画拖拽圆
//                canvas.drawCircle(pointFDragCenter.x, pointFDragCenter.y, mDragRadius, paint);
        }
        canvas.restore();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (stickyAnimUtil.isRunning())
            return true;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                isTouch = true;
                processData(event.getRawX(), event.getRawY());
                invalidate();
                if (dragStickViewListener != null)
                    dragStickViewListener.inRangeMove(pointFDragCenter);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (isTouch) {
                    processData(event.getRawX(), event.getRawY());
                    invalidate();
                    if (dragStickViewListener != null)
                        dragStickViewListener.inRangeMove(pointFDragCenter);
                }
                break;
            }

            case MotionEvent.ACTION_UP: {
                if (isTouch) {
                    isTouch = false;
                    Up();
                }
                break;
            }
        }
        return true;
    }


    float dx;
    float dy;
    float cosA, sinA;// 角度的 tanA sin cos;

    /**
     * 计算拖拽的距离、固定圆的半径、是否出了最大拖拽范围、控制点位置 、 计算几个切点的位置
     */
    private void processData(float touchX, float touchY) {
        pointFDragCenter.x = touchX;
        pointFDragCenter.y = touchY;

//        GraphicsUtil.process(pointFDragCenter, pointfFixCenter,dx,dy,mDraglength,cosA, sinA);
        //计算拖拽的距离
        dx = touchX - pointfFixCenter.x;
        dy = touchY - pointfFixCenter.y;
        mDraglength = (float) Math.sqrt(Math.pow(dx, 2.0D) + Math.pow((double) dy, 2.0D));
        cosA = dx / mDraglength;
        sinA = dy / mDraglength;

        if (mDraglength >= mFarthestDistance) {
            if (!isOut)
                isOut = true;
        } else {
            processFixRadius(mDraglength);
            processTangent(cosA, sinA);
        }
    }

    //计算固定圆的半径
    private void processFixRadius(float mDraglength) {
        mFixRadius = (mFarthestDistance - mDraglength) / mFarthestDistance * mChangeFixRadius + mMinFixRadius;
    }

    //计算切点 控制点
    private void processTangent(float cosA, float sinA) {
        //固定圆 切点
        pointfsFixTangent[0].set(pointfFixCenter.x + mFixRadius * sinA, pointfFixCenter.y - mFixRadius * cosA);
        pointfsFixTangent[1].set(pointfFixCenter.x - mFixRadius * sinA, pointfFixCenter.y + mFixRadius * cosA);

        // 拖拽圆切点
        pointFsDragTangent[0].set(pointFDragCenter.x + mDragRadius * sinA, pointFDragCenter.y - mDragRadius * cosA);
        pointFsDragTangent[1].set(pointFDragCenter.x - mDragRadius * sinA, pointFDragCenter.y + mDragRadius * cosA);

        //控制点位置
        pointControl.set(pointfFixCenter.x + dx / 2, pointfFixCenter.y + dy / 2);
    }

    private void Up() {
        if (mDraglength >= mFarthestDistance) {
            isOutUp = true;
            invalidate();
            if (dragStickViewListener != null)
                dragStickViewListener.outRangeUp(pointFDragCenter);
        } else {
            if (!isOut) {
                if (stickyAnimUtil != null) {
                    stickyAnimUtil.palyAnim();
                }
            } else {
                resetValue();
                invalidate();
                if (dragStickViewListener != null)
                    dragStickViewListener.inRangeUp(pointFDragCenter);
            }
        }
    }


    ValueAnimator.AnimatorUpdateListener animatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float currentValue = (float) animation.getAnimatedValue();

            pointFDragCenter.x = pointfFixCenter.x + dx * currentValue;
            pointFDragCenter.y = pointfFixCenter.y + dy * currentValue;
//            processFixRadius(Math.abs(mDraglength * currentValue));
//            if (currentValue >= 0) {
//                processTangent(cosA, sinA);
//            } else {
//                processTangent(-cosA, -sinA);
//            }
            invalidate();

            if (dragStickViewListener != null)
                dragStickViewListener.inRangeMove(pointFDragCenter);
        }
    };
    Animator.AnimatorListener animatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            resetValue();
            invalidate();
            if (dragStickViewListener != null)
                dragStickViewListener.inRangeUp(pointFDragCenter);
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };

    /**
     * 重置为初始状态的数据
     */
    private void resetValue() {
        mFixRadius = mMaxFixRadius;
        isTouch = false;
        isOut = false;
        isOutUp = false;
    }

    public void setCenterPoint(float x, float y) {
        pointfFixCenter.set(x, y);
//        //初始化时 固定圆的可触摸范围
//        regionCircle = new Region((int) (pointfFixCenter.x - mMaxFixRadius), (int) (pointfFixCenter.y - mMaxFixRadius), (int) (pointfFixCenter.x + mMaxFixRadius), (int) (pointfFixCenter.y + mMaxFixRadius));
    }

    /**
     * 设置状态栏高度，最好外面传进来，当view还没有绑定到窗体的时候是测量不到的
     *
     * @param mStatusBarHeight
     */
    public void setStatusBarHeight(int mStatusBarHeight) {
        this.mStatusBarHeight = mStatusBarHeight;
    }

    public void setmFarthestDistance(float mFarthestDistance) {
        this.mFarthestDistance = mFarthestDistance;
    }

    public void setPaintColor(int color) {
        paint.setColor(color);
    }
}
