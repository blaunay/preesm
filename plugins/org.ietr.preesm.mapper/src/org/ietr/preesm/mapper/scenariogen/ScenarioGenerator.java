/**
 * 
 */
package org.ietr.preesm.mapper.scenariogen;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.ietr.dftools.algorithm.model.sdf.SDFAbstractVertex;
import org.ietr.dftools.algorithm.model.sdf.SDFGraph;
import org.ietr.dftools.architecture.slam.ComponentInstance;
import org.ietr.dftools.architecture.slam.Design;
import org.ietr.dftools.architecture.slam.component.Operator;
import org.ietr.dftools.workflow.WorkflowException;
import org.ietr.dftools.workflow.elements.Workflow;
import org.ietr.dftools.workflow.implement.AbstractTaskImplementation;
import org.ietr.dftools.workflow.tools.WorkflowLogger;
import org.ietr.preesm.core.Activator;
import org.ietr.preesm.core.architecture.util.DesignTools;
import org.ietr.preesm.core.scenario.PreesmScenario;
import org.ietr.preesm.core.scenario.serialize.ScenarioParser;
import org.ietr.preesm.core.types.ImplementationPropertyNames;

/**
 * This class defines a method to load a new scenario and optionally change some
 * constraints from an output DAG.
 * 
 * @author mpelcat
 */
public class ScenarioGenerator extends AbstractTaskImplementation {

	@Override
	public Map<String, String> getDefaultParameters() {
		Map<String, String> parameters = new HashMap<String, String>();

		parameters.put("scenarioFile", "");
		parameters.put("dagFile", "");

		return parameters;
	}

	@Override
	public String monitorMessage() {
		return "generating a scenario";
	}

	@Override
	public Map<String, Object> execute(Map<String, Object> inputs,
			Map<String, String> parameters, IProgressMonitor monitor,
			String nodeName, Workflow workflow) throws WorkflowException {

		Map<String, Object> outputs = new HashMap<String, Object>();

		WorkflowLogger.getLogger().log(Level.INFO, "Generating scenario");

		String scenarioFileName = parameters.get("scenarioFile");

		// Retrieving the scenario
		if (scenarioFileName.isEmpty()) {
			WorkflowLogger.getLogger().log(Level.SEVERE,
					"lack of a scenarioFile parameter");
			return null;
		} else {

			// Retrieving the scenario from the given path
			ScenarioParser parser = new ScenarioParser();

			Path relativePath = new Path(scenarioFileName);
			IFile file = ResourcesPlugin.getWorkspace().getRoot()
					.getFile(relativePath);

			PreesmScenario scenario;
			// Retrieving the algorithm
			SDFGraph algo;
			
			try {
				scenario = parser.parseXmlFile(file);

				algo = ScenarioParser.getSDFGraph(scenario
						.getAlgorithmURL());
			} catch (Exception e) {
				throw new WorkflowException(e.getMessage());
			}
			
			if (algo == null) {
				WorkflowLogger.getLogger().log(Level.SEVERE,
						"cannot retrieve algorithm");
				return null;
			} else {
				outputs.put("SDF", algo);
			}

			// Retrieving the architecture
			Design archi = ScenarioParser.parseSlamDesign(scenario
					.getArchitectureURL());
			if (archi == null) {
				WorkflowLogger.getLogger().log(Level.SEVERE,
						"cannot retrieve architecture");
				return null;
			} else {
				outputs.put("architecture", archi);
			}

		}

		String dagFileName = parameters.get("dagFile");
		// Parsing the output DAG if present and updating the constraints
		if (dagFileName.isEmpty()) {
			WorkflowLogger.getLogger().log(Level.WARNING,
					"No dagFile -> retrieving the scenario as is");
		} else {
			GMLMapperDAGImporter importer = new GMLMapperDAGImporter();

			((PreesmScenario) outputs.get("scenario"))
					.getConstraintGroupManager().removeAll();
			((PreesmScenario) outputs.get("scenario")).getTimingManager()
					.removeAll();

			try {
				Path relativePath = new Path(dagFileName);
				IFile file = ResourcesPlugin.getWorkspace().getRoot()
						.getFile(relativePath);

				Activator.updateWorkspace();
				SDFGraph graph = importer
						.parse(file.getContents(), dagFileName);
				graph = importer.parse(file.getContents(), dagFileName);

				for (SDFAbstractVertex dagV : graph.vertexSet()) {
					String vName = (String) dagV.getPropertyBean().getValue(
							"name");
					String opName = (String) dagV.getPropertyBean().getValue(
							ImplementationPropertyNames.Vertex_Operator);
					String timeStr = (String) dagV.getPropertyBean().getValue(
							ImplementationPropertyNames.Task_duration);
					SDFAbstractVertex sdfV = ((SDFGraph) outputs.get("SDF"))
							.getVertex(vName);
					ComponentInstance op = DesignTools.getComponentInstance(
							(Design) outputs.get("architecture"), opName);

					if (sdfV != null && op != null
							&& op.getComponent() instanceof Operator) {
						((PreesmScenario) outputs.get("scenario"))
								.getConstraintGroupManager().addConstraint(
										opName, sdfV);
						((PreesmScenario) outputs.get("scenario"))
								.getTimingManager().setTiming(sdfV.getName(),
										op.getComponent().getVlnv().getName(),
										Long.parseLong(timeStr));
					}
				}

			} catch (Exception e) {
				WorkflowLogger.getLogger().log(Level.SEVERE, e.getMessage());
			}

		}

		return outputs;
	}

}
