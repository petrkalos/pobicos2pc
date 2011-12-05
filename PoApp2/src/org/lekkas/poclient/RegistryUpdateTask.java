package org.lekkas.poclient;

import java.nio.ByteBuffer;
import org.kalos.Log;



public class RegistryUpdateTask extends Thread implements Runnable {
	private final String TAG = "RegistryUpdateTask";
	private static final int DELAY = 10000;
	private int seed;
	private char addr;
	private boolean Running;
	
	public RegistryUpdateTask(char a, int s) {
		Running = false;
		addr = a;
		seed = s;
	}
	public void run() {
		Log.w(TAG, "Update thread started");
		Running = true;
		while(!Thread.currentThread().isInterrupted() && Running) {
			try {
				Thread.sleep(DELAY);
				update();
			} catch (InterruptedException e) {
				Log.w(TAG, "Registry update thread caught exception: "+e.toString());
				Running = false;
			}
		}
		Log.w(TAG, "Update thread stopping...");
	}
	public boolean isRunning() {
		return Running;
	}
	public boolean update() {
		Log.w(TAG, "Updating registry.");
		Network_Msg msg;
		ByteBuffer payload;
			
		byte payload_len = (byte)(1 + 4 + 2 + 4);	// Class of device + addr + seed
	
		
		payload = ByteBuffer.allocate(Serialization.uint8ToInt(payload_len));
		payload.put(Network_Msg.CLASS_SERVER).putInt(5444);
		payload.putChar(addr).putInt(seed);
		payload.position(0);
		
		msg = new Network_Msg(Network_Msg.SERVER_REGISTRY_REQ, payload_len, payload.array());
		payload.clear();
		return PoConnectionManager.getInstance().getOutMsgQ().offer(msg);
	}
	
	public void cancel() {
		interrupt();
	}
}
