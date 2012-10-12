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

package org.ietr.preesm.codegen;

import java.util.SortedSet;

import net.sf.dftools.algorithm.model.parameters.Parameter;
import net.sf.dftools.algorithm.model.parameters.ParameterSet;
import net.sf.dftools.algorithm.model.psdf.PSDFInitVertex;
import net.sf.dftools.algorithm.model.psdf.PSDFSubInitVertex;
import net.sf.dftools.algorithm.model.psdf.parameters.PSDFDynamicParameter;
import net.sf.dftools.algorithm.model.sdf.SDFAbstractVertex;

import org.ietr.preesm.codegen.model.ICodeGenSDFVertex;
import org.ietr.preesm.codegen.model.call.Variable;
import org.ietr.preesm.codegen.model.containers.AbstractCodeContainer;
import org.ietr.preesm.codegen.model.factories.CodeElementFactory;
import org.ietr.preesm.codegen.model.main.ICodeElement;
import org.ietr.preesm.codegen.model.types.CodeSectionType;
import org.ietr.preesm.core.types.DataType;

/**
 * Generates code for a code phase, init or loop
 * 
 * @author mpelcat
 */
public class PhaseCodeGenerator {

	/**
	 * Block of code filled by this generator
	 */
	protected AbstractCodeContainer container;

	public PhaseCodeGenerator(AbstractCodeContainer container) {
		this.container = container;
	}

	/**
	 * Adding variables for PSDF parameters
	 */
	public void addDynamicParameter(ParameterSet params) {
		if (params != null) {
			for (Parameter param : params.values()) {
				if (param instanceof PSDFDynamicParameter) {
					container.addVariable(
									new Variable(param.getName(), new DataType(
											"long")));
				}
			}
		}
	}

	/**
	 * Adds one code element (function call or hierarchical element) for each vertex if prototype
	 * advises it.
	 */
	public void addUserFunctionCalls(SortedSet<SDFAbstractVertex> vertices, CodeSectionType sectionType) {

		for (SDFAbstractVertex vertex : vertices) {

			// Managing regular vertices
			if (vertex instanceof ICodeGenSDFVertex) {
				ICodeElement mainCall = CodeElementFactory.createElement(
						container, vertex, sectionType);
				if (mainCall != null) {
					if (vertex instanceof PSDFInitVertex) {
						container.addInitCodeElement(mainCall);
					} else if (vertex instanceof PSDFSubInitVertex) {
						container.addCodeElementFirst(mainCall);
					} else {
						container.addCodeElement(mainCall);
					}
				}
			}
		}
	}
}
