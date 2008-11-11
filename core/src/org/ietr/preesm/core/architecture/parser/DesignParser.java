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

package org.ietr.preesm.core.architecture.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.ietr.preesm.core.architecture.ArchitectureComponent;
import org.ietr.preesm.core.architecture.ArchitectureComponentType;
import org.ietr.preesm.core.architecture.ArchitectureInterface;
import org.ietr.preesm.core.architecture.BusReference;
import org.ietr.preesm.core.architecture.MultiCoreArchitecture;
import org.ietr.preesm.core.architecture.simplemodel.MediumDefinition;
import org.ietr.preesm.core.log.PreesmLogger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * An xml parser retrieving architecture data from an IP-XACT design
 * 
 * @author mpelcat
 */
public class DesignParser {

	/**
	 * xml tree
	 */
	private Document dom = null;

	/**
	 * current architecture
	 */
	private MultiCoreArchitecture archi = null;
	
	private IFile currentFile = null;

	public DesignParser() {

	}

	public Document getDom() {
		return dom;
	}

	/**
	 * Retrieves the DOM document
	 */
	public void parseXmlFile(IFile file) {
		
		currentFile = file;
		// get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {

			// Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			// parse using builder to get DOM representation of the XML file
			dom = db.parse(file.getContents());

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (SAXException se) {
			se.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Parses the first level of hierarchy
	 */
	public MultiCoreArchitecture parseDocument() {
		if (dom != null) {
			// get the root elememt
			Element docElt = dom.getDocumentElement();

			Node node = docElt.getFirstChild();

			while (node != null) {

				if (node instanceof Element) {
					Element elt = (Element) node;
					String type = elt.getTagName();
					if (type.equals("spirit:name")) {
						archi = new MultiCoreArchitecture(elt.getTextContent());
					} else if (type.equals("spirit:componentInstances")) {

						if (archi == null) {
							PreesmLogger.getLogger().log(Level.SEVERE,
									"enter a name in the architecture");
						}

						parseComponentInstances(elt);
					} else if (type.equals("spirit:interconnections")) {

						if (archi == null) {
							PreesmLogger.getLogger().log(Level.SEVERE,
									"enter a name in the architecture");
						}

						parseInterconnections(elt);
					}
				}

				node = node.getNextSibling();
			}
		}

		return archi;
	}

	/**
	 * Parses the component instances
	 */
	private void parseComponentInstances(Element callElt) {

		Node node = callElt.getFirstChild();

		while (node != null) {

			if (node instanceof Element) {
				Element elt = (Element) node;
				String type = elt.getTagName();
				if (type.equals("spirit:componentInstance")) {
					parseComponentInstance(elt);
				}
			}

			node = node.getNextSibling();
		}
	}

	/**
	 * Parses one component instance
	 */
	private void parseComponentInstance(Element callElt) {

		String cmpName = "";
		String cmpDefId = "";
		String cmpType = "";
		IFile cmpFile = null;
		
		Element configElt = null;

		Node node = callElt.getFirstChild();

		while (node != null) {

			if (node instanceof Element) {
				Element elt = (Element) node;
				String type = elt.getTagName();
				if (type.equals("spirit:instanceName")) {
					cmpName = elt.getTextContent();
				} else if (type.equals("spirit:componentRef")) {
					cmpDefId = elt.getAttribute("spirit:name");
				} else if (type.equals("spirit:configurableElementValues")) {
					configElt = elt;
					cmpType = parseComponentType(configElt);
					cmpFile = findComponentRefinement(configElt);
				}
			}

			node = node.getNextSibling();
		}

		// If the component was well parsed, it is created and added to the
		// architecture
		ArchitectureComponentType type = ArchitectureComponentType
				.getType(cmpType);

		if (type != null) {
			ArchitectureComponent cmp = archi.addComponent(
					ArchitectureComponentType.getType(cmpType), cmpDefId,
					cmpName);

			if (configElt != null && type == ArchitectureComponentType.medium) {
				MediumParser.parse((MediumDefinition) cmp.getDefinition(),
						configElt);
			}

			// Parsing the component file if present and retrieving data
			if(cmpFile != null){
				ComponentParser cmpParser = new ComponentParser(archi, cmp);
				cmpParser.parseXmlFile(cmpFile);
				cmpParser.parseDocument();
			}
		}
	}

	/**
	 * Parses a component type and returns the associated value. The component type
	 * is the most important property of a component. It states how to consider
	 * the component (as an operator or as a medium for instance)
	 */
	private String parseComponentType(Element callElt) {

		String componentType = "";

		Node node = callElt.getFirstChild();

		while (node != null) {

			if (node instanceof Element) {
				Element elt = (Element) node;
				String eltType = elt.getTagName();
				String configurableElementName = elt
						.getAttribute("spirit:referenceId");
				if (eltType.equals("spirit:configurableElementValue")
						&& configurableElementName.equals("componentType")) {
					componentType = elt.getTextContent();
				}
			}

			node = node.getNextSibling();
		}

		return componentType;
	}

	/**
	 * The refinement is the file containing the IP-XACT component definition.
	 * If such a file is found, a parser is called and information is retrieved from it.
	 */
	private IFile findComponentRefinement(Element callElt) {
		
		IFile file = null;
		String componentRefinement = "";

		Node node = callElt.getFirstChild();

		while (node != null) {

			if (node instanceof Element) {
				Element elt = (Element) node;
				String eltType = elt.getTagName();
				String configurableElementName = elt
						.getAttribute("spirit:referenceId");
				if (eltType.equals("spirit:configurableElementValue")
						&& configurableElementName.equals("refinement")) {
					componentRefinement = elt.getTextContent();
				}
			}

			node = node.getNextSibling();
		}

		if(!componentRefinement.isEmpty()){

			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			// get all possible candidates
			List<IFile> files = new ArrayList<IFile>();
			String fileExt = "ipxactcmp";
			IResource resource = workspace.getRoot().findMember(
					currentFile.getFullPath().removeLastSegments(1) + "/" + componentRefinement + "." + fileExt);
			if (resource instanceof IFile) {
				file = ((IFile) resource);
			}
		}
		
		return file;
	}

	/**
	 * Parses all interconnections
	 */
	private void parseInterconnections(Element callElt) {

		Node node = callElt.getFirstChild();

		while (node != null) {

			if (node instanceof Element) {
				Element elt = (Element) node;
				String type = elt.getTagName();
				if (type.equals("spirit:interconnection")) {
					parseInterconnection(elt);
				}
			}

			node = node.getNextSibling();
		}
	}

	/**
	 * Parses one interconnection
	 */
	private void parseInterconnection(Element callElt) {

		List<String> busRefList = new ArrayList<String>();
		List<String> componentRefList = new ArrayList<String>();
		boolean isDirected = false;

		Node node = callElt.getFirstChild();

		while (node != null) {

			if (node instanceof Element) {
				Element elt = (Element) node;
				String type = elt.getTagName();
				if (type.equals("spirit:activeInterface")) {
					busRefList.add(elt.getAttribute("spirit:busRef"));
					componentRefList.add(elt
							.getAttribute("spirit:componentRef"));
				} else if (type.equals("spirit:displayName")) {
					isDirected = (elt.getTextContent()
							.equalsIgnoreCase("directed"));
				}
			}

			node = node.getNextSibling();
		}

		if (busRefList.size() == 2 && componentRefList.size() == 2) {

			ArchitectureComponent cmp1 = archi.getComponent(componentRefList
					.get(0));
			BusReference busRef1 = archi.createBusReference(busRefList.get(0));
			ArchitectureInterface if1 = cmp1
					.addInterface(new ArchitectureInterface(busRef1, cmp1));

			ArchitectureComponent cmp2 = archi.getComponent(componentRefList
					.get(1));
			BusReference busRef2 = archi.createBusReference(busRefList.get(1));
			ArchitectureInterface if2 = cmp2
					.addInterface(new ArchitectureInterface(busRef2, cmp2));

			archi.connect(cmp1, if1, cmp2, if2, isDirected);
		}

	}
}
