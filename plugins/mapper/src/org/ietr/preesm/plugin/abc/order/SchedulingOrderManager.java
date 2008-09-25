/**
 * 
 */
package org.ietr.preesm.plugin.abc.order;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ietr.preesm.core.architecture.ArchitectureComponent;
import org.ietr.preesm.plugin.mapper.model.ImplementationVertexProperty;
import org.ietr.preesm.plugin.mapper.model.MapperDAG;
import org.ietr.preesm.plugin.mapper.model.MapperDAGVertex;
import org.ietr.preesm.plugin.mapper.tools.TopologicalDAGIterator;

/**
 * The scheduling order manager keeps a total order of the vertices and
 * a partial order in each schedule. It is used by the schedule edge
 * adder to insert schedule edges
 *         
 * @author mpelcat
 */
public class SchedulingOrderManager {

	/**
	 * Contains the rank list of all the vertices in an implantation
	 */
	private Map<ArchitectureComponent, Schedule> schedules = null;

	/**
	 * total order of the vertices in the implantation
	 */
	private LinkedList<MapperDAGVertex> totalOrder = null;
	Schedule totalOrder2 = null;

	public SchedulingOrderManager() {

		schedules = new HashMap<ArchitectureComponent, Schedule>();
		totalOrder = new LinkedList<MapperDAGVertex>();
		totalOrder2 = new Schedule();
	}

	/**
	 * Considering that vertex already has a total order, inserts it at the
	 * appropriate position
	 */
	public void insertVertexInTotalOrder(MapperDAGVertex vertex) {

		AddScheduleIfNotPresent(vertex);

		ImplementationVertexProperty currImpProp = vertex
				.getImplementationVertexProperty();

		if (currImpProp.hasEffectiveComponent()) {
			// Retrieves the schedule corresponding to the vertex
			Schedule currentSched = getSchedule(currImpProp
					.getEffectiveComponent());

			// Iterates the schedule
			Iterator<MapperDAGVertex> it = currentSched.getVertices().iterator();
			int maxPrec = -1;

			while (it.hasNext()) {
				MapperDAGVertex current = it.next();

				// Adds the vertex after the first found vertex
				int currentTotalOrder = getSchedulingTotalOrder(current);

				if (currentTotalOrder >= 0
						&& currentTotalOrder < getSchedulingTotalOrder(vertex)
						&& currentTotalOrder > maxPrec)
					maxPrec = currentTotalOrder;
			}

			if (maxPrec >= 0) {
				MapperDAGVertex previous = totalOrder.get(maxPrec);
				currentSched.insertVertexAfter(previous, vertex);
			} else {
				currentSched.addVertexFirst(vertex);
			}

		}

	}

	/**
	 * Appends the vertex at the end of a schedule and at the end of total order
	 */
	public void addVertex(MapperDAGVertex vertex) {

		AddScheduleIfNotPresent(vertex);

		if (vertex.getImplementationVertexProperty().hasEffectiveComponent()) {
			ArchitectureComponent effectiveCmp = vertex
					.getImplementationVertexProperty().getEffectiveComponent();

			Schedule currentSchedule = getSchedule(effectiveCmp);

			currentSchedule.addVertex(vertex);

			if (totalOrder.contains(vertex))
				totalOrder.remove(vertex);

			totalOrder.addLast(vertex);

		}
	}

	/**
	 * Inserts vertex after previous
	 */
	public void insertVertexAfter(MapperDAGVertex previous,
			MapperDAGVertex vertex) {

		AddScheduleIfNotPresent(vertex);

		ImplementationVertexProperty prevImpProp = previous
				.getImplementationVertexProperty();
		ImplementationVertexProperty currImpProp = vertex
				.getImplementationVertexProperty();

		if (prevImpProp.hasEffectiveComponent()) {
			if (currImpProp.hasEffectiveComponent()) {

				if (!totalOrder.contains(vertex))
					if (totalOrder.indexOf(previous) >= 0) {
						if (totalOrder.indexOf(previous) + 1 < totalOrder
								.size())
							totalOrder.add(totalOrder.indexOf(previous) + 1,
									vertex);
						else
							totalOrder.addLast(vertex);
					}

				insertVertexInTotalOrder(vertex);

			}
		}

	}

	/**
	 * Gets the local scheduling order
	 */
	public int getSchedulingOrder(MapperDAGVertex vertex) {

		if (vertex.getImplementationVertexProperty().hasEffectiveComponent()) {

			Schedule sch = getSchedule(vertex.getImplementationVertexProperty()
					.getEffectiveComponent());
			if (sch != null)
				return sch.orderOf(vertex);
			else
				return -1;
		} else
			return -1;

	}

