/*
 * Copyright 2014 Luke Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.klinker.android.twitter.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import com.klinker.android.twitter.util.ColorUtils;

public class CircularProgressBar extends View {
    private static final String TAG = "CircularProgressBar";

    private static final double e = 2.71828;
    private static final double pi = 3.1415;

    private static final int DEFAULT_COLOR = 0xff3f51b5;
    private static final int DEFAULT_SHOWING_COLOR = 0xaa3f51b5;
    private static final int RADIUS = 48;
    private static final int DEFAULT_STROKE = RADIUS / 6;
    private static final int SMALLEST_ANGLE = 30;
    private static final int LARGEST_ANGLE = 335;
    private static final int STABLE_INCREASE = 4;

    private static final int STATE_WAITING = -2;
    private static final int STATE_SHOWING = -1;
    private static final int STATE_INCREASING = 0;
    private static final int STATE_DECREASING = 1;

    private Paint paint;
    private Paint showingPaint;
    private Paint shownPaint;
    private float currentStartAngle;
    private float currentSweepAngle;
    private RectF rectF;
    private RectF showingRectF;
    private RectF shownRectF;
    private float radius;
    private int resizedRadius = -1;
    private float padding;
    private int currentState = STATE_WAITING;

    private AnimationThread animator;
    private ColorUtils colorUtil = new ColorUtils();

    public CircularProgressBar(Context context) {
        this(context, null);
    }

    public CircularProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircularProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(toPx(DEFAULT_STROKE));
        paint.setColor(DEFAULT_COLOR);
        paint.setAlpha(0);

        showingPaint = new Paint();
        showingPaint.setAntiAlias(true);
        showingPaint.setStyle(Paint.Style.STROKE);
        showingPaint.setStrokeWidth(toPx(DEFAULT_STROKE));
        showingPaint.setColor(DEFAULT_SHOWING_COLOR);

        shownPaint = new Paint();
        shownPaint.setAntiAlias(true);
        shownPaint.setStyle(Paint.Style.STROKE);
        shownPaint.setStrokeWidth(toPx(DEFAULT_STROKE));
        shownPaint.setColor(DEFAULT_SHOWING_COLOR);

        currentStartAngle = 0;
        currentSweepAngle = SMALLEST_ANGLE;

        radius = toPx(RADIUS);
        padding = toPx(DEFAULT_STROKE);

        animator = new AnimationThread(this);
    }

    public void setColor(int color) {
        paint.setColor(color);
        showingPaint.setColor(colorUtil.adjustAlpha(color, .5f));
        shownPaint.setColor(colorUtil.adjustAlpha(color, .5f));
    }

    public void setColorResource(int resourceId) {
        paint.setColor(getResources().getColor(resourceId));
        showingPaint.setColor(colorUtil.adjustAlpha(getResources().getColor(resourceId), .5f));
        shownPaint.setColor(colorUtil.adjustAlpha(getResources().getColor(resourceId), .5f));
    }

    public void setRadius(int sizeInDp) {
        radius = toPx(sizeInDp);
        padding = radius / 6;
        paint.setStrokeWidth(padding);
        showingPaint.setStrokeWidth(padding);
        shownPaint.setStrokeWidth(padding);
        resizedRadius = sizeInDp;
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setRunning(true);
            }
        }, 250);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        setRunning(false);
    }

    public void setRunning(boolean running) {
        animator.setRunning(running);

        try {
            if (running) animator.start();
        } catch (Exception e) {
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);

        rectF = new RectF(w / 2 - radius / 2 - padding, h / 2 - radius / 2 - padding, w / 2 + radius / 2 + padding, h / 2 + radius / 2 + padding);
        showingRectF = new RectF(w / 2, h / 2, w / 2, h / 2);
        shownRectF = new RectF(w / 2, h / 2, w / 2, h / 2);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension((int) (radius * 2.5), (int) (radius * 2.5));
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawArc(showingRectF, 0, 360, false, showingPaint);
        canvas.drawArc(shownRectF, 0, 360, false, shownPaint);

        if (currentState != STATE_WAITING) {
            canvas.drawArc(rectF, currentStartAngle, currentSweepAngle, false, paint);
        }
    }

    public void restartSpinner() {
        currentState = STATE_WAITING;
        init();
        if (resizedRadius != -1) {
            setRadius(resizedRadius);
        }
    }

    // get the speed of the animation that we want based on the current sweep angle, needs a normal distribution
    private float interpolateSpeed(float sweepAngle) {
        double increase = getUnstableIncrease();
        double speed = (increase * Math.pow(e, -1 * pi * Math.pow(sweepAngle / 100 - 1.5, 2))) + increase;
        return (float) speed;
    }

    private double getUnstableIncrease() {
        return STABLE_INCREASE * 1.4;
    }

    private float toPx(int dp) {
        Resources r = getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    public class AnimationThread extends Thread {
        public View view;
        private boolean running = false;

        public AnimationThread(View v) {
            super();
            view = v;
        }

        public void setRunning(boolean run) {
            running = run;
        }

        private final static int MAX_FPS = 60;
        private final static int MAX_FRAME_SKIPS = 5;
        private final static int FRAME_PERIOD = 1000 / MAX_FPS;

        @Override
        public void run() {
            long beginTime;
            long timeDiff;
            int sleepTime;
            int framesSkipped;

            while (running) {
                try {
                    beginTime = System.currentTimeMillis();
                    framesSkipped = 0;

                    updateView();
                    postInvalidate();

                    timeDiff = System.currentTimeMillis() - beginTime;
                    sleepTime = (int) (FRAME_PERIOD - timeDiff);

                    if (sleepTime > 0) {
                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {
                        }
                    }

                    while (sleepTime < 0 && framesSkipped < MAX_FRAME_SKIPS) {
                        updateView();
                        sleepTime += FRAME_PERIOD;
                        framesSkipped++;
                    }
                } finally {
                    postInvalidate();
                }
            }
        }

        private void postInvalidate() {
            try {
                view.postInvalidate();
            } catch (Exception e) {
            }
        }

        public void updateView() {
            if (rectF == null) {
                return;
            }

            switch (currentState) {
                case STATE_WAITING:
                    int dist = STABLE_INCREASE * 2;
                    showingRectF = new RectF(showingRectF.left - dist, showingRectF.top - dist, showingRectF.right + dist, showingRectF.bottom + dist);
                    if (showingRectF.right > rectF.right) {
                        currentState = STATE_SHOWING;
                        showingRectF = new RectF(rectF.left, rectF.top, rectF.right, rectF.bottom);
                    }

                    break;
                case STATE_SHOWING:
                    dist = STABLE_INCREASE;
                    shownRectF = new RectF(shownRectF.left - dist, shownRectF.top - dist, shownRectF.right + dist, shownRectF.bottom + dist);
                    if (shownRectF.right > rectF.right - padding) {
                        currentState = STATE_INCREASING;
                        shownRectF = new RectF(rectF.left + padding, rectF.top + padding, rectF.right - padding, rectF.bottom - padding);
                    }
                case STATE_INCREASING:
                    currentStartAngle += STABLE_INCREASE;
                    currentSweepAngle += interpolateSpeed(currentSweepAngle);

                    if (currentSweepAngle >= LARGEST_ANGLE) {
                        currentState = STATE_DECREASING;
                    }

                    break;
                case STATE_DECREASING:
                    currentStartAngle += interpolateSpeed(currentSweepAngle);
                    currentSweepAngle -= STABLE_INCREASE;

                    if (currentSweepAngle <= SMALLEST_ANGLE) {
                        currentState = STATE_INCREASING;
                    }

                    break;
            }

            if (currentState != STATE_WAITING) {
                int alpha = showingPaint.getAlpha();
                if (alpha > 0) {
                    alpha = alpha - STABLE_INCREASE;
                    if (alpha < 0) {
                        alpha = 0;
                    }

                    showingPaint.setAlpha(alpha);
                }

                alpha = paint.getAlpha();
                if (alpha < 255) {
                    alpha = alpha + STABLE_INCREASE;
                    if (alpha > 255) {
                        alpha = 255;
                    }

                    paint.setAlpha(alpha);
                }
            }

            if (currentState != STATE_SHOWING) {
                int alpha = shownPaint.getAlpha();
                if (alpha > 0) {
                    alpha = alpha - STABLE_INCREASE;
                    if (alpha < 0) {
                        alpha = 0;
                    }

                    shownPaint.setAlpha(alpha);
                }
            }
        }
    }
}
