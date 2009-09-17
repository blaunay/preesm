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

package org.ietr.preesm.plugin.abc.order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.ietr.preesm.core.architecture.ArchitectureComponent;
import org.ietr.preesm.plugin.mapper.model.ImplementationVertexProperty;
import org.ietr.preesm.plugin.mapper.model.MapperDAGVertex;
import org.ietr.preesm.plugin.mapper.model.TimingVertexProperty;
import org.sdf4j.model.dag.DAGEdge;

/**
 * A group of vertices that have the same total order and the same T-Level
 * 
 * @author mpelcat
 */
public class SynchronizedVertices implements IScheduleElement {

	private List<MapperDAGVertex> vertices = null;

	public SynchronizedVertices() {
		super();
		this.vertices = new ArrayList<MapperDAGVertex>();
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public TimingVertexProperty getTimingVertexProperty() {
		if (!vertices.isEmpty()) {
			return vertices.get(0).getTimingVertexProperty();
		}
		return null;
	}

	@Override
	public ImplementationVertexProperty getImplementationVertexProperty() {
		if (!vertices.isEmpty()) {
			return vertices.get(0).getImplementationVertexProperty();
		}
		return null;
	}

	public List<MapperDAGVertex> getVertices() {
		return Collections.unmodifiableList(vertices);
	}

	public MapperDAGVertex getVertex(ArchitectureComponent cmp) {
		for (MapperDAGVertex v : vertices) {
			if (v.getImplementationVertexProperty().getEffectiveComponent()
					.equals(cmp)) {
				return v;
			}
		}
		return null;
	}

	public void remove(MapperDAGVertex v) {
		vertices.remove(v);
	}

	public void add(MapperDAGVertex v) {
		if (!vertices.contains(v)) {
			vertices.add(v);
		}
	}

	@Override
	public Set<DAGEdge> incomingEdges() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
