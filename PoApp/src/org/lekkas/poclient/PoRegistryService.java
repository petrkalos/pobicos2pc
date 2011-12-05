package org.lekkas.poclient;

import java.nio.ByteBuffer;
import org.kalos.Log;

public class PoRegistryService{
	private static final String TAG = "PoRegistryService";
	private static PoRegistryService INSTANCE;
	private STATE state;
	
	private int seed;
	private char myAddr;
	

	public STATE getState() {
		return state;
	}
	public void setState(STATE s) {
		state = s;
	}

	public static PoRegistryService getInstance() {
                if(INSTANCE==null) INSTANCE = new PoRegistryService();
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
		Log.w(TAG,"myAddr is set to: "+(int)myAddr+" and seed to "+(int)s);
	}

        
	public boolean register() {
		
		Network_Msg msg;
		ByteBuffer payload;
		
		byte payload_len = (byte)(1 + 8 + 8 + 2 + 4);	// Class of device + Lat + long + addr + seed

		payload = ByteBuffer.allocate(Serialization.uint8ToInt(payload_len));
		payload.put(Network_Msg.CLASS_SERVER).putInt(1234);
		payload.putChar(myAddr).putInt(seed);
		payload.position(0);
		

		msg = new Network_Msg(Network_Msg.SERVER_REGISTRY_REQ, payload_len, payload.array());
		payload.clear();
		return PoConnectionManager.getInstance().getOutMsgQ().offer(msg);
	}
        
        public boolean getServer(){
                Network_Msg msg;
		ByteBuffer payload;
		
		byte payload_len = (byte)(0);

		payload = ByteBuffer.allocate(Serialization.uint8ToInt(payload_len));
		payload.position(0);
		
		msg = new Network_Msg(Network_Msg.SERVER_SEARCH_REQ, payload_len, payload.array());
		payload.clear();
		return PoConnectionManager.getInstance().getOutMsgQ().offer(msg);
        }
	
	enum STATE {
		UNREGISTERED,
		REGISTERED,
		REJECTED
	};
}
