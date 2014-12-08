package org.ietr.preesm.experiment.model.transformation.properties;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;
import org.ietr.preesm.experiment.model.pimm.DataPort;
import org.ietr.preesm.experiment.model.pimm.Expression;
import org.ietr.preesm.experiment.model.pimm.Fifo;

public class FifoPropertiesSection extends DataPortPropertiesUpdater implements
		ITabbedPropertyConstants {

	/**
	 * Items of the {@link ActorPropertiesSection}
	 */
	private CLabel lblType;
	private Text txtTypeObj;

	private Text txtSourcePortExpression;
	private CLabel lblSourcePortExpression;
	private CLabel lblSourcePortValueObj;
	private CLabel lblSourcePortValue;

	private Text txtTargetPortExpression;
	private CLabel lblTargetPortExpression;
	private CLabel lblTargetPortValueObj;
	private CLabel lblTargetPortValue;

	private final int FIRST_COLUMN_WIDTH = 150;

	@Override
	public void createControls(Composite parent,
			TabbedPropertySheetPage tabbedPropertySheetPage) {
		super.createControls(parent, tabbedPropertySheetPage);

		TabbedPropertySheetWidgetFactory factory = getWidgetFactory();
		Composite composite = factory.createFlatFormComposite(parent);
		FormData data;

		/**** TYPE ****/
		txtTypeObj = factory.createText(composite, "");
		data = new FormData();
		data.left = new FormAttachment(0, FIRST_COLUMN_WIDTH);
		data.right = new FormAttachment(25, 0);
		txtTypeObj.setLayoutData(data);
		txtTypeObj.setEnabled(true);

		lblType = factory.createCLabel(composite, "Data type:");
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(txtTypeObj, -HSPACE);
		lblType.setLayoutData(data);

		/**** SOURCE PORT ****/
		/**** EXPRESSION ****/
		txtSourcePortExpression = factory.createText(composite, "");
		data = new FormData();
		data.left = new FormAttachment(0, FIRST_COLUMN_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(txtTypeObj);
		txtSourcePortExpression.setLayoutData(data);
		txtSourcePortExpression.setEnabled(true);

		lblSourcePortExpression = factory.createCLabel(composite,
				"Source port rate:");
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(txtSourcePortExpression, -HSPACE);
		data.top = new FormAttachment(lblType);
		lblSourcePortExpression.setLayoutData(data);

		/**** VALUE ****/
		lblSourcePortValueObj = factory.createCLabel(composite, "");
		data = new FormData();
		data.left = new FormAttachment(0, FIRST_COLUMN_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(txtSourcePortExpression);
		lblSourcePortValueObj.setLayoutData(data);

		lblSourcePortValue = factory.createCLabel(composite, "Default Value:");
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(lblSourcePortValueObj, -HSPACE);
		data.top = new FormAttachment(lblSourcePortExpression);
		lblSourcePortValue.setLayoutData(data);

		/**** TARGET PORT ****/
		/**** EXPRESION ****/
		txtTargetPortExpression = factory.createText(composite, "");
		data = new FormData();
		data.left = new FormAttachment(0, FIRST_COLUMN_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(lblSourcePortValueObj);
		txtTargetPortExpression.setLayoutData(data);
		txtTargetPortExpression.setEnabled(true);

		lblTargetPortExpression = factory.createCLabel(composite,
				"Target port rate:");
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(txtTargetPortExpression, -HSPACE);
		data.top = new FormAttachment(lblSourcePortValue);
		lblTargetPortExpression.setLayoutData(data);

		/**** VALUE ****/
		lblTargetPortValueObj = factory.createCLabel(composite, "");
		data = new FormData();
		data.left = new FormAttachment(0, FIRST_COLUMN_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(txtTargetPortExpression);
		lblTargetPortValueObj.setLayoutData(data);

		lblTargetPortValue = factory.createCLabel(composite, "Default Value:");
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(lblTargetPortValueObj, -HSPACE);
		data.top = new FormAttachment(lblTargetPortExpression);
		lblTargetPortValue.setLayoutData(data);

		/*** Type box listener ***/
		txtTypeObj.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				PictogramElement pe = getSelectedPictogramElement();
				if (pe != null) {
					EObject bo = Graphiti.getLinkService()
							.getBusinessObjectForLinkedPictogramElement(pe);
					if (bo == null)
						return;

					if (bo instanceof Fifo) {
						Fifo fifo = (Fifo) bo;
						if (!txtTypeObj.getText().equals(fifo.getType())) {
							setNewType(fifo, txtTypeObj.getText());
							getDiagramTypeProvider().getDiagramBehavior()
									.refreshContent();
						}
					}
				}
			}
		});

		/** SourcePort expression listener */
		txtSourcePortExpression.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				PictogramElement pe = getSelectedPictogramElement();
				if (pe != null) {
					EObject bo = Graphiti.getLinkService()
							.getBusinessObjectForLinkedPictogramElement(pe);
					if (bo == null)
						return;
					if (bo instanceof Fifo) {
						DataPort port = ((Fifo) bo).getSourcePort();
						updateDataPortProperties(port, txtSourcePortExpression);
						PictogramElement pict = Graphiti.getLinkService().getPictogramElements(getDiagram(), port).get(0);
						getDiagramTypeProvider().getDiagramBehavior()
						.refreshRenderingDecorators((PictogramElement) pict.eContainer());
					}					
				}
				refresh();
			}
		});
		
		/** TargetPort expression listener */
		txtTargetPortExpression.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				PictogramElement pe = getSelectedPictogramElement();
				if (pe != null) {
					EObject bo = Graphiti.getLinkService()
							.getBusinessObjectForLinkedPictogramElement(pe);
					if (bo == null)
						return;
					if (bo instanceof Fifo) {
						DataPort port = ((Fifo) bo).getTargetPort();
						updateDataPortProperties(port, txtTargetPortExpression);
						PictogramElement pict = Graphiti.getLinkService().getPictogramElements(getDiagram(), port).get(0);
						getDiagramTypeProvider().getDiagramBehavior()
						.refreshRenderingDecorators((PictogramElement) pict.eContainer());
					}					
				}
				
				refresh();
			}

		});

		// txtSourcePortExpression.addModifyListener(new ModifyListener() {
		// @Override
		// public void modifyText(ModifyEvent e) {
		// updateProperties();
		// }
		// });
		//
		// txtTargetPortExpression.addModifyListener(new ModifyListener() {
		// @Override
		// public void modifyText(ModifyEvent e) {
		// updateProperties();
		// }
		// });

	}

	// private void updateProperties() {
	// PictogramElement pe = getSelectedPictogramElement();
	// if (pe != null) {
	// EObject bo = Graphiti.getLinkService()
	// .getBusinessObjectForLinkedPictogramElement(pe);
	// if (bo == null)
	// return;
	//
	// if (bo instanceof Fifo) {
	// Fifo fifo = (Fifo) bo;
	// updateDataPortProperties(fifo.getSourcePort(),
	// txtSourcePortExpression);
	// updateDataPortProperties(fifo.getTargetPort(),
	// txtTargetPortExpression);
	//
	// getDiagramTypeProvider().getDiagramBehavior()
	// .refreshRenderingDecorators(
	// (PictogramElement) (pe.eContainer()));
	// }
	// }
	// refresh();
	// }

	/**
	 * Safely set a new type to the {@link Fifo}.
	 * 
	 * @param fifo
	 *            {@link Fifo} to set
	 * @param value
	 *            String value
	 */
	private void setNewType(final Fifo fifo, final String value) {
		TransactionalEditingDomain editingDomain = getDiagramTypeProvider()
				.getDiagramBehavior().getEditingDomain();
		editingDomain.getCommandStack().execute(
				new RecordingCommand(editingDomain) {
					@Override
					protected void doExecute() {
						fifo.setType(value);
					}
				});
	}

	@Override
	public void refresh() {
		PictogramElement pe = getSelectedPictogramElement();

		if (pe != null) {
			Object bo = Graphiti.getLinkService()
					.getBusinessObjectForLinkedPictogramElement(pe);
			if (bo == null)
				return;

			if (bo instanceof Fifo) {
				Fifo fifo = (Fifo) bo;
				txtTypeObj.setText(fifo.getType());

				txtSourcePortExpression.setEnabled(true);

				Expression srcRate = fifo.getSourcePort().getExpression();
				if (!txtSourcePortExpression.getText().equals(
						srcRate.getString())) {
					txtSourcePortExpression.setText(srcRate.getString());
				}

				lblSourcePortValueObj.setText(srcRate.evaluate());

				txtTargetPortExpression.setEnabled(true);

				Expression tgtRate = fifo.getTargetPort().getExpression();
				if (!txtTargetPortExpression.getText().equals(
						tgtRate.getString())) {
					txtTargetPortExpression.setText(tgtRate.getString());
				}

				lblTargetPortValueObj.setText(tgtRate.evaluate());
			}

		}
	}
}
