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

package org.ietr.preesm.plugin.mapper.graphtransfo;

import java.util.Iterator;

import org.ietr.preesm.core.architecture.MultiCoreArchitecture;
import org.ietr.preesm.core.architecture.simplemodel.Medium;
import org.ietr.preesm.core.architecture.simplemodel.Operator;
import org.ietr.preesm.core.codegen.DataType;
import org.ietr.preesm.core.codegen.VertexType;
import org.ietr.preesm.core.codegen.sdfProperties.BufferAggregate;
import org.ietr.preesm.core.codegen.sdfProperties.BufferProperties;
import org.ietr.preesm.core.scenario.IScenario;
import org.ietr.preesm.plugin.abc.AbstractAbc;
import org.ietr.preesm.plugin.abc.CommunicationRouter;
import org.ietr.preesm.plugin.abc.IAbc;
import org.ietr.preesm.plugin.abc.order.SchedulingOrderManager;
import org.ietr.preesm.plugin.abc.transaction.TransactionManager;
import org.ietr.preesm.plugin.mapper.model.MapperDAG;
import org.ietr.preesm.plugin.mapper.model.MapperDAGEdge;
import org.ietr.preesm.plugin.mapper.model.MapperDAGVertex;
import org.ietr.preesm.plugin.mapper.model.impl.ReceiveVertex;
import org.ietr.preesm.plugin.mapper.model.impl.SendVertex;
import org.ietr.preesm.plugin.mapper.model.impl.TransferVertex;
import org.ietr.preesm.plugin.mapper.model.impl.TransferVertexAdder;
import org.sdf4j.model.AbstractEdge;
import org.sdf4j.model.PropertyBean;
import org.sdf4j.model.dag.DAGEdge;
import org.sdf4j.model.dag.DAGVertex;
import org.sdf4j.model.sdf.SDFAbstractVertex;
import org.sdf4j.model.sdf.SDFEdge;
import org.sdf4j.model.sdf.SDFGraph;

/**
 * Tags an SDF with the implementation information necessary for code generation
 * 
 * @author pmenuet
 * @author mpelcat
 */
public class TagDAG {

	/**
	 * Main for test
	 */
	public static void main(String[] args) {

	}

	/**
	 * Constructor
	 */
	public TagDAG() {
		super();
	}

	/**
	 * tag adds the send and receive operations necessary to the code
	 * generation. It also adds the necessary properies.
	 */
	public void tag(MapperDAG dag, MultiCoreArchitecture architecture, IScenario scenario, IAbc simu) {

		PropertyBean bean = dag.getPropertyBean();
		bean.setValue(AbstractAbc.propertyBeanName, simu.getType());
		bean.setValue("SdfReferenceGraph", dag.getReferenceSdfGraph());

		addTransfers(dag, architecture);
		addProperties(dag);
		addAllAggregates(dag,scenario);
	}

	public void addTransfers(MapperDAG dag, MultiCoreArchitecture architecture) {

		// Temporary
		// TODO: add a scheduling order for Send/Receive.
		SchedulingOrderManager orderMgr = new SchedulingOrderManager();
		orderMgr.reconstructTotalOrderFromDAG(dag);
		TransferVertexAdder tvAdder = new TransferVertexAdder(
				new CommunicationRouter(architecture), orderMgr, true, true);
		tvAdder.addTransferVertices(dag, new TransactionManager());
		orderMgr.tagDAG(dag);
	}

