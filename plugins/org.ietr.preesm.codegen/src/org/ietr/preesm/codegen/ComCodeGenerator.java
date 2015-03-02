/*********************************************************
Copyright or � or Copr. IETR/INSA: Matthieu Wipliez, Jonathan Piat,
Maxime Pelcat, Jean-Fran�ois Nezan, Micka�l Raulet

[mwipliez,jpiat,mpelcat,jnezan,mraulet]@insa-rennes.fr

This software is a computer program whose purpose is to prototype
parallel applications.

This software is governed by the CeCILL-B license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-B
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-B license and that you accept its terms.
 *********************************************************/

package org.ietr.preesm.codegen;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.logging.Level;

import org.ietr.dftools.algorithm.model.sdf.SDFAbstractVertex;
import org.ietr.dftools.algorithm.model.sdf.SDFEdge;
import org.ietr.dftools.architecture.slam.ComponentInstance;
import org.ietr.dftools.workflow.tools.WorkflowLogger;
import org.ietr.preesm.codegen.model.ICodeGenSDFVertex;
import org.ietr.preesm.codegen.model.buffer.AbstractBufferContainer;
import org.ietr.preesm.codegen.model.buffer.Buffer;
import org.ietr.preesm.codegen.model.com.CommunicationFunctionCall;
import org.ietr.preesm.codegen.model.com.CommunicationFunctionCall.Phase;
import org.ietr.preesm.codegen.model.com.ReceiveMsg;
import org.ietr.preesm.codegen.model.com.SendMsg;
import org.ietr.preesm.codegen.model.containers.AbstractCodeContainer;
import org.ietr.preesm.codegen.model.containers.CodeSectionType;
import org.ietr.preesm.codegen.model.main.ICodeElement;
import org.ietr.preesm.codegen.model.main.SourceFile;
import org.ietr.preesm.codegen.model.main.SourceFileList;
import org.ietr.preesm.core.architecture.route.AbstractRouteStep;
import org.ietr.preesm.core.types.ImplementationPropertyNames;
import org.ietr.preesm.core.types.VertexType;
import org.jgrapht.alg.DirectedNeighborIndex;

/**
 * Generating communication code (initialization and calls) for a given type of
 * Route Step The abstract class gathers the common commands
 * 
 * @author mpelcat
 */
public class ComCodeGenerator {

	/**
	 * This class gathers communication function calls and existing calls that
	 * help insert them in existing code.
	 */
	public class ComCalls {
		/**
		 * Generated start communication calls
		 */
		protected List<CommunicationFunctionCall> startComZoneCalls = null;

		/**
		 * Generated end communication calls (for non-blocking communications)
		 */
		protected List<CommunicationFunctionCall> endComZoneCalls = null;

		/**
		 * Sender and receiver calls that make this communication necessary
		 */
		List<ICodeElement> receiverCalls = null;
		List<ICodeElement> senderCalls = null;

		/**
		 * Communication vertex corresponding to the calls
		 */
		SDFAbstractVertex vertex;

		/**
		 * Type (send or receive) of the current vertex
		 */
		VertexType type;

		public ComCalls(SDFAbstractVertex vertex) {
			super();
			this.startComZoneCalls = new ArrayList<CommunicationFunctionCall>();
			this.endComZoneCalls = new ArrayList<CommunicationFunctionCall>();
			this.senderCalls = new ArrayList<ICodeElement>();
			this.receiverCalls = new ArrayList<ICodeElement>();
			this.vertex = vertex;
			this.type = (VertexType) vertex.getPropertyBean().getValue(
					ImplementationPropertyNames.Vertex_vertexType);
		}

		public List<CommunicationFunctionCall> getStartComZoneCalls() {
			return startComZoneCalls;
		}

		public void addStartComZoneCall(
				CommunicationFunctionCall startComZoneCall) {
			this.startComZoneCalls.add(startComZoneCall);
		}

		public List<CommunicationFunctionCall> getEndComZoneCalls() {
			return endComZoneCalls;
		}

		public void addEndComZoneCall(CommunicationFunctionCall endComZoneCall) {
			this.endComZoneCalls.add(endComZoneCall);
		}

		public List<ICodeElement> getReceiverCalls() {
			return receiverCalls;
		}

		public void setReceiverCalls(List<ICodeElement> relativeCalls) {
			this.receiverCalls = relativeCalls;
		}

