package org.ietr.preesm.experiment.ui.pimemoc.features;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.algorithms.Rectangle;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.algorithms.styles.Font;
import org.eclipse.graphiti.mm.algorithms.styles.Orientation;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.util.ColorConstant;
import org.eclipse.graphiti.util.IColorConstant;

public class AddInputPortFeature extends AbstractAddActorPortFeature {

	public static final IColorConstant INPUT_PORT_FOREGROUND = AddActorFeature.ACTOR_FOREGROUND;
	public static final IColorConstant INPUT_PORT_BACKGROUND = new ColorConstant(
			182, 215, 122);
	public static final PortPosition INPUT_PORT_POSITION = PortPosition.LEFT;

	/**
	 * Default constructor
	 * 
	 * @param fp
	 *            the feature provider
	 */
	public AddInputPortFeature(IFeatureProvider fp) {
		super(fp);
	}

	@Override
	public String getName() {
		return "Add Input Port";
	}

	@Override
	public String getDescription() {
		return "Add an input port to the Actor";
	}

	@Override
	public PortPosition getPosition() {
		return INPUT_PORT_POSITION;
	}

	@Override
	public GraphicsAlgorithm addPortGA(GraphicsAlgorithm containerShape) {
		// Get the GaService
		IGaService gaService = Graphiti.getGaService();
		// Create the port GraphicAlgorithm
		Rectangle rectangle = gaService.createPlainRectangle(containerShape);
		rectangle.setForeground(manageColor(INPUT_PORT_FOREGROUND));
		rectangle.setBackground(manageColor(INPUT_PORT_BACKGROUND));
		rectangle.setLineWidth(1);
		gaService.setSize(rectangle, PORT_ANCHOR_GA_SIZE, PORT_ANCHOR_GA_SIZE);
		gaService.setLocation(rectangle, 0, 1 + (16 - PORT_ANCHOR_GA_SIZE) / 2);
		return rectangle;
	}

	@Override
	public GraphicsAlgorithm addPortLabel(GraphicsAlgorithm containerShape,
			String portName) {
		// Get the GaService
		IGaService gaService = Graphiti.getGaService();

		// Create the text
		final Text text = gaService.createText(containerShape);
		text.setValue(portName);
		text.setFont(getPortFont());
		text.setForeground(manageColor(PORT_TEXT_FOREGROUND));

		// Layout the text
		text.setHorizontalAlignment(Orientation.ALIGNMENT_RIGHT);
		gaService.setLocationAndSize(text, PORT_ANCHOR_GA_SIZE, 0,
				containerShape.getWidth() - PORT_ANCHOR_GA_SIZE, 16); 

		return text;
	}

	@Override
	public Font getPortFont() {
		// Get the GaService
		IGaService gaService = Graphiti.getGaService();
		return gaService.manageDefaultFont(getDiagram(), false, false);
	}

}
