/*
 * Author: Kwstas Lekkas , kwstasl@gmail.com
 */
package org.lekkas.poclient;

import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kalos.Log;

import org.lekkas.poclient.AppLoader.PoAppPillLoader;

public class PoMainA{

    private static final String TAG = "PoMainA";
    private static String[] args;
    private static String host = "192.168.178.29";

    public static void main(String args[]) {
        PoMainA.args = args;
        
        firstConnectivityCheck();
        
        PoApp.setAppPill(args.length==1);
        try {
            Thread.sleep(2000);
            PoRegistryService.getInstance().getServer();
        } catch (InterruptedException ex) {
            Logger.getLogger(PoMainA.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String getHost() {
        return host;
    }
    
    public static void startApp(){
        if(PoApp.isAppPill()){
            PoAppPillLoader.getInstance().loadAndStartApp(args[0]+"pc3App.hex");
        }
    }

    private static void firstConnectivityCheck() {
        if (connectivityCheck() && !PoMiddlewareService.isRunning()) {
            PoMiddlewareService.getInstance().onCreate();
        }
    }

    /*
     * Returns true if we have an available network, 
     * false otherwise.
     */
    private static boolean connectivityCheck() {
        try {
            int timeout = 2000;
            InetAddress[] addresses = InetAddress.getAllByName(PoMainA.getHost());
            for (InetAddress address : addresses) {
                if (address.isReachable(timeout)) {
                    Log.d(TAG, address+ " is reachable");
                    return true;
                } else {
                    Log.e(TAG,address+"could not be contacted%n");
                    return false;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Unknow host "+getHost());
        }
        return false;
    }
    
    @Override
    public void finalize(){
        try {
            super.finalize();
            PoMiddlewareService.setOnAppExit();
            PoConnectionManager.disconnectCalled();
        } catch (Throwable ex) {
            Logger.getLogger(PoMainA.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}