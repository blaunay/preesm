package org.ietr.preesm.plugin.mapper.scenariogen;

import net.sf.dftools.algorithm.factories.SDFEdgeFactory;
import net.sf.dftools.algorithm.factories.SDFVertexFactory;
import net.sf.dftools.algorithm.importer.GMLImporter;
import net.sf.dftools.algorithm.model.sdf.SDFAbstractVertex;
import net.sf.dftools.algorithm.model.sdf.SDFEdge;
import net.sf.dftools.algorithm.model.sdf.SDFGraph;

import org.ietr.preesm.plugin.mapper.model.MapperDAG;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Importer for DAG graphs produced by the mapper
 * 
 * @author mpelcat
 * 
 */
public class GMLMapperDAGImporter extends
		GMLImporter<SDFGraph, SDFAbstractVertex, SDFEdge> {

	/**
	 * COnstructs a new importer for SDF graphs
	 */
	public GMLMapperDAGImporter() {
		super(new SDFEdgeFactory());
	}

	/**
	 * Parses an Edge in the DOM document
	 * 
	 * @param edgeElt
	 *            The DOM Element
	 * @param parentGraph
	 *            The parent Graph of this Edge
	 */
	public void parseEdge(Element edgeElt, SDFGraph parentGraph) {
	}

	/**
	 * Parses a Graph in the DOM document
	 * 
	 * @param graphElt
	 *            The graph Element in the DOM document
	 * @return The parsed graph
	 */
	public SDFGraph parseGraph(Element graphElt) {
		SDFGraph graph = new SDFGraph((SDFEdgeFactory) edgeFactory);
		NodeList childList = graphElt.getChildNodes();
		parseParameters(graph, graphElt);
		parseVariables(graph, graphElt);
		for (int i = 0; i < childList.getLength(); i++) {
			if (childList.item(i).getNodeName().equals("node")) {
				Element vertexElt = (Element) childList.item(i);
				parseNode(vertexElt, graph);
			}
		}
		for (int i = 0; i < childList.getLength(); i++) {
			if (childList.item(i).getNodeName().equals("edge")) {
				Element edgeElt = (Element) childList.item(i);
				parseEdge(edgeElt, graph);
			}
		}
		parseKeys(graphElt, graph);
		return graph;
	}

	/**
	 * Parses a Vertex from the DOM document
	 * 
	 * @param vertexElt
	 *            The node Element in the DOM document
	 * @return The parsed node
	 */
	public SDFAbstractVertex parseNode(Element vertexElt, SDFGraph parentGraph) {

		SDFAbstractVertex vertex;
		/*
		 * HashMap<String, String> attributes = new HashMap<String, String>();
		 * for (int i = 0; i < vertexElt.getAttributes().getLength(); i++) {
		 * attributes.put(vertexElt.getAttributes().item(i).getNodeName(),
		 * vertexElt.getAttributes().item(i).getNodeValue()); }
		 * 
		 * attributes.put("kind", SDFVertex.VERTEX);
		 */
		vertex = SDFVertexFactory.getInstance().createVertex(vertexElt);

		vertex.setId(vertexElt.getAttribute("id"));

		for (int i = 0; i < vertexElt.getChildNodes().getLength(); i++) {
			Node n = vertexElt.getChildNodes().item(i);
			if (n.getNodeName().equals("data")) {
				vertex.getPropertyBean().setValue(
						n.getAttributes().getNamedItem("key").getTextContent(),
						n.getTextContent());
			}
		}

		parseKeys(vertexElt, vertex);
		vertexFromId.put(vertex.getId(), vertex);
		parseArguments(vertex, vertexElt);
		return vertex;
	}

	@Override
	public SDFAbstractVertex parsePort(Element portElt, SDFGraph parentGraph) {
		return null;
	}

}
