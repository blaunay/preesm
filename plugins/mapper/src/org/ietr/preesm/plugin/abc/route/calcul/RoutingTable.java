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
import java.util.HashMap;
import java.util.Map;

import org.ietr.preesm.core.architecture.route.Route;
import org.ietr.preesm.core.architecture.simplemodel.Operator;

/**
 * Table representing the different routes available to go from one operator to
 * another
 * 
 * @author mpelcat
 */
public class RoutingTable {

	/**
	 * A couple of operators to which the routes are linked
	 */
	private class OperatorCouple {

		private Operator op1;
		private Operator op2;

		public OperatorCouple(Operator op1, Operator op2) {
			super();
			this.op1 = op1;
			this.op2 = op2;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof OperatorCouple) {
				OperatorCouple doublet = (OperatorCouple) obj;
				if (doublet.getOp1().equals(getOp1())
						&& doublet.getOp2().equals(getOp2())) {
					return true;
				}
			}
			return false;
		}

		@Override
		public String toString() {
			return "(" + op1 + "," + op2 + ")";
		}

		public Operator getOp1() {
			return op1;
		}

		public Operator getOp2() {
			return op2;
		}
	}

	/**
	 * A list of routes
	 */
	private class RouteList extends ArrayList<Route> {
		private static final long serialVersionUID = -851695207011182681L;
	}

	/**
	 * List of available routes
	 */
	private Map<OperatorCouple, RouteList> table;

	public RoutingTable() {
		super();
		table = new HashMap<OperatorCouple, RouteList>();
	}

	/**
	 * Gets a route with a given index
	 */
	public Route getRoute(Operator op1, Operator op2, int index) {
		for (OperatorCouple c : table.keySet()) {
			if (c.equals(new OperatorCouple(op1, op2))) {
				return table.get(c).get(index);
			}
		}
		return null;
	}

	/**
	 * Adds a new route
	 */
	public void addRoute(Operator op1, Operator op2, Route route) {
		OperatorCouple key = new OperatorCouple(op1, op2);

		if (table.containsKey(key)) {
			table.get(key).add(route);
		} else {
			RouteList list = new RouteList();
			list.add(route);
			table.put(key, list);
		}
	}

	/**
	 * Displays the table
	 */
	@Override
	public String toString() {
		String result = "";
		for (OperatorCouple couple : table.keySet()) {
			result += couple.toString() + " -> " + table.get(couple).toString()
					+ "\n";
		}

		return result;
	}

}
