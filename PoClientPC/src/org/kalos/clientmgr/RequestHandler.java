/*
 * Author: Kwstas Lekkas , kwstasl@gmail.com
 */
package org.kalos.clientmgr;

import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.io.*;
import org.kalos.Log;
import org.lekkas.poclient.PoAPI.PoAPI;
import org.lekkas.poclient.PoEvents.PoNetworkPacketRx;
import org.lekkas.poclient.PoMainA;

public class RequestHandler implements Runnable {

    private static final String TAG = "RequestHandler:";
    private boolean v;
    SelectionKey sk;
    SocketChannel sock;
    private ByteBuffer network_msg_header;
    private ByteBuffer payload;
    private byte flag;

    // private char dstAddr;
    // private char srcAddr;
    enum OP {

        SEND_REG_MSG,
        SEND_PO_MSG,
        SEND_SERVERS_MSG
    };
    private OP op;

    public RequestHandler(SocketChannel s) throws Exception {
        sock = s;
        v = PoMainA.isVerbose();
    }

    /*
     * Socket is ready for read()'ing
     */
    @Override
    public void run() {
        try {
            read();
            process();
            write();
        } catch (IOException e) {
            Log.e(TAG, "IO Error: " + e.toString());
        }
    }

    private void read() throws IOException {
        if (v) {
            Log.d(TAG, "Read()");
        }
        /*
         * Reading network message header.
         */
        network_msg_header = ByteBuffer.allocate(Network_Msg.HEADER_LEN);
        int ret = sock.read(network_msg_header);
        if (ret == 0) {
            if (v) {
                Log.d(TAG, "Read 0 bytes.");
            }
            return;
        }
        if (ret == -1) {
            //exc_addr = Reactor.registry.isRegisted(sock).getPoNodeAddr();
            sock.close();
            throw new IOException("sock.read() returned -1. Server has probably "
                    + "closed the connection");
        }
        int bytes_read = ret;
        while (bytes_read < Network_Msg.HEADER_LEN) {
            ret = sock.read(network_msg_header);
            if (ret == -1) {
                throw new IOException("sock.read() returned -1. Server has probably "
                        + "closed the connection");
            }
            bytes_read += ret;
        }
        /*
         * Reading message payload
         */
        int pay_len = uByteToInt(network_msg_header.get(1));
        payload = ByteBuffer.allocate(pay_len);
        ret = sock.read(network_msg_header);
        if (ret == -1) {
            throw new IOException("sock.read() returned -1. Server has probably "
                    + "closed the connection");
        }
        bytes_read = ret;
        while (bytes_read < pay_len) {
            ret = sock.read(payload);
            if (ret == -1) {
                throw new IOException("sockch.read() returned -1. Server has probably "
                        + "closed the connection");
            }
            bytes_read += ret;
        }
        /*
         * We now have header + payload.
         */
        if (v) {
            Log.d(TAG, "Got header+payload");
        }
        if (v) {
            Log.d(TAG, "packet: " + byteArrayToAscii(network_msg_header.array())
                    + byteArrayToAscii(payload.array()));
        }
    }

    private void process() throws IOException {
        if (network_msg_header.hasRemaining()) {
            return;
        }
        if (v) {
            Log.e(TAG, "Process()");
        }

        flag = network_msg_header.get(0);

        if (flag == Network_Msg.POBICOS_MSG) {
            op = OP.SEND_PO_MSG;
            NodeInfo n = Reactor.registry.isRegisted(sock);
            if (n == null) {
                char addr = payload.getChar(0);
                
                int msg_len = payload.get(4);
                double lat = payload.getDouble(msg_len+7);
                double lon = payload.getDouble(msg_len+7+8);
                
                System.out.println("PETROS lat "+lat+"lan "+lon);
                
                Log.w(TAG, ": Add new node "+(int)addr);
                n = new NodeInfo(sock, addr);
                Reactor.registry.reg.add(n);
            } else {
                n.setLastSeen(System.currentTimeMillis());
            }
        }
    }

    private void write() throws IOException {
        if (network_msg_header.hasRemaining()) {
            return;
        }

        if (op == OP.SEND_PO_MSG) {
            Log.e(TAG, "TEST : Write to event queue from "+(int)payload.getChar(0));
            PoAPI.getEventQueue().offer(new PoNetworkPacketRx(flag, (byte) network_msg_header.get(1), payload.array()));
        }
    }

    private String byteArrayToAscii(byte[] b, int start, int end) {
        String result = "";
        if (b == null) {
            return result;
        }

        for (int i = start; i <= end; i++) {
            result +=
                    Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;

    }

    public String byteArrayToAscii(byte[] b) {
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

    public static int uByteToInt(byte b) {
        return (int) (b & 0xFF);
    }
}
