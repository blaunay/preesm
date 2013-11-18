package org.ietr.preesm.codegen.xtend.printer.c

import org.ietr.preesm.codegen.xtend.printer.c.C6678CPrinter
import org.ietr.preesm.codegen.xtend.model.codegen.FifoCall
import org.ietr.preesm.codegen.xtend.model.codegen.PortDirection
import org.ietr.preesm.codegen.xtend.model.codegen.FifoOperation
import org.ietr.preesm.codegen.xtend.model.codegen.SpecialCall
import org.ietr.preesm.codegen.xtend.model.codegen.FunctionCall
import org.ietr.preesm.codegen.xtend.model.codegen.SubBuffer
import org.ietr.preesm.codegen.xtend.model.codegen.Buffer
import org.ietr.preesm.codegen.xtend.model.codegen.Call
import org.ietr.preesm.codegen.xtend.model.codegen.CoreBlock
import org.ietr.preesm.codegen.xtend.model.codegen.CallBlock
import org.ietr.preesm.codegen.xtend.model.codegen.SharedMemoryCommunication
import org.ietr.preesm.codegen.xtend.model.codegen.Direction
import org.ietr.preesm.codegen.xtend.model.codegen.Delimiter

class DynamicAllocC6678CPrinter extends C6678CPrinter {
	new() {
		super()
		IGNORE_USELESS_MEMCPY = false
	}
	
	override printCoreBlockHeader(CoreBlock block) '''
		«super.printCoreBlockHeader(block)»
		#include <stdlib.h>
		#include <ti/ipc/SharedRegion.h>
		#include <ti/ipc/HeapMemMP.h>
		
		
	'''
	
	override printCoreInitBlockHeader(CallBlock callBlock) '''
	void «(callBlock.eContainer as CoreBlock).name.toLowerCase»(void){
		HeapMemMP_Handle sharedHeap;
		
		// Initialisation(s)
		communicationInit();
		sharedHeap = SharedRegion_getHeap(1);
		
	'''

	override printBufferDefinition(Buffer buffer) '''
		#pragma DATA_SECTION(«buffer.name»,".MSMCSRAM");
		#pragma DATA_ALIGN(«buffer.name», 128);
		«buffer.type» far *«buffer.name»; // «buffer.comment» size:= «buffer.size»*«buffer.type»
	'''
	
	override printSubBufferDefinition(SubBuffer buffer) '''
		«printBufferDefinition(buffer)»
	'''
	override printBufferDeclaration(Buffer buffer) '''
		extern  «buffer.type» far *«buffer.name»; // «buffer.comment» size:= «buffer.size»*«buffer.type»
	'''
	
	override printSubBufferDeclaration(SubBuffer buffer) '''
		«printBufferDeclaration(buffer)»
	'''
	
	override printFunctionCall(FunctionCall functionCall) '''
		«printCallWithMallocFree(functionCall,super.printFunctionCall(functionCall))»
	'''
		/**
	 * Add the necessary <code>malloc()</code> and <code>free()</code> calls to
	 * a call passed as a parameter. Before the {@link Call}, a
	 * <code>malloc()</code> for all the output {@link Buffer} of the
	 * {@link Call} is printed. After the {@link Call}, a <code>free()</code> is
	 * printed for each output {@link Buffer} of the {@link Call}.
	 * 
	 * @param call
	 *            the {@link Call} to print.
	 * @param sequence
	 *            the normal printed {@link Call} (by the {@link CPrinter}.
	 * @return the {@link Call} printed with <code>malloc()</code> and
	 */
	def printCallWithMallocFree(Call call, CharSequence sequence) '''
		«/*Allocate output buffers */
		IF call.parameters.size > 0»
			«FOR i : 0 .. call.parameters.size -1 »
				«IF call.parameterDirections.get(i) == PortDirection.OUTPUT»
					«printMalloc(call.parameters.get(i) as Buffer)»
				«ENDIF»
			«ENDFOR»
		«ENDIF»
		«sequence»
		«/*Free input buffers*/
		IF call.parameters.size > 0»
			«FOR i : 0 .. call.parameters.size -1 »
				«IF call.parameterDirections.get(i) == PortDirection.INPUT»
					«printFree(call.parameters.get(i) as Buffer)»
				«ENDIF»
			«ENDFOR»
		«ENDIF»
	'''

	override printFifoCall(FifoCall fifoCall) '''
		«IF fifoCall.operation == FifoOperation.INIT»
			«IF fifoCall.bodyBuffer != null»
				«printMalloc(fifoCall.bodyBuffer)»
			«ENDIF»
		«ENDIF»
		«IF fifoCall.operation == FifoOperation.PUSH || fifoCall.operation == FifoOperation.INIT»
			«printMalloc(fifoCall.headBuffer)»
		«ENDIF»
		«printCallWithMallocFree(fifoCall,super.printFifoCall(fifoCall))»
		«IF fifoCall.operation == FifoOperation.POP»
			«printFree(fifoCall.headBuffer)»
		«ENDIF»
	'''
	
	override printBroadcast(SpecialCall call) '''
		«printCallWithMallocFree(call,super.printBroadcast(call))»
	'''	
	
	override printRoundBuffer(SpecialCall call) '''
		«printCallWithMallocFree(call,super.printRoundBuffer(call))»
	'''	
	
	override printFork(SpecialCall call) '''
		«printCallWithMallocFree(call,super.printFork(call))»
	'''
	
	override printJoin(SpecialCall call) '''
		«printCallWithMallocFree(call,super.printJoin(call))»
	'''
	/**
	 * Methods used to print a Malloc.
	 * @param buffer 
	 * 			the {@link Buffer} that is allocated  
	 * @return the printed code as a {@link CharSequence}.
 	 */
	def printMalloc(Buffer buffer) {
		'''
			«buffer.doSwitch» = («buffer.type»*) HeapMemMP_alloc(sharedHeap,«buffer.size»*sizeof(«buffer.type»), 128);
			cache_wb(&«buffer.doSwitch», 1);
		'''
	}
	
	/**
	 * Methods used to print a Free.
	 * @param buffer 
	 * 			the {@link Buffer} that freed  
	 * @return the printed code as a {@link CharSequence}.
 	 */
	def printFree(Buffer buffer) {
		'''
			HeapMemMP_free(sharedHeap, «buffer.doSwitch», «buffer.size»*sizeof(«buffer.type»));
			cache_inv(&«buffer.doSwitch», 1);
		'''
	}
	
	override printSharedMemoryCommunication(SharedMemoryCommunication communication) '''
		«IF communication.direction == Direction::SEND && communication.delimiter == Delimiter::START»
			«FOR subbuffer : (communication.data as Buffer).childrens»
				cache_wbInv(«subbuffer.doSwitch», «subbuffer.size»*sizeof(«subbuffer.type»));
			«ENDFOR»
		«ENDIF»
		«super.printSharedMemoryCommunication(communication).toString.replaceAll("(cache_.*?;)","// $1")»
		«IF communication.direction == Direction::RECEIVE && communication.delimiter == Delimiter::END»
			«FOR subbuffer : (communication.data as Buffer).childrens»
				cache_inv(«subbuffer.doSwitch», «subbuffer.size»*sizeof(«subbuffer.type»));
			«ENDFOR»
		«ENDIF»	
	'''
	
}