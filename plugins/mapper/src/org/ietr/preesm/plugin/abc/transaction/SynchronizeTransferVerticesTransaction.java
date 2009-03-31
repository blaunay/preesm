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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;

import org.ietr.preesm.core.architecture.ArchitectureComponent;
import org.ietr.preesm.core.architecture.route.AbstractRouteStep;
import org.ietr.preesm.core.architecture.route.MediumRouteStep;
import org.ietr.preesm.core.architecture.simplemodel.Medium;
import org.ietr.preesm.core.architecture.simplemodel.MediumDefinition;
import org.ietr.preesm.core.tools.PreesmLogger;
import org.ietr.preesm.plugin.abc.edgescheduling.IEdgeSched;
import org.ietr.preesm.plugin.abc.order.SchedOrderManager;
import org.ietr.preesm.plugin.abc.order.SchedulingOrderComparator;
import org.ietr.preesm.plugin.mapper.model.MapperDAG;
import org.ietr.preesm.plugin.mapper.model.MapperDAGEdge;
import org.ietr.preesm.plugin.mapper.model.MapperDAGVertex;
import org.ietr.preesm.plugin.mapper.model.impl.PrecedenceEdge;
import org.ietr.preesm.plugin.mapper.model.impl.PrecedenceEdgeAdder;
import org.ietr.preesm.plugin.mapper.model.impl.SynchroEdge;
import org.ietr.preesm.plugin.mapper.model.impl.TransferVertex;
import org.ietr.preesm.core.architecture.simplemodel.ContentionNode;
import org.sdf4j.model.dag.DAGEdge;
import org.sdf4j.model.dag.DAGVertex;

/**
 * A transaction that adds one transfer vertex per contention node in an
 * implementation and schedules them given the right edge scheduler
 * 
 * @author mpelcat
 */
public class SynchronizeTransferVerticesTransaction extends Transaction {
	// Inputs
	/**
	 * If not null, the transfer vertices need to be chained with formerly added
	 * ones
	 */
	private List<MapperDAGVertex> verticesToSynchronize = null;

	/**
	 * Scheduling the transfer vertices on the media
	 */
	protected IEdgeSched edgeScheduler = null;

	/**
	 * Implementation DAG
	 */
	private MapperDAG implementation = null;

	/**
	 * Vertices order manager
	 */
	private SchedOrderManager orderManager;

	public SynchronizeTransferVerticesTransaction(MapperDAG implementation,
			List<MapperDAGVertex> verticesToSynchronize,
			IEdgeSched edgeScheduler, SchedOrderManager orderManager) {
		super();
		this.verticesToSynchronize = verticesToSynchronize;
		this.edgeScheduler = edgeScheduler;
		this.implementation = implementation;
		this.orderManager = orderManager;

	}

	private MapperDAGVertex getLatestPredecessor(MapperDAGVertex input) {
		MapperDAGVertex latestPredecessor = null;
		Set<DAGEdge> edges = input.incomingEdges();

		for (DAGEdge edge : edges) {
			MapperDAGVertex v = (MapperDAGVertex) edge.getSource();
			if (latestPredecessor == null
					|| orderManager.totalIndexOf(v) > orderManager.totalIndexOf(latestPredecessor)) {
				latestPredecessor = v;
			}
		}

		return latestPredecessor;
	}

	@Override
	public void execute(List<Object> resultList) {
		super.execute(resultList);

		//MapperDAGVertex precedingVertex = getLatestPredecessor(verticesToSynchronize.get(0));
		
		//MapperDAGVertex tempVertex = precedingVertex;
		for (MapperDAGVertex v : verticesToSynchronize){
			v.getTimingVertexProperty().setSynchronizedVertices(verticesToSynchronize);
			//orderManager.insertVertexAfter(tempVertex, v);
			//tempVertex = v;
		}

		/*for (MapperDAGVertex v : verticesToSynchronize) {
			SynchroEdge synchroEdge = new SynchroEdge();
			synchroEdge.getTimingEdgeProperty().setCost(0);
			implementation.addEdge(precedingVertex, v, synchroEdge);
		}*/
	}

	@Override
	public String toString() {
		return ("SynchronizeTransfer()");
	}

}
