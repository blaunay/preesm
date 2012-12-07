/**
 */
package org.ietr.preesm.experiment.model.pimm;

import java.util.Set;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc --> A representation of the model object '
 * <em><b>Graph</b></em>'. <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.ietr.preesm.experiment.model.pimm.Graph#getVertices <em>Vertices</em>}</li>
 *   <li>{@link org.ietr.preesm.experiment.model.pimm.Graph#getFifos <em>Fifos</em>}</li>
 *   <li>{@link org.ietr.preesm.experiment.model.pimm.Graph#getParameters <em>Parameters</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.ietr.preesm.experiment.model.pimm.PiMMPackage#getGraph()
 * @model
 * @generated
 */
public interface Graph extends AbstractVertex {
	/**
	 * Returns the value of the '<em><b>Vertices</b></em>' containment reference
	 * list. The list contents are of type
	 * {@link org.ietr.preesm.experiment.model.pimm.AbstractVertex}. <!--
	 * begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Vertices</em>' containment reference list
	 * isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Vertices</em>' containment reference list.
	 * @see org.ietr.preesm.experiment.model.pimm.PiMMPackage#getGraph_Vertices()
	 * @model containment="true"
	 * @generated
	 */
	EList<AbstractVertex> getVertices();

	/**
	 * Returns the value of the '<em><b>Fifos</b></em>' containment reference
	 * list. The list contents are of type
	 * {@link org.ietr.preesm.experiment.model.pimm.Fifo}. <!--
	 * begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Fifos</em>' containment reference list isn't
	 * clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Fifos</em>' containment reference list.
	 * @see org.ietr.preesm.experiment.model.pimm.PiMMPackage#getGraph_Fifos()
	 * @model containment="true"
	 * @generated
	 */
	EList<Fifo> getFifos();

	/**
	 * Returns the value of the '<em><b>Parameters</b></em>' containment reference list.
	 * The list contents are of type {@link org.ietr.preesm.experiment.model.pimm.Parameter}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Parameters</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Parameters</em>' containment reference list.
	 * @see org.ietr.preesm.experiment.model.pimm.PiMMPackage#getGraph_Parameters()
	 * @model containment="true"
	 * @generated
	 */
	EList<Parameter> getParameters();

	/**
	 * <!-- begin-user-doc --> This method will add the {@link InterfaceVertex}
	 * and create the corresponding {@link Port}. <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	boolean addInterfaceVertex(InterfaceVertex interfaceVertex);

	/**
	 * <!-- begin-user-doc --> This method will remove the
	 * {@link InterfaceVertex} and the corresponding {@link Port} from the
	 * {@link Graph}. <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	boolean removeInterfaceVertex(InterfaceVertex interfaceVertex);

	/**
	 * Return the list of the names of all vertices of the Graph.
	 * 
	 * @return the list of names
	 */
	public Set<String> getVerticesNames();

	/**
	 * Return the vertex whose name is given as a parameter.
	 * 
	 * @param name
	 *            the desired vertex, or <code>null</code> if no vertex has the
	 *            requested name.
	 * @return
	 */
	public AbstractVertex getVertexNamed(String name);
} // Graph
