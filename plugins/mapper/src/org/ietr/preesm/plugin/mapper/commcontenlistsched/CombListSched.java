package org.ietr.preesm.plugin.mapper.commcontenlistsched;

import java.util.HashMap;

import org.ietr.preesm.core.architecture.IArchitecture;
import org.ietr.preesm.core.scenario.IScenario;
import org.ietr.preesm.plugin.mapper.commcontenlistsched.AlgorithmTransformer;
import org.ietr.preesm.plugin.mapper.commcontenlistsched.descriptor.AlgorithmDescriptor;
import org.ietr.preesm.plugin.mapper.commcontenlistsched.descriptor.ArchitectureDescriptor;
import org.ietr.preesm.plugin.mapper.commcontenlistsched.descriptor.CommunicationDescriptor;
import org.ietr.preesm.plugin.mapper.commcontenlistsched.descriptor.ComponentDescriptor;
import org.ietr.preesm.plugin.mapper.commcontenlistsched.descriptor.ComponentType;
import org.ietr.preesm.plugin.mapper.commcontenlistsched.descriptor.ComputationDescriptor;
import org.ietr.preesm.plugin.mapper.commcontenlistsched.descriptor.LinkDescriptor;
import org.ietr.preesm.plugin.mapper.commcontenlistsched.descriptor.OperatorDescriptor;
import org.ietr.preesm.plugin.mapper.commcontenlistsched.descriptor.SwitchDescriptor;
import org.ietr.preesm.plugin.mapper.commcontenlistsched.parser.ArchitectureParser;
import org.ietr.preesm.plugin.mapper.commcontenlistsched.parser.ParameterParser;
import org.ietr.preesm.plugin.mapper.commcontenlistsched.plotter.GanttPlotter;
import org.ietr.preesm.plugin.mapper.commcontenlistsched.scheduler.*;
import org.ietr.preesm.plugin.mapper.graphtransfo.SdfToDagConverter;
import org.ietr.preesm.plugin.mapper.model.MapperDAG;
import org.jfree.ui.RefineryUtilities;

import org.sdf4j.model.sdf.SDFAbstractVertex;
import org.sdf4j.model.sdf.SDFGraph;

public class CombListSched {

	private String architectureFileName = "D:\\Projets\\PreesmSourceForge\\trunk\\plugins\\mapper\\src\\org\\ietr\\preesm\\plugin\\mapper\\commcontenlistsched\\architecture.xml";

	private String parameterFileName = "D:\\Projets\\PreesmSourceForge\\trunk\\plugins\\mapper\\src\\org\\ietr\\preesm\\plugin\\mapper\\commcontenlistsched\\parameter.xml";

	private AlgorithmTransformer algoTransformer = null;

	private ArchitectureTransformer archiTransformer = null;

	private ScenarioTransformer scenaTransformer = null;

	private SDFGraph sdf = null;
	private IArchitecture architecture = null;
	private IScenario scenario = null;

	private MapperDAG dag = null;

	private AlgorithmDescriptor algo = null;
	private ArchitectureDescriptor archi = null;
	private HashMap<String, Integer> computationWeights = null;

	private AbstractScheduler bestScheduler = null;

	private int bestScheduleLength = Integer.MAX_VALUE;

	public CombListSched(SDFGraph sdf, IArchitecture architecture,
			IScenario scenario) {
		algoTransformer = new AlgorithmTransformer();
		archiTransformer = new ArchitectureTransformer();
		scenaTransformer = new ScenarioTransformer();
		this.sdf = sdf;
		this.architecture = architecture;
		this.scenario = scenario;
		computationWeights = algoTransformer.generateRandomNodeWeight(sdf, 500,
				1000);
	}

	public CombListSched(String parameterFileName, String architectureFileName) {
		this.architectureFileName = architectureFileName;
		this.parameterFileName = parameterFileName;
		algoTransformer = new AlgorithmTransformer();

		// Generating random DAG-like SDF
		int nbVertex = 100, minInDegree = 1, maxInDegree = 3, minOutDegree = 1, maxOutDegree = 3;
		sdf = algoTransformer.randomSDF(nbVertex, minInDegree, maxInDegree,
				minOutDegree, maxOutDegree, 500, 1000);
		computationWeights = algoTransformer.generateRandomNodeWeight(sdf, 500,
				1000);
	}

