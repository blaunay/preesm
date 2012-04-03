package org.ietr.preesm.codegen.model.call;

import java.util.ArrayList;
import java.util.List;

import net.sf.dftools.algorithm.model.sdf.SDFAbstractVertex;
import net.sf.dftools.algorithm.model.sdf.SDFEdge;
import net.sf.dftools.algorithm.model.sdf.SDFGraph;

import org.ietr.preesm.codegen.model.buffer.AbstractBufferContainer;
import org.ietr.preesm.codegen.model.buffer.Buffer;
import org.ietr.preesm.codegen.model.main.AbstractCodeElement;
import org.ietr.preesm.codegen.model.printer.CodeZoneId;
import org.ietr.preesm.codegen.model.printer.IAbstractPrinter;

public abstract class SpecialBehaviorCall extends AbstractCodeElement {

	protected List<Buffer> inputBuffers;

	/**
	 * The buffer to explode
	 */
	protected List<Buffer> outputBuffers;

	public SpecialBehaviorCall(String name,
			AbstractBufferContainer parentContainer,
			SDFAbstractVertex correspondingVertex) {
		super(name, parentContainer, correspondingVertex);
		inputBuffers = new ArrayList<Buffer>();
		outputBuffers = new ArrayList<Buffer>();
		// Adding output buffers
		for (SDFEdge edge : ((SDFGraph) correspondingVertex.getBase())
				.incomingEdgesOf(correspondingVertex)) {
			AbstractBufferContainer parentBufferContainer = parentContainer;
			while (parentBufferContainer != null
					&& parentBufferContainer.getBuffer(edge) == null) {
				parentBufferContainer = parentBufferContainer
						.getParentContainer();
			}
			if (parentBufferContainer != null) {
				inputBuffers.add(parentBufferContainer.getBuffer(edge));
			}
		}

		// Adding input buffers
		for (SDFEdge edge : ((SDFGraph) correspondingVertex.getBase())
				.outgoingEdgesOf(correspondingVertex)) {
			AbstractBufferContainer parentBufferContainer = parentContainer;
			while (parentBufferContainer != null
					&& parentBufferContainer.getBuffer(edge) == null) {
				parentBufferContainer = parentBufferContainer
						.getParentContainer();
			}
			if (parentBufferContainer != null) {
				outputBuffers.add(parentBufferContainer.getBuffer(edge));
			}
		}
	}

	public abstract String getBehaviorId();

	public void accept(IAbstractPrinter printer, Object currentLocation) {
		currentLocation = printer.visit(this, CodeZoneId.body, currentLocation); // Visit
	}

	public List<Buffer> getOutputBuffers() {
		return outputBuffers;
	}

	public List<Buffer> getInputBuffers() {
		return inputBuffers;
	}

}
