/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilitytypes;

import baseclasses.PipelineRegister;
import java.util.HashMap;
import java.util.Map;

/**
 * This defines the common functionality among design units in a hierarchy
 * of modules that comprise a modular processor design.  Modules are typically
 * multiple pipeline stages, and they can have can have their own local set
 * of properties.  This is to be implemented by things like functional units
 * and whole processor cores.
 * 
 * @author millerti
 */
public interface IModule extends IComponent {
    /**
     * @return Reference to the LOCAL PropertiesContainer for this module
     */
    IProperties getProperties();
    
    /**
     * Traverses up the hierarchy to get the properties list held by the
     * main CPU core.
     * 
     * This can be tricked into stopping earlier by making a sub-module
     * implement its properties as an instance of a class that implements
     * IGlobals.
     * 
     * @return Reference to the IGlobals instance at the top of the hierarchy.
     */
    IGlobals getGlobals();

    /**
     * Create the PropertiesContainer and pre-fill it with any properties
     * that contained pipeline elements rely on.
     */
    default void initProperties() {};
    
    /**
     * Reset properties to initial state
     */
    default void resetProperties() {}
            
    
    /**
     * Create all pipeline registers.  Convenience methods named createPipeReg
     * will create a pipeline register and automatically add to the map
     * of pipeline registers.
     * 
     * If this is for a sub-module, be sure to create clearly defined module
     * input and output pipeline registers.  Input register names are expected
     * to be "in" or start with "in:", and output register names are expected 
     * to be "out" or start with "out:".
     */
    void createPipelineRegisters();

    /**
     * Create instances of all of your local pipeline stages.  Be sure the
     * stage has been given a name and use addPipeStage to insert it into
     * the map of pipeline stages.
     */
    void createPipelineStages();

    /**
     * Create instances of all sub-modules, which are direct children of this
     * module.  Be sure the module has a name and use addChildUnit to insert
     * it into the map of sub-modules.
     * 
     * Child modules must implement IFunctionalUnit and therefore export
     * input and output pipeline registers via that interface.
     */
    void createChildModules();

    /**
     * Use connect() methods to create links between pipeline stages and
     * pipeline registers.
     */
    void createConnections();
    
    /**
     * Use addForwardingSource(pipereg_name) to specify all pipeline registers
     * that could contain result values that could be forwarded to the inputs
     * of other pipeline stages.
     */
    void specifyForwardingSources();
    
    /**
     * Use addForwardingTarget(pipereg_name) to specify all pipeline registers
     * whose successor pipeline stages may want to receive forwarding data.
     * 
     * At the moment, this isn't actually used for anything.  Instead, 
     * forwarding targets need to call doPostedForwarding and/or 
     * forwardingSearch.
     * 
     * By convention a pipeline latch that requests automatic forwarding will
     * contain one or more of the following properties:
     * forward0 -- hierarchical name of forwarding source register to get data from for oper0
     * forward1 -- hierarchical name of forwarding source register to get data from for src1
     * forward2 -- hierarchical name of forwarding source register to get data from for src2
     */
    default void specifyForwardingTargets() {}
    
    /**
     * Construct and connect internal components.
     */
    void initModule();
    
    
    
    /**
     * @return Map of all contained pipeline stages, not including those
     * in sub-modules.
     */
    public Map<String,IPipeStage> getLocalPipeStages();
    
    /**
     * @return Map of all stages in this module and all submodules, with hierarchical names.
     */
    Map<String,IPipeStage> getPipeStagesRecursive();

    /**
     * @return Map of all contained pipeline registers, not including those in
     * sub-modules.
     */
    public Map<String,IPipeReg> getLocalPipeRegs();

    /**
     * @return Map of all pipeline registers in this module and all submodules, with hierarchical names.
     */
    Map<String,IPipeReg> getPipeRegsRecursive();
    
    
    /**
     * @return Map of all contained functional units, not including those in
     * sub-modules.
     */
    public Map<String,IFunctionalUnit> getLocalChildUnits();
    
    /**
     * Get pipeline stage from this module, excluding children, by local name.
     * @param name
     * @return pipeline stage
     */
    public IPipeStage getLocalPipeStage(String name);