	private void parse() {
		if (dag != null) {
			System.out.println("Transform DAG to algorithm...");
			algo = algoTransformer.dag2Algorithm(dag);
		} else {
			System.out.println("Transform SDF to algorithm...");
			algo = algoTransformer.sdf2Algorithm(sdf);
		}

		if (architecture != null) {
			System.out.println("Transform architecture...");
			archi = archiTransformer.architecture2Descriptor(architecture);
		} else {
			System.out.println("Parse architecture...");
			archi = new ArchitectureDescriptor();
			// Parse the design architecture document
			new ArchitectureParser(architectureFileName, archi).parse();
		}

		if (scenario != null) {
			System.out.println("Parse scenario...");
			scenaTransformer.parseScenario(scenario, algo, archi);
		} else {
			// set computation time with default weights
			for (SDFAbstractVertex indexVertex : sdf.vertexSet()) {
				algo.getComputation(indexVertex.getName()).setTime(
						computationWeights.get(indexVertex.getName()));
			}
			// Parse the design parameter document
			System.out.println("Parse parameters...");
			new ParameterParser(parameterFileName, archi, algo).parse();
			OperatorDescriptor defaultOperator = null;
			SwitchDescriptor defaultSwitch = null;
			LinkDescriptor defaultLink = null;
			for (ComponentDescriptor indexComponent : archi.getComponents()
					.values()) {
				if ((indexComponent.getType() == ComponentType.Ip || indexComponent
						.getType() == ComponentType.Processor)
						&& indexComponent.getId().equalsIgnoreCase(
								indexComponent.getName())) {
					defaultOperator = (OperatorDescriptor) indexComponent;
				} else if (indexComponent.getType() == ComponentType.Switch
						&& indexComponent.getId().equalsIgnoreCase(
								indexComponent.getName())) {
					defaultSwitch = (SwitchDescriptor) indexComponent;
				} else if ((indexComponent.getType() == ComponentType.Bus || indexComponent
						.getType() == ComponentType.Processor)
						&& indexComponent.getId().equalsIgnoreCase(
								indexComponent.getName())) {
					defaultLink = (LinkDescriptor) indexComponent;
				}
			}
			System.out.println(" default operator: Id="
					+ defaultOperator.getId() + "; Name="
					+ defaultOperator.getName());
			System.out.println(" default switch: Id=" + defaultSwitch.getId()
					+ "; Name=" + defaultSwitch.getName());
			System.out.println(" default link: Id=" + defaultLink.getId()
					+ "; Name=" + defaultLink.getName());

			System.out.println("Computations in the algorithm:");
			for (ComputationDescriptor indexComputation : algo
					.getComputations().values()) {
				// Allow a computation to be executed on each operator
				for (OperatorDescriptor indexOperator : archi.getAllOperators()
						.values()) {
					indexComputation.addOperator(indexOperator);
				}
				if (!indexComputation.getComputationDurations().containsKey(
						defaultOperator)) {
					indexComputation.addComputationDuration(defaultOperator,
							indexComputation.getTime());
					System.out.println(" Name="
							+ indexComputation.getName()
							+ "; default computationDuration="
							+ indexComputation
									.getComputationDuration(defaultOperator
											.getId()) + "; nbTotalRepeate="
							+ indexComputation.getNbTotalRepeat());
				}
			}
			System.out.println("Communications in the algorithm:");
			for (CommunicationDescriptor indexCommunication : algo
					.getCommunications().values()) {
				if (!indexCommunication.getCommunicationDurations()
						.containsKey(defaultSwitch)) {
					indexCommunication.addCommunicationDuration(defaultSwitch,
							indexCommunication.getWeight());
					System.out.println(" Name="
							+ indexCommunication.getName()
							+ "; default communicationDuration="
							+ indexCommunication
									.getCommunicationDuration(defaultSwitch));
				}
				if (!indexCommunication.getCommunicationDurations()
						.containsKey(defaultLink)) {
					indexCommunication.addCommunicationDuration(defaultLink,
							indexCommunication.getWeight());
					System.out.println(" Name="
							+ indexCommunication.getName()
							+ "; default communicationDuration="
							+ indexCommunication
									.getCommunicationDuration(defaultLink));
				}
			}
		}

		System.out.println("Operators in the architecture:");
		for (OperatorDescriptor indexOperator : archi.getAllOperators()
				.values()) {
			System.out.println(" Id=" + indexOperator.getId() + "; Name="
					+ indexOperator.getName());
		}
		System.out.println("Switches in the architecture:");
		for (SwitchDescriptor indexSwitch : archi.getAllSwitches().values()) {
			System.out.println(" Id=" + indexSwitch.getId() + "; Name="
					+ indexSwitch.getName());
		}
		System.out.println("Media(Buses) in the architecture:");
		for (LinkDescriptor indexLink : archi.getAllLinks().values()) {
			System.out.println(" Id=" + indexLink.getId() + "; Name="
					+ indexLink.getName());
		}
	}

