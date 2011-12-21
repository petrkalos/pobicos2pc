package org.kalos.server;

import java.io.IOException;
import java.net.InetSocketAddress;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.lekkas.poclient.PoConnectionManager;



import android.util.Log;

public class ServerNode {
	SocketChannel sock = null;
	String server_ip;
	int server_port;
	char PoAddr;

	public ServerNode(char PoAddr, String server_ip, int server_port) {
		this.PoAddr = PoAddr;
		this.server_ip = server_ip;
		this.server_port = server_port;
		this.getSocket();
	}

	public SocketChannel getSocket() {
		try {
			if (sock == null) {
				sock = SocketChannel.open();
				sock.configureBlocking(false);
				sock.register(PoConnectionManager.getInstance().getSelector(), SelectionKey.OP_READ);
				sock.connect(new InetSocketAddress(server_ip, server_port));
				
				while (!sock.finishConnect()) {
				}
				if (sock.isConnected()) {
					Log.w("NODE", "PETROS: socket is connected");
				} else {
					Log.w("NODE", "PETROS: socket is not connected");
				}
			}
			return sock;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public char getPoAddr() {
		return PoAddr;
	}

}
