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

import org.ietr.preesm.core.architecture.MultiCoreArchitecture;
import org.ietr.preesm.core.architecture.simplemodel.Operator;
import org.ietr.preesm.core.tools.PreesmLogger;
import org.ietr.preesm.plugin.abc.AbcType;
import org.ietr.preesm.plugin.abc.AbstractAbc;
import org.ietr.preesm.plugin.abc.CommunicationRouter;
import org.ietr.preesm.plugin.abc.TaskSwitcher;
import org.ietr.preesm.plugin.mapper.edgescheduling.EdgeSchedType;
import org.ietr.preesm.plugin.mapper.model.ImplementationVertexProperty;
import org.ietr.preesm.plugin.mapper.model.MapperDAG;
import org.ietr.preesm.plugin.mapper.model.MapperDAGEdge;
import org.ietr.preesm.plugin.mapper.model.MapperDAGVertex;
import org.ietr.preesm.plugin.mapper.model.impl.PrecedenceEdgeAdder;

/**
 * A loosely timed architecture simulator associates a simple cost to
 * each communication. This cost is the transfer size multiplied by the
 * medium speed. The communications are parallel with computation and
 * all parallel with each other.
 *         
 * @author mpelcat   
 */
public class LooselyTimedAbc extends
		AbstractAbc {

	/**
	 * simulator of the transfers
	 */
	protected CommunicationRouter router;

	/**
	 * Current precedence edge adder: called exclusively by simulator to schedule
	 * vertices on the different operators
	 */
	protected PrecedenceEdgeAdder precedenceEdgeAdder;
	
	/**
	 * Constructor of the simulator from a "blank" implementation where every
	 * vertex has not been implanted yet.
	 */
	public LooselyTimedAbc(EdgeSchedType edgeSchedType, MapperDAG dag, MultiCoreArchitecture archi, AbcType abcType) {
		super(dag, archi, abcType);

		// The media simulator calculates the edges costs
		router = new CommunicationRouter(archi);
		precedenceEdgeAdder = new PrecedenceEdgeAdder(orderManager);
	}

	@Override
	protected void fireNewMappedVertex(MapperDAGVertex vertex, boolean updateRank) {

		Operator effectiveOp = vertex.getImplementationVertexProperty()
				.getEffectiveOperator();

		if (effectiveOp == Operator.NO_COMPONENT) {
			PreesmLogger.getLogger().severe(
					"implementation of " + vertex.getName() + " failed");
		} else {

			if (updateRank) {
				if (this.abcType.isSwitchTask()) {
					TaskSwitcher taskSwitcher = new TaskSwitcher(
							orderManager, vertex);
					taskSwitcher.insertVertex();
				} else {
					orderManager.addLast(vertex);
				}
			} else {
				orderManager.insertVertexInTotalOrder(vertex);
			}
			
			long vertextime = vertex.getInitialVertexProperty().getTime(
					effectiveOp);
			
			precedenceEdgeAdder.scheduleNewVertex(implementation,transactionManager,vertex,vertex);
			//precedenceEdgeAdder.checkPrecedences(implementation, archi, null);
			
			// Set costs
			vertex.getTimingVertexProperty().setCost(vertextime);

			setEdgesCosts(vertex.incomingEdges());
			setEdgesCosts(vertex.outgoingEdges());
		}
	}

	@Override
	protected void fireNewUnmappedVertex(MapperDAGVertex vertex) {

		// unimplanting a vertex resets the cost of the current vertex
		// and its edges
		
		vertex.getTimingVertexProperty().resetCost();

		resetCost(vertex.incomingEdges());
		resetCost(vertex.outgoingEdges());

		//precedenceEdgeAdder.checkPrecedences(implementation, archi, null);

		transactionManager.undoTransactions(vertex);

		//precedenceEdgeAdder.checkPrecedences(implementation, archi, vertex);
	}

	/**
	 * Asks the time keeper to update timings. Crucial and costly operation.
	 * Depending on the king of timings we want, calls the necessary updates.
	 */
	@Override
	protected final void updateTimings() {

		timeKeeper.updateTLevels();
	}

	/**
	 * In the loosely timed ABC, the edges receive the communication times.
	 */
	protected final void setEdgeCost(MapperDAGEdge edge) {

		ImplementationVertexProperty sourceimp = ((MapperDAGVertex)edge.getSource())
				.getImplementationVertexProperty();
		ImplementationVertexProperty destimp = ((MapperDAGVertex)edge.getTarget())
				.getImplementationVertexProperty();

		Operator sourceOp = sourceimp.getEffectiveOperator();
		Operator destOp = destimp.getEffectiveOperator();

		if (sourceOp != Operator.NO_COMPONENT
				&& destOp != Operator.NO_COMPONENT) {
			if (sourceOp.equals(destOp)) {
				edge.getTimingEdgeProperty().setCost(0);
			} else {

				// The transfer evaluation takes into account the route

				edge.getTimingEdgeProperty().setCost(
						router.evaluateTransfer(edge, sourceOp, destOp));
			}
		}

	}
	
	public EdgeSchedType getEdgeSchedType() {
		return null;
	}
}
