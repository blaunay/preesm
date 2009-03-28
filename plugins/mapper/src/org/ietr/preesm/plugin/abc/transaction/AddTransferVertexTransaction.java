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

package org.ietr.preesm.plugin.abc.transaction;

import java.util.List;
import java.util.logging.Level;

import org.ietr.preesm.core.architecture.ArchitectureComponent;
import org.ietr.preesm.core.architecture.route.AbstractRouteStep;
import org.ietr.preesm.core.architecture.route.MediumRouteStep;
import org.ietr.preesm.core.architecture.simplemodel.Medium;
import org.ietr.preesm.core.architecture.simplemodel.MediumDefinition;
import org.ietr.preesm.core.tools.PreesmLogger;
import org.ietr.preesm.plugin.abc.edgescheduling.IEdgeSched;
import org.ietr.preesm.plugin.abc.order.SchedOrderManager;
import org.ietr.preesm.plugin.mapper.model.MapperDAG;
import org.ietr.preesm.plugin.mapper.model.MapperDAGEdge;
import org.ietr.preesm.plugin.mapper.model.MapperDAGVertex;
import org.ietr.preesm.plugin.mapper.model.impl.PrecedenceEdge;
import org.ietr.preesm.plugin.mapper.model.impl.PrecedenceEdgeAdder;
import org.ietr.preesm.plugin.mapper.model.impl.TransferVertex;

/**
 * A transaction that adds one transfer vertex in an implementation and
 * schedules it given the right edge scheduler
 * 
 * @author mpelcat
 */
public class AddTransferVertexTransaction extends Transaction {
	// Inputs
	/**
	 * If not null, the transfer vertices need to be chained with formerly added
	 * ones
	 */
	private Transaction precedingTransaction = null;
	/**
	 * Scheduling the transfer vertices on the media
	 */
	protected IEdgeSched edgeScheduler = null;

	/**
	 * Vertices order manager
	 */
	protected SchedOrderManager orderManager;

	/**
	 * Implementation DAG to which the vertex is added
	 */
	private MapperDAG implementation = null;

	/**
	 * Route step corresponding to this transfer
	 */
	AbstractRouteStep step = null;

	/**
	 * time of this transfer
	 */
	long transferTime = 0;

	/**
	 * Component corresponding to this transfer vertex
	 */
	private ArchitectureComponent effectiveComponent = null;

	/**
	 * Original edge corresponding to this overhead
	 */
	private MapperDAGEdge edge = null;

	/**
	 * Index of the route step within its route
	 */
	private int routeIndex = 0;

	/**
	 * Index of the node its route step
	 */
	private int nodeIndex = 0;

	// Generated objects
	/**
	 * overhead vertex added
	 */
	private TransferVertex tVertex = null;

	/**
	 * true if the added vertex needs to be scheduled
	 */
	private boolean scheduleVertex = false;

	/**
	 * edges added
	 */
	private MapperDAGEdge newInEdge = null;
	private MapperDAGEdge newOutEdge = null;

	public AddTransferVertexTransaction(Transaction precedingTransaction,
			IEdgeSched edgeScheduler, MapperDAGEdge edge,
			MapperDAG implementation, SchedOrderManager orderManager,
			int routeIndex, int nodeIndex, AbstractRouteStep step, long transferTime,
			ArchitectureComponent effectiveComponent, boolean scheduleVertex) {
		super();
		this.precedingTransaction = precedingTransaction;
		this.edgeScheduler = edgeScheduler;
		this.edge = edge;
		this.implementation = implementation;
		this.step = step;
		this.effectiveComponent = effectiveComponent;
		this.orderManager = orderManager;
		this.scheduleVertex = scheduleVertex;
		this.routeIndex = routeIndex;
		this.transferTime = transferTime;
		this.nodeIndex = nodeIndex;
		
	}

	@Override
	public void execute(List<Object> resultList) {
		super.execute(resultList);

		MapperDAGVertex currentSource = null;
		MapperDAGVertex currentTarget = (MapperDAGVertex) edge.getTarget();

		if (precedingTransaction != null
				&& precedingTransaction instanceof AddTransferVertexTransaction) {
			currentSource = ((AddTransferVertexTransaction) precedingTransaction).tVertex;
			currentSource.getBase()
					.removeAllEdges(currentSource, currentTarget);
		} else {
			currentSource = (MapperDAGVertex) edge.getSource();
		}

		String tvertexID = "__transfer" + routeIndex + "_" + nodeIndex + " ("
				+ ((MapperDAGVertex) edge.getSource()).getName() + ","
				+ currentTarget.getName() + ")";

		if (edge instanceof PrecedenceEdge) {
			PreesmLogger.getLogger().log(Level.INFO,
					"no transfer vertex corresponding to a schedule edge");
			return;
		}

		if (transferTime > 0) {
			tVertex = new TransferVertex(tvertexID, implementation,
					(MapperDAGVertex) edge.getSource(), (MapperDAGVertex) edge
							.getTarget(), routeIndex, nodeIndex);

			tVertex.setRouteStep(step);

			tVertex.getTimingVertexProperty().setCost(transferTime);

			tVertex.getImplementationVertexProperty().setEffectiveComponent(
					effectiveComponent);

			edgeScheduler.schedule(tVertex, currentSource, currentTarget);

			implementation.addVertex(tVertex);

			newInEdge = (MapperDAGEdge) implementation.addEdge(currentSource,
					tVertex);
			newOutEdge = (MapperDAGEdge) implementation.addEdge(tVertex,
					currentTarget);

			newInEdge.setInitialEdgeProperty(edge.getInitialEdgeProperty()
					.clone());
			newOutEdge.setInitialEdgeProperty(edge.getInitialEdgeProperty()
					.clone());

			newInEdge.getTimingEdgeProperty().setCost(0);
			newOutEdge.getTimingEdgeProperty().setCost(0);

			newInEdge.setAggregate(edge.getAggregate());
			newOutEdge.setAggregate(edge.getAggregate());

			if (scheduleVertex) {
				// Scheduling transfer vertex
				PrecedenceEdgeAdder precEdgeAdder = new PrecedenceEdgeAdder(
						orderManager);
				precEdgeAdder.scheduleVertex(implementation, tVertex);
			}
			
			if(resultList != null){
				resultList.add(tVertex);
			}
		}
	}

	@Override
	public String toString() {
		return ("AddTransfer(" + tVertex.toString() + ")");
	}

}
