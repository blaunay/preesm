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

import org.ietr.preesm.plugin.mapper.listsched.descriptor.AlgorithmDescriptor;
import org.ietr.preesm.plugin.mapper.listsched.descriptor.ArchitectureDescriptor;
import org.ietr.preesm.plugin.mapper.listsched.descriptor.CommunicationDescriptor;
import org.ietr.preesm.plugin.mapper.listsched.descriptor.LinkDescriptor;
import org.ietr.preesm.plugin.mapper.listsched.descriptor.OperatorDescriptor;

/**
 * This class gives a classic static list scheduling method with Critical Child,
 * Communication Delay and nodes sorted by input bottom level.
 * 
 * @author pmu
 */
public class CSListSchedCcCdBlin extends CSListSchedCcCd {

	/**
	 * Constructs the scheduler with algorithm and architecture.
	 * 
	 * @param algorithm
	 *            Algorithm descriptor
	 * @param architecture
	 *            Architecture descriptor
	 */
	public CSListSchedCcCdBlin(AlgorithmDescriptor algorithm,
			ArchitectureDescriptor architecture) {
		super(algorithm, architecture);
		// TODO Auto-generated constructor stub
		this.name = "Classic Static List Scheduling With Critical Child, Communication Delay And Nodes Sorted By Input Bottom Level";
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
		OperatorDescriptor bestOperator = null;
		for (OperatorDescriptor indexOperator : architecture.getAllOperators()
				.values()) {
			indexOperator.addReceiveCommunication(topCommunication);
			indexOperator.addSendCommunication(topCommunication);
			indexOperator.addOperation(topCommunication);
			indexOperator.addReceiveCommunication(bottomCommunication);
			indexOperator.addSendCommunication(bottomCommunication);
			indexOperator.addOperation(bottomCommunication);
//			for (LinkDescriptor indexLink : indexOperator.getInputLinks()) {
//				indexLink.addCommunication(topCommunication);
//				indexLink.addCommunication(bottomCommunication);
//			}
//			for (LinkDescriptor indexLink : indexOperator.getOutputLinks()) {
//				indexLink.addCommunication(topCommunication);
//				indexLink.addCommunication(bottomCommunication);
//			}
		}
		for (LinkDescriptor indexLink : architecture.getAllLinks()
				.values()) {
			indexLink.addCommunication(topCommunication);
			indexLink.addCommunication(bottomCommunication);
		}

		for (int i = 0; i < staOrder.size(); i++) {
			System.out.println(i + ": schedule " + staOrder.get(i).getName());
			bestOperator = selectOperator(staOrder.get(i));

			scheduleComputation(staOrder.get(i), bestOperator, false);
			// schedulingOrder.get(i).setOperator(bestOperator);
			updateTimes();
			System.out.println(" bestOperator" + "->" + bestOperator.getId());
			System.out.println(" startTime" + "="
					+ staOrder.get(i).getStartTime() + "; finishTime" + "="
					+ staOrder.get(i).getFinishTime());
			for (CommunicationDescriptor indexCommunication : staOrder.get(i)
					.getInputCommunications()) {
				System.out.println(" preceding communication:"
						+ indexCommunication.getName() + " startTimeOnLink="
						+ indexCommunication.getStartTimeOnLink()
						+ "; finishTimeOnLink="
						+ indexCommunication.getFinishTimeOnLink() + "; ALAP="
						+ indexCommunication.getALAP());
			}
		}
		for (int i = 0; i < staOrder.size(); i++) {
			scheduleLength = max(scheduleLength, staOrder.get(i)
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

}