	public void addProperties(MapperDAG dag) {

		MapperDAGVertex currentVertex;

		Iterator<DAGVertex> iter = dag.vertexSet().iterator();

		// Tagging the vertices with informations for code generation
		while (iter.hasNext()) {
			currentVertex = (MapperDAGVertex) iter.next();
			PropertyBean bean = currentVertex.getPropertyBean();

			if (currentVertex instanceof SendVertex) {

				MapperDAGEdge incomingEdge = (MapperDAGEdge) ((SendVertex) currentVertex)
						.incomingEdges().toArray()[0];
				bean.setValue(VertexType.propertyBeanName, VertexType.send);
				bean
						.setValue(Operator.propertyBeanName,
								((SendVertex) currentVertex).getRouteStep()
										.getSender());
				bean
						.setValue(Medium.propertyBeanName,
								((SendVertex) currentVertex).getRouteStep()
										.getMedium());
				bean.setValue("dataSize", incomingEdge.getInitialEdgeProperty()
						.getDataSize());
				bean.setValue("senderGraphName", incomingEdge.getSource()
						.getName());
				bean.setValue(Operator.propertyBeanName + "_address",
						((SendVertex) currentVertex).getRouteStep().getSender()
								.getBaseAddress());
			} else if (currentVertex instanceof ReceiveVertex) {

				MapperDAGEdge outgoingEdge = (MapperDAGEdge) ((ReceiveVertex) currentVertex)
						.outgoingEdges().toArray()[0];
				bean.setValue(VertexType.propertyBeanName, VertexType.receive);
				bean.setValue(Operator.propertyBeanName,
						((ReceiveVertex) currentVertex).getRouteStep()
								.getReceiver());
				bean.setValue(Medium.propertyBeanName,
						((ReceiveVertex) currentVertex).getRouteStep()
								.getMedium());
				bean.setValue("dataSize", outgoingEdge.getInitialEdgeProperty()
						.getDataSize());
				bean.setValue("receiverGraphName", outgoingEdge.getTarget()
						.getName());
				bean
						.setValue(Operator.propertyBeanName + "_address",
								((ReceiveVertex) currentVertex).getRouteStep()
										.getReceiver()
										.getBaseAddress());
			} else {

				bean.setValue(Operator.propertyBeanName, currentVertex
						.getImplementationVertexProperty()
						.getEffectiveOperator());
				bean.setValue(VertexType.propertyBeanName, VertexType.task);

				Operator effectiveOperator = currentVertex
						.getImplementationVertexProperty()
						.getEffectiveOperator();
				int singleRepeatTime = currentVertex.getInitialVertexProperty()
						.getTime(effectiveOperator);
				int nbRepeat = currentVertex.getInitialVertexProperty()
						.getNbRepeat();
				int totalTime = nbRepeat * singleRepeatTime;
				bean.setValue("duration", totalTime);
			}

			bean.setValue("schedulingOrder", currentVertex
					.getImplementationVertexProperty()
					.getSchedulingTotalOrder());
		}
	}

	/**
	 * Loop on the edges to add aggregates.
	 */
	public void addAllAggregates(MapperDAG dag, IScenario scenario) {

		MapperDAGEdge edge;

		Iterator<DAGEdge> iter = dag.edgeSet().iterator();

		// Tagging the vertices with informations for code generation
		while (iter.hasNext()) {
			edge = (MapperDAGEdge) iter.next();

			if (edge.getSource() instanceof TransferVertex
					|| edge.getTarget() instanceof TransferVertex) {
				addAggregateFromSDF(edge,scenario);
			} else {
				addAggregateFromSDF(edge,scenario);
			}
		}
	}

	/**
	 * Aggregate is imported from the SDF edge. An aggregate in SDF is a set of
	 * sdf edges that were merged into one DAG edge.
	 */
	public void addAggregateFromSDF(MapperDAGEdge edge, IScenario scenario) {

		BufferAggregate agg = new BufferAggregate();

		// Iterating the SDF aggregates
		for (AbstractEdge<SDFGraph, SDFAbstractVertex> aggMember : edge
				.getAggregate()) {
			SDFEdge sdfAggMember = (SDFEdge) aggMember;

			DataType dataType = scenario.getSimulationManager().getDataType(sdfAggMember.getDataType().toString());
			BufferProperties props = new BufferProperties(dataType, sdfAggMember
					.getSourceInterface().getName(), sdfAggMember
					.getTargetInterface().getName(), sdfAggMember.getProd()
					.intValue());

			agg.add(props);
		}
		edge.getPropertyBean().setValue(BufferAggregate.propertyBeanName, agg);
	}

}
