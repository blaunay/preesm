package org.ietr.preesm.plugin.abc.route.impl;

import java.util.logging.Level;

import org.ietr.preesm.core.architecture.route.AbstractRouteStep;
import org.ietr.preesm.core.architecture.route.MediumRouteStep;
import org.ietr.preesm.core.tools.PreesmLogger;
import org.ietr.preesm.plugin.abc.route.AbstractCommunicationRouter;
import org.ietr.preesm.plugin.abc.route.CommunicationRouter;
import org.ietr.preesm.plugin.abc.route.CommunicationRouterImplementer;
import org.ietr.preesm.plugin.abc.transaction.AddOverheadVertexTransaction;
import org.ietr.preesm.plugin.abc.transaction.AddSendReceiveTransaction;
import org.ietr.preesm.plugin.abc.transaction.AddTransferVertexTransaction;
import org.ietr.preesm.plugin.abc.transaction.Transaction;
import org.ietr.preesm.plugin.abc.transaction.TransactionManager;
import org.ietr.preesm.plugin.mapper.model.MapperDAGEdge;
import org.ietr.preesm.plugin.mapper.model.MapperDAGVertex;
import org.ietr.preesm.plugin.mapper.model.impl.TransferVertex;

public class DmaComRouterImplementer extends CommunicationRouterImplementer{


	public DmaComRouterImplementer(AbstractCommunicationRouter user) {
		super(user);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void removeVertices(MapperDAGEdge edge,
			TransactionManager transactions) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Transaction addVertices(AbstractRouteStep routeStep,
			MapperDAGEdge edge, TransactionManager transactions, int type,
			int routeStepIndex, Transaction lastTransaction) {

		if (type == CommunicationRouter.sendReceive) {

			// TODO: set a size to send and receive. From medium definition?
			Transaction transaction = new AddSendReceiveTransaction(
					lastTransaction, edge, getImplementation(),
					getOrderManager(), routeStepIndex, routeStep,
					TransferVertex.SEND_RECEIVE_COST);

			transactions.add(transaction);
			return transaction;
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
