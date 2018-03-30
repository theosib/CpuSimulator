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
public abstract class ModuleBase implements IModule {
    String name;
    IModule parent;
    
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
    public IPipeStage getPipeStage(String name) {
        return stages.get(name);
    }
    
    @Override
    public IPipeReg getPipeReg(String name) {
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
    public void setParent(IModule parent) {
        this.parent = parent;
    }

    @Override
    public IModule getParent() {
        return parent;
    }


    @Override
    public String getLocalName() {
        return name;
    }

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
        children.put(unit.getLocalName(), unit);
    }
    
    // Lesson learned:  Do not call overridable methods from a constructor,
    // especially if it's a superclass constructor.  If I move initModule
    // down to the constructor at the lowest subclass (MyCpuCore), then
    // I don't get any runtime errors associated with initialized member
    // variables.
//    ModuleBase() {
//        initModule();
//    }
}
