/**
 * 
 */
package org.ietr.preesm.core.scenario.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.forms.editor.SharedHeaderFormEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.ietr.preesm.core.scenario.Scenario;
import org.ietr.preesm.core.scenario.ScenarioParser;
import org.ietr.preesm.core.scenario.ScenarioWriter;

/**
 * The scenario editor allows to change all parameters depending
 * on both algorithm and architecture.
 * 
 * @author mpelcat
 */
public class ScenarioEditor extends SharedHeaderFormEditor implements IPropertyListener {

	boolean isDirty = false;
	
	private IFile scenarioFile = null;
	
	private Scenario scenario;
	
	public ScenarioEditor() {
		super();
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		
		setSite(site);
		setInput(input);
		setPartName(input.getName());
					
		if(input instanceof FileEditorInput){
			FileEditorInput fileInput = (FileEditorInput) input;
			scenarioFile = fileInput.getFile();
		}
		
		if(scenarioFile != null){
			scenario = new Scenario();
			ScenarioParser parser = new ScenarioParser();
			parser.parseXmlFile(scenarioFile);
			scenario = parser.parseDocument();
			
		}
	}

	@Override
	protected void addPages() {
		//this.activateSite();
		IFormPage overviewPage = new OverviewPage(scenario,this, "Overview","Overview");
		overviewPage.addPropertyListener(this);
		IFormPage constraintsPage = new ConstraintsPage(scenario,this, "Constraints","Constraints");
		constraintsPage.addPropertyListener(this);
		IFormPage timingsPage = new TimingsPage(scenario,this, "Timings","Timings");
		
		try {
			addPage(overviewPage);
			addPage(constraintsPage);
			addPage(timingsPage);
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {

		ScenarioWriter writer = new ScenarioWriter(scenario);
		writer.generateScenarioDOM();
		writer.writeDom(scenarioFile);

		isDirty = false;
		this.firePropertyChange(PROP_DIRTY);
	}
	
	@Override
	public boolean isDirty() {
		return isDirty;
	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void propertyChanged(Object source, int propId) {
		if(propId == PROP_DIRTY){
			isDirty = true;
			this.firePropertyChange(propId);
		}
	}

}
