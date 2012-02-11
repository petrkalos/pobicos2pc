/*
 * Author: Kwstas Lekkas , kwstasl@gmail.com
 */
package org.lekkas.poclient;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.lekkas.poclient.PoAPI.PoAPI;

import org.kalos.Log;

public class PoMiddlewareService {

    private static final String TAG = "PoMiddlewareService";
    private static final int CONNECTION_RETRY_DELAY = 5000;
    private static PoMiddlewareService INSTANCE;
    private static boolean Running;
    private static boolean onAppExit;

    public static boolean isRunning() {
        return Running;
    }

    public static PoMiddlewareService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PoMiddlewareService();
        }
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
        }
        PoRegistryService.getInstance().register();
        return true;
    }

    public void scheduleConnectRetry() {
        Log.d(TAG, "Schedule connection retry");
        while (!PoConnectionManager.getInstance().connect()) {
            Log.d(TAG, "Connection retry...");
            try {
               Thread.sleep(CONNECTION_RETRY_DELAY);
            } catch (InterruptedException ex) {
                Logger.getLogger(PoMiddlewareService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
