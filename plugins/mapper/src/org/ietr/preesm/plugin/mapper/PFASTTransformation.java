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


/**
 * 
 */
package org.ietr.preesm.plugin.mapper;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.ietr.preesm.core.architecture.ArchitectureComponentType;
import org.ietr.preesm.core.architecture.Examples;
import org.ietr.preesm.core.architecture.MultiCoreArchitecture;
import org.ietr.preesm.core.architecture.OperatorDefinition;
import org.ietr.preesm.core.log.PreesmLogger;
import org.ietr.preesm.core.scenario.IScenario;
import org.ietr.preesm.core.scenario.Scenario;
import org.ietr.preesm.core.scenario.Timing;
import org.ietr.preesm.core.scenario.TimingManager;
import org.ietr.preesm.core.task.TaskResult;
import org.ietr.preesm.core.task.TextParameters;
import org.ietr.preesm.core.workflow.sources.AlgorithmRetriever;
import org.ietr.preesm.plugin.abc.AbcType;
import org.ietr.preesm.plugin.abc.AbstractAbc;
import org.ietr.preesm.plugin.abc.IAbc;
import org.ietr.preesm.plugin.abc.infinitehomogeneous.InfiniteHomogeneousAbc;
import org.ietr.preesm.plugin.mapper.fastalgo.InitialLists;
import org.ietr.preesm.plugin.mapper.graphtransfo.SdfToDagConverter;
import org.ietr.preesm.plugin.mapper.graphtransfo.TagDAG;
import org.ietr.preesm.plugin.mapper.model.MapperDAG;
import org.ietr.preesm.plugin.mapper.pfastalgo.PFastAlgoParameters;
import org.ietr.preesm.plugin.mapper.pfastalgo.PFastAlgorithm;
import org.sdf4j.model.sdf.SDFGraph;

/**
 * Plug-in class for PFAST algorithm
 * 
 * @author mwipliez
 * @author pmenuet
 */
public class PFASTTransformation extends AbstractMapping {

	/**
	 * Main for test
	 */
	public static void main(String[] args) {
		// FASTTransformation transformation = new FASTTransformation();
		Logger logger = PreesmLogger.getLogger();
		logger.setLevel(Level.FINEST);

		// PreesmLogger.getLogger().setLevel(Level.FINER);

		// Generating archi
		MultiCoreArchitecture archi = Examples.get2C64Archi();

		// Generating random sdf dag
		int nbVertex = 20, minInDegree = 1, maxInDegree = 3, minOutDegree = 1, maxOutDegree = 3;
		SDFGraph graph = AlgorithmRetriever.randomDAG(nbVertex, minInDegree,
				maxInDegree, minOutDegree, maxOutDegree, 50,true);

		// Generating constraints
		IScenario scenario = new Scenario();

		TimingManager tmgr = scenario.getTimingManager();

		for (int i = 1; i <= nbVertex; i++) {
			String name = String.format("Vertex %d", i);
			Timing newt = new Timing((OperatorDefinition)archi.getComponentDefinition(ArchitectureComponentType.operator,"c64x"), graph
					.getVertex(name), 100);
			tmgr.addTiming(newt);
		}

		PFASTTransformation transformation = new PFASTTransformation();
		PFastAlgoParameters parameters = new PFastAlgoParameters(8, 20, 16, 5, 3,
				AbcType.LooselyTimed);
		transformation.transform(graph, archi, parameters.textParameters(), scenario);

		logger.log(Level.FINER, "Test fast finished");
	}

	/**
	 * 
	 */
	public PFASTTransformation() {
	}

	/**
	 * Function called while running the plugin
	 */
	@Override
	public TaskResult transform(SDFGraph algorithm, MultiCoreArchitecture architecture,
			TextParameters textParameters,
			IScenario scenario) {

		
		TaskResult result = new TaskResult();
		PFastAlgoParameters parameters;
		
		parameters = new PFastAlgoParameters(textParameters);

		MapperDAG dag = SdfToDagConverter.convert(algorithm,architecture,scenario, false);

		IAbc simu = new InfiniteHomogeneousAbc(
				dag, architecture);

		InitialLists initial = new InitialLists();

		if(!initial.constructInitialLists(dag, simu))
				return null;

		simu.resetDAG();

		IAbc simu2 = AbstractAbc
				.getInstance(parameters.getSimulatorType(), dag, architecture);

		PFastAlgorithm pfastAlgorithm = new PFastAlgorithm();

		dag = pfastAlgorithm.map(dag, architecture, parameters.getProcNumber(),
				parameters.getNodesmin(), initial, parameters.getMaxCount(),
				parameters.getMaxStep(), parameters.getMargIn(), parameters
						.getSimulatorType(), false, 0, null);

		simu2.setDAG(dag);

		//simu2.plotImplementation();

		TagDAG tagSDF = new TagDAG();

		tagSDF.tag(dag,architecture,simu2);

		result.setDAG(dag);

		return result;
	}

	@Override
	public void transform(SDFGraph algorithm, SDFGraph transformedAlgorithm) {
		// TODO Auto-generated method stub

	}


}
