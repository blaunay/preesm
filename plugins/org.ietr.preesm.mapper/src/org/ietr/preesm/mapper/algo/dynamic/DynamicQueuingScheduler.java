/*********************************************************
Copyright or � or Copr. IETR/INSA: Matthieu Wipliez, Jonathan Piat,
Maxime Pelcat, Jean-Fran�ois Nezan, Micka�l Raulet

[mwipliez,jpiat,mpelcat,jnezan,mraulet]@insa-rennes.fr

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

package org.ietr.preesm.mapper.algo.dynamic;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.ietr.dftools.architecture.slam.ComponentInstance;
import org.ietr.dftools.workflow.WorkflowException;
import org.ietr.dftools.workflow.tools.WorkflowLogger;
import org.ietr.preesm.mapper.abc.IAbc;
import org.ietr.preesm.mapper.abc.order.VertexOrderList;
import org.ietr.preesm.mapper.model.MapperDAGVertex;
import org.ietr.preesm.mapper.tools.TopologicalDAGIterator;

/**
 * Scheduler that simulates a dynamic queuing system
 * 
 * @author mpelcat
 */
public class DynamicQueuingScheduler {

	/**
	 * The queue of vertices to map
	 */
	private VertexOrderList orderList;

	/**
	 * Parameters of the workflow
	 */
	private Map<String, String> textParameters;

	/**
	 * constructor
	 */
	public DynamicQueuingScheduler(VertexOrderList orderList,
			Map<String, String> textParameters) {
		super();
		this.orderList = orderList;
		this.textParameters = textParameters;
	}

	/**
	 * maps the vertices on the operator with lowest final cost (soonest
	 * available)
	 */
	public void mapVertices(IAbc abc) throws WorkflowException {

		// Type of order to use while mapping/scheduling
		String listType = textParameters.get("listType");

		if (listType.isEmpty()) {
			listType = "optimised";
		}

		if (listType.equalsIgnoreCase("optimised")) {

			for (VertexOrderList.OrderProperty vP : orderList.elements()) {
				MapperDAGVertex currentvertex = (MapperDAGVertex) abc.getDAG()
						.getVertex(vP.getName());

				mapOnBestOp(abc, currentvertex);

			}
		} else if (listType.equalsIgnoreCase("topological")) {
			TopologicalDAGIterator it = new TopologicalDAGIterator(abc.getDAG());

			while (it.hasNext()) {
				MapperDAGVertex v = (MapperDAGVertex) it.next();
				MapperDAGVertex currentvertex = (MapperDAGVertex) abc.getDAG()
						.getVertex(v.getName());

				mapOnBestOp(abc, currentvertex);

			}
		}
	}

	public void mapOnBestOp(IAbc abc, MapperDAGVertex currentvertex)
			throws WorkflowException {

		List<ComponentInstance> adequateOps = abc
				.getCandidateOperators(currentvertex, true);
		long currentMinCost = Long.MAX_VALUE;
		ComponentInstance currentMinOp = null;

		for (ComponentInstance op : adequateOps) {
			abc.updateFinalCosts();
			long newCost = abc.getFinalCost(op);
			if (newCost < currentMinCost) {
				currentMinCost = newCost;
				currentMinOp = op;
			}
		}

		// ------------------25/06/2012 - Ugly temp fix, remove it soon ----------
		// -----------------This fix will ensure that broadcast (and roundbuffers) are mapped
		// ------------------on the same component as their immediate predecessor (and successor)
		// 04/12/2012 - commented out until semantics of special vertices is precise
		
/*		// If the currentVertex is a broadcast
		if (currentvertex.getKind().equals("dag_broadcast_vertex") 
				&& !(currentvertex.getCorrespondingSDFVertex() instanceof SDFRoundBufferVertex)) {
			if (currentvertex.incomingEdges().size() > 1) {
				WorkflowLogger
						.getLogger()
						.log(Level.SEVERE,
								"Broadcast with several inputs: activate \"SuppressImplodeExplode\" in HSDF to solve this issue.");
			} else {
				// Get the unique incoming edge of broadcast
				DAGEdge inEdge = currentvertex.incomingEdges().iterator()
						.next();
				currentMinOp = abc
						.getEffectiveComponent((MapperDAGVertex) inEdge
								.getSource());

			}
		}
		
		// If current vertex has a (or several) roundbuffer(s) as predecessor(s),
		// map the round buffer on the same component
		for(DAGEdge inEdge: currentvertex.incomingEdges()){
			if(inEdge.getSource().getCorrespondingSDFVertex() instanceof SDFRoundBufferVertex){
				abc.map((MapperDAGVertex)inEdge.getSource(), currentMinOp, true);
			}
		}
		
		// do not map Roundbuffers yet. They will be mapped with their successors
		if (!(currentvertex.getCorrespondingSDFVertex() instanceof SDFRoundBufferVertex)) {*/
			// -----------------End of the temp fix first half-----------------------------------

			// Mapping on operator with minimal final cost
			if (currentMinOp != null) {
				abc.map(currentvertex, currentMinOp, true, false);
			} else {
				WorkflowLogger.getLogger().log(Level.SEVERE,
						"No available operator for " + currentvertex);
			}

			// ------------------Second half of temp fix----------

		/*}else{ // Curent vertex is a RoundBuffer
			// Do not map round buffer until their immediate sucessor is mapped	
			if(currentvertex.outgoingEdges().size()>1){
				WorkflowLogger
				.getLogger()
				.log(Level.SEVERE,
						"RoundBuffer with several outputs: activate \"SuppressImplodeExplode\" in HSDF to solve this issue \n or it will be mapped with only one of its sucessors");
			}
		}*/
		// ------------------end of Second half of temp fix----------
	}
}
