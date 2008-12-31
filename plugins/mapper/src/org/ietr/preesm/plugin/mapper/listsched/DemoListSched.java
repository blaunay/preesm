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
package org.ietr.preesm.plugin.mapper.listsched;

import org.ietr.preesm.plugin.mapper.listsched.descriptor.AlgorithmDescriptor;
import org.ietr.preesm.plugin.mapper.listsched.descriptor.ArchitectureDescriptor;
import org.ietr.preesm.plugin.mapper.listsched.descriptor.CommunicationDescriptor;
import org.ietr.preesm.plugin.mapper.listsched.descriptor.ComponentDescriptor;
import org.ietr.preesm.plugin.mapper.listsched.descriptor.ComponentType;
import org.ietr.preesm.plugin.mapper.listsched.descriptor.ComputationDescriptor;
import org.ietr.preesm.plugin.mapper.listsched.descriptor.LinkDescriptor;
import org.ietr.preesm.plugin.mapper.listsched.descriptor.OperationDescriptor;
import org.ietr.preesm.plugin.mapper.listsched.descriptor.OperatorDescriptor;
import org.ietr.preesm.plugin.mapper.listsched.descriptor.SwitchDescriptor;
import org.ietr.preesm.plugin.mapper.listsched.parser.AlgorithmParser;
import org.ietr.preesm.plugin.mapper.listsched.parser.ArchitectureParser;
import org.ietr.preesm.plugin.mapper.listsched.parser.ParameterParser;
import org.ietr.preesm.plugin.mapper.listsched.plotter.GanttPlotter;
import org.ietr.preesm.plugin.mapper.listsched.scheduler.AbstractScheduler;
import org.ietr.preesm.plugin.mapper.listsched.scheduler.CombCSListSched;
import org.ietr.preesm.plugin.mapper.listsched.scheduler.CombCSListSchedCc;
import org.ietr.preesm.plugin.mapper.listsched.scheduler.CombCSListSchedCcCd;
import org.ietr.preesm.plugin.mapper.listsched.scheduler.CombCSListSchedCd;
import org.jfree.ui.RefineryUtilities;
import org.sdf4j.factories.DAGEdgeFactory;

/**
 * A demo of using different list scheduling methods
 * 
 * @author pmu
 * 
 */
public class DemoListSched {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String algorithmFileName = "src\\org\\ietr\\preesm\\plugin\\mapper\\listsched\\algorithm_test.xml";
		String architectureFileName = "src\\org\\ietr\\preesm\\plugin\\mapper\\listsched\\architecture.xml";
		String parameterFileName = "src\\org\\ietr\\preesm\\plugin\\mapper\\listsched\\parameter.xml";

