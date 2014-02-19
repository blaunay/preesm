/*******************************************************************************
 * Copyright or © or Copr. IETR/INSA: Maxime Pelcat, Jean-François Nezan,
 * Karol Desnos, Julien Heulot, Clément Guy, Yaset Oliva Venegas
 * 
 * [mpelcat,jnezan,kdesnos,jheulot,cguy,yoliva]@insa-rennes.fr
 * 
 * This software is a computer program whose purpose is to prototype
 * parallel applications.
 * 
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info". 
 * 
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability. 
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or 
 * data to be ensured and,  more generally, to use and operate it in the 
 * same conditions as regards security. 
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 ******************************************************************************/
package org.ietr.preesm.experiment.pimm.cppgenerator.scala.visitor

import org.eclipse.emf.ecore.EObject
import org.ietr.preesm.experiment.model.pimm._
import scala.collection.immutable.Stack
import java.util.Map
import java.util.HashMap
//Allows to consider Java collections as Scala collections and to use foreach...
import collection.JavaConversions._

/**
 * PiMM models visitor generating C++ code for COMPA Runtime
 * currentGraph: The most outer graph of the PiMM model
 * currentMethod: The StringBuilder used to write the C++ code
 */
class CPPCodeGenerationVisitor(private var currentGraph: PiGraph, private var currentMethod: StringBuilder) extends PiMMVisitor{

  //Stack and list to handle hierarchical graphs
  private val graphsStack: Stack[GraphDescription] = new Stack[GraphDescription]
  private var currentSubGraphs: List[PiGraph] = Nil
  //Variables containing the name and type of the currently visited AbstractActor for AbstractActor generation and PortDescriptions
  private var currentAbstractActorName: String = ""
  private var currentAbstractActorType: String = ""
  //Map linking ports to their correspondent description
  private val portMap: Map[Port, PortDescription] = new HashMap[Port, PortDescription]
  //Map linking Fifos to their C++ names
  private val fifoMap: Map[Fifo, String] = new HashMap[Fifo, String]
  //Shortcut for currentMethod.append()
  private def append(s: String) = currentMethod.append(s)
  
  
  /**
   * When visiting a PiGraph (either the most outer graph or an hierarchical actor),
   * we should generate a new C++ method
   */
  def visitPiGraph(pg: PiGraph): Unit = {
    //Add pg to the list of subgraphs of the current graph, except in the case pg is the current graph
    if (pg != currentGraph) pg :: currentSubGraphs
    //Stock the container graph and setpg pg as the new current graph
    push(pg)

    val pgName = pg.getName()

    append("// Method building PiGraph ")
    append(pgName)
    //Generating the method signature
    generateMethodSignature(pg)
    //Generating the method body
    generateMethodBody(pg)
    //Reset the container graph as the current graph
    pop()

    //We should also generate the C++ code as for any Actor
    currentAbstractActorType = "pisdf_vertex"
    visitAbstractActor(pg)
  }

  /**
   * Returns the name of the building method for the PiGraph pg
   */
  private def getMethodName(pg: PiGraph): String = "build" + pg.getName()

  /**
   * Concatenate the signature of the method corresponding to a PiGraph to the currentMethod StringBuilder
   */
  private def generateMethodSignature(pg: PiGraph): Unit = {
    //The method does not return anything
    append("void ")
    append(getMethodName(pg))
    //The method accept as parameter a pointer to the PiSDFGraph graph it will build and a pointer to the parent actor of graph (i.e., the hierarchical actor)
    append("(PiSDFGraph* graph, BaseVertex* parentVertex)")
  }

  /**
   * Concatenate the body of the method corresponding to a PiGraph to the currentMethod StringBuilder
   */
  private def generateMethodBody(pg: PiGraph): Unit = {
    append("{")
    //Generating parameters
    append("\n\t//Parameters")
    pg.getParameters().foreach(p => visit(p))
    //Generating vertices
    append("\n\t//Vertices")
    pg.getVertices().foreach(v => visit(v))
    //Generating edges
    append("\n\t//Edges")
    pg.getFifos().foreach(f => visit(f))
    //Generating call to methods generated for subgraphs, if any
    if (!currentSubGraphs.isEmpty) {
      append("\n\t//Subgraphs")
      generateCallsToSubgraphs()
    }
    append("}\n")
    visitAbstractActor(pg)
  }

