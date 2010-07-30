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

package org.ietr.preesm.core.architecture.route;

import java.util.logging.Level;

import org.ietr.preesm.core.architecture.simplemodel.Medium;
import org.ietr.preesm.core.architecture.simplemodel.MediumDefinition;
import org.ietr.preesm.core.architecture.simplemodel.Operator;
import org.ietr.preesm.core.tools.PreesmLogger;


/**
 * Represents a single step in a route between two operators separated by a medium
 * 
 * @author mpelcat
 */
public class MediumRouteStep extends AbstractRouteStep {

	/**
	 * Medium defining how to simulate the transfer
	 */
	private Medium medium;

	/**
	 * The route step type determines how the communication will be simulated.
	 */
	public static final String type = "MediumRouteStep";

	public MediumRouteStep(Operator sender, Medium medium, Operator receiver) {
		super(sender,receiver);
		this.medium = medium;
	}

	public Medium getMedium() {
		return medium;
	}

	public void setMedium(Medium medium) {
		this.medium = medium;
	}

	@Override
	public String toString() {
		return "{" + getSender().toString() + " -> " + medium.toString() + " -> "
				+ getReceiver().toString() + "}";
	}

	/**
	 * The route step type determines how the communication will be simulated.
	 */
	@Override
	public String getType() {
		return type;
	}

	/**
	 * The id is given to code generation. It selects the communication functions to use
	 */
	@Override
	public String getId() {
		return medium.getDefinition().getVlnv().getName();
	}

	/**
	 * Evaluates the cost of a data transfer with size transferSize
	 */
	@Override
	public long getTransferCost(long transfersSize) {
		if (medium != null) {
			MediumDefinition def = (MediumDefinition) medium
					.getDefinition();
			return def.getTransferTime(transfersSize);
		} else {

			PreesmLogger.getLogger().log(
					Level.SEVERE,
					"Data could not be correctly transfered from "
							+ getSender().getName() + " to "
							+ getReceiver().getName());

			return 0;
		}
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new MediumRouteStep((Operator)getSender().clone(),(Medium)medium.clone(),(Operator)getReceiver().clone());
	}

	@Override
	public long getWorstTransferTime(long transfersSize) {
		return getTransferCost(transfersSize);
	}
	
	
}
