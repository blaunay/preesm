package org.ietr.preesm.experiment.model.pimemoc.serialize;

import java.io.InputStream;

import net.sf.dftools.architecture.utils.DomUtil;

import org.eclipse.emf.common.util.URI;
import org.ietr.preesm.experiment.model.pimemoc.Actor;
import org.ietr.preesm.experiment.model.pimemoc.Graph;
import org.ietr.preesm.experiment.model.pimemoc.PIMeMoCFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GraphMLParser {

	/**
	 * The URI of the parsed file
	 */
	private URI uri;

	public GraphMLParser(URI uri) {
		this.uri = uri;
	}

	/**
	 * Parse the PIMeMoC {@link Graph} from the given {@link InputStream} using
	 * the GraphML format.
	 * 
	 * @param inputStream
	 *            The Parsed input stream
	 * @return The parsed Graph or null is something went wrong
	 */
	public Graph parse(InputStream inputStream) {
		// Instantiate the graph that will be filled with parser informations
		Graph graph = PIMeMoCFactory.eINSTANCE.createGraph();

		// Parse the input stream
		Document document = DomUtil.parseDocument(inputStream);

		// Retrieve the root element
		Element rootElt = document.getDocumentElement();

		try {
			// Fill the graph with parsed information
			parseGraphML(rootElt, graph);
		} catch (RuntimeException e) {
			e.printStackTrace();
			return null;
		}

		return null;
	}

	/**
	 * Parse the root element of the GraphML description
	 * 
	 * @param parentElt
	 *            The Element to fill (could be removed later if it is always
	 *            rootElt)
	 * @param graph
	 *            The deserialized {@link Graph}
	 */
	protected void parseGraphML(Element rootElt, Graph graph) {
		// TODO parseKeys() (Not sure if it is really necessary to do that)

		// Parse the graph element
		parseGraph(rootElt, graph);

	}

	/**
	 * Retrieve and parse the graph element of the GraphML description
	 * 
	 * @param rootElt
	 *            The root element (that must have a graph child)
	 * @param graph
	 *            The deserialized {@link Graph}
	 */
	protected void parseGraph(Element rootElt, Graph graph) {
		// Retrieve the Graph Element
		NodeList graphElts = rootElt.getElementsByTagName("graph");
		if (graphElts.getLength() == 0) {
			throw new RuntimeException(
					"No graph was found in the parsed document");
		}
		if (graphElts.getLength() > 1) {
			throw new RuntimeException(
					"More than one graph was found in the parsed document");
		}
		// If this code is reached, a unique graph element was found in the
		// document
		Element graphElt = (Element) graphElts.item(0);

		// TODO parseProperties() of the graph

		// Parse the elements of the graph
		NodeList childList = graphElt.getChildNodes();
		for (int i = 0; i < childList.getLength(); i++) {
			Element elt = (Element) childList.item(i);
			// Node elements
			if (elt.getNodeName().equals("node")) {
				parseNode(elt, graph);
			}
			// Edge elements
			if (elt.getNodeName().equals("edge")) {
				parseEdge(elt, graph);
			}
		}

	}

	/**
	 * Parse an edge {@link Element} of the GraphML description. An edge
	 * {@link Element} can be a parameter dependency or a FIFO of the parsed
	 * graph.
	 * 
	 * @param edgeElt
	 *            The edge {@link Element} to parse
	 * @param graph
	 *            The deserialized graph
	 */
	protected void parseEdge(Element edgeElt, Graph graph) {
		// TODO parseDependencies()
		// TODO parseFIFOs()
	}

	/**
	 * Parse a node {@link Element} of the GraphML description. A node
	 * {@link Element} can be a parameter or an vertex of the parsed graph.
	 * 
	 * @param nodeElt
	 *            The node {@link Element} to parse
	 * @param graph
	 *            The deserialized {@link Graph}
	 */
	protected void parseNode(Element nodeElt, Graph graph) {
		// Identify if the node is an actor or a parameter
		String nodeKind = getProperty(nodeElt, "kind");

		switch (nodeKind) {
		case "actor":
			parseActor(nodeElt, graph);
			break;
		// TODO Parse all types of nodes
		// case "implode":
		// break;
		// case "explode":
		// break;
		// case "parameter":
		// break;

		default:
			throw new RuntimeException("Parsed node " + nodeElt.getNodeName()
					+ "has an unknown kind: " + nodeKind);
		}

		// TODO parsePorts() of the vertex

	}

	protected void parseActor(Element nodeElt, Graph graph) {
		// Instantiate the new actor
		Actor actor = PIMeMoCFactory.eINSTANCE.createActor();

		// Set the actor properties
		actor.setName(getProperty(nodeElt, "name"));

		// Add the actor to the parsed graph
		graph.getVertices().add(actor);
	}

	/**
	 * Retrieve the value of a property of the given {@link Element}. A property
	 * is a data element child of the given element.<br>
	 * <br>
	 * 
	 * This method will iterate over the properties of the element so it might
	 * not be a good idea to use it in a method that would successively retrieve
	 * all properties of the element.
	 * 
	 * @param elt
	 *            The element containing the property
	 * @param propertyName
	 *            The name of the property
	 * @return The property value or null if the property was not found
	 * @author Jonathan Piat
	 */
	protected static String getProperty(Element elt, String propertyName) {
		NodeList childList = elt.getChildNodes();
		for (int i = 0; i < childList.getLength(); i++) {
			if (childList.item(i).getNodeName().equals("data")
					&& ((Element) childList.item(i)).getAttribute("key")
							.equals(propertyName)) {
				return childList.item(i).getTextContent();
			}
		}
		return null;
	}

}
