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
 
package org.ietr.preesm.core.scenario;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.ietr.preesm.core.architecture.simplemodel.Operator;
import org.sdf4j.model.sdf.SDFAbstractVertex;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

/**
 * Writes a scenario as an XML
 * 
 * @author mpelcat
 */
public class ScenarioWriter {

	/**
	 * Current document
	 */
	private Document dom;
	
	/**
	 * Current scenario
	 */
	private Scenario scenario;
	
	public ScenarioWriter(Scenario scenario) {
		super();

		this.scenario = scenario;

        try {
			DOMImplementation impl;
			impl = DOMImplementationRegistry.newInstance()
					.getDOMImplementation("Core 3.0 XML 3.0 LS");
			dom = impl.createDocument("", "scenario",null);
		} catch (Exception e) {
			e.printStackTrace();
		} 

	}
	public Document generateScenarioDOM() {

		Element root = dom.getDocumentElement();
		
		addFiles(root);
		addConstraints(root);
		addTimings(root);
		
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

		Element files= dom.createElement("files");
		parent.appendChild(files);
		
		Element algo = dom.createElement("algorithm");
		files.appendChild(algo);
		algo.setAttribute("url", scenario.getAlgorithmURL());
		
		Element archi = dom.createElement("architecture");
		files.appendChild(archi);
		archi.setAttribute("url", scenario.getArchitectureURL());
		
		Element timings = dom.createElement("timingfile");
		files.appendChild(timings);
		timings.setAttribute("url", scenario.getTimingManager().getTimingFileURL());
		
		
	}

	private void addConstraints(Element parent) {

		Element constraints = dom.createElement("constraints");
		parent.appendChild(constraints);
		
		for(ConstraintGroup cst:scenario.getConstraintGroupManager().getConstraintGroups()){
			addConstraint(constraints, cst);
		}
	}

	private void addConstraint(Element parent, ConstraintGroup cst) {

		Element constraintGroupElt = dom.createElement("constraintGroup");
		parent.appendChild(constraintGroupElt);
		
		for(Operator opdef:cst.getOperators()){
			Element opdefelt = dom.createElement("operator");
			constraintGroupElt.appendChild(opdefelt);
			opdefelt.setAttribute("name", opdef.getName());
		}
		
		for(SDFAbstractVertex vtx:cst.getVertices()){
			Element vtxelt = dom.createElement("task");
			constraintGroupElt.appendChild(vtxelt);
			vtxelt.setAttribute("name", vtx.getName());
		}
	}

	private void addTimings(Element parent) {

		Element timings = dom.createElement("timings");
		parent.appendChild(timings);
		
		for(Timing timing:scenario.getTimingManager().getTimings()){
			addTiming(timings, timing);
		}
	}

	private void addTiming(Element parent, Timing timing) {

		Element timingelt = dom.createElement("timing");
		parent.appendChild(timingelt);
		timingelt.setAttribute("vertexname", timing.getVertex().getName());
		timingelt.setAttribute("opname", timing.getOperatorDefinition().getId());
		timingelt.setAttribute("time", Integer.toString(timing.getTime()));
	}
}
