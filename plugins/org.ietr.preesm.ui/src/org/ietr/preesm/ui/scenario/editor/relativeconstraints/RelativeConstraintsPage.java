/*********************************************************
Copyright or � or Copr. IETR/INSA: Matthieu Wipliez, Jonathan Piat,
Maxime Pelcat, Jean-Fran�ois Nezan, Micka�l Raulet, Karol Desnos

[mwipliez,jpiat,mpelcat,jnezan,mraulet,kdesnos]@insa-rennes.fr

This software is a computer program whose purpose is to prototype
parallel applications.

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
 *********************************************************/

package org.ietr.preesm.ui.scenario.editor.relativeconstraints;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.ietr.preesm.core.scenario.PreesmScenario;
import org.ietr.preesm.core.scenario.serialize.PreesmAlgorithmListContentProvider;
import org.ietr.preesm.ui.scenario.editor.FileSelectionAdapter;
import org.ietr.preesm.ui.scenario.editor.Messages;

/**
 * Relative constraints editor within the scenario editor
 * 
 * @author mpelcat
 * @author kdesnos
 */
public class RelativeConstraintsPage extends FormPage implements IPropertyListener {

	final PreesmScenario scenario;
	TableViewer tableViewer = null;

	public RelativeConstraintsPage(PreesmScenario scenario, FormEditor editor, String id,
			String title) {
		super(editor, id, title);

		this.scenario = scenario;
	}

	/**
	 * Creates the elements to display
	 */
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		// TODO Auto-generated method stub
		super.createFormContent(managedForm);

		ScrolledForm form = managedForm.getForm();
		// FormToolkit toolkit = managedForm.getToolkit();
		form.setText(Messages.getString("RelativeConstraints.title"));

		GridLayout layout = new GridLayout();
		form.getBody().setLayout(layout);

		// Timing file chooser section
		createFileSection(managedForm,
				Messages.getString("RelativeConstraints.file"),
				Messages.getString("RelativeConstraints.fileDescription"),
				Messages.getString("RelativeConstraints.fileEdit"), scenario
						.getTimingManager().getExcelFileURL(),
				Messages.getString("RelativeConstraints.fileBrowseTitle"), "xls");

		createRelativeConstraintsSection(managedForm, Messages.getString("RelativeConstraints.title"),
				Messages.getString("RelativeConstraints.description"));

