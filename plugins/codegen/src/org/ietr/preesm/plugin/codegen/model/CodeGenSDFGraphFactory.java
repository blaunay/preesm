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
 
package org.ietr.preesm.plugin.codegen.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.ietr.preesm.core.codegen.model.CodeGenSDFEdge;
import org.ietr.preesm.core.codegen.model.CodeGenSDFGraph;
import org.ietr.preesm.core.codegen.model.CodeGenSDFTaskVertex;
import org.ietr.preesm.core.codegen.model.ICodeGenSDFVertex;
import org.jgrapht.alg.StrongConnectivityInspector;
import org.sdf4j.SDFMath;
import org.sdf4j.demo.SDFAdapterDemo;
import org.sdf4j.demo.SDFtoDAGDemo;
import org.sdf4j.factories.DAGVertexFactory;
import org.sdf4j.importer.GMLSDFImporter;
import org.sdf4j.importer.InvalidFileException;
import org.sdf4j.iterators.SDFIterator;
import org.sdf4j.model.AbstractEdge;
import org.sdf4j.model.dag.DAGEdge;
import org.sdf4j.model.dag.DAGVertex;
import org.sdf4j.model.dag.DirectedAcyclicGraph;
import org.sdf4j.model.parameters.InvalidExpressionException;
import org.sdf4j.model.sdf.SDFAbstractVertex;
import org.sdf4j.model.sdf.SDFEdge;
import org.sdf4j.model.sdf.SDFGraph;
import org.sdf4j.model.sdf.SDFInterfaceVertex;
import org.sdf4j.model.sdf.SDFVertex;
import org.sdf4j.model.sdf.esdf.SDFForkVertex;
import org.sdf4j.model.sdf.esdf.SDFJoinVertex;
import org.sdf4j.model.sdf.esdf.SDFRoundBufferVertex;
import org.sdf4j.model.sdf.esdf.SDFSinkInterfaceVertex;
import org.sdf4j.model.sdf.esdf.SDFSourceInterfaceVertex;
import org.sdf4j.model.sdf.types.SDFIntEdgePropertyType;
import org.sdf4j.model.sdf.visitors.DAGTransformation;
import org.sdf4j.model.visitors.SDF4JException;

/**
 * @author jpiat
 */
public class CodeGenSDFGraphFactory {
	
	private IFile mainFile ;
	
	public CodeGenSDFGraphFactory(IFile parentAlgoFile){
		mainFile = parentAlgoFile ;
	}
	