    /**
     * Get pipeline stage by name.  The name can be hierarchical but should
     * not start with name of this module.  Instead, it should be a name
     * that is relative to this module, either local or prefixed with the
     * name of a child, delimited by a dot.
     * 
     * @param name
     * @return pipeline stage
     */
    public IPipeStage getPipeStage(String name);
    
    /**
     * Get pipeline register from this module, excluding children, by local name.
     * @param name
     * @return pipeline register
     */
    public IPipeReg getLocalPipeReg(String name);

    /**
     * Get pipeline register by name.  The name can be hierarchical but should
     * not start with name of this module.  Instead, it should be a name
     * that is relative to this module, either local or prefixed with the
     * name of a child, delimited by a dot.
     * 
     * @param name
     * @return pipeline registe
     */
    public IPipeReg getPipeReg(String name);

    
    /**
     * Get functional unit (child module) by local name.
     * @param name
     * @return functional unit
     */
    public IFunctionalUnit getChildUnit(String name);

    /**
     * Add a new pipeline stage to the set of pipeline stages.  The order
     * in which addPipeStage is called does not affect the order in which
     * they connect or are evaluated.  
     * 
     * The new pipeline stage must have been assigned a name.
     * 
     * @param stage New pipeline stage with name.
     */
    public void addPipeStage(IPipeStage stage);
    
    /**
     * Add a new pipeline register to the set of pipeline registers.  The order
     * in which addPipeStage is called does not affect the order in which
     * they connect or are clocked.  
     * 
     * The new pipeline register must have been assigned a name.
     * 
     * @param reg New pipeline register with name.
     */
    public void addPipeReg(IPipeReg reg);

    /**
     * Add a new functional unit to the set of child modules
     * 
     * The new unit must have been assigned a name.
     * 
     * @param reg New functional unit with name.
     */
    public void addChildUnit(IFunctionalUnit unit);
    
    /**
     * Add an existing pipeline register as an output from an existing pipeline
     * stage.  Pipeline stages can have multiple outputs, but a pipeline
     * register may have a single source.
     * 
     * @param source_stage Pipe stage getting new output.
     * @param target_reg Pipe register being assigned source.
     */
    public void connect(IPipeStage source_stage, IPipeReg target_reg);
    
    /**
     * Add an existing pipeline register as an input to an existing pipeline
     * stage.  Pipeline stages can have multiple inputs, but a pipeline 
     * register may only have a single sink.
     * 
     * @param source_reg Pipe register being assigned sink.
     * @param target_stage Pipe stage getting new input.
     */
    public void connect(IPipeReg source_reg, IPipeStage target_stage);
    
    /**
     * Connect two pipeline elements by name.  One argument must be a
     * pipeline register, and the other must be a pipeline stage.
     * 
     * @param source
     * @param target
     */
    public void connect(String source_name, String target_name);
    
    public void connect(String stage1, String reg, String stage2);    
    
    /**
     * Create a new pipeline register, with the given list of default
     * property names, and add it to the map of pipeline registers.
     * 
     * @param name Name of new pipeline register
     * @param props String array of property names
     */
    void createPipeReg(String name, String[] props);

    /**
     * Create a new pipeline register with no default property names and
     * add it to the map of pipeline registers.
     * 
     * @param name Name of new pipeline register
     */
    void createPipeReg(String name);
    
    /**
     * Specify the name of a pipeline register that may contain a result that
     * could be forwarded to other stages that need result values.
     * @param name
     */
    void addForwardingSource(String name);

    /**
     * This doesn't do anything useful yet.  The idea is to make forwarding
     * at least partially automatic.
     * 
     * Unfortunately, forwarding can be complicated, where some stages might
     * need some results immediately, while others can have them posted for
     * one cycle into the future.
     * 
     * For now, there are the following methods in PipelineStageBase that are
     * to be used:
     * - A decode stage can use registerFileLookup
     * - Any stage that wants to set up forwarding can call forwardingSearch
     * - Any stage whose predecessor called forwardingSearch must call doPostedForwarding
     * 
     * @param name
     */
    void addForwardingTarget(String name);
    
    public void addStageAlias(String real_name, String alias_name);

    public void addRegAlias(String real_name, String alias_name);
    
    void clockProperties();
}
