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

package org.ietr.preesm.plugin.mapper;

import org.eclipse.core.runtime.IProgressMonitor;
import org.ietr.preesm.core.architecture.MultiCoreArchitecture;
import org.ietr.preesm.core.scenario.IScenario;
import org.ietr.preesm.core.task.PreesmException;
import org.ietr.preesm.core.task.TaskResult;
import org.ietr.preesm.core.task.TextParameters;
import org.ietr.preesm.plugin.abc.AbstractAbc;
import org.ietr.preesm.plugin.abc.IAbc;
import org.ietr.preesm.plugin.abc.impl.latency.InfiniteHomogeneousAbc;
import org.ietr.preesm.plugin.abc.taskscheduling.TopologicalTaskSched;
import org.ietr.preesm.plugin.mapper.algo.list.InitialLists;
import org.ietr.preesm.plugin.mapper.algo.pfast.PFastAlgorithm;
import org.ietr.preesm.plugin.mapper.graphtransfo.SdfToDagConverter;
import org.ietr.preesm.plugin.mapper.graphtransfo.TagDAG;
import org.ietr.preesm.plugin.mapper.model.MapperDAG;
import org.ietr.preesm.plugin.mapper.params.AbcParameters;
import org.ietr.preesm.plugin.mapper.params.PFastAlgoParameters;
import org.sdf4j.model.parameters.InvalidExpressionException;
import org.sdf4j.model.sdf.SDFGraph;

/**
 * Plug-in class for PFAST algorithm
 * 
 * @author mwipliez
 * @author pmenuet
 */
public class PFASTTransformation extends AbstractMapping {

	/**
	 * 
	 */
	public PFASTTransformation() {
	}

	/**
	 * Function called while running the plugin
	 */
	@Override
	public TaskResult transform(SDFGraph algorithm,
			MultiCoreArchitecture architecture, TextParameters textParameters,
			IScenario scenario, IProgressMonitor monitor)
			throws PreesmException {

		super.transform(algorithm, architecture, textParameters, scenario,
				monitor);
		TaskResult result = new TaskResult();

		PFastAlgoParameters pFastParams = new PFastAlgoParameters(textParameters);
		AbcParameters abcParameters = new AbcParameters(textParameters);

		MapperDAG dag = SdfToDagConverter.convert(algorithm, architecture,
				scenario, false);

		// calculates the DAG span length on the architecture main operator (the
		// tasks that can
		// not be executed by the main operator are deported without transfer
		// time to other operator
		calculateSpan(dag, architecture, scenario, abcParameters);

		IAbc simu = new InfiniteHomogeneousAbc(
				abcParameters, dag, architecture,
				abcParameters.getSimulatorType().getTaskSchedType(), scenario);

		InitialLists initial = new InitialLists();

		if (!initial.constructInitialLists(dag, simu))
			return null;

		TopologicalTaskSched taskSched = new TopologicalTaskSched(simu.getTotalOrder());
		simu.resetDAG();

		IAbc simu2 = AbstractAbc.getInstance(abcParameters, dag, architecture, scenario);

		PFastAlgorithm pfastAlgorithm = new PFastAlgorithm();

		dag = pfastAlgorithm.map(dag, architecture, scenario, initial, abcParameters, pFastParams, false, 0, pFastParams
						.isDisplaySolutions(), null, taskSched);

		simu2.setDAG(dag);

		// simu2.plotImplementation();

		// The transfers are reordered using the best found order during
		// scheduling
		simu2.reschedule(pfastAlgorithm.getBestTotalOrder());
		TagDAG tagSDF = new TagDAG();

		try {
			tagSDF.tag(dag, architecture, scenario, simu2, abcParameters
					.getEdgeSchedType());
		} catch (InvalidExpressionException e) {
			e.printStackTrace();
			throw (new PreesmException(e.getMessage()));
		}

		result.setDAG(dag);
		result.setAbc(simu2);

		super.clean(architecture, scenario);
		return result;
	}

	@Override
	public void transform(SDFGraph algorithm, SDFGraph transformedAlgorithm)
			throws PreesmException {
		// TODO Auto-generated method stub

	}

}
