package com.klinker.android.twitter.manipulations.widgets;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ScrollerCompat;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import java.util.Arrays;

public class ViewDragHelper {
    private static final String TAG = "ViewDragHelper";
    public static final int INVALID_POINTER = -1;
    public static final int STATE_IDLE = 0;
    public static final int STATE_DRAGGING = 1;
    public static final int STATE_SETTLING = 2;
    public static final int EDGE_LEFT = 1;
    public static final int EDGE_RIGHT = 2;
    public static final int EDGE_TOP = 4;
    public static final int EDGE_BOTTOM = 8;
    public static final int EDGE_ALL = 15;
    public static final int DIRECTION_HORIZONTAL = 1;
    public static final int DIRECTION_VERTICAL = 2;
    public static final int DIRECTION_ALL = 3;
    private static final int EDGE_SIZE = 20;
    private static final int BASE_SETTLE_DURATION = 256;
    private static final int MAX_SETTLE_DURATION = 600;
    private int mDragState;
    private int mTouchSlop;
    private int mActivePointerId = -1;
    private float[] mInitialMotionX;
    private float[] mInitialMotionY;
    private float[] mLastMotionX;
    private float[] mLastMotionY;
    private int[] mInitialEdgesTouched;
    private int[] mEdgeDragsInProgress;
    private int[] mEdgeDragsLocked;
    private int mPointersDown;
    private VelocityTracker mVelocityTracker;
    private float mMaxVelocity;
    private float mMinVelocity;
    private int mEdgeSize;
    private int mTrackingEdges;
    private ScrollerCompat mScroller;
    private final ViewDragHelper.Callback mCallback;
    private View mCapturedView;
    private boolean mReleaseInProgress;
    private final ViewGroup mParentView;
    private static final Interpolator sInterpolator = new Interpolator() {
        public float getInterpolation(float t) {
            --t;
            return t * t * t * t * t + 1.0F;
        }
    };
    private final Runnable mSetIdleRunnable = new Runnable() {
        public void run() {
            ViewDragHelper.this.setDragState(0);
        }
    };

    public static ViewDragHelper create(ViewGroup forParent, ViewDragHelper.Callback cb) {
        return new ViewDragHelper(forParent.getContext(), forParent, cb);
    }

    public static ViewDragHelper create(ViewGroup forParent, float sensitivity, ViewDragHelper.Callback cb) {
        ViewDragHelper helper = create(forParent, cb);
        helper.mTouchSlop = (int)((float)helper.mTouchSlop * (1.0F / sensitivity));
        return helper;
    }

    private ViewDragHelper(Context context, ViewGroup forParent, ViewDragHelper.Callback cb) {
        if(forParent == null) {
            throw new IllegalArgumentException("Parent view may not be null");
        } else if(cb == null) {
            throw new IllegalArgumentException("Callback may not be null");
        } else {
            this.mParentView = forParent;
            this.mCallback = cb;
            ViewConfiguration vc = ViewConfiguration.get(context);
            float density = context.getResources().getDisplayMetrics().density;
            this.mEdgeSize = (int)(20.0F * density + 0.5F);
            this.mTouchSlop = vc.getScaledTouchSlop();
            this.mMaxVelocity = (float)vc.getScaledMaximumFlingVelocity();
            this.mMinVelocity = (float)vc.getScaledMinimumFlingVelocity();
            this.mScroller = ScrollerCompat.create(context, sInterpolator);
        }
    }

    public void setMinVelocity(float minVel) {
        this.mMinVelocity = minVel;
    }

    public float getMinVelocity() {
        return this.mMinVelocity;
    }

    public int getViewDragState() {
        return this.mDragState;
    }

    public void setEdgeTrackingEnabled(int edgeFlags) {
        this.mTrackingEdges = edgeFlags;
    }

    public int getEdgeSize() {
        return this.mEdgeSize;
    }

    public void setEdgeSize(int size) {
        mEdgeSize = size;
    }

