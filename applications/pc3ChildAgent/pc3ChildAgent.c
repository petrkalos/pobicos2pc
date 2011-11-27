#include <pobicos.h>
#include <string.h>

#define TIMEOUT 1000

EVENT_HANDLER(PoInitEvent){
	PoDbgString("child: init\n");
}

EVENT_HANDLER(PoCommandArrivedEvent){
	PoMsg msg;
	
	PoDbgString("child: got a command\n");
	PoGetCommand(&msg);
	PoDbgString((char *)msg.data);
	PoSendReport(&msg,PO_MSG_BESTEFFORT);
}
