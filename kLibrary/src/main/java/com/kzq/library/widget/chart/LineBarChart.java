package com.kzq.library.widget.chart;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kzq.library.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LineBarChart extends FrameLayout {
    private static final int LEFT_COUNT = 5;

    public LineBarChart(@NonNull Context context) {
        super(context);
    }

    public LineBarChart(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_chart_container, this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0);
        LinearLayout.LayoutParams lpRight = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0);
        lp.weight = 1;
        lpRight.weight = 1;
        lpRight.gravity = Gravity.RIGHT;
        List<Integer> rateList = new ArrayList<>();
        rateList.add(0);
        List<Float> valueList = Arrays.asList(.35f, 2.16f, 8.45f, 13.27f, 18.21f, 30.55f, 30.55f);
        for (int i = 1; i < valueList.size(); i++) {
            int rate = (int) (valueList.get(i) / valueList.get(i - 1) * 100);
            rateList.add(rate);
        }
        float max = Collections.max(valueList);
        float maxValue = calculateMaxValue(max, 5);

        int rateMax = Collections.max(rateList);
        float rateMaxValue = calculateMaxValue(rateMax, 50);

        int dt = (int) (maxValue / LEFT_COUNT);
        int rateDt = (int) (rateMaxValue / LEFT_COUNT);
        LinearLayout leftContainer = findViewById(R.id.left_container);
        LinearLayout rightContainer = findViewById(R.id.right_container);
        TextView tl = new TextView(context);
        tl.setText("单位: 亿");
        tl.setTextSize(5);
        leftContainer.addView(tl, lp);
        TextView tr = new TextView(context);
        tr.setText("单位: %");
        tr.setTextSize(5);
        rightContainer.addView(tr, lpRight);
        for (int i = 0; i < LEFT_COUNT; i++) {
            TextView leftView = getTextView(context, maxValue, dt, i);
            leftContainer.addView(leftView, lp);

            TextView rightView = getTextView(context, rateMaxValue, rateDt, i);
            rightContainer.addView(rightView, lpRight);
        }
        MyLineChart myLineChart = findViewById(R.id.myLineChart);
        myLineChart.setValue(valueList, maxValue, rateList, rateMaxValue);
    }

    private TextView getTextView(Context context, float maxValue, int dt, int i) {
        TextView textView = new TextView(context);
        int r = (int) (maxValue - dt * i);
        textView.setText(r + "");
        textView.setTextSize(5);
        return textView;
    }

    private float calculateMaxValue(float max, int dt) {
        float maxValue;
        if (max % dt != 0) {
            maxValue = (int) (max / dt) * dt + dt;
        } else {
            maxValue = (int) (max / dt) * dt;
        }
        return maxValue;
    }
}