    public void captureChildView(View childView, int activePointerId) {
        if(childView.getParent() != this.mParentView) {
            throw new IllegalArgumentException("captureChildView: parameter must be a descendant of the ViewDragHelper\'s tracked parent view (" + this.mParentView + ")");
        } else {
            this.mCapturedView = childView;
            this.mActivePointerId = activePointerId;
            this.mCallback.onViewCaptured(childView, activePointerId);
            this.setDragState(1);
        }
    }

    public View getCapturedView() {
        return this.mCapturedView;
    }

    public int getActivePointerId() {
        return this.mActivePointerId;
    }

    public int getTouchSlop() {
        return this.mTouchSlop;
    }

    public void cancel() {
        this.mActivePointerId = -1;
        this.clearMotionHistory();
        if(this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }

    }

    public void abort() {
        this.cancel();
        if(this.mDragState == 2) {
            int oldX = this.mScroller.getCurrX();
            int oldY = this.mScroller.getCurrY();
            this.mScroller.abortAnimation();
            int newX = this.mScroller.getCurrX();
            int newY = this.mScroller.getCurrY();
            this.mCallback.onViewPositionChanged(this.mCapturedView, newX, newY, newX - oldX, newY - oldY);
        }

        this.setDragState(0);
    }

    public boolean smoothSlideViewTo(View child, int finalLeft, int finalTop) {
        this.mCapturedView = child;
        this.mActivePointerId = -1;
        boolean continueSliding = this.forceSettleCapturedViewAt(finalLeft, finalTop, 0, 0);
        if(!continueSliding && this.mDragState == 0 && this.mCapturedView != null) {
            this.mCapturedView = null;
        }

        return continueSliding;
    }

    public boolean settleCapturedViewAt(int finalLeft, int finalTop) {
        if(!this.mReleaseInProgress) {
            throw new IllegalStateException("Cannot settleCapturedViewAt outside of a call to Callback#onViewReleased");
        } else {
            return this.forceSettleCapturedViewAt(finalLeft, finalTop, (int)VelocityTrackerCompat.getXVelocity(this.mVelocityTracker, this.mActivePointerId), (int)VelocityTrackerCompat.getYVelocity(this.mVelocityTracker, this.mActivePointerId));
        }
    }

    private boolean forceSettleCapturedViewAt(int finalLeft, int finalTop, int xvel, int yvel) {
        int startLeft = this.mCapturedView.getLeft();
        int startTop = this.mCapturedView.getTop();
        int dx = finalLeft - startLeft;
        int dy = finalTop - startTop;
        if(dx == 0 && dy == 0) {
            this.mScroller.abortAnimation();
            this.setDragState(0);
            return false;
        } else {
            int duration = this.computeSettleDuration(this.mCapturedView, dx, dy, xvel, yvel);
            this.mScroller.startScroll(startLeft, startTop, dx, dy, duration);
            this.setDragState(2);
            return true;
        }
    }

    private int computeSettleDuration(View child, int dx, int dy, int xvel, int yvel) {
        xvel = this.clampMag(xvel, (int)this.mMinVelocity, (int)this.mMaxVelocity);
        yvel = this.clampMag(yvel, (int)this.mMinVelocity, (int)this.mMaxVelocity);
        int absDx = Math.abs(dx);
        int absDy = Math.abs(dy);
        int absXVel = Math.abs(xvel);
        int absYVel = Math.abs(yvel);
        int addedVel = absXVel + absYVel;
        int addedDistance = absDx + absDy;
        float xweight = xvel != 0?(float)absXVel / (float)addedVel:(float)absDx / (float)addedDistance;
        float yweight = yvel != 0?(float)absYVel / (float)addedVel:(float)absDy / (float)addedDistance;
        int xduration = this.computeAxisDuration(dx, xvel, this.mCallback.getViewHorizontalDragRange(child));
        int yduration = this.computeAxisDuration(dy, yvel, this.mCallback.getViewVerticalDragRange(child));
        return (int)((float)xduration * xweight + (float)yduration * yweight);
    }

