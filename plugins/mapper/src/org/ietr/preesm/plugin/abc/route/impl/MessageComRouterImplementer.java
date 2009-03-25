package org.ietr.preesm.plugin.abc.route.impl;

import org.ietr.preesm.core.architecture.route.AbstractRouteStep;
import org.ietr.preesm.core.architecture.route.DmaRouteStep;
import org.ietr.preesm.core.architecture.route.NodeRouteStep;
import org.ietr.preesm.core.architecture.simplemodel.AbstractNode;
import org.ietr.preesm.core.architecture.simplemodel.ContentionNode;
import org.ietr.preesm.plugin.abc.route.AbstractCommunicationRouter;
import org.ietr.preesm.plugin.abc.route.CommunicationRouter;
import org.ietr.preesm.plugin.abc.route.CommunicationRouterImplementer;
import org.ietr.preesm.plugin.abc.transaction.AddSendReceiveTransaction;
import org.ietr.preesm.plugin.abc.transaction.AddTransferVertexTransaction;
import org.ietr.preesm.plugin.abc.transaction.Transaction;
import org.ietr.preesm.plugin.abc.transaction.TransactionManager;
import org.ietr.preesm.plugin.mapper.model.MapperDAGEdge;
import org.ietr.preesm.plugin.mapper.model.impl.TransferVertex;

/**
 * Class responsible to generate the suited vertices while simulating a message
 * communication
 * 
 * @author mpelcat
 */
public class MessageComRouterImplementer extends CommunicationRouterImplementer {

	public MessageComRouterImplementer(AbstractCommunicationRouter user) {
		super(user);
	}

	@Override
	public void removeVertices(MapperDAGEdge edge,
			TransactionManager transactions) {

	}

	/**
	 * Adds the simulation vertices
	 */
	@Override
	public Transaction addVertices(AbstractRouteStep routeStep,
			MapperDAGEdge edge, TransactionManager transactions, int type,
			int routeStepIndex, Transaction lastTransaction) {

		if (routeStep instanceof DmaRouteStep) {
			NodeRouteStep nodeRouteStep = (NodeRouteStep) routeStep;
			if (type == CommunicationRouter.transferType) {
				long transferCost = 100;
				Transaction transaction = null;

				for (AbstractNode node : nodeRouteStep.getNodes()) {
					if (node instanceof ContentionNode) {
						transaction = new AddTransferVertexTransaction(
								lastTransaction, getEdgeScheduler(), edge,
								getImplementation(), getOrderManager(),
								routeStepIndex, nodeRouteStep, node,
								transferCost, true);

						transactions.add(transaction);
					}
				}

				return transaction;
			} else if (type == CommunicationRouter.overheadType) {

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

	@Override
	protected long evaluateSingleTransfer(MapperDAGEdge edge,
			AbstractRouteStep step) {
		// TODO Auto-generated method stub
		return 0;
	}

}
