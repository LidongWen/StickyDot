package com.wenld.simplestickydot.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.wenld.customviewsupport.DensityUtils;

/**
 * <p/>
 * Author: 温利东 on 2017/2/16 16:55.
 * blog: http://blog.csdn.net/sinat_15877283
 * github: https://github.com/LidongWen
 * <p>
 * 动画分析：
 * 1、由三部分组成  固定圆、拖拽圆、中间填充部分
 * 2、拖拽得越远 固定圆越小
 * 3、拖拽半径超过一个阈值时 固定圆消失
 * 4、拖拽半径超过一个阈值时松手，显示一个爆炸效果
 * 5、拖拽半径不超过阈值松手，出现弹效果
 */

public class StickyDots_001 extends View {
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

    /**
     * 在超出范围的地方松手
     */
    private boolean isOutUp;

    /**
     * 是否touch
     */
    private boolean isTouch;

    /**
     * 固定圆初始化时的范围
     */
    Region regionCircle;

    Paint paint;
    Path pathBezier;

    public StickyDots_001(Context context) {
        this(context, null);
    }

    public StickyDots_001(Context context, AttributeSet attrs) {
        super(context, attrs);
        initValue();
    }


    public void initValue() {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setAntiAlias(true);

        pathBezier = new Path();

        mMaxFixRadius = mFixRadius = DensityUtils.dip2px(getContext(), 8);
        mMinFixRadius = DensityUtils.dip2px(getContext(), 3);
        mChangeFixRadius = mMaxFixRadius - mMinFixRadius;

        mFarthestDistance = DensityUtils.dip2px(getContext(), 80);

        mDragRadius = DensityUtils.dip2px(getContext(), 8);
        pointFDragCenter = new PointF(touchX, touchY);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        pointfFixCenter = new PointF(getWidth(), getHeight());
        //初始化时 固定圆的可触摸范围
        regionCircle = new Region((int) (pointfFixCenter.x - mMaxFixRadius), (int) (pointfFixCenter.y - mMaxFixRadius), (int) (pointfFixCenter.x + mMaxFixRadius), (int) (pointfFixCenter.y + mMaxFixRadius));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        if (!isOutUp) {
            if (!isOut) {
                //画固定圆
                canvas.drawCircle(pointfFixCenter.x, pointfFixCenter.y, mFixRadius, paint);
            }
            if (isTouch) {
                if (!isOut) {
                    //画贝塞尔曲线
                    pathBezier.reset();
                    pathBezier.moveTo(pointfsFixTangent[0].x, pointfsFixTangent[0].y);
                    pathBezier.quadTo(pointControl.x, pointControl.y,
                            pointFsDragTangent[0].x, pointFsDragTangent[0].y);
                    //            从上一个点绘制一条直线到下面这个位置
                    pathBezier.lineTo(pointFsDragTangent[1].x, pointFsDragTangent[1].y);
                    //            再绘制一条二阶贝塞尔曲线
                    pathBezier.quadTo(pointControl.x, pointControl.y,
                            pointfsFixTangent[1].x, pointfsFixTangent[1].y);
//            执行close，表示形成闭合路径
                    pathBezier.close();
                    canvas.drawPath(pathBezier, paint);
                }
                //画拖拽圆
                canvas.drawCircle(pointFDragCenter.x, pointFDragCenter.y, mDragRadius, paint);
            }
        }
        canvas.restore();
    }

    float touchX;
    float touchY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isOutUp || animatorRebound != null && animatorRebound.isRunning())
            return true;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                touchX = event.getX();
                touchY = event.getY();
                // 判断是否点在圆内
                if (regionCircle.contains((int) touchX, (int) touchY)) {
                    isTouch = true;
                    processData(touchX, touchY);
                    invalidate();
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (isTouch) {
                    processData(event.getX(), event.getY());
                    invalidate();
                }
                break;
            }

            case MotionEvent.ACTION_UP: {
                if (isTouch) {
                    if (!isOut) {
                        //一直没有
                        inUp();
                    } else {
                        outUp();
                    }
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
        if (!isTouch)
            return;
        pointFDragCenter.x = touchX;
        pointFDragCenter.y = touchY;

        //计算拖拽的距离
        dx = touchX - pointfFixCenter.x;
        dy = touchY - pointfFixCenter.y;
        mDraglength = (float) Math.sqrt((dx * dx) + (dy * dy));
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

    //回弹动画
    ValueAnimator animatorRebound;

    private void inUp() {
        if (animatorRebound == null)
            animatorRebound = ValueAnimator.ofFloat(1f, -0.3f, 0.1f, 0.05f, 0);
        if (animatorRebound.isRunning())
            return;
        animatorRebound.setDuration(1000);
        animatorRebound.setInterpolator(new DecelerateInterpolator(2.0f));
        animatorRebound.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float currentValue = (float) animation.getAnimatedValue();

                pointFDragCenter.x = pointfFixCenter.x + dx * currentValue;
                pointFDragCenter.y = pointfFixCenter.y + dy * currentValue;
                processFixRadius(Math.abs(mDraglength * currentValue));
                if (currentValue >= 0) {
                    processTangent(cosA, sinA);
                } else {
                    processTangent(-cosA, -sinA);
                }
                invalidate();
            }
        });
        animatorRebound.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                resetValue();
                invalidate();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorRebound.start();
    }


    private void outUp() {
        if (mDraglength >= mFarthestDistance) {
            isOutUp = true;
            invalidate();
        } else {
            resetValue();
            invalidate();
        }
    }

    /**
     * 重置为初始状态的数据
     */
    private void resetValue() {
        mFixRadius = mMaxFixRadius;
        isTouch = false;
        isOut = false;
        isOutUp = false;
    }
}
