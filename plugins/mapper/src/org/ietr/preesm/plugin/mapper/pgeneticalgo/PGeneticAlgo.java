/**
 * 
 */
package org.ietr.preesm.plugin.mapper.pgeneticalgo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Set;
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
import org.ietr.preesm.core.constraints.IScenario;
import org.ietr.preesm.core.constraints.Scenario;
import org.ietr.preesm.core.constraints.Timing;
import org.ietr.preesm.core.constraints.TimingManager;
import org.ietr.preesm.core.log.PreesmLogger;
import org.ietr.preesm.core.workflow.sources.AlgorithmRetriever;
import org.ietr.preesm.plugin.abc.ArchitectureSimulatorType;
import org.ietr.preesm.plugin.abc.IAbc;
import org.ietr.preesm.plugin.abc.infinitehomogeneous.InfiniteHomogeneousAbc;
import org.ietr.preesm.plugin.mapper.fastalgo.InitialLists;
import org.ietr.preesm.plugin.mapper.geneticalgo.Chromosome;
import org.ietr.preesm.plugin.mapper.geneticalgo.StandardGeneticAlgorithm;
import org.ietr.preesm.plugin.mapper.graphtransfo.DAGCreator;
import org.ietr.preesm.plugin.mapper.graphtransfo.SdfToDagConverter;
import org.ietr.preesm.plugin.mapper.model.MapperDAG;
import org.ietr.preesm.plugin.mapper.pfastalgo.PFastAlgorithm;
import org.ietr.preesm.plugin.mapper.plot.PlotBestLatency;
import org.jfree.ui.RefineryUtilities;
import org.sdf4j.model.sdf.SDFGraph;

/**
 * Task scheduling genetic algorithm
 * 
 * @author pmenuet
 */
public class PGeneticAlgo extends Observable {

	/**
	 * FinalTimeComparator : comparator between two different implementation
	 * 
	 * @param : void
	 * @return : void
	 */
	private class FinalTimeComparator implements Comparator<Chromosome> {

		ArchitectureSimulatorType simulatorType = null;

		@Override
		public int compare(Chromosome o1, Chromosome o2) {

			int difference = 0;
			if (o1.isDirty())
				o1.evaluate(simulatorType);
			if (o2.isDirty())
				o2.evaluate(simulatorType);

			difference = o1.getEvaluateCost() - o2.getEvaluateCost();

			if (difference == 0) {
				difference = 1;
			}

			return difference;
		}

		/**
		 * Constructor : FinalTimeComparator
		 * 
		 * @param : ArchitectureSimulatorType, Chromosome, IArchitecture
		 * 
		 */
		public FinalTimeComparator(ArchitectureSimulatorType type,
				Chromosome chromosome) {
			super();
			this.simulatorType = type;
		}

	}

	/**
	 * Constructor
	 */
	public PGeneticAlgo() {
		super();
	}

	/**
	 * map = perform the PGenetic Algorithm (it's the main)
	 * 
	 * @param populationDAG
	 * @param archi
	 * @param type
	 * @param populationSize
	 * @param generationNumber
	 * @param processorNumber
	 * 
	 * @return List<Chromosome>
	 */
	public List<Chromosome> map(List<MapperDAG> populationDAG,
			IArchitecture archi, ArchitectureSimulatorType type,
			int populationSize, int generationNumber, int processorNumber) {

		// variables
		processorNumber = processorNumber - 1;

		// Convert list of MapperDAG into a List of Chromosomes
		StandardGeneticAlgorithm algorithm = new StandardGeneticAlgorithm();
		List<Chromosome> population = algorithm.convertListDAGtoListChromo(
				populationDAG, archi);

		// List which allow to save all the population (of all Thread)
		List<List<Chromosome>> listMap = new ArrayList<List<Chromosome>>();

		// best Population
		ConcurrentSkipListSet<Chromosome> result = new ConcurrentSkipListSet<Chromosome>(
				new FinalTimeComparator(type, population.get(0)));
		Logger logger = PreesmLogger.getLogger();

		// if only one processor is used we must do the Standard Genetic
		// Algorithm
		if (processorNumber == 0) {
			StandardGeneticAlgorithm geneticAlgorithm = new StandardGeneticAlgorithm();
			result = geneticAlgorithm.runGeneticAlgo("genetic algorithm",
					populationDAG, archi, type, populationSize,
					generationNumber, false);
			List<Chromosome> result2 = new ArrayList<Chromosome>();
			result2.addAll(result);
			return result2;
		}

		// Set Data window
		final PlotBestLatency demo = new PlotBestLatency(
				"Parallel genetic Algorithm");
		demo.setSUBPLOT_COUNT(1);
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);
		this.addObserver(demo);

		// set best population
		result.addAll(population);

		// Copy the initial population processorNumber times (number of thread
		// we will create)
		for (int i = 0; i < processorNumber; i++) {
			List<Chromosome> list = new ArrayList<Chromosome>();
			Iterator<Chromosome> iterator = population.listIterator();
			while (iterator.hasNext()) {
				list.add(iterator.next().clone());
			}
			listMap.add(list);
		}