	/**
	 * Gets the total scheduling order
	 */
	public int getSchedulingTotalOrder(MapperDAGVertex vertex) {
		return totalOrder.indexOf(vertex);
	}

	/**
	 * Gets the scheduling components
	 */
	public Set<ArchitectureComponent> getComponents() {

		return schedules.keySet();

	}

	/**
	 * Gets the schedule of a given component
	 */
	public List<MapperDAGVertex> getScheduleList(ArchitectureComponent cmp) {

		return getSchedule(cmp).getVertices();

	}

	/**
	 * Gets the schedule of a given component
	 */
	public Schedule getSchedule(ArchitectureComponent cmp) {

		Iterator<ArchitectureComponent> it = schedules.keySet().iterator();

		while (it.hasNext()) {
			ArchitectureComponent currentCmp = it.next();
			if (currentCmp.equals(cmp))
				return schedules.get(currentCmp);
		}

		return null;
	}

	/**
	 * Removes a given vertex
	 */
	public void removeVertex(MapperDAGVertex vertex,
			boolean removeFromTotalOrder) {

		if (vertex.getImplementationVertexProperty().hasEffectiveComponent()) {
			ArchitectureComponent cmp = vertex.getImplementationVertexProperty()
					.getEffectiveComponent();
			Schedule sch = getSchedule(cmp);

			if (sch != null)
				sch.removeVertex(vertex);
		} else {
			Iterator<Schedule> it = schedules.values().iterator();

			while (it.hasNext()) {
				it.next().removeVertex(vertex);
			}
		}

		if (removeFromTotalOrder) {
			totalOrder.remove(vertex);
		}
	}

	/**
	 * Resets Total Order
	 */
	public void resetTotalOrder() {
		totalOrder.clear();
	}

	/**
	 * Adds the schedule corresponding to the vertex effective component if not
	 * present
	 */
	public void AddScheduleIfNotPresent(MapperDAGVertex vertex) {

		ImplementationVertexProperty currImpProp = vertex
				.getImplementationVertexProperty();

		// Gets the component corresponding to the vertex
		if (currImpProp.hasEffectiveComponent()) {
			ArchitectureComponent effectiveCmp = currImpProp
					.getEffectiveComponent();

			// If no schedule exists for this component, 
			// adds a schedule for it
			if (getSchedule(effectiveCmp) == null)
				schedules.put(effectiveCmp, new Schedule());
		}

	}

	/**
	 * Reconstructs the total order using the total order stored in DAG
	 */
	public void reconstructTotalOrderFromDAG(MapperDAG dag,
			MapperDAG implantation) {

		resetTotalOrder();

		int currentOrder = 0;
		int numberOfRemainingVertices = dag.vertexSet().size();

		while (true) {
			TopologicalDAGIterator it = new TopologicalDAGIterator(dag);

			while (it.hasNext()) {
				MapperDAGVertex v = (MapperDAGVertex)it.next();

				if (v.getImplementationVertexProperty().getSchedulingTotalOrder() == currentOrder) {

					MapperDAGVertex internalVertex = implantation.getMapperDAGVertex(v
							.getName());

					addVertex(internalVertex);
					numberOfRemainingVertices -= 1;
					break;
				}
			}

			if (numberOfRemainingVertices == 0)
				break;

			currentOrder++;
		}

	}

	/**
	 * Returns the vertex of the given schedule that immediately precedes vertex
	 */
	public MapperDAGVertex previousInTotalOrder(Schedule currentSched,
			MapperDAGVertex vertex) {
		MapperDAGVertex previous = null;

		while (previous != null && !currentSched.hasVertex(previous)) {
			previous = currentSched.getPreviousVertex(previous);
		}

		return previous;
	}

	/**
	 * Sets the total order of each implantation property in DAG
	 */
	public void tagDAG(MapperDAG dag) {
		Iterator<MapperDAGVertex> iterator = totalOrder.iterator();

		while (iterator.hasNext()) {
			MapperDAGVertex internalVertex = iterator.next();
			MapperDAGVertex vertex = dag.getMapperDAGVertex(internalVertex.getName());

			if (vertex != null) {
				tagVertex(vertex);
			}
		}
	}

	/**
	 * Sets the total order of vertex implantation property in DAG
	 */
	public void tagVertex(MapperDAGVertex vertex) {
		
		vertex.getImplementationVertexProperty().setSchedulingTotalOrder(totalOrder.indexOf(vertex));
	}
}
