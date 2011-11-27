package org.lekkas.poclient.PoAPI;

import org.lekkas.poclient.PoApp;

public class NGResources implements NGResourcesI {

    private native int nativeDialogInputReceived(byte result);

    private native int nativeDialogTimeout();
    private static final String TAG = "NGResources";
    private static final NGResources INSTANCE = new NGResources();

    private NGResources() {
        System.out.println(TAG + "Started!");
    }

    public static NGResources getInstance() {
        return INSTANCE;
    }

    public void JNICallback_AlertText() {
        /*Intent myIntent = new Intent(PoApp.getMiddlewareService(), PoConnStatusBR.class);
        myIntent.putExtra("pobicos_alert", "");
        PoApp.getMiddlewareService().getApplicationContext().sendBroadcast(myIntent);*/
        System.err.append(TAG + "Alert Text");
        PoApp.getMiddlewareService().sendBroadcast("pobicos_alert", "");
        
    }

    public void JNICallback_NotifyText(String msg, int unsigned_millis) {
        /*Intent myIntent = new Intent(PoApp.getMiddlewareService(), PoConnStatusBR.class);
        myIntent.putExtra("pobicos_notify", msg);
        PoApp.getMiddlewareService().getApplicationContext().sendBroadcast(myIntent);*/
        System.err.append(TAG + "Notify Text");
        PoApp.getMiddlewareService().sendBroadcast("pobicos_notify", msg);
    }

    public void JNICallback_CreateDialog(String msg, int unsigned_seconds) {
        /*Intent myIntent = new Intent(PoApp.getMiddlewareService(), PoConnStatusBR.class);
        myIntent.putExtra("pobicos_dialog", msg);
        PoApp.getMiddlewareService().getApplicationContext().sendBroadcast(myIntent);*/
        System.err.append(TAG + "Create Dialog");
        PoApp.getMiddlewareService().sendBroadcast("pobicos_dialog", msg);
    }

    public void JNICallback_DismissDialog() {
        //
    }

    public void JNICall_DialogInputReceived(byte result) {
        System.out.println(TAG + "JNICALL DialogInputrECEIVED");
        nativeDialogInputReceived(result);
    }

    public void JNICall_DialogTimeout() {
        nativeDialogTimeout();
    }
}
