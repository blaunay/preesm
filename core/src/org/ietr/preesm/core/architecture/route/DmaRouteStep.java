/**
 * 
 */
package org.ietr.preesm.core.architecture.route;

import java.util.List;

import org.ietr.preesm.core.architecture.simplemodel.AbstractNode;
import org.ietr.preesm.core.architecture.simplemodel.Dma;
import org.ietr.preesm.core.architecture.simplemodel.Operator;

/**
 * Route step where the sender uses a dma to send data in
 * parallel with its processing
 * 
 * @author mpelcat
 */
public class DmaRouteStep extends NodeRouteStep {

	private Dma dma;

	public static final String id = "NodeRouteStep";
	
	public DmaRouteStep(Operator sender, List<AbstractNode> nodes, Operator receiver, Dma dma) {
		super(sender,nodes, receiver);
		this.dma = dma;
	}

	@Override
	public String getId() {
		return id;
	}
}
