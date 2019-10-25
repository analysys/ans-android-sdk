package com.analysys.visual.viewcrawler;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import com.analysys.utils.InternalAgent;

/**
 * 增加摇一摇功能，传感器先感知摇一摇，如果触发回调，则返回； 如果未触发，则该次onSensorChanged后续执行翻转屏幕的操作 add by
 * 2018.6.13
 */
class FlipGesture implements SensorEventListener {

    private final static long INTERVAL_TIME = 1000 * 1;
    private static final float MINIMUM_GRAVITY_FOR_FLIP = 9.8f - 2.0f;
    private static final float MAXIMUM_GRAVITY_FOR_FLIP = 9.8f + 2.0f;
    // 1000000000 one second
    // 250000000 one quarter second
    private static final long MINIMUM_UP_DOWN_DURATION = 250000000;
    private static final long MINIMUM_CANCEL_DURATION = 1000000000;
    private static final int FLIP_STATE_UP = -1;
    private static final int FLIP_STATE_NONE = 0;
    private static final int FLIP_STATE_DOWN = 1;
    private static final int TRIGGER_STATE_NONE = 0;
    private static final int TRIGGER_STATE_BEGIN = 1;
    // Higher is noisier but more responsive, 1.0 to 0.0
    private static final float ACCELEROMETER_SMOOTHING = 0.7f;
    private static final float ACCELEROMETER_SHAKE_THRESHOLD = 17.0f;
    private static final String TAG = "FlipGesture";
    private static long TIGGER_TIME = 0L;
    private final float[] mSmoothed = new float[3];
    private final OnFlipGestureListener mListener;
    private float maxValue = 0L;
    private Context mContext = null;
    private int mTriggerState = -1;
    private int mFlipState = FLIP_STATE_NONE;
    private long mLastFlipTime = -1;

    public FlipGesture(Context context, OnFlipGestureListener listener) {
        mListener = listener;
        mContext = context.getApplicationContext();
        maxValue = InternalAgent.getFloat(mContext, "shake_max_value",
                ACCELEROMETER_SHAKE_THRESHOLD);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Samples may come in around 4 times per second

        int sensorType = event.sensor.getType();
        // values[0]:X轴，values[1]：Y轴，values[2]：Z轴
        float[] values = event.values;
        final float[] smoothed = smoothXYZ(event.values);
        // 如果传感器类型为加速度传感器，则判断是否为摇一摇
        if (sensorType == Sensor.TYPE_ACCELEROMETER) {
            float curMax = Math.max(Math.max(Math.abs(values[0]), Math.abs(values[1])),
                    Math.abs(values[2]));
            if (curMax > maxValue) {
                maxValue = curMax;
                if (mListener != null) {
                    InternalAgent.setFloat(mContext, "shake_max_value", maxValue);
                }
            }
            if (curMax > maxValue * 0.6 && mListener != null && curMax > ACCELEROMETER_SHAKE_THRESHOLD
                    && System.currentTimeMillis() - TIGGER_TIME > INTERVAL_TIME) {
                TIGGER_TIME = System.currentTimeMillis();
                mListener.onFlipGesture();
                return;
            }
        }

        final int oldFlipState = mFlipState;

        final float totalGravitySquared = smoothed[0] * smoothed[0] + smoothed[1] * smoothed[1]
                + smoothed[2] * smoothed[2];

        final float minimumGravitySquared = MINIMUM_GRAVITY_FOR_FLIP * MINIMUM_GRAVITY_FOR_FLIP;
        final float maximumGravitySquared = MAXIMUM_GRAVITY_FOR_FLIP * MAXIMUM_GRAVITY_FOR_FLIP;

        mFlipState = FLIP_STATE_NONE;

        if (smoothed[2] > MINIMUM_GRAVITY_FOR_FLIP && smoothed[2] < MAXIMUM_GRAVITY_FOR_FLIP) {
            mFlipState = FLIP_STATE_UP;
        }

        if (smoothed[2] < -MINIMUM_GRAVITY_FOR_FLIP && smoothed[2] > -MAXIMUM_GRAVITY_FOR_FLIP) {
            mFlipState = FLIP_STATE_DOWN;
        }

        // Might overwrite current state, which is what we want.
        if (totalGravitySquared < minimumGravitySquared || totalGravitySquared > maximumGravitySquared) {
            mFlipState = FLIP_STATE_NONE;
        }

        if (oldFlipState != mFlipState) {
            mLastFlipTime = event.timestamp;
        }

        // We need at least 1/4 seconds to recognize an UP or DOWN state
        // We need at least 1 seconds to recognize a NONE state

        final long flipDurationNanos = event.timestamp - mLastFlipTime;

        switch (mFlipState) {
            case FLIP_STATE_DOWN:
                if (flipDurationNanos > MINIMUM_UP_DOWN_DURATION && mTriggerState == TRIGGER_STATE_NONE) {
                    InternalAgent.v(TAG, "Flip gesture begun");
                    mTriggerState = TRIGGER_STATE_BEGIN;
                }
                break;
            case FLIP_STATE_UP:
                if (flipDurationNanos > MINIMUM_UP_DOWN_DURATION && mTriggerState == TRIGGER_STATE_BEGIN) {
                    InternalAgent.v(TAG, "Flip gesture completed");
                    mTriggerState = TRIGGER_STATE_NONE;
                    mListener.onFlipGesture();
                }
                break;
            case FLIP_STATE_NONE:
                if (flipDurationNanos > MINIMUM_CANCEL_DURATION && mTriggerState != TRIGGER_STATE_NONE) {
                    InternalAgent.v(TAG, "Flip gesture abandoned");
                    mTriggerState = TRIGGER_STATE_NONE;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        ; // Do nothing
    }

    private float[] smoothXYZ(final float[] samples) {
        // Note that smoothing doesn't depend on sample timestamp!
        for (int i = 0; i < 3; i++) {
            final float oldVal = mSmoothed[i];
            mSmoothed[i] = oldVal + (ACCELEROMETER_SMOOTHING * (samples[i] - oldVal));
        }

        return mSmoothed;
    }

    public interface OnFlipGestureListener {
        public void onFlipGesture();
    }
}
