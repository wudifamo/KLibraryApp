package com.kzq.library.widget.chart;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.kzq.library.util.DensityUtil;

import java.util.Arrays;
import java.util.List;

public class MyLineChart extends View {
    private Paint linePaint, lineShadowPaint, barPaint, textPaint;
    private List<String> yearList = Arrays.asList("2015", "2016", "2017", "2018", "2019", "2020", "2021");
    private List<Integer> rateList;
    private int mWidth, mHeight;
    private int textStart, textY;
    private int itemWidth;
    private List<Float> valueList;
    private int barWidth, barBottomGap, barStart, barBottom, barMaxHeight;
    private float max, rateMax;
    private Path ratePath, linePath;
    float barDt = 0;
    private Path trPath = new Path();
    private int barLightColor=Color.parseColor("#199DFA"), barDarkColor = Color.parseColor("#095F9C");

    public MyLineChart(Context context) {
        super(context);
        init(context);
    }

    public MyLineChart(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void setValue(List<Float> list, float max, List<Integer> rateList, float rateMax) {
        this.valueList = list;
        this.max = max;
        this.rateList = rateList;
        this.rateMax = rateMax;
        ratePath = new Path();
        linePath = new Path();
    }

    private void init(Context context) {
        barWidth = DensityUtil.dip2px(context, 8);
        barBottomGap = DensityUtil.dip2px(context, 12);
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setColor(Color.parseColor("#23C28D"));
        linePaint.setStrokeWidth(DensityUtil.dip2px(context, 1));
        lineShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lineShadowPaint.setStyle(Paint.Style.FILL);
        lineShadowPaint.setColor(linePaint.getColor());

        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setStyle(Paint.Style.FILL);
        barPaint.setColor(Color.parseColor("#199DFA"));

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < yearList.size(); i++) {
            String year = yearList.get(i);
            canvas.drawText(year, textStart + i * itemWidth, textY, textPaint);

            float v = valueList.get(i);
            int barLeft = barStart + i * itemWidth;
            float barTop = barBottom - barBottom * v / max * barDt;
//            canvas.drawRect(barLeft, barTop, barLeft + barWidth, barBottom, barPaint);
            trPath.reset();
            trPath.moveTo(barLeft, barBottom);
            trPath.lineTo(barLeft, barTop);
            trPath.lineTo(barLeft + barWidth, barTop);
            barPaint.setColor(barLightColor);
            canvas.drawPath(trPath, barPaint);
            trPath.reset();
            trPath.moveTo(barLeft + barWidth, barTop);
            trPath.lineTo(barLeft + barWidth, barBottom);
            trPath.lineTo(barLeft, barBottom);
            barPaint.setColor(barDarkColor);
            canvas.drawPath(trPath, barPaint);



        }
        canvas.drawPath(linePath, linePaint);
        canvas.drawPath(ratePath, lineShadowPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        calculate();
    }

    private void calculate() {
        itemWidth = mWidth / yearList.size();
        float textLength = textPaint.measureText(yearList.get(0));
        textStart = (int) (itemWidth / 2 - textLength / 2);
        textY = mHeight - 2;

        barStart = itemWidth / 2 - barWidth / 2;
        barBottom = mHeight - barBottomGap;
        int lineStart = itemWidth / 2;
        for (int i = 0; i < rateList.size(); i++) {
            int v = rateList.get(i);
            float lineY = (1 - v / rateMax) * barBottom;
            Log.i("---", "x:" + (lineStart + itemWidth * i) + " Y:" + lineY);
            if (i == 0) {
                ratePath.moveTo(lineStart + itemWidth * i, lineY);
                linePath.moveTo(lineStart + itemWidth * i, lineY);
            } else {
                ratePath.lineTo(lineStart + itemWidth * i, lineY);
            }
        }
        post(new Runnable() {
            @Override
            public void run() {
                startAnim();
            }
        });
    }

    private void calculateAnim() {

    }

    public void startAnim() {
        final PathMeasure pathMeasure = new PathMeasure(ratePath, false);
        final ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, pathMeasure.getLength());
        valueAnimator.setDuration(1000);
        valueAnimator.start();
//        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float[] currentPosition = new float[2];
                pathMeasure.getPosTan((Float) animation.getAnimatedValue(), currentPosition, null);
                final float x = currentPosition[0];
                final float y = currentPosition[1];
                linePath.lineTo(x, y);
                calculateAnim();
                invalidate();
            }
        });

        ValueAnimator barAnimator = ValueAnimator.ofFloat(0, 1)
                .setDuration(1000);
        barAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                barDt = (float) animation.getAnimatedValue();
                Shader mShader = new LinearGradient(0, 0, 0, barBottom, new int[]{lineShadowPaint.getColor(), Color.TRANSPARENT}, new float[]{0, barDt}, Shader.TileMode.REPEAT);
                lineShadowPaint.setShader(mShader);
            }
        });
        barAnimator.start();
    }
}
