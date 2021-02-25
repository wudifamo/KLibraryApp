package com.kzq.library.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Size;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;

import com.kzq.library.R;
import com.kzq.library.interpolator.EaseCubicInterpolator;

import java.util.ArrayList;
import java.util.List;

public class KLoadingRectView extends WrappedView {
    /**
     * 方块数量
     */
    private int rectCount = 5;
    /**
     * 方块间距
     */
    private int rectGap = 10;
    /**
     * 方块宽度
     */
    private int rectWidth = 30;
    /**
     * 变形宽度
     */
    private int transWidth = 5;
    private int duration = 800;
    /**
     * 跳起来的高度
     */
    private int jumpHeight = rectWidth * 3;
    private List<RectF> rectList = new ArrayList<>();
    private List<Path> pathList = new ArrayList<>();
    private Paint paint = new Paint();
    private int rectColor = Color.parseColor("#00aabb");
    /**
     * 最左边的方块要右移的距离
     */
    private int firstRight;
    /**
     * 其它方块左移的距离
     */
    private int otherLeft;

    public KLoadingRectView(Context context) {
        super(context);
        init();
    }

    public KLoadingRectView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.KLoadingRectView);
        if (a.hasValue(R.styleable.KLoadingRectView_klrv_count)) {
            rectCount = Math.max(a.getInt(R.styleable.KLoadingRectView_klrv_count, rectCount), 2);
        }
        if (a.hasValue(R.styleable.KLoadingRectView_klrv_gap)) {
            rectGap = a.getInt(R.styleable.KLoadingRectView_klrv_gap, rectGap);
        }
        if (a.hasValue(R.styleable.KLoadingRectView_klrv_rect_width)) {
            rectWidth = a.getDimensionPixelOffset(R.styleable.KLoadingRectView_klrv_rect_width, rectWidth);
        }
        if (a.hasValue(R.styleable.KLoadingRectView_klrv_duration)) {
            duration = a.getInt(R.styleable.KLoadingRectView_klrv_duration, duration);
        }
        if (a.hasValue(R.styleable.KLoadingRectView_klrv_jump_height)) {
            jumpHeight = a.getInt(R.styleable.KLoadingRectView_klrv_jump_height, jumpHeight);
        }
        if (a.hasValue(R.styleable.KLoadingRectView_klrv_color)) {
            rectColor = a.getColor(R.styleable.KLoadingRectView_klrv_color, rectColor);
        }
        a.recycle();
        init();
    }

    private void init() {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(rectColor);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(1);

        final Path path = new Path();
        for (int i = 0; i < rectCount; i++) {
            float left = (rectGap + rectWidth) * i + transWidth;
            float bottom = getWrapSize().getHeight() - 1;
            RectF rectF = new RectF(left, bottom - rectWidth, left + rectWidth, bottom);
            rectList.add(rectF);
            Path iPath = new Path();
            iPath.moveTo(rectF.left, rectF.top);
            iPath.lineTo(rectF.right, rectF.top);
            iPath.lineTo(rectF.right, rectF.bottom);
            iPath.lineTo(rectF.left, rectF.bottom);
            pathList.add(iPath);
            if (i == 0) {
                //第一个中心点
                path.moveTo(rectF.centerX(), rectF.centerY());
            } else if (i == rectCount - 1) {
                //最后一个
                path.cubicTo(rectWidth / 2f, 0, rectF.centerX(), 0, rectF.centerX(), rectF.centerY());
            }
        }
        firstRight = (rectWidth + rectGap) * (rectCount - 1);
        otherLeft = rectWidth + rectGap;
        post(new Runnable() {
            @Override
            public void run() {
                //起跳动画
                final ValueAnimator upAnimator = ValueAnimator.ofFloat(0, transWidth, 0);
                final ValueAnimator resetAnimator = ValueAnimator.ofFloat(transWidth, 0);
                resetAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float v = (float) animation.getAnimatedValue();
                        for (int i = 1; i < pathList.size(); i++) {
                            RectF rectF = rectList.get(i);
                            Path p = pathList.get(i);
                            p.reset();
                            p.moveTo(rectF.left + v, rectF.top);
                            p.lineTo(rectF.right + v, rectF.top);

                            p.lineTo(rectF.right, rectF.bottom);
                            p.lineTo(rectF.left, rectF.bottom);
                        }
                        invalidate();
                    }
                });
                //平移动画
                final PathMeasure pathMeasure = new PathMeasure(path, false);
                final ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, pathMeasure.getLength());
                upAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float v = (float) animation.getAnimatedValue();
                        RectF rectF = rectList.get(0);
                        Path p = pathList.get(0);
                        p.reset();
                        float top = rectF.bottom - rectWidth + v;
                        p.moveTo(rectF.left, top);
                        p.lineTo(rectF.right, top);
                        p.quadTo(rectF.right + v, rectF.bottom - (rectWidth - v) / 2, rectF.right, rectF.bottom);
                        p.lineTo(rectF.left, rectF.bottom);
                        p.quadTo(rectF.left - v, rectF.bottom - (rectWidth - v) / 2, rectF.left, top);
                        invalidate();
                    }
                });
                final EaseCubicInterpolator interpolator = new EaseCubicInterpolator(0.5f, .0f, 1, .0f);
                upAnimator.setInterpolator(interpolator);
                upAnimator.setDuration(1000);
                final Animator.AnimatorListener upListener = new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        valueAnimator.start();
                    }
                };
                upAnimator.addListener(upListener);
                upAnimator.start();
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float[] currentPosition = new float[2];
                        pathMeasure.getPosTan((Float) animation.getAnimatedValue(), currentPosition, null);
                        final float x = currentPosition[0];
                        final float y = currentPosition[1];
                        calculate(x, y);
                        invalidate();
                    }
                });
                valueAnimator.setInterpolator(new EaseCubicInterpolator(0, .5f, 1, 0.5f));
                valueAnimator.setDuration(duration);
                valueAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        resetAnimator.start();
                        upAnimator.setInterpolator(new DecelerateInterpolator());
                        upAnimator.removeAllListeners();
                        upAnimator.start();
                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                RectF rectF = rectList.get(1);
                                calculate(rectF.centerX(), rectF.centerY());
                                invalidate();
                                upAnimator.addListener(upListener);
                                upAnimator.setInterpolator(interpolator);
                                upAnimator.start();
                            }
                        }, 2000);
                    }
                });
            }
        });
    }

    @Override
    protected Size getWrapSize() {
        return new Size(rectCount * rectWidth + (rectCount - 1) * rectGap + transWidth * 2, rectWidth + jumpHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Path path : pathList) {
            canvas.drawPath(path, paint);
        }
    }

    private void calculate(float x, float y) {
        float d = 0;
        for (int i = 0; i < rectList.size(); i++) {
            RectF rectF = rectList.get(i);
            Path path = pathList.get(i);
            if (i == 0) {
                rectF.left = x - rectWidth / 2;
                d = (rectF.left - transWidth) / firstRight;
                rectF.right = x + rectWidth / 2;
                rectF.bottom = y + rectWidth / 2;
                rectF.top = y - rectWidth / 2;
                path.reset();
                path.moveTo(rectF.left, rectF.top);
                path.lineTo(rectF.right, rectF.top);
                path.lineTo(rectF.right, rectF.bottom);
//                path.quadTo(rectF.right + rectWidth / 2, rectF.top + rectWidth / 2, rectF.right, rectF.bottom);
                path.lineTo(rectF.left, rectF.bottom);
            } else {
                float gap4 = d * otherLeft;
                rectF.left = (rectGap + rectWidth) * i - gap4 + transWidth;
                rectF.right = rectF.left + rectWidth;
                path.reset();
                float g = gap4 < transWidth ? gap4 : transWidth;

                path.moveTo(rectF.left + g, rectF.top);
                path.lineTo(rectF.right + g, rectF.top);

                path.lineTo(rectF.right, rectF.bottom);
                path.lineTo(rectF.left, rectF.bottom);
            }
        }
    }
}
