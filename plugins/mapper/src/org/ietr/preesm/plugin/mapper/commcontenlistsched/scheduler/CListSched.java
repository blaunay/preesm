package org.ietr.preesm.plugin.mapper.commcontenlistsched.scheduler;

import java.util.Vector;

import org.ietr.preesm.plugin.mapper.commcontenlistsched.descriptor.AlgorithmDescriptor;
import org.ietr.preesm.plugin.mapper.commcontenlistsched.descriptor.ArchitectureDescriptor;
import org.ietr.preesm.plugin.mapper.commcontenlistsched.descriptor.CommunicationDescriptor;
import org.ietr.preesm.plugin.mapper.commcontenlistsched.descriptor.ComputationDescriptor;
import org.ietr.preesm.plugin.mapper.commcontenlistsched.descriptor.LinkDescriptor;
import org.ietr.preesm.plugin.mapper.commcontenlistsched.descriptor.OperationType;
import org.ietr.preesm.plugin.mapper.commcontenlistsched.descriptor.OperatorDescriptor;

/**
 * This class gives a classic communication contentious list scheduling method.
 * 
 * @author pmu
 */
public class CListSched extends AbstractScheduler {

	public CListSched(AlgorithmDescriptor algorithm,
			ArchitectureDescriptor architecture) {
		super(algorithm);
		// TODO Auto-generated constructor stub
		this.architecture = architecture;
		this.name = "Classic List Scheduling";
	}

