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

package org.ietr.preesm.plugin.mapper;

import org.eclipse.core.runtime.IProgressMonitor;
import org.ietr.preesm.core.architecture.MultiCoreArchitecture;
import org.ietr.preesm.core.scenario.IScenario;
import org.ietr.preesm.core.task.IMapping;
import org.ietr.preesm.core.task.PreesmException;
import org.ietr.preesm.core.task.TaskResult;
import org.ietr.preesm.core.task.TextParameters;
import org.ietr.preesm.plugin.abc.edgescheduling.EdgeSchedType;
import org.ietr.preesm.plugin.abc.impl.latency.SpanLengthCalculator;
import org.ietr.preesm.plugin.abc.route.calcul.RouteCalculator;
import org.ietr.preesm.plugin.abc.taskscheduling.TaskSchedType;
import org.ietr.preesm.plugin.mapper.model.MapperDAG;
import org.ietr.preesm.plugin.mapper.params.AbstractParameters;
import org.sdf4j.model.sdf.SDFGraph;

/**
 * Generic class representing the scheduling algorithm behaviour
 * 
 * @author pmenuet
 * @author mpelcat
 */
public abstract class AbstractMapping implements IMapping {
	
	@Override
	public abstract void transform(SDFGraph algorithm, SDFGraph transformedAlgorithm) throws PreesmException;

	
	@Override
	public TaskResult transform(SDFGraph algorithm, MultiCoreArchitecture architecture,
			TextParameters textParameters,
			IScenario scenario, IProgressMonitor monitor)  throws PreesmException{
		
		// Asking to recalculate routes
		RouteCalculator.recalculate(architecture,scenario);
		return null;
	}
	
	protected void clean(MultiCoreArchitecture architecture,IScenario scenario) throws PreesmException{
		// Asking to delete route
		RouteCalculator.deleteRoutes(architecture,scenario);
	}
	
	/**
	 * Calculates the DAG span length on the architecture main operator 
	 * (the tasks that cannot be executed by the main operator are deported 
	 * without transfer time to other operator)
	 */
	protected void calculateSpan(MapperDAG dag,
			MultiCoreArchitecture archi, IScenario scenario, AbstractParameters parameters){
		
		SpanLengthCalculator spanCalc = new SpanLengthCalculator(parameters.getEdgeSchedType(),
				dag, archi, parameters.getSimulatorType().getTaskSchedType(), scenario);
		spanCalc.resetDAG();

	}
}
