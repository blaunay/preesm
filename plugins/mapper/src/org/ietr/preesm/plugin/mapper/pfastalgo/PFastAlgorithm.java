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
package org.ietr.preesm.plugin.mapper.pfastalgo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ietr.preesm.core.architecture.Examples;
import org.ietr.preesm.core.architecture.IArchitecture;
import org.ietr.preesm.core.architecture.MultiCoreArchitecture;
import org.ietr.preesm.core.log.PreesmLogger;
import org.ietr.preesm.plugin.abc.AbstractAbc;
import org.ietr.preesm.plugin.abc.ArchitectureSimulatorType;
import org.ietr.preesm.plugin.abc.IAbc;
import org.ietr.preesm.plugin.abc.infinitehomogeneous.InfiniteHomogeneousAbc;
import org.ietr.preesm.plugin.mapper.fastalgo.FastAlgorithm;
import org.ietr.preesm.plugin.mapper.fastalgo.InitialLists;
import org.ietr.preesm.plugin.mapper.fastalgo.ListScheduler;
import org.ietr.preesm.plugin.mapper.graphtransfo.DAGCreator;
import org.ietr.preesm.plugin.mapper.model.MapperDAG;
import org.ietr.preesm.plugin.mapper.model.MapperDAGVertex;
import org.ietr.preesm.plugin.mapper.plot.PlotBestLatency;
import org.jfree.ui.RefineryUtilities;

/**
 * Task scheduling FAST algorithm multithread
 * 
 * @author pmenuet
 */
public class PFastAlgorithm extends Observable {

	/**
	 * FinalTimeComparator : comparator between two different implementation
	 * 
	 * @param : void
	 * @return : void
	 */
	private class FinalTimeComparator implements Comparator<MapperDAG> {

		IAbc simulator = null;

		/**
		 * @Override compare
		 * 
		 * @param : MapperDAG, MapperDAG
		 * @return : integer
		 */
		@Override
		public int compare(MapperDAG o1, MapperDAG o2) {

			int difference = 0;

			difference = o1.getScheduleLatency();

			difference -= o2.getScheduleLatency();

			if (difference == 0) {
				difference = 1;
			}

			return difference;
		}

		/**
		 * Constructor : FinalTimeComparator
		 * 
		 * @param : ArchitectureSimulatorType, MapperDAG, IArchitecture
		 * 
		 */
		public FinalTimeComparator(ArchitectureSimulatorType type,
				MapperDAG dag, IArchitecture archi) {
			super();
			this.simulator = AbstractAbc.getInstance(type,
					dag, archi);
		}

	}

	/**
	 * Constructor : PFastAlgorithm
	 * 
	 */
	public PFastAlgorithm() {
		super();
	}

	/**
	 * pFastAlgoSetUp : Determine how many processors will be used among the
	 * available ones and return the set of nodes on which each processor will
	 * perform the fast algorithm
	 * 
	 * @param initialLists
	 * @param nboperator
	 *            // number of available processor
	 * @param nodesmin
	 *            // number of nodes necessary for each thread
	 * @param subSet
	 *            // Set with the partial BlockingNodesLists
	 * 
	 * @return integer
	 */
	public int pFastAlgoSetUp(InitialLists initialLists, int nboperator,
			int nodesmin, Set<Set<String>> subSet) {

		// initialization
		MapperDAGVertex currentvertex;
		int nbsubsets = 0;
		int nbnodes = 0;
		List<MapperDAGVertex> BNlist = new ArrayList<MapperDAGVertex>();
		BNlist.addAll(initialLists.getBlockingNodesList());
		Set<String> tempSet;

		// find number of thread possible
		nbsubsets = setnumber(BNlist, nboperator, nodesmin);

		// find number of nodes per thread
		nbnodes = nbnodes(BNlist, nbsubsets, nodesmin);

		Iterator<MapperDAGVertex> riter = BNlist.iterator();
		Iterator<Set<String>> itera = subSet.iterator();
		subSet.add(new HashSet<String>());

		// Put the nodes of the BlockingNodes List in the Sublist
		for (int i = 0; i < nbsubsets; i++) {
			subSet.add(new HashSet<String>());
			itera = subSet.iterator();
			tempSet = itera.next();
			for (int j = 0; j < nbnodes; j++) {
				if (!(subSet.containsAll(BNlist))) {
					riter = BNlist.iterator();
					currentvertex = riter.next();
					tempSet.add(new String(currentvertex.getName()));
					BNlist.remove(currentvertex);

				}
			}
		}
		
		return nbsubsets;
	}

