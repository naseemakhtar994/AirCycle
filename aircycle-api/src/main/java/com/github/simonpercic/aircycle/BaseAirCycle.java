package com.github.simonpercic.aircycle;

import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

/**
 * BaseAirCycle base listener.
 *
 * @author Simon Percic <a href="https://github.com/simonpercic">https://github.com/simonpercic</a>
 */
public abstract class BaseAirCycle<T extends Activity> implements ActivityLifecycleCallbacks {

    private static final String UNUSED_PARAMETERS = "UnusedParameters";

    private final T tActivity;
    private final Handler handler;
    private final AirCycleConfig config;

    // region Lifecycle runnables

    private final Runnable onActivityStartedRunnable = new Runnable() {
        @Override public void run() {
            notifyOnActivityStarted(tActivity);
        }
    };

    private final Runnable onActivityResumedRunnable = new Runnable() {
        @Override public void run() {
            notifyOnActivityResumed(tActivity);
        }
    };

    private final Runnable onActivityPausedRunnable = new Runnable() {
        @Override public void run() {
            notifyOnActivityPaused(tActivity);
        }
    };

    private final Runnable onActivityStoppedRunnable = new Runnable() {
        @Override public void run() {
            notifyOnActivityStopped(tActivity);
        }
    };

    private final Runnable onActivityDestroyedRunnable = new Runnable() {
        @Override public void run() {
            handler.removeCallbacksAndMessages(null);
            notifyOnActivityDestroyed(tActivity);
        }
    };

    // endregion Lifecycle runnables

    protected BaseAirCycle(T tActivity, AirCycleConfig config) {
        this.tActivity = tActivity;
        this.config = config;
        this.handler = new Handler(Looper.getMainLooper());
    }

    // region ActivityLifecycleCallbacks

    @Override public void onActivityCreated(Activity activity, final Bundle savedInstanceState) {
        if (activity != tActivity) {
            return;
        }

        final AirCycleConfig config = getConfig();
        if (config != null && config.ignoreLifecycleCallback(ActivityLifecycle.CREATE)) {
            return;
        }

        handler.post(new Runnable() {
            @Override public void run() {
                Bundle bundle = savedInstanceState;

                if (config != null && config.passIntentBundleOnCreate() && bundle == null) {
                    Intent intent = tActivity.getIntent();
                    if (intent != null) {
                        bundle = intent.getExtras();
                    }
                }

                notifyOnActivityCreated(tActivity, bundle);
            }
        });
    }

    @Override public void onActivityStarted(Activity activity) {
        if (activity != tActivity) {
            return;
        }

        if (ignoreLifecycleCallback(ActivityLifecycle.START)) {
            return;
        }

        handler.post(onActivityStartedRunnable);
    }

    @Override public void onActivityResumed(Activity activity) {
        if (activity != tActivity) {
            return;
        }

        if (ignoreLifecycleCallback(ActivityLifecycle.RESUME)) {
            return;
        }

        handler.post(onActivityResumedRunnable);
    }

    @Override public void onActivityPaused(Activity activity) {
        if (activity != tActivity) {
            return;
        }

        if (ignoreLifecycleCallback(ActivityLifecycle.PAUSE)) {
            return;
        }

        handler.post(onActivityPausedRunnable);
    }

    @Override public void onActivityStopped(Activity activity) {
        if (activity != tActivity) {
            return;
        }

        if (ignoreLifecycleCallback(ActivityLifecycle.STOP)) {
            return;
        }

        handler.post(onActivityStoppedRunnable);
    }

    @Override public void onActivitySaveInstanceState(Activity activity, final Bundle outState) {
        if (activity != tActivity) {
            return;
        }

        if (ignoreLifecycleCallback(ActivityLifecycle.SAVE_INSTANCE_STATE)) {
            return;
        }

        handler.post(new Runnable() {
            @Override public void run() {
                notifyOnActivitySaveInstanceState(tActivity, outState);
            }
        });
    }

    @Override public void onActivityDestroyed(Activity activity) {
        if (activity != tActivity) {
            return;
        }

        tActivity.getApplication().unregisterActivityLifecycleCallbacks(this);

        if (ignoreLifecycleCallback(ActivityLifecycle.DESTROY)) {
            return;
        }

        handler.post(onActivityDestroyedRunnable);
    }

    // endregion ActivityLifecycleCallbacks

    @SuppressWarnings(UNUSED_PARAMETERS)
    protected void notifyOnActivityCreated(T activity, Bundle bundle) {

    }

    @SuppressWarnings(UNUSED_PARAMETERS)
    protected void notifyOnActivityStarted(T activity) {

    }

    @SuppressWarnings(UNUSED_PARAMETERS)
    protected void notifyOnActivityResumed(T activity) {

    }

    @SuppressWarnings(UNUSED_PARAMETERS)
    protected void notifyOnActivityPaused(T activity) {

    }

    @SuppressWarnings(UNUSED_PARAMETERS)
    protected void notifyOnActivityStopped(T activity) {

    }

    @SuppressWarnings(UNUSED_PARAMETERS)
    protected void notifyOnActivitySaveInstanceState(T activity, Bundle bundle) {

    }

    @SuppressWarnings(UNUSED_PARAMETERS)
    protected void notifyOnActivityDestroyed(T activity) {

    }

    private AirCycleConfig getConfig() {
        if (config != null) {
            return config;
        } else {
            return AirCycleDefaultConfig.getConfig();
        }
    }

    private boolean ignoreLifecycleCallback(int lifecycle) {
        AirCycleConfig config = getConfig();
        return config != null && config.ignoreLifecycleCallback(lifecycle);
    }

    protected void registerCallbacks() {
        tActivity.getApplication().registerActivityLifecycleCallbacks(this);
    }
}
