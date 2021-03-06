/*******************************************************************************
 * Copyright or © or Copr. IETR/INSA: Maxime Pelcat, Jean-François Nezan,
 * Karol Desnos, Julien Heulot
 * 
 * [mpelcat,jnezan,kdesnos,jheulot]@insa-rennes.fr
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
package org.ietr.preesm.experiment.model.pimm.util;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.dialogs.IInputValidator;
import org.ietr.preesm.experiment.model.pimm.AbstractActor;
import org.ietr.preesm.experiment.model.pimm.Port;

/**
 * This validator is used to check whether a port in a vertex already has a
 * given name.
 * 
 * @author kdesnos
 * 
 */
public class PortNameValidator implements IInputValidator {

	protected AbstractActor vertex;
	protected Port renamedPort;
	protected Set<String> portsNames;

	/**
	 * Default constructor of the {@link PortNameValidator}
	 * 
	 * @param vertex
	 *            the port to which we add/rename a port
	 * @param renamedPort
	 *            the renamed port, or <code>null</code> if not a rename
	 *            operation
	 * @param kind
	 *            the kind of the port
	 */
	public PortNameValidator(AbstractActor vertex, Port renamedPort) {
		this.vertex = vertex;
		this.renamedPort = renamedPort;

		// Create the list of already existing names
		this.portsNames = new HashSet<>();

		for (Port port : vertex.getConfigInputPorts()) {
			this.portsNames.add(port.getName());
		}

		for (Port port : vertex.getConfigOutputPorts()) {
			this.portsNames.add(port.getName());
		}

		for (Port port : vertex.getDataInputPorts()) {
			this.portsNames.add(port.getName());
		}

		for (Port port : vertex.getDataOutputPorts()) {
			this.portsNames.add(port.getName());
		}

		if (this.renamedPort != null) {
			this.portsNames.remove(renamedPort.getName());
		}
	}

	@Override
	public String isValid(String newPortName) {
		String message = null;
		// Check if the name is not empty
		if (newPortName.length() < 1) {
			message = "/!\\ Port name cannot be empty /!\\";
			return message;
		}

		// Check if the name contains a space
		if (newPortName.contains(" ")) {
			message = "/!\\ Port name must not contain spaces /!\\";
			return message;
		}

		// Check if no other port has the same name
		if (portsNames.contains(newPortName)) {
			message = "/!\\ A port with name " + newPortName
					+ " already exists /!\\";
			return message;
		}

		return message;
	}

}
