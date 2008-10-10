/**
 * 
 */
package org.ietr.preesm.core.scenario.editor.constraints;

import org.eclipse.jface.viewers.LabelProvider;
import org.sdf4j.model.sdf.SDFAbstractVertex;
import org.sdf4j.model.sdf.SDFGraph;

/**
 * The label provider displays the name of the vertex displayed in a SDF tree
 * 
 * @author mpelcat
 */
public class SDFLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {
		String name = "";
		if (element instanceof SDFAbstractVertex) {
			name = ((SDFAbstractVertex) element).getName();
		} else if (element instanceof SDFGraph) {
			name = "graph";
		}

		return name;
	}

}
