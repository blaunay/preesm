/**
 * 
 */
package org.ietr.preesm.plugin.mapper;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.ietr.preesm.core.architecture.Examples;
import org.ietr.preesm.core.architecture.IArchitecture;
import org.ietr.preesm.core.architecture.Medium;
import org.ietr.preesm.core.architecture.MultiCoreArchitecture;
import org.ietr.preesm.core.architecture.Operator;
import org.ietr.preesm.core.codegen.VertexType;
import org.ietr.preesm.core.codegen.sdfProperties.BufferAggregate;
import org.ietr.preesm.core.codegen.sdfProperties.BufferProperties;
import org.ietr.preesm.core.constraints.IScenario;
import org.ietr.preesm.core.constraints.Scenario;
import org.ietr.preesm.core.constraints.Timing;
import org.ietr.preesm.core.constraints.TimingManager;
import org.ietr.preesm.core.log.PreesmLogger;
import org.ietr.preesm.core.task.TaskResult;
import org.ietr.preesm.core.task.TextParameters;
import org.ietr.preesm.core.workflow.sources.AlgorithmRetriever;
import org.ietr.preesm.plugin.abc.AbstractAbc;
import org.ietr.preesm.plugin.abc.ArchitectureSimulatorType;
import org.ietr.preesm.plugin.abc.IAbc;
import org.ietr.preesm.plugin.abc.infinitehomogeneous.InfiniteHomogeneousAbc;
import org.ietr.preesm.plugin.mapper.fastalgo.FastAlgoParameters;
import org.ietr.preesm.plugin.mapper.fastalgo.FastAlgorithm;
import org.ietr.preesm.plugin.mapper.fastalgo.InitialLists;
import org.ietr.preesm.plugin.mapper.graphtransfo.SdfToDagConverter;
import org.ietr.preesm.plugin.mapper.graphtransfo.TagDAG;
import org.ietr.preesm.plugin.mapper.model.MapperDAG;
import org.sdf4j.model.PropertyBean;
import org.sdf4j.model.sdf.SDFAbstractGraph;
import org.sdf4j.model.sdf.SDFAbstractVertex;
import org.sdf4j.model.sdf.SDFDefaultEdgePropertyType;
import org.sdf4j.model.sdf.SDFEdge;
import org.sdf4j.model.sdf.SDFGraph;
import org.sdf4j.model.sdf.SDFVertex;

/**
 * FAST Kwok algorithm
 * 
 * @author pmenuet
 */
public class FASTTransformation extends AbstractMapping {

	/**
	 * Main for test
	 */
	public static void main(String[] args) {
		
		Logger logger = PreesmLogger.getLogger();
		logger.setLevel(Level.FINER);

		// PreesmLogger.getLogger().setLevel(Level.FINER);

		// Generating archi
		MultiCoreArchitecture archi = Examples.get4C64Archi();

		// Generating random sdf dag
		int nbVertex = 20, minInDegree = 1, maxInDegree = 3, minOutDegree = 1, maxOutDegree = 3;
		SDFGraph graph = AlgorithmRetriever.randomDAG(nbVertex, minInDegree,
				maxInDegree, minOutDegree, maxOutDegree, 50,true);

		// Generating constraints
		IScenario scenario = new Scenario();

		TimingManager tmgr = scenario.getTimingManager();

		for (int i = 1; i <= nbVertex; i++) {
			String name = String.format("Vertex %d", i);
			Timing newt = new Timing(archi.getOperatorDefinition("c64x"), graph
					.getVertex(name), 100);
			tmgr.addTiming(newt);
		}

		FASTTransformation transformation = new FASTTransformation();
		FastAlgoParameters parameters = new FastAlgoParameters(500, 500, 16,
				ArchitectureSimulatorType.LooselyTimed);
		transformation.transform(graph, archi, parameters.textParameters(), scenario);

		logger.log(Level.FINER, "Test fast finished");
	}

	/**
	 * 
	 */
	public FASTTransformation() {
	}

	/**
	 * Function called while running the plugin
	 */
	@Override
	public TaskResult transform(SDFGraph algorithm, IArchitecture architecture,
			TextParameters textParameters, IScenario scenario) {

		FastAlgoParameters parameters;
		TaskResult result = new TaskResult();
		
		parameters = new FastAlgoParameters(textParameters);

		MapperDAG dag = SdfToDagConverter.convert(algorithm,architecture,scenario, false);

		IAbc simu = new InfiniteHomogeneousAbc(
				dag, architecture);

		InitialLists initial = new InitialLists();

		initial.constructInitialLists(dag, simu);

		simu.resetDAG();

		IAbc simu2 = AbstractAbc
				.getInstance(parameters.getSimulatorType(), dag, architecture);

		FastAlgorithm fastAlgorithm = new FastAlgorithm();

		dag = fastAlgorithm.map("test", parameters.getSimulatorType(), dag,
				architecture, initial.getCpnDominantList(), initial
						.getBlockingNodesList(), initial
						.getFinalcriticalpathList(), parameters.getMaxCount(),
				parameters.getMaxStep(), parameters.getMargIn(), false, false, null);

		simu2.setDAG(dag);

		simu2.plotImplementation();

		TagDAG tagSDF = new TagDAG();

		tagSDF.tag(dag,architecture);

		result.setDAG(dag);

		return result;
	}

