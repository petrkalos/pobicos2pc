/*
 * Author: Kwstas Lekkas , kwstasl@gmail.com
 */
package org.kalos.clientmgr;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kalos.Log;
import org.lekkas.poclient.PoConnectionManager;

public final class NodeInfo {

    private SocketChannel Sock;
    private long last_seen;
    private char PoNodeAddr;
    private String server_ip;
    private int server_port;

    public NodeInfo(SocketChannel s, char addr) throws ClosedChannelException {
        try {
            System.out.println("Mobile node connected with addr: " + (int) addr+ " ip: "+s.getRemoteAddress());
        } catch (IOException ex) {
            Logger.getLogger(NodeInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
        Sock = s;
        PoNodeAddr = addr;
        last_seen = System.currentTimeMillis();
        
    }

    public NodeInfo(char PoAddr, String server_ip, int server_port) {
        PoNodeAddr = PoAddr;
        this.server_ip = server_ip;
        this.server_port = server_port;
        this.connectSocket();
    }

    public void connectSocket() {
        try {
            if (Sock == null) {
                Sock = SocketChannel.open();
                Sock.configureBlocking(false);
                Sock.register(PoConnectionManager.getInstance().getSelector(), SelectionKey.OP_READ);
                Sock.connect(new InetSocketAddress(server_ip, server_port));

                while (!Sock.finishConnect()) {}
                
                if (Sock.isConnected()) {
                    Log.w("NODE", "PETROS: socket is connected"+ (int)PoNodeAddr);
                } else {
                    Log.w("NODE", "PETROS: socket is not connected"+(int) PoNodeAddr);
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setSock(SocketChannel s) {
        Sock = s;
    }

    public SocketChannel getSocketChannel() {
        return Sock;
    }

    public void setPoNodeAddr(char addr) {
        PoNodeAddr = addr;
    }

    public char getPoNodeAddr() {
        return PoNodeAddr;
    }

    public long getLastSeen() {
        return last_seen;
    }

    public void setLastSeen(long d) {
        last_seen = d;
    }
}