		public List<ICodeElement> getSenderCalls() {
			return senderCalls;
		}

		public void setSenderCalls(List<ICodeElement> senderCalls) {
			this.senderCalls = senderCalls;
		}

		public SDFAbstractVertex getVertex() {
			return vertex;
		}

		/**
		 * testing if calls are necessary
		 * 
		 * @return true if the communication calls are necessary because used by
		 *         both sender and receiver code or used to transmit to another operator
		 */
		public boolean isNecessary() {
			return (!getSenderCalls().isEmpty() && !getReceiverCalls()
					.isEmpty())
					|| VertexType.isIntermediateSend(vertex)
					|| VertexType.isIntermediateReceive(vertex);
		}

		/**
		 * @return the type (send or receive)
		 */
		public VertexType getType() {
			return type;
		}
	}

	/**
	 * Code container where we want to add communication
	 */
	protected AbstractCodeContainer container = null;

	/**
	 * The considered communication vertices (send, receive)
	 */
	protected SortedSet<SDFAbstractVertex> vertices = null;

	/**
	 * The considered communication vertices (send, receive)
	 */
	protected AbstractRouteStep step = null;

	public ComCodeGenerator(AbstractCodeContainer container,
			SortedSet<SDFAbstractVertex> vertices, AbstractRouteStep step) {
		super();
		this.container = container;
		this.vertices = vertices;
		this.step = step;
	}

	/**
	 * Creating coms for a given communication vertex
	 */
	public void insertComs(SDFAbstractVertex vertex,
			CodeSectionType sectionType, SourceFileList sourceFiles) {

		// Creating he calls to send and receive functions

		// createComCalls returns the calls to insert and the computing call
		// to which communication calls must be synchronized (sender or receiver
		// call).
		ComCalls comCalls = createComCalls(container, vertex, sectionType,
				sourceFiles);

		if (comCalls.isNecessary()) {
			/*WorkflowLogger.getLogger().log(
					Level.INFO,
					"Relative computation call found for communication: "
							+ vertex.getName()
							+ ". Inserted communication call in section "
							+ sectionType);*/

			if (comCalls.getType().isSend()) {
				insertStartInOrder(comCalls, vertex);
				if(VertexType.isIntermediateSend(vertex)){
					insertEndInOrder(comCalls, vertex);
				}
				else{
					// Delaying end to improve communication parallelism
					insertEndSendBeforeSender(comCalls, vertex);
				}
			} else if (comCalls.getType().isReceive()) {
				if(VertexType.isIntermediateSend(vertex)){
					insertStartInOrder(comCalls, vertex);
				}
				else{
					// anticipating start to improve communication parallelism
					insertStartReceiveAfterReceiver(comCalls, vertex);
				}
				insertEndInOrder(comCalls, vertex);
			}
		} else {
			/*WorkflowLogger.getLogger().log(
					Level.WARNING,
					"No relative computation call found for communication: "
							+ vertex.getName()
							+ ". No inserted communication call in section "
							+ sectionType);*/
		}
	}

	/**
	 * Inserting calls to start communicating data when necessary based on scheduling
	 * order
	 */
	private void insertStartInOrder(ComCalls comCalls, SDFAbstractVertex vertex) {
		List<CommunicationFunctionCall> startComZoneCalls = comCalls
				.getStartComZoneCalls();

		insertInSchedulingOrder(startComZoneCalls, vertex);
	}

	/**
	 * Inserting calls to end communicating data when necessary based on scheduling
	 * order
	 */
	private void insertEndInOrder(ComCalls comCalls, SDFAbstractVertex vertex) {
		List<CommunicationFunctionCall> endComZoneCalls = comCalls.getEndComZoneCalls();

		insertInSchedulingOrder(endComZoneCalls, vertex);
	}

	/**
	 * Inserting calls to end sending data just before new actor firing
	 */
	private void insertEndSendBeforeSender(ComCalls comCalls, SDFAbstractVertex vertex) {
		ICodeElement nextElement = comCalls.getSenderCalls().get(0);

		// This adds automatically elements in order because they are inserted before an element
		for (CommunicationFunctionCall comCall : comCalls.getEndComZoneCalls()) {
			container.addCodeElementBefore(nextElement, comCall);
		}
	}
	
