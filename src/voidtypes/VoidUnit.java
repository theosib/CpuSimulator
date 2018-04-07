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
import utilitytypes.IPipeReg;
import utilitytypes.IPipeStage;

/**
 *
 * @author millerti
 */
public class VoidUnit extends VoidModule implements IFunctionalUnit {
    private static final VoidUnit singleton = new VoidUnit();
    public static VoidUnit getVoidUnit() { return singleton; }
    protected VoidUnit() {}

    @Override
    public IPipeStage getInputPipeStage(String name) {
        return VoidStage.getVoidStage();
    }

    public static final Map<String,IPipeStage> void_stages = Collections.unmodifiableMap(new HashMap<String,IPipeStage>());
    public static final Map<String,IPipeReg> void_regs = Collections.unmodifiableMap(new HashMap<String,IPipeReg>());
    

    @Override
    public Map<String, IPipeStage> getInputPipeStages() { return void_stages; }

    @Override
    public IPipeReg getOutputPipeReg(String name) {
        return VoidRegister.getVoidRegister();
    }


    @Override
    public Map<String, IPipeReg> getOutputPipeRegs() { return void_regs; }
    
    @Override
    public String getShortName() {
        return ComponentBase.computeOnlyCaps(getLocalName(), "u");
    }
}