  private def generateCallsToSubgraphs(): Unit = {
    //For each subgraph of the current graph
    currentSubGraphs.foreach(sg => {
      val sgName = getSubraphName(sg)
      val vxName = getVertexName(sg)
      //Generate test in order to prevent to reach the limit of graphs
      append("\n\tif(nb_graphs >= MAX_NB_PiSDF_SUB_GRAPHS - 1) exitWithCode(1054);")
      //Get the pointer to the subgraph
      append("\n\tPiSDFGraph *")
      append(sgName)
      append(" = &graphs[nb_graphs];")
      //Increment the graph counter
      append("\n\tnb_graphs++;")
      //Call the building method of sg with the pointer
      append("\n\t")
      append(getMethodName(sg))
      append("(")
      //Pass the pointer to the subgraph
      append(sgName)
      append(",")
      //Pass the parent vertex
      append(vxName)
      append(");")
      append("\n\t")
      //Set the subgraph as subgraph of the vertex
      append(vxName)
      append("->setSubGraph(")
      append(sgName)
      append(");")
    })
  }

  /**
   * Returns the name of the subgraph pg
   */
  private def getSubraphName(pg: PiGraph): String = pg.getName() + "_subGraph"

  /**
   * Returns the name of the variable pointing to the C++ object corresponding to AbstractActor aa
   */
  private def getVertexName(aa: AbstractActor): String = "vx" + aa.getName()

  def visitActor(a: Actor): Unit = {
    visitAbstractActor(a)
  }

  /**
   * Generic visit method for all AbstractActors (Actors, PiGraph)
   */
  def visitAbstractActor(aa: AbstractActor): Unit = {
    //Stock the name of the current AbstractActor
    currentAbstractActorName = aa.getName()

    //Call the addVertex method on the current graph
    append("\n\tPiSDFVertex *")
    append(getVertexName(aa))

    //Call the addVertex method on the current graph
    append("\n\tPiSDFVertex *")
    append(getVertexName(aa))
    append(" = (PiSDFVertex*)graph->addVertex(\"")
    //Pass the name of the AbstractActor
    append(currentAbstractActorName)
    append("\",")
    //Pass the type of vertex
    append(currentAbstractActorType)
    append(");")

    //Visit data ports to fill the portMap
    aa.getDataInputPorts().foreach(p => visit(p))
    aa.getDataOutputPorts().foreach(p => visit(p))
    //TODO: Keep the visit of ConfigPorts?
    aa.getConfigInputPorts().foreach(p => visit(p))
    aa.getConfigOutputPorts().foreach(p => visit(p))
  }

  /**
   * When visiting data ports, we stock the necessary informations for edge generation into PortDescriptions
   */
  def visitDataInputPort(dip: DataInputPort): Unit = {
    portMap.put(dip, new PortDescription(currentAbstractActorName, dip.getExpression().getString()))
  }
  def visitDataOutputPort(dop: DataOutputPort): Unit = {
    portMap.put(dop, new PortDescription(currentAbstractActorName, dop.getExpression().getString()))
  }

  def visitConfigInputPort(cip: ConfigInputPort): Unit = {
    //TODO
  }

  def visitConfigOutputPort(cop: ConfigOutputPort): Unit = {
    //TODO
  }

  /**
   * When visiting a FIFO we should add an edge to the current graph
   */
  def visitFifo(f: Fifo): Unit = {
    val edgeName = generateEdgeName()
    fifoMap.put(f, edgeName)
    //Call the addEdge method on the current graph
    append("\n\t")
    append(edgeName)
    append("graph->addEdge(")
    //Use the PortDescription of the source port to get the informations about the source node
    val src: PortDescription = portMap.get(f.getSourcePort())
    //Pass the name of the source node
    append(src.nodeName)
    append(", \"")
    //Pass the production of the source node
    append(src.expression)
    append("\", ")
    //Use the PortDescription of the target port to get the informations about the target node
    val tgt: PortDescription = portMap.get(f.getTargetPort())
    //Pass the name of the target node
    append(tgt.nodeName)
    append(", \"")
    //Pass the consumption of the target node
    append(tgt.expression)
    append("\", ")
    //Pass the delay of the FIFO
    append(f.getDelay().getExpression().getString())
    append("\"")
    append(");")
  }
  
