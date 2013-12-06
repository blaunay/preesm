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
 * Custom feature to move down a port.
 * 
 * @author jheulot
 * 
 */
public class MoveDownActorPortFeature extends AbstractCustomFeature {

	protected boolean hasDoneChanges = false;

	/**
	 * Default Constructor
	 * 
	 * @param fp
	 *            the feature provider
	 */
	public MoveDownActorPortFeature(IFeatureProvider fp) {
		super(fp);
	}

	@Override
	public String getName() {
		return "Move down Port";
	}

	@Override
	public String getDescription() {
		return "Move down the Port";
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
						ret = ret && actor.getInputPorts().indexOf(port) < actor.getInputPorts().size()-1;
					}else if(kind.compareTo("output") == 0){
						ret = actor.getOutputPorts().size() > 1;
						ret = ret && actor.getOutputPorts().indexOf(port) < actor.getOutputPorts().size()-1;
					}else if(kind.compareTo("cfg_input") == 0){
						ret = actor.getConfigInputPorts().size() > 1;
						ret = ret && actor.getConfigInputPorts().indexOf(port) < actor.getConfigInputPorts().size()-1;
					}else if(kind.compareTo("cfg_output") == 0){
						ret = actor.getConfigOutputPorts().size() > 1;
						ret = ret && actor.getConfigOutputPorts().indexOf(port) < actor.getConfigOutputPorts().size()-1;
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
				
				portToMoveDown = (Port) bo;
				actor = (Actor)(portToMoveDown.eContainer());
				String portKind = portToMoveDown.getKind();

				// Switch Port into Actor Object
				if(portKind.compareTo("input") == 0){
					portToMoveDownIndex = actor.getInputPorts().indexOf(portToMoveDown);
					portToMoveUp = actor.getInputPorts().get(portToMoveDownIndex+1);
				}else if(portKind.compareTo("output") == 0){
					portToMoveDownIndex = actor.getOutputPorts().indexOf(portToMoveDown);
					portToMoveUp = actor.getOutputPorts().get(portToMoveDownIndex+1);
				}else if(portKind.compareTo("cfg_input") == 0){
					portToMoveDownIndex = actor.getConfigInputPorts().indexOf(portToMoveDown);
					portToMoveUp = actor.getConfigInputPorts().get(portToMoveDownIndex+1);
				}else if(portKind.compareTo("cfg_output") == 0){
					portToMoveDownIndex = actor.getConfigOutputPorts().indexOf(portToMoveDown);
					portToMoveUp = actor.getConfigOutputPorts().get(portToMoveDownIndex+1);
				}else{
					return;
				}
				portToMoveUpIndex = portToMoveDownIndex+1;
				
				// Switch Graphical Elements
				int anchorToMoveUpIndex=-1, anchorToMoveDownIndex;
				ContainerShape CsActor = (ContainerShape) ((BoxRelativeAnchor) pes[0]).getReferencedGraphicsAlgorithm().getPictogramElement();
				EList<Anchor> anchors = CsActor.getAnchors();
				
				anchorToMoveDownIndex = anchors.indexOf(pes[0]);
				
				for(Anchor a : anchors){
					if(a.getLink().getBusinessObjects().get(0).equals(portToMoveUp)){
						anchorToMoveUpIndex = anchors.indexOf(a);					
						break;
					}
				}
				
				if(anchorToMoveUpIndex == -1)
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
