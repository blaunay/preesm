/**
 * 
 */
package org.ietr.preesm.core.scenario.editor.constraints;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.forms.widgets.Section;
import org.ietr.preesm.core.architecture.ArchitectureComponentType;
import org.ietr.preesm.core.architecture.MultiCoreArchitecture;
import org.ietr.preesm.core.architecture.Operator;
import org.ietr.preesm.core.scenario.ConstraintGroup;
import org.ietr.preesm.core.scenario.Scenario;
import org.sdf4j.model.sdf.SDFAbstractVertex;
import org.sdf4j.model.sdf.SDFGraph;

/**
 * Listener of the check state of the SDF tree but also of the selection
 * modification of the current core definition. It updates the check
 * state of the vertices depending on the constraint groups in the
 * scenario
 * 
 * @author mpelcat
 */
public class SDFCheckStateListener implements SelectionListener,
		ICheckStateListener {

	/**
	 * Currently edited scenario
	 */
	private Scenario scenario = null;
	
	/**
	 * Current operator
	 */
	private Operator currentOpDef = null;

	/**
	 * Current section (necessary to diplay busy status)
	 */
	private Section section = null;

	/**
	 * Tree viewer used to set the checked status
	 */
	private CheckboxTreeViewer treeViewer = null;

	/**
	 * Content provider used to get the elements currently displayed
	 */
	private SDFTreeContentProvider contentProvider = null;

	/**
	 * Constraints page used as a property listener to change the dirty state
	 */
	private IPropertyListener propertyListener = null;

	public SDFCheckStateListener(Section section, Scenario scenario) {
		super();
		this.scenario = scenario;
		this.section = section;
	}

	/**
	 * Sets the different necessary attributes
	 */
	public void setTreeViewer(CheckboxTreeViewer treeViewer, SDFTreeContentProvider contentProvider,IPropertyListener propertyListener) {
		this.treeViewer = treeViewer;
		this.contentProvider = contentProvider;
		this.propertyListener = propertyListener;
	}

	/**
	 * Fired when an element has been checked or unchecked
	 */
	@Override
	public void checkStateChanged(CheckStateChangedEvent event) {
		final Object element = event.getElement();
		final boolean isChecked = event.getChecked();
		BusyIndicator.showWhile(section.getDisplay(), new Runnable() {

			public void run() {
				if (element instanceof SDFGraph) {
					SDFGraph graph = (SDFGraph) element;
					fireOnCheck(graph, isChecked);
				} else if (element instanceof SDFAbstractVertex) {
					SDFAbstractVertex vertex = (SDFAbstractVertex) element;
					fireOnCheck(vertex, isChecked);

				}
			}
		});
	}

	/**
	 * Adds or remove constraints for all vertices in the graph depending 
	 * on the isChecked status
	 */
	public void fireOnCheck(SDFGraph graph, boolean isChecked) {
		if (currentOpDef != null) {
			if (isChecked)
				scenario.getConstraintGroupManager().addConstraints(
						currentOpDef, graph.vertexSet());
			else
				scenario.getConstraintGroupManager().removeConstraints(
						currentOpDef, graph.vertexSet());
		}
		updateCheck();
	}

	/**
	 * Adds or remove a constraint depending on the isChecked status
	 */
	public void fireOnCheck(SDFAbstractVertex vertex, boolean isChecked) {
		if (currentOpDef != null) {
			if (isChecked)
				scenario.getConstraintGroupManager().addConstraint(
						currentOpDef, vertex);
			else
				scenario.getConstraintGroupManager().removeConstraint(
						currentOpDef, vertex);
		}
		updateCheck();
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub

	}

	/**
	 * Core combo box listener that selects the current core
	 */
	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.getSource() instanceof Combo) {
			Combo combo = ((Combo) e.getSource());
			String item = combo.getItem(combo.getSelectionIndex());

			MultiCoreArchitecture archi = (MultiCoreArchitecture) combo.getData();
			currentOpDef = (Operator)archi.getComponent(ArchitectureComponentType.operator,item);
			updateCheck();
		}

	}

	/**
	 * Update the check status of the whole tree
	 */
	public void updateCheck() {
		SDFGraph currentGraph = contentProvider.getCurrentGraph();
		if (scenario != null && currentOpDef != null && currentGraph != null) {
			Set<SDFAbstractVertex> cgSet = new HashSet<SDFAbstractVertex>();

			for (ConstraintGroup cg : scenario.getConstraintGroupManager()
					.getOpConstraintGroups(currentOpDef)) {
				
				// Retrieves the elements in the tree that have the same name as
				// the ones to select in the constraint group
				for(SDFAbstractVertex vertex:cg.getVertices()){
					cgSet.add(currentGraph.getVertex(vertex.getName()));
				}
			}

			treeViewer.setCheckedElements(cgSet.toArray());
			
			if(cgSet.size() == currentGraph.vertexSet().size())
				treeViewer.setChecked(currentGraph,true);
			
			propertyListener.propertyChanged(this, IEditorPart.PROP_DIRTY);
		}
	}
}