  private var edgeCounter: Integer = -1
  private def generateEdgeName(): String = {
    edgeCounter = edgeCounter+1
    "edge" + edgeCounter
  }

  def visitInterfaceActor(ia: InterfaceActor): Unit = {
    //TODO
  }

  def visitDataInputInterface(dii: DataInputInterface): Unit = {
    val vertexName = getVertexName(dii)
    //Adding the vertex to the current graph
    currentAbstractActorType = "input_vertex"
    visitAbstractActor(dii)
    //Setting direction to 0 (input)
    append("\t\n")
    append(vertexName)
    append("->setDirection(0);")
    //Setting the parent vertex
    append("\t\n")
    append(vertexName)
    append("->setParentVertex(parentVertex);")
    //Setting the parent edge
    append("\t\n")
    append(vertexName)
    append("->setParentEdge(")
    //Getting the Fifo corresponding to the parent edge
    val incomingFifo = dii.getDataInputPorts().get(0).getIncomingFifo()
    append(fifoMap.get(incomingFifo))
    append(");")    
  }

  def visitDataOutputInterface(doi: DataOutputInterface): Unit = {
    val vertexName = getVertexName(doi)
    //Adding the vertex to the current graph
    currentAbstractActorType = "output_vertex"
    visitAbstractActor(doi)
    //Setting direction to 1 (output)
    append("\t\n")
    append(vertexName)
    append("->setDirection(1);")
    //Setting the parent vertex
    append("\t\n")
    append(vertexName)
    append("->setParentVertex(parentVertex);")
    //Setting the parent edge
    append("\t\n")
    append(vertexName)
    append("->setParentEdge(")
    //Getting the Fifo corresponding to the parent edge
    val incomingFifo = doi.getDataOutputPorts().get(0).getOutgoingFifo()
    append(fifoMap.get(incomingFifo))
    append(");")    
  }

  def visitConfigOutputInterface(coi: ConfigOutputInterface): Unit = {
    //TODO
  }

  def visitRefinement(r: Refinement): Unit = {
    //TODO
  }

  /**
   * When visiting a parameter, we should add a parameter to the current graph
   */
  def visitParameter(p: Parameter): Unit = {
    append("\n\tPISDFParameter *")
    append(getParameterName(p))
    append(" = graph->addParameter(\"")
    append(p.getName())
    append("\");")
  }
  
  private def getParameterName(p: Parameter): String = "param_" + p.getName();

  def visitDependency(d: Dependency): Unit = {
    //TODO
  }

  def visitDelay(d: Delay): Unit = {
    //TODO
  }

  def visitExpression(e: Expression): Unit = {
    //TODO
  }

  def visitConfigInputInterface(cii: ConfigInputInterface): Unit = {
    //TODO
  }

  /**
   * Handling the graphs and methods stacks
   */
  private def push(pg: PiGraph): Unit = {
    //Test in order to ignore the most outer graph
    if (currentGraph != pg) {
      graphsStack.push(new GraphDescription(currentGraph, currentSubGraphs, currentMethod))
      currentGraph = pg
      currentSubGraphs = Nil
      currentMethod = new StringBuilder
    }
  }
  private def pop(): Unit = {
    val top = graphsStack.top
    //Test in order to ignore the most outer graph
    if (top != null) {
      currentGraph = top.pg
      currentSubGraphs = top.subGraphs
      currentMethod = top.method
      graphsStack.pop
    }
  }
}

/**
 * Class allowing to stock necessary information about graphs
 */
class GraphDescription(val pg: PiGraph, var subGraphs: List[PiGraph], var method: StringBuilder)

/**
 * Class allowing to stock necessary information about ports for the edges generation
 */
class PortDescription(val nodeName: String, val expression: String)