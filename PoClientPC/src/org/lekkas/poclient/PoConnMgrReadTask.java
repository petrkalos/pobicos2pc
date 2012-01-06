/*
 * Author: Kwstas Lekkas , kwstasl@gmail.com
 */
package org.lekkas.poclient;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;

import java.util.Iterator;
import org.lekkas.poclient.PoAPI.PoAPI;
import org.lekkas.poclient.PoEvents.PoNetworkPacketRx;

import org.kalos.Log;
import org.kalos.clientmgr.NodeInfo;
import org.kalos.clientmgr.Reactor;
import org.kalos.servermgr.ServerManager;

public class PoConnMgrReadTask extends Thread implements Runnable {

    private final String TAG = "PoCommMgrReadTask";
    private SocketChannel sockch;
    private ByteBuffer network_msg_header, payload;
    private boolean Running;
    public static boolean logBytes = false;
    public static int receivedBytes, cntRxPkts;
    private Selector selector;

    public static int uByteToInt(byte b) {
        return (int) (b & 0xFF);
    }

    public PoConnMgrReadTask(Selector sel, SocketChannel sock) {
        Running = false;
        sockch = sock;
        selector = sel;
    }

    public void run() {
        int ret, bytes_read;

        Log.w(TAG, "Thread started!");
        network_msg_header = ByteBuffer.allocate(Network_Msg.HEADER_LEN);	// Allocate space 
        // for Msg_type(1), Seq(1) + Len(1) fields
        payload = null;
        Running = true;
        while (!Thread.currentThread().isInterrupted()) {
            try {
                /*
                 * MSG Reply format:
                 * 
                 * Msg Type:	byte:	1 byte
                 * Len:			byte:	1 byte
                 * Payload:		byte[]:	{Len} bytes
                 * 
                 * Where Payload:
                 * 
                 * 1) For POBICOS messages:
                 * Src addr		: char	: 2 bytes
                 * Dest addr	: char	: 2 bytes
                 * Msg Len		: byte	: 1 byte
                 * Msg			: byte[]: {Msg Len} bytes;
                 * 
                 * 2) For registration messages:
                 *  a)2 bytes, (Registered node address) + 4 bytes(seed) for welcome messages
                 *  b)no payload for pong messages
                 * 
                 * 3) For find server response message:
                 * a)len - 4 -2 bytes ( Server IP ) + 4 bytes for port + 2 bytes for po addr
                 * 
                 */
                Log.w(TAG, "TEST: Read()'ing");
                /*
                 * Reading network message header.
                 */
                selector.select();
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    // Get the selection key

                    SelectionKey selKey = (SelectionKey) it.next();

                    // Remove it from the list to indicate that it is being
                    // processed
                    it.remove();
                    sockch = (SocketChannel) selKey.channel();

                    Log.w(TAG, "TEST:  read from: " + sockch.socket().getRemoteSocketAddress());

                    ret = sockch.read(network_msg_header);
                    if (ret == -1) {
                        throw new IOException("sockch.read() returned -1. Server has probably "
                                + "closed the connection");
                    }
                    if (ret == 0) {
                        continue;
                    }

                    bytes_read = ret;
                    while (bytes_read < Network_Msg.HEADER_LEN) {
                        ret = sockch.read(network_msg_header);
                        if (ret == -1) {
                            throw new IOException("sockch.read() returned -1");
                        }
                        bytes_read += ret;
                    }

                    /*
                     * Reading message payload
                     */
                    int len = uByteToInt(network_msg_header.get(1));
                    Log.w(TAG, "read() Header: " + ret + " bytes. Length of payload = " + len);
                    if (len != 0) {
                        payload = ByteBuffer.allocate(len);

                        ret = sockch.read(payload);
                        if (ret == -1) {
                            throw new IOException("sockch.read() returned -1");
                        }
                        bytes_read = ret;
                        while (bytes_read < len) {
                            ret = sockch.read(payload);
                            if (ret == -1) {
                                throw new IOException("sockch.read() returned -1");
                            }
                            bytes_read += ret;
                        }
                    }

                    byte flag = network_msg_header.get(0);

                    if (flag == Network_Msg.SERVER_SEARCH_RES) {
                        Log.w(TAG, ": Server Search Response");
                        String ip = new String(payload.array(), 0, len - 4 - 2);
                        int port = payload.getInt(len - 4 - 2);
                        char poaddr = payload.getChar(len - 2);
                        Log.w(TAG, ": server ip for " + (int) poaddr
                                + " is " + ip + ":" + port);
                        NodeInfo node = new NodeInfo(poaddr, ip, port);
                        ServerManager.getInstance().add(node);
                    }

                    if (flag == Network_Msg.REG_REPLY_WELCOME) {
                        Log.w(TAG, "GOT REG_REPLY_WELCOME! addr:" + byteArrayToAscii(payload.array()));
                        byte[] addr = {payload.get(0), payload.get(1)};
                        byte[] seed = {payload.get(2), payload.get(3), payload.get(4), payload.get(5)};
                        Log.w(TAG, "addr: " + byteArrayToAscii(addr));
                        Log.w(TAG, "seed: " + byteArrayToAscii(seed));

                        char a = Serialization.byteArrayToChar(addr);
                        int s = Serialization.byteArrayToInt(seed);
                        PoRegistryService.getInstance().setMyAddr(a, s);
                        PoRegistryService.getInstance().setState(PoRegistryService.STATE.REGISTERED);
                        sendRegistryIntent();	// first time connection; start registry update thread.
                    }
                    if (flag == Network_Msg.REG_REPLY_FAIL) {
                        Log.w(TAG, "Registration failed!");
                        PoRegistryService.getInstance().setState(PoRegistryService.STATE.REJECTED);
                        sendRegistryIntent();	// restart service
                        cancel();
                    }
                    if (flag == Network_Msg.REG_REPLY_PONG) {
                        Log.w(TAG, "GOT REG_REPLY_PONG!");
                        PoRegistryService.getInstance().setState(PoRegistryService.STATE.REGISTERED);
                        if (!PoConnectionManager.getInstance().isRegistryUpdateThreadRunning()) {
                            sendRegistryIntent();	// needed to restart update thread in case we have
                        }												// just reconnected.
                    }
                    if (flag == Network_Msg.POBICOS_MSG) {
                        char srcaddr = payload.getChar(0);
                        char dstaddr = payload.getChar(2);
                        Log.w(TAG, "GOT POBICOS MESSAGE! from " + (int) srcaddr);
                        if (logBytes) {
                            receivedBytes += (payload.capacity() + 2);
                            cntRxPkts++;
                        }
                        if (isServer(srcaddr)) {
                            if (ServerManager.getInstance().existServer(srcaddr)) {
                                Log.w(TAG,
                                        " server connection already open "
                                        + (int) srcaddr);
                            } else {
                                Log.w(TAG, " open connection with "
                                        + (int) srcaddr);
                                ServerManager.getInstance().searchServer(
                                        srcaddr);
                            }
                        } else if(dstaddr != (char) 0xffff){
                            NodeInfo n = Reactor.registry.isRegisted(srcaddr,false);
                            if (n == null) {
                                Log.w(TAG, ": Add new node");
                                char addr = payload.getChar(0);
                                n = new NodeInfo(sockch, addr);
                                Reactor.registry.reg.add(n);
                            } else {
                                n.setLastSeen(System.currentTimeMillis());
                            }
                        }
                        PoAPI.getEventQueue().offer(new PoNetworkPacketRx(flag, (byte) len, payload.array()));
                        payload.clear();
                    }

                    if (payload != null) {
                        payload.clear();
                    }
                    network_msg_header.clear();
                }
            } catch (ClosedByInterruptException e) {
                Log.w(TAG, "ClosedByInterruptException: " + e.toString());
                cleanup();
                //if(!PoConnectionManager.disconnectCalled())
                //sendReconnectIntent();
                return;
            } catch (ClosedChannelException e) {	// by socket	
                Log.w(TAG, "ClosedChannelException: " + e.toString());
                cleanup();
                //if(!PoConnectionManager.disconnectCalled())
                //sendReconnectIntent();
                return;
            } catch (NotYetConnectedException e) {	// by socket
                Log.w(TAG, "NotYetConnectedException: " + e.toString());
                cleanup();
                //if(!PoConnectionManager.disconnectCalled())
                //	sendReconnectIntent();
                return;
            } catch (IOException e) {
                Log.w(TAG, "IOException: " + e.toString());
                cleanup();
                return;
            }
        }
    }

    public boolean isRunning() {
        return Running;
    }

    private String byteArrayToAscii(byte[] b) {
        String result = "";
        if (b == null) {
            return result;
        }

        for (int i = 0; i < b.length; i++) {
            result +=
                    Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    private void cleanup() {
        Running = false;
        sockch = null;
        network_msg_header = null;
        payload = null;
    }

    private void sendRegistryIntent() {
        Log.w(TAG, "GOT registry event");
        /*
         * If we get a rejected state it means that our seed has expired.
         */
        if (PoRegistryService.getInstance().getState() == PoRegistryService.STATE.REJECTED) {
            PoMiddlewareService.getInstance().stopPoMiddleware();
        }
        if (PoRegistryService.getInstance().getState() == PoRegistryService.STATE.REGISTERED) {
            PoConnectionManager.getInstance().startUpdateThread();
        }
        Log.w(TAG, "BROADCAST RECEIVER ONCREATE() RETURNING...(REGISTRY_EVENT)");
    }

    public void cancel() {
        interrupt();
    }

    private boolean isServer(int dstAddr) {
        return dstAddr >= 5 && dstAddr != (char) 0xffff;
    }
}
