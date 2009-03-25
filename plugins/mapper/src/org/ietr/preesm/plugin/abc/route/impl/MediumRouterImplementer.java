/**
 * 
 */
package org.ietr.preesm.plugin.abc.route.impl;

import java.util.logging.Level;

import org.ietr.preesm.core.architecture.route.AbstractRouteStep;
import org.ietr.preesm.core.architecture.route.MediumRouteStep;
import org.ietr.preesm.core.architecture.simplemodel.Medium;
import org.ietr.preesm.core.architecture.simplemodel.MediumDefinition;
import org.ietr.preesm.core.architecture.simplemodel.Operator;
import org.ietr.preesm.core.tools.PreesmLogger;
import org.ietr.preesm.plugin.abc.route.AbstractCommunicationRouter;
import org.ietr.preesm.plugin.abc.route.CommunicationRouter;
import org.ietr.preesm.plugin.abc.route.CommunicationRouterImplementer;
import org.ietr.preesm.plugin.abc.transaction.AddOverheadVertexTransaction;
import org.ietr.preesm.plugin.abc.transaction.AddSendReceiveTransaction;
import org.ietr.preesm.plugin.abc.transaction.AddTransferVertexTransaction;
import org.ietr.preesm.plugin.abc.transaction.Transaction;
import org.ietr.preesm.plugin.abc.transaction.TransactionManager;
import org.ietr.preesm.plugin.mapper.model.InitialEdgeProperty;
import org.ietr.preesm.plugin.mapper.model.MapperDAGEdge;
import org.ietr.preesm.plugin.mapper.model.MapperDAGVertex;
import org.ietr.preesm.plugin.mapper.model.impl.TransferVertex;
import org.sdf4j.model.dag.DAGEdge;

/**
 * Class responsible to generate the suited vertices while simulating a medium
 * communication
 * 
 * @author mpelcat
 */
public class MediumRouterImplementer extends CommunicationRouterImplementer {

	public MediumRouterImplementer(AbstractCommunicationRouter user) {
		super(user);
	}

	/**
	 * Adds the simulation vertices
	 */
	@Override
	public Transaction addVertices(AbstractRouteStep routeStep,
			MapperDAGEdge edge, TransactionManager transactions, int type,
			int routeStepIndex, Transaction lastTransaction) {

		if (routeStep instanceof MediumRouteStep) {
			if (type == CommunicationRouter.transferType) {
				long transferCost = evaluateSingleTransfer(edge, routeStep);
				MediumRouteStep mediumRouteStep = (MediumRouteStep) routeStep;

				Transaction transaction = new AddTransferVertexTransaction(
						lastTransaction, getEdgeScheduler(), edge,
						getImplementation(), getOrderManager(), routeStepIndex,
						mediumRouteStep, mediumRouteStep.getMedium(), transferCost, true);

				transactions.add(transaction);
				return transaction;
			} else if (type == CommunicationRouter.overheadType) {
				MapperDAGEdge firstTransferIncomingEdge = (MapperDAGEdge) getTransfer(
						(MapperDAGVertex) edge.getSource(),
						(MapperDAGVertex) edge.getTarget(), routeStepIndex)
						.incomingEdges().toArray()[0];

				if (firstTransferIncomingEdge != null) {
					transactions.add(new AddOverheadVertexTransaction(
							firstTransferIncomingEdge, getImplementation(),
							routeStep, getOrderManager()));
				} else {
					PreesmLogger
							.getLogger()
							.log(
									Level.SEVERE,
									"The transfer following vertex"
											+ edge.getSource()
											+ "was not found. We could not add overhead.");
				}
			} else if (type == CommunicationRouter.sendReceive) {

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

	/**
	 * Evaluates the transfer along a route step
	 */
	@Override
	protected long evaluateSingleTransfer(MapperDAGEdge edge,
			AbstractRouteStep step) {

		if (step instanceof MediumRouteStep) {
			Operator sender = step.getSender();
			Operator receiver = step.getReceiver();
			Medium medium = ((MediumRouteStep) step).getMedium();

			if (medium != null) {
				MediumDefinition def = (MediumDefinition) medium
						.getDefinition();
				InitialEdgeProperty edgeprop = edge.getInitialEdgeProperty();
				Integer datasize = edgeprop.getDataSize();

				Float time = datasize.floatValue() * def.getInvSpeed();

				return time.longValue();
			} else {

				PreesmLogger.getLogger().log(
						Level.SEVERE,
						"Data could not be correctly transfered from "
								+ sender.getName() + " to "
								+ receiver.getName());

				return 0;
			}
		}

		return 0;
	}

	private TransferVertex getTransfer(MapperDAGVertex source,
			MapperDAGVertex target, int routeStepIndex) {

		for (DAGEdge transferEdge : source.outgoingEdges()) {
			if (transferEdge.getTarget() instanceof TransferVertex) {
				TransferVertex v = (TransferVertex) transferEdge.getTarget();
				if (v.getTarget().equals(target)) {
					if (v.getRouteStepIndex() == routeStepIndex) {
						return v;
					} else {
						return getTransfer(v, target, routeStepIndex);
					}
				}
			}
		}
		return null;
	}

	@Override
	public void removeVertices(MapperDAGEdge edge,
			TransactionManager transactions) {

	}

}