	public static void main(String [] args){
		SDFAdapterDemo applet1 = new SDFAdapterDemo();
		SDFtoDAGDemo applet2 = new SDFtoDAGDemo();
		GMLSDFImporter importer = new GMLSDFImporter();
		// SDFGraph demoGraph = createTestComGraph();
		SDFGraph demoGraph;
		try {
			
			 demoGraph = importer.parse(new File(
			  "D:\\Preesm\\trunk\\tests\\SmallTestCase\\Algo\\TestCase.graphml"));
			 
			/*demoGraph = importer.parse(new File(
					"D:\\Preesm\\trunk\\tests\\UMTS\\Tx_UMTS.graphml"));*/
			DAGTransformation<DirectedAcyclicGraph> dageur = new DAGTransformation<DirectedAcyclicGraph>(
					new DirectedAcyclicGraph(), new DAGVertexFactory());
			SDFGraph dag = demoGraph.clone();
			dag.accept(dageur);
			applet2.init(dageur.getOutput());
			/*CodeGenSDFGraphFactory codeGenGraphFactory = new CodeGenSDFGraphFactory();
			CodeGenSDFGraph codeGenGraph = codeGenGraphFactory.create(dageur.getOutput());
			System.out.println(codeGenGraph.toString());*/
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SDF4JException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public CodeGenSDFGraph create(DirectedAcyclicGraph dag) throws InvalidExpressionException, SDF4JException{
		CodeGenSDFVertexFactory vertexFactory = new CodeGenSDFVertexFactory(mainFile) ;
		HashMap<DAGVertex, SDFAbstractVertex> aliases = new  HashMap<DAGVertex, SDFAbstractVertex>() ;
		CodeGenSDFGraph output = new CodeGenSDFGraph(dag.getName()) ;
		output.copyProperties(dag);
		for(DAGVertex vertex : dag.vertexSet()){
			SDFAbstractVertex codeGenVertex = vertexFactory.create(vertex);
			if(codeGenVertex instanceof CodeGenSDFTaskVertex){
				((ICodeGenSDFVertex)codeGenVertex).setNbRepeat(vertex.getNbRepeat().intValue());
			}
			aliases.put(vertex, codeGenVertex);
			output.addVertex(codeGenVertex);
		}
		for(DAGEdge edge : dag.edgeSet()){
			DAGVertex source = edge.getSource();
			DAGVertex target = edge.getTarget();
			SDFAbstractVertex newSource = aliases.get(source);
			SDFAbstractVertex newTarget = aliases.get(target);
			for(AbstractEdge subEdge : edge.getAggregate()){
				if(subEdge instanceof SDFEdge){
					SDFEdge sdfSubEdge = (SDFEdge) subEdge ;
					CodeGenSDFEdge newEdge = (CodeGenSDFEdge) output.addEdge(newSource, newTarget);
					SDFInterfaceVertex sourceInterface = null;
					SDFInterfaceVertex targetInterface = null;
					if((sourceInterface = newSource.getInterface(sdfSubEdge.getSourceInterface().getName())) == null){
						sourceInterface = new SDFSinkInterfaceVertex();
						sourceInterface.setName(sdfSubEdge.getSourceInterface().getName());
						newSource.addSink(sourceInterface);
					}
					if((targetInterface = newSource.getInterface(sdfSubEdge.getTargetInterface().getName())) == null){
						targetInterface = new SDFSourceInterfaceVertex();
						targetInterface.setName(sdfSubEdge.getTargetInterface().getName());
						newTarget.addSource(targetInterface);
					}
					newEdge.setSourceInterface(sourceInterface);
					newEdge.setTargetInterface(targetInterface);
					newEdge.setCons(new SDFIntEdgePropertyType(sdfSubEdge.getCons().intValue()));
					newEdge.setProd(new SDFIntEdgePropertyType(sdfSubEdge.getProd().intValue()));
					newEdge.setDelay(new SDFIntEdgePropertyType(sdfSubEdge.getDelay().intValue()));
					newEdge.setDataType(sdfSubEdge.getDataType());
				}
			}
		}

		
		treatExplodeImplodePattern(output);
		treatDummyImplode(output);
		treatDummyExplode(output);
		treatImplodeRoundBufferPattern(output);
		return output ;
	}
	
	public CodeGenSDFGraph create(SDFGraph sdf) throws InvalidExpressionException, SDF4JException{
		clusterizeStronglyConnected(sdf);
		CodeGenSDFVertexFactory vertexFactory = new CodeGenSDFVertexFactory(mainFile) ;
		HashMap<SDFAbstractVertex, SDFAbstractVertex> aliases = new  HashMap<SDFAbstractVertex, SDFAbstractVertex>() ;
		CodeGenSDFGraph output = new CodeGenSDFGraph(sdf.getName()) ;
		SDFIterator iterator = new SDFIterator(sdf);
		int pos = 0 ;
		while(iterator.hasNext()){
			SDFAbstractVertex vertex = iterator.next();
			SDFAbstractVertex codeGenVertex = vertexFactory.create(vertex);
			if(codeGenVertex instanceof CodeGenSDFTaskVertex){
				((ICodeGenSDFVertex)codeGenVertex).setNbRepeat(vertex.getNbRepeat());
				((ICodeGenSDFVertex) codeGenVertex).setPos(pos);
				pos ++ ;
			}
			aliases.put(vertex, codeGenVertex);
			output.addVertex(codeGenVertex);
		}
		for(SDFEdge edge : sdf.edgeSet()){
			SDFAbstractVertex source = edge.getSource();
			SDFAbstractVertex target = edge.getTarget();
			SDFAbstractVertex newSource = aliases.get(source);
			SDFAbstractVertex newTarget = aliases.get(target);
			CodeGenSDFEdge newEdge = (CodeGenSDFEdge) output.addEdge(newSource, newTarget);
			SDFInterfaceVertex sourceInterface = null;
			SDFInterfaceVertex targetInterface = null;
			if((sourceInterface = newSource.getInterface(edge.getSourceInterface().getName())) == null){
				sourceInterface = new SDFSinkInterfaceVertex();
				sourceInterface.setName(edge.getSourceInterface().getName());
				newSource.addSink(sourceInterface);
			}
			if((targetInterface = newTarget.getInterface(edge.getTargetInterface().getName())) == null){
				targetInterface = new SDFSourceInterfaceVertex();
				targetInterface.setName(edge.getTargetInterface().getName());
				newTarget.addSource(targetInterface);
			}
			newEdge.setSourceInterface(sourceInterface);
			newEdge.setTargetInterface(targetInterface);
			newEdge.setCons(new SDFIntEdgePropertyType(edge.getCons().intValue()));
			newEdge.setProd(new SDFIntEdgePropertyType(edge.getProd().intValue()));
			newEdge.setDelay(new SDFIntEdgePropertyType(edge.getDelay().intValue()));
			newEdge.setDataType(edge.getDataType());
		}
		return output ;
	}
	
	public void clusterizeStronglyConnected(SDFGraph graph) throws SDF4JException{
		int i = 0 ;
		StrongConnectivityInspector<SDFAbstractVertex, SDFEdge> inspector = new StrongConnectivityInspector<SDFAbstractVertex, SDFEdge>(graph) ;
		for(Set<SDFAbstractVertex> strong : inspector.stronglyConnectedSets()){
			boolean noInterface = true ;
			for(SDFAbstractVertex vertex :strong){
				noInterface &= !(vertex instanceof SDFInterfaceVertex) ;
			}
			if(noInterface && strong.size() > 1){
				try {
					culsterizeLoopTest(graph, new ArrayList<SDFAbstractVertex>(strong), "cluster_"+i);
				} catch (InvalidExpressionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				i ++ ;
			}
			
		}
	}
	
	public static SDFAbstractVertex culsterizeLoopTest(SDFGraph graph,
			List<SDFAbstractVertex> block, String name) throws InvalidExpressionException, SDF4JException {
		try {
			graph.validateModel();
		} catch (SDF4JException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		
		if (block.size() > 1) {
			int pgcd = 0 ;
			int nbLoopPort = 0 ;
			SDFGraph clusterGraph = graph.clone();
			clusterGraph.setName(name);
			SDFVertex cluster = new SDFVertex();
			cluster.setName(name);
			cluster.setGraphDescription(clusterGraph);
			graph.addVertex(cluster);
			HashMap<SDFAbstractVertex, SDFAbstractVertex> copies = new HashMap<SDFAbstractVertex, SDFAbstractVertex>();
			List<SDFAbstractVertex> vertices = new ArrayList<SDFAbstractVertex>(clusterGraph.vertexSet());
			for(int i = 0 ; i < vertices.size() ; i ++){
				boolean isInBlock = false;
				for (int r = 0; r < block.size(); r++) {
					if(block.get(r).getName().equals(vertices.get(i).getName())){
						isInBlock = true ;
						copies.put(block.get(r), vertices.get(i));
					}
				}
				if(!isInBlock){
					clusterGraph.removeVertex(vertices.get(i));
				}
			}
			for (int r = 0; r < block.size(); r++) {
				SDFAbstractVertex seed = copies.get(block.get(r));
				if(pgcd == 0){
					pgcd = seed.getNbRepeat();
				}else{
					pgcd = SDFMath.gcd(pgcd, seed.getNbRepeat());
				}
				List<SDFEdge> outgoingEdges = new ArrayList<SDFEdge>(graph
						.outgoingEdgesOf(block.get(r)));
				for (SDFEdge edge : outgoingEdges) {
					SDFAbstractVertex target = graph.getEdgeTarget(edge);
					if(!block.contains(target)){
						SDFInterfaceVertex targetPort = new SDFSinkInterfaceVertex();
						targetPort.setName(cluster.getName() + "_"
								+ edge.getTargetInterface().getName());
						cluster.addSink(targetPort);
						SDFEdge extEdge = graph.addEdge(cluster, target);
						extEdge.copyProperties(edge);
						extEdge.setSourceInterface(targetPort);
						cluster.setInterfaceVertexExternalLink(extEdge, targetPort);
						SDFEdge newEdge = clusterGraph
								.addEdge(seed, targetPort);
						newEdge.copyProperties(edge);
						newEdge.setCons(new SDFIntEdgePropertyType(newEdge.getProd().intValue()));
						graph.removeEdge(edge);
					}
				}
				List<SDFEdge> incomingEdges = new ArrayList<SDFEdge>(graph
						.incomingEdgesOf(block.get(r)));
				for (SDFEdge edge : incomingEdges) {
					SDFAbstractVertex source = graph.getEdgeSource(edge);
					SDFAbstractVertex target = graph.getEdgeTarget(edge);
					if(block.contains(source) && block.contains(target)&& edge.getDelay().intValue() > 0){
						SDFInterfaceVertex targetPort = new SDFSinkInterfaceVertex();
						targetPort.setName("outLoopPort_"+nbLoopPort);
						SDFInterfaceVertex sourcePort = new SDFSourceInterfaceVertex();
						sourcePort.setName("inLoopPort_"+nbLoopPort);
						nbLoopPort ++ ;
						cluster.addSink(targetPort);
						cluster.addSource(sourcePort);
						
						SDFEdge loopEdge = graph.addEdge(cluster, cluster);
						loopEdge.copyProperties(edge);
						loopEdge.setSourceInterface(targetPort);
						loopEdge.setTargetInterface(sourcePort);
						
						
						SDFEdge lastLoop = clusterGraph.addEdge(copies.get(source), targetPort);
						lastLoop.copyProperties(edge);
						lastLoop.setDelay(new SDFIntEdgePropertyType(0));
						
						SDFEdge firstLoop = clusterGraph.addEdge(sourcePort, copies.get(target));
						firstLoop.copyProperties(edge);
						firstLoop.setDelay(new SDFIntEdgePropertyType(0));
						SDFEdge inLoopEdge = clusterGraph.getEdge(copies.get(source), copies.get(target));
						if(inLoopEdge.getDelay().intValue() > 0){
							clusterGraph.removeEdge(inLoopEdge);
						}
						graph.removeEdge(edge);
					}else if (!block.contains(source)) {
						SDFInterfaceVertex sourcePort = new SDFSourceInterfaceVertex();
						sourcePort.setName(cluster.getName() + "_"
								+ edge.getSourceInterface().getName());
						cluster.addSource(sourcePort);
						SDFEdge extEdge = graph.addEdge(source, cluster);
						extEdge.copyProperties(edge);
						extEdge.setTargetInterface(sourcePort);
						cluster.setInterfaceVertexExternalLink(extEdge, sourcePort);
						SDFEdge newEdge = clusterGraph
								.addEdge(sourcePort, seed);
						newEdge.copyProperties(edge);
						newEdge.setProd(newEdge.getCons());
						graph.removeEdge(edge);
					}
				}
			}
			for (int r = 0; r < block.size(); r++) {
				graph.removeVertex(block.get(r));
			}
			clusterGraph.validateModel();	
			cluster.setNbRepeat(pgcd);
			return cluster;
		}else{
			return null ;
		}
	}
	
	public static SDFAbstractVertex culsterizeLoop(SDFGraph graph,
			List<SDFAbstractVertex> block, String name) throws InvalidExpressionException, SDF4JException {
		
		if (block.size() > 1) {
			int pgcd = 0 ;
			int nbLoopPort = 0 ;
			SDFGraph clusterGraph = new SDFGraph();
			clusterGraph.setName(name);
			SDFVertex cluster = new SDFVertex();
			cluster.setName(name);
			cluster.setGraphDescription(clusterGraph);
			graph.addVertex(cluster);
			for (int r = 0; r < block.size(); r++) {
				SDFAbstractVertex seed = block.get(r);
				if(! clusterGraph.vertexSet().contains(seed)){
					clusterGraph.addVertex(seed);
				}
				if(pgcd == 0){
					pgcd = seed.getNbRepeat();
				}else{
					pgcd = SDFMath.gcd(pgcd, seed.getNbRepeat());
				}
				List<SDFEdge> outgoingEdges = new ArrayList<SDFEdge>(graph
						.outgoingEdgesOf(seed));
				for (SDFEdge edge : outgoingEdges) {
					SDFAbstractVertex target = graph.getEdgeTarget(edge);

					if (block.contains(target) && edge.getDelay().intValue() == 0) {
						if (!clusterGraph.vertexSet().contains(target)) {
							clusterGraph.addVertex(target);
						}
						SDFEdge newEdge = clusterGraph.addEdge(seed, target);
						newEdge.copyProperties(edge);
					} else if(!block.contains(target)){
						SDFInterfaceVertex targetPort = new SDFSinkInterfaceVertex();
						targetPort.setName(cluster.getName() + "_"
								+ edge.getTargetInterface().getName());
						cluster.addSink(targetPort);
						SDFEdge extEdge = graph.addEdge(cluster, target);
						extEdge.copyProperties(edge);
						extEdge.setSourceInterface(targetPort);
						cluster.setInterfaceVertexExternalLink(extEdge, targetPort);
						SDFEdge newEdge = clusterGraph
								.addEdge(seed, targetPort);
						newEdge.copyProperties(edge);
						newEdge.setCons(new SDFIntEdgePropertyType(newEdge.getProd().intValue()));
					}
					graph.removeEdge(edge);
				}
				List<SDFEdge> incomingEdges = new ArrayList<SDFEdge>(graph
						.incomingEdgesOf(seed));
				for (SDFEdge edge : incomingEdges) {
					SDFAbstractVertex source = graph.getEdgeSource(edge);
					if(block.contains(source) && edge.getDelay().intValue() > 0){
						SDFInterfaceVertex targetPort = new SDFSinkInterfaceVertex();
						targetPort.setName("outLoopPort_"+nbLoopPort);
						SDFInterfaceVertex sourcePort = new SDFSourceInterfaceVertex();
						sourcePort.setName("inLoopPort_"+nbLoopPort);
						nbLoopPort ++ ;
						cluster.addSink(targetPort);
						cluster.addSource(sourcePort);
						
						SDFEdge loopEdge = graph.addEdge(cluster, cluster);
						loopEdge.copyProperties(edge);
						edge.setSourceInterface(targetPort);
						edge.setTargetInterface(sourcePort);
						
						SDFAbstractVertex target = graph.getEdgeTarget(edge);
						
						SDFEdge lastLoop = clusterGraph.addEdge(source, targetPort);
						lastLoop.copyProperties(edge);
						lastLoop.setDelay(new SDFIntEdgePropertyType(0));
						
						SDFEdge firstLoop = clusterGraph.addEdge(sourcePort, target);
						firstLoop.copyProperties(edge);
						firstLoop.setDelay(new SDFIntEdgePropertyType(0));
					}else if (!block.contains(source)) {
						SDFInterfaceVertex sourcePort = new SDFSourceInterfaceVertex();
						sourcePort.setName(cluster.getName() + "_"
								+ edge.getSourceInterface().getName());
						cluster.addSource(sourcePort);
						SDFEdge extEdge = graph.addEdge(source, cluster);
						extEdge.copyProperties(edge);
						extEdge.setTargetInterface(sourcePort);
						cluster.setInterfaceVertexExternalLink(extEdge, sourcePort);
						SDFEdge newEdge = clusterGraph
								.addEdge(sourcePort, seed);
						newEdge.copyProperties(edge);
						newEdge.setProd(newEdge.getCons());
						graph.removeEdge(edge);
					}
				}
			}
			for (int r = 0; r < block.size(); r++) {
				graph.removeVertex(block.get(r));
			}
			clusterGraph.validateModel();	
			cluster.setNbRepeat(pgcd);
			return cluster;
		}
		return null;
	}
	
	
	protected void treatDummyImplode(CodeGenSDFGraph graph) {
		List<SDFAbstractVertex> vertices = new ArrayList<SDFAbstractVertex>(
				graph.vertexSet());
		while (vertices.size() > 0) {
			SDFAbstractVertex vertex = vertices.get(0);
			vertices.remove(0);
			if (vertex instanceof SDFJoinVertex) {
				if(graph.incomingEdgesOf(vertex).size() ==  1 && graph.outgoingEdgesOf(vertex).size() == 1){
					SDFEdge inEdge = (SDFEdge) graph.incomingEdgesOf(vertex).toArray()[0];
					SDFEdge outEdge = (SDFEdge) graph.outgoingEdgesOf(vertex).toArray()[0];
					if(outEdge.getTarget() instanceof SDFJoinVertex){
						SDFAbstractVertex trueSource = inEdge.getSource();
						SDFAbstractVertex trueTarget = outEdge.getTarget();
						int index = ((SDFJoinVertex) trueTarget).getEdgeIndex(outEdge);
						SDFEdge newEdge = graph.addEdge(trueSource, trueTarget);
						newEdge.copyProperties(inEdge);
						newEdge.setSourceInterface(inEdge.getSourceInterface());
						newEdge.setTargetInterface(outEdge.getTargetInterface());
						((SDFJoinVertex) trueTarget).setConnectionIndex(newEdge, index);
						graph.removeEdge(inEdge);
						graph.removeEdge(outEdge);
					}else{
						SDFAbstractVertex trueSource = inEdge.getSource();
						SDFAbstractVertex trueTarget = outEdge.getTarget();
						SDFEdge newEdge = graph.addEdge(trueSource, trueTarget);
						newEdge.copyProperties(inEdge);
						newEdge.setSourceInterface(inEdge.getSourceInterface());
						newEdge.setTargetInterface(outEdge.getTargetInterface());
						graph.removeEdge(inEdge);
						graph.removeEdge(outEdge);
					}
					graph.removeVertex(vertex);
				}
				
			}
		}

	}

	
	protected void treatExplodeImplodePattern(CodeGenSDFGraph graph) throws InvalidExpressionException {
		List<SDFAbstractVertex> vertices = new ArrayList<SDFAbstractVertex>(
				graph.vertexSet());
		while (vertices.size() > 0) {
			SDFAbstractVertex vertex = vertices.get(0);
			vertices.remove(0);
			if (vertex instanceof SDFJoinVertex) {
				SDFEdge edge = (SDFEdge) graph.outgoingEdgesOf(vertex)
						.toArray()[0];
				if (edge.getTarget() instanceof SDFForkVertex) {
					SDFJoinVertex joinVertex = (SDFJoinVertex) edge.getSource();
					SDFForkVertex forkVertex = (SDFForkVertex) edge.getTarget();
					List<SDFAbstractVertex> targetVertices = new ArrayList<SDFAbstractVertex>();
					List<SDFAbstractVertex> sourceVertices = new ArrayList<SDFAbstractVertex>();
					Map<SDFAbstractVertex, SDFEdge> connectionEdge = new HashMap<SDFAbstractVertex, SDFEdge>();
					for (SDFEdge inEdge : joinVertex.getIncomingConnections()) {
							sourceVertices.add(inEdge.getSource());
							connectionEdge.put(inEdge.getSource(), inEdge);
					}
					for (SDFEdge outEdge : forkVertex.getOutgoingConnections()) {
						targetVertices.add(outEdge.getTarget());
						connectionEdge.put(outEdge.getTarget(), outEdge);
					}
					if (sourceVertices.size() == targetVertices.size()) {
						int inc = 0;
						for (SDFAbstractVertex srcVertex : sourceVertices) {
							SDFEdge newEdge = graph.addEdge(srcVertex,
									targetVertices.get(inc));
							vertices.remove(srcVertex);
							vertices.remove(targetVertices.get(inc));
							newEdge.copyProperties(joinVertex
									.getIncomingConnections().get(inc));
							newEdge.setProd(new SDFIntEdgePropertyType(connectionEdge.get(srcVertex).getProd().intValue()));
							newEdge.setCons(new SDFIntEdgePropertyType(connectionEdge.get(targetVertices.get(inc)).getCons().intValue()));
							newEdge.setSourceInterface(connectionEdge.get(srcVertex).getSourceInterface());
							newEdge.setTargetInterface(connectionEdge.get(targetVertices.get(inc)).getTargetInterface());
							if (edge.getDelay().intValue() > 0) {
								newEdge.setDelay(new SDFIntEdgePropertyType(
										newEdge.getProd().intValue()));
							}
							inc++;
						}
						graph.removeVertex(joinVertex);
						graph.removeVertex(forkVertex);
						vertices.remove(forkVertex);
					}
				}

			}
		}

	}
	
	protected void treatImplodeRoundBufferPattern(CodeGenSDFGraph graph) {
		List<SDFAbstractVertex> vertices = new ArrayList<SDFAbstractVertex>(
				graph.vertexSet());
		while (vertices.size() > 0) {
			SDFAbstractVertex vertex = vertices.get(0);
			vertices.remove(0);
			if (vertex instanceof SDFJoinVertex) {
				SDFEdge outEdge = (SDFEdge) graph.outgoingEdgesOf(vertex).toArray()[0];
				if(outEdge.getTarget() instanceof SDFRoundBufferVertex){
					for(SDFEdge inEdge : graph.incomingEdgesOf(vertex)){
						SDFAbstractVertex trueSource = inEdge.getSource();
						SDFAbstractVertex trueTarget = outEdge.getTarget();
						SDFEdge newEdge = graph.addEdge(trueSource, trueTarget);
						newEdge.copyProperties(inEdge);
						newEdge.setSourceInterface(inEdge.getSourceInterface());
						newEdge.setTargetInterface(outEdge.getTargetInterface());
					}
					graph.removeVertex(vertex);
				}
				
			}
		}

	}
	
	
	protected void treatDummyExplode(CodeGenSDFGraph graph) {
		List<SDFAbstractVertex> vertices = new ArrayList<SDFAbstractVertex>(
				graph.vertexSet());
		while (vertices.size() > 0) {
			SDFAbstractVertex vertex = vertices.get(0);
			vertices.remove(0);
			if (vertex instanceof SDFForkVertex) {
				if(graph.incomingEdgesOf(vertex).size() ==  1 && graph.outgoingEdgesOf(vertex).size() == 1){
					SDFEdge inEdge = (SDFEdge) graph.incomingEdgesOf(vertex).toArray()[0];
					SDFEdge outEdge = (SDFEdge) graph.outgoingEdgesOf(vertex).toArray()[0];
					if(inEdge.getSource() instanceof SDFForkVertex){
						SDFAbstractVertex trueSource = inEdge.getSource();
						SDFAbstractVertex trueTarget = outEdge.getTarget();
						int index = ((SDFForkVertex) trueSource).getEdgeIndex(inEdge);
						SDFEdge newEdge = graph.addEdge(trueSource, trueTarget);
						newEdge.copyProperties(outEdge);
						newEdge.setSourceInterface(inEdge.getSourceInterface());
						newEdge.setTargetInterface(outEdge.getTargetInterface());
						((SDFForkVertex) trueSource).setConnectionIndex(newEdge, index);
						graph.removeEdge(inEdge);
						graph.removeEdge(outEdge);
					}else{
						SDFAbstractVertex trueSource = inEdge.getSource();
						SDFAbstractVertex trueTarget = outEdge.getTarget();
						SDFEdge newEdge = graph.addEdge(trueSource, trueTarget);
						newEdge.copyProperties(outEdge);
						newEdge.setSourceInterface(inEdge.getSourceInterface());
						newEdge.setTargetInterface(outEdge.getTargetInterface());
						graph.removeEdge(inEdge);
						graph.removeEdge(outEdge);
					}
					graph.removeVertex(vertex);
				}
				
			}
		}

	}


}
