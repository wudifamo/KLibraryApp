package com.kzq.library.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Size;

import androidx.annotation.Nullable;

import com.kzq.library.R;

import java.util.ArrayList;
import java.util.List;

public class KStepView extends WrappedView {
    private static final int DEFAULT_WIDTH = 1080;
    private static final int DEFAULT_HEIGHT = 12;
    private Paint paint;
    private int count = 3;
    private int step = 1;
    private int aColor = Color.RED, nColor = Color.parseColor("#E1E5ED");
    private List<RectF> list = new ArrayList<>();

    public KStepView(Context context) {
        super(context);
        init(context);
    }

    public KStepView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.KStepView);
        if (a.hasValue(R.styleable.KStepView_ksv_count)) {
            count = a.getColor(R.styleable.KStepView_ksv_count, count);
        }
        a.recycle();
        init(context);
    }

    private void init(Context context) {
        setBackgroundColor(Color.WHITE);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected Size getWrapSize() {
        return new Size(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < list.size(); i++) {
            RectF rectF = list.get(i);
            if (i < step) {
                paint.setColor(aColor);
            } else {
                paint.setColor(nColor);
            }
            canvas.drawRect(rectF, paint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        calculateRect();
    }

    private void calculateRect() {
        list.clear();
        int space = DEFAULT_HEIGHT;
        int width = (getMeasuredWidth() - space * (count - 1)) / count;
        int start = width + space;
        for (int i = 0; i < count; i++) {
            RectF rectF = new RectF(i * start, 0, i * start + width, getMeasuredHeight());
            list.add(rectF);
        }
    }

}
