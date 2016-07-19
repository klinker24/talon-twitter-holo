/*
 * Copyright 2014 Luke Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.klinker.android.twitter.utils.text;

import android.content.Context;
import android.os.Handler;
import android.os.Vibrator;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

public class TouchableMovementMethod extends LinkMovementMethod {

    private TouchableSpan mPressedSpan;

    @Override
    public boolean onTouchEvent(TextView textView, final Spannable spannable, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mPressedSpan = getPressedSpan(textView, spannable, event);
            if (mPressedSpan != null) {
                mPressedSpan.setTouched(true);
                touched = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (touched && mPressedSpan != null) {
                            Vibrator v = (Vibrator) mPressedSpan.mContext.getSystemService(Context.VIBRATOR_SERVICE);
                            v.vibrate(25);

                            mPressedSpan.onLongClick();
                            mPressedSpan.setTouched(false);
                            mPressedSpan = null;
                            Selection.removeSelection(spannable);
                        }
                    }
                }, 500);
                Selection.setSelection(spannable, spannable.getSpanStart(mPressedSpan),
                        spannable.getSpanEnd(mPressedSpan));
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            TouchableSpan touchedSpan = getPressedSpan(textView, spannable, event);
            if (mPressedSpan != null && touchedSpan != mPressedSpan) {
                mPressedSpan.setTouched(false);
                touched = false;
                mPressedSpan = null;
                Selection.removeSelection(spannable);
            }
        } else if(event.getAction() == MotionEvent.ACTION_UP) {
            if (mPressedSpan != null) {
                mPressedSpan.onClick(textView);
                mPressedSpan.setTouched(false);
                mPressedSpan = null;
                Selection.removeSelection(spannable);
            }
        } else {
            if (mPressedSpan != null) {
                mPressedSpan.setTouched(false);
                touched = false;
                super.onTouchEvent(textView, spannable, event);
            }
            mPressedSpan = null;
            Selection.removeSelection(spannable);
        }
        return true;
    }

    private int lastX = 0;
    private int lastY = 0;

    private TouchableSpan getPressedSpan(TextView widget, Spannable spannable, MotionEvent event) {

        int x = (int) event.getX();
        int y = (int) event.getY();
        lastX = x;
        lastY = y;
        int deltaX = Math.abs(x-lastX);
        int deltaY = Math.abs(y-lastY);

        x -= widget.getTotalPaddingLeft();
        y -= widget.getTotalPaddingTop();

        x += widget.getScrollX();
        y += widget.getScrollY();

        Layout layout = widget.getLayout();
        int line = layout.getLineForVertical(y);
        int off = layout.getOffsetForHorizontal(line, x);
        int end = layout.getLineEnd(line);

        // offset seems like it can be one off in some cases
        // Could be what was causing issue 7 in the first place:
        // https://github.com/klinker24/Android-TextView-LinkBuilder/issues/7
        if (off != end && off != end - 1) {
            TouchableSpan[] link = spannable.getSpans(off, off, TouchableSpan.class);

            if (link.length > 0)
                return link[0];
        }

        return null;
    }

    private static TouchableMovementMethod sInstance;
    public static boolean touched = false;

    public static MovementMethod getInstance() {
        if (sInstance == null)
            sInstance = new TouchableMovementMethod();

        return sInstance;
    }
}
