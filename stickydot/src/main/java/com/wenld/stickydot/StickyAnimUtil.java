package com.wenld.stickydot;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.animation.DecelerateInterpolator;

/**
 * Created by wenld on 2017/2/18.
 */

public class StickyAnimUtil {
    //回弹动画
    private ValueAnimator animatorRebound;

    private ValueAnimator.AnimatorUpdateListener animatorUpdateListener;
    private Animator.AnimatorListener animatorListener;

    public StickyAnimUtil(ValueAnimator.AnimatorUpdateListener animatorUpdateListener, Animator.AnimatorListener animatorListener) {

        this.animatorUpdateListener = animatorUpdateListener;
        this.animatorListener = animatorListener;


    }

    public void palyAnim() {
        if (animatorUpdateListener == null)
            return;
        if (animatorListener == null)
            return;
        if (animatorRebound == null) {
            animatorRebound = ValueAnimator.ofFloat(1f, -0.4f, 0.2f, -0.1f, 0);
            animatorRebound.setDuration(300);
            animatorRebound.setInterpolator(new DecelerateInterpolator(2.0f));
            animatorRebound.addUpdateListener(animatorUpdateListener);
            animatorRebound.addListener(animatorListener);
        }
        if (animatorRebound.isRunning())
            return;

        animatorRebound.start();
    }

    public boolean isRunning() {
        if (animatorRebound == null)
            return false;
        return animatorRebound.isRunning();
    }
}
