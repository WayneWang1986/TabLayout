package com.wang.wayne.library;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by WayneWang on 2016/11/26.
 */

public class TabLayout extends LinearLayout {
    private
    @ColorInt
    int mNormalColor;
    private
    @ColorInt
    int mSelectColor;

    private
    @LayoutRes
    int mTabLayoutResId;
    private int mIndicatorSize;
    private int mIndicatorPadding;

    private List<String> mData = new ArrayList<>();

    private List<Float> mTabItemWidth = new ArrayList<>();
    private List<Float> mTabItemLeft = new ArrayList<>();

    private OnTabClickListener mOnTabClickListener;

    private int mCurrentIndex;
    private float mCurrentIndicatorWidth;
    private float mCurrentIndicatorLeft;

    private int mBaseDuration;

    private ValueAnimator mAnimator;

    private Paint mPaint;
    private RectF mRectF;

    public TabLayout(Context context) {
        this(context, null, 0);
    }

    public TabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.tabLayout);
        mNormalColor = a.getColor(R.styleable.tabLayout_normal_color, 0);
        mSelectColor = a.getColor(R.styleable.tabLayout_select_color, 0);
        mTabLayoutResId = a.getResourceId(R.styleable.tabLayout_tab_layout, 0);
        mIndicatorSize = a.getDimensionPixelOffset(R.styleable.tabLayout_indicator_size, 0);
        mIndicatorPadding = a.getDimensionPixelOffset(R.styleable.tabLayout_indicator_padding, 0);
        mBaseDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        a.recycle();
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_tab, this, true);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStrokeWidth(mIndicatorSize);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mSelectColor);
        mRectF = new RectF();
    }

    public void setOnTabClickListener(OnTabClickListener onTabClickListener) {
        this.mOnTabClickListener = onTabClickListener;
    }

    public void setData(Collection<? extends String> data) {
        mData.clear();
        mData.addAll(data);

        removeAllViews();
        int size = data.size();
        for (int i = 0; i < size; i++) {
            View tabItem = LayoutInflater.from(getContext()).inflate(mTabLayoutResId, this, false);
            if (!(tabItem instanceof TextView)) {
                throw new IllegalStateException("TabLayout's item must be textView.");
            }
            TextView textView = ((TextView) tabItem);
            if (i == 0) {
                textView.setTextColor(mSelectColor);
            } else {
                textView.setTextColor(mNormalColor);
            }
            textView.setText(mData.get(i));
            tabItem.setTag(i);
            tabItem.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index = (int) v.getTag();
                    if (index == mCurrentIndex) {
                        return;
                    }
                    animateIndicator(mCurrentIndex, index);
                    if (mOnTabClickListener != null) {
                        mOnTabClickListener.onTabClick(index);
                    }
                }
            });
            addView(tabItem);
        }

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                initializeTabWidthAndCenter();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });
    }

    private void animateIndicator(int currentIndex, int index) {
        final float startWidth = mTabItemWidth.get(currentIndex);
        final float endWidth = mTabItemWidth.get(index);

        final float startLeft = mTabItemLeft.get(currentIndex);
        final float endLeft = mTabItemLeft.get(index);

        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
        }

        mAnimator = ValueAnimator.ofFloat(0, 1);
        mAnimator.setDuration(mBaseDuration);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                mCurrentIndicatorWidth = startWidth + (endWidth - startWidth) * fraction;
                mCurrentIndicatorLeft = startLeft + (endLeft - startLeft) * fraction;
                invalidate();
            }
        });
        mAnimator.start();
        mCurrentIndex = index;
        changeTabTextColor(mCurrentIndex);
    }

    private void changeTabTextColor(int currentIndex) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            TextView tabItem = (TextView) getChildAt(i);
            if (i == currentIndex) {
                tabItem.setTextColor(mSelectColor);
            } else {
                tabItem.setTextColor(mNormalColor);
            }
        }
    }

    private void initializeTabWidthAndCenter() {
        mTabItemWidth.clear();
        mTabItemLeft.clear();
        float left = getPaddingLeft() + mIndicatorPadding;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            float rawWidth = getChildAt(i).getWidth();
            float width = rawWidth - mIndicatorPadding * 2;
            mTabItemWidth.add(width <= 0 ? rawWidth : width);

            mTabItemLeft.add(left);
            left += rawWidth;
        }
        mCurrentIndicatorWidth = mTabItemWidth.get(0);
        mCurrentIndicatorLeft = mTabItemLeft.get(0);

        int tabItemHeight = getChildAt(0).getHeight();
        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = tabItemHeight + mIndicatorSize;
        setLayoutParams(params);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        mRectF.set(mCurrentIndicatorLeft, getHeight() - getPaddingBottom() - mIndicatorSize, mCurrentIndicatorLeft + mCurrentIndicatorWidth, getHeight() - getPaddingBottom());
        canvas.drawRect(mRectF, mPaint);
    }

    public interface OnTabClickListener {
        void onTabClick(int index);
    }
}
