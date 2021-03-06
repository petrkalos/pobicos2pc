#include <pobicos.h>
#include <string.h>

#define CHILD_TYPE 0x0000CC32

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
  PoDbgString("root: child created\n");
  PoGetChildInfoOld(&cid,&ctype,&h);
  PoSetTimer(0,TIMER_TIMEOUT);
}

EVENT_HANDLER(PoTimeoutEvent){
  PoMsg msg;
  PoTimerID id;
  
  PoGetTimerID(&id);
  
  
  PoDbgString("root: send command\n");
  sprintf((char *)msg.data,"%s --- %d","PETROS: message test",id);
  msg.len=strlen((char *)msg.data)+1;
  PoSendCommand(cid,&msg,PO_MSG_BESTEFFORT);
  
  if(id>MAX_MESSAGES) PoRelease(PoGetMyID());
  else PoSetTimer(++id,TIMER_TIMEOUT);
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
  
}
