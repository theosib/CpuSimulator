/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package baseclasses;

import java.util.HashMap;
import java.util.Map;
import utilitytypes.IFunctionalUnit;
import utilitytypes.IModule;
import utilitytypes.IPipeReg;
import utilitytypes.IPipeStage;

/**
 *
 * @author millerti
 */
public abstract class FunctionalUnitBase extends ModuleBase implements IFunctionalUnit {
    
    public FunctionalUnitBase(IModule parent, String name) {
        super(parent, name);
    }
    
    @Override
    public IPipeStage getInputPipeStage(String name) {
        int first_dot = name.indexOf('.');
        
        if (first_dot > 0) {
            String child_name = name.substring(0, first_dot);
            String inner_name = name.substring(first_dot+1);
            IFunctionalUnit child = getChildUnit(child_name);
            return child.getInputPipeStage(inner_name);
        }
        
        if (!name.equals("in") && !name.startsWith("in:")) {
            throw new RuntimeException(
                    "Functional unit input register names must be 'in' or start with 'in:'");
        }
        return getLocalPipeStage(name);
    }
    
    @Override
    public Map<String,IPipeStage> getInputPipeStages() {
        Map<String,IPipeStage> stages = new HashMap<>();
        
        for (Map.Entry<String,IPipeStage> entry : getLocalPipeStages().entrySet()) {
            String name = entry.getKey();
            if (name.equals("in") || name.startsWith("in:")) {
                stages.put(name, entry.getValue());
            }
        }
        
        
        return stages;
    }
    
    @Override
    public IPipeReg getOutputPipeReg(String name) {
        int first_dot = name.indexOf('.');
        
        if (first_dot > 0) {
            String child_name = name.substring(0, first_dot);
            String inner_name = name.substring(first_dot+1);
            IFunctionalUnit child = getChildUnit(child_name);
            return child.getOutputPipeReg(inner_name);
        }

        if (!name.equals("out") && !name.startsWith("out:")) {
            throw new RuntimeException(
                    "Functional unit output register names must be 'out' or start with 'out:'");
        }
        return getLocalPipeReg(name);
    }


    @Override
    public Map<String,IPipeReg> getOutputPipeRegs() {
        Map<String,IPipeReg> regs = new HashMap<>();
        
        for (Map.Entry<String,IPipeReg> entry : getLocalPipeRegs().entrySet()) {
            String name = entry.getKey();
            if (name.equals("out") || name.startsWith("out:")) {
                regs.put(name, entry.getValue());
            }
        }

        
        return regs;
    }
    
}
