/*
 * Author: Kwstas Lekkas , kwstasl@gmail.com
 */

package org.kalos.clientmgr;

import java.nio.channels.*;

public class SockRegisterTask {
	RequestHandler hndl;
	SocketChannel sock;
	Request REQ;
	
	public SockRegisterTask(RequestHandler h, SocketChannel s) {
		hndl = h;
		sock = s;
		REQ = Request.REGISTER;
	}
	
	enum Request {
		REGISTER,
		UNREGISTER
	} ;
}
