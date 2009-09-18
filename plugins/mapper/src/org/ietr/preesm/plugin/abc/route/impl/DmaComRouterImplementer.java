package org.ietr.preesm.plugin.abc.route.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.ietr.preesm.core.architecture.route.AbstractRouteStep;
import org.ietr.preesm.core.architecture.route.DmaRouteStep;
import org.ietr.preesm.core.architecture.simplemodel.ContentionNode;
import org.ietr.preesm.core.architecture.simplemodel.Dma;
import org.ietr.preesm.core.architecture.simplemodel.DmaDefinition;
import org.ietr.preesm.core.tools.PreesmLogger;
import org.ietr.preesm.plugin.abc.edgescheduling.IEdgeSched;
import org.ietr.preesm.plugin.abc.edgescheduling.SimpleEdgeSched;
import org.ietr.preesm.plugin.abc.impl.ImplementationCleaner;
import org.ietr.preesm.plugin.abc.route.AbstractCommunicationRouter;
import org.ietr.preesm.plugin.abc.route.CommunicationRouter;
import org.ietr.preesm.plugin.abc.route.CommunicationRouterImplementer;
import org.ietr.preesm.plugin.abc.transaction.AddOverheadVertexTransaction;
import org.ietr.preesm.plugin.abc.transaction.AddSendReceiveTransaction;
import org.ietr.preesm.plugin.abc.transaction.AddTransferVertexTransaction;
import org.ietr.preesm.plugin.abc.transaction.SynchronizeTransferVerticesTransaction;
import org.ietr.preesm.plugin.abc.transaction.Transaction;
import org.ietr.preesm.plugin.abc.transaction.TransactionManager;
import org.ietr.preesm.plugin.mapper.model.MapperDAGEdge;
import org.ietr.preesm.plugin.mapper.model.MapperDAGVertex;
import org.ietr.preesm.plugin.mapper.model.impl.PrecedenceEdgeAdder;
import org.ietr.preesm.plugin.mapper.model.impl.TransferVertex;

/**
 * Class responsible to generate the suited vertices while simulating a dma
 * communication
 * 
 * @author mpelcat
 */
public class DmaComRouterImplementer extends CommunicationRouterImplementer {

	public DmaComRouterImplementer(AbstractCommunicationRouter user) {
		super(user);
	}

	@Override
	public void removeVertices(MapperDAGEdge edge,
			TransactionManager transactions) {

	}

	/**
	 * Careful!!! Only simple edge scheduler allowed for synchronized edges
	 */
	@Override
	public IEdgeSched getEdgeScheduler() {
		return new SimpleEdgeSched(getOrderManager());
	}

	/**
	 * Adds the simulation vertices for a given edge and a given route step
	 */
	@Override
	public Transaction addVertices(AbstractRouteStep routeStep,
			MapperDAGEdge edge, TransactionManager transactions, int type,
			int routeStepIndex, Transaction lastTransaction,
			List<Object> alreadyCreatedVertices) {

		if (routeStep instanceof DmaRouteStep) {
			// Adding the transfers
			DmaRouteStep dmaStep = ((DmaRouteStep) routeStep);

			// Adding the transfers of a dma route step
			if (type == CommunicationRouter.transferType) {
				// All the transfers along the path have the same time: the time
				// to transfer the data on the slowest contention node
				long transferTime = dmaStep.getWorstTransferTime(edge
						.getInitialEdgeProperty().getDataSize());
				List<ContentionNode> nodes = dmaStep.getContentionNodes();
				AddTransferVertexTransaction transaction = null;

				for (ContentionNode node : nodes) {
					int nodeIndex = nodes.indexOf(node);
					transaction = new AddTransferVertexTransaction("transfer",
							lastTransaction, getEdgeScheduler(), edge,
							getImplementation(), getOrderManager(),
							routeStepIndex, nodeIndex, routeStep, transferTime,
							node, true);
					transactions.add(transaction);
				}

				return transaction;
			} else if (type == CommunicationRouter.overheadType) {
				// Adding the overhead
				MapperDAGEdge incomingEdge = null;

				for (Object o : alreadyCreatedVertices) {
					if (o instanceof TransferVertex) {
						TransferVertex v = (TransferVertex) o;
						if (v.getSource().equals(edge.getSource())
								&& v.getTarget().equals(edge.getTarget())
								&& v.getRouteStep() == routeStep && v.getNodeIndex() == 0) {
								// Finding the edge where to add an overhead
								incomingEdge = (MapperDAGEdge) v
										.incomingEdges().toArray()[0];
						}

					}
				}

				DmaDefinition dmaDef = (DmaDefinition) ((Dma) dmaStep.getDma())
						.getDefinition();
				long overheadTime = dmaDef.getSetupTime(dmaStep.getSender());
				if (incomingEdge != null) {
					transactions.add(new AddOverheadVertexTransaction(
							incomingEdge, getImplementation(), routeStep,
							overheadTime, getOrderManager()));
				} else {
					PreesmLogger
							.getLogger()
							.log(
									Level.FINE,
									"The transfer following vertex"
											+ edge.getSource()
											+ "was not found. We could not add overhead.");
				}

			} else if (type == CommunicationRouter.synchroType) {

				// Synchronizing the previously created transfers
				List<MapperDAGVertex> toSynchronize = new ArrayList<MapperDAGVertex>();

				for (Object o : alreadyCreatedVertices) {
					if (o instanceof TransferVertex) {
						TransferVertex v = (TransferVertex) o;
						if (v.getSource().equals(edge.getSource())
								&& v.getTarget().equals(edge.getTarget())
								&& v.getRouteStep() == routeStep) {
							toSynchronize.add(v);
						}

					}
				}

				if (toSynchronize.size() > 1) {
					ImplementationCleaner cleaner = new ImplementationCleaner(getOrderManager(),getImplementation());
					PrecedenceEdgeAdder adder = new PrecedenceEdgeAdder(getOrderManager(),getImplementation());
					for(MapperDAGVertex v : toSynchronize){
						cleaner.unscheduleVertex(v);
						adder.scheduleVertex(v);
					}
					transactions
							.add(new SynchronizeTransferVerticesTransaction(
									getImplementation(), toSynchronize, getOrderManager()));
				}
			} else if (type == CommunicationRouter.sendReceiveType) {

				Transaction transaction = new AddSendReceiveTransaction(
						lastTransaction, edge, getImplementation(),
						getOrderManager(), routeStepIndex, routeStep,
						TransferVertex.SEND_RECEIVE_COST);

				transactions.add(transaction);
				return transaction;
			}
		}
		return null;
	}

}
