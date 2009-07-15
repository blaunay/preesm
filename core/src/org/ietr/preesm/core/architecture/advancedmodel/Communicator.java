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
package org.ietr.preesm.core.architecture.advancedmodel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.ietr.preesm.core.architecture.ArchitectureComponent;
import org.ietr.preesm.core.architecture.ArchitectureComponentType;

/**
 * a Communicator (example: an edma)
 * 
 * @author pmu
 */
public class Communicator extends ArchitectureComponent implements
		ICommunicationPerformer {

	/**
	 * ID used to reference the element in a property bean in case of a
	 * computation vertex
	 */
	public static final String propertyBeanName = "communicator";

	/**
	 * This communicator can be configured by some processors by using some
	 * time. The used times for different processors are stored in setupTimes.
	 */
	private Map<String, Double> setupTimes;

	private Set<String> accessTerminalNames;

	public Communicator(String name, CommunicatorDefinition type) {
		super(name, type);
		setupTimes = new HashMap<String, Double>();
		accessTerminalNames = new HashSet<String>();
	}

	public void addSetupTime(String procName, double time) {
		setupTimes.put(procName, time);
	}

	public Set<String> getProcessorNames() {
		return setupTimes.keySet();
	}

	public double getSetupTime(String procName) {
		return setupTimes.get(procName);
	}

	public Map<String, Double> getSetupTimes() {
		return setupTimes;
	}

	public void setSetupTimes(Map<String, Double> setupTimes) {
		this.setupTimes = setupTimes;
	}

	public void addAccessTerminalName(String terminalName) {
		accessTerminalNames.add(terminalName);
	}

	public Set<String> getAccessTerminalNames() {
		return accessTerminalNames;
	}

	public ArchitectureComponentType getType() {
		return ArchitectureComponentType.communicator;
	}

	/*@Override
	public ArchitectureComponent clone() {
		Communicator com = new Communicator(getName(), null);
		com.getSetupTimes().putAll(setupTimes);
		return com;
	}*/

	public boolean isNode() {
		return true;
	}
}
