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

package org.ietr.preesm.mapper.model.property;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.ietr.dftools.architecture.slam.ComponentInstance;
import org.ietr.dftools.workflow.tools.WorkflowLogger;
import org.ietr.preesm.core.architecture.util.DesignTools;
import org.ietr.preesm.mapper.model.MapperDAG;
import org.ietr.preesm.mapper.model.MapperDAGVertex;

/**
 * Properties of a mapped vertex. Can be shared by multiple vertices that have
 * the same relative constraints
 * 
 * @author mpelcat
 */
public class VertexMapping extends GroupProperty {

	public VertexMapping() {
		super();
	}

	@Override
	public VertexMapping clone() {

		VertexMapping property = (VertexMapping) super.clone();
		return property;
	}

	/**
	 * Returns a list of components, computed from initial and relative
	 * constraints. If the boolean considerGroupMapping is true, one mapped
	 * vertex in the group causes the return of its effective operator.
	 */
	public List<ComponentInstance> getCandidateComponents(
			MapperDAGVertex vertex, boolean considerGroupMapping) {

		List<ComponentInstance> operators = new ArrayList<ComponentInstance>();
		MapperDAG dag = (MapperDAG) vertex.getBase();

		// Gets all vertices corresponding to the relative constraint group
		List<MapperDAGVertex> relatedVertices = getVertices(dag);

		if (relatedVertices.size() < 1) {
			WorkflowLogger.getLogger().log(Level.SEVERE,
					"Relative constraint with no vertex.");

			return operators;
		}

		MapperDAGVertex firstVertex = relatedVertices.get(0);
		
		// Adding to the list all candidate components of the first vertex
		operators.addAll(firstVertex.getInit().getInitialOperatorList());

		// computing intersection with other initial operator lists
		for (MapperDAGVertex locVertex : relatedVertices) {
			DesignTools.retainAll(operators, locVertex.getInit()
					.getInitialOperatorList());
			
		}
		
		// If we consider group mapping and a vertex in the group is mapped, we keep its operator only
		if(considerGroupMapping){
			for (MapperDAGVertex locVertex : relatedVertices) {
				ComponentInstance op = locVertex.getEffectiveComponent();
				if(op != null){
					Set<ComponentInstance> effectiveOp = new HashSet<ComponentInstance>();
					effectiveOp.add(op);
					operators.retainAll(effectiveOp);
				}
			}
		}

		if (operators.isEmpty()) {
			WorkflowLogger.getLogger().log(Level.SEVERE,
					"Relative constraint with no operator." + relatedVertices);
		}

		return operators;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "<" + super.toString() + ">";
	}

}