	/**
	 * Inserting calls to start receiving data just after new actor firing
	 */
	private void insertStartReceiveAfterReceiver(ComCalls comCalls, SDFAbstractVertex vertex) {
		ICodeElement previousElement = comCalls.getReceiverCalls().get(
				comCalls.getReceiverCalls().size() - 1);

		for (CommunicationFunctionCall comCall : comCalls
				.getStartComZoneCalls()) {
			container.addCodeElementAfter(previousElement, comCall);
			previousElement = comCall;
		}
	}

	private void insertInSchedulingOrder(
			List<CommunicationFunctionCall> comCalls, SDFAbstractVertex vertex) {
		int vertexSchedulingOrder = (Integer) vertex.getPropertyBean()
				.getValue(ImplementationPropertyNames.Vertex_schedulingOrder);

		if(container.getCodeElements().isEmpty()){
			for (CommunicationFunctionCall comCall : comCalls) {
				container.addCodeElement(comCall);
			}
		}
		else{
			ICodeElement previousElement = null;
	
			// Inserting the calls in total order. A send is always preceded by at
			// least an element
			for (ICodeElement element : container.getCodeElements()) {
				int currentSchedulingOrder = (Integer) element
						.getCorrespondingVertex()
						.getPropertyBean()
						.getValue(
								ImplementationPropertyNames.Vertex_schedulingOrder);
				if (vertexSchedulingOrder < currentSchedulingOrder) {
					break;
				} else {
					previousElement = element;
				}
			}
	
			for (CommunicationFunctionCall comCall : comCalls) {
				if(previousElement == null){
					container.addCodeElementFirst(comCall);
				}
				else{
					container.addCodeElementAfter(previousElement, comCall);
				}
				previousElement = comCall;
			}
		}
	}

	/**
	 * creates send calls
	 * 
	 * @return the code elements to which the communication must be synchronized
	 */
	@SuppressWarnings("unchecked")
	protected ComCalls createSendCalls(SDFAbstractVertex comVertex,
			AbstractBufferContainer parentContainer,
			CodeSectionType sectionType, SourceFileList sourceFiles) {

		ComCalls comCalls = new ComCalls(comVertex);

		// Retrieving the communication route step
		AbstractRouteStep rs = (AbstractRouteStep) comVertex.getPropertyBean()
				.getValue(ImplementationPropertyNames.SendReceive_routeStep);

		if (rs == null) {
			WorkflowLogger.getLogger()
					.log(Level.WARNING,
							"Route step is null for send vertex "
									+ comVertex.getName());
			return null;
		}

		DirectedNeighborIndex<SDFAbstractVertex, SDFEdge> neighborindex = new DirectedNeighborIndex<SDFAbstractVertex, SDFEdge>(
				comVertex.getBase());

		// Getting relative sender calls to synchronize with
		List<SDFAbstractVertex> predList = neighborindex
				.predecessorListOf(comVertex);
		SDFAbstractVertex senderVertex = (predList.get(0));

		// The target is the operator on which the corresponding
		// receive operation is mapped
		List<SDFAbstractVertex> succList = neighborindex
				.successorListOf(comVertex);
		SDFAbstractVertex receive = (succList.get(0));
		ComponentInstance target = (ComponentInstance) receive
				.getPropertyBean().getValue(
						ImplementationPropertyNames.Vertex_Operator);

		succList = neighborindex.successorListOf(comVertex);
		SDFAbstractVertex receiveVertex = (succList.get(0));

		succList = neighborindex.successorListOf(receiveVertex);
		ICodeGenSDFVertex receiverVertex = (ICodeGenSDFVertex) (succList.get(0));

		SourceFile receiverFile = sourceFiles.get(receiverVertex.getOperator());

		// Checking if the relative receive generated code
		// in the equivalent section of another source. Otherwise, no com is
		// necessary
		List<ICodeElement> relativeReceiverCode = receiverFile.getContainer(
				sectionType)
				.getCodeElements((SDFAbstractVertex) receiverVertex);

		comCalls.setSenderCalls(container.getCodeElements(senderVertex));
		comCalls.setReceiverCalls(relativeReceiverCode);

		Set<SDFEdge> inEdges = (comVertex.getBase().incomingEdgesOf(comVertex));
		List<Buffer> bufferSet = parentContainer.getBuffers(inEdges);

		// Case of one send per buffer
		for (Buffer buf : bufferSet) {
			List<Buffer> singleBufferSet = new ArrayList<Buffer>();
			singleBufferSet.add(buf);

			int comId = container.getComNumber();

			comCalls.addStartComZoneCall(new SendMsg(parentContainer,
					comVertex, singleBufferSet, rs, target, comId, Phase.START));
			comCalls.addEndComZoneCall(new SendMsg(parentContainer, comVertex,
					singleBufferSet, rs, target, comId, Phase.END));
			container.incrementComNumber();
		}

		return comCalls;
	}