		managedForm.refresh();
		managedForm.reflow(true);

	}

	/**
	 * Creates the section editing relarive constraints
	 */
	private void createRelativeConstraintsSection(IManagedForm managedForm, String title,
			String desc) {

		// Creates the section
		managedForm.getForm().setLayout(new FillLayout());
		Composite container = createSection(managedForm, title, desc, 1,
				new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
		FormToolkit toolkit = managedForm.getToolkit();

		addTable(container, toolkit);
	}

	/**
	 * Creates a generic section
	 */
	public Composite createSection(IManagedForm mform, String title,
			String desc, int numColumns, GridData gridData) {

		final ScrolledForm form = mform.getForm();
		FormToolkit toolkit = mform.getToolkit();
		Section section = toolkit.createSection(form.getBody(), ExpandableComposite.TWISTIE
				| ExpandableComposite.TITLE_BAR | Section.DESCRIPTION | ExpandableComposite.EXPANDED);
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
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(false);
			}
		});
		section.setLayoutData(gridData);
		return client;
	}

	/**
	 * Adds a table to edit relative constraints
	 */
	protected void addTable(Composite parent, FormToolkit toolkit) {

		Composite tablecps = toolkit.createComposite(parent);
		tablecps.setVisible(true);

		tableViewer = new TableViewer(tablecps, SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		Table table = tableViewer.getTable();
		table.setLayout(new GridLayout());
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		tableViewer.setContentProvider(new PreesmAlgorithmListContentProvider());

		final SDFTableLabelProvider labelProvider = new SDFTableLabelProvider(
				scenario, tableViewer, this);
		tableViewer.setLabelProvider(labelProvider);

		// Create columns
		final TableColumn column1 = new TableColumn(table, SWT.NONE, 0);
		column1.setText(Messages.getString("RelativeConstraints.taskColumn"));

		final TableColumn column2 = new TableColumn(table, SWT.NONE, 1);
		column2.setText(Messages.getString("RelativeConstraints.constraintColumn"));

		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent e) {
				labelProvider.handleDoubleClick((IStructuredSelection) e
						.getSelection());
			}
		});

		final Table tref = table;
		final Composite comp = tablecps;

		// Setting the column width
		tablecps.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				Rectangle area = comp.getClientArea();
				Point size = tref.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				ScrollBar vBar = tref.getVerticalBar();
				int width = area.width - tref.computeTrim(0, 0, 0, 0).width - 2;
				if (size.y > area.height + tref.getHeaderHeight()) {
					Point vBarSize = vBar.getSize();
					width -= vBarSize.x;
				}
				Point oldSize = tref.getSize();
				if (oldSize.x > area.width) {
					column1.setWidth(width / 4 - 1);
					column2.setWidth(width - column1.getWidth());
					tref.setSize(area.width, area.height);
				} else {
					tref.setSize(area.width, area.height);
					column1.setWidth(width / 4 - 1);
					column2.setWidth(width - column1.getWidth());
				}
			}
		});

		tableViewer.setInput(scenario);
		tablecps.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.FILL_VERTICAL));

		// Tree is refreshed in case of algorithm modifications
		parent.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {
				tableViewer.refresh();

			}

		});
	}

	/**
	 * Function of the property listener used to transmit the dirty property
	 */
	@Override
	public void propertyChanged(Object source, int propId) {
		if (source instanceof SDFTableLabelProvider && propId == PROP_DIRTY)
			firePropertyChange(PROP_DIRTY);

	}

	/**
	 * Creates a section to edit a file
	 * 
	 * @param mform
	 *            form containing the section
	 * @param title
	 *            section title
	 * @param desc
	 *            description of the section
	 * @param fileEdit
	 *            text to display in text label
	 * @param initValue
	 *            initial value of Text
	 * @param browseTitle
	 *            title of file browser
	 */
	private void createFileSection(IManagedForm mform, String title,
			String desc, String fileEdit, String initValue, String browseTitle,
			String fileExtension) {

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.heightHint = 120;
		Composite client = createSection(mform, title, desc, 3, gridData);

		FormToolkit toolkit = mform.getToolkit();

		GridData gd = new GridData();
		toolkit.createLabel(client, fileEdit);

		Text text = toolkit.createText(client, initValue, SWT.SINGLE);
		text.setData(title);

		// If the text is modified or Enter key pressed, relative constraints are imported
		text.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				Text text = (Text) e.getSource();
				scenario.getTimingManager().setExcelFileURL(text.getText());
				scenario.getTimingManager().importTimings(scenario);
				tableViewer.refresh();
				firePropertyChange(PROP_DIRTY);

			}
		});
		text.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR) {
					Text text = (Text) e.getSource();
					scenario.getTimingManager().setExcelFileURL(text.getText());
					scenario.getTimingManager().importTimings(scenario);
					tableViewer.refresh();
				}

			}

			@Override
			public void keyReleased(KeyEvent e) {

			}
		});

		gd.widthHint = 400;
		text.setLayoutData(gd);

		// Add a "Refresh" button to the scenario editor
		final Button refreshButton = toolkit.createButton(client,
				Messages.getString("RelativeConstraints.fileRefresh"), SWT.PUSH);
		refreshButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// Cause scenario editor to import timings from excel sheet
				scenario.getTimingManager().importTimings(scenario);
				tableViewer.refresh();
				// Force the "file has changed" property of scenario.
				// Timing changes will have no effects if the scenario
				// is not saved.
				firePropertyChange(PROP_DIRTY);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		toolkit.paintBordersFor(client);

		final Button browseButton = toolkit.createButton(client,
				Messages.getString("Overview.browse"), SWT.PUSH);
		SelectionAdapter browseAdapter = new FileSelectionAdapter(text,
				client.getShell(), browseTitle, fileExtension);
		browseButton.addSelectionListener(browseAdapter);

		final Button exportButton = toolkit.createButton(client,
				Messages.getString("RelativeConstraints.exportExcel"), SWT.PUSH);
		exportButton.addSelectionListener(new ExcelRelativeConstraintsWriter(scenario));

	}
}