	public void schedule() {
		System.out
				.println("\n***** Combined List Scheduling With Static Order Begins! *****");
		if ((architecture != null) && (scenario != null)) {
			dag = SdfToDagConverter.convert(sdf, architecture, scenario, false);
		}
		parse();
		CListSchedBlcomp scheduler1 = new CListSchedBlcomp(algo, archi);
		scheduler1.schedule();

		parse();
		CListSchedBl scheduler2 = new CListSchedBl(algo, archi);
		scheduler2.schedule();

		parse();
		CListSchedBlin scheduler3 = new CListSchedBlin(algo, archi);
		scheduler3.schedule();

		parse();
		CListSchedBlout scheduler4 = new CListSchedBlout(algo, archi);
		scheduler4.schedule();

		parse();
		CListSchedBlinout scheduler5 = new CListSchedBlinout(algo, archi);
		scheduler5.schedule();

		parse();
		CListSchedCcBlcomp scheduler6 = new CListSchedCcBlcomp(algo, archi);
		scheduler6.schedule();

		parse();
		CListSchedCcBl scheduler7 = new CListSchedCcBl(algo, archi);
		scheduler7.schedule();

		parse();
		CListSchedCcBlin scheduler8 = new CListSchedCcBlin(algo, archi);
		scheduler8.schedule();

		parse();
		CListSchedCcBlout scheduler9 = new CListSchedCcBlout(algo, archi);
		scheduler9.schedule();

		parse();
		CListSchedCcBlinout scheduler10 = new CListSchedCcBlinout(algo, archi);
		scheduler10.schedule();

		parse();
		CListSchedCdBlcomp scheduler11 = new CListSchedCdBlcomp(algo, archi);
		scheduler11.schedule();

		parse();
		CListSchedCdBl scheduler12 = new CListSchedCdBl(algo, archi);
		scheduler12.schedule();

		parse();
		CListSchedCdBlin scheduler13 = new CListSchedCdBlin(algo, archi);
		scheduler13.schedule();

		parse();
		CListSchedCdBlout scheduler14 = new CListSchedCdBlout(algo, archi);
		scheduler14.schedule();

		parse();
		CListSchedCdBlinout scheduler15 = new CListSchedCdBlinout(algo, archi);
		scheduler15.schedule();

		parse();
		CListSchedCcCdBlcomp scheduler16 = new CListSchedCcCdBlcomp(algo, archi);
		scheduler16.schedule();

		parse();
		CListSchedCcCdBl scheduler17 = new CListSchedCcCdBl(algo, archi);
		scheduler17.schedule();

		parse();
		CListSchedCcCdBlin scheduler18 = new CListSchedCcCdBlin(algo, archi);
		scheduler18.schedule();

		parse();
		CListSchedCcCdBlout scheduler19 = new CListSchedCcCdBlout(algo, archi);
		scheduler19.schedule();

		parse();
		CListSchedCcCdBlinout scheduler20 = new CListSchedCcCdBlinout(algo,
				archi);
		scheduler20.schedule();

		chooseBestScheduler(scheduler1);
		chooseBestScheduler(scheduler2);
		chooseBestScheduler(scheduler3);
		chooseBestScheduler(scheduler4);
		chooseBestScheduler(scheduler5);
		chooseBestScheduler(scheduler6);
		chooseBestScheduler(scheduler7);
		chooseBestScheduler(scheduler8);
		chooseBestScheduler(scheduler9);
		chooseBestScheduler(scheduler10);
		chooseBestScheduler(scheduler11);
		chooseBestScheduler(scheduler12);
		chooseBestScheduler(scheduler13);
		chooseBestScheduler(scheduler14);
		chooseBestScheduler(scheduler15);
		chooseBestScheduler(scheduler16);
		chooseBestScheduler(scheduler17);
		chooseBestScheduler(scheduler18);
		chooseBestScheduler(scheduler19);
		chooseBestScheduler(scheduler20);

		System.out.println("***Compared Results***");

		System.out
				.print("No.\tScheduling Method\t\t\t\t\t\t\t\t\t\t\t\tSchedule Length\t\tUsed Operators\t\tScheduling Order");

		System.out.print("\n1\t" + scheduler1.getName() + "\t\t\t\t\t\t"
				+ scheduler1.getScheduleLength() + "\t\t\t"
				+ scheduler1.getUsedOperators().size() + "\t\t\t");
		for (ComputationDescriptor indexComputation : scheduler1
				.getSchedulingOrder()) {
			System.out.print(indexComputation.getName() + " ");
		}

		System.out.print("\n2\t" + scheduler2.getName() + "\t\t\t\t\t\t\t"
				+ scheduler2.getScheduleLength() + "\t\t\t"
				+ scheduler2.getUsedOperators().size() + "\t\t\t");
		for (ComputationDescriptor indexComputation : scheduler2
				.getSchedulingOrder()) {
			System.out.print(indexComputation.getName() + " ");
		}

		System.out.print("\n3\t" + scheduler3.getName() + "\t\t\t\t\t\t\t"
				+ scheduler3.getScheduleLength() + "\t\t\t"
				+ scheduler3.getUsedOperators().size() + "\t\t\t");
		for (ComputationDescriptor indexComputation : scheduler3
				.getSchedulingOrder()) {
			System.out.print(indexComputation.getName() + " ");
		}

		System.out.print("\n4\t" + scheduler4.getName() + "\t\t\t\t\t\t"
				+ scheduler4.getScheduleLength() + "\t\t\t"
				+ scheduler4.getUsedOperators().size() + "\t\t\t");
		for (ComputationDescriptor indexComputation : scheduler4
				.getSchedulingOrder()) {
			System.out.print(indexComputation.getName() + " ");
		}

		System.out.print("\n5\t" + scheduler5.getName() + "\t\t\t\t\t\t"
				+ scheduler5.getScheduleLength() + "\t\t\t"
				+ scheduler5.getUsedOperators().size() + "\t\t\t");
		for (ComputationDescriptor indexComputation : scheduler5
				.getSchedulingOrder()) {
			System.out.print(indexComputation.getName() + " ");
		}

		System.out.print("\n6\t" + scheduler6.getName() + "\t\t\t"
				+ scheduler6.getScheduleLength() + "\t\t\t"
				+ scheduler6.getUsedOperators().size() + "\t\t\t");
		for (ComputationDescriptor indexComputation : scheduler6
				.getSchedulingOrder()) {
			System.out.print(indexComputation.getName() + " ");
		}

		System.out.print("\n7\t" + scheduler7.getName() + "\t\t\t\t\t"
				+ scheduler7.getScheduleLength() + "\t\t\t"
				+ scheduler7.getUsedOperators().size() + "\t\t\t");
		for (ComputationDescriptor indexComputation : scheduler7
				.getSchedulingOrder()) {
			System.out.print(indexComputation.getName() + " ");
		}

		System.out.print("\n8\t" + scheduler8.getName() + "\t\t\t\t"
				+ scheduler8.getScheduleLength() + "\t\t\t"
				+ scheduler8.getUsedOperators().size() + "\t\t\t");
		for (ComputationDescriptor indexComputation : scheduler8
				.getSchedulingOrder()) {
			System.out.print(indexComputation.getName() + " ");
		}

		System.out.print("\n9\t" + scheduler9.getName() + "\t\t\t\t"
				+ scheduler9.getScheduleLength() + "\t\t\t"
				+ scheduler9.getUsedOperators().size() + "\t\t\t");
		for (ComputationDescriptor indexComputation : scheduler9
				.getSchedulingOrder()) {
			System.out.print(indexComputation.getName() + " ");
		}

		System.out.print("\n10\t" + scheduler10.getName() + "\t\t\t"
				+ scheduler10.getScheduleLength() + "\t\t\t"
				+ scheduler10.getUsedOperators().size() + "\t\t\t");
		for (ComputationDescriptor indexComputation : scheduler10
				.getSchedulingOrder()) {
			System.out.print(indexComputation.getName() + " ");
		}

		System.out.print("\n11\t" + scheduler11.getName() + "\t\t\t"
				+ scheduler11.getScheduleLength() + "\t\t\t"
				+ scheduler11.getUsedOperators().size() + "\t\t\t");
		for (ComputationDescriptor indexComputation : scheduler11
				.getSchedulingOrder()) {
			System.out.print(indexComputation.getName() + " ");
		}

		System.out.print("\n12\t" + scheduler12.getName() + "\t\t\t\t"
				+ scheduler12.getScheduleLength() + "\t\t\t"
				+ scheduler12.getUsedOperators().size() + "\t\t\t");
		for (ComputationDescriptor indexComputation : scheduler12
				.getSchedulingOrder()) {
			System.out.print(indexComputation.getName() + " ");
		}

		System.out.print("\n13\t" + scheduler13.getName() + "\t\t\t\t"
				+ scheduler13.getScheduleLength() + "\t\t\t"
				+ scheduler13.getUsedOperators().size() + "\t\t\t");
		for (ComputationDescriptor indexComputation : scheduler13
				.getSchedulingOrder()) {
			System.out.print(indexComputation.getName() + " ");
		}

		System.out.print("\n14\t" + scheduler14.getName() + "\t\t\t"
				+ scheduler14.getScheduleLength() + "\t\t\t"
				+ scheduler14.getUsedOperators().size() + "\t\t\t");
		for (ComputationDescriptor indexComputation : scheduler14
				.getSchedulingOrder()) {
			System.out.print(indexComputation.getName() + " ");
		}

		System.out.print("\n15\t" + scheduler15.getName() + "\t\t\t"
				+ scheduler15.getScheduleLength() + "\t\t\t"
				+ scheduler15.getUsedOperators().size() + "\t\t\t");
		for (ComputationDescriptor indexComputation : scheduler15
				.getSchedulingOrder()) {
			System.out.print(indexComputation.getName() + " ");
		}

		System.out.print("\n16\t" + scheduler16.getName() + "\t"
				+ scheduler16.getScheduleLength() + "\t\t\t"
				+ scheduler16.getUsedOperators().size() + "\t\t\t");
		for (ComputationDescriptor indexComputation : scheduler16
				.getSchedulingOrder()) {
			System.out.print(indexComputation.getName() + " ");
		}

		System.out.print("\n17\t" + scheduler17.getName() + "\t\t"
				+ scheduler17.getScheduleLength() + "\t\t\t"
				+ scheduler17.getUsedOperators().size() + "\t\t\t");
		for (ComputationDescriptor indexComputation : scheduler17
				.getSchedulingOrder()) {
			System.out.print(indexComputation.getName() + " ");
		}

		System.out.print("\n18\t" + scheduler18.getName() + "\t\t"
				+ scheduler18.getScheduleLength() + "\t\t\t"
				+ scheduler18.getUsedOperators().size() + "\t\t\t");
		for (ComputationDescriptor indexComputation : scheduler18
				.getSchedulingOrder()) {
			System.out.print(indexComputation.getName() + " ");
		}

		System.out.print("\n19\t" + scheduler19.getName() + "\t"
				+ scheduler19.getScheduleLength() + "\t\t\t"
				+ scheduler19.getUsedOperators().size() + "\t\t\t");
		for (ComputationDescriptor indexComputation : scheduler19
				.getSchedulingOrder()) {
			System.out.print(indexComputation.getName() + " ");
		}

		System.out.print("\n20\t" + scheduler20.getName() + "\t"
				+ scheduler20.getScheduleLength() + "\t\t\t"
				+ scheduler20.getUsedOperators().size() + "\t\t\t");
		for (ComputationDescriptor indexComputation : scheduler20
				.getSchedulingOrder()) {
			System.out.print(indexComputation.getName() + " ");
		}

		System.out.print("\n\nBest Scheduler:\t\t" + bestScheduler.getName()
				+ "\nSchedule Length:\t" + bestScheduler.getScheduleLength()
				+ "\nUsed Operators:\t\t"
				+ bestScheduler.getUsedOperators().size());

		plot(bestScheduler);
		System.out
				.println("\n\n***** Combined List Scheduling With Static Order Finishes!*****");
	}

