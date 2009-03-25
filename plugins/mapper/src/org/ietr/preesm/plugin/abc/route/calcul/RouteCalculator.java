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

package org.ietr.preesm.plugin.abc.route.calcul;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.ietr.preesm.core.architecture.ArchitectureComponent;
import org.ietr.preesm.core.architecture.ArchitectureComponentType;
import org.ietr.preesm.core.architecture.Interconnection;
import org.ietr.preesm.core.architecture.MultiCoreArchitecture;
import org.ietr.preesm.core.architecture.route.AbstractRouteStep;
import org.ietr.preesm.core.architecture.route.Route;
import org.ietr.preesm.core.architecture.route.RouteStepFactory;
import org.ietr.preesm.core.architecture.simplemodel.AbstractNode;
import org.ietr.preesm.core.architecture.simplemodel.Operator;
import org.ietr.preesm.core.tools.PreesmLogger;
import org.ietr.preesm.plugin.mapper.model.MapperDAGEdge;
import org.ietr.preesm.plugin.mapper.model.MapperDAGVertex;

/**
 * This class can evaluate a given transfer and choose the best route between
 * two operators
 * 
 * @author mpelcat
 */
public class RouteCalculator {

	private static RouteCalculator singleton = null;

	private MultiCoreArchitecture archi;

	private RoutingTable table = null;

	private RouteStepFactory stepFactory = null;

	static public RouteCalculator getInstance(MultiCoreArchitecture archi) {
		if (singleton == null) {
			singleton = new RouteCalculator(archi);
		}
		return singleton;
	}

	/**
	 * Constructor from a given architecture
	 */
	public RouteCalculator(MultiCoreArchitecture archi) {

		this.archi = archi;
		table = new RoutingTable();
		stepFactory = new RouteStepFactory(archi);

		createRouteSteps();
		createRoutes();
	}

	/**
	 * Creating recursively the route steps from the architecture.
	 */
	private void createRouteSteps() {
		PreesmLogger.getLogger().log(Level.INFO, "creating route steps.");

		for (ArchitectureComponent c : archi
				.getComponents(ArchitectureComponentType.operator)) {
			Operator o = (Operator) c;

			createRouteSteps(o);
		}
	}
	
	private ArchitectureComponent getOtherEnd(Interconnection i, ArchitectureComponent c){
		if(i.getTarget() != c)
			return i.getTarget();
		else
			return i.getSource();
	}

	private void createRouteSteps(Operator source) {

		// Iterating on outgoing and undirected edges
		Set<Interconnection> outgoingAndUndirected = new HashSet<Interconnection>();
		outgoingAndUndirected.addAll(archi.undirectedEdgesOf(source));
		outgoingAndUndirected.addAll(archi.outgoingEdgesOf(source));

		for (Interconnection i : outgoingAndUndirected) {
			if (getOtherEnd(i,source).isNode()) {
				AbstractNode node = (AbstractNode)getOtherEnd(i,source);

				List<AbstractNode> alreadyVisitedNodes = new ArrayList<AbstractNode>();
				alreadyVisitedNodes.add(node);
				exploreRoute(source, node, alreadyVisitedNodes);
			}
		}
	}

	private void exploreRoute(Operator source, AbstractNode node,
			List<AbstractNode> alreadyVisitedNodes) {

		// Iterating on outgoing and undirected edges
		Set<Interconnection> outgoingAndUndirected = new HashSet<Interconnection>();
		outgoingAndUndirected.addAll(archi.undirectedEdgesOf(node));
		outgoingAndUndirected.addAll(archi.outgoingEdgesOf(node));

		for (Interconnection i : outgoingAndUndirected) {
			if (getOtherEnd(i,node).isNode()) {
				AbstractNode newNode = (AbstractNode) getOtherEnd(i,node);
				if (!alreadyVisitedNodes.contains(newNode)) {
					List<AbstractNode> newAlreadyVisitedNodes = new ArrayList<AbstractNode>(
							alreadyVisitedNodes);
					newAlreadyVisitedNodes.add(newNode);
					exploreRoute(source, node, newAlreadyVisitedNodes);
				}
			} else if (getOtherEnd(i,node) instanceof Operator && getOtherEnd(i,node) != source) {
				Operator target = (Operator) getOtherEnd(i,node);
				AbstractRouteStep step = stepFactory.getRouteStep(source,
						alreadyVisitedNodes, target);
				table.addRoute(source, target, new Route(step));
			}
		}
	}

	/**
	 * Building recursively the routes between the cores.
	 */
	private void createRoutes() {
		PreesmLogger.getLogger().log(Level.INFO, "Initializing routing table.");

	}

	/**
	 * Returns true if route1 better than route2
	 */
	public boolean compareRoutes(Route route1, Route route2) {

		return route1.size() < route2.size();
	}

	/**
	 * Choosing a route between 2 operators
	 */
	public Route getRoute(Operator op1, Operator op2) {
		Route r = table.getRoute(op1, op2, 0);
		
		if(r==null){
			PreesmLogger.getLogger().log(Level.SEVERE,"Did not find a route between " + op1 + " and " + op2 + ".");
		}
		
		return r;
	}

	/**
	 * Choosing a route between 2 operators
	 */
	public Route getRoute(MapperDAGEdge edge) {
		MapperDAGVertex source = (MapperDAGVertex) edge.getSource();
		MapperDAGVertex target = (MapperDAGVertex) edge.getTarget();
		Operator sourceOp = source.getImplementationVertexProperty()
				.getEffectiveOperator();
		Operator targetOp = target.getImplementationVertexProperty()
				.getEffectiveOperator();
		return getRoute(sourceOp, targetOp);
	}

}
