package org.kalos.server;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lekkas.poclient.Network_Msg;
import org.lekkas.poclient.PoConnectionManager;
import org.lekkas.poclient.Serialization;

import android.util.Log;

public class ServerManager {
	private static final String TAG = "ServerFinder";
	private static final ServerManager INSTANCE = new ServerManager();
	private List<ServerNode> list = Collections
			.synchronizedList(new ArrayList<ServerNode>());

	public static ServerManager getInstance() {
		return INSTANCE;
	}

	public SocketChannel getServerSocket(char addr) {
		synchronized (list) {
			while (true) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				for (ServerNode node : list) {
					if ((int)node.getPoAddr() == (int)addr) {
						Log.w(TAG,"PETROS socket found with addr "+(int)node.getPoAddr());
						return node.getSocket();
					}
				}

			}
		}
	}

	public boolean searchServer(char addr) {
		Log.w(TAG, "Search Server");
		Network_Msg msg;
		ByteBuffer payload;

		byte payload_len = (byte) (1 + 2); // Class of device + addr

		payload = ByteBuffer.allocate(Serialization.uint8ToInt(payload_len));
		payload.put(Network_Msg.CLASS_MOBILE).putChar(addr);
		payload.position(0);

		msg = new Network_Msg(Network_Msg.SERVER_SEARCH_REQ, payload_len,
				payload.array());
		payload.clear();
		return PoConnectionManager.getInstance().getOutMsgQ().offer(msg);
	}

	public void add(ServerNode node) {
		synchronized (list) {
			for(ServerNode snode: list){
				if(snode.getPoAddr() == node.getPoAddr()) return;
			}
			Log.w(TAG, "PETROS: Server added to the list");
			list.add(node);
		}
	}
	
	public boolean existServer(char addr){
		synchronized (list) {
			for(ServerNode node: list) if(addr == node.getPoAddr()) return true;
		}
		return false;
	}

}
