/**
 * 
 */
package org.ietr.preesm.core.scenario.editor.constraints;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.ietr.preesm.core.scenario.Scenario;
import org.ietr.preesm.core.scenario.editor.Messages;

/**
 * Constraint editor within the implementation editor
 * 
 * @author mpelcat
 */
public class ConstraintsPage extends FormPage implements IPropertyListener {

	/**
	 * Currently edited scenario
	 */
	private Scenario scenario;

	public ConstraintsPage(Scenario scenario, FormEditor editor, String id, String title) {
		super(editor, id, title);
		this.scenario = scenario;
	}

	/**
	 * Initializes the display content
	 */
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		super.createFormContent(managedForm);
		
		ScrolledForm f = managedForm.getForm();
		f.setText(Messages.getString("Constraints.title"));
		f.getBody().setLayout(new GridLayout());
		
		createConstraintsSection(managedForm, Messages.getString("Constraints.title"),
				Messages.getString("Constraints.description"));
		
		
		managedForm.refresh();

	}

	/**
	 * Creates the section editing constraints
	 */
	private void createConstraintsSection(IManagedForm managedForm, String title, String desc) {

		// Creates the section
		managedForm.getForm().setLayout(new FillLayout());
		Section section = createSection(managedForm, Messages.getString("Constraints.title"), Messages.getString("Constraints.description"), 2);
		section.setLayout(new ColumnLayout());
	
		// Creates the section part containing the tree with SDF vertices
		SDFTreeSection sdfTreeSection = new SDFTreeSection(scenario, section, managedForm.getToolkit(),Section.DESCRIPTION,this);
	}

	/**
	 * Creates a generic section
	 */
	private Section createSection(IManagedForm mform, String title,
			String desc, int numColumns) {
		
		final ScrolledForm form = mform.getForm();
		FormToolkit toolkit = mform.getToolkit();
		Section section = toolkit.createSection(form.getBody(), Section.TWISTIE
				| Section.TITLE_BAR | Section.DESCRIPTION | Section.EXPANDED);
		section.setText(title);
		section.setDescription(desc);
		toolkit.createCompositeSeparator(section);
		Composite client = toolkit.createComposite(section);
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.numColumns = numColumns;
		client.setLayout(layout);
		section.setClient(client);
		section.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(false);
			}
		});
		return section;
	}

	/**
	 * Function of the property listener used to transmit the dirty property
	 */
	@Override
	public void propertyChanged(Object source, int propId) {
		if(source instanceof SDFCheckStateListener && propId == PROP_DIRTY)
			firePropertyChange(PROP_DIRTY);
		
	}
}
