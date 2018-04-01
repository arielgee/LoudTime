package com.arielg.loudtime;

import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;

abstract class TimeCounter {

    private final Context mContext;
    private long mMilliseconds;
    private final long mCounterInterval;
    private boolean mStatus;
    private boolean mStopped = false;
    private Runnable mCounter;

    private final Boolean foreword;

    private PowerManager.WakeLock wakeLock;

    public TimeCounter(Context context, long millisInTheFuture, long counterInterval) {
        mContext = context;
        foreword = false;
        this.mMilliseconds = millisInTheFuture;
        this.mCounterInterval = counterInterval;
        mStatus = false;
        Initialize();
    }

    public TimeCounter(Context context, long counterInterval) {
        mContext = context;
        foreword = true;
        this.mMilliseconds = 0;       // counting foreword
        this.mCounterInterval = counterInterval;
        mStatus = false;
        Initialize();
    }

    abstract void onCounterInterval(long millis);

    public boolean isRunning() {
        return mStatus;
    }

    public void Start() {
        mStatus = true;
        new Handler().post(mCounter);
        acquireWakeLock();
    }

    public void Pause() {
        mStatus = false;
    }

    public void Resume() {
        mStatus = true;
    }

    public void Stop() {
        mStatus = false;
        mStopped = true;
        wakeLock.release();
    }

    private void Initialize() {
        final Handler handler = new Handler();

        mCounter = new Runnable() {

            public void run() {

                if (mStopped)
                    return;

                if (mStatus) {
                    if (!foreword && mMilliseconds <= 0) {
                        Stop();
                        onCounterInterval(0);
                    } else {
                        handler.postDelayed(this, mCounterInterval);
                        onCounterInterval(mMilliseconds);
                        mMilliseconds -= foreword ? -mCounterInterval : mCounterInterval;
                    }
                } else {
                    handler.postDelayed(this, mCounterInterval);
                }
            }
        };
    }

    private void acquireWakeLock() {
        // take CPU lock to prevent CPU from going off if the user
        // presses the power button during download
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        wakeLock.acquire();
    }
}
