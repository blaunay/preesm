package org.ietr.preesm.experiment.ui.pimm.features;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.impl.AbstractAddFeature;
import org.eclipse.graphiti.mm.algorithms.Rectangle;
import org.eclipse.graphiti.mm.algorithms.RoundedRectangle;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.algorithms.styles.Orientation;
import org.eclipse.graphiti.mm.pictograms.BoxRelativeAnchor;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeCreateService;
import org.eclipse.graphiti.util.IColorConstant;
import org.ietr.preesm.experiment.model.pimm.Graph;
import org.ietr.preesm.experiment.model.pimm.InputPort;
import org.ietr.preesm.experiment.model.pimm.SinkInterface;

/**
 * Add feature to add a new {@link SinkInterface} to the {@link Graph}
 * 
 * @author kdesnos
 * 
 */
public class AddSinkInterfaceFeature extends AbstractAddFeature {

	public static final IColorConstant SNK_TEXT_FOREGROUND = IColorConstant.BLACK;

	public static final IColorConstant SNK_FOREGROUND = AddOutputPortFeature.OUTPUT_PORT_FOREGROUND;

	public static final IColorConstant SNK_BACKGROUND = AddOutputPortFeature.OUTPUT_PORT_BACKGROUND;

	/**
	 * The default constructor of {@link AddSinkInterfaceFeature}
	 * 
	 * @param fp
	 *            the feature provider
	 */
	public AddSinkInterfaceFeature(IFeatureProvider fp) {
		super(fp);
	}

	@Override
	public PictogramElement add(IAddContext context) {
		SinkInterface snkInterface = (SinkInterface) context.getNewObject();
		InputPort port = snkInterface.getInputPorts().get(0);
		Diagram targetDiagram = (Diagram) context.getTargetContainer();

		// CONTAINER SHAPE WITH ROUNDED RECTANGLE
		IPeCreateService peCreateService = Graphiti.getPeCreateService();
		ContainerShape containerShape = peCreateService.createContainerShape(
				targetDiagram, true);

		// define a default size for the shape
		int width = 16;
		int height = 16;
		int invisibRectHeight = 20;
		IGaService gaService = Graphiti.getGaService();

		Rectangle invisibleRectangle = gaService
				.createInvisibleRectangle(containerShape);
		gaService.setLocationAndSize(invisibleRectangle, context.getX(),
				context.getY(), 200, invisibRectHeight);


		RoundedRectangle roundedRectangle; // need to access it later
		{
			final BoxRelativeAnchor boxAnchor = peCreateService
					.createBoxRelativeAnchor(containerShape);
			boxAnchor.setRelativeWidth(0.0);
			boxAnchor
					.setRelativeHeight((((double) invisibRectHeight - (double) height))
							/ 2.0 / (double) invisibRectHeight);
			boxAnchor.setReferencedGraphicsAlgorithm(invisibleRectangle);

			// create and set graphics algorithm for the anchor
			roundedRectangle = gaService
					.createRoundedRectangle(boxAnchor, 5, 5);
			roundedRectangle.setForeground(manageColor(SNK_FOREGROUND));
			roundedRectangle.setBackground(manageColor(SNK_BACKGROUND));
			roundedRectangle.setLineWidth(2);
			gaService.setLocationAndSize(roundedRectangle, 0, 0, width, height);

			// if added SinkInterface has no resource we add it to the
			// resource of the graph
			if (snkInterface.eResource() == null) {
				Graph graph = (Graph) getBusinessObjectForPictogramElement(getDiagram());
				graph.addInterfaceActor(snkInterface);
			}
			link(boxAnchor, port);
		}

		// Name of the SinkInterface - SHAPE WITH TEXT
		{
			// create and set text graphics algorithm
			// create shape for text
			Shape shape = peCreateService.createShape(containerShape, false);
			Text text = gaService.createText(shape, snkInterface.getName());
			text.setForeground(manageColor(SNK_TEXT_FOREGROUND));
			text.setHorizontalAlignment(Orientation.ALIGNMENT_RIGHT);
			// vertical alignment has as default value "center"
			text.setFont(gaService.manageDefaultFont(getDiagram(), false, true));
			text.setHeight(20);
			text.setWidth(200);
			link(shape, snkInterface);
		}
		
		// create link and wire it
		link(containerShape, snkInterface);
		
		// Call the layout feature
		layoutPictogramElement(containerShape);

		return containerShape;
	}

	@Override
	public boolean canAdd(IAddContext context) {
		// Check that the user wants to add an SinkInterface to the Diagram
		return context.getNewObject() instanceof SinkInterface
				&& context.getTargetContainer() instanceof Diagram;
	}

}
