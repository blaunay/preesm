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

import org.ietr.preesm.core.codegen.UserFunctionCall.CodeSection;
import org.ietr.preesm.core.codegen.model.CodeGenSDFBroadcastVertex;
import org.ietr.preesm.core.codegen.model.CodeGenSDFForkVertex;
import org.ietr.preesm.core.codegen.model.CodeGenSDFInitVertex;
import org.ietr.preesm.core.codegen.model.CodeGenSDFJoinVertex;
import org.ietr.preesm.core.codegen.model.CodeGenSDFRoundBufferVertex;
import org.ietr.preesm.core.codegen.model.ICodeGenSDFVertex;
import org.sdf4j.model.parameters.InvalidExpressionException;
import org.sdf4j.model.sdf.SDFAbstractVertex;
import org.sdf4j.model.sdf.SDFEdge;

/**
 * Creating code elements from a vertex
 * 
 * @author jpiat
 * @author mpelcat
 */
public class CodeElementFactory {

	public static ICodeElement createElement(String name,
			AbstractBufferContainer parentContainer, SDFAbstractVertex vertex) {
		try {
			if (vertex instanceof ICodeGenSDFVertex && vertex.getNbRepeat() > 1) {
				FiniteForLoop loop = new FiniteForLoop(parentContainer,
						(ICodeGenSDFVertex) vertex);
				return loop;
			} else if (vertex instanceof CodeGenSDFBroadcastVertex) {
				return null;
			} else if (vertex instanceof CodeGenSDFForkVertex) {
				return null;
			} else if (vertex instanceof CodeGenSDFJoinVertex) {
				return null;
			} else if (vertex instanceof CodeGenSDFInitVertex) {
				if (vertex.getBase().outgoingEdgesOf(vertex).size() > 0) {
					SDFEdge initEdge = (SDFEdge) vertex.getBase()
							.outgoingEdgesOf(vertex).toArray()[0];
					UserFunctionCall initCall = new UserFunctionCall("init_"
							+ initEdge.getTargetInterface().getName(),
							parentContainer);
					initCall.addParameter(parentContainer.getBuffer(initEdge));
					try {
						initCall.addParameter(new Constant("init_size",
								initEdge.getProd().intValue()));
					} catch (InvalidExpressionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return initCall;
				}
				return null;
			} else if (vertex instanceof CodeGenSDFRoundBufferVertex) {
				return null;
			} else if (vertex instanceof ICodeGenSDFVertex
					&& vertex.getGraphDescription() == null) {
				return new UserFunctionCall(vertex, parentContainer,
						CodeSection.LOOP);
			} else {
				CompoundCodeElement compound = new CompoundCodeElement(name,
						parentContainer, (ICodeGenSDFVertex) vertex);
				return compound;
			}
		} catch (InvalidExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public static void treatSpecialBehaviorVertex(String name,
			AbstractBufferContainer parentContainer, SDFAbstractVertex vertex) {
		try {
			if (vertex instanceof CodeGenSDFForkVertex) {
				SDFEdge incomingEdge = null;
				int i = 0;
				for (SDFEdge inEdge : vertex.getBase().incomingEdgesOf(vertex)) {
					incomingEdge = inEdge;
				}
				Buffer inBuffer = parentContainer.getBuffer(incomingEdge);
				for (SDFEdge outEdge : vertex.getBase().outgoingEdgesOf(vertex)) {
					ConstantValue index = new ConstantValue("", new DataType(
							"int"), ((CodeGenSDFForkVertex) vertex)
							.getEdgeIndex(outEdge));
					String buffName = parentContainer.getBuffer(outEdge)
							.getName();
					SubBuffer subElt = new SubBuffer(buffName, outEdge
							.getProd().intValue(), index, inBuffer, outEdge,
							parentContainer);
					parentContainer.removeBufferAllocation(parentContainer
							.getBuffer(outEdge));
					parentContainer.addBuffer(new SubBufferAllocation(subElt));
					i++;
				}
			} else if (vertex instanceof CodeGenSDFJoinVertex) {
				SDFEdge outgoingEdge = null;
				int i = 0;
				for (SDFEdge outEdge : vertex.getBase().outgoingEdgesOf(vertex)) {
					outgoingEdge = outEdge;
				}
				Buffer outBuffer = parentContainer.getBuffer(outgoingEdge);
				for (SDFEdge inEdge : vertex.getBase().incomingEdgesOf(vertex)) {
					ConstantValue index = new ConstantValue("", new DataType(
							"int"), ((CodeGenSDFJoinVertex) vertex)
							.getEdgeIndex(inEdge));
					String buffName = parentContainer.getBuffer(inEdge)
							.getName();
					SubBuffer subElt = new SubBuffer(buffName, inEdge.getCons()
							.intValue(), index, outBuffer, inEdge,
							parentContainer);
					parentContainer.removeBufferAllocation(parentContainer
							.getBuffer(inEdge));
					parentContainer.addBuffer(new SubBufferAllocation(subElt));
					i++;
				}
			} else if (vertex instanceof CodeGenSDFBroadcastVertex) {
				SDFEdge incomingEdge = null;
				for (SDFEdge inEdge : vertex.getBase().incomingEdgesOf(vertex)) {
					incomingEdge = inEdge;
				}
				Buffer inBuffer = parentContainer.getBuffer(incomingEdge);
				for (SDFEdge outEdge : vertex.getBase().outgoingEdgesOf(vertex)) {
					ConstantValue index = new ConstantValue("", new DataType(
							"int"), 0);
					String buffName = parentContainer.getBuffer(outEdge)
							.getName();
					SubBuffer subElt = new SubBuffer(buffName, outEdge
							.getCons().intValue(), index, inBuffer, outEdge,
							parentContainer);
					parentContainer.removeBufferAllocation(parentContainer
							.getBuffer(outEdge));
					parentContainer.addBuffer(new SubBufferAllocation(subElt));
				}
			} else if (vertex instanceof CodeGenSDFRoundBufferVertex) {
				SDFEdge outgoingEdge = null;
				for (SDFEdge outEdge : vertex.getBase().outgoingEdgesOf(vertex)) {
					outgoingEdge = outEdge;
				}
				Buffer outBuffer = parentContainer.getBuffer(outgoingEdge);
				for (SDFEdge inEdge : vertex.getBase().incomingEdgesOf(vertex)) {
					ConstantValue index = new ConstantValue("", new DataType(
							"int"), 0);
					String buffName = parentContainer.getBuffer(inEdge)
							.getName();
					SubBuffer subElt = new SubBuffer(buffName, inEdge.getCons()
							.intValue(), index, outBuffer, inEdge,
							parentContainer);
					parentContainer.removeBufferAllocation(parentContainer
							.getBuffer(inEdge));
					parentContainer.addBuffer(new SubBufferAllocation(subElt));
				}
			}
		} catch (InvalidExpressionException e) {
			e.printStackTrace();
		}
	}
}
