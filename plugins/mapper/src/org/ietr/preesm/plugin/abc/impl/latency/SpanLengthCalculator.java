/**
 * 
 */
package org.ietr.preesm.plugin.abc.impl.latency;

import org.ietr.preesm.core.architecture.MultiCoreArchitecture;
import org.ietr.preesm.core.scenario.SDFAndArchitectureScenario;
import org.ietr.preesm.plugin.abc.taskscheduling.TaskSchedType;
import org.ietr.preesm.plugin.mapper.model.MapperDAG;
import org.ietr.preesm.plugin.mapper.model.MapperDAGEdge;
import org.ietr.preesm.plugin.mapper.params.AbcParameters;

/**
 * Using infinite homogeneous simulation to calculate the span length of a dag
 * 
 * @author mpelcat
 */
public class SpanLengthCalculator extends InfiniteHomogeneousAbc {

	public static final String DAG_SPAN = "dag span length";

	public SpanLengthCalculator(AbcParameters params, MapperDAG dag,
			MultiCoreArchitecture archi, TaskSchedType taskSchedType,
			SDFAndArchitectureScenario scenario) {
		super(params, dag, archi, taskSchedType, scenario);

		this.updateTimings();

		// The span corresponds to the final latency of an infinite homogeneous
		// simulation
		dag.getPropertyBean().setValue(DAG_SPAN, (Long) getFinalLatency());
	}

	@Override
	protected void setEdgeCost(MapperDAGEdge edge) {
		edge.getTimingEdgeProperty().setCost(1);
	}
}
