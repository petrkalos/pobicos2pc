/*
 * Author: Kwstas Lekkas , kwstasl@gmail.com
 */
package org.lekkas.poclient;

public class PoConnStatusBR {

    private static STATE status;
    private static final String TAG = "CONNSTATUSBR";

    public void onReceive(String msg,String extramsg) {
        System.out.println(TAG + "Inside BR");

        /*
         * In case we have just been restarted from an AlarmManager intent.
         */
        if (msg.equals("alarm_message")) {
            System.out.println(TAG + "GOT ALARM MESSAGE: " + msg);
            firstConnectivityCheck();
            return;
        }
        /*
         * Got notification message from POBICOS
         */
        if (msg.equals("pobicos_notify")) {
            System.out.println(TAG + "GOT pobicos notify: ");
            PoApp.getMiddlewareService().createMessageNotification(extramsg);
            return;
        }
        /*
         * Got dialog message from POBICOS
         */
        if (msg.equals("pobicos_dialog")) {
            System.out.println(TAG + "GOT pobicos dialog: ");
            PoApp.getMiddlewareService().createDialogNotification(extramsg);
            return;
        }
        /*
         * Got POBICOS Alert
         */
        if (msg.equals("pobicos_alert")) {
            System.out.println(TAG + "GOT pobicos alert");
            return;
        }
        /*
         * disconnected from socket: reconnect, sent from reader thread
         *
        if(intent.hasExtra("reconnect")) {
        System.out.println(TAG+ "GOT RECONNECT");
        //PoConnectionManager.getInstance().disconnect();
        PoApp.getMiddlewareService().scheduleConnectRetry();
        return;
        }*/
        /*
         * Connection retry; alarm intent timer set by our service.
         */
        if (msg.equals("connection_retry")) {
            System.out.println(TAG + "GOT connection retry");
            if (PoApp.getMiddlewareService() == null) {
                return;
            }
            PoApp.getMiddlewareService().connect();
            return;
        }
        /*
         * We've got a registry intent
         */
        if (msg.equals("registry_event")) {
            System.out.println(TAG + "GOT registry event");
            /*
             * If we get a rejected state it means that our seed has expired.
             */
            if (PoRegistryService.getInstance().getStatusState() == PoRegistryService.STATE.REJECTED) {
                stopMiddlewareService();
            }
            if (PoRegistryService.getInstance().getStatusState() == PoRegistryService.STATE.REGISTERED) {
                PoConnectionManager.getInstance().startUpdateThread();
            }
            System.out.println(TAG + "BROADCAST RECEIVER ONCREATE() RETURNING...(REGISTRY_EVENT)");
            return;
        }

        if (msg != null && msg.equals("CONNECTIVITY_ACTION")) {
            System.out.println(TAG + "RECEIVED CONNECTIVITY_ACTION");

            /*
             * The following 3 'if' are very ugly; The reason for these checks
             * is that the CONNECTIVITY_ACTION intents differ when having
             * different connectivity status. 
             */
            if (msg.equals("EXTRA_NO_CONNECTIVITY")) {
                System.out.println(TAG + "NO connectivity: ");
                status = STATE.DISCONECTED;
                //stopMiddlewareService();
                PoConnectionManager.getInstance().disconnect();
                if (PoApp.getMainActivity() != null) {
                    PoApp.getMainActivity().setConnectivityStatus(false);
                }
                return;
            }

            if (msg.equals("EXTRA_IS_FAILOVER")) {
                if (msg.equals("EXTRA_IS_FAILOVER")) {
                    System.out.println(TAG + "FAILOVER ");

                    status = STATE.WIFI;

                    //stopMiddlewareService();
                    PoConnectionManager.getInstance().disconnect();
                    if (PoApp.getMiddlewareService() == null) {
                        System.out.println(TAG + "FAILOVER: Starting middlewareservice.");
                        startMiddlewareService();
                    } else {
                        System.out.println(TAG + "FAILOVER: Connecting...");
                        PoMiddlewareService.getInstance().connect();
                        if (PoApp.getMainActivity() != null) {
                            PoApp.getMainActivity().setConnectivityStatus(true);
                        }
                    }
                    System.out.println(TAG + "BROADCAST RECEIVER ONCREATE() RETURNING(failover)...");
                    return;
                }
            }

            if (msg.equals("EXTRA_NETWORK_INFO")) {
                if (status == STATE.DISCONECTED) {
                    status = STATE.WIFI;
                }

                if (PoApp.getMiddlewareService() == null) {
                    System.out.println(TAG + "Starting middleware service");
                    startMiddlewareService();
                } else {
                    System.out.println(TAG + "trying to connect");
                    PoMiddlewareService.getInstance().connect();
                    if (PoApp.getMainActivity() != null) {
                        PoApp.getMainActivity().setConnectivityStatus(true);
                    }
                }
                return;
            }

        }
        System.out.println(TAG + "BROADCAST RECEIVER ONCREATE() RETURNING...");
    }

    private void stopMiddlewareService() {
        System.out.println(TAG + "Stopping service.");
        if (PoMiddlewareService.isRunning()) {
            
            PoMiddlewareService.getInstance().stopPoMiddleware();
        }

    }

    private void startMiddlewareService() {
        System.out.println(TAG + "Starting service");
        if (!PoMiddlewareService.isRunning()) {
            PoMiddlewareService.getInstance().startPoMiddleware();
        }
    }

    private void firstConnectivityCheck() {
        /*NetworkInfo wifi, mobile;
        Intent service_intent;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
        wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if ((wifi.isConnected() || mobile.isConnected()) && !PoMiddlewareService.isRunning()) {
            service_intent = new Intent(context, PoMiddlewareService.class);
            context.startService(service_intent);
        }*/
    }

    public enum STATE {
        MOBILE,
        WIFI,
        DISCONECTED
    };
}
