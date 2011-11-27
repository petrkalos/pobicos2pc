/*
 * Author: Kwstas Lekkas , kwstasl@gmail.com
 */
package org.lekkas.poclient;

import org.lekkas.poclient.AppLoader.PoAppPillLoader;

import java.net.InetAddress;

public class PoMainA {

    private static final String TAG = "PoMainA";
    private static final String host = "192.168.178.29";
    private static boolean app_status;
    private static String path;

    public static void main(String args[]) {
        PoMainA main = new PoMainA();
        if (args.length == 0) {
            PoApp.setAppPill(true);
        } else {
            path = args[0];
        }
        main.create();
        main.start();
    }

    public static String getHost() {
        return host;
    }
    /*
     * False for disconnected, true for connected.
     */

    public static void startApp() {
        if (!PoApp.isAppPill()) {
            System.err.println("Start application...");
            PoAppPillLoader.getInstance().loadApp(path + "pc3App.hex");
        }
    }

    public void setConnectivityStatus(boolean s) {
        if (s) {
            System.out.println("Connected.");
        } else {
            System.out.println("Disconnected.");
        }
    }

    private void setAppStatus(boolean s) {
        app_status = s;
        if (s) {
            System.out.println("App started.");
        } else {
            System.out.println("No App loaded.");
        }
    }

    /** Called when the activity is first created. */
    private void create() {
        PoApp.setMainActivity(this);
        System.out.println(TAG + " onCreate()");
        firstConnectivityCheck();
        System.out.println(TAG + " MAINACTIVITY oncreate RETURNING");
    }

    private void start() {
        System.out.println(TAG + " onStart() called.");
        if (!PoApp.isAppPill()) {
            connectivityCheck();
        } else {
            setAppStatus(app_status);
        }
        System.out.println(TAG + " MAINACTIVITY onstart RETURNING ");
    }

    public boolean onOptionsItemSelected() {
        /*switch (item.getItemId()) {
        case R.id.menu_exit:
        PoMiddlewareService.setOnAppExit();
        if (PoMiddlewareService.isRunning()) {
        service_intent = new Intent(this, PoMiddlewareService.class);
        stopService(service_intent);
        
        service_intent = new Intent(this, PoRegistryService.class);
        stopService(service_intent);
        }
        finish();
        }*/
        return true;
    }

    private void firstConnectivityCheck() {
        if (connectivityCheck() && !PoMiddlewareService.isRunning()) {
            System.out.println("Starting the middleware...");

            PoRegistryService.getInstance().create();
            PoMiddlewareService.getInstance().create();
        }
    }

    /*
     * Returns true if we have an available network, 
     * false otherwise.
     */
    private boolean connectivityCheck() {
        try {
            int timeout = 2000;
            InetAddress[] addresses = InetAddress.getAllByName(PoMainA.getHost());
            for (InetAddress address : addresses) {
                if (address.isReachable(timeout)) {
                    System.out.printf("%s is reachable%n", address);
                    return true;
                } else {
                    System.out.printf("%s could not be contacted%n", address);
                    return false;
                }
            }
        } catch (Exception e) {
            System.out.printf("Unknown host: %s%n", "www.java2s.com");
        }
        return false;
    }
}