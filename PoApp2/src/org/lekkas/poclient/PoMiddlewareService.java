/*
 * Author: Kwstas Lekkas , kwstasl@gmail.com
 */
package org.lekkas.poclient;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.lekkas.poclient.PoAPI.PoAPI;

import org.kalos.Log;

public class PoMiddlewareService {

    private static final String TAG = "PoMiddlewareService";
    private static final int CONNECTION_RETRY_DELAY = 5;
    private static PoMiddlewareService INSTANCE;
    private static boolean Running;
    private static boolean onAppExit;
    private static ScheduledThreadPoolExecutor reconnect_sched;
    private static ScheduledFuture<?> reconnect_task;

    public static boolean isRunning() {
        return Running;
    }

    public static PoMiddlewareService getInstance() {
        if(INSTANCE==null) INSTANCE = new PoMiddlewareService();
        return INSTANCE;
    }

    public void onCreate() {
        Log.d(TAG, "onCreate()");
        PoApp.setMiddlewareService(this);
        INSTANCE = this;
        Running = true;
        onAppExit = false;
        startPoMiddleware();
    }

    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        stopPoMiddleware();
        Running = false;
        if (!onAppExit) {
            //TODO setAlarmAndExit();
        }
        System.exit(0);
    }

    public void stopPoMiddleware() {
        if (PoConnectionManager.isConnected()) {
            PoConnectionManager.getInstance().disconnect();
        }
        if (PoAPI.isRunning()) {
            Log.w(TAG, "Stopping PoAPI");
            PoAPI.getInstance().stop();
        }
        INSTANCE = null;
    }

    public void startPoMiddleware() {
        //TODO Connect
        if (!connect()) {
            return;
        }
        PoAPI.getInstance().start();
    }

    public static void setOnAppExit() {
        onAppExit = true;
    }

    public static boolean onAppExit() {
        return onAppExit;
    }

    public void msg(String str, boolean length) {
        Log.d(TAG, str);
    }

    public boolean connect() {
        if (!PoConnectionManager.getInstance().connect()) {
            scheduleConnectRetry();
            return false;
        }
        cancelConnectRetry();
        PoRegistryService.getInstance().register();
        return true;
    }

    public void scheduleConnectRetry() {
        Log.d(TAG, "Schedule connection retry");

        reconnect_sched = new ScheduledThreadPoolExecutor(1);
        reconnect_task = reconnect_sched.schedule(new Runnable() {

            @Override
            public void run() {
                PoConnectionManager.getInstance().connect();
            }
        }, CONNECTION_RETRY_DELAY, TimeUnit.SECONDS);
    }

    public void cancelConnectRetry() {
        if (reconnect_task != null) {
            reconnect_task.cancel(false);
            reconnect_task = null;
        }
        if (reconnect_sched != null) {
            reconnect_sched.shutdownNow();
            reconnect_sched = null;
        }
    }
}
