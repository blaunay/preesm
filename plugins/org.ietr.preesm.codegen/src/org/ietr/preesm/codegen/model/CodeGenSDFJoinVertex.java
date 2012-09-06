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

package org.ietr.preesm.codegen.model;

import java.util.Map;

import net.sf.dftools.algorithm.model.AbstractEdge;
import net.sf.dftools.algorithm.model.parameters.InvalidExpressionException;
import net.sf.dftools.algorithm.model.sdf.SDFEdge;
import net.sf.dftools.algorithm.model.sdf.SDFGraph;
import net.sf.dftools.algorithm.model.sdf.esdf.SDFJoinVertex;
import net.sf.dftools.architecture.slam.ComponentInstance;

import org.ietr.preesm.codegen.model.buffer.AbstractBufferContainer;
import org.ietr.preesm.codegen.model.buffer.Buffer;
import org.ietr.preesm.codegen.model.buffer.BufferAtIndex;
import org.ietr.preesm.codegen.model.buffer.SubBuffer;
import org.ietr.preesm.codegen.model.buffer.SubBufferAllocation;
import org.ietr.preesm.codegen.model.call.Constant;
import org.ietr.preesm.codegen.model.call.UserFunctionCall;
import org.ietr.preesm.codegen.model.containers.AbstractCodeContainer;
import org.ietr.preesm.codegen.model.containers.CompoundCodeElement;
import org.ietr.preesm.codegen.model.expression.BinaryExpression;
import org.ietr.preesm.codegen.model.expression.ConstantExpression;
import org.ietr.preesm.codegen.model.expression.IExpression;
import org.ietr.preesm.codegen.model.main.ICodeElement;
import org.ietr.preesm.core.types.DataType;
import org.ietr.preesm.core.types.ImplementationPropertyNames;
import org.ietr.preesm.core.types.VertexType;

public class CodeGenSDFJoinVertex extends SDFJoinVertex implements
		ICodeGenSDFVertex, ICodeGenSpecialBehaviorVertex {

	public static final String TYPE = ImplementationPropertyNames.Vertex_vertexType;

	public CodeGenSDFJoinVertex() {
		this.getPropertyBean().setValue(TYPE, VertexType.task);
		FunctionPrototype joinCall = new FunctionPrototype("join");
		this.setRefinement(joinCall);
	}

	public ComponentInstance getOperator() {
		return (ComponentInstance) this.getPropertyBean().getValue(OPERATOR,
				ComponentInstance.class);
	}

	public void setOperator(ComponentInstance op) {
		this.getPropertyBean().setValue(OPERATOR, getOperator(), op);
	}

	public int getPos() {
		if (this.getPropertyBean().getValue(POS) != null) {
			return (Integer) this.getPropertyBean()
					.getValue(POS, Integer.class);
		}
		return 0;
	}

	public void setPos(int pos) {
		this.getPropertyBean().setValue(POS, getPos(), pos);
	}

	private void addConnection(SDFEdge newEdge) {
		int i = 0;
		while (getConnections().get(i) != null) {
			i++;
		}
		getConnections().put(i, newEdge);
	}

	private void removeConnection(SDFEdge newEdge) {
		getConnections().remove(newEdge);
	}

	/**
	 * Sets this edge connection index
	 * 
	 * @param edge
	 *            The edge connected to the vertex
	 * @param index
	 *            The index in the connections
	 */
	public void setConnectionIndex(SDFEdge edge, int index) {
		Map<Integer, SDFEdge> connections = getConnections();
		connections.put(index, edge);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void connectionAdded(AbstractEdge e) {
		addConnection((SDFEdge) e);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void connectionRemoved(AbstractEdge e) {
		removeConnection((SDFEdge) e);
	}

	public String toString() {
		return "";
	}

	@Override
	public ICodeElement getCodeElement(AbstractCodeContainer parentContainer) {
		SDFEdge outgoingEdge = null;
		CompoundCodeElement container = new CompoundCodeElement(this.getName(),
				parentContainer);
		container.setCorrespondingVertex(this);
		for (SDFEdge outedge : ((SDFGraph) this.getBase())
				.outgoingEdgesOf(this)) {
			outgoingEdge = outedge;
		}
		for (SDFEdge inEdge : ((SDFGraph) this.getBase()).incomingEdgesOf(this)) {
			UserFunctionCall copyCall = new UserFunctionCall("memcpy",
					parentContainer);
			try {
				copyCall.addArgument(new BufferAtIndex(new ConstantExpression(
						"", new DataType("int"), this.getEdgeIndex(inEdge)
								* inEdge.getProd().intValue()), parentContainer
						.getBuffer(outgoingEdge)));
				copyCall.addArgument(parentContainer.getBuffer(inEdge));
				copyCall.addArgument(new Constant("size", inEdge.getCons()
						.intValue()
						+ "*sizeof("
						+ outgoingEdge.getDataType().toString() + ")"));
			} catch (InvalidExpressionException e) {
				copyCall.addArgument(new Constant("size", 0));
			}
			container.addCall(copyCall);
		}
		return container;
	}

	@Override
	public boolean generateSpecialBehavior(
			AbstractBufferContainer parentContainer)
			throws InvalidExpressionException {
		SDFEdge outgoingEdge = null;

		for (SDFEdge outEdge : ((SDFGraph) this.getBase())
				.outgoingEdgesOf(this)) {
			outgoingEdge = outEdge;
		}
		Buffer outBuffer = parentContainer.getBuffer(outgoingEdge);
		for (SDFEdge inEdge : ((SDFGraph) this.getBase()).incomingEdgesOf(this)) {
			ConstantExpression index = new ConstantExpression("", new DataType(
					"int"), ((CodeGenSDFJoinVertex) this).getEdgeIndex(inEdge));
			String buffName = parentContainer.getBuffer(inEdge).getName();
			IExpression expr = new BinaryExpression("%", new BinaryExpression(
					"*", index, new ConstantExpression(inEdge.getCons()
							.intValue())), new ConstantExpression(
					outBuffer.getSize()));
			SubBuffer subElt = new SubBuffer(buffName, inEdge.getCons()
					.intValue(), expr, outBuffer, inEdge, parentContainer);
			if (parentContainer.getBuffer(inEdge) == null) {
				parentContainer.removeBufferAllocation(parentContainer
						.getBuffer(inEdge));
				parentContainer.addSubBufferAllocation(new SubBufferAllocation(
						subElt));
			}

		}
		return true;
	}

}
