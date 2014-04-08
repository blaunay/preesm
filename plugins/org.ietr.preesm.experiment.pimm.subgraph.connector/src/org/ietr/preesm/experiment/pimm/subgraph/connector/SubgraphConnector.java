package org.ietr.preesm.experiment.pimm.subgraph.connector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ietr.preesm.experiment.model.pimm.AbstractActor;
import org.ietr.preesm.experiment.model.pimm.AbstractVertex;
import org.ietr.preesm.experiment.model.pimm.Actor;
import org.ietr.preesm.experiment.model.pimm.ConfigInputInterface;
import org.ietr.preesm.experiment.model.pimm.ConfigInputPort;
import org.ietr.preesm.experiment.model.pimm.ConfigOutputInterface;
import org.ietr.preesm.experiment.model.pimm.ConfigOutputPort;
import org.ietr.preesm.experiment.model.pimm.DataInputInterface;
import org.ietr.preesm.experiment.model.pimm.DataInputPort;
import org.ietr.preesm.experiment.model.pimm.DataOutputInterface;
import org.ietr.preesm.experiment.model.pimm.DataOutputPort;
import org.ietr.preesm.experiment.model.pimm.Delay;
import org.ietr.preesm.experiment.model.pimm.Dependency;
import org.ietr.preesm.experiment.model.pimm.Expression;
import org.ietr.preesm.experiment.model.pimm.Fifo;
import org.ietr.preesm.experiment.model.pimm.ISetter;
import org.ietr.preesm.experiment.model.pimm.InterfaceActor;
import org.ietr.preesm.experiment.model.pimm.Parameter;
import org.ietr.preesm.experiment.model.pimm.Parameterizable;
import org.ietr.preesm.experiment.model.pimm.PiGraph;
import org.ietr.preesm.experiment.model.pimm.Port;
import org.ietr.preesm.experiment.model.pimm.Refinement;
import org.ietr.preesm.experiment.model.pimm.util.PiMMVisitor;

public class SubgraphConnector extends PiMMVisitor {

	// Actor in the outer graph corresponding to the currently visited graph
	private AbstractActor currentActor = null;

	private Map<PiGraph, List<ActorByGraphReplacement>> graphReplacements = new HashMap<PiGraph, List<ActorByGraphReplacement>>();

	public Map<PiGraph, List<ActorByGraphReplacement>> getGraphReplacements() {
		return graphReplacements;
	}

	private PiGraph currentGraph = null;

	@Override
	public void visitPiGraph(PiGraph pg) {
		PiGraph oldGraph = currentGraph;
		currentGraph = pg;
		for (AbstractActor v : pg.getVertices()) {
			v.accept(this);
		}
		for (Parameter p : pg.getParameters()) {
			p.accept(this);
		}
		currentGraph = oldGraph;
	}

	@Override
	public void visitActor(Actor a) {
		// If the refinement of the Actor a points to the description of
		// PiGraph, visit it to connect the subgraph to its supergraph
		AbstractActor aa = a.getRefinement().getAbstractActor();
		if (aa != null && aa instanceof PiGraph) {
			PiGraph innerGraph = (PiGraph) aa;
			// Connect all Fifos and Dependencies incoming into a and outgoing
			// from a in order to make them incoming into innerGraph and
			// outgoing from innerGraph instead
			reconnectPiGraph(a, innerGraph);

			currentActor = innerGraph;
			innerGraph.accept(this);

			ActorByGraphReplacement replacement = new ActorByGraphReplacement(
					a, innerGraph);
			if (!graphReplacements.containsKey(currentGraph)) {
				graphReplacements.put(currentGraph,
						new ArrayList<ActorByGraphReplacement>());
			}
			graphReplacements.get(currentGraph).add(replacement);
		}
	}

