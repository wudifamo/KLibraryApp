package com.kzq.library.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;

import androidx.annotation.Nullable;

import com.kzq.library.R;
import com.kzq.library.interpolator.EaseCubicInterpolator;


public class KLoadingView extends View {

    /**
     * 默认弧宽度
     */
    private static final int DEFAULT_ARC_WIDTH = 20;
    /**
     * 默认第二个弧的长度
     */
    private static final float DEFAULT_SEC_ARC_LEN = 120f;

    /**
     * 弧的画笔
     */
    private Paint arcPaint;
    /**
     * 弧的矩形
     */
    private RectF arcRectF;
    /**
     * 弧的角度
     */
    private float angle, startAngle, endAngle, secStart, secEnd, trdStart, trdEnd;
    private int firstColor = Color.parseColor("#7E8FE1"),
            secColor = Color.parseColor("#30CFCA"),
            trdColor = Color.parseColor("#FAA61A");
    /**
     * 弧的中心点
     */
    private int arcX, arcY;
    /**
     * 弧的宽高
     */
    private int arcWidth = -1, arcHeight = -1;

    public KLoadingView(Context context) {
        super(context);
        init();
    }

    public KLoadingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.KLoadingView);
        if (a.hasValue(R.styleable.KLoadingView_klv_first_arc_color)) {
            firstColor = a.getColor(R.styleable.KLoadingView_klv_first_arc_color, firstColor);
        }
        if (a.hasValue(R.styleable.KLoadingView_klv_sec_arc_color)) {
            secColor = a.getColor(R.styleable.KLoadingView_klv_sec_arc_color, secColor);
        }
        if (a.hasValue(R.styleable.KLoadingView_klv_trd_arc_color)) {
            trdColor = a.getColor(R.styleable.KLoadingView_klv_trd_arc_color, trdColor);
        }
        if (a.hasValue(R.styleable.KLoadingView_klv_arc_width)) {
            arcWidth = a.getDimensionPixelOffset(R.styleable.KLoadingView_klv_arc_width, arcWidth);
        }
        if (a.hasValue(R.styleable.KLoadingView_klv_arc_height)) {
            arcHeight = a.getDimensionPixelOffset(R.styleable.KLoadingView_klv_arc_height, arcHeight);
        }
        if (a.hasValue(R.styleable.KLoadingView_klv_arc_stroke_width)) {
            arcPaint.setStrokeWidth(a.getDimensionPixelOffset(R.styleable.KLoadingView_klv_arc_stroke_width, DEFAULT_ARC_WIDTH));
        }
        a.recycle();
    }

    private void init() {

        arcPaint = new Paint();
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setAntiAlias(true);
        arcPaint.setColor(Color.parseColor("#7E8FE1"));
        arcPaint.setStrokeWidth(DEFAULT_ARC_WIDTH);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);

        post(new Runnable() {
            @Override
            public void run() {
                int width = getWidth();
                int height = getHeight();
                arcX = width / 2;
                arcY = height / 2;

                if (arcWidth == -1) {
                    arcWidth = width;
                }
                if (arcHeight == -1) {
                    arcHeight = height;
                }

                arcRectF = new RectF(arcX - arcWidth / 2f, arcY - arcHeight / 2f, arcX + arcWidth / 2f, arcY + arcHeight / 2f);

                ValueAnimator valueAnimator = ValueAnimator.ofInt(-50, 720);
                valueAnimator.setRepeatCount(Animation.INFINITE);
                valueAnimator.setDuration(2000);
                valueAnimator.setInterpolator(new EaseCubicInterpolator(.33f, 0, .18f, 1));
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        angle = (int) valueAnimator.getAnimatedValue();
                        if (angle < 0) {
                            return;
                        }
                        if (angle <= 360) {
                            startAngle = -90.25f;
                            endAngle = 0.25f + angle;
                        } else {
                            float dt = angle - 360;
                            startAngle = -90.25f + dt;
                            endAngle = 720.25f - angle;

                            //第二个的end始终对齐第一个的start,第二个的start从默认长度递减
                            float secLength = DEFAULT_SEC_ARC_LEN * (1 - dt / 360);
                            secStart = startAngle - secLength;
                            secEnd = secLength;

                            //第三个的end先从第二个的start距离0到45度再到0,start从默认长度递减
                            float trdLength = 45f * (1f - Math.abs(180 - dt) / 180);
                            trdStart = secStart - secLength - trdLength;
                            trdEnd = secLength;

                        }
                        invalidate();
                    }
                });
                valueAnimator.start();
            }
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (arcRectF == null) {
            return;
        }
        if (angle > 360) {
            //先画第三个在最下面
            arcPaint.setColor(trdColor);
            canvas.drawArc(arcRectF, trdStart, trdEnd, false, arcPaint);

            arcPaint.setColor(secColor);
            canvas.drawArc(arcRectF, secStart, secEnd, false, arcPaint);
        }
        arcPaint.setColor(firstColor);
        canvas.drawArc(arcRectF, startAngle, endAngle, false, arcPaint);

    }
}
