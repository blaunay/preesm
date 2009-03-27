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

import org.ietr.preesm.core.architecture.ArchitectureComponent;
import org.ietr.preesm.core.architecture.MultiCoreArchitecture;
import org.ietr.preesm.core.scenario.IScenario;
import org.ietr.preesm.plugin.abc.AbcType;
import org.ietr.preesm.plugin.abc.AbstractAbc;
import org.ietr.preesm.plugin.abc.edgescheduling.EdgeSchedType;
import org.ietr.preesm.plugin.mapper.model.MapperDAG;
import org.ietr.preesm.plugin.mapper.model.MapperDAGEdge;
import org.ietr.preesm.plugin.mapper.model.MapperDAGVertex;
import org.ietr.preesm.plugin.mapper.plot.IImplementationPlotter;

/**
 * Prepared for communication contentious list scheduling ABC
 * 
 * @author pmu
 */
public class CommContenAbc extends AbstractAbc {

	public CommContenAbc(EdgeSchedType edgeSchedType, MapperDAG dag, MultiCoreArchitecture archi, AbcType abcType, IScenario scenario) {
		super(dag, archi, abcType, scenario);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void fireNewMappedVertex(MapperDAGVertex vertex, boolean updateRank) {
		// TODO Called whenever a vertex is implanteds
		// Call here your edge scheduling and specific vertices adder (via
		// transactions)

	}

	@Override
	protected void fireNewUnmappedVertex(MapperDAGVertex vertex) {
		// TODO Called whenever a vertex is unimplanted

	}

	@Override
	protected void setEdgeCost(MapperDAGEdge edge) {
		// TODO Called from your own fireNewMappedVertex when necessary to set
		// the cost of
		// edges during simulation

	}

	public EdgeSchedType getEdgeSchedType() {
		return null;
	}

	@Override
	public long getFinalCost() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getFinalCost(MapperDAGVertex vertex) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getFinalCost(ArchitectureComponent component) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IImplementationPlotter plotImplementation(boolean delegateDisplay) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDAG(MapperDAG dag) {
		// TODO Auto-generated method stub
		
	}
}
