package com.kzq.library.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Size;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.Nullable;

public class KSwitchView extends WrappedView {

    private static final int DEFAULT_WIDTH = 500;
    private static final int DEFAULT_HEIGHT = 240;
    private static final long DEFAULT_DURATION = 300L;

    private int mWidth = -1, mHeight = -1;

    private Paint containerStrokePaint;
    private RectF containerRectF = new RectF();
    private Paint containerFillPaint;
    private RectF fillRectF = new RectF();

    private Paint ovalPaint;
    private RectF btnRectF = new RectF();

    /**
     * cr:按钮半径
     * ci: 按钮最大移动距离
     */
    private float cr, ci;

    private final static int BTN_SHADOW_DEFAULT = 20;
    private float btnShadowRadius = BTN_SHADOW_DEFAULT;
    private float drawShadowRadius = btnShadowRadius;
    private float btnStart, btnEnd;
    private boolean checked;
    private float lastX = -1;
    private float downX = -1;
    private ValueAnimator leftAnim, rightAnim;

    public interface OnCheckChangeListener {
        void onCheckChanged(boolean isChecked, boolean isMotion);
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        setChecked(checked, false);
    }

    public void setChecked(final boolean checked, final boolean animated) {
        if (this.checked == checked) {
            return;
        }
        this.checked = checked;
        post(new Runnable() {
            @Override
            public void run() {
                if (onCheckChangeListener != null) {
                    onCheckChangeListener.onCheckChanged(checked, false);
                }
                if (animated) {
                    autoAnimation(checked);
                } else {
                    if (checked) {
                        btnRectF.right = btnEnd;
                        btnRectF.left = btnEnd - cr * 2;
                    } else {
                        btnRectF.left = btnStart;
                        btnRectF.right = btnStart + cr * 2;
                    }
                    fillRectF.set(containerRectF);
                    setFillColor();
                    invalidate();
                }
            }
        });
    }

    private OnCheckChangeListener onCheckChangeListener;

    public void setOnCheckChangeListener(OnCheckChangeListener onCheckChangeListener) {
        this.onCheckChangeListener = onCheckChangeListener;
    }

    public KSwitchView(Context context) {
        super(context);
        init();
    }

    public KSwitchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected Size getWrapSize() {
        int width = DEFAULT_WIDTH;
        if (mWidth != -1) {
            width = mWidth;
        }
        int height = DEFAULT_HEIGHT;
        if (mHeight != -1) {
            height = mHeight;
        }
        return new Size(width, height);
    }

