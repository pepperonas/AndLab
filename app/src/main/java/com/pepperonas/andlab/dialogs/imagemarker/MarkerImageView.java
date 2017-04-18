/*
 * Copyright (c) 2017 Martin Pfeffer
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

package com.pepperonas.andlab.dialogs.imagemarker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import com.pepperonas.andlab.R;
import java.util.HashSet;

/**
 * @author Martin Pfeffer
 * @see <a href="https://celox.io">https://celox.io</a>
 */
public class MarkerImageView extends android.support.v7.widget.AppCompatImageView {

    private static final String TAG = "MarkerImageView";

    /**
     * Main bitmap
     */
    private Bitmap mBitmap = null;

    /**
     * Paint to draw circles
     */
    private Paint mCirclePaint;

    // Radius limit in pixels
    private int mRadius = 20;
    private int mCircleLimit = 1;
    private int mColor = Color.RED;
    private float mStrokeWidth = 8;
    private Style mPaintStyle = Style.STROKE;
    private int mNewBitmapWidth;
    private int mNewBitmapHeight;

    public void setRadius(int radius) {
        mRadius = radius;
    }

    public void setCircleLimit(int circleLimit) {
        mCircleLimit = circleLimit;
    }

    public void setCircleColor(int color) {
        mColor = color;
    }

    public void setColor(int color) {
        this.mColor = color;
    }

    public void setStrokeWidth(float strokeWidth) {
        this.mStrokeWidth = strokeWidth;
    }

    public void setPaintStyle(Style paintStyle) {
        this.mPaintStyle = paintStyle;
    }

    public int getPosX() {
        return xTouch;
    }

    public int getPosY() {
        return yTouch;
    }

    public int getRelativeX() {
        return (int) ((float) xTouch / (float) getWidth() * 100);
    }

    public int getRelativeY() {
        return (int) ((float) yTouch / (float) getHeight() * 100);
    }

    /**
     * All available circles
     */
    private HashSet<CircleArea> mCircles = new HashSet<CircleArea>(mCircleLimit);
    private SparseArray<CircleArea> mCirclePointer = new SparseArray<CircleArea>(mCircleLimit);


    /**
     * Stores data about single circle
     */
    private static class CircleArea {

        int radius;
        int centerX;
        int centerY;

        CircleArea(int centerX, int centerY, int radius) {
            this.radius = radius;
            this.centerX = centerX;
            this.centerY = centerY;
        }

        @Override
        public String toString() {
            return "Circle[" + centerX + ", " + centerY + ", " + radius + "]";
        }
    }


    /**
     * Default constructor
     *
     * @param context {@link android.content.Context}
     */
    public MarkerImageView(final Context context) {
        super(context);
        init(context);
    }

    public MarkerImageView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MarkerImageView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(final Context ct) {
        // Generate bitmap used for background
        mBitmap = BitmapFactory.decodeResource(ct.getResources(), R.drawable.android);

        mCirclePaint = new Paint();
        mCirclePaint.setColor(mColor);
        mCirclePaint.setStrokeWidth(mStrokeWidth);
        mCirclePaint.setStyle(mPaintStyle);

        setImageDrawable(ct.getResources().getDrawable(R.drawable.android));
    }

    @Override
    public void onDraw(final Canvas canvas) {
        // background bitmap to cover all area

        Log.d(TAG, "onDraw: ...");

        float scaleX = 1f;
        float scaleY = 1f;

        if (mBitmap.getWidth() > getWidth()) {
            scaleX = (float) mBitmap.getWidth() / (float) getWidth();
        }
        if (mBitmap.getHeight() > getHeight()) {
            scaleY = (float) mBitmap.getHeight() / (float) getHeight();
        }
        float scale = scaleX > scaleY ? scaleX : scaleY;
        mNewBitmapWidth = (int) (mBitmap.getWidth() / scale);
        mNewBitmapHeight = (int) (mBitmap.getHeight() / scale);
        int centerX = (getWidth() - mNewBitmapWidth) / 2;
        int centerY = (getHeight() - mNewBitmapHeight) / 2;
        mBitmap = Bitmap.createScaledBitmap(mBitmap, mNewBitmapWidth,
            mNewBitmapHeight, false);

        canvas.drawBitmap(mBitmap, centerX, centerY, null);

        for (CircleArea circle : mCircles) {
            canvas.drawCircle(circle.centerX, circle.centerY, circle.radius, mCirclePaint);
        }
    }

