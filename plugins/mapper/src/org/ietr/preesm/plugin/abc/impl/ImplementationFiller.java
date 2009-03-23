/*********************************************************
Copyright or � or Copr. IETR/INSA: Matthieu Wipliez, Jonathan Piat,
Maxime Pelcat, Peng Cheng Mu, Jean-Fran�ois Nezan, Micka�l Raulet

[mwipliez,jpiat,mpelcat,pmu,jnezan,mraulet]@insa-rennes.fr

This software is a computer program whose purpose is to prototype
parallel applications.

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
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
knowledge of the CeCILL-C license and that you accept its terms.
 *********************************************************/

package org.ietr.preesm.plugin.abc.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

import org.ietr.preesm.core.architecture.route.AbstractRouteStep;
import org.ietr.preesm.core.architecture.route.Route;
import org.ietr.preesm.core.architecture.route.MediumRouteStep;
import org.ietr.preesm.core.architecture.simplemodel.Operator;
import org.ietr.preesm.core.tools.PreesmLogger;
import org.ietr.preesm.plugin.abc.edgescheduling.IEdgeSched;
import org.ietr.preesm.plugin.abc.order.SchedOrderManager;
import org.ietr.preesm.plugin.abc.route.RouteCalculator;
import org.ietr.preesm.plugin.abc.transaction.AddOverheadVertexTransaction;
import org.ietr.preesm.plugin.abc.transaction.AddSendReceiveTransaction;
import org.ietr.preesm.plugin.abc.transaction.AddTransferVertexTransaction;
import org.ietr.preesm.plugin.abc.transaction.RemoveEdgeTransaction;
import org.ietr.preesm.plugin.abc.transaction.RemoveVertexTransaction;
import org.ietr.preesm.plugin.abc.transaction.Transaction;
import org.ietr.preesm.plugin.abc.transaction.TransactionManager;
import org.ietr.preesm.plugin.mapper.model.ImplementationVertexProperty;
import org.ietr.preesm.plugin.mapper.model.MapperDAG;
import org.ietr.preesm.plugin.mapper.model.MapperDAGEdge;
import org.ietr.preesm.plugin.mapper.model.MapperDAGVertex;
import org.ietr.preesm.plugin.mapper.model.impl.OverheadVertex;
import org.ietr.preesm.plugin.mapper.model.impl.PrecedenceEdge;
import org.ietr.preesm.plugin.mapper.model.impl.TransferVertex;
import org.sdf4j.model.dag.DAGEdge;
import org.sdf4j.model.dag.DAGVertex;

/**
 * The TransferVertexAdder creates the vertices allowing edge scheduling
 * 
 * @author mpelcat
 */
public class ImplementationFiller {

	/**
	 * True if we take into account the transfer overheads
	 */
	private boolean handleOverheads;

	private RouteCalculator router;

	private SchedOrderManager orderManager;

	/**
	 * True if we the edge that will go through transfers replaces the original
	 * edge. False if both paths are kept
	 */
	private boolean rmvOrigEdge;

	/**
	 * Scheduling the transfer vertices on the media
	 */
	protected IEdgeSched edgeScheduler;

	public ImplementationFiller(IEdgeSched edgeScheduler,
			RouteCalculator router, SchedOrderManager orderManager, boolean rmvOrigEdge, boolean handleOverheads) {
		super();
		this.edgeScheduler = edgeScheduler;
		this.router = router;
		this.orderManager = orderManager;
		this.rmvOrigEdge = rmvOrigEdge;
		this.handleOverheads = handleOverheads;
	}

	/**
	 * Adds all necessary transfer vertices for one given vertex
	 */
	public void addAndScheduleTransferVertices(MapperDAG implementation, MapperDAGVertex refVertex) {

		addNewVertexTransfers(implementation, refVertex);
		if (handleOverheads) {
			addNewVertexOverheads(implementation, refVertex);
		}
	}
	
	public void addNewVertexTransfers(MapperDAG implementation, MapperDAGVertex newVertex){
		TransactionManager localTransactionManager = new TransactionManager();

		Set<DAGEdge> edges = new HashSet<DAGEdge>();
		if(newVertex.incomingEdges()!= null)
			edges.addAll(newVertex.incomingEdges());
		if(newVertex.outgoingEdges()!= null)
			edges.addAll(newVertex.outgoingEdges());

		for (DAGEdge edge : edges) {

			if (!(edge instanceof PrecedenceEdge)) {
				ImplementationVertexProperty currentSourceProp = ((MapperDAGVertex) edge
						.getSource()).getImplementationVertexProperty();
				ImplementationVertexProperty currentDestProp = ((MapperDAGVertex) edge
						.getTarget()).getImplementationVertexProperty();

				if (currentSourceProp.hasEffectiveOperator()
						&& currentDestProp.hasEffectiveOperator()) {
					if (!currentSourceProp.getEffectiveOperator().equals(currentDestProp
							.getEffectiveOperator())) {
						// Adds several transfers for one edge depending on the
						// route steps
						addTransferVertices(
								(MapperDAGEdge) edge, implementation,
								localTransactionManager, null, true);
					}
				}
			}
		}
		
		localTransactionManager.execute();
	}
	
	
	public void addNewVertexOverheads(MapperDAG implementation, MapperDAGVertex newVertex){
		TransactionManager localTransactionManager = new TransactionManager();

		Set<DAGVertex> transfers = ImplementationTools.getAllTransfers(newVertex);

		for (DAGVertex tvertex : transfers) {
			for(DAGEdge incomingEdge : implementation.incomingEdgesOf(tvertex)){
				if(!(incomingEdge instanceof PrecedenceEdge)){
					localTransactionManager.add(new AddOverheadVertexTransaction((MapperDAGEdge)incomingEdge,implementation, ((TransferVertex)tvertex).getRouteStep(), getOrderManager()));
				}
			}
		}

		
		localTransactionManager.execute();
	}