    private void init() {
        setClickable(true);
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        containerStrokePaint = new Paint();
        containerStrokePaint.setStyle(Paint.Style.STROKE);
        containerStrokePaint.setStrokeWidth(6);
        containerStrokePaint.setColor(Color.parseColor("#66bcbcbc"));

        containerFillPaint = new Paint();
        containerFillPaint.setStyle(Paint.Style.FILL);
        containerFillPaint.setAntiAlias(true);
        containerFillPaint.setColor(Color.WHITE);
        containerFillPaint.setStrokeWidth(2);

        ovalPaint = new Paint();
        ovalPaint.setStyle(Paint.Style.FILL);
        ovalPaint.setAntiAlias(true);
        ovalPaint.setColor(Color.WHITE);

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if ((leftAnim != null && leftAnim.isRunning()) || (rightAnim != null && rightAnim.isRunning())) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float x = event.getX();
                float y = event.getY();
                downX = event.getRawX();
                if (x > btnRectF.left && x < btnRectF.right && y > btnRectF.top && y < btnRectF.bottom) {
                    drawShadowRadius = btnShadowRadius / 2;
                    lastX = event.getRawX();
                    invalidate();
                } else {
                    lastX = -1;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (lastX != -1) {
                    float rawX = event.getRawX();
                    if (checked) {
                        btnRectF.left += rawX - lastX;
                        btnRectF.left = Math.max(btnStart, btnRectF.left);
                        btnRectF.left = Math.min(btnEnd, btnRectF.left);
                        btnRectF.left = Math.min(btnRectF.right - cr * 2, btnRectF.left);
                    } else {
                        btnRectF.right += rawX - lastX;
                        btnRectF.right = Math.max(btnStart, btnRectF.right);
                        btnRectF.right = Math.min(btnEnd, btnRectF.right);
                        btnRectF.right = Math.max(btnRectF.left + cr * 2, btnRectF.right);
                    }
                    lastX = rawX;
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                drawShadowRadius = btnShadowRadius;
                if (event.getRawX() == downX) {
                    autoAnimation(!checked);
                } else {
                    if (checked && btnRectF.left < mWidth / 2f) {
                        autoAnimation(false);
                    } else if (!checked && btnRectF.right > mWidth / 2f) {
                        autoAnimation(true);
                    }
                }
                if (onCheckChangeListener != null) {
                    onCheckChangeListener.onCheckChanged(checked, true);
                }
                invalidate();
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRoundRect(fillRectF, mHeight / 2f, mHeight / 2f, containerFillPaint);

        canvas.drawRoundRect(containerRectF, mHeight / 2f, mHeight / 2f, containerStrokePaint);

        ovalPaint.setShadowLayer(drawShadowRadius, 0, drawShadowRadius / 2f, Color.parseColor("#ffc1c1c1"));
        canvas.drawRoundRect(btnRectF, cr, cr, ovalPaint);
    }


    private void autoAnimation(boolean isChecked) {
        this.checked = isChecked;
        if (isChecked) {
            float dt = Math.abs(btnRectF.right - btnEnd) / ci;
            if (dt > 0) {
                startRightAnim(btnRectF.right, btnEnd);
                rightAnim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        startFillAnim();
                        startLeftAnim(btnRectF.left, btnEnd - 2 * cr);
                    }
                });
            } else {
                startFillAnim();
                startLeftAnim(btnRectF.left, btnEnd - 2 * cr);
            }
        } else {
            float dt = Math.abs(btnRectF.left - btnStart) / ci;
            if (dt > 0) {
                startLeftAnim(btnRectF.left, btnStart);
                leftAnim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        startFillAnim();
                        startRightAnim(btnRectF.right, btnStart + 2 * cr);
                    }
                });
            } else {
                startFillAnim();
                startRightAnim(btnRectF.right, btnStart + 2 * cr);
            }
        }
    }

    private void startLeftAnim(float start, float end) {
        if (leftAnim != null) {
            leftAnim.cancel();
        }
        float dt = Math.abs(btnRectF.left - end) / ci;
        leftAnim = ValueAnimator.ofFloat(start, end);
        leftAnim.setDuration((long) (DEFAULT_DURATION * dt));
        leftAnim.setInterpolator(new OvershootInterpolator());
        leftAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                btnRectF.left = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        leftAnim.start();
    }

    private void startRightAnim(float start, float end) {
        if (rightAnim != null) {
            rightAnim.cancel();
        }
        float dt = Math.abs(btnRectF.right - end) / ci;
        rightAnim = ValueAnimator.ofFloat(start, end);
        rightAnim.setDuration((long) (DEFAULT_DURATION * dt));
        rightAnim.setInterpolator(new OvershootInterpolator());
        rightAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                btnRectF.right = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        rightAnim.start();
    }

    private ValueAnimator fillAnim;

    private void startFillAnim() {
        if (fillAnim != null) {
            fillAnim.cancel();
        }
        setFillColor();
        fillRectF.set(containerRectF.centerX(), containerRectF.centerY(), containerRectF.centerX(), containerRectF.centerY());
        fillAnim = ValueAnimator.ofFloat(0, 1);
        fillAnim.setDuration(DEFAULT_DURATION);
        fillAnim.setInterpolator(new DecelerateInterpolator());
        final float dx = containerRectF.width() / 2f;
        final float dy = containerRectF.height() / 2f;
        fillAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float dt = (float) valueAnimator.getAnimatedValue();
                fillRectF.left = containerRectF.centerX() - dx * dt;
                fillRectF.right = containerRectF.centerX() + dx * dt;
                fillRectF.top = containerRectF.centerY() - dy * dt;
                fillRectF.bottom = containerRectF.centerY() + dy * dt;
            }
        });
        fillAnim.start();
    }

    private void setFillColor() {
        if (checked) {
            containerFillPaint.setColor(Color.GREEN);
        } else {
            containerFillPaint.setColor(Color.WHITE);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();

        float swV2 = containerStrokePaint.getStrokeWidth() / 2f;
        containerRectF.set(swV2, swV2, mWidth - swV2, mHeight - swV2);

        cr = mHeight / 2f - btnShadowRadius;
        btnRectF.set(btnShadowRadius, btnShadowRadius, btnShadowRadius + cr * 2, mHeight - btnShadowRadius);

        btnStart = btnShadowRadius;
        btnEnd = mWidth - btnShadowRadius;
        ci = btnEnd - btnRectF.right;

    }
}