    int xTouch = 0;
    int yTouch = 0;

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        boolean handled = false;

        CircleArea touchedCircle;

        int pointerId;
        int actionIndex = event.getActionIndex();

        // get touch event coordinates and make transparent circle from it
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                // it's the first pointer, so clear all existing pointers data
                clearCirclePointer();

                xTouch = (int) event.getX(0);
                yTouch = (int) event.getY(0);

                // check if we've touched inside some circle
                touchedCircle = obtainTouchedCircle(xTouch, yTouch);
                touchedCircle.centerX = xTouch;
                touchedCircle.centerY = yTouch;
                mCirclePointer.put(event.getPointerId(0), touchedCircle);

                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                Log.w(TAG, "Pointer down");
                // It secondary pointers, so obtain their ids and check circles
                pointerId = event.getPointerId(actionIndex);

                xTouch = (int) event.getX(actionIndex);
                yTouch = (int) event.getY(actionIndex);

                // check if we've touched inside some circle
                touchedCircle = obtainTouchedCircle(xTouch, yTouch);

                mCirclePointer.put(pointerId, touchedCircle);
                touchedCircle.centerX = xTouch;
                touchedCircle.centerY = yTouch;
                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_MOVE:
                final int pointerCount = event.getPointerCount();

                Log.w(TAG, "Move");

                for (actionIndex = 0; actionIndex < pointerCount; actionIndex++) {
                    // Some pointer has moved, search it by pointer id
                    pointerId = event.getPointerId(actionIndex);

                    xTouch = (int) event.getX(actionIndex);
                    yTouch = (int) event.getY(actionIndex);

                    touchedCircle = mCirclePointer.get(pointerId);

                    if (null != touchedCircle) {
                        touchedCircle.centerX = xTouch;
                        touchedCircle.centerY = yTouch;
                    }
                }
                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_UP:
                clearCirclePointer();
                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_POINTER_UP:
                // not general pointer was up
                pointerId = event.getPointerId(actionIndex);

                mCirclePointer.remove(pointerId);
                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_CANCEL:
                handled = true;
                break;

            default:
                // do nothing
                break;
        }

        return super.onTouchEvent(event) || handled;
    }

    /**
     * Clears all CircleArea - pointer id relations
     */
    private void clearCirclePointer() {
        Log.w(TAG, "clearCirclePointer");

        mCirclePointer.clear();
    }

    /**
     * Search and creates new (if needed) circle based on touch area
     *
     * @param xTouch int x of touch
     * @param yTouch int y of touch
     * @return obtained {@link CircleArea}
     */
    private CircleArea obtainTouchedCircle(final int xTouch, final int yTouch) {
        CircleArea touchedCircle = getTouchedCircle(xTouch, yTouch);

        if (null == touchedCircle) {
            touchedCircle = new CircleArea(xTouch, yTouch, mRadius);

            if (mCircles.size() == mCircleLimit) {
                Log.w(TAG, "Clear all circles, size is " + mCircles.size());
                // remove first circle
                mCircles.clear();
            }

            Log.w(TAG, "Added circle " + touchedCircle);
            mCircles.add(touchedCircle);
        }

        return touchedCircle;
    }

    /**
     * Determines touched circle
     *
     * @param xTouch int x touch coordinate
     * @param yTouch int y touch coordinate
     * @return {@link CircleArea} touched circle or null if no circle has been touched
     */
    private CircleArea getTouchedCircle(final int xTouch, final int yTouch) {
        CircleArea touched = null;

        for (CircleArea circle : mCircles) {
            if ((circle.centerX - xTouch) * (circle.centerX - xTouch)
                + (circle.centerY - yTouch) * (circle.centerY - yTouch)
                <= circle.radius * circle.radius) {
                touched = circle;
                break;
            }
        }

        return touched;
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}
