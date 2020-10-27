package com.kzq.library.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Size;

import androidx.annotation.Nullable;

public class KProgressView extends WrappedView {
    private final static int[] gradientColors = new int[]{Color.parseColor("#92D16C"), Color.parseColor("#F6F19C"), Color.parseColor("#F6BD8B")};
    private final static float[] gradientRadius = new float[]{0, 0.5f, 1.0f};
    private Paint stripPaint;
    private Paint grayPaint;
    private Paint ballPaint;
    private Paint msgPaint;
    private Paint textPaint;
    private int mHeight;
    private int barHeight;
    /**
     * 球半径、阴影半径
     */
    private float ballRadius, ballSr;
    private LinearGradient mLinearGradient;
    private RectF rectF = new RectF();
    private RectF rectGray = new RectF();
    private float percent;
    private Path path = new Path();
    private float ballRx, msgWidth, msgHeight;
    private RectF rectMsg = new RectF();
    private String text;

    public KProgressView(Context context) {
        super(context);
        init(context);
    }

    public KProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @Override
    protected Size getWrapSize() {
        barHeight = getMeasuredWidth() / 13;
        ballRadius = barHeight * 1.5f / 2f;
        ballSr = ballRadius / 3f;
        msgHeight = barHeight;
        msgWidth = msgHeight * 3f;
        return new Size(getMeasuredWidth(), (int) (barHeight * 3.5));
    }

    private void init(Context context) {
        stripPaint = new Paint();
        stripPaint.setAntiAlias(true);
        stripPaint.setStyle(Paint.Style.FILL);

        grayPaint = new Paint();
        grayPaint.setColor(Color.parseColor("#EBEBEB"));
        grayPaint.setAntiAlias(true);
        grayPaint.setStyle(Paint.Style.FILL);

        ballPaint = new Paint();
        ballPaint.setColor(Color.WHITE);
        ballPaint.setAntiAlias(true);
        ballPaint.setStyle(Paint.Style.FILL);

        msgPaint = new Paint();
        msgPaint.setColor(Color.parseColor("#F6BD8B"));
        msgPaint.setAntiAlias(true);
        msgPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(42);
    }

    public void setProgress(final float percent, String text) {
        this.percent = percent;
        this.text = text;
        post(new Runnable() {
            @Override
            public void run() {
                rectGray.set(rectF.left + rectF.width() * percent, rectF.top, rectF.right, rectF.bottom);
                ballPaint.setShadowLayer(ballSr, 0, 0, Color.GRAY);
                invalidate();
            }
        });
    }

    private void calculateMsg() {
        ballRx = rectF.left + rectF.width() * percent;
//        path.moveTo(ballRx, rectF.top - rectF.height());
//        rectF.set(ballRx - 30, rectF.top - rectF.height() - 30, ballRx + 30, rectF.top - rectF.height());
        rectMsg.set(ballRx - msgWidth / 2, 0, ballRx + msgWidth / 2, msgHeight);
        path.addRoundRect(rectMsg, 10, 10, Path.Direction.CCW);
        float dt = msgHeight / 2;
        path.moveTo(ballRx - dt / 2, rectMsg.bottom);
        path.lineTo(ballRx, rectMsg.bottom + dt * 0.75f);
        path.lineTo(ballRx + dt / 2, rectMsg.bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        calculateMsg();
        canvas.drawRoundRect(rectF, mHeight, mHeight, stripPaint);

        canvas.drawRoundRect(rectGray, mHeight, mHeight, grayPaint);

        canvas.drawCircle(ballRx, rectF.centerY(), ballRadius, ballPaint);

        canvas.drawPath(path, msgPaint);

        if (TextUtils.isEmpty(text)) {
            return;
        }
        int textWidth = (int) textPaint.measureText(text);
        Paint.FontMetricsInt rFontMetrics = textPaint.getFontMetricsInt();
        float rBaseLine = (rectMsg.bottom - rFontMetrics.bottom - rFontMetrics.top) / 2f;
        canvas.drawText(text, ballRx - textWidth / 2, rBaseLine, textPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        float dr = ballRadius - barHeight / 2f + ballSr;
        rectF.set(msgWidth / 2, mHeight - dr - barHeight, mWidth - msgWidth / 2, mHeight - dr);
        if (mLinearGradient == null) {
            mLinearGradient = new LinearGradient(rectF.left, rectF.top, rectF.right, rectF.bottom, gradientColors, gradientRadius, Shader.TileMode.CLAMP);
            stripPaint.setShader(mLinearGradient);
        }
    }
}
