/*********************************************************
Copyright or � or Copr. IETR/INSA: Matthieu Wipliez, Jonathan Piat,
Maxime Pelcat, Peng Cheng Mu, Jean-Fran�ois Nezan, Micka�l Raulet

[mwipliez,jpiat,mpelcat,pmu,jnezan,mraulet]@insa-rennes.fr

This software is a computer program whose purpose is to prototype
parallel applications.

This software is governed by the CeCILL-B license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-B
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
knowledge of the CeCILL-B license and that you accept its terms.
 *********************************************************/
 
package org.ietr.preesm.plugin.codegen;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ietr.preesm.core.architecture.Examples;
import org.ietr.preesm.core.architecture.MultiCoreArchitecture;
import org.ietr.preesm.core.architecture.Operator;
import org.ietr.preesm.core.architecture.simplemodel.Medium;
import org.ietr.preesm.core.codegen.SourceFileList;
import org.ietr.preesm.core.codegen.VertexType;
import org.ietr.preesm.core.codegen.sdfProperties.BufferAggregate;
import org.ietr.preesm.core.codegen.sdfProperties.BufferProperties;
import org.ietr.preesm.core.log.PreesmLogger;
import org.ietr.preesm.core.task.ICodeGeneration;
import org.ietr.preesm.core.task.TaskResult;
import org.ietr.preesm.core.task.TextParameters;
import org.ietr.preesm.plugin.codegen.print.PrinterChooser;
import org.sdf4j.model.dag.DAGEdge;
import org.sdf4j.model.dag.DAGVertex;
import org.sdf4j.model.dag.DirectedAcyclicGraph;
import org.sdf4j.model.sdf.SDFDefaultEdgePropertyType;

/**
 * Code generation.
 * 
 * @author Matthieu Wipliez
 */
public class CodeGeneration implements ICodeGeneration {

	/**
	 * Main for test
	 */
	public static void main(String[] args) {
		Logger logger = PreesmLogger.getLogger();
		logger.setLevel(Level.FINER);

		logger.log(Level.FINER, "Testing code generation with an example");

		// Creating generator
		CodeGeneration gen = new CodeGeneration();

		// Input archi & algo
		// TODO: parse algorithm
		MultiCoreArchitecture architecture = Examples.get2C64Archi();
		DirectedAcyclicGraph algorithm = null;

		// Input file list
		Map<String, String> map = new HashMap<String, String>();
		map.put("sourcePath", "d:/Test");
		TextParameters params = new TextParameters(map);

		gen.transform(algorithm, architecture, params);

		logger.log(Level.FINER, "Code generated");
	}

	/**
	 * Adding send and receive between v1 and v2. It removes the original vertex
	 * and copies its buffer aggregate
	 */
	public DAGVertex addComVertices(DAGVertex v1, DAGVertex v2, Medium medium,
			Operator sendOp, Operator receiveOp, int schedulingOrder) {
		DirectedAcyclicGraph graph = v1.getBase();

		DAGEdge originalEdge = graph.getEdge(v1, v2);
		Object aggregate = originalEdge.getPropertyBean().getValue(
				BufferAggregate.propertyBeanName);
		graph.removeEdge(originalEdge);

		// Tagging the communication vertices with their operator, type and
		// medium
		DAGVertex send = new DAGVertex();
		send.setId("snd" + v2.getName() + sendOp.getName());
		send.getPropertyBean().setValue("schedulingOrder", schedulingOrder);
		send.getPropertyBean().setValue(VertexType.propertyBeanName,
				VertexType.send);
		send.getPropertyBean().setValue(Medium.propertyBeanName, medium);
		send.getPropertyBean().setValue(Operator.propertyBeanName, sendOp);

		DAGVertex receive = new DAGVertex();
		receive.setId("rcv" + v1.getName() + receiveOp.getName());
		receive.getPropertyBean().setValue("schedulingOrder", schedulingOrder);
		receive.getPropertyBean().setValue(VertexType.propertyBeanName,
				VertexType.receive);
		receive.getPropertyBean().setValue(Medium.propertyBeanName, medium);
		receive.getPropertyBean()
				.setValue(Operator.propertyBeanName, receiveOp);

		graph.addVertex(send);
		graph.addVertex(receive);

		graph.addEdge(v1, send).getPropertyBean().setValue(
				BufferAggregate.propertyBeanName, aggregate);
		graph.addEdge(send, receive).getPropertyBean().setValue(
				BufferAggregate.propertyBeanName, aggregate);
		graph.addEdge(receive, v2).getPropertyBean().setValue(
				BufferAggregate.propertyBeanName, aggregate);

		return receive;
	}

	/**
	 * Adding a new edge to graph from a few properties
	 */
	public DAGEdge addExampleEdge(DirectedAcyclicGraph graph, String source,
			String dest, String type, int prodCons) {
		DAGEdge edge;

		edge = graph.addEdge(graph.getVertex(source), graph.getVertex(dest));
		edge.getPropertyBean().setValue("dataType", type);

		// DAG => prod = cons
		edge.setWeight(new SDFDefaultEdgePropertyType(prodCons));

		// DAG => no delay

		// Example buffer aggregate with one single buffer
		BufferAggregate agg = new BufferAggregate();
		agg.add(new BufferProperties(type, "out", "in", prodCons));

		edge.getPropertyBean().setValue(BufferAggregate.propertyBeanName, agg);

		return edge;
	}

	/**
	 * Generates the source files from an implementation and an architecture.
	 * The implementation is a tagged SDF graph.
	 */
	private void generateSourceFiles(DirectedAcyclicGraph algorithm,
			MultiCoreArchitecture architecture, SourceFileList list) {
		CodeGenerator codegen = new CodeGenerator(list);
		codegen.generateSourceFiles(algorithm, architecture);
	}

	@Override
	public TaskResult transform(DirectedAcyclicGraph algorithm,
			MultiCoreArchitecture architecture, TextParameters parameters) {
		String sourcePath = parameters.getVariable("sourcePath");
		TaskResult result = new TaskResult();
		SourceFileList list = new SourceFileList();

		// Generate source file class
		generateSourceFiles(algorithm, architecture, list);

		// Generates the code
		PrinterChooser printerChooser = new PrinterChooser(sourcePath);
		printerChooser.printList(list);

		result.setSourcefilelist(list);

		return result;
	}

}
