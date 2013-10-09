package org.ietr.preesm.experiment.ui.pimm.features;

import org.eclipse.emf.common.util.EList;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.BoxRelativeAnchor;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.ietr.preesm.experiment.model.pimm.Actor;
import org.ietr.preesm.experiment.model.pimm.Port;

/**
 * Custom feature to move up a port.
 * 
 * @author jheulot
 * 
 */
public class MoveUpActorPortFeature extends AbstractCustomFeature {

	protected boolean hasDoneChanges = false;

	/**
	 * Default Constructor
	 * 
	 * @param fp
	 *            the feature provider
	 */
	public MoveUpActorPortFeature(IFeatureProvider fp) {
		super(fp);
	}

	@Override
	public String getName() {
		return "Move up Port";
	}

	@Override
	public String getDescription() {
		return "Move up the Port";
	}

	@Override
	public boolean canExecute(ICustomContext context) {
		// allow move up if exactly one pictogram element
		// representing a Port is selected 
		// and it is not the first port
		boolean ret = false;
		PictogramElement[] pes = context.getPictogramElements();
		if (pes != null && pes.length == 1) {
			Object bo = getBusinessObjectForPictogramElement(pes[0]);
			if (bo instanceof Port) {
				Port port = (Port) bo;
				if (port.eContainer() instanceof Actor) {
					Actor actor = (Actor)(port.eContainer());
					String kind = port.getKind();
					if(kind.compareTo("input") == 0){
						ret = actor.getInputPorts().size() > 1;
						ret = ret && actor.getInputPorts().indexOf(port) > 0;
					}else if(kind.compareTo("output") == 0){
						ret = actor.getOutputPorts().size() > 1;
						ret = ret && actor.getOutputPorts().indexOf(port) > 0;
					}else if(kind.compareTo("cfg_input") == 0){
						ret = actor.getConfigInputPorts().size() > 1;
						ret = ret && actor.getConfigInputPorts().indexOf(port) > 0;
					}else if(kind.compareTo("cfg_output") == 0){
						ret = actor.getConfigOutputPorts().size() > 1;
						ret = ret && actor.getConfigOutputPorts().indexOf(port) > 0;
					}
				}
			}
		}
		return ret;
	}
	
	protected boolean avoidNegativeCoordinates() {
		return false;
	}

	@Override
	public void execute(ICustomContext context) {
		// Re-check if only one element is selected
		PictogramElement[] pes = context.getPictogramElements();
		if (pes != null && pes.length == 1) {
			Object bo = getBusinessObjectForPictogramElement(pes[0]);
			if (bo instanceof Port) {
				Port portToMoveUp, portToMoveDown;
				int portToMoveUpIndex, portToMoveDownIndex;
				Actor actor;
				
				portToMoveUp = (Port) bo;
				actor = (Actor)(portToMoveUp.eContainer());
				String portKind = portToMoveUp.getKind();

				// Switch Port into Actor Object
				if(portKind.compareTo("input") == 0){
					portToMoveUpIndex = actor.getInputPorts().indexOf(portToMoveUp);
					portToMoveDown = actor.getInputPorts().get(portToMoveUpIndex-1);
				}else if(portKind.compareTo("output") == 0){
					portToMoveUpIndex = actor.getOutputPorts().indexOf(portToMoveUp);
					portToMoveDown = actor.getOutputPorts().get(portToMoveUpIndex-1);
				}else if(portKind.compareTo("cfg_input") == 0){
					portToMoveUpIndex = actor.getConfigInputPorts().indexOf(portToMoveUp);
					portToMoveDown = actor.getConfigInputPorts().get(portToMoveUpIndex-1);
				}else if(portKind.compareTo("cfg_output") == 0){
					portToMoveUpIndex = actor.getConfigOutputPorts().indexOf(portToMoveUp);
					portToMoveDown = actor.getConfigOutputPorts().get(portToMoveUpIndex-1);
				}else{
					return;
				}
				portToMoveDownIndex = portToMoveUpIndex-1;
				
				// Switch Graphical Elements
				int anchorToMoveUpIndex, anchorToMoveDownIndex=-1;
				ContainerShape CsActor = (ContainerShape) ((BoxRelativeAnchor) pes[0]).getReferencedGraphicsAlgorithm().getPictogramElement();
				EList<Anchor> anchors = CsActor.getAnchors();
				
				anchorToMoveUpIndex = anchors.indexOf(pes[0]);
				
				for(Anchor a : anchors){
					if(a.getLink().getBusinessObjects().get(0).equals(portToMoveDown)){
						anchorToMoveDownIndex = anchors.indexOf(a);					
						break;
					}
				}
				
				if(anchorToMoveDownIndex == -1)
					return;

				// Do Modifications
				this.hasDoneChanges = true;
				
				if(portKind.compareTo("input") == 0){
					actor.getInputPorts().move(portToMoveDownIndex, portToMoveUpIndex);
				}else if(portKind.compareTo("output") == 0){
					actor.getOutputPorts().move(portToMoveDownIndex, portToMoveUpIndex);
				}else if(portKind.compareTo("cfg_input") == 0){
					actor.getConfigInputPorts().move(portToMoveDownIndex, portToMoveUpIndex);
				}else if(portKind.compareTo("cfg_output") == 0){
					actor.getConfigOutputPorts().move(portToMoveDownIndex, portToMoveUpIndex);
				}
				anchors.move(anchorToMoveDownIndex, anchorToMoveUpIndex);
				
				
				// Layout the Port
				layoutPictogramElement(pes[0]);
				updatePictogramElement(pes[0]);

				// Layout the actor				
				layoutPictogramElement(CsActor);
				updatePictogramElement(CsActor);
				
			}
		}
	}

	@Override
	public boolean hasDoneChanges() {
		return this.hasDoneChanges;
	}

}
