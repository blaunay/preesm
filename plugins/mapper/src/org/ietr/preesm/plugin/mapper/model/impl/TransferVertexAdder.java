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

package org.ietr.preesm.plugin.mapper.model.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.ietr.preesm.core.architecture.Route;
import org.ietr.preesm.core.architecture.RouteStep;
import org.ietr.preesm.plugin.abc.CommunicationRouter;
import org.ietr.preesm.plugin.abc.order.SchedOrderManager;
import org.ietr.preesm.plugin.abc.transaction.AddNewVertexTransfersTransaction;
import org.ietr.preesm.plugin.abc.transaction.AddSendReceiveTransaction;
import org.ietr.preesm.plugin.abc.transaction.AddTransferVertexTransaction;
import org.ietr.preesm.plugin.abc.transaction.RemoveEdgeTransaction;
import org.ietr.preesm.plugin.abc.transaction.RemoveVertexTransaction;
import org.ietr.preesm.plugin.abc.transaction.Transaction;
import org.ietr.preesm.plugin.abc.transaction.TransactionManager;
import org.ietr.preesm.plugin.mapper.edgescheduling.IEdgeSched;
import org.ietr.preesm.plugin.mapper.model.ImplementationVertexProperty;
import org.ietr.preesm.plugin.mapper.model.MapperDAG;
import org.ietr.preesm.plugin.mapper.model.MapperDAGEdge;
import org.ietr.preesm.plugin.mapper.model.MapperDAGVertex;
import org.sdf4j.model.dag.DAGEdge;
import org.sdf4j.model.dag.DAGVertex;

/**
 * The TransferVertexAdder creates the vertices allowing edge scheduling
 * 
 * @author mpelcat
 */
public class TransferVertexAdder {

	private CommunicationRouter router;

	private SchedOrderManager orderManager;

	/**
	 * True if we want to add a send and a receive operation, false if a simple
	 * transfer vertex should be added
	 */
	private boolean sendReceive;

	/**
	 * True if we the edge that will go through transfers replaces the original
	 * edge. False if both paths are kept
	 */
	private boolean rmvOrigEdge;

	/**
	 * Scheduling the transfer vertices on the media
	 */
	protected IEdgeSched edgeScheduler;

	public TransferVertexAdder(IEdgeSched edgeScheduler,
			CommunicationRouter router, SchedOrderManager orderManager,
			boolean sendReceive, boolean rmvOrigEdge) {
		super();
		this.edgeScheduler = edgeScheduler;
		this.router = router;
		this.orderManager = orderManager;
		this.sendReceive = sendReceive;
		this.rmvOrigEdge = rmvOrigEdge;
	}

	/**
	 * Adds all necessary transfer vertices
	 */
	public void addAndScheduleTransferVertices(MapperDAG implementation,
			TransactionManager transactionManager, MapperDAGVertex refVertex) {

		transactionManager.add(
				new AddNewVertexTransfersTransaction(edgeScheduler, router,
						orderManager, implementation, refVertex), refVertex);
		transactionManager.execute();
	}

	/**
	 * Adds all necessary transfer vertices
	 */
	public void addTransferVertices(MapperDAG implementation,
			TransactionManager transactionManager) {

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
						addTransferVertices(currentEdge, implementation,
								transactionManager, null);
					}
				}
			}
		}

		transactionManager.execute();
	}

	/**
	 * Adds one transfer vertex per route step. It does not remove the original
	 * edge
	 */
	public void addTransferVertices(MapperDAGEdge edge,
			MapperDAG implementation, TransactionManager transactionManager,
			MapperDAGVertex refVertex) {

		MapperDAGVertex currentSource = (MapperDAGVertex) edge.getSource();
		MapperDAGVertex currentDest = (MapperDAGVertex) edge.getTarget();

		Route route = router.getRoute(currentSource
				.getImplementationVertexProperty().getEffectiveOperator(),
				currentDest.getImplementationVertexProperty()
						.getEffectiveOperator());

		Iterator<RouteStep> it = route.iterator();
		int i = 1;

		while (it.hasNext()) {
			RouteStep step = it.next();

			Transaction transaction = null;

			if (sendReceive) {
				// TODO: set a size to send and receive. From medium definition?
				transaction = new AddSendReceiveTransaction(edge,
						implementation, orderManager, i, step, 100);
			} else {

				int transferCost = router.evaluateTransfer(edge, step
						.getSender(), step.getReceiver());

				transaction = new AddTransferVertexTransaction(edgeScheduler,
						edge, implementation, orderManager, i, step, transferCost);
			}

			transactionManager.add(transaction, refVertex);

			if (rmvOrigEdge) {
				transactionManager.add(new RemoveEdgeTransaction(edge,
						implementation), refVertex);
			}

			i++;
		}
	}

	/**
	 * Removes all transfers from routes coming from or going to vertex
	 */
	public void removeAllTransfers(MapperDAGVertex vertex,
			MapperDAG implementation, TransactionManager transactionManager) {

		for (DAGVertex v : getAllTransfers(vertex, implementation,
				transactionManager)) {
			if (v instanceof TransferVertex) {
				transactionManager.add(new RemoveVertexTransaction(
						(MapperDAGVertex) v, implementation, orderManager),
						null);

			}
		}

		transactionManager.execute();
	}

	/**
	 * Gets all transfers from routes coming from or going to vertex. Do not
	 * execute if overheads are present
	 */
	public static Set<DAGVertex> getAllTransfers(MapperDAGVertex vertex,
			MapperDAG implementation, TransactionManager transactionManager) {

		Set<DAGVertex> transfers = new HashSet<DAGVertex>();

		transfers.addAll(getPrecedingTransfers(vertex, implementation,
				transactionManager));
		transfers.addAll(getFollowingTransfers(vertex, implementation,
				transactionManager));

		return transfers;
	}

	/**
	 * Gets all transfers preceding vertex. Recursive function
	 */
	public static Set<DAGVertex> getPrecedingTransfers(MapperDAGVertex vertex,
			MapperDAG implementation, TransactionManager transactionManager) {

		Set<DAGVertex> transfers = new HashSet<DAGVertex>();

		for (DAGEdge edge : vertex.incomingEdges()) {
			if (!(edge instanceof PrecedenceEdge)) {
				MapperDAGVertex v = (MapperDAGVertex) edge.getSource();
				if (v instanceof TransferVertex) {
					transfers.add(v);
					transfers.addAll(getPrecedingTransfers(v, implementation,
							transactionManager));
				}
			}
		}

		return transfers;
	}

	/**
	 * Gets all transfers following vertex. Recursive function
	 */
	public static Set<DAGVertex> getFollowingTransfers(MapperDAGVertex vertex,
			MapperDAG implementation, TransactionManager transactionManager) {

		Set<DAGVertex> transfers = new HashSet<DAGVertex>();

		for (DAGEdge edge : vertex.outgoingEdges()) {
			if (!(edge instanceof PrecedenceEdge)) {
				MapperDAGVertex v = (MapperDAGVertex) edge.getTarget();
				if (v instanceof TransferVertex) {
					transfers.add(v);
					transfers.addAll(getFollowingTransfers(v, implementation,
							transactionManager));
				}
			}
		}

		return transfers;
	}
}
