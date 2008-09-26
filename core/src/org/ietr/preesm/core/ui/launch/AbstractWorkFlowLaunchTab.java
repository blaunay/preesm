/*********************************************************
Copyright or � or Copr. IETR/INSA: Matthieu Wipliez, Jonathan Piat,
Maxime Pelcat, Peng Cheng Mu, Jean-Fran�ois Nezan, Micka�l Raulet

[mwipliez,jpiat,mpelcat,pmu,jnezan,mraulet]@insa-rennes.fr

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


/**
 * 
 */
package org.ietr.preesm.core.ui.launch;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Containing common funtionalities of launch tabs.
 * 
 * @author mpelcat
 */
public abstract class AbstractWorkFlowLaunchTab extends
		AbstractLaunchConfigurationTab {

	/**
	 * current Composite
	 */
	private Composite currentComposite;

	/**
	 * file attribute name to save the entered file
	 */
	private String fileAttributeName = null;
	
	
	/**
	 * file path of the current Tab. There can be only one file chooser for the
	 * moment
	 */
	private Text filePath;

	

	/**
	 * Displays a file browser in a shell
	 */
	protected void browseFiles() {
		FileDialog fileDialog = new FileDialog(currentComposite.getShell());
		try {
			fileDialog.setFilterPath(ResourcesPlugin.getWorkspace().getRoot()
					.getLocation().toOSString());
		} catch (Exception e) {
			System.out.print(e.getMessage());
		}

		String path = fileDialog.open();
		if (path != null) {
			filePath.setText(path.trim());
		}
	}

	@Override
	public void createControl(Composite parent) {

		currentComposite = new Composite(parent, SWT.NONE);
		setControl(currentComposite);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		currentComposite.setLayout(gridLayout);

	}

	/**
	 * Displays a file text window with a browser button.
	 * 
	 * @param title
	 *            A line of text displayed before the file chooser
	 * @param attributeName
	 *            The name of the attribute in which the property should be
	 *            saved
	 */
	public void drawFileChooser(String title, String attributeName) {

		Label label2 = new Label(currentComposite, SWT.NONE);
		label2.setText(title);
		fileAttributeName = attributeName;

		new Label(currentComposite, SWT.NONE);

		Button buttonBrowse = new Button(currentComposite, SWT.PUSH);
		buttonBrowse.setText("Browse...");
		buttonBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				browseFiles();
			}
		});

		filePath = new Text(currentComposite, SWT.BORDER);

		GridData layoutData = new GridData(SWT.FILL, SWT.TOP, true, false);
		layoutData.widthHint = 200;
		filePath.setLayoutData(layoutData);
		filePath.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setDirty(true);
				updateLaunchConfigurationDialog();
			}
		});

	}
	
	

	protected Composite getCurrentComposite() {
		return currentComposite;
	}
	

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			filePath.setText(configuration.getAttribute(fileAttributeName, ""));
		} catch (CoreException e) {
			// OcamlPlugin.logError("ocaml plugin error", e);
			filePath.setText("");
		}

		setDirty(false);
	}
	
	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		return true;
	}
	
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		// Saving the file path chosen in a tab attribute
		if(filePath != null && fileAttributeName != null){
			configuration.setAttribute(fileAttributeName,filePath.getText());
		}
		setDirty(false);
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		
		configuration.setAttribute(fileAttributeName,"");
		setDirty(false);
	}
	
}