#include <pobicos.h>

#define MAX_TIMERS 10
#define TIMER_TIMEOUT 1000

EVENT_HANDLER(PoInitEvent){
  int id=0;
  PoDbgString("PETROS: Testing timers for MAX_TIMERS:");
  PoDbgUInt32(MAX_TIMERS);
  PoSetTimer(id,TIMER_TIMEOUT);
}

EVENT_HANDLER(PoTimeoutEvent){
  PoTimerID id;
  
  PoGetTimerID(&id);
  
  PoDbgString("PETROS: Timeout timer:");
  PoDbgUInt32(id);
  
  if(id<MAX_TIMERS) PoSetTimer(++id,TIMER_TIMEOUT);
  else	PoRelease(PoGetMyID());
}
