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
package org.ietr.preesm.plugin.mapper.listsched.scheduler;

import java.util.HashSet;
import java.util.Set;

import org.ietr.preesm.plugin.mapper.listsched.descriptor.AlgorithmDescriptor;
import org.ietr.preesm.plugin.mapper.listsched.descriptor.ArchitectureDescriptor;
import org.ietr.preesm.plugin.mapper.listsched.descriptor.CommunicationDescriptor;
import org.ietr.preesm.plugin.mapper.listsched.descriptor.ComputationDescriptor;
import org.ietr.preesm.plugin.mapper.listsched.descriptor.LinkDescriptor;
import org.ietr.preesm.plugin.mapper.listsched.descriptor.OperatorDescriptor;

/**
 * This class gives a classic dynamic list scheduling method with nodes sorted
 * by input bottom level in the case of communication contention.
 * 
 * @author pmu
 */
public class CDListSchedBlin extends CSListSched {

	/**
	 * Constructs the scheduler with algorithm and architecture.
	 * 
	 * @param algorithm
	 *            Algorithm descriptor
	 * @param architecture
	 *            Architecture descriptor
	 */
	public CDListSchedBlin(AlgorithmDescriptor algorithm,
			ArchitectureDescriptor architecture) {
		super(algorithm, architecture);
		// TODO Auto-generated constructor stub
		this.name = "Classic Dynamic List Scheduling With Nodes Sorted By Input Bottom Level";
	}

	public boolean schedule() {
		System.out.println("\n***** " + name + " *****");
		algorithm.computeTopLevelIn();
		algorithm.computeBottomLevelIn();
		staOrder = algorithm.sortComputationsByBottomLevelIn();
		System.out.println("static scheduling order:");
		for (int i = 0; i < staOrder.size(); i++) {
			System.out.println(" " + i + " -> " + staOrder.get(i).getName()
					+ " (b-level-in=" + staOrder.get(i).getBottomLevelIn()
					+ "; t-level-in=" + staOrder.get(i).getTopLevelIn() + ")");
		}

		for (OperatorDescriptor indexOperator : architecture.getAllOperators()
				.values()) {
			indexOperator.addReceiveCommunication(topCommunication);
			indexOperator.addSendCommunication(topCommunication);
			indexOperator.addOperation(topCommunication);
			indexOperator.addReceiveCommunication(bottomCommunication);
			indexOperator.addSendCommunication(bottomCommunication);
			indexOperator.addOperation(bottomCommunication);
			for (LinkDescriptor indexLink : indexOperator.getInputLinks()) {
				indexLink.addCommunication(topCommunication);
				indexLink.addCommunication(bottomCommunication);
			}
			for (LinkDescriptor indexLink : indexOperator.getOutputLinks()) {
				indexLink.addCommunication(topCommunication);
				indexLink.addCommunication(bottomCommunication);
			}
		}

		/*
		 * Create the set of unscheduledComputation and add all the computations
		 * to it.
		 */
		unscheduledComputations = new HashSet<ComputationDescriptor>();
		for (ComputationDescriptor indexComputation : algorithm
				.getComputations().values()) {
			if ((indexComputation != topComputation)
					&& (indexComputation != bottomComputation)) {
				unscheduledComputations.add(indexComputation);
			}
		}

		int step = 0;
		while (!unscheduledComputations.isEmpty()) {
			ComputationDescriptor criticalNode = chooseNode(unscheduledComputations);
			System.out.println("step " + step + ": schedule "
					+ criticalNode.getName());

			dynOrder.add(criticalNode);
			OperatorDescriptor bestOperator = selectOperator(criticalNode);
			scheduleComputation(criticalNode, bestOperator);

			// schedulingOrder.get(i).setOperator(bestOperator);
			updateTimes();
			System.out.println(" bestOperator" + "->" + bestOperator.getId());
			System.out.println(" startTime" + "=" + criticalNode.getStartTime()
					+ "; finishTime" + "=" + criticalNode.getFinishTime());
			for (CommunicationDescriptor indexCommunication : criticalNode
					.getInputCommunications()) {
				System.out.println(" preceding communication:"
						+ indexCommunication.getName() + " startTimeOnLink="
						+ indexCommunication.getStartTimeOnLink()
						+ "; finishTimeOnLink="
						+ indexCommunication.getFinishTimeOnLink() + "; ALAP="
						+ indexCommunication.getALAP());
			}
			unscheduledComputations.remove(criticalNode);
			step++;
		}

		/* Calculate schedule length and finish times of each operator */
		for (ComputationDescriptor indexComputation : algorithm
				.getComputations().values()) {
			if ((indexComputation != topComputation)
					&& (indexComputation != bottomComputation)) {
				scheduleLength = max(scheduleLength, indexComputation
						.getFinishTime());
			}
		}
		for (OperatorDescriptor indexOperator : architecture.getAllOperators()
				.values()) {
			if (indexOperator.getOperations().size() > 2) {
				usedOperators.add(indexOperator);
				indexOperator.setFinishTime(indexOperator
						.getOccupiedTimeInterval(
								indexOperator
										.getOperation(
												indexOperator.getOperations()
														.size() - 2).getName())
						.getFinishTime());
			}
		}
		return true;
	}

	/**
	 * Choose the critical node to be scheduled.
	 * 
	 * @param nodeSet
	 *            The set of unscheduled nodes
	 * @return The critical node to be scheduled
	 */
	private ComputationDescriptor chooseNode(Set<ComputationDescriptor> nodeSet) {
		ComputationDescriptor bestNode = null;
		HashSet<ComputationDescriptor> readyNodes = new HashSet<ComputationDescriptor>();

		/* Clear the mark of ready */
		for (ComputationDescriptor indexComputation : algorithm
				.getComputations().values()) {
			indexComputation.clearReady();
		}

		/*
		 * topComputation should always marked as scheduled.
		 */
		topComputation.setScheduled();
		for (ComputationDescriptor indexComputation : nodeSet) {
			if (!indexComputation.isScheduled()) {
				indexComputation.setReady();
				for (CommunicationDescriptor indexCommunication : indexComputation
						.getInputCommunications()) {
					if (!algorithm.getComputation(
							indexCommunication.getOrigin()).isScheduled()) {
						indexComputation.clearReady();
						break;
					}
				}
				if (indexComputation.isReady()) {
					readyNodes.add(indexComputation);
				}
			}
		}

		for (ComputationDescriptor indexComputation : readyNodes) {
			int time = 0;
			for (CommunicationDescriptor indexCommunication : indexComputation
					.getInputCommunications()) {
				int drt = algorithm.getComputation(
						indexCommunication.getOrigin()).getFinishTime()
						+ indexCommunication.getCommunicationDuration();
				if (time < drt) {
					time = drt;
				}
			}
			indexComputation.setDataReadyTime(time);
			if (bestNode == null) {
				bestNode = indexComputation;
			} else {
				if ((bestNode.getDataReadyTime() + bestNode.getBottomLevelIn()) < (indexComputation
						.getDataReadyTime() + indexComputation
						.getBottomLevelIn())) {
					bestNode = indexComputation;
				} else if ((bestNode.getDataReadyTime() + bestNode
						.getBottomLevelIn()) == (indexComputation
						.getDataReadyTime() + indexComputation
						.getBottomLevelIn())) {
					if (bestNode.getBottomLevelIn() < indexComputation
							.getBottomLevelIn()) {
						bestNode = indexComputation;
					}
				}
			}
		}
		return bestNode;
	}
}