	/**
	 * Adding a new edge to graph from a few properties
	 */
	public SDFEdge addExampleEdge(SDFGraph graph, String source, String dest,
			String type, int prodCons) {

		SDFEdge edge;

		edge = graph.addEdge(graph.getVertex(source), graph.getVertex(dest));
		edge.getPropertyBean().setValue("dataType", type);

		// DAG => prod = cons
		edge.setProd(new SDFDefaultEdgePropertyType(prodCons));
		edge.setCons(new SDFDefaultEdgePropertyType(prodCons));

		// DAG => no delay
		edge.setDelay(new SDFDefaultEdgePropertyType(0));

		// Example buffer aggregate with one single buffer
		BufferAggregate agg = new BufferAggregate();
		agg.add(new BufferProperties(type, "out", "in", prodCons));

		edge.getPropertyBean().setValue(BufferAggregate.propertyBeanName, agg);

		return edge;
	}
	
	/**
	 * Adding send and receive between v1 and v2. It removes the original vertex and
	 * copies its buffer aggregate
	 */
	public SDFAbstractVertex addComVertices(SDFAbstractVertex v1, SDFAbstractVertex v2, Medium medium,Operator sendOp,Operator receiveOp, int schedulingOrder) {

		SDFAbstractGraph graph = v1.getBase();

		SDFEdge originalEdge = graph.getEdge(v1, v2);
		Object aggregate = originalEdge.getPropertyBean().getValue(BufferAggregate.propertyBeanName);
		graph.removeEdge(originalEdge);
		
		// Tagging the communication vertices with their operator, type and medium
		SDFAbstractVertex send = new SDFVertex();
		send.setId("snd" + v2.getId() + sendOp.getName());
		send.getPropertyBean().setValue("schedulingOrder", schedulingOrder);
		send.getPropertyBean().setValue(VertexType.propertyBeanName, VertexType.send);
		send.getPropertyBean().setValue(Medium.propertyBeanName, medium);
		send.getPropertyBean().setValue(Operator.propertyBeanName, sendOp);
		
		SDFAbstractVertex receive = new SDFVertex();
		receive.setId("rcv" + v1.getId() + receiveOp.getName());
		receive.getPropertyBean().setValue("schedulingOrder", schedulingOrder);
		receive.getPropertyBean().setValue(VertexType.propertyBeanName, VertexType.receive);
		receive.getPropertyBean().setValue(Medium.propertyBeanName, medium);
		receive.getPropertyBean().setValue(Operator.propertyBeanName, receiveOp);
		
		graph.addVertex(send);
		graph.addVertex(receive);
		
		graph.addEdge(v1, send).getPropertyBean().setValue(BufferAggregate.propertyBeanName, aggregate);
		graph.addEdge(send, receive).getPropertyBean().setValue(BufferAggregate.propertyBeanName, aggregate);
		graph.addEdge(receive, v2).getPropertyBean().setValue(BufferAggregate.propertyBeanName, aggregate);
		
		return receive;
	}

	/**
	 * Kwok example 2 -> implanted DAG on one processor
	 */
	public SDFGraph implanteddagexample2_single(IArchitecture architecture) {

		/* Construct DAG */
		SDFGraph graph = new SDFGraph();

		for (int index = 1; index < 10; index++) {
			SDFVertex vertex = new SDFVertex();
			vertex.setId(String.format("n%d", index));
			vertex.setName(String.format("n%d", index));
			graph.addVertex(vertex);

			PropertyBean bean = vertex.getPropertyBean();
			bean.setValue(Operator.propertyBeanName, architecture
					.getMainOperator());
			bean.setValue(VertexType.propertyBeanName, VertexType.task);
		}

		graph.getVertex("n1").getPropertyBean().setValue("schedulingOrder", 1);
		graph.getVertex("n3").getPropertyBean().setValue("schedulingOrder", 2);
		graph.getVertex("n2").getPropertyBean().setValue("schedulingOrder", 3);
		graph.getVertex("n7").getPropertyBean().setValue("schedulingOrder", 4);
		graph.getVertex("n6").getPropertyBean().setValue("schedulingOrder", 5);
		graph.getVertex("n5").getPropertyBean().setValue("schedulingOrder", 6);
		graph.getVertex("n4").getPropertyBean().setValue("schedulingOrder", 7);
		graph.getVertex("n8").getPropertyBean().setValue("schedulingOrder", 8);
		graph.getVertex("n9").getPropertyBean().setValue("schedulingOrder", 9);

		// Route route = new Route();
		// edge.getPropertyBean().setValue("route", route);

		addExampleEdge(graph, "n1", "n2", "char", 4);
		addExampleEdge(graph, "n1", "n3", "char", 1);
		addExampleEdge(graph, "n1", "n7", "char", 20);
		addExampleEdge(graph, "n1", "n4", "char", 1);
		addExampleEdge(graph, "n1", "n5", "char", 1);

		addExampleEdge(graph, "n2", "n6", "char", 1);
		addExampleEdge(graph, "n2", "n7", "char", 5);
		addExampleEdge(graph, "n2", "n8", "char", 5);

		addExampleEdge(graph, "n3", "n7", "char", 5);
		addExampleEdge(graph, "n3", "n8", "char", 1);

		addExampleEdge(graph, "n4", "n8", "char", 1);

		addExampleEdge(graph, "n5", "n8", "char", 10);

		addExampleEdge(graph, "n6", "n9", "char", 10);

		addExampleEdge(graph, "n7", "n9", "char", 10);

		addExampleEdge(graph, "n8", "n9", "char", 10);

		return graph;
	}

	@Override
	public void transform(SDFGraph algorithm, SDFGraph transformedAlgorithm) {
		// TODO Auto-generated method stub

	}

}