	@Override
	public boolean schedule() {
		// TODO Auto-generated method stub
		System.out.println("\n***** " + name + " *****");
		algorithm.computeTopLevel();
		algorithm.computeBottomLevel();
		schedulingOrder = algorithm.sortComputationsByBottomLevel();
		OperatorDescriptor bestOperator = null;
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

		for (int i = 0; i < schedulingOrder.size(); i++) {
			System.out.println(i + ": schedule "
					+ schedulingOrder.get(i).getName() + " (bottom level="
					+ schedulingOrder.get(i).getBottomLevel() + ")");
			bestOperator = selectOperator(schedulingOrder.get(i));

			scheduleComputation(schedulingOrder.get(i), bestOperator);
			// schedulingOrder.get(i).setOperator(bestOperator);
			updateTimes();
			// System.out.println(" bestOperator" + "->" +
			// bestOperator.getId());
			// System.out.println(" startTime" + "="
			// + schedulingOrder.get(i).getStartTime() + "; finishTime"
			// + "=" + schedulingOrder.get(i).getFinishTime());
			// for (CommunicationDescriptor indexCommunication : schedulingOrder
			// .get(i).getPrecedingCommunications()) {
			// System.out.println(" preceding communication:"
			// + indexCommunication.getName() + " startTimeOnLink="
			// + indexCommunication.getStartTimeOnLink()
			// + "; finishTimeOnLink="
			// + indexCommunication.getFinishTimeOnLink() + "; ALAP="
			// + indexCommunication.getALAP());
			// }
		}
		for (int i = 0; i < schedulingOrder.size(); i++) {
			scheduleLength = max(scheduleLength, schedulingOrder.get(i)
					.getFinishTime());
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

	protected OperatorDescriptor selectOperator(
			ComputationDescriptor computation) {
		int bestOperatorFinishTime = Integer.MAX_VALUE;
		OperatorDescriptor bestOperator = null;
		System.out.println(" * select operator for " + computation.getName());
		if (computation.getOperator() == null) {
			// for (OperatorDescriptor indexOperator : architecture
			// .getAllOperators().values()) {
			for (String indexOperatorId : computation.getOperatorSet()) {
				int minOperatorFinishTime = scheduleComputation(computation,
						architecture.getOperator(indexOperatorId));
				if (bestOperatorFinishTime > minOperatorFinishTime) {
					bestOperatorFinishTime = minOperatorFinishTime;
					bestOperator = architecture.getOperator(indexOperatorId);
				}
				// System.out.println(" minOperatorFinishTime="
				// + minOperatorFinishTime);
				// remove inserted communications(source!=destination) and
				// computations;
				for (OperatorDescriptor indexOperator2 : architecture
						.getAllOperators().values()) {
					for (CommunicationDescriptor indexCommunication : computation
							.getInputCommunications()) {
						indexOperator2
								.removeSendCommunication(indexCommunication);
						indexOperator2
								.removeReceiveCommunication(indexCommunication);
						indexOperator2.removeOperation(indexCommunication);
						if (indexCommunication.getSendLink() != null) {
							indexCommunication.getSendLink()
									.removeCommunication(indexCommunication);
							indexCommunication.setSendLink(null);
						}
						if (indexCommunication.getReceiveLink() != null) {
							indexCommunication.getReceiveLink()
									.removeCommunication(indexCommunication);
							indexCommunication.setReceiveLink(null);
						}
						indexCommunication.setExist();
						indexCommunication.clearScheduled();
					}
				}
				architecture.getOperator(indexOperatorId).removeComputation(
						computation);
				architecture.getOperator(indexOperatorId).removeOperation(
						computation);
				restoreTimes();
				computation.clearScheduled();
				computation.setOperator(null);
			}
			// System.out.println(" bestOperatorFinishTime="
			// + bestOperatorFinishTime);
		} else {
			bestOperator = computation.getOperator();
		}
		return bestOperator;
	}

	protected int scheduleComputation(ComputationDescriptor computation,
			OperatorDescriptor operator) {
		int dataReadyTime = 0;
		int maxOperatorFinishTime = 0;
		// System.out.println(" ** schedule computation: " +
		// computation.getName()
		// + " on: " + operator.getId());
		if (computation.getComputationDurations().containsKey(
				operator.getName())
				&& computation.getOperatorSet().contains(operator.getId())) {
			// schedule preceding communications
			for (CommunicationDescriptor indexCommunication : computation
					.getInputCommunications()) {
				if (!indexCommunication.getOrigin().equalsIgnoreCase(
						topComputation.getName())) {
					scheduleCommunication(indexCommunication, operator);
				}
			}
			// calculate data ready time
			for (CommunicationDescriptor indexCommunication : computation
					.getInputCommunications()) {
				if (indexCommunication.isScheduled()) {
					if (dataReadyTime < indexCommunication
							.getFinishTimeOnReceiveOperator()) {
						dataReadyTime = indexCommunication
								.getFinishTimeOnReceiveOperator();
					}
				}
			}
			computation.setDataReadyTime(dataReadyTime);
			// find time interval in operator
			int maxTime = 0;
			for (int i = 0; i < operator.getOperations().size() - 1; i++) {
				maxTime = max(operator.getOccupiedTimeInterval(
						operator.getOperation(i).getName()).getFinishTime(),
						dataReadyTime);
				if (operator.getOccupiedTimeInterval(
						operator.getOperation(i + 1).getName()).getStartTime()
						- maxTime >= computation
						.getTotalComputationDuration(operator)) {
					computation.setStartTime(maxTime);
					computation
							.setFinishTime(maxTime
									+ computation
											.getTotalComputationDuration(operator));
					operator.addComputation(computation);
					operator.addOperation(i + 1, computation);
					operator.addOccupiedTimeInterval(computation.getName(),
							computation.getStartTime(), computation
									.getFinishTime());
					break;
				}
			}

			// Examine the latest finish time of all operators
			// for (OperatorDescriptor indexOperator : architecture
			// .getAllOperators().values()) {
			// if (maxOperatorFinishTime < indexOperator
			// .getOccupiedTimeInterval(
			// indexOperator
			// .getOperation(
			// indexOperator.getOperations()
			// .size() - 2).getName())
			// .getFinishTime()) {
			// maxOperatorFinishTime = indexOperator
			// .getOccupiedTimeInterval(
			// indexOperator.getOperation(
			// indexOperator.getOperations()
			// .size() - 2).getName())
			// .getFinishTime();
			// }
			// }
			//
			// give the finish time of the computation on this operator
			maxOperatorFinishTime = computation.getFinishTime();
			computation.setScheduled();
			computation.setOperator(operator);
		} else {
			maxOperatorFinishTime = Integer.MAX_VALUE;
		}
		return maxOperatorFinishTime;
	}

	protected void scheduleCommunication(CommunicationDescriptor communication,
			OperatorDescriptor destinationOperator) {
		ComputationDescriptor sourceComputation = algorithm
				.getComputation(communication.getOrigin());
		// System.out.println(" *** schedule communication: "
		// + communication.getName());
		if (sourceComputation.isScheduled()) {
			OperatorDescriptor sourceOperator = sourceComputation.getOperator();
			if (sourceOperator == destinationOperator) {
				// System.out
				// .println(" sourceOperator == destinationOperator");
				communication.clearExist();
				communication.setStartTimeOnSendOperator(sourceComputation
						.getFinishTime());
				communication.setFinishTimeOnSendOperator();
				communication.setStartTimeOnLink(sourceComputation
						.getFinishTime());
				communication.setFinishTimeOnLink(sourceComputation
						.getFinishTime());
				communication.setStartTimeOnReceiveOperator(sourceComputation
						.getFinishTime());
				communication.setFinishTimeOnReceiveOperator();
			} else {
				// System.out
				// .println(" sourceOperator != destinationOperator");
				// RouteDescriptor route = findRoute(sourceOperator,
				// destinationOperator);
				LinkDescriptor sendLink = sourceOperator.getOutputLink(0);
				LinkDescriptor receiveLink = destinationOperator
						.getInputLink(0);
				Vector<CommunicationDescriptor> sendCommunicationList = sendLink
						.getCommunications();
				Vector<CommunicationDescriptor> receiveCommunicationList = receiveLink
						.getCommunications();
				// if (sendLink != receiveLink) {
				// find slot in links
				int indexCommunicationOnSendLink = 0;
				int indexCommunicationOnReceiveLink = 0;
				int infSendCommunicationTime = 0;
				int supSendCommunicationTime = 0;
				int infReceiveCommunicationTime = 0;
				int supReceiveCommunicationTime = 0;
				int infCommunicationTime = 0;
				int supCommunicationTime = 0;
				while (indexCommunicationOnSendLink < sendCommunicationList
						.size()
						&& indexCommunicationOnReceiveLink < receiveCommunicationList
								.size()) {
					if (sourceOperator.getSendCommunications().contains(
							sendCommunicationList
									.get(indexCommunicationOnSendLink))) {
						infSendCommunicationTime = max(algorithm
								.getComputation(communication.getOrigin())
								.getFinishTime()
								+ communication.getSendOverhead(),
								sendCommunicationList.get(
										indexCommunicationOnSendLink)
										.getFinishTimeOnSendOperator()
										+ communication.getSendOverhead(),
								sendCommunicationList.get(
										indexCommunicationOnSendLink)
										.getFinishTimeOnLink());
					} else {
						infSendCommunicationTime = max(algorithm
								.getComputation(communication.getOrigin())
								.getFinishTime()
								+ communication.getSendOverhead(),
								sendCommunicationList.get(
										indexCommunicationOnSendLink)
										.getFinishTimeOnReceiveOperator()
										+ communication.getSendOverhead(),
								sendCommunicationList.get(
										indexCommunicationOnSendLink)
										.getFinishTimeOnLink());
					}
					supSendCommunicationTime = sendCommunicationList.get(
							indexCommunicationOnSendLink + 1)
							.getStartTimeOnLink();
					infReceiveCommunicationTime = max(algorithm.getComputation(
							communication.getOrigin()).getFinishTime()
							+ communication.getSendOverhead(),
							receiveCommunicationList.get(
									indexCommunicationOnReceiveLink)
									.getFinishTimeOnLink());
					supReceiveCommunicationTime = receiveCommunicationList.get(
							indexCommunicationOnReceiveLink + 1)
							.getStartTimeOnLink();
					infCommunicationTime = max(infSendCommunicationTime,
							infReceiveCommunicationTime);
					supCommunicationTime = min(supSendCommunicationTime,
							supReceiveCommunicationTime);
					if (infCommunicationTime
							+ communication.getCommunicationDuration() <= supCommunicationTime) {
						break;
					} else {
						if (infSendCommunicationTime
								+ communication.getCommunicationDuration() > supSendCommunicationTime) {
							indexCommunicationOnSendLink++;
						} else {
							if (infReceiveCommunicationTime
									+ communication.getCommunicationDuration() > supReceiveCommunicationTime) {
								indexCommunicationOnReceiveLink++;
							} else {
								if (infSendCommunicationTime < infReceiveCommunicationTime) {
									indexCommunicationOnSendLink++;
								} else {
									indexCommunicationOnReceiveLink++;
								}
							}
						}
					}
				}

				// set new start times in source operator and links
				int indexOperationOnSourceOperator = max(sourceOperator
						.getOperations().indexOf(
								sendCommunicationList
										.get(indexCommunicationOnSendLink)),
						sourceOperator.getOperations().indexOf(
								algorithm.getComputation(communication
										.getOrigin())));
				if (sourceOperator.getOperation(indexOperationOnSourceOperator)
						.getType() == OperationType.Computation) {
					communication
							.setStartTimeOnSendOperator(((ComputationDescriptor) sourceOperator
									.getOperation(indexOperationOnSourceOperator))
									.getFinishTime());
				} else {
					if (sourceOperator
							.getSendCommunications()
							.contains(
									(CommunicationDescriptor) sourceOperator
											.getOperation(indexOperationOnSourceOperator))) {
						communication
								.setStartTimeOnSendOperator(((CommunicationDescriptor) sourceOperator
										.getOperation(indexOperationOnSourceOperator))
										.getFinishTimeOnSendOperator());
					} else {
						communication
								.setStartTimeOnSendOperator(((CommunicationDescriptor) sourceOperator
										.getOperation(indexOperationOnSourceOperator))
										.getFinishTimeOnReceiveOperator());
					}
				}
				communication.setStartTimeOnLink(infCommunicationTime);
				sendCommunicationList
						.get(indexCommunicationOnSendLink + 1)
						.setStartTimeOnLink(
								max(
										communication.getFinishTimeOnLink(),
										sendCommunicationList
												.get(
														indexCommunicationOnSendLink + 1)
												.getStartTimeOnLink()));
				receiveCommunicationList
						.get(indexCommunicationOnReceiveLink + 1)
						.setStartTimeOnLink(
								max(
										communication.getFinishTimeOnLink(),
										receiveCommunicationList
												.get(
														indexCommunicationOnReceiveLink + 1)
												.getStartTimeOnLink()));

				// // Check delay
				// int delayStartTime = 0;
				// int delayDuration = 0;
				// boolean needDelay = true;
				// if (sourceOperator.getOperation(
				// indexOperationOnSourceOperator + 1).getType() ==
				// OperationType.Computation) {
				// if (((ComputationDescriptor) sourceOperator
				// .getOperation(indexOperationOnSourceOperator + 1))
				// .getStartTime() >= communication
				// .getFinishTimeOnSendOperator()) {
				// needDelay = false;
				// } else {
				// delayStartTime = ((ComputationDescriptor) sourceOperator
				// .getOperation(indexOperationOnSourceOperator + 1))
				// .getStartTime();
				// delayDuration = communication
				// .getFinishTimeOnSendOperator()
				// - delayStartTime;
				// }
				// } else {
				// if (sourceOperator
				// .getSendCommunications()
				// .contains(
				// (CommunicationDescriptor) sourceOperator
				// .getOperation(indexOperationOnSourceOperator + 1))) {
				// if (((CommunicationDescriptor) sourceOperator
				// .getOperation(indexOperationOnSourceOperator + 1))
				// .getStartTimeOnSendOperator() >= communication
				// .getFinishTimeOnSendOperator()) {
				// needDelay = false;
				// } else {
				// delayStartTime = ((CommunicationDescriptor)
				// sourceOperator
				// .getOperation(indexOperationOnSourceOperator + 1))
				// .getStartTimeOnSendOperator();
				// delayDuration = communication
				// .getFinishTimeOnSendOperator()
				// - delayStartTime;
				// }
				// } else {
				// if (((CommunicationDescriptor) sourceOperator
				// .getOperation(indexOperationOnSourceOperator + 1))
				// .getStartTimeOnReceiveOperator() >= communication
				// .getFinishTimeOnSendOperator()) {
				// needDelay = false;
				// } else {
				// delayStartTime = ((CommunicationDescriptor)
				// sourceOperator
				// .getOperation(indexOperationOnSourceOperator + 1))
				// .getStartTimeOnReceiveOperator();
				// delayDuration = communication
				// .getFinishTimeOnSendOperator()
				// - delayStartTime;
				// }
				// }
				// }
				// if (needDelay) {
				// // delay operations
				// // System.out.println(" delay operations for: "
				// // + delayDuration + " after: " + delayStartTime);
				// for (OperatorDescriptor indexOperator : architecture
				// .getAllOperators().values()) {
				// for (int i = 1; i < indexOperator.getOperations()
				// .size() - 1; i++) {
				// if (indexOperator.getOperation(i).getType() ==
				// OperationType.Computation) {
				// if (((ComputationDescriptor) indexOperator
				// .getOperation(i)).getStartTime() >= delayStartTime) {
				// ((ComputationDescriptor) indexOperator
				// .getOperation(i))
				// .setStartTime(((ComputationDescriptor) indexOperator
				// .getOperation(i))
				// .getStartTime()
				// + delayDuration);
				// }
				// } else {
				// // check only the receive communication to avoid
				// // duplicate checking
				// if (indexOperator
				// .getReceiveCommunications()
				// .contains(
				// (CommunicationDescriptor) indexOperator
				// .getOperation(i))) {
				// if (((CommunicationDescriptor) indexOperator
				// .getOperation(i))
				// .getStartTimeOnSendOperator() >= delayStartTime) {
				// ((CommunicationDescriptor) indexOperator
				// .getOperation(i))
				// .setStartTimeOnSendOperator(((CommunicationDescriptor)
				// indexOperator
				// .getOperation(i))
				// .getStartTimeOnSendOperator()
				// + delayDuration);
				// }
				// if (((CommunicationDescriptor) indexOperator
				// .getOperation(i))
				// .getStartTimeOnLink() >= delayStartTime) {
				// ((CommunicationDescriptor) indexOperator
				// .getOperation(i))
				// .setStartTimeOnLink(((CommunicationDescriptor)
				// indexOperator
				// .getOperation(i))
				// .getStartTimeOnLink()
				// + delayDuration);
				// }
				// if (((CommunicationDescriptor) indexOperator
				// .getOperation(i))
				// .getStartTimeOnReceiveOperator() >= delayStartTime) {
				// ((CommunicationDescriptor) indexOperator
				// .getOperation(i))
				// .setStartTimeOnReceiveOperator(((CommunicationDescriptor)
				// indexOperator
				// .getOperation(i))
				// .getStartTimeOnReceiveOperator()
				// + delayDuration);
				// }
				// }
				// }
				// }
				// }
				// }

				// find slot in destination operator
				int indexOperationOnDestinationOperator = 0;
				if (destinationOperator.getOperations()
						.contains(
								sendCommunicationList
										.get(indexCommunicationOnSendLink))) {
					indexOperationOnDestinationOperator = destinationOperator
							.getOperations().indexOf(
									sendCommunicationList
											.get(indexCommunicationOnSendLink));
				}
				int maxTime = 0;
				for (int i = indexOperationOnDestinationOperator; i < destinationOperator
						.getOperations().size() - 1; i++) {
					maxTime = max(destinationOperator.getOccupiedTimeInterval(
							destinationOperator.getOperation(i).getName())
							.getFinishTime()
							+ communication.getReceiveInvolvement(),
							communication.getFinishTimeOnLink());
					if (destinationOperator.getOccupiedTimeInterval(
							destinationOperator.getOperation(i + 1).getName())
							.getStartTime()
							- maxTime >= communication.getReceiveOverhead()) {
						communication.setStartTimeOnReceiveOperator(maxTime
								- communication.getReceiveInvolvement());
						indexOperationOnDestinationOperator = i;
						break;
					}
				}

				// insert communication
				// if (!sourceOperator.getSendCommunications().contains(
				// communication)) {
				sourceOperator.addSendCommunication(communication);
				sourceOperator.addOperation(indexOperationOnSourceOperator + 1,
						communication);
				sourceOperator.addOccupiedTimeInterval(communication.getName(),
						communication.getStartTimeOnSendOperator(),
						communication.getFinishTimeOnSendOperator());
				// }
				destinationOperator.addReceiveCommunication(communication);
				destinationOperator.addOperation(
						indexOperationOnDestinationOperator + 1, communication);
				destinationOperator.addOccupiedTimeInterval(communication
						.getName(), communication
						.getStartTimeOnReceiveOperator(), communication
						.getFinishTimeOnReceiveOperator());
				sendLink.addCommunication(indexCommunicationOnSendLink + 1,
						communication);
				if (sendLink != receiveLink) {
					receiveLink.addCommunication(
							indexCommunicationOnReceiveLink + 1, communication);
				}
				communication.setSendLink(sendLink);
				communication.setReceiveLink(receiveLink);
			}
			communication.setScheduled();
		} else {
			System.out.println(" communication name: "
					+ communication.getName());
			System.out.println(" source computation has not been scheduled");
		}
	}

	// protected RouteDescriptor findRoute(OperatorDescriptor sourceOperator,
	// OperatorDescriptor destinationOperator) {
	// RouteDescriptor route = new RouteDescriptor();
	// route.addLink(sourceOperator.getOutputLinks().get(0));
	// route.addLink(destinationOperator.getInputLinks().get(0));
	// return route;
	// }

}
