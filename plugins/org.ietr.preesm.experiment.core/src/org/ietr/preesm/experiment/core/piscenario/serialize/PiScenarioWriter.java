/*******************************************************************************
 * Copyright or © or Copr. IETR/INSA: Maxime Pelcat, Jean-François Nezan,
 * Karol Desnos, Julien Heulot
 * 
 * [mpelcat,jnezan,kdesnos,jheulot]@insa-rennes.fr
 * 
 * This software is a computer program whose purpose is to prototype
 * parallel applications.
 * 
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info". 
 * 
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability. 
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or 
 * data to be ensured and,  more generally, to use and operate it in the 
 * same conditions as regards security. 
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 ******************************************************************************/
package org.ietr.preesm.experiment.core.piscenario.serialize;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.ietr.preesm.experiment.core.piscenario.ActorNode;
import org.ietr.preesm.experiment.core.piscenario.ActorTree;
import org.ietr.preesm.experiment.core.piscenario.PiScenario;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

/**
 * Writes a {@link PiScenario} as an XML
 * 
 * @author jheulot
 */
public class PiScenarioWriter {

	/**
	 * Current document
	 */
	private Document dom;

	/**
	 * Current scenario
	 */
	private PiScenario piscenario;

	public PiScenarioWriter(PiScenario piscenario) {
		super();

		this.piscenario = piscenario;

		try {
			DOMImplementation impl;
			impl = DOMImplementationRegistry.newInstance()
					.getDOMImplementation("Core 3.0 XML 3.0 LS");
			dom = impl.createDocument("", "piscenario", null);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public Document generateScenarioDOM() {

		Element root = dom.getDocumentElement();

		addFiles(root);
		addActorTree(root);

		return dom;
	}

	public void writeDom(IFile file) {

		try {
			// Gets the DOM implementation of document
			DOMImplementation impl = dom.getImplementation();
			DOMImplementationLS implLS = (DOMImplementationLS) impl;

			LSOutput output = implLS.createLSOutput();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			output.setByteStream(out);

			LSSerializer serializer = implLS.createLSSerializer();
			serializer.getDomConfig().setParameter("format-pretty-print", true);
			serializer.write(dom, output);

			file.setContents(new ByteArrayInputStream(out.toByteArray()), true,
					false, new NullProgressMonitor());
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addFiles(Element parent) {

		Element files = dom.createElement("files");
		parent.appendChild(files);

		Element algo = dom.createElement("algorithm");
		files.appendChild(algo);
		algo.setAttribute("url", piscenario.getAlgorithmURL());

		Element archi = dom.createElement("architecture");
		files.appendChild(archi);
		archi.setAttribute("url", piscenario.getArchitectureURL());
	}
	
	private void writeActorTreeChildren(ActorNode parentNode, Element parentElement){
		if(parentNode.isHierarchical()){
			for(ActorNode childNode : parentNode.getChildren()){
				Element childElement = dom.createElement(childNode.getName());
				parentElement.appendChild(childElement);
				writeActorTreeChildren(childNode, childElement);
			}
		}else{
			/* Write Constraints */
			Element constElement = dom.createElement("constraints");
			parentElement.appendChild(constElement);
			for(String core : piscenario.getOperatorIds()){
				Element coreElement = dom.createElement(core);
				constElement.appendChild(coreElement);
				coreElement.setAttribute("value", ""+parentNode.getConstraint(core));
			}
			
			/* Write Timings */
			Element timElement = dom.createElement("timings");
			parentElement.appendChild(timElement);
			for(String coreType : parentNode.getTimings().keySet()){
				String timing = parentNode.getTimings().get(coreType).getStringValue();
				Element coreTypeElement = dom.createElement(coreType);
				timElement.appendChild(coreTypeElement);
				coreTypeElement.setAttribute("timing", timing);
			}
		}
	}
	
	private void addActorTree(Element parent){
		Element elementTree = dom.createElement("actorTree");
		parent.appendChild(elementTree);
		
		ActorTree tree = piscenario.getActorTree(); 
		Element rootElement = dom.createElement(tree.getRoot().getName());
		elementTree.appendChild(rootElement);
		writeActorTreeChildren(tree.getRoot(), rootElement);
	}
}
