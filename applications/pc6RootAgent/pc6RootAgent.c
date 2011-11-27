#include <pobicos.h>
#include <string.h>

#define CHILD_TYPE 0x0000CC62

#define CREATION_TIMEOUT 2
#define TIMER_TIMEOUT 2000

#define MAX_MESSAGES 10

PoAgentID cid;
PoAgentType ctype;
PoReqHandle h;

EVENT_HANDLER(PoInitEvent){
  PoDbgString("root: init\n");
  PoCreateGenericAgent(CHILD_TYPE,CREATION_TIMEOUT, 0);
}

EVENT_HANDLER(PoChildCreatedEvent){
  PoMsg msg;
  
  PoDbgString("root: child created\n");
  PoGetChildInfoOld(&cid,&ctype,&h);
  
  PoDbgString("root: send command\n");
  sprintf((char *)msg.data,"ping");
  msg.len=strlen((char *)msg.data)+1;
  PoSendCommand(cid,&msg,PO_MSG_BESTEFFORT);
}

EVENT_HANDLER(PoReportArrivedEvent){
  PoMsg msg;
  
  PoAgentID cid;
  PoAgentType ctype;
  PoReqHandle h;
  
  PoGetChildInfoOld(&cid,&ctype,&h);	
  
  PoDbgString("root: report arrived\n");
  PoGetReport(&msg);
  PoDbgString((char *)msg.data);
  
  PoRelease(PoGetMyID());
}
