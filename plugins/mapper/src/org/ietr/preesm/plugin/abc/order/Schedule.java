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

package org.ietr.preesm.plugin.abc.order;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.ietr.preesm.core.architecture.ArchitectureComponent;
import org.ietr.preesm.plugin.mapper.model.MapperDAGVertex;
import org.sdf4j.model.dag.DAGVertex;

/**
 * A schedule represents the consecutive tasks mapped on a single
 * {@link ArchitectureComponent}
 * 
 * @author mpelcat
 */
public class Schedule {

	private static final long serialVersionUID = 1L;

	/**
	 * The ordered list of vertices in this schedule
	 */
	private LinkedList<MapperDAGVertex> vervexList;
	
	/**
	 * The total time of the schedule vertices
	 */
	private long busyTime;

	public Schedule() {

		super();
		this.vervexList = new LinkedList<MapperDAGVertex>();
		resetBusyTime();
	}

	/**
	 * Appends a vertex at the end of the schedule
	 */
	public void addVertexLast(MapperDAGVertex vertex) {
		if(vertex.getTimingVertexProperty().getCost() >= 0){
			busyTime += vertex.getTimingVertexProperty().getCost();}
		else{
			//PreesmLogger.getLogger().log(Level.SEVERE,"problem in schedule busy time calculation 1.");
			}
		
		if (!vervexList.contains(vertex))
			vervexList.addLast(vertex);
	}

	/**
	 * Inserts a vertex at the beginning of the schedule
	 */
	public void addVertexFirst(MapperDAGVertex vertex) {
		if(vertex.getTimingVertexProperty().getCost() >= 0){
			busyTime += vertex.getTimingVertexProperty().getCost();}
		else{
			//PreesmLogger.getLogger().log(Level.SEVERE,"problem in schedule busy time calculation 2.");
		}
		
		if (!vervexList.contains(vertex))
			vervexList.addFirst(vertex);
	}

	/**
	 * Inserts a vertex after the given one
	 */
	public void insertVertexAfter(MapperDAGVertex previous,
			MapperDAGVertex vertex) {
		if(vertex.getTimingVertexProperty().getCost() >= 0){
			busyTime += vertex.getTimingVertexProperty().getCost();}
		else{
			//PreesmLogger.getLogger().log(Level.SEVERE,"problem in schedule busy time calculation 3.");
		}

		if (!vervexList.contains(vertex))
			if (vervexList.indexOf(previous) >= 0) {
				if (vervexList.indexOf(previous) + 1 < vervexList.size()) {
					MapperDAGVertex next = vervexList.get(vervexList.indexOf(previous) + 1);
					if(!areSynchronized(previous,next)){
						vervexList.add(vervexList.indexOf(next), vertex);
					}
					else{
						insertVertexAfter(next,vertex);
					}
				} else{
					vervexList.addLast(vertex);
				}
			}
	}

	/**
	 * Inserts a vertex before the given one
	 */
	public void insertVertexBefore(MapperDAGVertex next, MapperDAGVertex vertex) {
		if(vertex.getTimingVertexProperty().getCost() >= 0){
			busyTime += vertex.getTimingVertexProperty().getCost();}
		else{
			//PreesmLogger.getLogger().log(Level.SEVERE,"problem in schedule busy time calculation 4.");
		}

		if (!vervexList.contains(vertex))
			if (vervexList.indexOf(next) >= 0) {
				MapperDAGVertex previous = vervexList.get(vervexList.indexOf(next) - 1);
				if(!areSynchronized(previous,next)){
					vervexList.add(vervexList.indexOf(next), vertex);
				}
				else{
					insertVertexAfter(next,vertex);
				}
			}
	}
	
	public void clear(){
		resetBusyTime();
		
		vervexList.clear();
	}
	
	public void resetBusyTime(){
		busyTime = 0;
	}
	
	public void remove(MapperDAGVertex vertex){
		if(vervexList.contains(vertex)){
			if(vertex.getTimingVertexProperty().getCost() >= 0){
				busyTime -= vertex.getTimingVertexProperty().getCost();}
			else{
				//PreesmLogger.getLogger().log(Level.SEVERE,"problem in schedule busy time calculation 5.");
			}
		}
		
		vervexList.remove(vertex);
	}
	
	// Access without modification

	/**
	 * Returns true if two vertices are synchronized; i.e. they must have the same starting time
	 */
	private boolean areSynchronized(MapperDAGVertex previous, MapperDAGVertex next){
		boolean areSynchronized = false;
		List<MapperDAGVertex> synchros = previous.getTimingVertexProperty().getSynchronizedVertices();
		
		if(synchros != null && synchros.contains(next)){
			areSynchronized = true;
		}
		
		return areSynchronized;
	}
	
	public MapperDAGVertex get(int i){
		return vervexList.get(i);
	}
	
	public MapperDAGVertex getLast(){
		return vervexList.getLast();
	}
	/**
	 * Gets the previous vertex in the current schedule
	 */
	public MapperDAGVertex getPreviousVertex(MapperDAGVertex vertex) {
		if (vervexList.indexOf(vertex) <= 0)
			return null;
		return (vervexList.get(vervexList.indexOf(vertex) - 1));
	}

	/**
	 * Gets the next vertex in the current schedule
	 */
	public MapperDAGVertex getNextVertex(MapperDAGVertex vertex) {
		int currentIndex = vervexList.indexOf(vertex);
		if (currentIndex < 0 || vervexList.indexOf(vertex) >= vervexList.size() - 1)
			return null;
		return (vervexList.get(currentIndex + 1));
	}

	/**
	 * Gets the next vertex in the current schedule
	 */
	public Set<DAGVertex> getSuccessors(MapperDAGVertex vertex) {
		int currentIndex = vervexList.indexOf(vertex);
		if (currentIndex < 0 || vervexList.indexOf(vertex) >= vervexList.size())
			return null;
		
		Set<DAGVertex> vSet = new HashSet<DAGVertex>();
		for(int i = currentIndex+1;i<vervexList.size();i++){
			vSet.add(vervexList.get(i));
		}
		return vSet;
	}
	
	public int indexOf(MapperDAGVertex v){
		return vervexList.indexOf(v);
	}
	
	public boolean contains(MapperDAGVertex v){
		return vervexList.contains(v);
	}
	
	public boolean isEmpty(){
		return vervexList.isEmpty();
	}

	public LinkedList<MapperDAGVertex> getVervexList() {
		return vervexList;
	}

	@Override
	public String toString() {
		return "{" + super.toString() + "}";
	}

	public List<String> toStringList() {

		List<String> order = new ArrayList<String>();

		for (MapperDAGVertex v : vervexList) {
			order.add(v.getName());
		}

		return order;
	}

	public long getBusyTime() {
		return busyTime;
	}

	public int size() {
		return vervexList.size();
	}	
}
