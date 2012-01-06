package org.kalos.servermgr;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lekkas.poclient.Network_Msg;
import org.lekkas.poclient.PoConnectionManager;
import org.lekkas.poclient.Serialization;

import org.kalos.Log;
import org.kalos.clientmgr.NodeInfo;

public class ServerManager {

    private static final String TAG = "ServerFinder";
    private static final ServerManager INSTANCE = new ServerManager();
    private final List<NodeInfo> list = Collections.synchronizedList(new ArrayList<NodeInfo>());

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
                for (NodeInfo node : list) {
                    if ((int) node.getPoNodeAddr() == (int) addr) {
                        Log.w(TAG, " socket found with addr " + (int) node.getPoNodeAddr());
                        return node.getSocketChannel();
                    }
                }

            }
        }
    }
    
    /* 
     * Search server request to directory service
     */
    
    public boolean searchServer(char addr) {
        Log.w(TAG, "Search Server");
        Network_Msg msg;
        ByteBuffer payload;

        byte payload_len = (byte) (1 + 2); // Class of device + addr

        payload = ByteBuffer.allocate(Serialization.uint8ToInt(payload_len));
        payload.put(Network_Msg.CLASS_SERVER).putChar(addr);
        payload.position(0);

        msg = new Network_Msg(Network_Msg.SERVER_SEARCH_REQ, payload_len,
                payload.array());
        payload.clear();
        return PoConnectionManager.getInstance().getOutMsgQ().offer(msg);
    }

    public void add(NodeInfo node) {
        synchronized (list) {
            for (NodeInfo snode : list) {
                if (snode.getPoNodeAddr() == node.getPoNodeAddr()) {
                    return;
                }
            }
            Log.w(TAG, ": Server added to the list");
            list.add(node);
        }
    }

    public boolean existServer(char addr) {
        synchronized (list) {
            for (NodeInfo node : list) {
                if (addr == node.getPoNodeAddr()) {
                    return true;
                }
            }
        }
        return false;
    }
}
