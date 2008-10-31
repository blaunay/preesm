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


/**
 * 
 */
package org.ietr.preesm.plugin.mapper.tools;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.ietr.preesm.core.architecture.MultiCoreArchitecture;
import org.ietr.preesm.core.architecture.Operator;
import org.ietr.preesm.plugin.mapper.model.MapperDAGVertex;

/**
 * Iterator on operators able to execute a given vertex
 * 
 * @author mpelcat
 */
public class OperatorIterator implements Iterator<Operator> {

	private int currentIndex = -1;

	private List<Operator> operatorlist;

	/**
	 * Constructor from an architecture and a reference vertex
	 */
	public OperatorIterator(MapperDAGVertex vertex, MultiCoreArchitecture archi) {
		super();

		createList(vertex, archi);

		currentIndex = 0;
	}

	public void createList(MapperDAGVertex vertex, MultiCoreArchitecture archi) {

		Set<Operator> defset = vertex.getInitialVertexProperty()
				.getOperatorSet();

		operatorlist = new ArrayList<Operator>();
		operatorlist.addAll(defset);
	}

	public List<Operator> getOperatorList() {
		return operatorlist;
	}

	@Override
	public boolean hasNext() {
		return (currentIndex < operatorlist.size());
	}

	@Override
	public Operator next() {
		return operatorlist.get(currentIndex++);
	}

	@Override
	public void remove() {
		if (currentIndex > 0) {
			operatorlist.remove(currentIndex - 1);
			currentIndex -= 1;
		}
	}
}
