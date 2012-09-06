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
import java.util.logging.Level;

import net.sf.dftools.algorithm.model.parameters.Parameter;
import net.sf.dftools.algorithm.model.parameters.ParameterSet;
import net.sf.dftools.algorithm.model.psdf.PSDFInitVertex;
import net.sf.dftools.algorithm.model.psdf.PSDFSubInitVertex;
import net.sf.dftools.algorithm.model.psdf.parameters.PSDFDynamicParameter;
import net.sf.dftools.algorithm.model.sdf.SDFAbstractVertex;
import net.sf.dftools.workflow.tools.WorkflowLogger;

import org.ietr.preesm.codegen.communication.ComCodeGeneratorFactory;
import org.ietr.preesm.codegen.communication.IComCodeGenerator;
import org.ietr.preesm.codegen.model.CodeGenSDFTokenInitVertex;
import org.ietr.preesm.codegen.model.FunctionPrototype;
import org.ietr.preesm.codegen.model.ICodeGenSDFVertex;
import org.ietr.preesm.codegen.model.buffer.AbstractBufferContainer;
import org.ietr.preesm.codegen.model.call.UserFunctionCall;
import org.ietr.preesm.codegen.model.call.Variable;
import org.ietr.preesm.codegen.model.containers.ForLoop;
import org.ietr.preesm.codegen.model.containers.LinearCodeContainer;
import org.ietr.preesm.codegen.model.factories.CodeElementFactory;
import org.ietr.preesm.codegen.model.main.ICodeElement;
import org.ietr.preesm.codegen.model.threads.ComputationThreadDeclaration;
import org.ietr.preesm.codegen.model.types.CodeSectionType;
import org.ietr.preesm.core.architecture.route.AbstractRouteStep;
import org.ietr.preesm.core.types.DataType;
import org.ietr.preesm.core.types.ImplementationPropertyNames;

/**
 * Generates code for a computation thread
 * 
 * @author Matthieu Wipliez
 * @author mpelcat
 */
public class CompThreadCodeGenerator {

	private ComputationThreadDeclaration thread;

	public CompThreadCodeGenerator(ComputationThreadDeclaration thread) {
		this.thread = thread;
	}


	/**
	 * Adds send and receive functions from vertices allocated on the current
	 * core. Vertices are already in the correct order. The code thread com
	 * generator delegates com creation to each route step appropriate generator
	 */
	public void addSendsAndReceives(SortedSet<SDFAbstractVertex> vertices,
			AbstractBufferContainer bufferContainer) {

		// a com code generator factory outputs the commmunication generator
		// that will add communication primitives into the code
		ComCodeGeneratorFactory factory = new ComCodeGeneratorFactory(
				thread, vertices);
		for (SDFAbstractVertex vertex : vertices) {
			AbstractRouteStep step = (AbstractRouteStep) vertex
					.getPropertyBean().getValue(
							ImplementationPropertyNames.SendReceive_routeStep);

			// Delegates the com creation to the appropriate generator
			IComCodeGenerator generator = factory.getCodeGenerator(step);

			// Creates all functions and buffers related to the given vertex
			generator.insertComs(vertex);
		}
	}

	/**
	 * Adding variables for PSDF parameters
	 */
	public void addDynamicParameter(ParameterSet params) {
		if (params != null) {
			for (Parameter param : params.values()) {
				if (param instanceof PSDFDynamicParameter) {
					thread.getLoopCode()
							.addVariable(
									new Variable(param.getName(), new DataType(
											"long")));
				}
			}
		}
	}

	/**
	 * Adds one function call for each vertex in the ordered set
	 */
	public void addUserFunctionCalls(SortedSet<SDFAbstractVertex> vertices) {

		LinearCodeContainer beginningCode = thread.getBeginningCode();
		ForLoop loopCode = thread.getLoopCode();
		LinearCodeContainer endCode = thread.getEndCode();

		// Treating regular vertices
		for (SDFAbstractVertex vertex : vertices) {
			if (vertex instanceof ICodeGenSDFVertex
					&& vertex.getGraphDescription() == null) {
				FunctionPrototype vertexCall = (FunctionPrototype) vertex.getRefinement();
				if (vertex instanceof CodeGenSDFTokenInitVertex) {
					ICodeElement beginningCall = new UserFunctionCall(
							(CodeGenSDFTokenInitVertex) vertex, thread,
							CodeSectionType.beginning, false);
					// Adding init call if any
					beginningCode.addCodeElement(beginningCall);
					// PreesmLogger.getLogger().log(Level.INFO, "");
				} else if ((vertexCall != null && vertexCall.getInitCall() != null)) {
					ICodeElement beginningCall = new UserFunctionCall(vertex,
							thread, CodeSectionType.beginning, false);
					// Adding init call if any
					beginningCode.addCodeElement(beginningCall);
				}
				if (vertexCall != null && vertexCall.getEndCall() != null) {
					ICodeElement endCall = new UserFunctionCall(vertex, thread,
							CodeSectionType.end, false);
					// Adding end call if any
					endCode.addCodeElement(endCall);
				}

			}

			if (vertex instanceof ICodeGenSDFVertex) {
				ICodeElement loopCall = CodeElementFactory.createElement(
						vertex.getName(), loopCode, vertex);
				if (loopCall != null) {
					if (vertex instanceof PSDFInitVertex) {
						loopCode.addInitCodeElement(loopCall);
					} else if (vertex instanceof PSDFSubInitVertex) {
						loopCode.addCodeElementFirst(loopCall);
					} else {
						// Adding loop call if any
						WorkflowLogger.getLogger()
								.log(Level.FINE,
										"Adding code elt "
												+ loopCall.toString()
												+ " on operator "
												+ thread.getParentContainer()
														.getName());
						loopCode.addCodeElement(loopCall);
					}
				}
			}
		}
	}
}
