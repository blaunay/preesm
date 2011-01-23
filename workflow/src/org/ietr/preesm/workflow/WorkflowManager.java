/*********************************************************
Copyright or � or Copr. IETR/INSA: Matthieu Wipliez, Jonathan Piat,
Maxime Pelcat, Jean-Fran�ois Nezan, Micka�l Raulet

[mwipliez,jpiat,mpelcat,jnezan,mraulet]@insa-rennes.fr

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

package org.ietr.preesm.workflow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.ietr.preesm.workflow.elements.AbstractScenarioImplementation;
import org.ietr.preesm.workflow.elements.AbstractTaskImplementation;
import org.ietr.preesm.workflow.elements.AbstractWorkflowNode;
import org.ietr.preesm.workflow.elements.ScenarioNode;
import org.ietr.preesm.workflow.elements.TaskNode;
import org.ietr.preesm.workflow.elements.Workflow;
import org.ietr.preesm.workflow.elements.WorkflowEdge;
import org.ietr.preesm.workflow.tools.WorkflowLogger;

/**
 * This class provides methods to check and execute a workflow. A workflow
 * consists of several transformation plug-ins tasks applied to a scenario.
 * 
 * @author mpelcat
 */
public class WorkflowManager {

	public WorkflowManager() {
		super();
	}

	/**
	 * Checks the existence of all task and scenario classes and sets the
	 * classes in the workflow nodess
	 */
	public boolean check(Workflow workflow, IProgressMonitor monitor) {

		monitor.subTask("Checking workflow...");

		boolean workflowOk = true;
		for (AbstractWorkflowNode node : workflow.vertexSet()) {
			if (node.isScenarioNode()) {
				workflowOk = ((ScenarioNode) node).getExtensionInformation();

				// The plugin declaring the scenario class was not found
				if (!workflowOk) {
					WorkflowLogger.getLogger().logFromProperty(Level.SEVERE,
							"Workflow.FailedFindScenarioPlugin",
							((ScenarioNode) node).getScenarioId());
					return false;
				}
			} else if (node.isTaskNode()) {
				// Testing only connected nodes
				if (!workflow.edgesOf(node).isEmpty()) {
					workflowOk = ((TaskNode) node).getExtensionInformation();

					// The plugin declaring the task class was not found
					if (!workflowOk) {
						WorkflowLogger.getLogger().logFromProperty(
								Level.SEVERE, "Workflow.FailedFindTaskPlugin",
								((TaskNode) node).getTaskId());
						return false;
					}
				}
			}
		}

		// Check the workflow
		monitor.worked(1);

		return workflowOk;
	}

	/**
	 * Checks that the workflow scenario node edges fit the task prototype
	 */
	public boolean checkScenarioPrototype(ScenarioNode scenarioNode,
			Workflow workflow) {
		AbstractScenarioImplementation scenario = scenarioNode.getScenario();
		Set<String> outputPorts = new HashSet<String>();

		// There may be several output edges with same data type (sharing same
		// port). All output names are retrieved here.
		for (WorkflowEdge edge : workflow.outgoingEdgesOf(scenarioNode)) {
			if (!outputPorts.contains(edge.getSourcePort())) {
				outputPorts.add(edge.getSourcePort());
			}
		}

		// The scenario node prototype is compared to the output port names
		if (!scenario.acceptOutputs(outputPorts)) {
			WorkflowLogger.getLogger().logFromProperty(Level.SEVERE,
					"Workflow.WrongScenarioPrototype",
					scenarioNode.getScenarioId(), scenario.displayPrototype());

			return false;
		}

		return true;
	}

	/**
	 * Checks that the workflow task node edges fit the task prototype
	 */
	public boolean checkTaskPrototype(TaskNode taskNode, Workflow workflow) {
		AbstractTaskImplementation task = taskNode.getTask();
		Map<String, String> inputs = new HashMap<String, String>();
		Set<String> outputs = new HashSet<String>();

		// input ports are retrieved as well as the data type
		// of the corresponding output port in the connected node
		for (WorkflowEdge edge : workflow.incomingEdgesOf(taskNode)) {
			AbstractWorkflowNode predecessor = workflow.getEdgeSource(edge);
			String type = predecessor.getImplementation().getOutputType(
					edge.getSourcePort());
			inputs.put(edge.getTargetPort(), type);
		}

		// There may be several output edges with same data type (sharing same
		// port)
		for (WorkflowEdge edge : workflow.outgoingEdgesOf(taskNode)) {
			if (!outputs.contains(edge.getSourcePort())) {
				outputs.add(edge.getSourcePort());
			}
		}

		if (!task.acceptInputs(inputs)) {
			WorkflowLogger.getLogger().logFromProperty(Level.SEVERE,
					"Workflow.WrongTaskPrototype", taskNode.getTaskId(),
					task.displayPrototype());

			return false;
		}

		// The task prototype is compared to the output port names
		if (!task.acceptOutputs(outputs)) {
			WorkflowLogger.getLogger().logFromProperty(Level.SEVERE,
					"Workflow.WrongTaskPrototype", taskNode.getTaskId(),
					task.displayPrototype());

			return false;
		}

		return true;
	}

