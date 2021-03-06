#include <pobicos.h>

#define CREATION_TIMEOUT 5000

#define CHILD_ID 0x0000CC42

EVENT_HANDLER(PoInitEvent){
  PoDbgString("PETROS: root: Create child\n");
  PoCreateGenericAgent(CHILD_ID,CREATION_TIMEOUT,0);
}

EVENT_HANDLER(PoChildCreatedEvent){
  PoAgentID cid;
  PoAgentType ctype;
  PoReqHandle h;
  
  PoGetChildInfoOld(&cid,&ctype,&h);
  
  PoDbgString("PETROS: root: child created\n");
  PoDbgUInt32(cid);
  PoDbgUInt32(ctype);
  PoRelease(PoGetMyID());
}