	private void chooseBestScheduler(AbstractScheduler scheduler) {
		if (bestScheduleLength > scheduler.getScheduleLength()) {
			bestScheduler = scheduler;
			bestScheduleLength = scheduler.getScheduleLength();
		} else if (bestScheduleLength == scheduler.getScheduleLength()) {
			if (bestScheduler.getUsedOperators().size() > scheduler
					.getUsedOperators().size()) {
				bestScheduler = scheduler;
				bestScheduleLength = scheduler.getScheduleLength();
			}
		}
	}

	public AbstractScheduler getBestScheduler() {
		return bestScheduler;
	}

	public int getBestScheduleLength() {
		return bestScheduleLength;
	}

	private void plot(AbstractScheduler scheduler) {
		GanttPlotter plot = new GanttPlotter(scheduler.getName()
				+ " -> Schedule Length=" + scheduler.getScheduleLength(),
				scheduler);
		plot.pack();
		RefineryUtilities.centerFrameOnScreen(plot);
		plot.setVisible(true);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String architectureFileName = "src\\org\\ietr\\preesm\\plugin\\mapper\\commcontenlistsched\\architecture.xml";
		String parameterFileName = "src\\org\\ietr\\preesm\\plugin\\mapper\\commcontenlistsched\\parameter.xml";

		CombListSched test = new CombListSched(parameterFileName,
				architectureFileName);
		test.schedule();
	}
}