    private int computeAxisDuration(int delta, int velocity, int motionRange) {
        if(delta == 0) {
            return 0;
        } else {
            int width = this.mParentView.getWidth();
            int halfWidth = width / 2;
            float distanceRatio = Math.min(1.0F, (float)Math.abs(delta) / (float)width);
            float distance = (float)halfWidth + (float)halfWidth * this.distanceInfluenceForSnapDuration(distanceRatio);
            velocity = Math.abs(velocity);
            int duration;
            if(velocity > 0) {
                duration = 4 * Math.round(1000.0F * Math.abs(distance / (float)velocity));
            } else {
                float range = (float)Math.abs(delta) / (float)motionRange;
                duration = (int)((range + 1.0F) * 256.0F);
            }

            return Math.min(duration, 600);
        }
    }

    private int clampMag(int value, int absMin, int absMax) {
        int absValue = Math.abs(value);
        return absValue < absMin?0:(absValue > absMax?(value > 0?absMax:-absMax):value);
    }

    private float clampMag(float value, float absMin, float absMax) {
        float absValue = Math.abs(value);
        return absValue < absMin?0.0F:(absValue > absMax?(value > 0.0F?absMax:-absMax):value);
    }

    private float distanceInfluenceForSnapDuration(float f) {
        f -= 0.5F;
        f = (float)((double)f * 0.4712389167638204D);
        return (float)Math.sin((double)f);
    }

    public void flingCapturedView(int minLeft, int minTop, int maxLeft, int maxTop) {
        if(!this.mReleaseInProgress) {
            throw new IllegalStateException("Cannot flingCapturedView outside of a call to Callback#onViewReleased");
        } else {
            this.mScroller.fling(this.mCapturedView.getLeft(), this.mCapturedView.getTop(), (int)VelocityTrackerCompat.getXVelocity(this.mVelocityTracker, this.mActivePointerId), (int)VelocityTrackerCompat.getYVelocity(this.mVelocityTracker, this.mActivePointerId), minLeft, maxLeft, minTop, maxTop);
            this.setDragState(2);
        }
    }

    public boolean continueSettling(boolean deferCallbacks) {
        if(this.mDragState == 2) {
            boolean keepGoing = this.mScroller.computeScrollOffset();
            int x = this.mScroller.getCurrX();
            int y = this.mScroller.getCurrY();
            int dx = x - this.mCapturedView.getLeft();
            int dy = y - this.mCapturedView.getTop();
            if(dx != 0) {
                this.mCapturedView.offsetLeftAndRight(dx);
            }

            if(dy != 0) {
                this.mCapturedView.offsetTopAndBottom(dy);
            }

            if(dx != 0 || dy != 0) {
                this.mCallback.onViewPositionChanged(this.mCapturedView, x, y, dx, dy);
            }

            if(keepGoing && x == this.mScroller.getFinalX() && y == this.mScroller.getFinalY()) {
                this.mScroller.abortAnimation();
                keepGoing = false;
            }

            if(!keepGoing) {
                if(deferCallbacks) {
                    this.mParentView.post(this.mSetIdleRunnable);
                } else {
                    this.setDragState(0);
                }
            }
        }

        return this.mDragState == 2;
    }

    private void dispatchViewReleased(float xvel, float yvel) {
        this.mReleaseInProgress = true;
        this.mCallback.onViewReleased(this.mCapturedView, xvel, yvel);
        this.mReleaseInProgress = false;
        if(this.mDragState == 1) {
            this.setDragState(0);
        }

    }

    private void clearMotionHistory() {
        if(this.mInitialMotionX != null) {
            Arrays.fill(this.mInitialMotionX, 0.0F);
            Arrays.fill(this.mInitialMotionY, 0.0F);
            Arrays.fill(this.mLastMotionX, 0.0F);
            Arrays.fill(this.mLastMotionY, 0.0F);
            Arrays.fill(this.mInitialEdgesTouched, 0);
            Arrays.fill(this.mEdgeDragsInProgress, 0);
            Arrays.fill(this.mEdgeDragsLocked, 0);
            this.mPointersDown = 0;
        }
    }

    private void clearMotionHistory(int pointerId) {
        if(this.mInitialMotionX != null) {
            this.mInitialMotionX[pointerId] = 0.0F;
            this.mInitialMotionY[pointerId] = 0.0F;
            this.mLastMotionX[pointerId] = 0.0F;
            this.mLastMotionY[pointerId] = 0.0F;
            this.mInitialEdgesTouched[pointerId] = 0;
            this.mEdgeDragsInProgress[pointerId] = 0;
            this.mEdgeDragsLocked[pointerId] = 0;
            this.mPointersDown &= ~(1 << pointerId);
        }
    }

