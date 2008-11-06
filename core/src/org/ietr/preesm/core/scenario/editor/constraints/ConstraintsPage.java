/**
 * 
 */
package org.ietr.preesm.core.scenario.editor.constraints;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
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
import org.ietr.preesm.core.scenario.editor.FileSelectionAdapter;
import org.ietr.preesm.core.scenario.editor.Messages;
import org.ietr.preesm.core.scenario.editor.timings.ExcelTimingParser;

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

		// Constrints file chooser section
		createFileSection(managedForm, Messages.getString("Constraints.file"),
				Messages.getString("Constraints.fileDescription"),
				Messages.getString("Constraints.fileEdit"),
				scenario.getTimingManager().getTimingFileURL(),
				Messages.getString("Constraints.fileBrowseTitle"),
				"xls");
		
		createConstraintsSection(managedForm, Messages.getString("Constraints.title"),
				Messages.getString("Constraints.description"));
		
		
		managedForm.refresh();

	}

	/**
	 * Creates a generic section
	 */
	public Section createSection(IManagedForm mform, String title,
			String desc, int numColumns) {
		
		final ScrolledForm form = mform.getForm();
		FormToolkit toolkit = mform.getToolkit();
		Section section = toolkit.createSection(form.getBody(), Section.TWISTIE
				| Section.TITLE_BAR | Section.DESCRIPTION | Section.EXPANDED);
		section.setText(title);
		section.setDescription(desc);
		toolkit.createCompositeSeparator(section);
		return section;
	}

	/**
	 * Creates a generic section
	 */
	public Composite createSection(IManagedForm mform, String title,
			String desc, int numColumns, GridData gridData) {

		
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
		section.setLayoutData(gridData);
		return client;
	}

	/**
	 * Creates the section editing constraints
	 */
	private void createConstraintsSection(IManagedForm managedForm, String title, String desc) {

		// Creates the section
		managedForm.getForm().setLayout(new FillLayout());
		Section section = createSection(managedForm, title, desc, 2);
		section.setLayout(new ColumnLayout());
	
		// Creates the section part containing the tree with SDF vertices
		new SDFTreeSection(scenario, section, managedForm.getToolkit(),Section.DESCRIPTION,this);
	}

	/**
	 * Function of the property listener used to transmit the dirty property
	 */
	@Override
	public void propertyChanged(Object source, int propId) {
		if(source instanceof SDFCheckStateListener && propId == PROP_DIRTY)
			firePropertyChange(PROP_DIRTY);
		
	}


	/**
	 * Creates a section to edit a file
	 * 
	 * @param mform form containing the section
	 * @param title section title
	 * @param desc description of the section
	 * @param fileEdit text to display in text label
	 * @param initValue initial value of Text
	 * @param browseTitle title of file browser
	 */
	private void createFileSection(IManagedForm mform, String title, String desc, String fileEdit, String initValue, String browseTitle,String fileExtension) {
		
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.heightHint = 120;
		Composite client = createSection(mform, title, desc, 2,gridData);
		FormToolkit toolkit = mform.getToolkit();

		GridData gd = new GridData();
		toolkit.createLabel(client, fileEdit);

		Text text = toolkit.createText(client, initValue, SWT.SINGLE);
		text.setData(title);
		text.addModifyListener(new ModifyListener(){

			@Override
			public void modifyText(ModifyEvent e) {
				Text text = (Text)e.getSource();
				String type = ((String)text.getData());

				ExcelConstraintsParser parser = new ExcelConstraintsParser(scenario);
				parser.parse(text.getText());
				
				firePropertyChange(PROP_DIRTY);
				
			}});
		
		gd.widthHint =400;
		text.setLayoutData(gd);

		final Button button = toolkit.createButton(client, Messages.getString("Overview.browse"), SWT.PUSH);
		SelectionAdapter adapter = new FileSelectionAdapter(text,client.getShell(),browseTitle,fileExtension);
		button.addSelectionListener(adapter);
		
		toolkit.paintBordersFor(client);
	}
}
