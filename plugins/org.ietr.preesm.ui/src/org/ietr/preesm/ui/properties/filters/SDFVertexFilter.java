package org.ietr.preesm.ui.properties.filters;

import net.sf.graphiti.model.Graph;
import net.sf.graphiti.model.Vertex;

import org.eclipse.gef.EditPart;
import org.eclipse.jface.viewers.IFilter;

public class SDFVertexFilter implements IFilter {

	@Override
	public boolean select(Object toTest) {
		if (toTest instanceof EditPart) {
			Object model = ((EditPart) toTest).getModel();
			if (model instanceof Vertex) {
				Vertex vertex = (Vertex) model;
				Graph graph = vertex.getParent();
				return graph.getType().getName().equals("SDF Graph");
			}
		}
		return false;
	}

}
