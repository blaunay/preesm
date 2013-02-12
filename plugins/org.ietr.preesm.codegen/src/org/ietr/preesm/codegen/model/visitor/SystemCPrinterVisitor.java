/*********************************************************
Copyright or � or Copr. IETR/INSA: Matthieu Wipliez, Jonathan Piat,
Maxime Pelcat, Jean-Fran�ois Nezan, Micka�l Raulet

[mwipliez,jpiat,mpelcat,jnezan,mraulet]@insa-rennes.fr

This software is a computer program whose purpose is to prototype
parallel applications.

This software is governed by the CeCILL-B license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-B
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
knowledge of the CeCILL-B license and that you accept its terms.
 *********************************************************/

package org.ietr.preesm.codegen.model.visitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.dftools.algorithm.model.CodeRefinement;
import net.sf.dftools.algorithm.model.IInterface;
import net.sf.dftools.algorithm.model.parameters.Argument;
import net.sf.dftools.algorithm.model.parameters.InvalidExpressionException;
import net.sf.dftools.algorithm.model.parameters.Parameter;
import net.sf.dftools.algorithm.model.parameters.Variable;
import net.sf.dftools.algorithm.model.sdf.SDFAbstractVertex;
import net.sf.dftools.algorithm.model.sdf.SDFEdge;
import net.sf.dftools.algorithm.model.sdf.SDFGraph;
import net.sf.dftools.algorithm.model.sdf.SDFInterfaceVertex;
import net.sf.dftools.algorithm.model.sdf.esdf.SDFSinkInterfaceVertex;
import net.sf.dftools.algorithm.model.sdf.esdf.SDFSourceInterfaceVertex;
import net.sf.dftools.algorithm.model.visitors.IGraphVisitor;
import net.sf.dftools.algorithm.model.visitors.SDF4JException;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.ietr.preesm.codegen.idl.ActorPrototypes;
import org.ietr.preesm.codegen.model.CodeGenSDFBroadcastVertex;
import org.ietr.preesm.codegen.model.CodeGenSDFEdge;
import org.ietr.preesm.codegen.model.CodeGenSDFForkVertex;
import org.ietr.preesm.codegen.model.CodeGenSDFGraph;
import org.ietr.preesm.codegen.model.CodeGenSDFJoinVertex;
import org.ietr.preesm.codegen.model.CodeGenSDFReceiveVertex;
import org.ietr.preesm.codegen.model.CodeGenSDFSendVertex;
import org.ietr.preesm.codegen.model.CodeGenSDFTaskVertex;