    private void ensureMotionHistorySizeForId(int pointerId) {
        if(this.mInitialMotionX == null || this.mInitialMotionX.length <= pointerId) {
            float[] imx = new float[pointerId + 1];
            float[] imy = new float[pointerId + 1];
            float[] lmx = new float[pointerId + 1];
            float[] lmy = new float[pointerId + 1];
            int[] iit = new int[pointerId + 1];
            int[] edip = new int[pointerId + 1];
            int[] edl = new int[pointerId + 1];
            if(this.mInitialMotionX != null) {
                System.arraycopy(this.mInitialMotionX, 0, imx, 0, this.mInitialMotionX.length);
                System.arraycopy(this.mInitialMotionY, 0, imy, 0, this.mInitialMotionY.length);
                System.arraycopy(this.mLastMotionX, 0, lmx, 0, this.mLastMotionX.length);
                System.arraycopy(this.mLastMotionY, 0, lmy, 0, this.mLastMotionY.length);
                System.arraycopy(this.mInitialEdgesTouched, 0, iit, 0, this.mInitialEdgesTouched.length);
                System.arraycopy(this.mEdgeDragsInProgress, 0, edip, 0, this.mEdgeDragsInProgress.length);
                System.arraycopy(this.mEdgeDragsLocked, 0, edl, 0, this.mEdgeDragsLocked.length);
            }

            this.mInitialMotionX = imx;
            this.mInitialMotionY = imy;
            this.mLastMotionX = lmx;
            this.mLastMotionY = lmy;
            this.mInitialEdgesTouched = iit;
            this.mEdgeDragsInProgress = edip;
            this.mEdgeDragsLocked = edl;
        }

    }

    private void saveInitialMotion(float x, float y, int pointerId) {
        this.ensureMotionHistorySizeForId(pointerId);
        this.mInitialMotionX[pointerId] = this.mLastMotionX[pointerId] = x;
        this.mInitialMotionY[pointerId] = this.mLastMotionY[pointerId] = y;
        this.mInitialEdgesTouched[pointerId] = this.getEdgesTouched((int)x, (int)y);
        this.mPointersDown |= 1 << pointerId;
    }

    private void saveLastMotion(MotionEvent ev) {
        int pointerCount = MotionEventCompat.getPointerCount(ev);

        for(int i = 0; i < pointerCount; ++i) {
            int pointerId = MotionEventCompat.getPointerId(ev, i);
            float x = MotionEventCompat.getX(ev, i);
            float y = MotionEventCompat.getY(ev, i);
            this.mLastMotionX[pointerId] = x;
            this.mLastMotionY[pointerId] = y;
        }

    }

    public boolean isPointerDown(int pointerId) {
        return (this.mPointersDown & 1 << pointerId) != 0;
    }

    void setDragState(int state) {
        if(this.mDragState != state) {
            this.mDragState = state;
            this.mCallback.onViewDragStateChanged(state);
            if(this.mDragState == 0) {
                this.mCapturedView = null;
            }
        }

    }

    boolean tryCaptureViewForDrag(View toCapture, int pointerId) {
        if(toCapture == this.mCapturedView && this.mActivePointerId == pointerId) {
            return true;
        } else if(toCapture != null && this.mCallback.tryCaptureView(toCapture, pointerId)) {
            this.mActivePointerId = pointerId;
            this.captureChildView(toCapture, pointerId);
            return true;
        } else {
            return false;
        }
    }

    protected boolean canScroll(View v, boolean checkV, int dx, int dy, int x, int y) {
        if(v instanceof ViewGroup) {
            ViewGroup group = (ViewGroup)v;
            int scrollX = v.getScrollX();
            int scrollY = v.getScrollY();
            int count = group.getChildCount();

            for(int i = count - 1; i >= 0; --i) {
                View child = group.getChildAt(i);
                if(x + scrollX >= child.getLeft() && x + scrollX < child.getRight() && y + scrollY >= child.getTop() && y + scrollY < child.getBottom() && this.canScroll(child, true, dx, dy, x + scrollX - child.getLeft(), y + scrollY - child.getTop())) {
                    return true;
                }
            }
        }

        return checkV && (ViewCompat.canScrollHorizontally(v, -dx) || ViewCompat.canScrollVertically(v, -dy));
    }

