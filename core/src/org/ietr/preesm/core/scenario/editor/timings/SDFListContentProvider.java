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
 
package org.ietr.preesm.core.scenario.editor.timings;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.ietr.preesm.core.scenario.Scenario;
import org.ietr.preesm.core.scenario.ScenarioParser;
import org.ietr.preesm.core.tools.NameComparator;
import org.sdf4j.model.sdf.SDFAbstractVertex;
import org.sdf4j.model.sdf.SDFGraph;

/**
 * Provides the elements contained in the timing editor
 * 
 * @author mpelcat
 */
public class SDFListContentProvider implements IStructuredContentProvider{
	
	//private Scenario scenario = null;
	
	private SDFGraph currentGraph = null;

	Object[] elementTable = null;
	
	@Override
	public Object[] getElements(Object inputElement) {


		if(inputElement instanceof Scenario){
			Scenario inputScenario = (Scenario)inputElement;
			
			// Opening algorithm from file
			currentGraph = ScenarioParser.getAlgorithm(inputScenario.getAlgorithmURL());
			
			// Displays the task names in alphabetical order
			if(currentGraph != null){
				ConcurrentSkipListSet<SDFAbstractVertex> vertices = new ConcurrentSkipListSet<SDFAbstractVertex>(new NameComparator());
				
				// Looks for the vertices in hierarchy
				findHierarchicalVertices(vertices, currentGraph);
				
				// Filters the results
				filterVertices(vertices);
				
				elementTable = vertices.toArray();
			}
		}
		return elementTable;
	}

	public void findHierarchicalVertices(Set<SDFAbstractVertex> vertices, SDFGraph graph) {

		for(SDFAbstractVertex vertex: graph.vertexSet()){
			vertices.add(vertex);
			
			if(vertex.getGraphDescription() != null){
				findHierarchicalVertices(vertices, (SDFGraph)vertex.getGraphDescription());
			}
		}
	}

	/**
	 * Depending on the kind of vertex, timings are edited or not
	 */
	public void filterVertices(Set<SDFAbstractVertex> vertices) {

		Iterator<SDFAbstractVertex> iterator = vertices.iterator();
		
		while(iterator.hasNext()){
			SDFAbstractVertex vertex = iterator.next();
			
			if(vertex.getKind() == "Broadcast"){
				iterator.remove();
			}
			else if(vertex.getKind() == "port"){
				iterator.remove();
			}
		}
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub
		
	}

}
