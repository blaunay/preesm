package org.ietr.preesm.experiment.model.pimemoc.util;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.dialogs.IInputValidator;
import org.ietr.preesm.experiment.model.pimemoc.AbstractVertex;
import org.ietr.preesm.experiment.model.pimemoc.Port;

/**
 * This validator is used to check whether a port in a vertex already has a
 * given name.
 * 
 * @author kdesnos
 * 
 */
public class PortNameValidator implements IInputValidator {

	protected AbstractVertex vertex;
	protected Port renamedPort;
	protected String kind;
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
	public PortNameValidator(AbstractVertex vertex, Port renamedPort,
			String kind) {
		this.vertex = vertex;
		this.renamedPort = renamedPort;
		this.kind = kind;

		switch (kind) {
		case "input":
			this.portsNames = new HashSet<>(vertex.getInputPorts().size());
			for (Port port : vertex.getInputPorts()) {
				this.portsNames.add(port.getName());
			}
			break;
		case "output":
			this.portsNames = new HashSet<>(vertex.getOutputPorts().size());
			for (Port port : vertex.getOutputPorts()) {
				this.portsNames.add(port.getName());
			}
			break;
		default:
			this.portsNames = new HashSet<String>(0);
		}
		
		if(this.renamedPort != null){
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
			message = "/!\\ An "+kind+" port with name " + newPortName
					+ " already exists /!\\";
			return message;
		}

		return message;
	}

}
