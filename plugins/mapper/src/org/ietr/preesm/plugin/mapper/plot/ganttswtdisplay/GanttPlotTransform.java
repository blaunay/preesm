/**
 * 
 */
package org.ietr.preesm.plugin.mapper.plot.ganttswtdisplay;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.ietr.preesm.core.architecture.MultiCoreArchitecture;
import org.ietr.preesm.core.scenario.IScenario;
import org.ietr.preesm.core.task.IPlotter;
import org.ietr.preesm.core.task.TextParameters;
import org.ietr.preesm.plugin.mapper.model.MapperDAG;
import org.sdf4j.model.dag.DirectedAcyclicGraph;
import org.sdf4j.model.sdf.SDFGraph;

public class GanttPlotTransform implements IPlotter {

	@Override
	public void transform(DirectedAcyclicGraph dag, SDFGraph sdf,
			MultiCoreArchitecture archi, IScenario scenario, TextParameters params) {

		MapperDAG mapperDag = (MapperDAG) dag;


		IEditorInput input = new ImplementationEditorInput(archi, mapperDag, params, scenario, sdf);

		PlatformUI.getWorkbench().getDisplay().asyncExec(
				new ImplementationEditorRunnable(input));
	}

}