    public boolean shouldInterceptTouchEvent(MotionEvent ev) {
        int action = MotionEventCompat.getActionMasked(ev);
        int actionIndex = MotionEventCompat.getActionIndex(ev);
        if(action == 0) {
            this.cancel();
        }

        if(this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }

        this.mVelocityTracker.addMovement(ev);
        int pointerId;
        float i;
        int var23;
        View var24;
        switch(action) {
            case 0:
                float var22 = ev.getX();
                i = ev.getY();
                var23 = MotionEventCompat.getPointerId(ev, 0);
                this.saveInitialMotion(var22, i, var23);
                var24 = this.findTopChildUnder((int)var22, (int)i);
                if(var24 == this.mCapturedView && this.mDragState == 2) {
                    this.tryCaptureViewForDrag(var24, var23);
                }

                int var26 = this.mInitialEdgesTouched[var23];
                if((var26 & this.mTrackingEdges) != 0) {
                    this.mCallback.onEdgeTouched(var26 & this.mTrackingEdges, var23);
                }
                break;
            case 1:
            case 3:
                this.cancel();
                break;
            case 2:
                pointerId = MotionEventCompat.getPointerCount(ev);

                for(int var21 = 0; var21 < pointerId; ++var21) {
                    var23 = MotionEventCompat.getPointerId(ev, var21);
                    float var25 = MotionEventCompat.getX(ev, var21);
                    float y = MotionEventCompat.getY(ev, var21);
                    float dx = var25 - this.mInitialMotionX[var23];
                    float dy = y - this.mInitialMotionY[var23];
                    View toCapture = this.findTopChildUnder((int)var25, (int)y);
                    boolean pastSlop = toCapture != null && this.checkTouchSlop(toCapture, dx, dy);
                    if(pastSlop) {
                        int oldLeft = toCapture.getLeft();
                        int targetLeft = oldLeft + (int)dx;
                        int newLeft = this.mCallback.clampViewPositionHorizontal(toCapture, targetLeft, (int)dx);
                        int oldTop = toCapture.getTop();
                        int targetTop = oldTop + (int)dy;
                        int newTop = this.mCallback.clampViewPositionVertical(toCapture, targetTop, (int)dy);
                        int horizontalDragRange = this.mCallback.getViewHorizontalDragRange(toCapture);
                        int verticalDragRange = this.mCallback.getViewVerticalDragRange(toCapture);
                        if((horizontalDragRange == 0 || horizontalDragRange > 0 && newLeft == oldLeft) && (verticalDragRange == 0 || verticalDragRange > 0 && newTop == oldTop)) {
                            break;
                        }
                    }

                    this.reportNewEdgeDrags(dx, dy, var23);
                    if(this.mDragState == 1 || pastSlop && this.tryCaptureViewForDrag(toCapture, var23)) {
                        break;
                    }
                }

                this.saveLastMotion(ev);
            case 4:
            default:
                break;
            case 5:
                pointerId = MotionEventCompat.getPointerId(ev, actionIndex);
                i = MotionEventCompat.getX(ev, actionIndex);
                float pointerId1 = MotionEventCompat.getY(ev, actionIndex);
                this.saveInitialMotion(i, pointerId1, pointerId);
                if(this.mDragState == 0) {
                    int x = this.mInitialEdgesTouched[pointerId];
                    if((x & this.mTrackingEdges) != 0) {
                        this.mCallback.onEdgeTouched(x & this.mTrackingEdges, pointerId);
                    }
                } else if(this.mDragState == 2) {
                    var24 = this.findTopChildUnder((int)i, (int)pointerId1);
                    if(var24 == this.mCapturedView) {
                        this.tryCaptureViewForDrag(var24, pointerId);
                    }
                }
                break;
            case 6:
                pointerId = MotionEventCompat.getPointerId(ev, actionIndex);
                this.clearMotionHistory(pointerId);
        }

        return this.mDragState == 1;
    }

