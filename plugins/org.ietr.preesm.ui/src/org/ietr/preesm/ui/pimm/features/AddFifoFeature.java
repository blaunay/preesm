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

import org.eclipse.graphiti.datatypes.ILocation;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IAddConnectionContext;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.context.impl.MoveShapeContext;
import org.eclipse.graphiti.features.impl.AbstractAddFeature;
import org.eclipse.graphiti.mm.GraphicsAlgorithmContainer;
import org.eclipse.graphiti.mm.algorithms.Polygon;
import org.eclipse.graphiti.mm.algorithms.Polyline;
import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.FreeFormConnection;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeCreateService;
import org.eclipse.graphiti.util.ColorConstant;
import org.eclipse.graphiti.util.IColorConstant;
import org.ietr.preesm.experiment.model.pimm.Fifo;
import org.ietr.preesm.experiment.model.pimm.PiGraph;

/**
 * Add feature to add a {@link Fifo} to the {@link Diagram}
 * 
 * @author kdesnos
 * 
 */
public class AddFifoFeature extends AbstractAddFeature {

	public static final IColorConstant FIFO_FOREGROUND = new ColorConstant(100,
			100, 100);

	/**
	 * The default constructor of {@link AddFifoFeature}
	 * 
	 * @param fp
	 *            the feature provider
	 */
	public AddFifoFeature(IFeatureProvider fp) {
		super(fp);
	}

	@Override
	public PictogramElement add(IAddContext context) {
		IAddConnectionContext addContext = (IAddConnectionContext) context;
		Fifo addedFifo = (Fifo) context.getNewObject();
		IPeCreateService peCreateService = Graphiti.getPeCreateService();

		// if added Fifo has no resource we add it to the resource
		// of the diagram
		// in a real scenario the business model would have its own resource
		if (addedFifo.eResource() == null) {
			PiGraph graph = (PiGraph) getBusinessObjectForPictogramElement(getDiagram());
			graph.getFifos().add(addedFifo);
		}

		// CONNECTION WITH POLYLINE
		FreeFormConnection connection = peCreateService
				.createFreeFormConnection(getDiagram());
		createEdge(addContext, connection);

		// Add the arrow
		ConnectionDecorator cd;
		cd = peCreateService.createConnectionDecorator(connection, false, 1.0,
				true);
		createArrow(cd);

		// create link and wire it
		link(connection, addedFifo);

		return connection;
	}

	@Override
	public boolean canAdd(IAddContext context) {
		// Return true if the given Business object is a Fifo and the context is
		// an instance of IAddConnectionContext
		if (context instanceof IAddConnectionContext
				&& context.getNewObject() instanceof Fifo) {
			return true;
		}
		return false;
	}

	/**
	 * Create the arrow {@link Polygon} that will decorate the {@link Fifo}
	 * 
	 * @param gaContainer
	 *            the {@link GraphicsAlgorithmContainer}
	 * @return
	 */
	protected Polygon createArrow(GraphicsAlgorithmContainer gaContainer) {
		IGaService gaService = Graphiti.getGaService();
		Polygon polygon = gaService.createPlainPolygon(gaContainer, new int[] {
				-10, 5, 0, 0, -10, -5 });
		polygon.setForeground(manageColor(FIFO_FOREGROUND));
		polygon.setBackground(manageColor(FIFO_FOREGROUND));
		polygon.setLineWidth(2);
		return polygon;
	}

	/**
	 * @param addContext
	 * @param connection
	 */
	protected void createEdge(IAddConnectionContext addContext,
			FreeFormConnection connection) {
		// Set the connection src and snk
		connection.setStart(addContext.getSourceAnchor());
		connection.setEnd(addContext.getTargetAnchor());

		// Layout the edge
		// Call the move feature of the anchor owner to layout the connection
		MoveAbstractActorFeature moveFeature = new MoveAbstractActorFeature(
				getFeatureProvider());
		// Move source
		ContainerShape cs = (ContainerShape) connection.getStart()
				.getReferencedGraphicsAlgorithm().getPictogramElement();
		MoveShapeContext moveCtxt = new MoveShapeContext(cs);
		moveCtxt.setDeltaX(0);
		moveCtxt.setDeltaY(0);
		ILocation csLoc = Graphiti.getPeLayoutService()
				.getLocationRelativeToDiagram(cs);
		moveCtxt.setLocation(csLoc.getX(), csLoc.getY());
		moveFeature.moveShape(moveCtxt);
		// Move target
		cs = (ContainerShape) connection.getEnd()
				.getReferencedGraphicsAlgorithm().getPictogramElement();
		moveCtxt = new MoveShapeContext(cs);
		moveCtxt.setDeltaX(0);
		moveCtxt.setDeltaY(0);
		csLoc = Graphiti.getPeLayoutService()
				.getLocationRelativeToDiagram(cs);
		moveCtxt.setLocation(csLoc.getX(), csLoc.getY());
		moveFeature.moveShape(moveCtxt);

		// Create the associated Polyline
		IGaService gaService = Graphiti.getGaService();
		Polyline polyline = gaService.createPolyline(connection);
		polyline.setLineWidth(2);
		polyline.setForeground(manageColor(FIFO_FOREGROUND));
	}
}
