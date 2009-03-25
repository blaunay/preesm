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

package org.ietr.preesm.core.architecture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.ietr.preesm.core.architecture.advancedmodel.Fifo;
import org.ietr.preesm.core.architecture.simplemodel.Medium;
import org.ietr.preesm.core.architecture.simplemodel.Operator;
import org.sdf4j.model.AbstractGraph;
import org.sdf4j.model.sdf.SDFAbstractVertex;
import org.sdf4j.model.sdf.SDFEdge;
import org.sdf4j.model.sdf.SDFInterfaceVertex;

/**
 * Architecture based on a fixed number of cores
 * 
 * @author mpelcat
 */
public class MultiCoreArchitecture extends
		AbstractGraph<ArchitectureComponent, Interconnection> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7297201138766758092L;

	/**
	 * List of the component definitions with their IDs.
	 */
	private Map<String, ArchitectureComponentDefinition> architectureComponentDefinitions;

	/**
	 * List of the bus references associated to interfaces
	 */
	private Map<String, BusReference> busReferences;

	/**
	 * name of the archi.
	 */
	private String name;

	/**
	 * main operator and medium.
	 */
	private Operator mainOperator = null;
	private Medium mainMedium = null;

	/**
	 * Creating an empty architecture.
	 */
	public MultiCoreArchitecture(String name) {
		super(new InterconnectionFactory());
		architectureComponentDefinitions = new HashMap<String, ArchitectureComponentDefinition>();
		busReferences = new HashMap<String, BusReference>();

		this.name = name;
	}

	public BusReference getBusReference(String id) {
		return busReferences.get(id);
	}

	/**
	 * Adds the definition of a component and returns it to let the user add
	 * specific properties
	 */
	public ArchitectureComponentDefinition addComponentDefinition(
			ArchitectureComponentType type, String id) {
		if (architectureComponentDefinitions.containsKey(id)) {
			return architectureComponentDefinitions.get(id);
		} else {
			ArchitectureComponentDefinition def = ArchitectureComponentDefinitionFactory
					.createElement(type, id);
			architectureComponentDefinitions.put(def.getId(), def);
			return def;
		}
	}

	/**
	 * Creates and adds a component
	 */
	public ArchitectureComponent addComponent(ArchitectureComponentType type,
			String defId, String name) {
		if (getVertex(name) != null) {
			return getVertex(name);
		} else {
			ArchitectureComponentDefinition newDef = addComponentDefinition(
					type, defId);
			ArchitectureComponent cmp = ArchitectureComponentFactory
					.createElement(newDef, name);
			addVertex(cmp);
			return cmp;
		}
	}

	/**
	 * Adds the reference of a bus and returns it to let the user add specific
	 * properties
	 */
	public BusReference createBusReference(String id) {
		if (busReferences.containsKey(id)) {
			return busReferences.get(id);
		} else {
			BusReference def = new BusReference(id);
			busReferences.put(def.getId(), def);
			return def;
		}
	}

	@Override
	public MultiCoreArchitecture clone() {
		super.clone();
		
		// Creating archi
		MultiCoreArchitecture newArchi = new MultiCoreArchitecture(this.name);
		HashMap<ArchitectureComponent, ArchitectureComponent> matchCopies = new HashMap<ArchitectureComponent, ArchitectureComponent>();

		for (BusReference ref : busReferences.values()) {
			newArchi.createBusReference(ref.getId());
		}

		for (ArchitectureComponentDefinition def : architectureComponentDefinitions
				.values()) {
			newArchi.addComponentDefinition(def.getType(), def.getId());
		}

		for (ArchitectureComponent vertex : vertexSet()) {
			ArchitectureComponent newVertex = (ArchitectureComponent) vertex
					.clone();
			newVertex.fill(vertex, newArchi);
			newArchi.addVertex(newVertex);
			matchCopies.put(vertex, newVertex);
		}

		for (Interconnection edge : edgeSet()) {
			ArchitectureComponent newSource = matchCopies.get(edge.getSource());
			ArchitectureComponent newTarget = matchCopies.get(edge.getTarget());
			Interconnection newEdge = newArchi.addEdge(newSource, newTarget);
			newEdge.setIf1(newSource.getInterface(edge.getIf1()
					.getBusReference()));
			newEdge.setIf2(newTarget.getInterface(edge.getIf2()
					.getBusReference()));
			newEdge.setDirected(edge.isDirected());
		}

		return newArchi;
	}

	/**
	 * Connect two components. If the connection is directed, cmp1 and cmp2 are
	 * source and target components relatively.
	 * 
	 */
	public void connect(ArchitectureComponent cmp1, ArchitectureInterface if1,
			ArchitectureComponent cmp2, ArchitectureInterface if2,
			boolean isDirected) {
		if (!existInterconnection(cmp1, if1, cmp2, if2)) {
			Interconnection itc = this.addEdge(cmp1, cmp2);
			
			if (itc != null) {
				itc.setIf1(if1);
				itc.setIf2(if2);
				itc.setDirected(isDirected);

				if (isDirected) {
					if (cmp1.getType() == ArchitectureComponentType.fifo) {
						if (((Fifo) cmp1).getOutputInterface() == null) {
							((Fifo) cmp1).setOutputInterface(if1);
						}
					} else if (cmp2.getType() == ArchitectureComponentType.fifo) {
						if (((Fifo) cmp2).getInputInterface() == null) {
							((Fifo) cmp2).setInputInterface(if2);
						}
					}
				}
			}
		}
	}

	/**
	 * Interconnections have no direction
	 */
	private boolean existInterconnection(ArchitectureComponent cmp1,
			ArchitectureInterface if1, ArchitectureComponent cmp2,
			ArchitectureInterface if2) {

		Set<Interconnection> iSet = getAllEdges(cmp1, cmp2);
		Interconnection testInter = new Interconnection(cmp1, if1, cmp2, if2);
		Iterator<Interconnection> iterator = iSet.iterator();

		while (iterator.hasNext()) {
			Interconnection currentInter = iterator.next();

			if (currentInter.equals(testInter))
				return true;
		}

		return false;
	}
	
	public Set<Interconnection> undirectedEdgesOf(ArchitectureComponent cmp){
		Set<Interconnection> iSet = new HashSet<Interconnection>();
		
		for(Interconnection incoming : incomingEdgesOf(cmp)){
			if (!incoming.isDirected())
				iSet.add(incoming);
		}
		
		for(Interconnection outgoing : outgoingEdgesOf(cmp)){
			if (!outgoing.isDirected())
				iSet.add(outgoing);
		}
		
		return iSet;
	}

	private boolean existInterconnection(ArchitectureComponent cmp1,
			ArchitectureComponent cmp2) {

		// Traduction in case the components are equal in names but notas java objects
		for(ArchitectureComponent cmp : vertexSet()){
			if(cmp.equals(cmp1)) cmp1 = cmp;
			if(cmp.equals(cmp2)) cmp2 = cmp;
		}
		
		boolean existInterconnection = false;
		Set<Interconnection> connections = getAllEdges(cmp1, cmp2);

		if (connections != null && !connections.isEmpty()) {
			existInterconnection = true;
		} else {
			// In case of undirected edges, tests opposite edges
			Set<Interconnection> reverseConnections = getAllEdges(cmp2, cmp1);
			if (reverseConnections != null) {
				for (Interconnection i : reverseConnections) {
					if (!i.isDirected()) {
						return true;
					}
				}
			}
		}

		return existInterconnection;
	}

	public Medium getMainMedium() {
		if (mainMedium == null) {
			Set<ArchitectureComponent> cmpSet = getComponents(ArchitectureComponentType.medium);
			if (!cmpSet.isEmpty())
				return (Medium) getComponents(ArchitectureComponentType.medium)
						.toArray()[0];
			else
				return null;
		} else {
			return mainMedium;
		}
	}

	public Operator getMainOperator() {
		if (mainOperator == null) {
			Set<ArchitectureComponent> cmpSet = getComponents(ArchitectureComponentType.operator);
			if (!cmpSet.isEmpty())
				return (Operator) getComponents(
						ArchitectureComponentType.operator).toArray()[0];
			else
				return null;
		} else {
			return mainOperator;
		}
	}

	/**
	 * 
	 */
	public Set<Medium> getMedia(Operator op) {
		Set<Medium> media = new HashSet<Medium>();
		Iterator<ArchitectureComponent> iterator = getComponents(
				ArchitectureComponentType.medium).iterator();

		while (iterator.hasNext()) {

			Medium currentMedium = (Medium) iterator.next();

			if (existInterconnection(currentMedium, op)) {
				media.add(currentMedium);
			}
		}

		return media;
	}

	public Set<Medium> getMedia(Operator op1, Operator op2) {

		Set<Medium> intersection = getMedia(op1);
		intersection.retainAll(getMedia(op2));

		return intersection;
	}

	public int getNumberOfOperators() {
		return getComponents(ArchitectureComponentType.operator).size();
	}

	/**
	 * Returns the Component with the given type and name
	 */
	public ArchitectureComponent getComponent(ArchitectureComponentType type,
			String name) {
		Iterator<ArchitectureComponent> iterator = getComponents(type)
				.iterator();

		while (iterator.hasNext()) {
			ArchitectureComponent currentcmp = iterator.next();

			if (currentcmp.getName().compareTo(name) == 0) {
				return (currentcmp);
			}
		}

		return null;
	}

	/**
	 * Returns the Component with the given name
	 */
	public ArchitectureComponent getComponent(String name) {
		return getVertex(name);
	}

	/**
	 * Returns all the components of type type
	 */
	public Set<ArchitectureComponent> getComponents(
			ArchitectureComponentType type) {
		Set<ArchitectureComponent> ops = new ConcurrentSkipListSet<ArchitectureComponent>(
				new ArchitectureComponent.ArchitectureComponentComparator());

		Iterator<ArchitectureComponent> iterator = vertexSet().iterator();

		while (iterator.hasNext()) {
			ArchitectureComponent currentCmp = iterator.next();

			if (currentCmp.getType() == type) {
				ops.add(currentCmp);
			}
		}

		return ops;
	}

	/**
	 * Returns all the components
	 */
	public List<ArchitectureComponent> getComponents() {
		return new ArrayList<ArchitectureComponent>(vertexSet());
	}

	/**
	 * Returns all the components definitions of type type
	 */
	public Set<ArchitectureComponentDefinition> getComponentDefinitions(
			ArchitectureComponentType type) {
		Set<ArchitectureComponentDefinition> opdefs = new HashSet<ArchitectureComponentDefinition>();

		Iterator<ArchitectureComponentDefinition> iterator = architectureComponentDefinitions
				.values().iterator();

		while (iterator.hasNext()) {
			ArchitectureComponentDefinition currentCmp = iterator.next();

			if (currentCmp.getType() == type) {
				opdefs.add(currentCmp);
			}
		}

		return opdefs;
	}

	/**
	 * Returns the component definition with the given id and type
	 */
	public ArchitectureComponentDefinition getComponentDefinition(
			ArchitectureComponentType type, String id) {

		ArchitectureComponentDefinition def = architectureComponentDefinitions
				.get(id);

		if (def != null && def.getType() == type) {
			return def;
		}

		return null;
	}

	/**
	 * Returns all the interconnections
	 */
	public Set<Interconnection> getInterconnections() {
		return edgeSet();
	}

	public String getName() {
		return name;
	}

	public void setMainOperator(String mainOperatorName) {
		Operator o = (Operator) getComponent(
				ArchitectureComponentType.operator, mainOperatorName);
		if (o != null) {
			this.mainOperator = o;
		}
	}

	public void setMainMedium(String mainMediumName) {
		Medium m = (Medium) getComponent(ArchitectureComponentType.medium,
				mainMediumName);
		if (m != null) {
			this.mainMedium = m;
		}

	}

	@Override
	public void update(AbstractGraph<?, ?> observable, Object arg) {
		// TODO Auto-generated method stub

	}

}
