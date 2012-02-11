/*
 * Author: Kwstas Lekkas , kwstasl@gmail.com
 */
package org.lekkas.poclient;

import java.util.concurrent.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;

import org.lekkas.poclient.PoAPI.PoAPI;

import org.kalos.Log;
import org.kalos.clientmgr.Reactor;
import org.kalos.servermgr.ServerManager;

public class PoCommMgrWriteTask extends Thread implements Runnable {

    private final String TAG = "PoCommMgrWriteTask";
    private LinkedBlockingQueue<Network_Msg> OutMsgQ;
    private SocketChannel sockch;
    private Network_Msg msg;
    private ByteBuffer buf;
    private boolean Running;
    public static boolean logBytes = false;
    public static int sentBytes;
    public static int cntTxPkts;
    public static double app_lat = 39;
    public static double app_lon = 22;
    public static char app_radiuskm = 100;

    public PoCommMgrWriteTask(SocketChannel s) {
        sockch = s;
        Running = false;
        Log.e(TAG, "app_lat = " + app_lat);
        Log.e(TAG, "app_lon = " + app_lon);
        Log.e(TAG, "app_radiuskm = " + app_radiuskm);
    }

    @Override
    public void run() {
        OutMsgQ = PoConnectionManager.getInstance().getOutMsgQ();

        Log.w(TAG, "Thread started!");
        Running = true;
        while (!Thread.currentThread().isInterrupted()) {
            try {
                msg = OutMsgQ.take();	// BLOCKS WHEN EMPTY

                /*
                 * MSG format:
                 * 
                 * Msg Type:	byte:	1 byte
                 * Len:			byte:	1 bytes
                 * Payload:		byte[]:	{Len} bytes
                 * 
                 * Where Payload:
                 * 
                 * 1) For POBICOS messages:
                 * Source addr		: char		: 2 bytes
                 * Destination addr	: char		: 2 bytes
                 * 			Msg Len	: byte		: 1 byte
                 * 				Msg	: byte[]	: {Msg Len} bytes;
                 * 			RadiusKM: char		: 2 bytes
                 * 			Lat		: double	: 8 bytes
                 * 			Lon		: double	: 8 bytes
                 * 
                 * 2) For registration messages:
                 * Class Of Device:	byte	: 1 byte 
                 *    GPS Latitude:	double	: 8 bytes
                 *   GPS Longitude: double	: 8 bytes			
                 */

                int capacity = Network_Msg.HEADER_LEN + Serialization.uint8ToInt(msg.payload_len);
                int p = Serialization.uint8ToInt(msg.payload_len);

                if (msg.msg_type == Network_Msg.POBICOS_MSG) {
                    capacity += 18;
                    p += 18;
                }

                byte paylen = (byte) p;
                Log.w(TAG, "paylen(byte) == " + paylen);

                buf = ByteBuffer.allocate(capacity);
                buf.put(msg.msg_type).put(paylen);              // Packet header.
                buf.put(msg.payload);                           // Packet payload

                if (msg.msg_type == Network_Msg.POBICOS_MSG) {
                    //if (PoAPI.hostsRootAgent()) {
                        //buf.putChar(app_radiuskm).putDouble(app_lat).putDouble(app_lon);
                    //} else {
                        buf.putChar((char) 0).putDouble(-1).putDouble(-1);
                    //}
                    Log.w(TAG, "sending packet: " + byteArrayToAscii(buf.array()));
                }

                buf.position(0);

                if (msg.msg_type == Network_Msg.POBICOS_MSG) {
                    char destaddr = buf.getChar(4);
                    if (destaddr == (char) 0xffff) {
                        Log.d(TAG,"broadcast message");
                        while (buf.hasRemaining()) {
                            sockch.write(buf);
                        }
                    } else if (isServer(destaddr)) {
                        Log.w(TAG, "send message to server client" + (int) destaddr);
                        SocketChannel servsock = ServerManager.getInstance().getServerSocket(destaddr);
                        while (buf.hasRemaining()) {
                            servsock.write(buf);
                        }
                        Log.w(TAG, "packet: " + byteArrayToAscii(buf.array()));
                    } else {
                        SocketChannel client_sockch = Reactor.registry.isRegisted(destaddr,true).getSocketChannel();
                        Log.w(TAG, "send message to mobile client");
                        if (client_sockch != null) {
                            Log.d(TAG,"unicast message");
                            while (buf.hasRemaining()) {
                                client_sockch.write(buf);
                            }

                        }

                    }
                } else {
                    Log.d(TAG,"send message through directory");
                    while (buf.hasRemaining()) {
                        sockch.write(buf);
                    }
                }

                Log.w(TAG, "Wrote() " + buf.capacity() + "bytes.");
                if (logBytes && msg.msg_type == Network_Msg.POBICOS_MSG) {
                    sentBytes += buf.capacity();
                    cntTxPkts++;
                }
                buf.clear();
            } catch (InterruptedException e) {	// thrown by queue	
                Log.w(TAG, "Writer Thread Interrupted.Queue Exception" + e.toString());
                cleanup();
                return;
            } catch (ClosedChannelException e) {
                Log.w(TAG, "Writer Thread Interrupted." + e.toString());
                cleanup();
                return;
            } catch (NotYetConnectedException e) {
                Log.w(TAG, "Writer Thread Interrupted." + e.toString());
                cleanup();
                return;
            } catch (IOException e) {
                Log.w(TAG, "Writer Thread Interrupted." + e.toString());
                cleanup();
                return;
            } catch (Exception e) {
                Log.w(TAG, "Writer thread err: " + e.toString());
                cleanup();
                return;
            }

        }
    }

    private void cleanup() {
        Running = false;
        sockch = null;
        msg = null;
        buf = null;
        OutMsgQ = null;
    }

    public boolean isRunning() {
        return Running;
    }

    public void cancel() {
        interrupt();
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

    private boolean isServer(int dstAddr) {
        Log.w(TAG, ": dst addr is " + dstAddr);
        return dstAddr >= 5 && dstAddr != (char) 0xffff;
    }
}
