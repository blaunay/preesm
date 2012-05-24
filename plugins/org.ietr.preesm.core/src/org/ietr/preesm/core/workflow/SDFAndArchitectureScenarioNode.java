/*********************************************************
Copyright or © or Copr. IETR/INSA: Maxime Pelcat, Jean-François Nezan,
Karol Desnos

[mpelcat,jnezan,kdesnos]@insa-rennes.fr

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
package org.ietr.preesm.core.workflow;

import java.util.HashMap;
import java.util.Map;

import net.sf.dftools.algorithm.model.parameters.VariableSet;
import net.sf.dftools.algorithm.model.sdf.SDFGraph;
import net.sf.dftools.architecture.slam.Design;
import net.sf.dftools.workflow.WorkflowException;
import net.sf.dftools.workflow.implement.AbstractScenarioImplementation;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.ietr.preesm.core.scenario.PreesmScenario;
import org.ietr.preesm.core.scenario.serialize.ScenarioParser;

/**
 * Implementing the new DFTools scenario node behavior for Preesm. This version
 * generates an architecture with the S-LAM2 meta-model type and an algorithm
 * with the IBSDF type
 * 
 * @author mpelcat
 * @author kdesnos
 * 
 */
public class SDFAndArchitectureScenarioNode extends
		AbstractScenarioImplementation {

	/**
	 * The scenario node in Preesm outputs three elements: SDF, architecture and
	 * scenario
	 */
	@Override
	public Map<String, Object> extractData(String path)
			throws WorkflowException {

		Map<String, Object> outputs = new HashMap<String, Object>();

		// Retrieving the scenario from the given path
		ScenarioParser scenarioParser = new ScenarioParser();

		Path relativePath = new Path(path);
		IFile file = ResourcesPlugin.getWorkspace().getRoot()
				.getFile(relativePath);

		PreesmScenario scenario;
		// Retrieving the algorithm
		SDFGraph algorithm;
		try {
			scenario = scenarioParser.parseXmlFile(file);

			algorithm = ScenarioParser.getAlgorithm(scenario.getAlgorithmURL());
		} catch (Exception e) {
			throw new WorkflowException(e.getMessage());
		}

		// Apply to the algorithm the values of variables
		// defined in the scenario
		VariableSet variablesGraph = algorithm.getVariables();
		VariableSet variablesScenario = scenario.getVariablesManager()
				.getVariables();

		for (String variableName : variablesScenario.keySet()) {
			String newValue = variablesScenario.get(variableName).getValue();
			variablesGraph.getVariable(variableName).setValue(newValue);
		}

		// Retrieving the architecture
		Design slamDesign = ScenarioParser.parseSlamDesign(scenario
				.getArchitectureURL());

		outputs.put("scenario", scenario);
		outputs.put("SDF", algorithm);
		outputs.put("architecture", slamDesign);
		return outputs;
	}

	@Override
	public String monitorMessage() {
		return "Scenario, algorithm and architecture parsing.";
	}

}
