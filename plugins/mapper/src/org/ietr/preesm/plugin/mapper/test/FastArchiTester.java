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

package org.ietr.preesm.plugin.mapper.test;

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
import org.ietr.preesm.core.workflow.sources.AlgorithmRetriever;
import org.ietr.preesm.plugin.abc.AbcType;
import org.ietr.preesm.plugin.abc.AbstractAbc;
import org.ietr.preesm.plugin.abc.IAbc;
import org.ietr.preesm.plugin.abc.impl.InfiniteHomogeneousAbc;
import org.ietr.preesm.plugin.mapper.fastalgo.FastAlgorithm;
import org.ietr.preesm.plugin.mapper.fastalgo.InitialLists;
import org.ietr.preesm.plugin.mapper.graphtransfo.DAGCreator;
import org.ietr.preesm.plugin.mapper.graphtransfo.SdfToDagConverter;
import org.ietr.preesm.plugin.mapper.model.MapperDAG;
import org.sdf4j.model.sdf.SDFGraph;

/**
 * Tester for the FAST algorithm
 * 
 * @author pmenuet
 */
public class FastArchiTester {

	/**
	 * FastArchiTester Main for tests: example of mapping from random archi
	 */
	public static void main(String[] args) {

		Logger logger = PreesmLogger.getLogger();
		logger.setLevel(Level.FINE);

		FastArchiTester tester = new FastArchiTester();
		DAGCreator dagCreator = new DAGCreator();

		// Generating random sdf dag
		int nbVertex = 100, minInDegree = 1, maxInDegree = 3, minOutDegree = 1, maxOutDegree = 3;
		SDFGraph graph = AlgorithmRetriever.randomDAG(nbVertex, minInDegree,
				maxInDegree, minOutDegree, maxOutDegree, 1500,true);

		// Generating archi
		MultiCoreArchitecture archi = Examples.get2C64Archi();

		// Generating constraints
		IScenario scenario = new Scenario();
		TimingManager tmgr = scenario.getTimingManager();
		for (int i = 1; i <= nbVertex; i++) {
			String name = String.format("Vertex %d", i);
			Double taskSize = Math.random() * 1000 + 500;
			Timing newt = new Timing((OperatorDefinition)archi.getComponentDefinition(ArchitectureComponentType.operator,"c64x"), graph
					.getVertex(name), taskSize.intValue());
			tmgr.addTiming(newt);
		}

		// Converting sdf dag in mapper dag
		MapperDAG dag = SdfToDagConverter.convert(graph, archi, scenario,false);

		// Looselytimed
		// choixsimu=0;

		// Approximatelytimed
		// choixsimu = 1;

		// Accuratelytimed
		// choixsimu=2;

		// different architectures with the same simulator
		int choixsimu = 1;
		tester.testArchi(dag, Examples.get2C64Archi(), choixsimu);
		//tester.testArchi(dag, Examples.get3C64Archi(), choixsimu);
		//tester.testArchi(dag, Examples.get4C64Archi(), choixsimu);
		//tester.testArchi(dag, Examples.get4C64_6edmaArchi(), choixsimu);
		//tester.testArchi(dag, Examples.get2FaradayArchi(), choixsimu);

		// different simulators with the same architecture
		//tester.testArchi(dag, Examples.get2FaradayArchi(), 0);
		//tester.testArchi(dag, Examples.get2FaradayArchi(), 1);
		//tester.testArchi(dag, Examples.get2FaradayArchi(), 2);

		logger.log(Level.FINE, "Test demo finished");

	}

	/**
	 * Test of a SDFDAG on architecture archi
	 * 
	 * @param dag
	 * @param archi
	 * @param choixsimu
	 */
	public void testArchi(MapperDAG dag, MultiCoreArchitecture archi,
			int choixsimu) {

		Logger logger = PreesmLogger.getLogger();
		logger.setLevel(Level.FINE);

		IAbc simu = new InfiniteHomogeneousAbc(
				dag, archi);
		logger.log(Level.FINEST, "Initialization list scheduling");

		InitialLists initial = new InitialLists();

		logger.log(Level.FINEST, "Evaluating constructInitialList ");
		initial.constructInitialLists(dag, simu);

		logger.log(Level.FINEST, "Displaying Cpndominantlist ");
		initial.orderlistdisplay(initial.getCpnDominantList());

		logger.log(Level.FINEST, "Displaying blockingNodes ");
		initial.orderlistdisplay(initial.getBlockingNodesList());

		logger.log(Level.FINEST, "Displaying fcp ");
		initial.orderlistdisplay(initial.getFinalcriticalpathList());

		simu.resetDAG();

		// perform tester fast algorithm
		logger.log(Level.FINEST, "Evaluating fast algo");
		FastAlgorithm algorithm = new FastAlgorithm();

		// chosen simulator
		AbcType simulatorType = null;
		if (choixsimu == 0)
			simulatorType = AbcType.LooselyTimed;
		if (choixsimu == 1)
			simulatorType = AbcType.ApproximatelyTimed;
		if (choixsimu == 2)
			simulatorType = AbcType.AccuratelyTimed;

		dag = algorithm.map("test", simulatorType, dag, archi, initial
				.getCpnDominantList(), initial.getBlockingNodesList(), initial
				.getFinalcriticalpathList(), 40, 40, 6, false, false, null);

		// display
		IAbc simu2 = AbstractAbc
				.getInstance(simulatorType, dag, archi);
		simu2.resetImplementation();
		simu2.setDAG(dag);
		logger.log(Level.FINE, "FinalSPlength " + simu2.getFinalTime());
		simu2.plotImplementation(false);
		simu2.resetImplementation();

		logger.log(Level.FINE, "Test finished");
	}
}
