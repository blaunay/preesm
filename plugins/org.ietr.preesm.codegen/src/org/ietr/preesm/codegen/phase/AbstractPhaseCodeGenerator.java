/*********************************************************
Copyright or � or Copr. IETR/INSA: Matthieu Wipliez, Jonathan Piat,
Maxime Pelcat, Jean-Fran�ois Nezan, Micka�l Raulet

[mwipliez,jpiat,mpelcat,jnezan,mraulet]@insa-rennes.fr

This software is a computer program whose purpose is to prototype
parallel applications.

This software is governed by the CeCILL-B license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-B
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
knowledge of the CeCILL-B license and that you accept its terms.
 *********************************************************/

package org.ietr.preesm.codegen.phase;

import java.util.SortedSet;

import net.sf.dftools.algorithm.model.parameters.ParameterSet;
import net.sf.dftools.algorithm.model.sdf.SDFAbstractVertex;

import org.ietr.preesm.codegen.model.buffer.AbstractBufferContainer;
import org.ietr.preesm.codegen.model.containers.AbstractCodeContainer;

/**
 * Generates code for a code phase
 * 
 * @author mpelcat
 */
public abstract class AbstractPhaseCodeGenerator {

	/**
	 * Block of code filled by this generator
	 */
	protected AbstractCodeContainer container;
	
	public AbstractPhaseCodeGenerator(AbstractCodeContainer container) {
	}

	/**
	 * Adds send and receive functions from vertices allocated on the current
	 * core. Vertices are already in the correct order. The code thread com
	 * generator delegates com creation to each route step appropriate generator
	 */
	public abstract void addSendsAndReceives(SortedSet<SDFAbstractVertex> vertices,
			AbstractBufferContainer bufferContainer);

	/**
	 * Adding variables for PSDF parameters
	 */
	public abstract void addDynamicParameter(ParameterSet params);

	/**
	 * Adds one function call for each vertex in the ordered set
	 */
	public abstract void addUserFunctionCalls(SortedSet<SDFAbstractVertex> vertices);
}
