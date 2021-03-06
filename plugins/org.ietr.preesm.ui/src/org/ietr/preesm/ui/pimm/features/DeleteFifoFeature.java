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

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IDeleteContext;
import org.eclipse.graphiti.features.context.IRemoveContext;
import org.eclipse.graphiti.features.context.impl.DeleteContext;
import org.eclipse.graphiti.features.context.impl.MultiDeleteInfo;
import org.eclipse.graphiti.features.context.impl.RemoveContext;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.ui.features.DefaultDeleteFeature;
import org.ietr.preesm.experiment.model.pimm.Delay;
import org.ietr.preesm.experiment.model.pimm.Fifo;

/**
 * Delete feature to delete a {@link Fifo} from a graph.
 * 
 * @author kdesnos
 * 
 */
public class DeleteFifoFeature extends DefaultDeleteFeature {

	/**
	 * If the {@link Fifo} has a {@link Delay}, it is associated to 2
	 * {@link Connection}. One of the two {@link Connection} might needs to be
	 * removed manually in the {@link #postDelete(IDeleteContext)} method. This
	 * {@link Connection} attribute will be used to store this remaining
	 * {@link Connection}.
	 */
	protected Connection remainingConnection = null;

	/**
	 * Default constructor for {@link DeleteFifoFeature}.
	 * 
	 * @param fp
	 *            the feature provider
	 */
	public DeleteFifoFeature(IFeatureProvider fp) {
		super(fp);
	}

	@Override
	public void preDelete(IDeleteContext context) {
		// If the Fifo has a delay, first delete it.
		Connection connection = (Connection) context.getPictogramElement();
		Fifo fifo = (Fifo) getBusinessObjectForPictogramElement(connection);

		if (fifo.getDelay() != null) {
			// Is the "first half" of the connection (the one before the delay)
			// the one given to the delete context.
			Object obj = getBusinessObjectForPictogramElement(connection
					.getStart().getParent());
			if (obj instanceof Delay) {
				DeleteContext delCtxt = new DeleteContext(connection.getStart()
						.getParent());
				// To deactivate dialog box
				delCtxt.setMultiDeleteInfo(new MultiDeleteInfo(false, false, 0));

				getFeatureProvider().getDeleteFeature(delCtxt).delete(delCtxt);
			}

			// Is the second half of the connection the one given to the delete
			// context.
			obj = getBusinessObjectForPictogramElement(connection.getEnd()
					.getParent());
			if (obj instanceof Delay) {
				// In this case, only the first half of the connection, and the
				// delay will be removed by the delete function.
				// Backup the remaining second half to remove it in postDelete()

				// The only outgoing connection from the unique anchor of the
				// delay is the second half of the removed fifo connection
				remainingConnection = connection.getEnd().getParent()
						.getAnchors().get(0).getOutgoingConnections().get(0);

				DeleteContext delCtxt = new DeleteContext(connection.getEnd()
						.getParent());
				// To deactivate dialog box
				delCtxt.setMultiDeleteInfo(new MultiDeleteInfo(false, false, 0));
				getFeatureProvider().getDeleteFeature(delCtxt).delete(delCtxt);
			}

		}

		// Super call
		super.preDelete(context);
	}

	@Override
	public void postDelete(IDeleteContext context) {
		// If there was a remaining connection, remove it !
		// The Fifo was also associated to the other half of the connection wich
		// was removed during the delete() method call.
		if (remainingConnection != null) {
			IRemoveContext rmCtxt = new RemoveContext(remainingConnection);
			getFeatureProvider().getRemoveFeature(rmCtxt).remove(rmCtxt);
		}

		// Super call !
		super.postDelete(context);
	}
}