	/**
	 * setnumber : return the number of processor(thread) necessary to process
	 * the PFastAlgorithm with the List of Vertex
	 * 
	 * @param BLlist
	 *            // BlockingNodesList
	 * @param nboperator
	 *            // number of available processor
	 * @param nodesmin
	 *            // number of nodes necessary for each thread
	 * 
	 * @return integer
	 */

	public int setnumber(List<MapperDAGVertex> BLlist, int nboperator,
			int nodesmin) {

		int nbnodes = BLlist.size();
		int nbsubsets;
		if (nboperator == 0)
			nboperator = 1;
		// verify if we have enough nodes to do the PFastAlgorithm
		if (nbnodes >= nboperator * nodesmin) {
			nbsubsets = nboperator;
		} else {
			nbsubsets = (int) nbnodes / nodesmin;
		}
		return nbsubsets;
	}

	/**
	 * nbnodes : return the number of nodes per partial BlockingNodesList
	 * 
	 * @param BLlist
	 *            // BlockingNodesList
	 * @param nbsubsets
	 *            // Number of thread
	 * @param nodesmin
	 *            // Number of nodes per thread
	 * @return integer
	 */
	public int nbnodes(List<MapperDAGVertex> BLlist, int nbsubsets, int nodesmin) {

		int nbnodes = BLlist.size();
		if (nbsubsets == 0)
			nbsubsets = 1;
		// Find the number of nodes for each thread
		int nbnodeset = (int) nbnodes / nbsubsets;
		if ((nbnodes % nodesmin) != 0)
			nbnodeset++;
		return nbnodeset;
	}

	/**
	 * mapcontain = verify if the vertex is already in the other partial lists
	 * 
	 * @param subSet
	 * @param vertex
	 * 
	 * @return boolean
	 */
	public boolean mapcontain(Set<Set<MapperDAGVertex>> subSet,
			MapperDAGVertex vertex) {

		Iterator<Set<MapperDAGVertex>> itera = subSet.iterator();
		// Verify if the vertex is already in the subset
		while (itera.hasNext()) {
			Set<MapperDAGVertex> temp = itera.next();
			if (temp.contains(vertex))
				return true;
		}
		return false;
	}

	/**
	 * map = perform the Pfast Algo (it is the main thread)
	 * 
	 * @param dag
	 * @param archi
	 * @param nboperator
	 * @param nodesmin
	 * @param initialLists
	 * @param maxCount
	 * @param maxStep
	 * @param margIn
	 * @param simulatorType
	 * @param population
	 *            // Determine if we want the Pfast solution or a population of
	 *            good solution to perform another algorithm
	 * @param populationsize
	 *            // if we want a population this parameter determine how many
	 *            individuals we want in the population
	 * @param populationList
	 *            // List of MapperDAG which are solution
	 * 
	 * @return MapperDAG
	 */

