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

import java.util.logging.Level;

import org.ietr.preesm.core.architecture.RouteStep;
import org.ietr.preesm.core.architecture.simplemodel.MediumDefinition;
import org.ietr.preesm.core.log.PreesmLogger;
import org.ietr.preesm.plugin.abc.order.SchedulingOrderManager;
import org.ietr.preesm.plugin.mapper.model.MapperDAG;
import org.ietr.preesm.plugin.mapper.model.MapperDAGEdge;
import org.ietr.preesm.plugin.mapper.model.MapperDAGVertex;
import org.ietr.preesm.plugin.mapper.model.impl.OverheadVertex;
import org.ietr.preesm.plugin.mapper.model.impl.PrecedenceEdge;

/**
 * Transaction executing the addition of an overhead (or set-up) vertex.
 * 
 * @author mpelcat
 */
public class AddOverheadVertexTransaction extends Transaction {
	
	// Inputs
	/**
	 * Implementation DAG to which the vertex is added
	 */
	private MapperDAG implementation = null;

	/**
	 * Route step corresponding to this overhead
	 */
	private RouteStep step = null;

	/**
	 * Original edge corresponding to this overhead
	 */
	private MapperDAGEdge edge = null;

	/**
	 * manager keeping scheduling orders
	 */
	private SchedulingOrderManager orderManager = null;
	
	// Generated objects
	/**
	 * overhead vertex added
	 */
	private OverheadVertex oVertex = null;
	
	/**
	 * edges added
	 */
	private MapperDAGEdge newInEdge = null;
	private MapperDAGEdge newOutEdge = null;
	
	
	public AddOverheadVertexTransaction(MapperDAGEdge edge,
			MapperDAG implementation, RouteStep step,SchedulingOrderManager orderManager) {
		super();
		this.edge = edge;
		this.implementation = implementation;
		this.step = step;
		this.orderManager = orderManager;
	}

	@Override
	public void execute() {

		super.execute();
		
		MapperDAGVertex currentSource = (MapperDAGVertex)edge.getSource();
		MapperDAGVertex currentTarget = (MapperDAGVertex)edge.getTarget();

		MediumDefinition mediumDef = (MediumDefinition) step.getMedium()
				.getDefinition();

		if (edge instanceof PrecedenceEdge) {
			PreesmLogger.getLogger().log(Level.INFO,
					"no overhead vertex corresponding to a schedule edge");
			return;
		}

		String overtexID = "__overhead (" + currentSource.getName() + ","
				+ currentTarget.getName() + ")";

		if (mediumDef.getInvSpeed() != 0) {
			oVertex = new OverheadVertex(overtexID, implementation);

			oVertex.getTimingVertexProperty().setCost(
					mediumDef.getOverhead());

			oVertex.getImplementationVertexProperty().setEffectiveOperator(
					step.getSender());

			orderManager.insertVertexAfter(currentSource, oVertex);

			implementation.addVertex(oVertex);
			
			newInEdge = (MapperDAGEdge)implementation.addEdge(currentSource, oVertex);
			newOutEdge = (MapperDAGEdge)implementation.addEdge(oVertex, currentTarget);

			newInEdge.setInitialEdgeProperty(edge.getInitialEdgeProperty().clone());
			newOutEdge.setInitialEdgeProperty(edge.getInitialEdgeProperty().clone());
			
			newInEdge.getTimingEdgeProperty().setCost(0);
			newOutEdge.getTimingEdgeProperty().setCost(0);
		}
		
	}

	@Override
	public void undo() {
		super.undo();
		
		implementation.removeEdge(newInEdge);
		implementation.removeEdge(newOutEdge);
		implementation.removeVertex(oVertex);
		orderManager.remove(oVertex, true);
	}

}
