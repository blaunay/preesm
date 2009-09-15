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

package org.ietr.preesm.plugin.mapper.params;

import java.util.logging.Level;

import org.ietr.preesm.core.task.TextParameters;
import org.ietr.preesm.core.tools.PreesmLogger;

/**
 * Class which purpose is setting the parameters for the fast algorithm
 * 
 * @author pmenuet
 * @author mpelcat
 */

public class FastAlgoParameters {

	protected TextParameters textParameters = null;

	/**
	 * true if we need to display the intermediate solutions
	 */
	private boolean displaySolutions;

	/**
	 * Time in seconds we want to run FAST
	 */
	private int fastTime = -1;

	public FastAlgoParameters(TextParameters textParameters) {

		this.displaySolutions = textParameters
				.getBooleanVariable("displaySolutions");
		if (textParameters.getIntVariable("fastTime") > 0) {
			this.fastTime = textParameters
					.getIntVariable("fastTime");
		}

		PreesmLogger
				.getLogger()
				.log(
						Level.INFO,
						"The Fast algo parameters are: displaySolutions=true/false; fastTime in seconds");
	}

	/**
	 * 
	 * Constructors
	 * 
	 */

	public FastAlgoParameters(int fastTime,
			boolean displaySolutions) {

		this.displaySolutions = displaySolutions;
		this.fastTime = fastTime;
	}

	/**
	 * 
	 * Getters and setters
	 * 
	 */

	public boolean isDisplaySolutions() {
		return displaySolutions;
	}

	public void setDisplaySolutions(boolean displaySolutions) {
		this.displaySolutions = displaySolutions;
	}

	/**
	 * Returns the time in seconds between two FAST probabilistic hops in the
	 * critical path
	 */
	public int getFastTime() {
		return fastTime;
	}
}
