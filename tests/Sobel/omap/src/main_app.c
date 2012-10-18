#include <xdc/std.h>

/*  -----------------------------------XDC.RUNTIME module Headers    */
#include <xdc/runtime/Memory.h>
#include <xdc/runtime/System.h>
#include <xdc/runtime/IHeap.h>

/*  ----------------------------------- IPC module Headers           */
#include <ti/ipc/MultiProc.h>
#include <ti/ipc/Notify.h>
#include <ti/ipc/Ipc.h>
#include <ti/ipc/GateMP.h>
#include <ti/ipc/MessageQ.h>
#include <ti/ipc/HeapBufMP.h>

/*  ----------------------------------- BIOS6 module Headers         */
#include <ti/sysbios/knl/Semaphore.h>
#include <ti/sysbios/syncs/SyncSem.h>
#include <ti/sysbios/BIOS.h>

/*  ----------------------------------- To get globals from .cfg Header */
#include <xdc/cfg/global.h>

#include <com.h>

void computationThread(void);

int Semaphores_init(Semaphore_Handle *sem, int number){
	int i;
	for(i=0; i<number; i++){
		sem[i] = Semaphore_create(0, NULL, NULL);
	}
	return 0;
}

/*
 *  ======== main ========
 */
Int main(Int argc, Char* argv[]){
    /* Set up interprocessor notifications */
    Ipc_start();
    BIOS_start();
    return (0);
}

/*
 *  ======== tsk1_func ========
 *  Receive and return messages until the die request comes.
 */
Void tsk1_func(UArg arg0, UArg arg1){
	UInt16 hostProcId = MultiProc_getId("MPU");
	while (Ipc_attach(hostProcId) < 0);
	UInt16 sysProcId = MultiProc_getId("SysM3");
	while (Ipc_attach(sysProcId) < 0);

	Task_sleep(10);
	comInitCoPro();
	computationThread();
	comClear();

	Ipc_detach(sysProcId);
	Ipc_detach(hostProcId);
}

/*
 *  @(#) ti.sdo.ipc.samples; 1, 0, 0, 0,238; 6-11-2009 16:20:18; /db/vtree/library/trees/ipc/ipc-b48x/src/
 */
