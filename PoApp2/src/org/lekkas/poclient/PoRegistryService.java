package org.lekkas.poclient;

import java.nio.ByteBuffer;
import org.kalos.Log;

public class PoRegistryService {

    private static final String TAG = "PoRegistryService";
    private static PoRegistryService INSTANCE;
    private STATE state;
    private int seed = 0;
    private char myAddr = '0';

    public STATE getState() {
        return state;
    }

    public void setState(STATE s) {
        state = s;
    }

    public static PoRegistryService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PoRegistryService();
        }
        return INSTANCE;
    }

    public char getMyAddr() {
        return myAddr;
    }

    public int getSeed() {
        return seed;
    }

    public void setMyAddr(char b, int s) {
        myAddr = b;
        seed = s;
        state = STATE.REGISTERED;
        Log.w(TAG, "myAddr is set to: " + (int) myAddr + " and seed to " + (int) s);
    }

    public boolean register() {
        boolean ret;
        Log.w(TAG, "Updating registry.");
        Network_Msg msg;
        ByteBuffer payload;

        byte payload_len = (byte) (1 + 4 + 2 + 4);	// Class of device + addr + seed


        payload = ByteBuffer.allocate(Serialization.uint8ToInt(payload_len));
        payload.put(Network_Msg.CLASS_SERVER).putInt(5444);
        payload.putChar(myAddr).putInt(seed);
        payload.position(0);

        msg = new Network_Msg(Network_Msg.SERVER_REGISTRY_REQ, payload_len, payload.array());
        payload.clear();
        ret = PoConnectionManager.getInstance().getOutMsgQ().offer(msg);
        return ret;
    }

    public boolean getServer() {
        Log.d(TAG, "findServer");
        
        byte payload_len = (byte) (0);
        ByteBuffer payload = ByteBuffer.allocate(Serialization.uint8ToInt(payload_len));
        
        Network_Msg msg = new Network_Msg(Network_Msg.SERVER_FIND_REQ,payload_len, payload.array());
        return PoConnectionManager.getInstance().getOutMsgQ().offer(msg);
    }

    enum STATE {

        UNREGISTERED,
        REGISTERED,
        REJECTED
    };

    public void onCreate() {
        Log.d(TAG, "onCreate()");
    }
}
