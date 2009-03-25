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

package org.ietr.preesm.plugin.mapper.model.impl;

import org.ietr.preesm.core.architecture.route.AbstractRouteStep;
import org.ietr.preesm.plugin.mapper.model.MapperDAG;
import org.ietr.preesm.plugin.mapper.model.MapperDAGVertex;
import org.sdf4j.model.dag.DAGEdge;

/**
 * A transfer vertex represents a route step
 * 
 * @author mpelcat
 */
public class TransferVertex extends MapperDAGVertex {

	public static final long SEND_RECEIVE_COST = 100;
	
	private AbstractRouteStep step;
	
	/**
	 * Source and target of the vertex that originated this transfer
	 */
	private MapperDAGVertex source; 
	private MapperDAGVertex target;
	
	private int routeStepIndex;

	public TransferVertex(String id, MapperDAG base, MapperDAGVertex source, MapperDAGVertex target, int routeStepIndex) {
		super(id, base);
		this.source = source;
		this.target = target;
		this.routeStepIndex = routeStepIndex; 
	}

	public AbstractRouteStep getRouteStep() {
		return step;
	}

	public void setRouteStep(AbstractRouteStep step) {
		this.step = step;
	}

	/**
	 * A transfer vertex follows only one vertex
	 */
	public OverheadVertex getPrecedingOverhead() {
		for(DAGEdge incomingEdge : getBase().incomingEdgesOf(this)){
			if(!(incomingEdge instanceof PrecedenceEdge)){
				MapperDAGVertex precV = (MapperDAGVertex)incomingEdge.getSource();
				if(precV instanceof OverheadVertex)
				return (OverheadVertex)precV;
			}
		}
		
		return null;
	}

	public MapperDAGVertex getSource() {
		return source;
	}

	public MapperDAGVertex getTarget() {
		return target;
	}

	public int getRouteStepIndex() {
		return routeStepIndex;
	}
}
