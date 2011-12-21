package org.lekkas.poclient;

import java.nio.ByteBuffer;

public class PoRegistryService {

    private static final String TAG = "PoRegistryService";
    private static PoRegistryService INSTANCE;
    private STATE status;
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private int seed;
    private char myAddr;
    private double lat=-1;
    private double lon=-1;

    public void setState(STATE s) {
        status = s;
    }

    public STATE getState() {
        return status;
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
        status = STATE.REGISTERED;
        System.out.println(TAG + "myAddr is set to: " + (int) myAddr + " and seed to " + (int) s);
    }

    public boolean register() {

        Network_Msg msg;
        ByteBuffer payload;

        byte payload_len = (byte) (1 + 8 + 8 + 2 + 4 + 4);	// Class of device + Lat + long + addr + seed

        payload = ByteBuffer.allocate(Serialization.uint8ToInt(payload_len));
        payload.put(Network_Msg.CLASS_SERVER).putDouble(lat).putDouble(lon);
        payload.putChar(myAddr).putInt(seed).putInt(PoMainA.getPort());
        payload.position(0);


        msg = new Network_Msg(Network_Msg.REGISTRY_REQ, payload_len, payload.array());
        payload.clear();
        return PoConnectionManager.getInstance().getOutMsgQ().offer(msg);
    }

    double getLat() {
        return lat;
    }

    double getLon() {
        return lon;
    }

    enum STATE {

        UNREGISTERED,
        REGISTERED,
        REJECTED
    };

    public int onStartCommand() {
        /*System.out.println(TAG+ "onStartCommand()");
        return START_NOT_STICKY;*/
        return 0;
    }

}