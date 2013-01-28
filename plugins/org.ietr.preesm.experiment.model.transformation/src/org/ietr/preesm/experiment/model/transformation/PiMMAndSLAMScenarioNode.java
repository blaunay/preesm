/**
 * 
 */
package org.ietr.preesm.experiment.model.transformation;

import java.util.HashMap;
import java.util.Map;

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
 * with the PiMM type
 * 
 * @author mpelcat
 * 
 */
public class PiMMAndSLAMScenarioNode extends AbstractScenarioImplementation {

	/**
	 * The scenario node in Preesm outputs three elements: PiMM, architecture and
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
		try {
			scenario = scenarioParser.parseXmlFile(file);

		} catch (Exception e) {
			throw new WorkflowException(e.getMessage());
		}

		// Retrieving the architecture
		Design slamDesign = ScenarioParser.parseSlamDesign(scenario
				.getArchitectureURL());

		outputs.put("scenario", scenario);
		outputs.put("PiMM", null);
		outputs.put("S-LAM", slamDesign);
		return outputs;
	}

	@Override
	public String monitorMessage() {
		return "Scenario, algorithm and architecture parsing.";
	}

}
