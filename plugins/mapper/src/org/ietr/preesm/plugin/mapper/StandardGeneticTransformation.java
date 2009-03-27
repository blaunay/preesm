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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.ietr.preesm.core.architecture.ArchitectureComponentType;
import org.ietr.preesm.core.architecture.Examples;
import org.ietr.preesm.core.architecture.MultiCoreArchitecture;
import org.ietr.preesm.core.architecture.simplemodel.OperatorDefinition;
import org.ietr.preesm.core.scenario.IScenario;
import org.ietr.preesm.core.scenario.Scenario;
import org.ietr.preesm.core.scenario.Timing;
import org.ietr.preesm.core.scenario.TimingManager;
import org.ietr.preesm.core.task.TaskResult;
import org.ietr.preesm.core.task.TextParameters;
import org.ietr.preesm.core.tools.PreesmLogger;
import org.ietr.preesm.core.workflow.sources.AlgorithmRetriever;
import org.ietr.preesm.plugin.abc.AbcType;
import org.ietr.preesm.plugin.abc.AbstractAbc;
import org.ietr.preesm.plugin.abc.IAbc;
import org.ietr.preesm.plugin.abc.edgescheduling.EdgeSchedType;
import org.ietr.preesm.plugin.abc.impl.latency.InfiniteHomogeneousAbc;
import org.ietr.preesm.plugin.mapper.algo.fast.FastPopulation;
import org.ietr.preesm.plugin.mapper.algo.genetic.Chromosome;
import org.ietr.preesm.plugin.mapper.algo.genetic.StandardGeneticAlgorithm;
import org.ietr.preesm.plugin.mapper.algo.list.InitialLists;
import org.ietr.preesm.plugin.mapper.algo.pfast.PFastAlgorithm;
import org.ietr.preesm.plugin.mapper.graphtransfo.SdfToDagConverter;
import org.ietr.preesm.plugin.mapper.graphtransfo.TagDAG;
import org.ietr.preesm.plugin.mapper.model.MapperDAG;
import org.ietr.preesm.plugin.mapper.params.FastAlgoParameters;
import org.ietr.preesm.plugin.mapper.params.GeneticAlgoParameters;
import org.ietr.preesm.plugin.mapper.params.PFastAlgoParameters;
import org.sdf4j.model.sdf.SDFGraph;

/**
 * Plug-in class for genetic algorithm
 * 
 * @author pmenuet
 */
public class StandardGeneticTransformation extends AbstractMapping {

	/**
	 * 
	 */
	public StandardGeneticTransformation() {
	}

	/**
	 * Function called while running the plugin
	 */
	@Override
	public TaskResult transform(SDFGraph algorithm, MultiCoreArchitecture architecture,
			TextParameters textParameters,
			IScenario scenario, IProgressMonitor monitor) {

		super.transform(algorithm,architecture,textParameters,scenario,monitor);
		TaskResult transfoResult = new TaskResult();
		GeneticAlgoParameters parameters;

		parameters = new GeneticAlgoParameters(textParameters);

		MapperDAG dag = SdfToDagConverter.convert(algorithm,architecture,scenario, false);

		IAbc simu = new InfiniteHomogeneousAbc(parameters.getEdgeSchedType(), 
				dag, architecture, parameters.getSimulatorType().getTaskSchedType(), scenario);

		InitialLists initial = new InitialLists();

		if(!initial.constructInitialLists(dag, simu))
				return null;

		simu.resetDAG();

		List<MapperDAG> populationDAG = new ArrayList<MapperDAG>();

		if (parameters.isPfastused2makepopulation()) {
			PFastAlgorithm pfastAlgorithm = new PFastAlgorithm();

			PFastAlgoParameters parameter = new PFastAlgoParameters(8, 20, 16, true, 5,
					3, parameters.getSimulatorType(), parameters.getEdgeSchedType());

			dag = pfastAlgorithm.map(dag, architecture, scenario, parameter
					.getProcNumber(), parameter.getNodesmin(), initial,
					parameter.getMaxCount(), parameter.getMaxStep(), parameter
							.getMargIn(), parameters.getSimulatorType(),parameters.getEdgeSchedType(), true,
					parameters.getPopulationSize(), true, populationDAG);

		} else {

			FastPopulation population = new FastPopulation(parameters
					.getPopulationSize(), populationDAG, parameters
					.getSimulatorType(),parameters.getEdgeSchedType(), architecture);

			FastAlgoParameters parameter = new FastAlgoParameters(50, 50, 8, false, 
					parameters.getSimulatorType(), parameters.getEdgeSchedType());

			population.constructPopulation(dag, parameter.getMaxCount(),
					parameter.getMaxStep(), parameter.getMargIn(), scenario);
		}

		IAbc simu2 = AbstractAbc
				.getInstance(parameters.getSimulatorType(), parameters.getEdgeSchedType(), dag, architecture, scenario);

		StandardGeneticAlgorithm geneticAlgorithm = new StandardGeneticAlgorithm();

		ConcurrentSkipListSet<Chromosome> result;
		result = geneticAlgorithm.runGeneticAlgo("test", populationDAG,
				architecture, scenario, parameters.getSimulatorType(),parameters.getEdgeSchedType(), parameters
						.getPopulationSize(), parameters.getGenerationNumber(),
				false);

		Chromosome chromosome = result.first().clone();

		chromosome.evaluate(parameters.getSimulatorType(), parameters.getEdgeSchedType());

		dag = chromosome.getDag();

		simu2.setDAG(dag);

		//simu2.plotImplementation();

		TagDAG tagSDF = new TagDAG();

		tagSDF.tag(dag,architecture,scenario,simu2, parameters.getEdgeSchedType());

		transfoResult.setDAG(dag);
		transfoResult.setAbc(simu2);

		super.clean(architecture,scenario);
		return transfoResult;
	}

	@Override
	public void transform(SDFGraph algorithm, SDFGraph transformedAlgorithm) {
		// TODO Auto-generated method stub
	}


}
