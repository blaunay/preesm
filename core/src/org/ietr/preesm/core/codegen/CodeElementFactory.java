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


import org.sdf4j.model.AbstractVertex;
import org.sdf4j.model.dag.DAGVertex;
import org.sdf4j.model.sdf.SDFAbstractVertex;
import org.sdf4j.model.sdf.SDFGraph;
import org.sdf4j.model.sdf.SDFVertex;

public class CodeElementFactory {

	public static ICodeElement createElement(String name,
			AbstractBufferContainer parentContainer, DAGVertex vertex) {
		if (vertex.getCorrespondingSDFVertex().getGraphDescription() == null
				&& vertex.getNbRepeat().intValue() > 1) {
			FiniteForLoop loop = new FiniteForLoop(parentContainer, vertex, vertex.getNbRepeat().intValue());
			loop.addCall(new UserFunctionCall(name, vertex, loop));
			return loop;
		} else if (vertex.getCorrespondingSDFVertex().getGraphDescription() == null
				&& vertex.getNbRepeat().intValue() == 1) {
			return new UserFunctionCall(name, vertex, parentContainer);
		} else if (vertex.getCorrespondingSDFVertex().getGraphDescription() != null
				&& vertex.getNbRepeat().intValue() > 1) {
			SDFGraph graph = vertex.getCorrespondingSDFVertex()
					.getGraphDescription();
			FiniteForLoop loop = new FiniteForLoop(parentContainer, vertex, vertex.getNbRepeat().intValue());
			for (SDFAbstractVertex child : graph.vertexSet()) {
				loop.addCall(CodeElementFactory
						.createElement(name, loop, child));
			}
			return loop;
		} else {
			SDFGraph graph = vertex.getCorrespondingSDFVertex()
					.getGraphDescription();
			CompoundCodeElement compound = new CompoundCodeElement(name,
					parentContainer, vertex);
			for (SDFAbstractVertex child : graph.vertexSet()) {
				compound.addCall(CodeElementFactory.createElement(name,
						parentContainer, child));
			}
			return compound;
		}
	}

	public static ICodeElement createElement(String name,
			AbstractBufferContainer parentContainer, SDFAbstractVertex vertex) {
		if (vertex.getGraphDescription() == null) {
			return new UserFunctionCall(name, vertex, parentContainer);
		} else if (vertex.getGraphDescription() != null
				&& vertex.getBase().getVRB().get(vertex) > 1) {
			SDFGraph graph = vertex.getGraphDescription();
			FiniteForLoop loop = new FiniteForLoop(parentContainer,
					(SDFVertex) vertex, vertex.getBase().getVRB().get(vertex)) ;
			for (SDFAbstractVertex child : graph.vertexSet()) {
				loop.addCall(CodeElementFactory
						.createElement(name, loop, child));
			}
			return loop;
		} else {
			SDFGraph graph = vertex.getGraphDescription();
			CompoundCodeElement compound = new CompoundCodeElement(name,
					parentContainer, (SDFVertex) vertex);
			for (SDFAbstractVertex child : graph.vertexSet()) {
				compound.addCall(CodeElementFactory.createElement(name,
						parentContainer, child));
			}
			return compound;
		}
	}

	/**
	 * Creates an element from an AbstractVertex
	 * 
	 * @param name
	 * @param parentContainer
	 * @param vertex
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static ICodeElement createElement(String name,
			AbstractBufferContainer parentContainer, AbstractVertex vertex) {
		if (vertex instanceof DAGVertex) {
			return createElement(name, parentContainer, (DAGVertex) vertex);
		} else if (vertex instanceof SDFAbstractVertex) {
			return createElement(name, parentContainer,
					(SDFAbstractVertex) vertex);
		}
		return null;
	}
}
