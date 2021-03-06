<?xml version="1.0" encoding="UTF-8"?>
<dftools:workflow xmlns:dftools="http://net.sf.dftools">
    <dftools:scenario pluginId="org.ietr.preesm.scenario.task"/>
    <dftools:task
        pluginId="org.ietr.preesm.experiment.pimm2sdf.PiMM2SDFTask" taskId="PiMM2SDF">
        <dftools:data key="variables"/>
    </dftools:task>
    <dftools:task
        pluginId="org.ietr.preesm.algorithm.exportXml.MultiSDFExporter" taskId="MultiSDFExporter">
        <dftools:data key="variables">
            <dftools:variable name="path" value="export/"/>
        </dftools:data>
    </dftools:task>
    <dftools:task
        pluginId="org.ietr.preesm.algorithm.transforms.MultiHierarchyFlattening" taskId="MultiSDFHierarchyFlattening">
        <dftools:data key="variables">
            <dftools:variable name="depth" value="3"/>
        </dftools:data>
    </dftools:task>
    <dftools:task
        pluginId="org.ietr.preesm.algorithm.transforms.MultiHSDFTransformation" taskId="MultiHSDFTransformation">
        <dftools:data key="variables">
            <dftools:variable name="ExplodeImplodeSuppr" value="false"/>
        </dftools:data>
    </dftools:task>
    <dftools:task
        pluginId="org.ietr.preesm.algorithm.exportXml.MultiSDFExporter" taskId="MultiSDFExporter2">
        <dftools:data key="variables">
            <dftools:variable name="path" value="HSDF/"/>
        </dftools:data>
    </dftools:task>
    <dftools:task
        pluginId="org.ietr.preesm.experiment.pimm.mapper.MultiSDFListSchedulingMapping" taskId="MultiListScheduling">
        <dftools:data key="variables">
            <dftools:variable name="balanceLoads" value="false"/>
            <dftools:variable name="edgeSchedType" value="Simple"/>
            <dftools:variable name="simulatorType" value="LooselyTimed"/>
        </dftools:data>
    </dftools:task>
    <dftools:task
        pluginId="org.ietr.preesm.experiment.pimm.mapper.MultiStatEditorTransform" taskId="MultiGantt">
        <dftools:data key="variables"/>
    </dftools:task>
    <dftools:task
        pluginId="org.ietr.preesm.experiment.pimm.mapper.MultiDAGExportTransform" taskId="MultiDAGExport">
        <dftools:data key="variables">
            <dftools:variable name="path" value="DAG/dag"/>
        </dftools:data>
    </dftools:task>
    <dftools:task
        pluginId="org.ietr.preesm.experiment.memory.MultiMemoryExclusionGraphBuilder" taskId="MultiMemExBuilder">
        <dftools:data key="variables">
            <dftools:variable name="Suppr Fork/Join" value="? C {True, False}"/>
            <dftools:variable name="Verbose" value="? C {True, False}"/>
        </dftools:data>
    </dftools:task>
    <dftools:task
        pluginId="org.ietr.preesm.experiment.memory.MultiMemoryAllocator" taskId="MultiMemAlloc">
        <dftools:data key="variables">
            <dftools:variable name="Allocator(s)" value="Basic"/>
            <dftools:variable name="Best/First Fit order" value="LargestFirst"/>
            <dftools:variable name="Data alignment" value="Data"/>
            <dftools:variable name="Merge broadcasts" value="? C {True, False}"/>
            <dftools:variable name="Nb of Shuffling Tested" value="10"/>
            <dftools:variable name="Verbose" value="True"/>
        </dftools:data>
    </dftools:task>
    <dftools:task
        pluginId="org.ietr.preesm.experiment.codegen.xtend.MultiCodegenTask" taskId="MultiCodegen">
        <dftools:data key="variables">
            <dftools:variable name="Printer" value="C"/>
        </dftools:data>
    </dftools:task>
    <dftools:dataTransfer from="scenario" sourceport="scenario"
        targetport="scenario" to="PiMM2SDF"/>
    <dftools:dataTransfer from="PiMM2SDF" sourceport="SDFs"
        targetport="SDFs" to="MultiSDFExporter"/>
    <dftools:dataTransfer from="PiMM2SDF" sourceport="SDFs"
        targetport="SDFs" to="MultiSDFHierarchyFlattening"/>
    <dftools:dataTransfer from="MultiSDFHierarchyFlattening"
        sourceport="SDFs" targetport="SDFs" to="MultiHSDFTransformation"/>
    <dftools:dataTransfer from="MultiHSDFTransformation"
        sourceport="SDFs" targetport="SDFs" to="MultiSDFExporter2"/>
    <dftools:dataTransfer from="scenario" sourceport="PiMM"
        targetport="PiMM" to="PiMM2SDF"/>
    <dftools:dataTransfer from="scenario" sourceport="scenario"
        targetport="scenario" to="MultiListScheduling"/>
    <dftools:dataTransfer from="scenario" sourceport="architecture"
        targetport="architecture" to="MultiListScheduling"/>
    <dftools:dataTransfer from="MultiHSDFTransformation"
        sourceport="SDFs" targetport="SDFs" to="MultiListScheduling"/>
    <dftools:dataTransfer from="MultiListScheduling" sourceport="ABCs"
        targetport="ABCs" to="MultiGantt"/>
    <dftools:dataTransfer from="scenario" sourceport="scenario"
        targetport="scenario" to="MultiGantt"/>
    <dftools:dataTransfer from="MultiListScheduling" sourceport="DAGs"
        targetport="DAGs" to="MultiDAGExport"/>
    <dftools:dataTransfer from="MultiListScheduling" sourceport="DAGs"
        targetport="DAGs" to="MultiMemExBuilder"/>
    <dftools:dataTransfer from="scenario" sourceport="scenario"
        targetport="scenario" to="MultiMemExBuilder"/>
    <dftools:dataTransfer from="MultiMemExBuilder"
        sourceport="DAGsAndMemExs" targetport="DAGsAndMemExs" to="MultiMemAlloc"/>
    <dftools:dataTransfer from="MultiMemAlloc"
        sourceport="DAGsAndMemExs" targetport="DAGsAndMemExs" to="MultiCodegen"/>
    <dftools:dataTransfer from="scenario" sourceport="scenario"
        targetport="scenario" to="MultiCodegen"/>
    <dftools:dataTransfer from="scenario" sourceport="architecture"
        targetport="architecture" to="MultiCodegen"/>
</dftools:workflow>
