package org.ietr.preesm.experiment.ui.pimm.features;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.impl.AbstractAddFeature;
import org.eclipse.graphiti.mm.algorithms.Polygon;
import org.eclipse.graphiti.mm.algorithms.Rectangle;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.algorithms.styles.Font;
import org.eclipse.graphiti.mm.algorithms.styles.Orientation;
import org.eclipse.graphiti.mm.pictograms.BoxRelativeAnchor;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeCreateService;
import org.eclipse.graphiti.ui.services.GraphitiUi;
import org.eclipse.graphiti.util.IColorConstant;
import org.ietr.preesm.experiment.model.pimm.Graph;
import org.ietr.preesm.experiment.model.pimm.Parameter;

/**
 * Add feature to add a new {@link Parameter} to the {@link Graph}
 * 
 * @author kdesnos
 * 
 */
public class AddConfigInputInterfaceFeature extends AbstractAddFeature {

	public static final IColorConstant CFG_IN_TEXT_FOREGROUND = IColorConstant.BLACK;

	public static final IColorConstant CFG_IN_FOREGROUND = AddParameterFeature.PARAMETER_FOREGROUND;

	public static final IColorConstant CFG_IN_BACKGROUND = AddParameterFeature.PARAMETER_BACKGROUND;

	/**
	 * The default constructor of {@link AddConfigInputInterfaceFeature}
	 * 
	 * @param fp
	 *            the feature provider
	 */
	public AddConfigInputInterfaceFeature(IFeatureProvider fp) {
		super(fp);
	}

	@Override
	public PictogramElement add(IAddContext context) {
		Parameter addedParam = (Parameter) context.getNewObject();
		Diagram targetDiagram = (Diagram) context.getTargetContainer();

		// CONTAINER SHAPE WITH ROUNDED RECTANGLE
		IPeCreateService peCreateService = Graphiti.getPeCreateService();
		ContainerShape containerShape = peCreateService.createContainerShape(
				targetDiagram, true);

		// define a default size for the shape
		int width = 16;
		int height = 16;
		IGaService gaService = Graphiti.getGaService();
		Font font = gaService.manageDefaultFont(getDiagram(), false, true);
		int fontHeight = GraphitiUi.getUiLayoutService()
				.calculateTextSize("Abcq", font).getHeight();

		Rectangle invisibleRectangle = gaService
				.createInvisibleRectangle(containerShape);
		gaService.setLocationAndSize(invisibleRectangle, context.getX(),
				context.getY(), 200, height + fontHeight + 2);

		Polygon triangle; // need to access it later
		{
			final BoxRelativeAnchor boxAnchor = peCreateService
					.createBoxRelativeAnchor(containerShape);
			boxAnchor.setRelativeWidth(0.5);
			boxAnchor.setRelativeHeight(1.0);
			boxAnchor.setReferencedGraphicsAlgorithm(invisibleRectangle);

			// create and set graphics algorithm for the anchor
			int xy[] = { width / 2, 0, width, height, 0, height };
			triangle = gaService.createPolygon(boxAnchor, xy);
			triangle.setForeground(manageColor(CFG_IN_FOREGROUND));
			triangle.setBackground(manageColor(CFG_IN_BACKGROUND));
			triangle.setLineWidth(2);
			gaService.setLocationAndSize(triangle, -width / 2, -height, width,
					height);

			// if added Parameter has no resource we add it to the
			// resource of the graph
			if (addedParam.eResource() == null) {
				Graph graph = (Graph) getBusinessObjectForPictogramElement(getDiagram());
				graph.addInterface(addedParam);
			}
			// link(boxAnchor, port);
		}

		// Name of the ConfigInIf - SHAPE WITH TEXT
		{
			// create and set text graphics algorithm
			// create shape for text
			Shape shape = peCreateService.createShape(containerShape, false);
			Text text = gaService.createText(shape, addedParam.getName());
			text.setForeground(manageColor(CFG_IN_TEXT_FOREGROUND));
			text.setVerticalAlignment(Orientation.ALIGNMENT_TOP);
			text.setHorizontalAlignment(Orientation.ALIGNMENT_CENTER);
			// vertical alignment has as default value "center"
			text.setFont(font);
			text.setHeight(20);
			text.setWidth(200);
			link(shape, addedParam);
		}
		// create link and wire it
		link(containerShape, addedParam);

		// Call the layout feature
		layoutPictogramElement(containerShape);

		return containerShape;
	}

	@Override
	public boolean canAdd(IAddContext context) {
		// Check that the user wants to add an ConfigInputInterface to the
		// Diagram
		return context.getNewObject() instanceof Parameter
				&& ((Parameter) context.getNewObject())
						.isConfigurationInterface()
				&& context.getTargetContainer() instanceof Diagram;
	}

}