    public void processTouchEvent(MotionEvent ev) {
        int action = MotionEventCompat.getActionMasked(ev);
        int actionIndex = MotionEventCompat.getActionIndex(ev);
        if(action == 0) {
            this.cancel();
        }

        if(this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }

        this.mVelocityTracker.addMovement(ev);
        int pointerId;
        int newActivePointer;
        int pointerCount;
        int i;
        int id;
        float x;
        float y;
        float var13;
        float var14;
        View var15;
        switch(action) {
            case 0:
                float var12 = ev.getX();
                var13 = ev.getY();
                pointerCount = MotionEventCompat.getPointerId(ev, 0);
                var15 = this.findTopChildUnder((int)var12, (int)var13);
                this.saveInitialMotion(var12, var13, pointerCount);
                this.tryCaptureViewForDrag(var15, pointerCount);
                id = this.mInitialEdgesTouched[pointerCount];
                if((id & this.mTrackingEdges) != 0) {
                    this.mCallback.onEdgeTouched(id & this.mTrackingEdges, pointerCount);
                }
                break;
            case 1:
                if(this.mDragState == 1) {
                    this.releaseViewForPointerUp();
                }

                this.cancel();
                break;
            case 2:
                if(this.mDragState == 1) {
                    pointerId = MotionEventCompat.findPointerIndex(ev, this.mActivePointerId);
                    var13 = MotionEventCompat.getX(ev, pointerId);
                    var14 = MotionEventCompat.getY(ev, pointerId);
                    i = (int)(var13 - this.mLastMotionX[this.mActivePointerId]);
                    id = (int)(var14 - this.mLastMotionY[this.mActivePointerId]);
                    this.dragTo(this.mCapturedView.getLeft() + i, this.mCapturedView.getTop() + id, i, id);
                    this.saveLastMotion(ev);
                } else {
                    pointerId = MotionEventCompat.getPointerCount(ev);

                    for(newActivePointer = 0; newActivePointer < pointerId; ++newActivePointer) {
                        pointerCount = MotionEventCompat.getPointerId(ev, newActivePointer);
                        float var16 = MotionEventCompat.getX(ev, newActivePointer);
                        float var17 = MotionEventCompat.getY(ev, newActivePointer);
                        x = var16 - this.mInitialMotionX[pointerCount];
                        y = var17 - this.mInitialMotionY[pointerCount];
                        this.reportNewEdgeDrags(x, y, pointerCount);
                        if(this.mDragState == 1) {
                            break;
                        }

                        View toCapture = this.findTopChildUnder((int)var16, (int)var17);
                        if(this.checkTouchSlop(toCapture, x, y) && this.tryCaptureViewForDrag(toCapture, pointerCount)) {
                            break;
                        }
                    }

                    this.saveLastMotion(ev);
                }
                break;
            case 3:
                if(this.mDragState == 1) {
                    this.dispatchViewReleased(0.0F, 0.0F);
                }

                this.cancel();
            case 4:
            default:
                break;
            case 5:
                pointerId = MotionEventCompat.getPointerId(ev, actionIndex);
                var13 = MotionEventCompat.getX(ev, actionIndex);
                var14 = MotionEventCompat.getY(ev, actionIndex);
                this.saveInitialMotion(var13, var14, pointerId);
                if(this.mDragState == 0) {
                    var15 = this.findTopChildUnder((int)var13, (int)var14);
                    this.tryCaptureViewForDrag(var15, pointerId);
                    id = this.mInitialEdgesTouched[pointerId];
                    if((id & this.mTrackingEdges) != 0) {
                        this.mCallback.onEdgeTouched(id & this.mTrackingEdges, pointerId);
                    }
                } else if(this.isCapturedViewUnder((int)var13, (int)var14)) {
                    this.tryCaptureViewForDrag(this.mCapturedView, pointerId);
                }
                break;
            case 6:
                pointerId = MotionEventCompat.getPointerId(ev, actionIndex);
                if(this.mDragState == 1 && pointerId == this.mActivePointerId) {
                    newActivePointer = -1;
                    pointerCount = MotionEventCompat.getPointerCount(ev);

                    for(i = 0; i < pointerCount; ++i) {
                        id = MotionEventCompat.getPointerId(ev, i);
                        if(id != this.mActivePointerId) {
                            x = MotionEventCompat.getX(ev, i);
                            y = MotionEventCompat.getY(ev, i);
                            if(this.findTopChildUnder((int)x, (int)y) == this.mCapturedView && this.tryCaptureViewForDrag(this.mCapturedView, id)) {
                                newActivePointer = this.mActivePointerId;
                                break;
                            }
                        }
                    }

                    if(newActivePointer == -1) {
                        this.releaseViewForPointerUp();
                    }
                }

                this.clearMotionHistory(pointerId);
        }

    }

