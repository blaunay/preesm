package org.ietr.preesm.experiment.pimm.cppgenerator.scala.visitor

import org.ietr.preesm.experiment.model.pimm._
import java.util.HashMap
import java.util.Map
//Allows to consider Java collections as Scala collections and to use foreach...
import collection.JavaConversions._

/**
 * The C++ code generation needs a preprocess in order to get source/target nodes/actors for every Fifo and Dependency
 */
class CPPCodeGenerationPreProcessVisitor extends PiMMVisitor {

  //Variables containing the name of the currently visited AbstractActor for PortDescriptions
  private var currentAbstractActorName: String = ""
  //Map linking data ports to their corresponding description
  private val dataPortMap: Map[Port, DataPortDescription] = new HashMap[Port, DataPortDescription]
  def getDataPortMap(): Map[Port, DataPortDescription] = dataPortMap

  //Map linking data ports to their corresponding description
  private val cfgPortMap: Map[Port, String] = new HashMap[Port, String]

  //Map linking data ports to their corresponding description
  private val dependencyMap: Map[Dependency, DependencyDescription] = new HashMap[Dependency, DependencyDescription]
  def getDependencyMap(): Map[Dependency, DependencyDescription] = dependencyMap

  def visitPiGraph(pg: PiGraph): Unit = {
    visitAbstractActor(pg)
    pg.getActors().foreach(a => visit(a))
    pg.getDependencies().foreach(d => visit(d))
  }

  def visitActor(a: Actor): Unit = {
    visitAbstractActor(a)
  }

  def visitAbstractActor(aa: AbstractActor): Unit = {
    //Stock the name of the current AbstractActor
    currentAbstractActorName = aa.getName()
    //Visit data ports to fill the dataPortMap
    aa.getDataInputPorts().foreach(p => visit(p))
    aa.getDataOutputPorts().foreach(p => visit(p))
    //Visit configuration ports to fill the cfgPortMap
    aa.getConfigInputPorts().foreach(p => visit(p))
    aa.getConfigOutputPorts().foreach(p => visit(p))
  }

  /**
   * When visiting data ports, we stock the necessary informations for edge generation into PortDescriptions
   */
  def visitDataInputPort(dip: DataInputPort): Unit = {
    dataPortMap.put(dip, new DataPortDescription(currentAbstractActorName, dip.getExpression().getString()))
  }
  def visitDataOutputPort(dop: DataOutputPort): Unit = {
    dataPortMap.put(dop, new DataPortDescription(currentAbstractActorName, dop.getExpression().getString()))
  }

  def visitConfigInputPort(cip: ConfigInputPort): Unit = {
    cfgPortMap.put(cip, currentAbstractActorName)
  }

  def visitConfigOutputPort(cop: ConfigOutputPort): Unit = {
    cfgPortMap.put(cop, currentAbstractActorName)
  }

  def visitFifo(f: Fifo): Unit = {
    throw new UnsupportedOperationException()
  }

  def visitInterfaceActor(ia: InterfaceActor): Unit = {
    visitAbstractActor(ia)
  }

  def visitDataInputInterface(dii: DataInputInterface): Unit = {
    visitInterfaceActor(dii)
  }

  def visitDataOutputInterface(doi: DataOutputInterface): Unit = {
    visitInterfaceActor(doi)
  }

  def visitConfigOutputInterface(coi: ConfigOutputInterface): Unit = {
    visitInterfaceActor(coi)
  }

  def visitRefinement(r: Refinement): Unit = {
    throw new UnsupportedOperationException()
  }

  def visitParameter(p: Parameter): Unit = {
    throw new UnsupportedOperationException()
  }

  def visitDependency(d: Dependency): Unit = {
    val setter = d.getSetter()
    val getter = d.getGetter()

    val srcName = setter match {
      case p: Parameter => p.getName()
      case cop: ConfigOutputPort => cfgPortMap.get(cop)
    }
    val tgtName = cfgPortMap.get(getter)
    dependencyMap.put(d, new DependencyDescription(srcName, tgtName))
  }

  def visitDelay(d: Delay): Unit = {
    throw new UnsupportedOperationException()
  }

  def visitExpression(e: Expression): Unit = {
    throw new UnsupportedOperationException()
  }

  def visitConfigInputInterface(cii: ConfigInputInterface): Unit = {
    throw new UnsupportedOperationException()
  }
}

/**
 * Class allowing to stock necessary information about data ports for the edges generation
 */
class DataPortDescription(val nodeName: String, val expression: String)

/**
 * Class allowing to stock necessary information about dependencies for parameter connections
 */
class DependencyDescription(val srcName: String, val tgtName: String)