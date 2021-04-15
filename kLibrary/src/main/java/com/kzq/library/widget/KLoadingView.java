package com.kzq.library.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Size;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import com.kzq.library.R;
import com.kzq.library.interpolator.EaseCubicInterpolator;


public class KLoadingView extends WrappedView {

    /**
     * 默认弧宽度
     */
    private static final int DEFAULT_ARC_WIDTH = 15;
    /**
     * 默认第二个弧的长度
     */
    private static final float DEFAULT_SEC_ARC_LEN = 120f;
    private static final int DEFAULT_ARC_RADIUS = 600;

    /**
     * 弧的画笔
     */
    private Paint arcPaint;
    /**
     * 弧的矩形
     */
    private RectF arcRectF, ovalRectF;
    private static final float DEFAULT_OVAL_RADIUS = 20;
    /**
     * 弧的角度
     */
    private float angle, startAngle, endAngle, secStart, secEnd, trdStart, trdEnd;
    private int firstColor = Color.parseColor("#7E8FE1"),
            secColor = Color.parseColor("#30CFCA"),
            trdColor = Color.parseColor("#FAA61A");
    /**
     * 弧的宽高
     */
    private int arcWidth = -1, arcHeight = -1;
    private float ovalRadius = -1;
    private static final String DEFAULT_TEXT = "loading";
    private Paint txtPaint;
    private Paint ovalPaint;

    private Path txtPath;

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

    @Override
    protected Size getWrapSize() {
        int width = DEFAULT_ARC_RADIUS;
        if (arcWidth != -1) {
            width = (int) (arcWidth + arcPaint.getStrokeWidth());
        }
        int height = DEFAULT_ARC_RADIUS;
        if (arcHeight != -1) {
            height = (int) (arcHeight + arcPaint.getStrokeWidth());
        }
        return new Size(width, height);
    }

    private void init() {

        arcPaint = new Paint();
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setAntiAlias(true);
        arcPaint.setStrokeWidth(DEFAULT_ARC_WIDTH);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);

        ovalPaint = new Paint();
        ovalPaint.setStyle(Paint.Style.FILL);
        ovalPaint.setColor(Color.WHITE);
        ovalPaint.setAntiAlias(true);

        txtPaint = new Paint();
        txtPaint.setTextSize(70);
        txtPaint.setColor(Color.WHITE);
        txtPaint.setAntiAlias(true);

        txtPath = new Path();

        post(new Runnable() {
            @Override
            public void run() {
                int width = getWidth();
                int height = getHeight();

                if (arcWidth == -1) {
                    arcWidth = (int) (width - arcPaint.getStrokeWidth());
                }
                if (arcHeight == -1) {
                    arcHeight = (int) (height - arcPaint.getStrokeWidth());
                }
                if (ovalRadius == -1) {
                    ovalRadius = DEFAULT_OVAL_RADIUS;
                }

                final int arcX = width / 2;
                final int arcY = height / 2;
                arcRectF = new RectF(arcX - arcWidth / 2f, arcY - arcHeight / 2f, arcX + arcWidth / 2f, arcY + arcHeight / 2f);

                ValueAnimator valueAnimator = ValueAnimator.ofInt(-50, 720);
                valueAnimator.setRepeatCount(Animation.INFINITE);
                valueAnimator.setDuration(2000);
                valueAnimator.setInterpolator(new EaseCubicInterpolator(.33f, 0, .18f, 1));
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        angle = (int) valueAnimator.getAnimatedValue();
                        if (angle > 0) {
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
                        }
                        invalidate();

                    }
                });
                valueAnimator.start();

                Rect bounds = new Rect();
                txtPaint.getTextBounds(DEFAULT_TEXT, 0, DEFAULT_TEXT.length(), bounds);

                //文本宽度
                final float txtWidth = bounds.right - bounds.left;
                Paint.FontMetricsInt fontMetrics = txtPaint.getFontMetricsInt();

                //基线
                final float txtY = (height - fontMetrics.bottom - fontMetrics.top) / 2f;
                final float txtX = arcX - txtWidth / 2;

                float ovalMax = arcRectF.top + arcPaint.getStrokeWidth();
                ovalRectF = new RectF(arcX - ovalRadius, ovalMax, arcX + ovalRadius, ovalMax + ovalRadius * 2);

                txtPath.moveTo(txtX, txtY);
                txtPath.lineTo(arcX + txtWidth / 2, txtY);

                final ValueAnimator txtAnimator = ValueAnimator.ofFloat(0, 50, -20, 20, 10, 0);
                txtAnimator.setInterpolator(new LinearInterpolator());
                txtAnimator.setDuration(300);
                txtAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        float value = (float) valueAnimator.getAnimatedValue();
                        txtPath.reset();
                        txtPath.moveTo(txtX, txtY);
                        txtPath.quadTo(arcX, txtY + value, arcX + txtWidth / 2, txtY);
                    }
                });

                final float ovalMin = txtY - (fontMetrics.bottom - fontMetrics.top);
                ValueAnimator ovalAnimator = ValueAnimator.ofFloat(ovalMax, ovalMin, ovalMax);
                ovalAnimator.setInterpolator(new EaseCubicInterpolator(.6f, 0, .4f, 1));
                ovalAnimator.setDuration(1200);
                ovalAnimator.setRepeatCount(Animation.INFINITE);
                ovalAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        float value = (float) valueAnimator.getAnimatedValue();
                        ovalRectF.top = value;
                        ovalRectF.bottom = value + ovalRadius * 2;

                        if (value > ovalMin - 10 && !txtAnimator.isStarted()) {
                            txtAnimator.start();
                        }
                    }
                });
                ovalAnimator.start();

            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (arcRectF == null) {
            return;
        }
        if (angle > 360) {
            arcPaint.setColor(trdColor);
            canvas.drawArc(arcRectF, trdStart, trdEnd, false, arcPaint);

            arcPaint.setColor(secColor);
            canvas.drawArc(arcRectF, secStart, secEnd, false, arcPaint);
        }
        arcPaint.setColor(firstColor);
        canvas.drawArc(arcRectF, startAngle, endAngle, false, arcPaint);

        canvas.drawTextOnPath(DEFAULT_TEXT, txtPath, 0, 0, txtPaint);

        canvas.drawOval(ovalRectF, ovalPaint);
    }

}