	/**
	 * Executes the workflow
	 */
	public boolean execute(String workflowPath, String scenarioPath,
			IProgressMonitor monitor) {

		Workflow workflow = new WorkflowParser().parse(workflowPath);

		// Initializing the workflow console
		WorkflowLogger.getLogger().initConsole();
		monitor.beginTask("Executing workflow", workflow.vertexSet().size());

		WorkflowLogger.getLogger().logFromProperty(Level.INFO,
				"Workflow.StartInfo", workflowPath);

		// Checking the existence of plugins and retrieving prototypess
		if (!check(workflow, monitor)) {
			monitor.setCanceled(true);
			return false;
		}

		if (workflow.vertexSet().size() == 0) {
			WorkflowLogger.getLogger().logFromProperty(Level.SEVERE,
					"Workflow.EmptyVertexSet");

			return false;
		}

		if (!workflow.hasScenario()) {
			WorkflowLogger.getLogger().logFromProperty(Level.SEVERE,
					"Workflow.OneScenarioNeeded");

			return false;
		}

		for (AbstractWorkflowNode node : workflow.vertexTopologicalList()) {
			if (workflow.edgesOf(node).isEmpty()) {
				WorkflowLogger.getLogger().logFromProperty(Level.WARNING,
						"Workflow.IgnoredNonConnectedTask");
			} else {
				// Data outputs of the node
				Map<String, Object> outputs = null;
				String nodeId = null;

				if (node.isScenarioNode()) {
					// The scenario node is special because it gets a reference
					// path and generates the inputs of the rapid prototyping
					// process
					ScenarioNode scenarioNode = (ScenarioNode) node;
					nodeId = scenarioNode.getScenarioId();
					AbstractScenarioImplementation scenario = scenarioNode
							.getScenario();

					// Checks that the scenario node output edges fit the task
					// prototype
					if (!checkScenarioPrototype(scenarioNode, workflow)) {
						return false;
					}

					try {
						// updating monitor
						monitor.subTask(scenario.monitorMessage());
						outputs = scenario.extractData(scenarioPath);
						// Each node execution is equivalent for the monitor
						monitor.worked(1);
					} catch (WorkflowException e) {
						WorkflowLogger.getLogger().logFromProperty(
								Level.SEVERE,
								"Workflow.ScenarioExecutionException",
								e.getMessage());

						return false;
					}

				} else if (node.isTaskNode()) {
					TaskNode taskNode = (TaskNode) node;
					AbstractTaskImplementation task = taskNode.getTask();
					nodeId = taskNode.getTaskId();

					// Checks that the workflow task node edges fit the task
					// prototype
					if (!checkTaskPrototype(taskNode, workflow)) {
						return false;
					}

					// Preparing the input and output maps of the execute method
					Map<String, Object> inputs = new HashMap<String, Object>();

					// Retrieving the data from input edges
					for (WorkflowEdge edge : workflow.incomingEdgesOf(taskNode)) {
						inputs.put(edge.getTargetPort(), edge.getData());
					}

					// Executing the workflow task
					try {
						// updating monitor
						monitor.subTask(task.monitorMessage());

						// Workflow cancellation was requested
						if (monitor.isCanceled()) {

							WorkflowLogger.getLogger().logFromProperty(
									Level.SEVERE,
									"Workflow.CancellationRequested");
							return false;
						}

						// execution
						outputs = task.execute(inputs,
								taskNode.getParameters(), monitor, nodeId);

						// Each node execution is equivalent for the monitor
						monitor.worked(1);
					} catch (WorkflowException e) {
						WorkflowLogger.getLogger().logFromProperty(
								Level.SEVERE, "Workflow.ExecutionException",
								nodeId, e.getMessage());
						return false;
					}

				}

				// If the execution incorrectly initialized the outputs
				if (outputs == null) {
					WorkflowLogger.getLogger().logFromProperty(Level.SEVERE,
							"Workflow.NullNodeOutput", nodeId);
					return false;
				} else {
					// Retrieving output of the current node
					// Putting the data in output edges
					for (WorkflowEdge edge : workflow.outgoingEdgesOf(node)) {
						String type = edge.getSourcePort();
						// The same data may be transferred to several
						// successors
						if (outputs.containsKey(type)) {
							edge.setData(outputs.get(type));
						} else {
							edge.setData(null);
							WorkflowLogger.getLogger().logFromProperty(
									Level.SEVERE, "Workflow.IncorrectOutput",
									nodeId, type);
							return false;
						}
					}
				}
			}
		}

		WorkflowLogger.getLogger().logFromProperty(Level.INFO,
				"Workflow.EndInfo", workflowPath);

		return true;
	}
}
