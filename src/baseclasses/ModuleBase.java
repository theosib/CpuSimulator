/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package baseclasses;

import java.util.HashMap;
import java.util.Map;
import utilitytypes.IFunctionalUnit;
import utilitytypes.IGlobals;
import utilitytypes.IModule;
import utilitytypes.IPipeReg;
import utilitytypes.IPipeStage;
import utilitytypes.IProperties;

/**
 *
 * @author millerti
 */
public abstract class ModuleBase extends ComponentBase implements IModule {
    
    // Container of data items specific to this module
    protected IProperties properties;
    
    // List of pipeline stages to be automatically told to compute
    protected Map<String, IPipeStage> stages    = new HashMap<>();
    
    // List of pipeline registrs to be automatically clocked
    protected Map<String, IPipeReg> registers   = new HashMap<>();
    
    // List of child modules.  It is expected that child modules form
    // segments of the pipeline and therefore must provide the full
    // IFunctionalUnit interface so that input and output register can 
    // be retrieved.
    protected Map<String, IFunctionalUnit> children = new HashMap<>();

    @Override
    public IPipeStage getLocalPipeStage(String name) {
        return stages.get(name);
    }
    
    @Override
    public IPipeReg getLocalPipeReg(String name) {
        return registers.get(name);
    }
    
    public void addPipeStage(IPipeStage stage) {
        stages.put(stage.getName(), stage);
    }
    
    public void addPipeReg(IPipeReg reg) {
        registers.put(reg.getName(), reg);
    }

    public void connect(IPipeStage source_stage, IPipeReg target_reg) {
        source_stage.addOutputRegister(target_reg);
    }
    
    public void connect(IPipeReg source_reg, IPipeStage target_stage) {
        target_stage.addInputRegister(source_reg);
    }

    
    
    @Override
    public IProperties getProperties() {
        if (properties == null) properties = new PropertiesContainer();
        return properties;
    }

    @Override
    public void initProperties() { }


    @Override
    public Map<String, IPipeStage> getLocalPipeStages() {
        return stages;
    }

    @Override
    public Map<String, IPipeReg> getLocalPipeRegs() {
        return registers;
    }

    @Override
    public Map<String, IFunctionalUnit> getLocalChildUnits() {
        return children;
    }

    @Override
    public IFunctionalUnit getChildUnit(String name) {
        return children.get(name);
    }

    @Override
    public void addChildUnit(IFunctionalUnit unit) {
        unit.initModule();
        children.put(unit.getLocalName(), unit);
    }
    
    public ModuleBase(IModule parent, String name) {
        super(parent, name);
    }
    
    // Lesson learned:  Do not call overridable methods from a constructor,
    // especially if it's a superclass constructor.  If I move initModule
    // down to the constructor at the lowest subclass (MyCpuCore), then
    // I don't get any runtime errors associated with initialized member
    // variables.
//    ModuleBase() {
//        initModule();
//    }
    
    public IGlobals getGlobals() {
        IProperties props = getProperties();
        if (props instanceof IGlobals) {
            return (IGlobals)props;
        } else {
            return getParent().getGlobals();
        }
    }
    
    
    public void resetProperties() {
        IProperties props = getProperties();
        if (props != null) props.clear();
        initProperties();
    }
    
    
    public void reset() {
        resetProperties();
        
        Map<String,IPipeStage> stages = getLocalPipeStages();
        if (stages != null) {
            for (IPipeStage s : stages.values()) {
                s.reset();
            }
        }
        
        Map<String,IPipeReg> regs = getLocalPipeRegs();
        if (regs != null) {
            for (IPipeReg r : regs.values()) {
                r.reset();
            }
        }

        Map<String,IFunctionalUnit> children = getLocalChildUnits();
        if (children != null) {
            for (IFunctionalUnit fu : children.values()) {
                fu.reset();
            }
        }
    }


