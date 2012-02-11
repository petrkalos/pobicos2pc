/*
 * Author: Kwstas Lekkas , kwstasl@gmail.com
 */
package org.lekkas.poclient;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kalos.Log;

import org.kalos.clientmgr.Reactor;
import org.lekkas.poclient.AppLoader.PoAppPillLoader;

public class PoMainA{

    private static final String TAG = "PoMainA";
    private static String[] args;
    private static String directory_ip = "192.168.178.29";
    private static int server_port;

    public static void main(String args[]) {
        
        PoMainA.args = args;
        Random rad = new Random();
        
        server_port = rad.nextInt(50000);
        
        
        PoApp.setAppPill(args.length==1);
        
        firstConnectivityCheck();
        
        try {
            Reactor r = new Reactor(server_port);
            r.run();
        } catch (IOException ex) {
            Logger.getLogger(PoMainA.class.getName()).log(Level.SEVERE, null, ex);
        }   
    }

    public static String getHost() {
        return directory_ip;
    }
    
    public static int getPort(){
        return server_port;
    }
    
    public static void startApp(){
        if(PoApp.isAppPill()){
            Log.d(TAG,"Start application");
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

    public static boolean isVerbose() {
        return true;
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