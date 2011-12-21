/*
 * Author: Kwstas Lekkas , kwstasl@gmail.com
 */

package org.kalos.clientmgr;


import java.nio.channels.*;
import java.io.*;
import org.lekkas.poclient.PoMainA;

public class Accept_Handler implements Runnable{
	private static final String TAG = "ACCEPT_HANDLER:";
	SocketChannel sock;
	ServerSocketChannel serverSock;
	Selector selector;
	boolean v;
	
	public Accept_Handler(ServerSocketChannel s, Selector sel) {
		serverSock = s;
		selector = sel;
		v = PoMainA.isVerbose();
	}
	
	public void run() {
		try {
			sock = serverSock.accept();
			if(sock == null)
				return;
		
			sock.configureBlocking(false); // false for Non-Blocking
			if(v) System.out.println(TAG+"Accepted, now creating handler." +
					"(is sock null?: "+(sock == null)+")");
			SelectionKey sk = sock.register(selector,  SelectionKey.OP_READ);
			sk.attach(new RequestHandler(sock));
			if(v) System.out.println(TAG+"Registered handler");
			//selector.wakeup();
			
		} catch (IOException e) {
			System.out.println("Error accepting: "+e.toString());
		} catch (Exception e) {
			System.out.println("Exception thrown from run() of AcceptHandler: "+e.getMessage());
			System.out.println("AcceptHandler: is sock null?"+(sock == null));
		}
	}
}
