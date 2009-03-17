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

package org.ietr.preesm.core.codegen;

import org.ietr.preesm.core.codegen.printer.CodeZoneId;
import org.ietr.preesm.core.codegen.printer.IAbstractPrinter;
import org.sdf4j.model.sdf.SDFEdge;

/**
 * Buffer abstraction
 * 
 * @author mpelcat
 */
public class Buffer extends Parameter {

	// Buffer representing an edge: characteristic names

	/**
	 * The edge this buffer correspond to
	 */
	private SDFEdge edge;

	/**
	 * destination name of the corresponding edge
	 */
	private String destID;

	/**
	 * destination port name of the corresponding edge
	 */
	private String destInputPortID;

	/**
	 * Size of the allocated buffer
	 */
	private Integer size;

	/**
	 * source name of the corresponding edge
	 */
	private String sourceID;

	/**
	 * source port name of the corresponding edge
	 */
	private String sourceOutputPortID;

	/**
	 * Maximal size of a reduced name
	 */
	private static final int maxReducedNameSize = 15;

	public Buffer(String name, Integer size, DataType type, SDFEdge edge,
			AbstractBufferContainer container) {

		super(name, type);
		reduceName(container);
		this.sourceID = null;
		this.destID = null;
		if (edge != null) {
			this.sourceOutputPortID = edge.getSourceInterface().getName();
			this.destInputPortID = edge.getTargetInterface().getName();
		}

		this.size = size;
		this.edge = edge;
	}

	public Buffer(String sourceID, String destID, String sourceOutputPortID,
			String destInputPortID, Integer size, DataType type, SDFEdge edge,
			AbstractBufferContainer container) {

		super(sourceID + sourceOutputPortID + destID + destInputPortID, type);
		reduceName(container);
		this.sourceID = sourceID;
		this.destID = destID;
		this.sourceOutputPortID = sourceOutputPortID;
		this.destInputPortID = destInputPortID;

		this.size = size;

		this.edge = edge;
	}

	@Override
	public void accept(IAbstractPrinter printer, Object currentLocation) {

		currentLocation = printer.visit(this, CodeZoneId.body, currentLocation); // Visit
		// self
	}

	public SDFEdge getEdge() {
		return edge;
	}

	public Integer getSize() {
		return size;
	}

	public String getDestInputPortID() {
		return destInputPortID;
	}

	public String getSourceOutputPortID() {
		return sourceOutputPortID;
	}

	/**
	 * Generates a shorter name fitting the max name size and not existing in
	 * name set
	 */
	public void reduceName(AbstractBufferContainer container) {

		if (container != null) {
			String currentName = getName();
			if (maxReducedNameSize <= currentName.length()
					|| container.existBuffer(currentName,true)) {
				String indexString = "_" + String.valueOf(0);

				for (int index = 0; maxReducedNameSize>=indexString.length(); index++) {
					
					indexString = "_" + String.valueOf(index);
					int reducedNameSizeMinIndex = Math.min(maxReducedNameSize
							- indexString.length(), getName().length());
					currentName = currentName.substring(0,
							reducedNameSizeMinIndex);
					currentName = currentName + indexString;
					if (!container.existBuffer(currentName,true)){
						break;
					}
				}

				setName(currentName);
			}
		}
	}
}
