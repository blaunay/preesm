<?xml version="1.0" encoding="UTF-8"?>
<dftools:workflow xmlns:dftools="http://net.sf.dftools">
    <dftools:scenario pluginId="org.ietr.preesm.experiment.model.transformation.PiMMAndS-LAM"/>
    <dftools:task
        pluginId="org.ietr.preesm.experiment.model.transformation.taskExpression" taskId="PiMM2IBSDF">
        <dftools:data key="variables">
            <dftools:variable name="size" value="2"/>
        </dftools:data>
    </dftools:task>
    <dftools:task
        pluginId="org.ietr.preesm.experiment.pimm.cppgenerator.scala.PiMMCppGenerationTask" taskId="Export2C">
        <dftools:data key="variables"/>
    </dftools:task>
    <dftools:task
        pluginId="org.ietr.preesm.experiment.pimm.subgraph.connector.SubgraphConnectorTask" taskId="SubgraphConnector">
        <dftools:data key="variables"/>
    </dftools:task>
    <dftools:dataTransfer from="scenario" sourceport="PiMM"
        targetport="PiMM" to="PiMM2IBSDF"/>
    <dftools:dataTransfer from="scenario" sourceport="PiMM"
        targetport="PiMM" to="SubgraphConnector"/>
    <dftools:dataTransfer from="SubgraphConnector" sourceport="PiMM"
        targetport="PiMM" to="Export2C"/>
</dftools:workflow>
