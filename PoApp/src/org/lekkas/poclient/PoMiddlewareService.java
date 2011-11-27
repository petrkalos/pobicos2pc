/*
 * Author: Kwstas Lekkas , kwstasl@gmail.com
 */
package org.lekkas.poclient;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.net.InetAddress;

import org.lekkas.poclient.PoAPI.PoAPI;

public class PoMiddlewareService {

    private static final String TAG = "PoMiddlewareService";
    private static final int DIALOG_NOTIFICATION = 1;
    private static final int MESSAGE_NOTIFICATION = 2;
    private static final int CONNECTION_RETRY_DELAY = 5000;
    private static PoMiddlewareService INSTANCE;
    private static boolean Running = false;
    private static boolean onAppExit;
    private static ScheduledThreadPoolExecutor reconnect_sched;
    private static ScheduledFuture<?> reconnect_task;
    private static PoConnStatusBR br;

    public static boolean isRunning() {
        System.out.println(" is running " + Running);
        return Running;
    }

    public static PoMiddlewareService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PoMiddlewareService();
        }
        return INSTANCE;
    }

    public void create() {
        System.out.println(TAG + " onCreate() ");
        br = new PoConnStatusBR();
        PoApp.setMiddlewareService(this);
        startPoMiddleware();
        INSTANCE = this;
        Running = true;
        onAppExit = false;
    }

    public void destroy() {
        System.out.println(TAG + "onDestroy()");

        stopPoMiddleware();
        Running = false;
        if (!onAppExit) {
            setAlarmAndExit();
        }
        System.exit(0);
    }

    public void stopPoMiddleware() {
        if (PoConnectionManager.isConnected()) {
            PoConnectionManager.getInstance().disconnect();
        }
        if (PoAPI.isRunning()) {
            System.out.println(TAG + " Stopping PoAPI ");
            PoAPI.getInstance().stop();
        }
        INSTANCE = null;
    }

    public void startPoMiddleware() {

        try {
            int timeout = 2000;
            InetAddress[] addresses = InetAddress.getAllByName(PoMainA.getHost());
            for (InetAddress address : addresses) {
                if (address.isReachable(timeout)) {
                    if (!connect()) {
                        if (PoApp.getMainActivity() != null) {
                            PoApp.getMainActivity().setConnectivityStatus(false);
                        }
                        return;
                    }

                    PoAPI.getInstance().start();
                    if (PoApp.getMainActivity() != null) {
                        PoApp.getMainActivity().setConnectivityStatus(true);
                    }
                } else {
                    if (PoApp.getMainActivity() != null) {
                        PoApp.getMainActivity().setConnectivityStatus(false);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Unknown host: " + e.getMessage());
        }
    }

    /*
     * Restarts our application.
     */
    private void setAlarmAndExit() {
        sendBroadcast("alarm_message", "This is an alarm.");
        System.exit(0);
    }

    public static void setOnAppExit() {
        onAppExit = true;
    }

    public static boolean onAppExit() {
        return onAppExit;
    }

    public void createDialogNotification(String msg) {
        System.out.println(TAG + "CalledDialogNotification " + msg);
    }

    public void createMessageNotification(String msg) {
        System.out.println(TAG + "CalledMessageNotification " + msg);
    }

    public boolean connect() {
        if (!PoConnectionManager.getInstance().connect()) {
            connectionRetryIntent();
            return false;
        }
        PoRegistryService.getInstance().register();
        return true;
    }

    private void connectionRetryIntent() {
        System.err.println(TAG + "connectionRetryIntent");
        this.sendBroadcast("connection_retry", "");

    }

    public void scheduleConnectRetry() {
        reconnect_sched = new ScheduledThreadPoolExecutor(1);
        reconnect_task = reconnect_sched.schedule(new Runnable() {

            public void run() {
                PoMiddlewareService.getInstance().connect();
            }
        }, 10000, TimeUnit.MILLISECONDS);
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

    public void sendBroadcast(String msg, String extramsg) {
        br.onReceive(msg, extramsg);
    }
}
