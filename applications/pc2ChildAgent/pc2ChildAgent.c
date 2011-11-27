#include <pobicos.h>

EVENT_HANDLER(PoInitEvent){
  PoDbgString("PETROS: child: Hello\n");
  PoRelease(PoGetMyID());
}
