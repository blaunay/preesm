/*********************************************************
Copyright or � or Copr. IETR/INSA: Matthieu Wipliez, Jonathan Piat,
Maxime Pelcat, Jean-Fran�ois Nezan, Micka�l Raulet

[mwipliez,jpiat,mpelcat,jnezan,mraulet]@insa-rennes.fr

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

package org.ietr.preesm.core.architecture.route;

import java.util.List;

import org.ietr.dftools.architecture.slam.ComponentInstance;
import org.ietr.dftools.architecture.slam.component.Dma;

/**
 * Route step where the sender uses a dma to send data in parallel with its
 * processing
 * 
 * @author mpelcat
 */
public class DmaRouteStep extends MessageRouteStep {

	private Dma dma;

	/**
	 * The route step type determines how the communication will be simulated.
	 */
	public static final String type = "DmaRouteStep";

	public DmaRouteStep(ComponentInstance sender,
			List<ComponentInstance> nodes, ComponentInstance receiver, Dma dma) {
		super(sender, nodes, receiver);
		this.dma = dma;
	}

	/**
	 * The route step type determines how the communication will be simulated.
	 */
	@Override
	public String getType() {
		return type;
	}

	public Dma getDma() {
		return dma;
	}

	@Override
	public String toString() {
		String trace = super.toString();
		trace = trace.substring(0, trace.length() - 1);
		trace += "[" + dma + "]}";
		return trace;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new DmaRouteStep(getSender(), getNodes(), getReceiver(), dma);
	}
}