	public MapperDAG map(MapperDAG dag, IArchitecture archi, int nboperator,
			int nodesmin, InitialLists initialLists, int maxCount, int maxStep,
			int margIn, ArchitectureSimulatorType simulatorType,
			boolean population, int populationsize,
			List<MapperDAG> populationList) {

		// Variables
		int i = 0;
		if (populationsize < 1)
			populationsize = 1;
		int k = 0;
		int totalsearchcount = 0;
		Vector<MapperDAGVertex> cpnDominantVector = new Vector<MapperDAGVertex>(
				initialLists.getCpnDominantList());
		Vector<MapperDAGVertex> blockingnodeVector = new Vector<MapperDAGVertex>(
				initialLists.getBlockingNodesList());
		Vector<MapperDAGVertex> fcpVector = new Vector<MapperDAGVertex>(
				initialLists.getFinalcriticalpathList());
		MapperDAG dagfinal;
		ListScheduler scheduler = new ListScheduler();
		IAbc archisimu = AbstractAbc
				.getInstance(simulatorType, dag, archi);
		Set<Set<String>> subSet = new HashSet<Set<String>>();
		Logger logger = PreesmLogger.getLogger();

		// if only one operator the fast must be used
		if (nboperator == 0) {
			FastAlgorithm algorithm = new FastAlgorithm();

			dag = algorithm.map("Fast", simulatorType, dag, archi,
					cpnDominantVector, blockingnodeVector, fcpVector, maxCount,
					maxStep, margIn, false, false, null);
			return dag;
		}

		// Data window set
		final PlotBestLatency demo = new PlotBestLatency("PFast Algorithm");

		if (!population) {
			demo.setSUBPLOT_COUNT(1);
			demo.pack();
			RefineryUtilities.centerFrameOnScreen(demo);
			demo.setVisible(true);

			this.addObserver(demo);
		}

		// step 1
		// step 2
		dagfinal = scheduler.schedule(dag, cpnDominantVector,
				blockingnodeVector, fcpVector, archisimu, null, null).clone();

		int iBest = (Integer) archisimu.getFinalTime();
		int initiale = iBest;
		setChanged();
		notifyObservers(iBest);
		dagfinal.setScheduleLatency(iBest);
		dag.setScheduleLatency(iBest);
		// step 3/4
		int q = pFastAlgoSetUp(initialLists, nboperator, nodesmin, subSet);

		Iterator<Set<String>> subiter = subSet.iterator();
		ConcurrentSkipListSet<MapperDAG> mappedDAGSet = new ConcurrentSkipListSet<MapperDAG>(
				new FinalTimeComparator(simulatorType, dagfinal, archi));

		// step 5/7/8
		for (int j = 2; totalsearchcount < maxCount; j++) {

			logger.log(Level.FINE, "It�ration " + j);
			// Mode Pause
			while (demo.getActionType() == 2)
				;

			// Mode stop
			if (demo.getActionType() == 1)
				return mappedDAGSet.first().clone();

			// step 11
			int maxcounttemp = Math.max(((Double) Math.ceil(((double) maxCount)
					/ ((double) j) / ((double) q))).intValue(), 1);

			// create ExecutorService to manage threads
			subiter = subSet.iterator();
			Set<FutureTask<MapperDAG>> futureTasks = new HashSet<FutureTask<MapperDAG>>();
			ExecutorService es = Executors.newFixedThreadPool(q);

			// step 6
			for (i = k; i < q + k; i++) {
				String name = String.format("thread%d", i);

				// step 9/11
				PFastCallable thread = new PFastCallable(name, dag, archi,
						subiter.next(), maxcounttemp, maxStep, margIn, true,
						simulatorType);

				FutureTask<MapperDAG> task = new FutureTask<MapperDAG>(thread);
				futureTasks.add(task);
				es.submit(task);

			}
			k = i;

			// step 10
			try {

				Iterator<FutureTask<MapperDAG>> it = futureTasks.iterator();

				while (it.hasNext()) {

					FutureTask<MapperDAG> task = it.next();

					MapperDAG currentOutDAG = task.get();
					mappedDAGSet.add(currentOutDAG);

				}
				while (mappedDAGSet.size() > populationsize) {

					mappedDAGSet.pollLast();
				}
				// step 12
				if (!population) {
					dag = mappedDAGSet.first().clone();

					iBest = dag.getScheduleLatency();
					setChanged();
					notifyObservers(iBest);

				}

				es.shutdown();

			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			// step 13
			totalsearchcount += maxcounttemp;

		}

		if (population) {
			Iterator<MapperDAG> ite = mappedDAGSet.iterator();
			while (ite.hasNext()) {
				MapperDAG currentdag;
				currentdag = ite.next();
				populationList.add(currentdag);
			}

		} else {

			dagfinal = mappedDAGSet.first().clone();

		}
		dagfinal = mappedDAGSet.first().clone();
		int finale = dagfinal.getScheduleLatency();
		logger
				.log(
						Level.FINE,
						"Gain total PFast Algo : "
								+ ((((double) initiale - (double) finale) / (double) initiale) * 100)
								+ " %");

		return dagfinal;
	}

	/**
	 * Main for test
	 */
	public static void main(String[] args) {

		Logger logger = PreesmLogger.getLogger();
		logger.setLevel(Level.FINER);

		logger.log(Level.FINER, "Creating 4 cores archi");
		MultiCoreArchitecture archi = Examples.get2C64Archi();

		logger.log(Level.FINER, "Creating DAG");
		MapperDAG dag = new DAGCreator().dagexample2(archi);

		IAbc simu = new InfiniteHomogeneousAbc(
				dag, archi);

		InitialLists initial = new InitialLists();

		logger.log(Level.FINER, "Evaluating constructInitialList ");
		initial.constructInitialLists(dag, simu);
		simu.resetImplementation();
		simu.resetDAG();
		logger.log(Level.FINER, "Evaluating set up pfast ");
		// Set<Set<String>> subSet = new HashSet<Set<String>>();
		PFastAlgorithm pfastAlgorithm = new PFastAlgorithm();

		// pfastAlgorithm.pFastAlgoSetUp(initial, 3, 3, subSet);

		logger.log(Level.FINER, "Evaluating pfast algorithm ");
		pfastAlgorithm.map(dag, archi, 1, 3, initial, 30, 30, 6,
				ArchitectureSimulatorType.LooselyTimed, true, 10, null);

		logger.log(Level.FINER, "Test finished");

	}
}
