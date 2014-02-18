package org.ietr.preesm.ui.properties.filters;

import org.eclipse.gef.EditPart;
import org.eclipse.jface.viewers.IFilter;
import org.ietr.dftools.graphiti.model.Graph;
import org.ietr.dftools.graphiti.model.Vertex;

/**
 * This class filters SDF vertices to enable the correct property tabs
 * 
 * @author mpelcat
 * 
 */
public class SDFVertexFilter implements IFilter {

	@Override
	public boolean select(Object toTest) {
		if (toTest instanceof EditPart) {
			Object model = ((EditPart) toTest).getModel();
			if (model instanceof Vertex) {
				Vertex vertex = (Vertex) model;
				Graph graph = vertex.getParent();
				return graph.getType().getName().equals("Dataflow Graph");
			}
		}
		return false;
	}

}
