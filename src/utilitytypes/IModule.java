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
public interface IModule {
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
    default IGlobals getGlobals() {
        IProperties props = getProperties();
        if (props instanceof IGlobals) {
            return (IGlobals)props;
        } else {
            return getParent().getGlobals();
        }
    }

    /**
     * Create the PropertiesContainer and pre-fill it with any properties
     * that contained pipeline elements rely on.
     */
    default void initProperties() {};
    
    /**
     * Reset properties to initial state
     */
    default void resetProperties() {
        IProperties props = getProperties();
        if (props != null) props.clear();
        initProperties();
    }
    
    /**
     * Reset to initial conditions
     */
    default void reset() {
        resetProperties();
        
        Map<String,IPipeStage> stages = getLocalPipeStages();
        for (IPipeStage s : stages.values()) {
            s.reset();
        }
        
        Map<String,IPipeReg> regs = getLocalPipeRegs();
        for (IPipeReg r : regs.values()) {
            r.reset();
        }

        Map<String,IFunctionalUnit> children = getLocalChildUnits();
        for (IFunctionalUnit fu : children.values()) {
            fu.reset();
        }
    }
    
    /**
     * Set a reference to the next level up in the hierarchy.
     * @param parent
     */
    void setParent(IModule parent);

    /**
     * @return Reference to parent in the hierarchy
     */
    IModule getParent();
    
    /**
     * @return The top-level module, which should be an instance of ICpuCore
     */
    default ICpuCore getCore() {
        if (this instanceof ICpuCore) return (ICpuCore)this;
        return getParent().getCore();
    }
    
    default int getCycleNumber() {
        return getParent().getCycleNumber();
    }
    
    
    /**
     * Create all pipeline registers.  Convenience methods named createPipeReg
     * will create a pipeline register and automatically add to the map
     * of pipeline registers.
     * 
     * If this is for a sub-module, be sure to create clearly defined module
     * input and output pipeline registers.  Input register names are expected
     * to start with "in:", and output register names are expected to start
     * with "out:".
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
     * By convention a pipeline latch that requests automatic forwarding will
     * contain one or more of the following properties:
     * forward0 -- hierarchical name of forwarding source register to get data from for oper0
     * forward1 -- hierarchical name of forwarding source register to get data from for src1
     * forward2 -- hierarchical name of forwarding source register to get data from for src2
     */
    void specifyForwardingTargets();
    
    /**
     * Construct and connect internal components
     */
    default void initModule() {
        initProperties();
        createPipelineRegisters();
        createPipelineStages();
        createChildModules();
        createConnections();
        specifyForwardingSources();
        specifyForwardingTargets();
    }
    
    /**
     * @return Name of this module
     */
    String getLocalName();
    
    /**
     * @return Name of this module in hierarchy of modules
     */
    default String getHierarchicalName() {
        IModule parent = getParent();
        if (parent == null) {
            return getLocalName();
        } else {
            return parent.getHierarchicalName() + '.' + getLocalName();
        }
    }
    
    /**
     * @return Map of all contained pipeline stages, not including those
     * in sub-modules.
     */
    public Map<String,IPipeStage> getLocalPipeStages();
    
    public default Map<String,IPipeStage> getPipeStagesRecursive() {
        Map<String,IFunctionalUnit> children = getLocalChildUnits();
        if (children == null || children.size() == 0) {
            return getLocalPipeStages();
        }
        
        // New map to contain everything in hierarchy
        Map<String,IPipeStage> recursiveStages = new HashMap<>();
        
        // Add all local stages
        for (Map.Entry<String,IPipeStage> entry : getLocalPipeStages().entrySet()) {
            recursiveStages.put(entry.getKey(), entry.getValue());
        }
        
        // Iterate child modules
        for (Map.Entry<String,IFunctionalUnit> child : children.entrySet()) {
            IModule childUnit = child.getValue();
            Map<String,IPipeStage> childStages = childUnit.getPipeStagesRecursive();
            
            // Iterate child stages, prepending name of child module to
            // pipeline stage names.
            String prefix = child.getKey() + '.';
            for (Map.Entry<String,IPipeStage> entry : childStages.entrySet()) {
                String name = prefix + entry.getKey();
                recursiveStages.put(name, entry.getValue());
            }
        }
        
        return recursiveStages;
    }

