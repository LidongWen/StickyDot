package com.wenld.stickydot;

import android.graphics.PointF;

/**
 * 拖拽过程监听接口
 */
public interface DragStickViewListener {
    /**
     * 移动回调
     *
     * @param dragCanterPoint 拖拽的中心坐标
     */
    void inRangeMove(PointF dragCanterPoint);
    /**
     * 当移出了规定范围，最后在范围外松手的回调
     *
     * @param dragCanterPoint
     */
    void outRangeUp(PointF dragCanterPoint);

    /**
     * 一直没有移动出范围，在范围内松手的回调
     *
     * @param dragCanterPoint
     */
    void inRangeUp(PointF dragCanterPoint);
}