		new DemoListSched(algorithmFileName, parameterFileName,
				architectureFileName);
	}

	/**
	 * The AlgorithmDescriptor
	 */
	private AlgorithmDescriptor algorithm = null;

	/**
	 * The ArchitectureDescriptor
	 */
	private ArchitectureDescriptor architecture = null;

	/**
	 * Construct the DemoListSched using three files of algorithm, parameters
	 * and architecture
	 * 
	 * @param algorithmFileName
	 *            File name of algorithm
	 * @param parameterFileName
	 *            File name of parameters
	 * @param architectureFileName
	 *            File name of architecture
	 */
	public DemoListSched(String algorithmFileName, String parameterFileName,
			String architectureFileName) {
		System.out.println("\n***** DemoListScheduling begins! *****");

		parse(algorithmFileName, architectureFileName, parameterFileName);
		AlgorithmDescriptor algo = algorithm.clone();
		ArchitectureDescriptor archi = architecture.clone();
		CombCSListSched scheduler1 = new CombCSListSched(algo, archi);
		scheduler1.schedule();
		testScheduler(scheduler1.getBestScheduler(), algo, archi);

		algo = algorithm.clone();
		archi = architecture.clone();
		CombCSListSchedCc scheduler2 = new CombCSListSchedCc(algo, archi);
		scheduler2.schedule();
		testScheduler(scheduler2.getBestScheduler(), algo, archi);

		algo = algorithm.clone();
		archi = architecture.clone();
		CombCSListSchedCd scheduler3 = new CombCSListSchedCd(algo, archi);
		scheduler3.schedule();
		testScheduler(scheduler3.getBestScheduler(), algo, archi);

		algo = algorithm.clone();
		archi = architecture.clone();
		CombCSListSchedCcCd scheduler4 = new CombCSListSchedCcCd(algo, archi);
		scheduler4.schedule();
		testScheduler(scheduler4.getBestScheduler(), algo, archi);

		System.out.println("***Compared Results***");

		System.out
				.print("No.\tScheduling Method\t\t\t\t\t\t\t\tSchedule Length\t\tUsed Operators\t\tScheduling Order");

		System.out.print("\n1\t" + scheduler1.getName() + "\t\t\t\t\t\t"
				+ scheduler1.getBestScheduler().getScheduleLength() + "\t\t\t"
				+ scheduler1.getBestScheduler().getUsedOperators().size()
				+ "\t\t\t");
		for (ComputationDescriptor indexComputation : scheduler1
				.getBestScheduler().getStaticOrder()) {
			System.out.print(indexComputation.getName() + " ");
		}
		GanttPlotter plot1 = new GanttPlotter(scheduler1.getBestScheduler()
				.getName()
				+ " -> Schedule Length="
				+ scheduler1.getBestScheduler().getScheduleLength(), scheduler1
				.getBestScheduler());
		plot1.pack();
		RefineryUtilities.centerFrameOnScreen(plot1);
		plot1.setVisible(true);

		System.out.print("\n2\t" + scheduler2.getName() + "\t\t\t\t"
				+ scheduler2.getBestScheduler().getScheduleLength() + "\t\t\t"
				+ scheduler2.getBestScheduler().getUsedOperators().size()
				+ "\t\t\t");
		for (ComputationDescriptor indexComputation : scheduler2
				.getBestScheduler().getStaticOrder()) {
			System.out.print(indexComputation.getName() + " ");
		}
		GanttPlotter plot2 = new GanttPlotter(scheduler2.getBestScheduler()
				.getName()
				+ " -> Schedule Length="
				+ scheduler2.getBestScheduler().getScheduleLength(), scheduler2
				.getBestScheduler());
		plot2.pack();
		RefineryUtilities.centerFrameOnScreen(plot2);
		plot2.setVisible(true);

		System.out.print("\n3\t" + scheduler3.getName() + "\t\t\t"
				+ scheduler3.getBestScheduler().getScheduleLength() + "\t\t\t"
				+ scheduler3.getBestScheduler().getUsedOperators().size()
				+ "\t\t\t");
		for (ComputationDescriptor indexComputation : scheduler3
				.getBestScheduler().getStaticOrder()) {
			System.out.print(indexComputation.getName() + " ");
		}
		GanttPlotter plot3 = new GanttPlotter(scheduler3.getBestScheduler()
				.getName()
				+ " -> Schedule Length="
				+ scheduler3.getBestScheduler().getScheduleLength(), scheduler3
				.getBestScheduler());
		plot3.pack();
		RefineryUtilities.centerFrameOnScreen(plot3);
		plot3.setVisible(true);

		System.out.print("\n4\t" + scheduler4.getName() + "\t"
				+ scheduler4.getBestScheduler().getScheduleLength() + "\t\t\t"
				+ scheduler4.getBestScheduler().getUsedOperators().size()
				+ "\t\t\t");
		for (ComputationDescriptor indexComputation : scheduler4
				.getBestScheduler().getStaticOrder()) {
			System.out.print(indexComputation.getName() + " ");
		}
		GanttPlotter plot4 = new GanttPlotter(scheduler4.getBestScheduler()
				.getName()
				+ " -> Schedule Length="
				+ scheduler4.getBestScheduler().getScheduleLength(), scheduler4
				.getBestScheduler());
		plot4.pack();
		RefineryUtilities.centerFrameOnScreen(plot4);
		plot4.setVisible(true);

		System.out.println("\n\n*****DemoListScheduling finishes!*****");
	}

	/**
	 * Parse algorithm, architecture and parameters
	 * 
	 * @param algorithmFileName
	 *            File name of algorithm
	 * @param architectureFileName
	 *            File name of architecture
	 * @param parameterFileName
	 *            File name of parameters
	 */
	private void parse(String algorithmFileName, String architectureFileName,
			String parameterFileName) {
		algorithm = new AlgorithmDescriptor(new DAGEdgeFactory());
		architecture = new ArchitectureDescriptor();
		// Parse the design algorithm document
		new AlgorithmParser(algorithmFileName, algorithm).parse();
		// Parse the design parameter document
		new ParameterParser(parameterFileName, architecture, algorithm).parse();
		// Parse the architecture document
		new ArchitectureParser(architectureFileName, architecture).parse();

		OperatorDescriptor defaultOperator = null;
		SwitchDescriptor defaultNetwork = null;
		for (ComponentDescriptor indexComponent : architecture.getComponents()
				.values()) {
			if ((indexComponent.getType() == ComponentType.Ip || indexComponent
					.getType() == ComponentType.Processor)
					&& indexComponent.getId().equalsIgnoreCase(
							indexComponent.getName())) {
				defaultOperator = (OperatorDescriptor) indexComponent;
			} else if (indexComponent.getType() == ComponentType.Switch
					&& indexComponent.getId().equalsIgnoreCase(
							indexComponent.getName())) {
				defaultNetwork = (SwitchDescriptor) indexComponent;
			}
		}

		System.out.println(" default operator: Id=" + defaultOperator.getId()
				+ "; Name=" + defaultOperator.getName());
		System.out.println(" default network: Id=" + defaultNetwork.getId()
				+ "; Name=" + defaultNetwork.getName());
		System.out.println("Computations in the algorithm:");
		for (ComputationDescriptor indexComputation : algorithm
				.getComputations().values()) {
			// Allow a computation to be executed on each operator
			for (OperatorDescriptor indexOperator : architecture
					.getAllOperators().values()) {
				indexComputation.addOperator(indexOperator);
			}
			if (!indexComputation.getComputationDurations().containsKey(
					defaultOperator)) {
				indexComputation.addComputationDuration(defaultOperator,
						indexComputation.getTime());
				System.out
						.println(" Name="
								+ indexComputation.getName()
								+ "; default computationDuration="
								+ indexComputation
										.getComputationDuration(defaultOperator
												.getId()) + "; nbTotalRepeate="
								+ indexComputation.getNbTotalRepeat());
			}
		}
		System.out.println("Communications in the algorithm:");
		for (CommunicationDescriptor indexCommunication : algorithm
				.getCommunications().values()) {
			if (!indexCommunication.getCommunicationDurations().containsKey(
					defaultNetwork)) {
				indexCommunication.addCommunicationDuration(defaultNetwork,
						indexCommunication.getWeight());
				System.out.println(" Name="
						+ indexCommunication.getName()
						+ "; default communicationDuration="
						+ indexCommunication
								.getCommunicationDuration(defaultNetwork));
			}
		}
		System.out.println("Operators in the architecture:");
		for (OperatorDescriptor indexOperator : architecture.getAllOperators()
				.values()) {
			System.out.println(" Id=" + indexOperator.getId() + "; Name="
					+ indexOperator.getName());
		}
	}

	/**
	 * Test the performance of a scheduler
	 * 
	 * @param scheduler
	 *            The scheduler
	 * @param algorithm
	 *            The algorithm
	 * @param architecture
	 *            The architecture
	 */
	private void testScheduler(AbstractScheduler scheduler,
			AlgorithmDescriptor algorithm, ArchitectureDescriptor architecture) {

		System.out.println("\nSchedule method: " + scheduler.getName());
		System.out.println("\n***** Schedule results *****");
		for (OperatorDescriptor indexOperator : architecture.getAllOperators()
				.values()) {
			System.out.println("\n Operator: Id=" + indexOperator.getId()
					+ "; Name=" + indexOperator.getName());
			for (OperationDescriptor indexOperation : indexOperator
					.getOperations()) {
				if (indexOperation != scheduler.getTopCommunication()
						&& indexOperation != scheduler.getBottomCommunication()) {
					if (indexOperator.getComputations()
							.contains(indexOperation)) {
						System.out.println("  computation: Name="
								+ indexOperation.getName()
								+ "\n   1> startTime="
								+ indexOperator.getOccupiedTimeInterval(
										indexOperation.getName())
										.getStartTime()
								+ "\n   2> finishTime="
								+ indexOperator.getOccupiedTimeInterval(
										indexOperation.getName())
										.getFinishTime());
					} else {
						if (indexOperator.getSendCommunications().contains(
								indexOperation)) {
							System.out
									.println("  sendCommunication: Name="
											+ indexOperation.getName()
											+ "\n   1> startTimeOnSendOperator="
											+ indexOperator
													.getOccupiedTimeInterval(
															indexOperation
																	.getName())
													.getStartTime()
											+ "\n   2> finishTimeOnSendOperator="
											+ indexOperator
													.getOccupiedTimeInterval(
															indexOperation
																	.getName())
													.getFinishTime()
											+ "\n   3> startTimeOnLink="
											+ ((CommunicationDescriptor) indexOperation)
													.getStartTimeOnLink()
											+ "\n   4> finishTimeOnLink="
											+ ((CommunicationDescriptor) indexOperation)
													.getFinishTimeOnLink());
						} else {
							System.out
									.println("  receiveCommunication: Name="
											+ indexOperation.getName()
											+ "\n   1> startTimeOnReceiveOperator="
											+ indexOperator
													.getOccupiedTimeInterval(
															indexOperation
																	.getName())
													.getStartTime()
											+ "\n   2> finishTimeOnReceiveOperator="
											+ indexOperator
													.getOccupiedTimeInterval(
															indexOperation
																	.getName())
													.getFinishTime()
											+ "\n   3> startTimeOnLink="
											+ ((CommunicationDescriptor) indexOperation)
													.getStartTimeOnLink()
											+ "\n   4> finishTimeOnLink="
											+ ((CommunicationDescriptor) indexOperation)
													.getFinishTimeOnLink());
						}
					}
				}
			}
			for (LinkDescriptor indexLink : indexOperator.getOutputLinks()) {
				System.out.println(" outputLink: Id=" + indexLink.getId()
						+ "; Name=" + indexLink.getName());
				for (CommunicationDescriptor indexCommunication : indexLink
						.getCommunications()) {
					if (indexCommunication.getSendLink() == indexLink) {
						System.out.println("  sendCommunication: Name="
								+ indexCommunication.getName()
								+ "\n   1> startTimeOnLink="
								+ indexCommunication.getStartTimeOnLink()
								+ "\n   2> finishTimeOnLink="
								+ indexCommunication.getFinishTimeOnLink());
					}
				}
			}
			for (LinkDescriptor indexLink : indexOperator.getInputLinks()) {
				System.out.println(" inputLink: Id=" + indexLink.getId()
						+ "; Name=" + indexLink.getName());
				for (CommunicationDescriptor indexCommunication : indexLink
						.getCommunications()) {
					if (indexCommunication.getReceiveLink() == indexLink) {
						System.out.println("  receiveCommunication: Name="
								+ indexCommunication.getName()
								+ "\n   1> startTimeOnLink="
								+ indexCommunication.getStartTimeOnLink()
								+ "\n   2> finishTimeOnLink="
								+ indexCommunication.getFinishTimeOnLink());
					}
				}
			}
		}
		System.out.println("\n***** Schedule Length="
				+ scheduler.getScheduleLength() + " *****\n");
	}
}