    private void reportNewEdgeDrags(float dx, float dy, int pointerId) {
        int dragsStarted = 0;
        if(this.checkNewEdgeDrag(dx, dy, pointerId, 1)) {
            dragsStarted |= 1;
        }

        if(this.checkNewEdgeDrag(dy, dx, pointerId, 4)) {
            dragsStarted |= 4;
        }

        if(this.checkNewEdgeDrag(dx, dy, pointerId, 2)) {
            dragsStarted |= 2;
        }

        if(this.checkNewEdgeDrag(dy, dx, pointerId, 8)) {
            dragsStarted |= 8;
        }

        if(dragsStarted != 0) {
            this.mEdgeDragsInProgress[pointerId] |= dragsStarted;
            this.mCallback.onEdgeDragStarted(dragsStarted, pointerId);
        }

    }

    private boolean checkNewEdgeDrag(float delta, float odelta, int pointerId, int edge) {
        float absDelta = Math.abs(delta);
        float absODelta = Math.abs(odelta);
        if((this.mInitialEdgesTouched[pointerId] & edge) == edge && (this.mTrackingEdges & edge) != 0 && (this.mEdgeDragsLocked[pointerId] & edge) != edge && (this.mEdgeDragsInProgress[pointerId] & edge) != edge && (absDelta > (float)this.mTouchSlop || absODelta > (float)this.mTouchSlop)) {
            if(absDelta < absODelta * 0.5F && this.mCallback.onEdgeLock(edge)) {
                this.mEdgeDragsLocked[pointerId] |= edge;
                return false;
            } else {
                return (this.mEdgeDragsInProgress[pointerId] & edge) == 0 && absDelta > (float)this.mTouchSlop;
            }
        } else {
            return false;
        }
    }

    private boolean checkTouchSlop(View child, float dx, float dy) {
        if(child == null) {
            return false;
        } else {
            boolean checkHorizontal = this.mCallback.getViewHorizontalDragRange(child) > 0;
            boolean checkVertical = this.mCallback.getViewVerticalDragRange(child) > 0;
            return checkHorizontal && checkVertical?dx * dx + dy * dy > (float)(this.mTouchSlop * this.mTouchSlop):(checkHorizontal?Math.abs(dx) > (float)this.mTouchSlop:(checkVertical?Math.abs(dy) > (float)this.mTouchSlop:false));
        }
    }

    public boolean checkTouchSlop(int directions) {
        int count = this.mInitialMotionX.length;

        for(int i = 0; i < count; ++i) {
            if(this.checkTouchSlop(directions, i)) {
                return true;
            }
        }

        return false;
    }

    public boolean checkTouchSlop(int directions, int pointerId) {
        if(!this.isPointerDown(pointerId)) {
            return false;
        } else {
            boolean checkHorizontal = (directions & 1) == 1;
            boolean checkVertical = (directions & 2) == 2;
            float dx = this.mLastMotionX[pointerId] - this.mInitialMotionX[pointerId];
            float dy = this.mLastMotionY[pointerId] - this.mInitialMotionY[pointerId];
            return checkHorizontal && checkVertical?dx * dx + dy * dy > (float)(this.mTouchSlop * this.mTouchSlop):(checkHorizontal?Math.abs(dx) > (float)this.mTouchSlop:(checkVertical?Math.abs(dy) > (float)this.mTouchSlop:false));
        }
    }