public class SystemCPrinterVisitor implements
		IGraphVisitor<CodeGenSDFGraph, SDFAbstractVertex, CodeGenSDFEdge> {

	private StringTemplateGroup group;
	private String outputPath;

	private boolean isTestBed;;

	private List<StringTemplate> ports;
	private List<StringTemplate> edges_declarations;
	private List<StringTemplate> edges_instanciations;
	private List<StringTemplate> actor_declarations;
	private List<StringTemplate> actor_instanciations;
	private List<StringTemplate> connections;
	private List<StringTemplate> firingRules;
	private List<StringTemplate> firingRulesSensitivityList;
	private List<StringTemplate> edge_delay_init;
	private List<String> includes;
	private List<StringTemplate> defines;

	public SystemCPrinterVisitor(File templateFile, String outputPath) {
		try {
			group = new StringTemplateGroup(new FileReader(templateFile));
			ports = new ArrayList<StringTemplate>();
			edges_declarations = new ArrayList<StringTemplate>();
			edges_instanciations = new ArrayList<StringTemplate>();
			actor_declarations = new ArrayList<StringTemplate>();
			actor_instanciations = new ArrayList<StringTemplate>();
			connections = new ArrayList<StringTemplate>();
			firingRules = new ArrayList<StringTemplate>();
			firingRulesSensitivityList = new ArrayList<StringTemplate>();
			includes = new ArrayList<String>();
			defines = new ArrayList<StringTemplate>();
			edge_delay_init = new ArrayList<StringTemplate>();
			this.outputPath = outputPath;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public SystemCPrinterVisitor(StringTemplateGroup templateGroup,
			String outputPath) {
		group = templateGroup;
		ports = new ArrayList<StringTemplate>();
		edges_declarations = new ArrayList<StringTemplate>();
		edges_instanciations = new ArrayList<StringTemplate>();
		actor_declarations = new ArrayList<StringTemplate>();
		actor_instanciations = new ArrayList<StringTemplate>();
		connections = new ArrayList<StringTemplate>();
		firingRules = new ArrayList<StringTemplate>();
		firingRulesSensitivityList = new ArrayList<StringTemplate>();
		edge_delay_init = new ArrayList<StringTemplate>();
		includes = new ArrayList<String>();
		defines = new ArrayList<StringTemplate>();
		this.outputPath = outputPath;
	}

	private String toActorName(String name) {
		String newName = new String();
		newName = name.substring(0, 1).toUpperCase() + name.substring(1);
		return newName;
	}

	private String toInstanceName(String name) {
		String newName = new String();
		newName = name.substring(0, 1).toLowerCase() + name.substring(1);
		return newName;
	}

	@Override
	public void visit(CodeGenSDFEdge sdfEdge) {
		String fifoName = new String();
		if (!(sdfEdge.getSource() instanceof SDFSourceInterfaceVertex || sdfEdge
				.getTarget() instanceof SDFSinkInterfaceVertex)) {
			fifoName = sdfEdge.getTarget().getName() + "_"
					+ sdfEdge.getSource().getName() + "_"
					+ sdfEdge.getTargetInterface().getName();

			StringTemplate edgeDeclarationTemplate = group
					.getInstanceOf("edge_declaration");
			StringTemplate edgeInstanciationTemplate = group
					.getInstanceOf("edge_instanciation");
			edgeDeclarationTemplate.setAttribute("name", fifoName);
			edgeInstanciationTemplate.setAttribute("name", fifoName);
			edgeDeclarationTemplate.setAttribute("type", sdfEdge.getDataType()
					.toString());
			if (isTestBed) {
				edgeInstanciationTemplate.setAttribute("type", sdfEdge
						.getDataType().toString());
			}
			edgeInstanciationTemplate.setAttribute("size", sdfEdge.getSize());
			edges_instanciations.add(edgeInstanciationTemplate);
			edges_declarations.add(edgeDeclarationTemplate);
		}

		try {
			if (sdfEdge.getDelay().intValue() > 0) {
				StringTemplate edgeDelayTemplate = group
						.getInstanceOf("edge_delay");
				edgeDelayTemplate.setAttribute("fifo", fifoName);
				edgeDelayTemplate.setAttribute("delay_size", sdfEdge.getDelay()
						.intValue());
				edgeDelayTemplate.setAttribute("delay_value", 0);
				edge_delay_init.add(edgeDelayTemplate);
			}
		} catch (InvalidExpressionException e) {
			e.printStackTrace();
		}

		StringTemplate srcConnection = group.getInstanceOf("connection");
		if (sdfEdge.getTarget() instanceof SDFSinkInterfaceVertex) {
			srcConnection.setAttribute("actor", sdfEdge.getSource().getName());
			srcConnection.setAttribute("edge", sdfEdge.getTarget().getName());
		} else {
			srcConnection.setAttribute("actor", sdfEdge.getSource().getName());
			srcConnection.setAttribute("edge", fifoName);
		}
		if (sdfEdge.getSource() instanceof CodeGenSDFForkVertex) {
			CodeGenSDFForkVertex forkSource = ((CodeGenSDFForkVertex) sdfEdge
					.getSource());
			int edgeIndex = forkSource.getEdgeIndex(sdfEdge);
			srcConnection.setAttribute("port", "outs[" + edgeIndex + "]");
		} else if (sdfEdge.getSource() instanceof CodeGenSDFBroadcastVertex) {
			CodeGenSDFBroadcastVertex broadcastSource = ((CodeGenSDFBroadcastVertex) sdfEdge
					.getSource());
			int edgeIndex = broadcastSource.getEdgeIndex(sdfEdge);
			srcConnection.setAttribute("port", "outs[" + edgeIndex + "]");
		} else if (sdfEdge.getSource() instanceof CodeGenSDFJoinVertex) {
			srcConnection.setAttribute("port", "out");
		} else if (sdfEdge.getSource() instanceof CodeGenSDFSendVertex
				|| sdfEdge.getSource() instanceof CodeGenSDFReceiveVertex) {
			srcConnection.setAttribute("port", "out");
		} else {
			srcConnection.setAttribute("port", sdfEdge.getSourceInterface()
					.getName());
		}

		if (!(sdfEdge.getSource() instanceof SDFSourceInterfaceVertex)) {
			connections.add(srcConnection);
		}

		StringTemplate trgtConnection = group.getInstanceOf("connection");
		if (sdfEdge.getSource() instanceof SDFSourceInterfaceVertex) {
			trgtConnection.setAttribute("actor", sdfEdge.getTarget().getName());
			trgtConnection.setAttribute("edge", sdfEdge.getSource().getName());
		} else {
			trgtConnection.setAttribute("actor", sdfEdge.getTarget().getName());
			trgtConnection.setAttribute("edge", fifoName);
		}
		if (sdfEdge.getTarget() instanceof CodeGenSDFJoinVertex) {
			CodeGenSDFJoinVertex joinTarget = ((CodeGenSDFJoinVertex) sdfEdge
					.getTarget());
			int edgeIndex = joinTarget.getEdgeIndex(sdfEdge);
			trgtConnection.setAttribute("port", "ins[" + edgeIndex + "]");
		} else if (sdfEdge.getTarget() instanceof CodeGenSDFForkVertex) {
			trgtConnection.setAttribute("port", "in");
		} else if (sdfEdge.getTarget() instanceof CodeGenSDFSendVertex
				|| sdfEdge.getTarget() instanceof CodeGenSDFReceiveVertex) {
			trgtConnection.setAttribute("port", "in");
		} else {
			trgtConnection.setAttribute("port", sdfEdge.getTargetInterface()
					.getName());
		}

		if (!(sdfEdge.getTarget() instanceof SDFSinkInterfaceVertex)) {
			connections.add(trgtConnection);
		}

	}

	@Override
	public void visit(CodeGenSDFGraph sdf) throws SDF4JException {
		if (sdf.getParentVertex() == null) {
			isTestBed = true;
		} else {
			isTestBed = false;
		}
		for (SDFEdge edge : sdf.edgeSet()) {
			edge.accept(this);
		}

		for (SDFAbstractVertex vertex : sdf.vertexSet()) {
			if (vertex instanceof SDFInterfaceVertex) {
				treatInterface((SDFInterfaceVertex) vertex, null, sdf, ports,
						firingRules, firingRulesSensitivityList);
				isTestBed = false;
			} else {
				vertex.accept(this);
			}
		}

		String graphName = sdf.getName();
		if (graphName.contains(".")) {
			graphName = graphName.substring(0, graphName.lastIndexOf('.'));
		}

		StringTemplate actorDeclarationTemplate;
		if (isTestBed) {
			actorDeclarationTemplate = group.getInstanceOf("test_bed");
			actorDeclarationTemplate.setAttribute("name", graphName);
			actorDeclarationTemplate.setAttribute("actor_declarations",
					actor_declarations);
			actorDeclarationTemplate.setAttribute("connections", connections);
			actorDeclarationTemplate.setAttribute("edge_declarations",
					edges_declarations);
			actorDeclarationTemplate.setAttribute("edges_instanciations",
					edges_instanciations);
			// actorDeclarationTemplate.setAttribute("body", actorBodyTemplate);
		} else {
			actorDeclarationTemplate = group.getInstanceOf("actor_declaration");
			actorDeclarationTemplate.setAttribute("name", graphName);
			actorDeclarationTemplate.setAttribute("ports", ports);
			actorDeclarationTemplate.setAttribute("actor_declarations",
					actor_declarations);
			actorDeclarationTemplate.setAttribute("edge_declarations",
					edges_declarations);
			actorDeclarationTemplate.setAttribute("connections", connections);
			actorDeclarationTemplate.setAttribute("edges_instanciations",
					edges_instanciations);
			// actorDeclarationTemplate.setAttribute("body", actorBodyTemplate);
			actorDeclarationTemplate.setAttribute("firing_rules", firingRules);
			actorDeclarationTemplate.setAttribute("firing_rules_sensitivity",
					firingRulesSensitivityList);
			actorDeclarationTemplate.setAttribute("actor_instanciations",
					actor_instanciations);
			actorDeclarationTemplate
					.setAttribute("edge_delay", edge_delay_init);
		}
		defines.clear();
		if (sdf.getVariables() != null) {
			for (Variable var : sdf.getVariables().values()) {
				StringTemplate variableDeclatationTemplate = group
						.getInstanceOf("define");
				variableDeclatationTemplate
						.setAttribute("label", var.getName());
				variableDeclatationTemplate.setAttribute("value",
						var.getValue());
				defines.add(variableDeclatationTemplate);
			}
		}

		if (!isTestBed && sdf.getParameters() != null
				&& sdf.getParameters().size() > 0) {
			List<String> parNames = new ArrayList<String>();
			for (Parameter par : sdf.getParameters().values()) {
				parNames.add(par.getName().toLowerCase());
			}
			actorDeclarationTemplate.setAttribute("generics", parNames);
		}

		StringTemplate fileTemplate = group.getInstanceOf("actor_file");
		fileTemplate.setAttribute("defines", defines);
		fileTemplate.setAttribute("includes", includes);
		fileTemplate.setAttribute("actor", actorDeclarationTemplate);
		fileTemplate.setAttribute("symbol", graphName.toUpperCase() + "_H");

		IPath path = new Path(this.outputPath);
		String extension;
		if (isTestBed) {
			extension = ".cpp";
		} else {
			extension = ".h";
		}

		path = path.append(graphName + extension);
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		String fsPath;
		if (workspace.getRoot().getFile(path).getLocation() != null) {
			fsPath = workspace.getRoot().getFile(path).getLocation()
					.toOSString();
		} else {
			fsPath = path.toString();
		}

		File printFile = new File(fsPath);
		FileWriter fileWriter;
		try {
			if (!printFile.exists()) {
				printFile.createNewFile();
			}
			fileWriter = new FileWriter(printFile);
			fileWriter.write(fileTemplate.toString());
			fileWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(actorDeclarationTemplate);
	}

	void treatInterface(SDFInterfaceVertex interfaceVertex,
			SDFAbstractVertex parentVertex, SDFGraph parentGraph,
			List<StringTemplate> actorPorts,
			List<StringTemplate> actorFiringRules,
			List<StringTemplate> actorFiringRulesSensitivity) {
		StringTemplate interfaceTemplate;
		if (interfaceVertex instanceof SDFSourceInterfaceVertex) {
			interfaceTemplate = group.getInstanceOf("input_port");
		} else if (interfaceVertex instanceof SDFSinkInterfaceVertex) {
			interfaceTemplate = group.getInstanceOf("output_port");
		} else {
			interfaceTemplate = group.getInstanceOf("output_port");
		}
		if (interfaceVertex.getDataType() != null) {
			interfaceTemplate.setAttribute("type",
					interfaceVertex.getDataType());
		} else {
			if (interfaceVertex instanceof SDFSourceInterfaceVertex
					&& parentGraph.incomingEdgesOf(parentVertex) != null) {
				for (SDFEdge edge : parentGraph.incomingEdgesOf(parentVertex)) {
					if (edge.getTargetInterface().equals(interfaceVertex)) {
						interfaceTemplate.setAttribute("type",
								edge.getDataType());
						break;
					}
				}
			}
			if (interfaceVertex instanceof SDFSinkInterfaceVertex
					&& parentGraph.outgoingEdgesOf(parentVertex) != null) {
				for (SDFEdge edge : parentGraph.outgoingEdgesOf(parentVertex)) {
					if (edge.getSourceInterface().equals(interfaceVertex)) {
						interfaceTemplate.setAttribute("type",
								edge.getDataType());
						break;
					}
				}
			}
		}
		interfaceTemplate.setAttribute("name", interfaceVertex.getName());
		actorPorts.add(interfaceTemplate);

		if (interfaceVertex instanceof SDFSourceInterfaceVertex) {
			Object interfaceSize = null;
			StringTemplate sdfFiringRuleTemplate = group
					.getInstanceOf("sdf_firing_rule");
			sdfFiringRuleTemplate.setAttribute("port",
					interfaceVertex.getName());
			for (SDFEdge edge : parentGraph.edgeSet()) {
				if (edge.getSource().equals(interfaceVertex)
						|| (edge.getTargetInterface().equals(interfaceVertex) && edge
								.getTarget().equals(parentVertex))) {
					interfaceSize = edge.getCons();
					StringTemplate portEvent = group
							.getInstanceOf("port_event");
					portEvent.setAttribute("port", interfaceVertex.getName());
					actorFiringRulesSensitivity.add(portEvent);
					break;
				}
			}
			if (interfaceSize != null) {
				sdfFiringRuleTemplate.setAttribute("nb_tokens", interfaceSize
						.toString().toLowerCase()); // TODO: need to get cleaner
													// lower case version
				actorFiringRules.add(sdfFiringRuleTemplate);
			}
		}

	}

	@Override
	public void visit(SDFAbstractVertex sdfVertex) throws SDF4JException {
		StringTemplate vertexInstanciationTemplate = group
				.getInstanceOf("vertex_instanciation");

		StringTemplate vertexDeclarationTemplate;
		if (isTestBed) {
			vertexDeclarationTemplate = group
					.getInstanceOf("vertex_test_bed_instanciation");
		} else {
			vertexDeclarationTemplate = group
					.getInstanceOf("vertex_declaration");
		}

		if (sdfVertex instanceof CodeGenSDFBroadcastVertex) {
			StringTemplate broadcastTemplate = broadcastTemplateAttribute((CodeGenSDFBroadcastVertex) sdfVertex);
			vertexDeclarationTemplate.setAttribute("type_template",
					broadcastTemplate);
			vertexDeclarationTemplate.setAttribute("type", "preesm_broadcast");
			if (!includes.contains("preesm_broadcast")) {
				includes.add("preesm_broadcast");
			}
		} else if (sdfVertex instanceof CodeGenSDFJoinVertex) {
			StringTemplate joinTemplate = joinTemplateAttribute((CodeGenSDFJoinVertex) sdfVertex);
			vertexDeclarationTemplate.setAttribute("type_template",
					joinTemplate);
			vertexDeclarationTemplate.setAttribute("type", "preesm_join");
			if (!includes.contains("preesm_join")) {
				includes.add("preesm_join");
			}
		} else if (sdfVertex instanceof CodeGenSDFForkVertex) {
			StringTemplate forkTemplate = forkTemplateAttribute((CodeGenSDFForkVertex) sdfVertex);
			vertexDeclarationTemplate.setAttribute("type_template",
					forkTemplate);
			vertexDeclarationTemplate.setAttribute("type", "preesm_fork");
			if (!includes.contains("preesm_fork")) {
				includes.add("preesm_fork");
			}
		} else if (sdfVertex instanceof CodeGenSDFSendVertex) {
			StringTemplate sendTemplate = sendTemplateAttribute((CodeGenSDFSendVertex) sdfVertex);
			vertexDeclarationTemplate.setAttribute("type_template",
					sendTemplate);
			vertexDeclarationTemplate.setAttribute("type", "preesm_send");
			if (!includes.contains("preesm_send")) {
				includes.add("preesm_send");
			}
		} else if (sdfVertex instanceof CodeGenSDFReceiveVertex) {
			StringTemplate recTemplate = receiveTemplateAttribute((CodeGenSDFReceiveVertex) sdfVertex);
			vertexDeclarationTemplate
					.setAttribute("type_template", recTemplate);
			vertexDeclarationTemplate.setAttribute("type", "preesm_receive");
			if (!includes.contains("preesm_receive")) {
				includes.add("preesm_receive");
			}
		} else if (sdfVertex instanceof CodeGenSDFTaskVertex) {
			String refinementName = null;
			if (((CodeGenSDFTaskVertex) sdfVertex).getRefinement() != null
					&& ((CodeGenSDFTaskVertex) sdfVertex).getRefinement() instanceof ActorPrototypes) {
				refinementName = ((ActorPrototypes) ((CodeGenSDFTaskVertex) sdfVertex)
						.getRefinement()).getLoopPrototype().getFunctionName();
				if (!includes.contains(refinementName)) {
					includes.add(refinementName);
				}
				exportAtomicActor((CodeGenSDFTaskVertex) sdfVertex);
			} else if (((CodeGenSDFTaskVertex) sdfVertex).getRefinement() != null
					&& ((CodeGenSDFTaskVertex) sdfVertex).getRefinement() instanceof CodeRefinement) {
				refinementName = sdfVertex.getRefinement().toString();
				if (!includes.contains(refinementName)) {
					includes.add(refinementName);
				}
				exportAtomicActor((CodeGenSDFTaskVertex) sdfVertex);
			} else if (((CodeGenSDFTaskVertex) sdfVertex).getRefinement() != null
					&& ((CodeGenSDFTaskVertex) sdfVertex).getRefinement() instanceof CodeGenSDFGraph) {
				CodeGenSDFGraph refGraph = ((CodeGenSDFGraph) ((CodeGenSDFTaskVertex) sdfVertex)
						.getRefinement());
				refinementName = refGraph.getName();
				if (refinementName.lastIndexOf(".") > 0) {
					refinementName = refinementName.substring(0,
							refinementName.lastIndexOf("."));
				}
				if (!includes.contains(refinementName)) {
					includes.add(refinementName);
				}
				SystemCPrinterVisitor childVisitor = new SystemCPrinterVisitor(
						this.group, this.outputPath);
				refGraph.accept(childVisitor);
			}
			vertexDeclarationTemplate.setAttribute("type", refinementName);
		} else {
			vertexDeclarationTemplate.setAttribute("type", sdfVertex.getName());
		}
		vertexInstanciationTemplate.setAttribute("name", sdfVertex.getName());
		vertexDeclarationTemplate.setAttribute("name", sdfVertex.getName());
		if (sdfVertex.getArguments() != null
				&& sdfVertex.getArguments().size() > 0) {
			List<String> argVals = new ArrayList<String>();
			for (Argument arg : sdfVertex.getArguments().values()) {
				argVals.add(arg.getValue());
			}
			vertexInstanciationTemplate.setAttribute("args", argVals);
			vertexDeclarationTemplate.setAttribute("args", argVals);
		}

		actor_declarations.add(vertexDeclarationTemplate);
		actor_instanciations.add(vertexInstanciationTemplate);

		StringTemplate signalDeclarationTemplate = group
				.getInstanceOf("signal_declaration");
		signalDeclarationTemplate.setAttribute("name",
				"enable_" + sdfVertex.getName());
		signalDeclarationTemplate.setAttribute("type", "bool");
		edges_declarations.add(signalDeclarationTemplate);
		StringTemplate enableConnection = group.getInstanceOf("connection");
		enableConnection.setAttribute("actor", sdfVertex.getName());
		enableConnection.setAttribute("port", "enable_port");
		enableConnection.setAttribute("edge", "enable_" + sdfVertex.getName());
		connections.add(enableConnection);

		StringTemplate invokeConnection = group.getInstanceOf("connection");
		invokeConnection.setAttribute("actor", sdfVertex.getName());
		invokeConnection.setAttribute("port", "invoke_port");
		invokeConnection.setAttribute("edge", "enable_" + sdfVertex.getName());
		connections.add(invokeConnection);

	}

	public void exportAtomicActor(CodeGenSDFTaskVertex actomicActor) {

		List<StringTemplate> atomicPorts = new ArrayList<StringTemplate>();
		List<StringTemplate> atomicFiringRules = new ArrayList<StringTemplate>();
		List<StringTemplate> atomicFiringRulesSensitivityList = new ArrayList<StringTemplate>();

		String functionName;
		if (actomicActor.getRefinement() instanceof ActorPrototypes) {
			functionName = ((ActorPrototypes) actomicActor.getRefinement())
					.getLoopPrototype().getFunctionName();
		} else {
			functionName = actomicActor.getRefinement().toString();
		}

		for (IInterface port : actomicActor.getInterfaces()) {
			treatInterface((SDFInterfaceVertex) port, actomicActor,
					(SDFGraph) actomicActor.getBase(), atomicPorts,
					atomicFiringRules, atomicFiringRulesSensitivityList);
		}

		StringTemplate actorDeclarationTemplate = group
				.getInstanceOf("actor_declaration");

		actorDeclarationTemplate.setAttribute("name", functionName);
		actorDeclarationTemplate.setAttribute("ports", atomicPorts);

		if (actomicActor.getArguments() != null
				&& actomicActor.getArguments().size() > 0) {
			List<String> argNames = new ArrayList<String>();
			for (Argument arg : actomicActor.getArguments().values()) {
				argNames.add(arg.getName().toLowerCase());
			}
			actorDeclarationTemplate.setAttribute("generics", argNames);
		}

		actorDeclarationTemplate
				.setAttribute("firing_rules", atomicFiringRules);
		actorDeclarationTemplate.setAttribute("firing_rules_sensitivity",
				atomicFiringRulesSensitivityList);

		StringTemplate fileTemplate = group.getInstanceOf("actor_file");
		fileTemplate.setAttribute("actor", actorDeclarationTemplate);
		fileTemplate.setAttribute("symbol", functionName.toUpperCase() + "_H");

		IPath path = new Path(this.outputPath);
		path = path.append(functionName + ".h");
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		String fsPath;
		if (workspace.getRoot().getFile(path).getLocation() != null) {
			fsPath = workspace.getRoot().getFile(path).getLocation()
					.toOSString();
		} else {
			fsPath = path.toString();
		}

		File printFile = new File(fsPath);
		FileWriter fileWriter;
		try {
			if (!printFile.exists()) {
				printFile.createNewFile();
			}
			fileWriter = new FileWriter(printFile);
			fileWriter.write(fileTemplate.toString());
			fileWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(actorDeclarationTemplate);
	}

	StringTemplate broadcastTemplateAttribute(
			CodeGenSDFBroadcastVertex broadcastVertex) {
		StringTemplate template_attribute = group
				.getInstanceOf("template_attribute");
		List<String> attributes = new ArrayList<String>();
		int nb_output = ((CodeGenSDFGraph) broadcastVertex.getBase())
				.outgoingEdgesOf(broadcastVertex).size();
		int outputSize = 1;
		String dataType = "char";
		for (SDFEdge edge : ((SDFGraph) broadcastVertex.getBase())
				.outgoingEdgesOf(broadcastVertex)) {
			try {
				outputSize = edge.getProd().intValue();
			} catch (InvalidExpressionException e) {

			}
			if (edge.getDataType() != null) {
				dataType = edge.getDataType().toString();
			}
		}
		attributes.add(dataType);
		attributes.add(String.valueOf(outputSize));
		attributes.add(String.valueOf(nb_output));
		template_attribute.setAttribute("attributes", attributes);
		return template_attribute;
	}

	StringTemplate joinTemplateAttribute(CodeGenSDFJoinVertex joinVertex) {
		StringTemplate template_attribute = group
				.getInstanceOf("template_attribute");
		List<String> attributes = new ArrayList<String>();
		int nb_input = ((CodeGenSDFGraph) joinVertex.getBase())
				.incomingEdgesOf(joinVertex).size();
		int inputSize = 1;
		String dataType = "char";
		for (SDFEdge edge : ((SDFGraph) joinVertex.getBase())
				.incomingEdgesOf(joinVertex)) {
			try {
				inputSize = edge.getProd().intValue();
			} catch (InvalidExpressionException e) {

			}
			if (edge.getDataType() != null) {
				dataType = edge.getDataType().toString();
			}
		}
		attributes.add(dataType);
		attributes.add(String.valueOf(inputSize));
		attributes.add(String.valueOf(nb_input));
		template_attribute.setAttribute("attributes", attributes);
		return template_attribute;
	}

	StringTemplate forkTemplateAttribute(CodeGenSDFForkVertex forkVertex) {
		StringTemplate template_attribute = group
				.getInstanceOf("template_attribute");
		List<String> attributes = new ArrayList<String>();
		int nb_output = ((CodeGenSDFGraph) forkVertex.getBase())
				.outgoingEdgesOf(forkVertex).size();
		int inputSize = 1;
		String dataType = "char";
		for (SDFEdge edge : ((SDFGraph) forkVertex.getBase())
				.incomingEdgesOf(forkVertex)) {
			try {
				inputSize = edge.getCons().intValue();
			} catch (InvalidExpressionException e) {

			}
			if (edge.getDataType() != null) {
				dataType = edge.getDataType().toString();
			}
		}
		attributes.add(dataType);
		attributes.add(String.valueOf(inputSize));
		attributes.add(String.valueOf(nb_output));
		template_attribute.setAttribute("attributes", attributes);
		return template_attribute;
	}

	StringTemplate sendTemplateAttribute(CodeGenSDFSendVertex sendVertex) {
		StringTemplate template_attribute = group
				.getInstanceOf("template_attribute");
		List<String> attributes = new ArrayList<String>();
		int inputSize = 1;
		String dataType = "char";
		for (SDFEdge edge : ((SDFGraph) sendVertex.getBase())
				.incomingEdgesOf(sendVertex)) {
			try {
				inputSize = edge.getCons().intValue();
			} catch (InvalidExpressionException e) {

			}
			if (edge.getDataType() != null) {
				dataType = edge.getDataType().toString();
			}
		}
		attributes.add(dataType);
		attributes.add(String.valueOf(inputSize));
		template_attribute.setAttribute("attributes", attributes);
		return template_attribute;
	}

	StringTemplate receiveTemplateAttribute(CodeGenSDFReceiveVertex recVertex) {
		StringTemplate template_attribute = group
				.getInstanceOf("template_attribute");
		List<String> attributes = new ArrayList<String>();
		int inputSize = 1;
		String dataType = "char";
		for (SDFEdge edge : ((SDFGraph) recVertex.getBase())
				.incomingEdgesOf(recVertex)) {
			try {
				inputSize = edge.getCons().intValue();
			} catch (InvalidExpressionException e) {

			}
			if (edge.getDataType() != null) {
				dataType = edge.getDataType().toString();
			}
		}
		attributes.add(dataType);
		attributes.add(String.valueOf(inputSize));
		template_attribute.setAttribute("attributes", attributes);
		return template_attribute;
	}

}