	/**
	 * creates receive calls
	 * 
	 * @return the code elements to which the communication must be synchronized
	 */
	@SuppressWarnings("unchecked")
	protected ComCalls createReceiveCalls(SDFAbstractVertex comVertex,
			AbstractBufferContainer parentContainer,
			CodeSectionType sectionType, SourceFileList sourceFiles) {

		ComCalls comCalls = new ComCalls(comVertex);

		// Retrieving the communication route step
		AbstractRouteStep rs = (AbstractRouteStep) comVertex.getPropertyBean()
				.getValue(ImplementationPropertyNames.SendReceive_routeStep);

		if (rs == null) {
			WorkflowLogger.getLogger().log(
					Level.WARNING,
					"Route step is null for receive vertex "
							+ comVertex.getName());
			return null;
		}

		DirectedNeighborIndex<SDFAbstractVertex, SDFEdge> neighborindex = new DirectedNeighborIndex<SDFAbstractVertex, SDFEdge>(
				comVertex.getBase());

		List<SDFAbstractVertex> predList = neighborindex
				.predecessorListOf(comVertex);

		SDFAbstractVertex send = (predList.get(0));
		ICodeGenSDFVertex senderVertex = (ICodeGenSDFVertex) (neighborindex
				.predecessorListOf(send).get(0));

		SourceFile senderFile = sourceFiles.get(senderVertex.getOperator());

		// Checking if the relative receive generated code
		// in the equivalent section of another source. Otherwise, no com is
		// necessary
		List<ICodeElement> relativeSenderCode = senderFile.getContainer(
				sectionType).getCodeElements((SDFAbstractVertex) senderVertex);

		SDFAbstractVertex receiverVertex = (neighborindex
				.successorListOf(comVertex).get(0));

		comCalls.setSenderCalls(relativeSenderCode);
		comCalls.setReceiverCalls(container.getCodeElements(receiverVertex));

		Set<SDFEdge> outEdges = (comVertex.getBase().outgoingEdgesOf(comVertex));

		List<Buffer> bufferSet = parentContainer.getBuffers(outEdges);

		// The source is the operator on which the corresponding
		// send
		// operation is allocated
		ComponentInstance source = (ComponentInstance) send.getPropertyBean()
				.getValue(ImplementationPropertyNames.Vertex_Operator);

		// Case of one receive per buffer
		for (Buffer buf : bufferSet) {
			List<Buffer> singleBufferSet = new ArrayList<Buffer>();
			singleBufferSet.add(buf);

			int comId = container.getComNumber();

			comCalls.addStartComZoneCall(new ReceiveMsg(parentContainer,
					comVertex, singleBufferSet, rs, source, comId, Phase.START));
			comCalls.addEndComZoneCall(new ReceiveMsg(parentContainer,
					comVertex, singleBufferSet, rs, source, comId, Phase.END));
			container.incrementComNumber();
		}

		return comCalls;
	}

	/**
	 * creates send or receive calls depending on the vertex type
	 * 
	 * @return the code elements to which the communication must be synchronized
	 */
	protected ComCalls createComCalls(AbstractBufferContainer parentContainer,
			SDFAbstractVertex comVertex, CodeSectionType sectionType,
			SourceFileList sourceFiles) {

		// retrieving the vertex type
		VertexType type = (VertexType) comVertex.getPropertyBean().getValue(
				ImplementationPropertyNames.Vertex_vertexType);

		if (type != null) {

			// Creating send calls
			if (type.isSend()) {

				return createSendCalls(comVertex, parentContainer, sectionType,
						sourceFiles);

				// Creating receive calls
			} else if (type.isReceive()) {

				return createReceiveCalls(comVertex, parentContainer,
						sectionType, sourceFiles);
			}
		}

		return null;

	}

}