    public boolean isEdgeTouched(int edges) {
        int count = this.mInitialEdgesTouched.length;

        for(int i = 0; i < count; ++i) {
            if(this.isEdgeTouched(edges, i)) {
                return true;
            }
        }

        return false;
    }

    public boolean isEdgeTouched(int edges, int pointerId) {
        return this.isPointerDown(pointerId) && (this.mInitialEdgesTouched[pointerId] & edges) != 0;
    }

    private void releaseViewForPointerUp() {
        this.mVelocityTracker.computeCurrentVelocity(1000, this.mMaxVelocity);
        float xvel = this.clampMag(VelocityTrackerCompat.getXVelocity(this.mVelocityTracker, this.mActivePointerId), this.mMinVelocity, this.mMaxVelocity);
        float yvel = this.clampMag(VelocityTrackerCompat.getYVelocity(this.mVelocityTracker, this.mActivePointerId), this.mMinVelocity, this.mMaxVelocity);
        this.dispatchViewReleased(xvel, yvel);
    }

    private void dragTo(int left, int top, int dx, int dy) {
        int clampedX = left;
        int clampedY = top;
        int oldLeft = this.mCapturedView.getLeft();
        int oldTop = this.mCapturedView.getTop();
        if(dx != 0) {
            clampedX = this.mCallback.clampViewPositionHorizontal(this.mCapturedView, left, dx);
            this.mCapturedView.offsetLeftAndRight(clampedX - oldLeft);
        }

        if(dy != 0) {
            clampedY = this.mCallback.clampViewPositionVertical(this.mCapturedView, top, dy);
            this.mCapturedView.offsetTopAndBottom(clampedY - oldTop);
        }

        if(dx != 0 || dy != 0) {
            int clampedDx = clampedX - oldLeft;
            int clampedDy = clampedY - oldTop;
            this.mCallback.onViewPositionChanged(this.mCapturedView, clampedX, clampedY, clampedDx, clampedDy);
        }

    }

    public boolean isCapturedViewUnder(int x, int y) {
        return this.isViewUnder(this.mCapturedView, x, y);
    }

    public boolean isViewUnder(View view, int x, int y) {
        return view == null?false:x >= view.getLeft() && x < view.getRight() && y >= view.getTop() && y < view.getBottom();
    }

    public View findTopChildUnder(int x, int y) {
        int childCount = this.mParentView.getChildCount();

        for(int i = childCount - 1; i >= 0; --i) {
            View child = this.mParentView.getChildAt(this.mCallback.getOrderedChildIndex(i));
            if(x >= child.getLeft() && x < child.getRight() && y >= child.getTop() && y < child.getBottom()) {
                return child;
            }
        }

        return null;
    }

    private int getEdgesTouched(int x, int y) {
        int result = 0;
        if(x < this.mParentView.getLeft() + this.mEdgeSize) {
            result |= 1;
        }

        if(y < this.mParentView.getTop() + this.mEdgeSize) {
            result |= 4;
        }

        if(x > this.mParentView.getRight() - this.mEdgeSize) {
            result |= 2;
        }

        if(y > this.mParentView.getBottom() - this.mEdgeSize) {
            result |= 8;
        }

        return result;
    }

    public abstract static class Callback {
        public Callback() {
        }

        public void onViewDragStateChanged(int state) {
        }

        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
        }

        public void onViewCaptured(View capturedChild, int activePointerId) {
        }

        public void onViewReleased(View releasedChild, float xvel, float yvel) {
        }

        public void onEdgeTouched(int edgeFlags, int pointerId) {
        }

        public boolean onEdgeLock(int edgeFlags) {
            return false;
        }

        public void onEdgeDragStarted(int edgeFlags, int pointerId) {
        }

        public int getOrderedChildIndex(int index) {
            return index;
        }

        public int getViewHorizontalDragRange(View child) {
            return 0;
        }

        public int getViewVerticalDragRange(View child) {
            return 0;
        }

        public abstract boolean tryCaptureView(View var1, int var2);

        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return 0;
        }

        public int clampViewPositionVertical(View child, int top, int dy) {
            return 0;
        }
    }
}