		// simple verification and variables
		if (result.first().isDirty())
			result.first().evaluate(type);
		int iBest = result.first().getEvaluateCost();
		setChanged();
		notifyObservers(iBest);
		int i = 0;
		int k = 0;
		int totalgeneration = 0;
		Iterator<List<Chromosome>> itefinal = listMap.listIterator();

		// Perform the PGenetic Algorithm
		for (int j = 2; totalgeneration < generationNumber; j++) {

			itefinal = listMap.listIterator();
			logger.log(Level.FINE, "regroup number " + j);

			// Mode Pause
			while (demo.getActionType() == 2)
				;

			// Mode stop
			if (demo.getActionType() == 1) {
				List<Chromosome> result2 = new ArrayList<Chromosome>();
				result2.addAll(result);
				return result2;
			}

			// determine the number of generation we need for the threads this
			// time
			int generationNumberTemp = Math.max(((Double) Math
					.ceil(((double) generationNumber)// / ((double) j)
							/ ((double) processorNumber))).intValue(), 1);

			// create ExecutorService to manage threads
			Set<FutureTask<List<Chromosome>>> futureTasks = new HashSet<FutureTask<List<Chromosome>>>();
			ExecutorService es = Executors.newFixedThreadPool(processorNumber);

			// Create the threads with the right parameters
			Iterator<List<Chromosome>> iter = listMap.listIterator();
			for (i = k; i < processorNumber + k; i++) {
				String name = String.format("thread%d", i);

				// step 9/11
				PGeneticAlgoCallable thread = new PGeneticAlgoCallable(archi,
						generationNumberTemp, iter.next(), populationSize,
						type, name);
				// Add the thread in the pool for the executor
				FutureTask<List<Chromosome>> task = new FutureTask<List<Chromosome>>(
						thread);
				futureTasks.add(task);
				es.submit(task);
			}
			k = i;

			// The thread are launch
			try {
				Iterator<FutureTask<List<Chromosome>>> it = futureTasks
						.iterator();

				// retrieve all the results
				while (it.hasNext()) {

					FutureTask<List<Chromosome>> task = it.next();

					List<Chromosome> temp = task.get();

					itefinal.next().addAll(temp);
					result.addAll(temp);
				}

				// reduce the best population to keep the best individuals
				while (result.size() > populationSize) {

					result.pollLast();
				}

				// step 12
				iBest = result.first().getEvaluateCost();
				setChanged();
				notifyObservers(iBest);
				es.shutdown();

			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}

			// distribute the best solution to all the population
			Iterator<List<Chromosome>> bestDistributed = listMap.listIterator();
			while (bestDistributed.hasNext()) {
				bestDistributed.next().add(result.first().clone());
			}

			totalgeneration += generationNumberTemp;
		}

		List<Chromosome> result2 = new ArrayList<Chromosome>();
		result2.addAll(result);
		return result2;
	}

	/**
	 * Main for test
	 */
	public static void main(String[] args) {

		PreesmLogger.getLogger().setLevel(Level.FINER);
		Logger logger = PreesmLogger.getLogger();

		DAGCreator dagCreator = new DAGCreator();
		MultiCoreArchitecture archi = Examples.get4C64Archi();
		// Generating random sdf dag
		int nbVertex = 15, minInDegree = 1, maxInDegree = 3, minOutDegree = 1, maxOutDegree = 3;
		SDFGraph graph = AlgorithmRetriever.randomDAG(nbVertex, minInDegree,
				maxInDegree, minOutDegree, maxOutDegree, 100,true);

		// Generating constraints
		IScenario scenario = new Scenario();

		TimingManager tmgr = scenario.getTimingManager();

		for (int i = 1; i <= nbVertex; i++) {
			String name = String.format("Vertex %d", i);
			Timing newt = new Timing(archi.getOperatorDefinition("c64x"), graph
					.getVertex(name), 50);
			tmgr.addTiming(newt);
		}

		// Converting sdf dag in mapper dag
		MapperDAG dag = SdfToDagConverter.convert(graph, archi, scenario,false);
		// MapperDAG dag = dagCreator.dagexample2(archi);

		IAbc simu = new InfiniteHomogeneousAbc(
				dag, archi);
		InitialLists initialLists = new InitialLists();
		initialLists.constructInitialLists(dag, simu);
		simu.resetDAG();

		ArchitectureSimulatorType simulatorType = ArchitectureSimulatorType.ApproximatelyTimed;

		PFastAlgorithm fastAlgorithm = new PFastAlgorithm();

		List<MapperDAG> popuList = new ArrayList<MapperDAG>();
		fastAlgorithm.map(dag, archi, 2, 5, initialLists, 10, 6, 3,
				simulatorType, true, 4, popuList);

		PGeneticAlgo geneticAlgo = new PGeneticAlgo();

		geneticAlgo.map(popuList, archi, simulatorType, 4, 5, 2);
		logger.log(Level.FINE, "test finished");
	}

}