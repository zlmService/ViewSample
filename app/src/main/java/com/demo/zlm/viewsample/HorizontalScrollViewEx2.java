package com.demo.zlm.viewsample;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by malinkang on 2016/5/11.
 */

public class HorizontalScrollViewEx2 extends ViewGroup {
    private static final String TAG = "HorizontalScrollViewEx2";

    private int mChildIndex;//用来判断滑页
    private int mChildWidth;//子元素的宽度
    private int mChildrenSize;//子元素的个数

    //记录上次滑动的坐标
    private int mLastX = 0;
    private int mLastY = 0;

    //记录上次滑动的坐标，在onInterceptTouchEvent中使用
    private int mLastIntercepX = 0;
    private int mLastIntercepY = 0;

    private Scroller mScroller;//用于弹性滑动
    private VelocityTracker mVelocityTracker;//用于速度追踪 ，滑页使用

    public HorizontalScrollViewEx2(Context context) {
        super(context);
        init();
    }

    public HorizontalScrollViewEx2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public HorizontalScrollViewEx2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if (mScroller == null) {
            //初始化
            mScroller = new Scroller(getContext());
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    //测量 并且支持wrap_content
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = 0;
        int measuredHeight = 0;
        final int childCount = getChildCount();
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (childCount == 0) {
            setMeasuredDimension(0, 0);
        } else if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            final View childView = getChildAt(0);
            //支持wrap_content 宽度为 子元素的个数 * 子元素的测量宽度 =ViewGroup的宽度
            measuredWidth = childCount * childView.getMeasuredWidth();
            measuredHeight=childView.getMeasuredHeight();
            setMeasuredDimension(measuredWidth, measuredHeight);
        } else if (widthMode == MeasureSpec.AT_MOST) {
            final View childView = getChildAt(0);//获取第一个子元素
            measuredWidth=childView.getMeasuredWidth() * childCount;
            setMeasuredDimension(measuredWidth, heightSize);
        } else if (heightMode == MeasureSpec.AT_MOST) {
            final View childView=getChildAt(0);
            measuredHeight=childView.getMeasuredHeight();
            setMeasuredDimension(widthSize, measuredHeight);
        }
    }

    //布局
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childLeft=0;
        final int childCount=getChildCount();
        mChildrenSize=childCount;
        for (int i = 0; i < childCount; i++) {
            final View viewChild=getChildAt(i);
            //如果子元素没有GONE的  就获取它的测量宽度，然后调用layout进行布局
            if (viewChild.getVisibility()!=View.GONE){
                final int childWidth=viewChild.getMeasuredWidth();
                mChildWidth=childWidth;
                viewChild.layout(childLeft,0,childLeft+childWidth,viewChild.getMeasuredHeight());
                childLeft+=childWidth;
            }
        }
    }

    private void smoothScrollBy(int dx, int i) {
        mScroller.startScroll(getScrollX(),0,dx,0,500);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()){
            scrollTo(mScroller.getCurrX(),mScroller.getCurrY());
            postInvalidate();
        }
    }

    //是否拦截
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercepted = false;
        int x = (int) ev.getX();
        int y = (int) ev.getY();

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                intercepted = false;
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                    intercepted = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                //手指移动的坐标-手指刚落下的坐标
                int deltaX = x - mLastIntercepX;
                int deltaY = y - mLastIntercepY;
                //如果 X>Y 就表示左右滑动  需要拦截 返回true
                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    intercepted = true;
                } else {
                    intercepted = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                intercepted = false;
                break;
            default:
                break;
        }
        Log.d(TAG, "intercepted" + intercepted);
        mLastX = x;
        mLastY = y;
        mLastIntercepX = x;
        mLastIntercepY = y;
        return intercepted;
    }
    //拦截后的处理
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mVelocityTracker.addMovement(event);
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = x - mLastX;
                int deltaY = y - mLastY;
                scrollBy(-deltaX, 0);
                break;
            case MotionEvent.ACTION_UP:
                int scrollX = getScrollX();
                mVelocityTracker.computeCurrentVelocity(1000);
                float xVelocity = mVelocityTracker.getXVelocity();
                //如果1秒内速度达到50，就直接翻页
                if (Math.abs(xVelocity) >= 50) {
                    mChildIndex = xVelocity > 0 ? mChildIndex - 1 : mChildIndex + 1;
                } else {
                    //如果是速度没到，就获取 滑动的距离，如果达到一半就翻页，否则不翻页
                    mChildIndex = (scrollX + mChildWidth / 2) / mChildWidth;
                }
                mChildIndex = Math.max(0, Math.min(mChildIndex, mChildrenSize - 1));
                int dx = mChildIndex * mChildWidth - scrollX;
                //弹性滑动，
                smoothScrollBy(dx, 0);
                mVelocityTracker.clear();
                break;
            default:
                break;
        }
        mLastX = x;
        mLastY = y;
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        mVelocityTracker.recycle();
        super.onDetachedFromWindow();
    }
}