	/*
	 * Connect all the ports of the PiGraph to the Fifos and Dependencies
	 * connected to the ports of the Actor
	 */
	private void reconnectPiGraph(Actor a, PiGraph pg) {
		boolean found = false;
		for (DataInputPort dip1 : a.getDataInputPorts()) {
			found = false;
			for (DataInputPort dip2 : pg.getDataInputPorts()) {
				if (dip1.getName().equals(dip2.getName())) {
					Fifo fifo = dip1.getIncomingFifo();
					dip2.setIncomingFifo(fifo);
					fifo.setTargetPort(dip2);
					
					dip2.setExpression(dip1.getExpression());
										
					found = true;
					break;
				}
			}
			if (!found) {
				throw new RuntimeException("PiGraph" + pg.getName()
						+ "does not have a corresponding DataInputPort for "
						+ dip1.getName() + " of Actor " + a.getName());
			}
		}
		for (DataOutputPort dop1 : a.getDataOutputPorts()) {
			found = false;
			for (DataOutputPort dop2 : pg.getDataOutputPorts()) {
				if (dop1.getName().equals(dop2.getName())) {
					Fifo fifo = dop1.getOutgoingFifo();
					dop2.setOutgoingFifo(fifo);
					fifo.setSourcePort(dop2);
					
					dop2.setExpression(dop1.getExpression());
					
					found = true;
					break;
				}
			}
			if (!found) {
				throw new RuntimeException("PiGraph" + pg.getName()
						+ "does not have a corresponding DataOutputPort for "
						+ dop1.getName() + " of Actor " + a.getName());
			}
		}
		for (ConfigInputPort cip1 : a.getConfigInputPorts()) {
			found = false;
			for (ConfigInputPort cip2 : pg.getConfigInputPorts()) {
				if (cip1.getName().equals(cip2.getName())) {
					Dependency dep = cip1.getIncomingDependency();
					cip2.setIncomingDependency(dep);
					dep.setGetter(cip2);										
					found = true;
					break;
				}
			}
			if (!found) {
				throw new RuntimeException("PiGraph" + pg.getName()
						+ "does not have a corresponding ConfigInputPort for "
						+ cip1.getName() + " of Actor " + a.getName());
			}
		}
		for (ConfigOutputPort cop1 : a.getConfigOutputPorts()) {
			found = false;
			for (ConfigOutputPort cop2 : pg.getConfigOutputPorts()) {
				if (cop1.getName().equals(cop2.getName())) {
					for (Dependency dep : cop1.getOutgoingDependencies()) {
						cop2.getOutgoingDependencies().add(dep);
						dep.setSetter(cop2);						
					}
					found = true;
					break;
				}
			}
			if (!found) {
				throw new RuntimeException("PiGraph" + pg.getName()
						+ "does not have a corresponding ConfigOutputPort for "
						+ cop1.getName() + " of Actor " + a.getName());
			}
		}
	}

	@Override
	public void visitDataInputInterface(DataInputInterface dii) {
		// Connect the interface to the incoming fifo from the outer graph
		DataInputPort correspondingPort = null;
		for (DataInputPort dip : currentActor.getDataInputPorts()) {
			if (dip.getName() == dii.getName()) {
				correspondingPort = dip;
				break;
			}
		}
		if (correspondingPort != null) {
			dii.setGraphPort(correspondingPort);
		}
	}

	@Override
	public void visitDataOutputInterface(DataOutputInterface doi) {
		// Connect the interface to the outgoing fifo to the outer graph
		DataOutputPort correspondingPort = null;
		for (DataOutputPort dop : currentActor.getDataOutputPorts()) {
			if (dop.getName() == doi.getName()) {
				correspondingPort = dop;
				break;
			}
		}
		if (correspondingPort != null) {
			doi.setGraphPort(correspondingPort);
		}
	}

	@Override
	public void visitConfigInputInterface(ConfigInputInterface cii) {
		// Connect the interface to the incoming dependencies from the outer
		// graph
		ConfigInputPort correspondingPort = null;
		for (ConfigInputPort cip : currentActor.getConfigInputPorts()) {
			if (cip.getName() == cii.getName()) {
				correspondingPort = cip;
				break;
			}
		}
		if (correspondingPort != null) {
			cii.setGraphPort(correspondingPort);
		}
	}

	@Override
	public void visitConfigOutputInterface(ConfigOutputInterface coi) {
		// Connect the interface to the outgoing dependencies to the outer graph
		ConfigOutputPort correspondingPort = null;
		for (ConfigOutputPort cop : currentActor.getConfigOutputPorts()) {
			if (cop.getName() == coi.getName()) {
				correspondingPort = cop;
				break;
			}
		}
		if (correspondingPort != null) {
			coi.setGraphPort(correspondingPort);
		}
	}

	@Override
	public void visitParameter(Parameter p) {
		// We only do something for ConfigInputInterface (subclass of
		// Parameter), other parameters are visited but nothing should be done
		// DO NOTHING
	}

	@Override
	public void visitAbstractActor(AbstractActor aa) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void visitAbstractVertex(AbstractVertex av) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void visitConfigInputPort(ConfigInputPort cip) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void visitConfigOutputPort(ConfigOutputPort cop) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void visitDataInputPort(DataInputPort dip) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void visitDataOutputPort(DataOutputPort dop) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void visitDelay(Delay d) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void visitDependency(Dependency d) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void visitExpression(Expression e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void visitFifo(Fifo f) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void visitInterfaceActor(InterfaceActor ia) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void visitISetter(ISetter is) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void visitParameterizable(Parameterizable p) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void visitPort(Port p) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void visitRefinement(Refinement r) {
		throw new UnsupportedOperationException();
	}

	public class ActorByGraphReplacement {
		public Actor toBeRemoved;
		public PiGraph toBeAdded;

		public ActorByGraphReplacement(Actor toBeRemoved, PiGraph toBeAdded) {
			this.toBeRemoved = toBeRemoved;
			this.toBeAdded = toBeAdded;
		}
	}
}
