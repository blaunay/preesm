/*******************************************************************************
 * Copyright or © or Copr. IETR/INSA: Maxime Pelcat, Jean-François Nezan,
 * Karol Desnos, Julien Heulot, Clément Guy
 * 
 * [mpelcat,jnezan,kdesnos,jheulot,cguy]@insa-rennes.fr
 * 
 * This software is a computer program whose purpose is to prototype
 * parallel applications.
 * 
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info". 
 * 
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability. 
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or 
 * data to be ensured and,  more generally, to use and operate it in the 
 * same conditions as regards security. 
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 ******************************************************************************/
package org.ietr.preesm.core.algorithm.visitors;

import java.util.HashSet;
import java.util.Iterator;

import org.ietr.dftools.algorithm.model.dag.DAGEdge;
import org.ietr.dftools.algorithm.model.dag.DAGVertex;
import org.ietr.dftools.algorithm.model.dag.DirectedAcyclicGraph;
import org.ietr.dftools.algorithm.model.visitors.IGraphVisitor;
import org.ietr.dftools.algorithm.model.visitors.SDF4JException;
import org.ietr.dftools.architecture.slam.ComponentInstance;

/**
 * Visitor to identify the inter-core communications of a mapped DAG. This
 * visitor is inspired by CommunicationRouter.routeAll() implementation.
 * 
 * @author kdesnos
 * 
 */
public class CommunicationIdentifierVisitor implements
		IGraphVisitor<DirectedAcyclicGraph, DAGVertex, DAGEdge> {

	protected HashSet<DAGEdge> interCoreComm;

	/**
	 * Constructor of the CommunicationIdentifier.
	 */
	public CommunicationIdentifierVisitor() {
		interCoreComm = new HashSet<DAGEdge>();
	}

	/**
	 * Return the result of the visitor algorithm.
	 * 
	 * @return list containing edges that are inter-core communications
	 */
	public HashSet<DAGEdge> getResult() {
		return interCoreComm;
	}

	@Override
	public void visit(DAGEdge currentEdge) {
		// First, we check that both source and target vertices are tasks
		// i.e. we skip existing send/receive nodes
		if (currentEdge.getSource().getPropertyBean().getValue("vertexType")
				.toString().equals("task")
				&& currentEdge.getTarget().getPropertyBean()
						.getValue("vertexType").toString().equals("task")) {

			ComponentInstance sourceComponent = (ComponentInstance) (currentEdge
					.getSource()).getPropertyBean().getValue("Operator");
			ComponentInstance targetComponent = (ComponentInstance) (currentEdge
					.getTarget()).getPropertyBean().getValue("Operator");

			if (sourceComponent != null && targetComponent != null) {
				if (!sourceComponent.equals(targetComponent)) {
					// This code is reached only if the current edge is an
					// inter-core communication
					interCoreComm.add(currentEdge);
				}
			}
		}
	}

	@Override
	public void visit(DirectedAcyclicGraph dag) throws SDF4JException {
		// We iterate the edges
		Iterator<DAGEdge> iterator = dag.edgeSet().iterator();

		while (iterator.hasNext()) {
			DAGEdge currentEdge = iterator.next();
			currentEdge.accept(this);
		}
	}

	@Override
	public void visit(DAGVertex dagVertex) throws SDF4JException {
		// Nothing to do here for this visitor

	}

}
