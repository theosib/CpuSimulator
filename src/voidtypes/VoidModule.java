/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voidtypes;

import baseclasses.ComponentBase;
import java.util.Collections;
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
public class VoidModule extends VoidComponent implements IModule {
    private static final VoidModule singleton = new VoidModule();    
    public static VoidModule getVoidModule() {
        return singleton;
    }
    protected VoidModule() {}

    @Override
    public IProperties getProperties() {
        return VoidProperties.getVoidProperties();
    }

    @Override
    public IGlobals getGlobals() {
        return VoidGlobals.getVoidGlobals();
    }

    @Override
    public void initProperties() {}

    @Override
    public void resetProperties() {}

    @Override
    public void createPipelineRegisters() {}

    @Override
    public void createPipelineStages() {}

    @Override
    public void createChildModules() {}

    @Override
    public void createConnections() {}

    @Override
    public void specifyForwardingSources() {}

    @Override
    public void specifyForwardingTargets() {}

    @Override
    public void initModule() {}

    @Override
    public Map<String, IPipeStage> getLocalPipeStages() {
        return VoidUnit.void_stages;
    }

    @Override
    public Map<String, IPipeStage> getPipeStagesRecursive() {
        return VoidUnit.void_stages;
    }

    @Override
    public Map<String, IPipeReg> getLocalPipeRegs() {
        return VoidUnit.void_regs;
    }

    @Override
    public Map<String, IPipeReg> getPipeRegsRecursive() {
        return VoidUnit.void_regs;
    }

    public static final Map<String,IFunctionalUnit> void_units = 
            Collections.unmodifiableMap(new HashMap<String,IFunctionalUnit>());
    
    @Override
    public Map<String, IFunctionalUnit> getLocalChildUnits() { return void_units; }

    @Override
    public IPipeStage getLocalPipeStage(String name) {
        return VoidStage.getVoidStage();
    }

    @Override
    public IPipeStage getPipeStage(String name) {
        return VoidStage.getVoidStage();
    }

    @Override
    public IPipeReg getLocalPipeReg(String name) {
        return VoidRegister.getVoidRegister();
    }

    @Override
    public IPipeReg getPipeReg(String name) {
        return VoidRegister.getVoidRegister();
    }

    @Override
    public IFunctionalUnit getChildUnit(String name) {
        return VoidUnit.getVoidUnit();
    }

    @Override
    public void addPipeStage(IPipeStage stage) {}

    @Override
    public void addPipeReg(IPipeReg reg) {}

    @Override
    public void addChildUnit(IFunctionalUnit unit) {}

    @Override
    public void connect(IPipeStage source_stage, IPipeReg target_reg) {}

    @Override
    public void connect(IPipeReg source_reg, IPipeStage target_stage) {}

    @Override
    public void connect(String source_name, String target_name) {}

    @Override
    public void createPipeReg(String name, String[] props) {}

    @Override
    public void createPipeReg(String name) {}

    @Override
    public void addForwardingSource(String name) {}

    @Override
    public void addForwardingTarget(String name) {}

    @Override
    public String getShortName() {
        return ComponentBase.computeOnlyCaps(getLocalName(), "m");
    }    

    @Override
    public void clockProperties() {}

    @Override
    public void connect(String stage1, String reg, String stage2) {}

    @Override
    public void addStageAlias(String real_name, String alias_name) {}

    @Override
    public void addRegAlias(String real_name, String alias_name) {}
}
