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
package org.ietr.preesm.plugin.mapper.model.implementation;

import java.util.Iterator;
import java.util.List;

import org.ietr.preesm.core.architecture.ArchitectureComponent;
import org.ietr.preesm.plugin.abc.order.Schedule;
import org.ietr.preesm.plugin.abc.order.SchedulingOrderManager;
import org.ietr.preesm.plugin.abc.transaction.AddPrecedenceEdgeTransaction;
import org.ietr.preesm.plugin.abc.transaction.Transaction;
import org.ietr.preesm.plugin.abc.transaction.TransactionManager;
import org.ietr.preesm.plugin.mapper.model.MapperDAG;
import org.ietr.preesm.plugin.mapper.model.MapperDAGVertex;

/**
 * The edge adder automatically generates edges between vertices
 * successive on a single operator. It can also remove all the edges of
 * type PrecedenceEdgeAdder from the graph
 * 
 * @author mpelcat   
 */
public class PrecedenceEdgeAdder {

	private SchedulingOrderManager orderManager;

	public PrecedenceEdgeAdder(SchedulingOrderManager orderManager) {
		super();

		this.orderManager = orderManager;
	}

	/**
	 * Adds all necessary schedule edges to an implementation respecting
	 * the order given by the scheduling order manager.
	 */
	public void addPrecedenceEdge(MapperDAG implementation, TransactionManager transactionManager, MapperDAGVertex refVertex) {

			Schedule schedule = orderManager
					.getSchedule(refVertex.getImplementationVertexProperty().getEffectiveComponent());

			MapperDAGVertex prevVertex = schedule.getPreviousVertex(refVertex);

			if (implementation.getAllEdges(prevVertex, refVertex).isEmpty()) {
				// Adds a transaction
				Transaction transaction = new AddPrecedenceEdgeTransaction(implementation,prevVertex,refVertex);
				transactionManager.add(transaction,refVertex);
			}

		// Executes the transactions
		transactionManager.executeTransactionList();
	}

	/**
	 * Adds all necessary schedule edges to an implementation respecting
	 * the order given by the scheduling order manager.
	 */
	public void addPrecedenceEdges(MapperDAG implementation, TransactionManager transactionManager) {

		Iterator<ArchitectureComponent> schedIt = orderManager.getArchitectureComponents()
				.iterator();

		// Iterates the schedules
		while (schedIt.hasNext()) {
			List<MapperDAGVertex> schedule = orderManager
					.getScheduleList(schedIt.next());

			Iterator<MapperDAGVertex> schedit = schedule.iterator();

			MapperDAGVertex src;

			// Iterates all vertices in each schedule
			if (schedit.hasNext()) {
				MapperDAGVertex dst = schedit.next();

				while (schedit.hasNext()) {

					src = dst;
					dst = schedit.next();

					if (implementation.getAllEdges(src, dst).isEmpty()) {
						// Adds a transaction
						Transaction transaction = new AddPrecedenceEdgeTransaction(implementation,src,dst);
						transactionManager.add(transaction,null);
					}
				}
			}
		}

		// Executes the transactions
		transactionManager.executeTransactionList();
	}

}
