package org.lekkas.poclient.PoAPI;

import org.kalos.Log;

public class NGResources implements NGResourcesI {

    private native int nativeDialogInputReceived(byte result);

    private native int nativeDialogTimeout();
    private static final String TAG = "NGResources";
    private static final NGResources INSTANCE = new NGResources();

    private NGResources() {
        Log.d(TAG,"Started!");
    }

    public static NGResources getInstance() {
        return INSTANCE;
    }

    @Override
    public void JNICallback_AlertText() {

    }

    @Override
    public void JNICallback_NotifyText(String msg, int unsigned_millis) {
        
    }

    @Override
    public void JNICallback_CreateDialog(String msg, int unsigned_seconds) {
        
    }

    @Override
    public void JNICallback_DismissDialog() {
        
    }

    @Override
    public void JNICall_DialogInputReceived(byte result) {
        
    }

    @Override
    public void JNICall_DialogTimeout() {
        
    }
}
