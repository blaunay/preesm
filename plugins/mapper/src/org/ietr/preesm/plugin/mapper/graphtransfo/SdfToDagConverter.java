/**
 * 
 */
package org.ietr.preesm.plugin.mapper.graphtransfo;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.ietr.preesm.core.architecture.Examples;
import org.ietr.preesm.core.architecture.IArchitecture;
import org.ietr.preesm.core.architecture.OperatorDefinition;
import org.ietr.preesm.core.constraints.ConstraintGroup;
import org.ietr.preesm.core.constraints.IScenario;
import org.ietr.preesm.core.constraints.Scenario;
import org.ietr.preesm.core.constraints.Timing;
import org.ietr.preesm.core.constraints.TimingManager;
import org.ietr.preesm.plugin.mapper.model.InitialEdgeProperty;
import org.ietr.preesm.plugin.mapper.model.InitialVertexProperty;
import org.ietr.preesm.plugin.mapper.model.MapperDAG;
import org.ietr.preesm.plugin.mapper.model.MapperDAGEdge;
import org.ietr.preesm.plugin.mapper.model.MapperDAGVertex;
import org.ietr.preesm.plugin.mapper.model.MapperEdgeFactory;
import org.ietr.preesm.plugin.mapper.model.MapperVertexFactory;
import org.ietr.preesm.plugin.mapper.tools.TopologicalDAGIterator;
import org.sdf4j.demo.SDFAdapterDemo;
import org.sdf4j.demo.SDFtoDAGDemo;
import org.sdf4j.generator.SDFRandomGraph;
import org.sdf4j.model.dag.DAGEdge;
import org.sdf4j.model.sdf.SDFAbstractVertex;
import org.sdf4j.model.sdf.SDFGraph;
import org.sdf4j.visitors.DAGTransformation;
import org.sdf4j.visitors.TopologyVisitor;

/**
 * Uses the SDF4J library to convert the input SDF into a DAG before scheduling.
 * 
 * @author mpelcat
 */
public class SdfToDagConverter {

	/**
	 * Main for test
	 */
	public static void main(String[] args) {
		int nbVertex = 10, minInDegree = 1, maxInDegree = 3, minOutDegree = 1, maxOutDegree = 3;

		// Creates a random SDF graph
		int minrate = 1, maxrate = 15;
		SDFRandomGraph test = new SDFRandomGraph();
		SDFGraph demoGraph = test.createRandomGraph(nbVertex, minInDegree,
				maxInDegree, minOutDegree, maxOutDegree, minrate, maxrate);
		//SDFGraph demoGraph =createTestComGraph();
		TopologyVisitor topo = new TopologyVisitor();
		demoGraph.accept(topo);

		IArchitecture architecture = Examples.get1C64Archi();
		
		IScenario scenario = new Scenario();
		TimingManager tmanager = new TimingManager();
		
		Iterator<SDFAbstractVertex> it = demoGraph.vertexSet().iterator();
		
		while(it.hasNext()){
			SDFAbstractVertex vertex = it.next();
			
			Timing t = new Timing((OperatorDefinition)architecture.getMainOperator().getDefinition(),vertex);
			t.setTime(1000);
			tmanager.addTiming(t);
		}
		
		MapperDAG dag = convert(demoGraph, architecture, scenario, true);

		
	}

	/**
	 * Converts a SDF in a DAG and retrieves the interesting properties form the SDF.
	 * 
	 * @author mpelcat
	 */
	public static MapperDAG convert(SDFGraph sdf, IArchitecture architecture,
			IScenario scenario, boolean display) {

		// Generates a dag
		MapperDAG dag = new MapperDAG(new MapperEdgeFactory(), sdf);
		TopologyVisitor topo = new TopologyVisitor();
		sdf.accept(topo);

		// Creates a visitor parameterized with the DAG
		DAGTransformation<MapperDAG> visitor = new DAGTransformation<MapperDAG>(
				dag, new MapperVertexFactory());
		
		// visits the SDF to generate the DAG
		sdf.accept(visitor);

		// Adds the necessary properties to vertices and edges
		addInitialProperty(dag, architecture, scenario);
		
		// Displays the  DAG
		if (display) {
			SDFAdapterDemo applet1 = new SDFAdapterDemo();
			applet1.init(sdf);
			SDFtoDAGDemo applet2 = new SDFtoDAGDemo();
			for(DAGEdge testEdge : dag.edgeSet()){
				testEdge.getWeight();
			}
			applet2.init(dag);
		}


		return dag;
	}

	/**
	 * Retrieves the constraints and adds them to the DAG initial properties
	 * 
	 * @param dag
	 * @param architecture
	 * @param constraints
	 * @return The DAG with initial properties
	 */
	public static MapperDAG addInitialProperty(MapperDAG dag,
			IArchitecture architecture, IScenario scenario) {

		/**
		 * Importing constraint timings
		 */
		// Iterating over dag vertices
		TopologicalDAGIterator dagiterator = new TopologicalDAGIterator(dag);

		while (dagiterator.hasNext()) {
			MapperDAGVertex currentVertex = (MapperDAGVertex) dagiterator
					.next();
			InitialVertexProperty currentVertexInit = currentVertex
					.getInitialVertexProperty();
			
			int nbRepeat = currentVertex.getNbRepeat().intValue();
			
			currentVertexInit.setNbRepeat(nbRepeat);

			List<Timing> timelist = scenario.getTimingManager()
					.getGraphTimings(currentVertex.getName());

			// Iterating over timings for each DAG vertex
			Iterator<Timing> listiterator = timelist.iterator();

			while (listiterator.hasNext()) {
				Timing timing = listiterator.next();

				currentVertexInit.addTiming(timing);
			}

		}

		/**
		 * Importing data edge weights
		 */
		Iterator<DAGEdge> edgeiterator = dag.edgeSet().iterator();

		while (edgeiterator.hasNext()) {
			MapperDAGEdge currentEdge = (MapperDAGEdge) edgeiterator.next();
			InitialEdgeProperty currentEdgeInit = currentEdge
					.getInitialEdgeProperty();

			int weight = currentEdge.getWeight().intValue();
			currentEdgeInit.setDataSize(weight);
		}

		/**
		 * Importing constraint groups
		 */

		// Iterating over constraint groups
		Iterator<ConstraintGroup> cgit = scenario.getConstraintGroupManager()
				.getConstraintGroups().iterator();

		while (cgit.hasNext()) {
			ConstraintGroup cg = cgit.next();

			// Iterating over graphs in constraint groups
			Iterator<SDFAbstractVertex> graphit = cg.getVertices().iterator();

			while (graphit.hasNext()) {
				SDFAbstractVertex currentsdfvertex = graphit.next();

				Set<MapperDAGVertex> vertexset = dag
						.getVertices(currentsdfvertex);

				// Iterating over DAG vertices corresponding to the same sdf
				// graph
				Iterator<MapperDAGVertex> vertexit = vertexset.iterator();

				while (vertexit.hasNext()) {
					MapperDAGVertex currentvertex = vertexit.next();

					// Iterating over operators in constraint group
					Iterator<OperatorDefinition> opit = cg
							.getOperatorDefinitions().iterator();

					while (opit.hasNext()) {
						OperatorDefinition currentop = opit.next();

						if (!currentvertex.getInitialVertexProperty()
								.isImplantable(currentop)) {

							// If no timing is set, we add a unavailable timing
							Timing newTiming = new Timing(currentop,
									currentsdfvertex);
							currentvertex.getInitialVertexProperty().addTiming(
									newTiming);
						}
					}
				}

			}

		}

		return null;
	}
}
