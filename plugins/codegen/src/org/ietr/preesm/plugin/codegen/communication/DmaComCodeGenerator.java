package org.ietr.preesm.plugin.codegen.communication;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.ietr.preesm.core.architecture.route.AbstractRouteStep;
import org.ietr.preesm.core.architecture.simplemodel.Operator;
import org.ietr.preesm.core.codegen.AbstractCodeContainer;
import org.ietr.preesm.core.codegen.DataType;
import org.ietr.preesm.core.codegen.ImplementationPropertyNames;
import org.ietr.preesm.core.codegen.LinearCodeContainer;
import org.ietr.preesm.core.codegen.VertexType;
import org.ietr.preesm.core.codegen.WaitForCore;
import org.ietr.preesm.core.codegen.buffer.AbstractBufferContainer;
import org.ietr.preesm.core.codegen.buffer.Buffer;
import org.ietr.preesm.core.codegen.buffer.BufferAllocation;
import org.ietr.preesm.core.codegen.com.CommunicationFunctionCall;
import org.ietr.preesm.core.codegen.com.CommunicationFunctionInit;
import org.ietr.preesm.core.codegen.com.CommunicationThreadDeclaration;
import org.ietr.preesm.core.codegen.com.Receive;
import org.ietr.preesm.core.codegen.com.ReceiveInit;
import org.ietr.preesm.core.codegen.com.Send;
import org.ietr.preesm.core.codegen.com.SendInit;
import org.ietr.preesm.core.codegen.model.CodeGenSDFSendVertex;
import org.sdf4j.model.sdf.SDFAbstractVertex;
import org.sdf4j.model.sdf.SDFEdge;

/**
 * Generating communication code (initialization and calls) for a dma Route Step
 * 
 * @author mpelcat
 */
public class DmaComCodeGenerator extends AbstractComCodeGenerator {

	/**
	 * Buffer containing all the addresses of the distant core where to send the data
	 */
	private Buffer addressBuffer = null;
	
	private AbstractCodeContainer dmaInitCodeContainer = null;
	
	public DmaComCodeGenerator(CommunicationThreadDeclaration comThread,SortedSet<SDFAbstractVertex> vertices, AbstractRouteStep step) {
		super(comThread,vertices, step);
		
		dmaInitCodeContainer = new LinearCodeContainer(this.comThread.getBeginningCode());
		
		// Testing the number of send functions with the given route step
		int addressBufferSize = 0;
		for(SDFAbstractVertex v : vertices){
			AbstractRouteStep vStep = (AbstractRouteStep)v.getPropertyBean().getValue(ImplementationPropertyNames.SendReceive_routeStep);
			if(vStep.equals(step) && v instanceof CodeGenSDFSendVertex){
				addressBufferSize++;
			}
		}
		
		// Adding the address buffer
		if(addressBufferSize!=0){
			addressBuffer = new Buffer("addressBuffer",addressBufferSize,new DataType("void *",4),null,comThread.getGlobalContainer());
			BufferAllocation alloc = new BufferAllocation(addressBuffer);
			comThread.getGlobalContainer().addBuffer(alloc);
		}
	}


	/**
	 * Calls the initialization functions at the beginning of computation and
	 * communication thread executions
	 */
	protected void createinits(CommunicationFunctionCall call,
			AbstractBufferContainer bufferContainer,
			Set<CommunicationFunctionInit> alreadyInits) {

		CommunicationFunctionInit initCom = null;
		WaitForCore wait = null;

		// Creating Send and Receive initialization calls
		if (call instanceof Send) {
			Send send = (Send) call;

			initCom = new SendInit(bufferContainer, send.getTarget().getName(),
					send.getRouteStep());
			wait = new WaitForCore(bufferContainer, send.getTarget().getName());
			
		} else if (call instanceof Receive) {
			Receive receive = (Receive) call;

			initCom = new ReceiveInit(bufferContainer, receive.getSource()
					.getName(), receive.getRouteStep());
			wait = new WaitForCore(bufferContainer, receive.getSource()
					.getName());
		}

		// Checking that the initialization has not already been done
		if (initCom != null) {
			for (CommunicationFunctionInit oldInit : alreadyInits) {
				if (oldInit.getConnectedCoreId().equals(
						initCom.getConnectedCoreId())
						&& oldInit.getRouteStep().equals(initCom.getRouteStep())) {
					// core wait has already been done
					wait = null;

					if (oldInit.getName().equals(initCom.getName())) {
						// init has already been done with same direction
						initCom = null;
					}
					break;
				}
			}
		}

		// Adding Send and Receive initialization calls
		if (initCom != null) {
			dmaInitCodeContainer.addCodeElementFirst(initCom);
			alreadyInits.add(initCom);
		}

		// Adding other cores wait
		if (wait != null) {
			dmaInitCodeContainer.addCodeElement(wait);
		}
	}

	/**
	 * creates a send or a receive depending on the vertex type
	 */
	protected List<CommunicationFunctionCall> createCalls(
			AbstractBufferContainer parentContainer, SDFAbstractVertex vertex) {

		List<CommunicationFunctionCall> calls = new ArrayList<CommunicationFunctionCall>();

		// retrieving the vertex type
		VertexType type = (VertexType) vertex.getPropertyBean().getValue(
				ImplementationPropertyNames.Vertex_vertexType);

		AbstractRouteStep rs = (AbstractRouteStep) vertex.getPropertyBean()
				.getValue(ImplementationPropertyNames.SendReceive_routeStep);

		Set<SDFEdge> inEdges = (vertex.getBase().incomingEdgesOf(vertex));
		Set<SDFEdge> outEdges = (vertex.getBase().outgoingEdgesOf(vertex));

		if (type != null && rs != null) {
			if (type.isSend()) {

				List<Buffer> bufferSet = parentContainer.getBuffers(inEdges);

				// The target is the operator on which the corresponding receive
				// operation is mapped
				SDFAbstractVertex receive = ((SDFEdge) outEdges.toArray()[0])
						.getTarget();
				Operator target = (Operator) receive.getPropertyBean()
						.getValue(ImplementationPropertyNames.Vertex_Operator);

				// Case of one send for multiple buffers
				// calls.add(new Send(parentContainer, vertex, bufferSet,
				// medium,
				// target));

				// Case of one send per buffer
				for (Buffer buf : bufferSet) {
					List<Buffer> singleBufferSet = new ArrayList<Buffer>();
					singleBufferSet.add(buf);
					calls.add(new Send(parentContainer, vertex,
							singleBufferSet, rs, target));
				}

			} else if (type.isReceive()) {
				List<Buffer> bufferSet = parentContainer.getBuffers(outEdges);

				// The source is the operator on which the corresponding send
				// operation is allocated
				SDFAbstractVertex send = ((SDFEdge) inEdges.toArray()[0])
						.getSource();
				Operator source = (Operator) send.getPropertyBean().getValue(
						ImplementationPropertyNames.Vertex_Operator);

				// Case of one receive for multiple buffers
				// calls.add(new Receive(parentContainer, vertex, bufferSet,
				// medium,
				// source));

				// Case of one receive per buffer
				for (Buffer buf : bufferSet) {
					List<Buffer> singleBufferSet = new ArrayList<Buffer>();
					singleBufferSet.add(buf);
					calls.add(new Receive(parentContainer, vertex,
							singleBufferSet, rs, source));
				}
			}
		}

		return calls;
	}
}
