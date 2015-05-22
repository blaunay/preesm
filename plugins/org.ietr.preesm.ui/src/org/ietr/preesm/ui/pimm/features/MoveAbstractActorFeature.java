/*******************************************************************************
 * Copyright or © or Copr. IETR/INSA: Maxime Pelcat, Jean-François Nezan,
 * Karol Desnos, Julien Heulot
 * 
 * [mpelcat,jnezan,kdesnos,jheulot]@insa-rennes.fr
 * 
 * This software is a computer program whose purpose is to prototype
 * parallel applications.
 * 
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info". 
 * 
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability. 
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or 
 * data to be ensured and,  more generally, to use and operate it in the 
 * same conditions as regards security. 
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 ******************************************************************************/
package org.ietr.preesm.ui.pimm.features;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.graphiti.datatypes.ILocation;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IMoveShapeContext;
import org.eclipse.graphiti.features.impl.DefaultMoveShapeFeature;
import org.eclipse.graphiti.mm.algorithms.styles.Point;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.FreeFormConnection;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaCreateService;
import org.eclipse.graphiti.services.IPeLayoutService;
import org.ietr.preesm.experiment.model.pimm.Fifo;
import org.ietr.preesm.experiment.model.pimm.Port;

/**
 * The Move Feature for {@link AbstractActor}
 * 
 * @author kdesnos
 * 
 */
public class MoveAbstractActorFeature extends DefaultMoveShapeFeature {

	/**
	 * Default constructor for {@link MoveAbstractActorFeature}
	 * 
	 * @param fp
	 *            the feature provider
	 */
	public MoveAbstractActorFeature(IFeatureProvider fp) {
		super(fp);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void postMoveShape(IMoveShapeContext context) {
		// Here, we layout bendpoint of connections incoming/outgoing to this
		// AbstractVertex Ports
		ContainerShape cs = (ContainerShape) context.getPictogramElement();
		EList<Anchor> anchors = cs.getAnchors();

		for (Anchor anchor : anchors) {
			// If the anchor does not correspond to a port, skip the loop
			// (e.g. ChopBoxAnchors are used as connection points for
			// dependencies)
			if (!(getBusinessObjectForPictogramElement(anchor) instanceof Port)) {
				continue;
			}

			// Retrieve the connections of the anchor.
			List<FreeFormConnection> connections = new ArrayList<>();

			EList<Connection> iConnections = anchor.getIncomingConnections();

			boolean isSrcMove = false;
			if (!iConnections.isEmpty()) {

				connections
						.addAll((Collection<? extends FreeFormConnection>) iConnections);
				isSrcMove = false;
			}
			EList<Connection> oConnections = anchor.getOutgoingConnections();
			if (!oConnections.isEmpty()) {
				connections
						.addAll((Collection<? extends FreeFormConnection>) oConnections);
				isSrcMove = true;
			}

			for (FreeFormConnection connection : connections) {
				// Check wether the FIFO corresponding to the connection has a
				// delay
				Object fifo = getBusinessObjectForPictogramElement(connection);

				// If the fifo has no delay, it remove a bendpoint if there are
				// at least two
				// if the fifo has a delay, remove a bendpoint (if any).
				int nbBendpoints = (fifo != null && ((Fifo) fifo).getDelay() != null) ? -1
						: 0;

				// Check if the last or first Bendpoint exists.
				// If so, move it with the same delta as the associated port.
				// (but it will still be removed and recreated.)
				boolean bendpointExists = false;
				int bendpointX = -1, bendpointY = -1;
				int index = connection.getBendpoints().size() - 1;
				if (index > nbBendpoints && !isSrcMove) {
					bendpointExists = true;
					bendpointX = connection.getBendpoints().get(index).getX();
					bendpointY = connection.getBendpoints().get(index).getY();
					connection.getBendpoints().remove(index);
				}
				if (index > nbBendpoints && isSrcMove) {
					bendpointExists = true;
					bendpointX = connection.getBendpoints().get(0).getX();
					bendpointY = connection.getBendpoints().get(0).getY();
					connection.getBendpoints().remove(0);
				}

				// Add one bendpoints it didn't exist, move it otherwise
				IPeLayoutService peLayoutService = Graphiti
						.getPeLayoutService();
				IGaCreateService createService = Graphiti.getGaCreateService();
				int midHeight = anchor.getGraphicsAlgorithm().getHeight() / 2 - 1;

				// Creation cases
				if (isSrcMove) {
					ILocation srcLoc = peLayoutService
							.getLocationRelativeToDiagram(connection.getStart());
					Point pSrc = null;
					if (!bendpointExists) {
						pSrc = createService.createPoint(srcLoc.getX() + 20,
								srcLoc.getY() + midHeight);
					} else {
						pSrc = createService.createPoint(
								bendpointX + context.getDeltaX(), bendpointY
										+ context.getDeltaY());
					}
					connection.getBendpoints().add(0, pSrc);
				}
				if (!isSrcMove) {
					ILocation trgtLoc = peLayoutService
							.getLocationRelativeToDiagram(connection.getEnd());
					Point pTrgt = null;
					if (!bendpointExists) {
						pTrgt = createService.createPoint(trgtLoc.getX() - 20,
								trgtLoc.getY() + midHeight);
					} else {
						pTrgt = createService.createPoint(
								bendpointX + context.getDeltaX(), bendpointY
										+ context.getDeltaY());
					}
					connection.getBendpoints().add(pTrgt);
				}
			}
		}

		return;
	}
}
