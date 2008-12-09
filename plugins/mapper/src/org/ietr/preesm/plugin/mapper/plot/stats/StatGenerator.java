/**
 * 
 */
package org.ietr.preesm.plugin.mapper.plot.stats;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.ietr.preesm.core.architecture.MultiCoreArchitecture;
import org.ietr.preesm.core.architecture.simplemodel.MediumDefinition;
import org.ietr.preesm.core.architecture.simplemodel.Operator;
import org.ietr.preesm.core.scenario.IScenario;
import org.ietr.preesm.core.task.TextParameters;
import org.ietr.preesm.core.tools.PreesmLogger;
import org.ietr.preesm.plugin.abc.AbcType;
import org.ietr.preesm.plugin.abc.AbstractAbc;
import org.ietr.preesm.plugin.abc.IAbc;
import org.ietr.preesm.plugin.abc.impl.InfiniteHomogeneousAbc;
import org.ietr.preesm.plugin.mapper.edgescheduling.EdgeSchedType;
import org.ietr.preesm.plugin.mapper.model.MapperDAG;
import org.ietr.preesm.plugin.mapper.model.MapperDAGEdge;
import org.ietr.preesm.plugin.mapper.model.MapperDAGVertex;
import org.ietr.preesm.plugin.mapper.model.impl.ReceiveVertex;
import org.ietr.preesm.plugin.mapper.model.impl.SendVertex;
import org.sdf4j.model.PropertyBean;
import org.sdf4j.model.dag.DAGEdge;
import org.sdf4j.model.dag.DAGVertex;
import org.sdf4j.model.sdf.SDFGraph;

/**
 * Generating the statistics to be displayed in stat editor
 * 
 * @author mpelcat
 */
public class StatGenerator {

	private MapperDAG dag = null;
	private SDFGraph sdf = null;
	private MultiCoreArchitecture archi = null;
	private IScenario scenario = null;
	private TextParameters params = null;
	private IAbc abc = null;
	private int finalTime = 0;

	public StatGenerator(MultiCoreArchitecture archi, MapperDAG dag,
			TextParameters params, IScenario scenario, SDFGraph sdf) {
		super();
		this.archi = archi;
		this.dag = dag;
		this.params = params;
		this.scenario = scenario;
		this.sdf = sdf;

		initAbc();
		
		//getDAGComplexSpanLength();
		//getDAGComplexWorkLength();
		//getLoad(archi.getMainOperator());
		//getMem(archi.getMainOperator());
	}
	
	/**
	 * The span is the shortest possible execution time. It is theoretic
	 * because no communication time is taken into account. We consider that we have
	 * an infinity of cores of main type totally connected with perfect media. The span
	 * complex because the DAG is not serial-parallel but can be any DAG. 
	 */
	public int getDAGComplexSpanLength(){

		MapperDAG taskDag = dag.clone();
		removeSendReceive(taskDag);
		
		MultiCoreArchitecture localArchi = archi.clone();

		MediumDefinition mainMediumDef = (MediumDefinition)localArchi.getMainMedium().getDefinition();
		mainMediumDef.setInvSpeed(0);
		mainMediumDef.setOverhead(0);
		
		IAbc simu = new InfiniteHomogeneousAbc(EdgeSchedType.none, taskDag, localArchi);
		int span = simu.getFinalTime();
		
		PreesmLogger.getLogger().log(Level.INFO, "infinite homogeneous timing: " + span);
		
		return span;
		
	}

	/**
	 * The work is the sum of all task lengths 
	 */
	public int getDAGComplexWorkLength(){

		int work = 0;
		MapperDAG localDag = getDag().clone();
		StatGenerator.removeSendReceive(localDag);
		MultiCoreArchitecture archi = getArchi();
		
		
		if(localDag != null && archi != null){

			// Gets the appropriate abc to generate the gantt.
			PropertyBean bean = localDag.getPropertyBean();
			AbcType abctype = (AbcType)bean.getValue(AbstractAbc.propertyBeanName);
			
			IAbc simu = AbstractAbc
			.getInstance(abctype, EdgeSchedType.none, localDag, archi);

			simu.resetDAG();
			simu.implantAllVerticesOnOperator(archi.getMainOperator());
			
			work = simu.getFinalTime();
			
			PreesmLogger.getLogger().log(Level.INFO, "single core timing: " + work);

			return work;
		}
		return -1;
	}

	/**
	 * The load is the percentage of a processing resource used for the given algorithm
	 */
	public Integer getLoad(Operator operator){

		return abc.getLoad(operator);
	}

	/**
	 * The memory is the sum of all buffers allocated by the mapping
	 */
	public Integer getMem(Operator operator){

		int mem = 0;
		
		if(abc != null){
			
			for(DAGEdge e : abc.getDAG().edgeSet()){
				MapperDAGEdge me = (MapperDAGEdge)e;
				MapperDAGVertex scr = (MapperDAGVertex)me.getSource();
				MapperDAGVertex tgt = (MapperDAGVertex)me.getTarget();
				if(scr.getImplementationVertexProperty().getEffectiveComponent().equals(operator) || 
						tgt.getImplementationVertexProperty().getEffectiveComponent().equals(operator)){
					mem += me.getInitialEdgeProperty().getDataSize();
				}
			}
		}
				
		return mem;
		
	}

	public MapperDAG getDag() {
		return dag;
	}

	public SDFGraph getSdf() {
		return sdf;
	}

	public MultiCoreArchitecture getArchi() {
		return archi;
	}

	public IScenario getScenario() {
		return scenario;
	}

	public TextParameters getParams() {
		return params;
	}

	public int getFinalTime() {
		return finalTime;
	}
	
	public static void removeSendReceive(MapperDAG localDag){

		// Every send and receive vertices are removed
		Set<DAGVertex> vset = new HashSet<DAGVertex>(localDag.vertexSet());
		for(DAGVertex v:vset)
			if(v instanceof SendVertex || v instanceof ReceiveVertex)
				localDag.removeVertex(v);
		
	}

	
	public void initAbc(){

		MapperDAG localDag = getDag().clone();
		MultiCoreArchitecture archi = getArchi();
		
		
		if(localDag != null && archi != null){

			// Gets the appropriate abc to generate the gantt.
			PropertyBean bean = localDag.getPropertyBean();
			AbcType abctype = (AbcType)bean.getValue(AbstractAbc.propertyBeanName);
			
			abc = AbstractAbc
			.getInstance(abctype, EdgeSchedType.none, localDag, archi);

			StatGenerator.removeSendReceive(localDag);

			abc.setDAG(localDag);			
			finalTime = abc.getFinalTime();
			
			PreesmLogger.getLogger().log(Level.INFO, "stat abc of type " + abctype.toString() + " initialized");
		}
	}
}