    public void initModule() {
        initProperties();
        createPipelineRegisters();
        createPipelineStages();
        createChildModules();
        createConnections();
        specifyForwardingSources();
        specifyForwardingTargets();
    }
    
    
    public Map<String,IPipeStage> getPipeStagesRecursive() {
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
    
    public Map<String,IPipeReg> getPipeRegsRecursive() {
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
    
    public IPipeStage getPipeStage(String name) {
        int first_dot = name.indexOf('.');
        if (first_dot < 0) {
            IPipeStage stage = getLocalPipeStage(name);
            return stage;
        }
        
        String child_name = name.substring(0, first_dot);
        String inner_name = name.substring(first_dot+1);
        IFunctionalUnit child = getChildUnit(child_name);
        if (child == null) {
            throw new RuntimeException("Unable to find child " + child_name + 
                    " to look for " + inner_name);
        }
        return child.getPipeStage(inner_name);
    }
 
    public IPipeReg getPipeReg(String name) {
        int first_dot = name.indexOf('.');
        if (first_dot < 0) {
            return getLocalPipeReg(name);
        }
        
        String child_name = name.substring(0, first_dot);
        String inner_name = name.substring(first_dot+1);
        IFunctionalUnit child = getChildUnit(child_name);
        return child.getPipeReg(inner_name);
    }
    
    public void connect(String source_name, String target_name) {
        IFunctionalUnit module_src = null;
        IFunctionalUnit module_sink = null;
        IPipeStage stage_src = null;
        IPipeStage stage_sink = null;
        IPipeReg reg_src = null;
        IPipeReg reg_sink = null;
        
        module_src = getChildUnit(source_name);
        module_sink = getChildUnit(target_name);
        
        if (module_src != null) {
            Map<String,IPipeReg> src_regs = module_src.getOutputPipeRegs();
            if (src_regs.size() != 1) {
                throw new RuntimeException("To form automatic connections, module " 
                        + source_name + 
                        " must export exactly one register via getOutputPipeRegs");
            }
            reg_src = src_regs.values().iterator().next();
        } else {
            reg_src = getPipeReg(source_name);
        }
        
        if (module_sink != null) {
            Map<String,IPipeStage> dst_stages = module_sink.getInputPipeStages();
            if (dst_stages.size() != 1) {
                throw new RuntimeException("To form automatic connections, module " 
                        + target_name + 
                        " must export exactly one pipeline stage via getInputPipeStages");
            }
            stage_sink = dst_stages.values().iterator().next();
        } else {
            stage_sink = getPipeStage(target_name);
        }
        
        stage_src = getPipeStage(source_name);
        reg_sink = getPipeReg(target_name);
        
        if (stage_src != null && reg_src != null) {
            throw new RuntimeException("Module " + getHierarchicalName() +
                    ": source name " + source_name + 
                    " matches both register and stage");
        }
        if (stage_src == null && reg_src == null) {
            throw new RuntimeException("Module " + getHierarchicalName() +
                    ": source name " + source_name + 
                    " matches matches no known register or stage");
        }
        if (stage_sink != null && reg_sink != null) {
            throw new RuntimeException("Module " + getHierarchicalName() +
                    ": target name " + target_name + 
                    " matches both register and stage");
        }
        if (stage_sink == null && reg_sink == null) {
            throw new RuntimeException("Module " + getHierarchicalName() +
                    ": target name " + target_name + 
                    " matches matches no known register or stage");
        }

        if (stage_src != null && reg_sink != null) {
            connect(stage_src, reg_sink);
        } else if (reg_src != null && stage_sink != null) {
            connect(reg_src, stage_sink);
        } else {
            throw new RuntimeException("Module " + getHierarchicalName() +
                    ": cannot connect " + source_name + " to " +
                    target_name);
        }
    }
    
    
    /**
     * Create a new pipeline register, with the given list of default
     * property names, and add it to the map of pipeline registers.
     * 
     * @param name Name of new pipeline register
     * @param props String array of property names
     */
    public void createPipeReg(String name, String[] props) {
        IPipeReg pr = new PipelineRegister(this, name, props);
        this.addPipeReg(pr);
    }

    /**
     * Create a new pipeline register with no default property names and
     * add it to the map of pipeline registers.
     * 
     * @param name Name of new pipeline register
     */
    public void createPipeReg(String name) {
        IPipeReg pr = new PipelineRegister(this, name);
        this.addPipeReg(pr);
    }
    
    
    public void addForwardingSource(String name) {
        IModule parent = getParent();
        if (parent == null) {
            throw new RuntimeException("Top level module must override addForwardingSource");
        }
        parent.addForwardingSource(getLocalName() + '.' + name);
    }

    public void addForwardingTarget(String name) {
        IModule parent = getParent();
        if (parent == null) {
            throw new RuntimeException("Top level module must override addForwardingTarget");
        }
        parent.addForwardingTarget(getLocalName() + '.' + name);
    }
    
}