	/**
	 * Adds all necessary transfer vertices for the whole implementation
	 */
	public void addAndScheduleAllSendReceiveVertices(MapperDAG implementation, boolean scheduleThem) {
		TransactionManager localTransactionManager = new TransactionManager();

		// We iterate the edges and process the ones with different allocations
		Iterator<DAGEdge> iterator = implementation.edgeSet().iterator();

		while (iterator.hasNext()) {
			MapperDAGEdge currentEdge = (MapperDAGEdge) iterator.next();

			if (!(currentEdge instanceof PrecedenceEdge)) {
				ImplementationVertexProperty currentSourceProp = ((MapperDAGVertex) currentEdge
						.getSource()).getImplementationVertexProperty();
				ImplementationVertexProperty currentDestProp = ((MapperDAGVertex) currentEdge
						.getTarget()).getImplementationVertexProperty();

				if (currentSourceProp.hasEffectiveOperator()
						&& currentDestProp.hasEffectiveOperator()) {
					if (currentSourceProp.getEffectiveOperator() != currentDestProp
							.getEffectiveOperator()) {
						// Adds several transfers for one edge depending on the
						// route steps
						addSendReceiveVertices(currentEdge, implementation,
								localTransactionManager, null, scheduleThem);
					}
				}
			}
		}

		localTransactionManager.execute();
	}

	/**
	 * Adds one transfer vertex per route step. It does not remove the original
	 * edge
	 */
	public void addSendReceiveVertices(MapperDAGEdge edge,
			MapperDAG implementation, TransactionManager transactionManager,
			MapperDAGVertex refVertex, boolean scheduleVertex) {

		MapperDAGVertex currentSource = (MapperDAGVertex) edge.getSource();
		MapperDAGVertex currentDest = (MapperDAGVertex) edge.getTarget();

		Operator sourceOp = currentSource.getImplementationVertexProperty()
				.getEffectiveOperator();
		Operator destOp = currentDest.getImplementationVertexProperty()
				.getEffectiveOperator();

		Route route = router.getRoute(sourceOp, destOp);

		if (route == null) {
			PreesmLogger.getLogger().log(
					Level.SEVERE,
					"No route was found between the cores: " + sourceOp
							+ " and " + destOp);
			return;
		}
		Iterator<AbstractRouteStep> it = route.iterator();
		int i = 1;
		// Transactions need to be linked so that the communication vertices
		// created are also linked
		Transaction precedingTransaction = null;

		while (it.hasNext()) {
			MediumRouteStep step = (MediumRouteStep)it.next();

			Transaction transaction = null;

			// TODO: set a size to send and receive. From medium definition?
			transaction = new AddSendReceiveTransaction(
					precedingTransaction, edge, implementation,
					orderManager, i, step,
					TransferVertex.SEND_RECEIVE_COST, scheduleVertex);

			transactionManager.add(transaction);
			precedingTransaction = transaction;

			if (rmvOrigEdge) {
				transactionManager.add(new RemoveEdgeTransaction(edge,
						implementation));
			}

			i++;
		}
	}

	/**
	 * Adds one transfer vertex per route step. It does not remove the original
	 * edge
	 */
	public void addTransferVertices(MapperDAGEdge edge,
			MapperDAG implementation, TransactionManager transactionManager,
			MapperDAGVertex refVertex, boolean scheduleVertex) {

		MapperDAGVertex currentSource = (MapperDAGVertex) edge.getSource();
		MapperDAGVertex currentDest = (MapperDAGVertex) edge.getTarget();

		Operator sourceOp = currentSource.getImplementationVertexProperty()
				.getEffectiveOperator();
		Operator destOp = currentDest.getImplementationVertexProperty()
				.getEffectiveOperator();

		Route route = router.getRoute(sourceOp, destOp);

		if (route == null) {
			PreesmLogger.getLogger().log(
					Level.SEVERE,
					"No route was found between the cores: " + sourceOp
							+ " and " + destOp);
			return;
		}
		Iterator<AbstractRouteStep> it = route.iterator();
		int i = 1;
		// Transactions need to be linked so that the communication vertices
		// created are also linked
		Transaction precedingTransaction = null;

		while (it.hasNext()) {
			MediumRouteStep step = (MediumRouteStep)it.next();

			Transaction transaction = null;

			long transferCost = router.evaluateTransfer(edge, step
					.getSender(), step.getReceiver());

			transaction = new AddTransferVertexTransaction(
					precedingTransaction, edgeScheduler, edge,
					implementation, orderManager,i, step, transferCost,
					scheduleVertex);

			transactionManager.add(transaction);
			precedingTransaction = transaction;

			if (rmvOrigEdge) {
				transactionManager.add(new RemoveEdgeTransaction(edge,
						implementation));
			}

			i++;
		}
	}

	public SchedOrderManager getOrderManager() {
		return orderManager;
	}
}