    /**
     * @return Map of all contained pipeline registers, not including those in
     * sub-modules.
     */
    public Map<String,IPipeReg> getLocalPipeRegs();

    public default Map<String,IPipeReg> getPipeRegsRecursive() {
        Map<String,IFunctionalUnit> children = getLocalChildUnits();
        if (children == null || children.size() == 0) {
            return getLocalPipeRegs();
        }
        
        // New map to contain everything in hierarchy
        Map<String,IPipeReg> recursiveStages = new HashMap<>();
        
        // Add all local stages
        for (Map.Entry<String,IPipeReg> entry : getLocalPipeRegs().entrySet()) {
            recursiveStages.put(entry.getKey(), entry.getValue());
        }
        
        // Iterate child modules
        for (Map.Entry<String,IFunctionalUnit> child : children.entrySet()) {
            IModule childUnit = child.getValue();
            Map<String,IPipeReg> childRegs = childUnit.getPipeRegsRecursive();
            
            // Iterate child stages, prepending name of child module to
            // pipeline stage names.
            String prefix = child.getKey() + '.';
            for (Map.Entry<String,IPipeReg> entry : childRegs.entrySet()) {
                String name = prefix + entry.getKey();
                recursiveStages.put(name, entry.getValue());
            }
        }
        
        return recursiveStages;
    }
    
    
    /**
     * @return Map of all contained functional units, not including those in
     * sub-modules.
     */
    public Map<String,IFunctionalUnit> getLocalChildUnits();
    
    /**
     * Get pipeline stage by name.
     * @param name
     * @return pipeline stage
     */
    public IPipeStage getPipeStage(String name);

    /**
     * Get pipeline register by name.
     * @param name
     * @return pipeline register
     */
    public IPipeReg getPipeReg(String name);
    
    /**
     * Get functional unit (child module) by name.
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
    public default void connect(String source_name, String target_name) {
        Map<String,IPipeStage> stages = getLocalPipeStages();
        Map<String,IPipeReg> registers = getLocalPipeRegs();
        
        if (stages.containsKey(source_name) && registers.containsKey(target_name)) {
            connect(stages.get(source_name), registers.get(target_name));
        } else if (registers.containsKey(source_name) && stages.containsKey(target_name)) {
            connect(registers.get(source_name), stages.get(target_name));
        } else {
            throw new RuntimeException("Pipeline construction for module " 
                    + getHierarchicalName() + ": Cannot connect " + 
                    source_name + " to " + target_name);
        }        
    }
    
    
    /**
     * Create a new pipeline register, with the given list of default
     * property names, and add it to the map of pipeline registers.
     * 
     * @param name Name of new pipeline register
     * @param props String array of property names
     */
    default void createPipeReg(String name, String[] props) {
        IPipeReg pr = new PipelineRegister(this, name, props);
        this.addPipeReg(pr);
    }

    /**
     * Create a new pipeline register with no default property names and
     * add it to the map of pipeline registers.
     * 
     * @param name Name of new pipeline register
     */
    default void createPipeReg(String name) {
        IPipeReg pr = new PipelineRegister(this, name);
        this.addPipeReg(pr);
    }
    
    
    default void addForwardingSource(String name) {
        IModule parent = getParent();
        if (parent == null) {
            throw new RuntimeException("Top level module must override addForwardingSource");
        }
        parent.addForwardingSource(getLocalName() + '.' + name);
    }

    default void addForwardingTarget(String name) {
        IModule parent = getParent();
        if (parent == null) {
            throw new RuntimeException("Top level module must override addForwardingTarget");
        }
        parent.addForwardingTarget(getLocalName() + '.' + name);
    }
}
