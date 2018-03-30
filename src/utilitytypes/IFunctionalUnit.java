/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilitytypes;

import java.util.HashMap;
import java.util.Map;

/**
 * This defines a "container" of pipeline stages the together perform a 
 * coherent function.  This should aid in modularizing your processor design.
 * 
 * This extends IModule, giving it local properties and accessors for 
 * contained pipeline stages and pipeline registers and child functional units.
 * 
 * IFunctionalUnit is a specialization of IModule that requires the functional
 * unit to have well-defined input registers and output registers.
 * 
 * @author millerti
 */
public interface IFunctionalUnit extends IModule {

    /**
     * Lookup input pipeline register by local name.  
     * 
     * @param module-local name of input pipeline register
     * @return IPipeReg
     */
    default IPipeReg getInputPipeReg(String name) {
        if (!name.startsWith("in:")) {
            throw new RuntimeException(
                    "Functional unit input register names must start with 'in:'");
        }
        return getPipeReg(name);
    }
    
    /**
     * Pipeline registers that are inputs to a functional unit must have names
     * starting with "in:".
     * 
     * @return all local pipeline registers with names that start with "in:"
     */
    default Map<String,IPipeReg> getInputPipeRegs() {
        Map<String,IPipeReg> regs = new HashMap<>();
        
        for (Map.Entry<String,IPipeReg> entry : getLocalPipeRegs().entrySet()) {
            String name = entry.getKey();
            if (name.startsWith("in:")) {
                regs.put(name, entry.getValue());
            }
        }
        
        return regs;
    }
    
    /**
     * Lookup output pipeline register by local name.  
     * 
     * @param module-local name of output pipeline register
     * @return IPipeReg
     */
    default IPipeReg getOutputPipeReg(String name) {
        if (!name.startsWith("out:")) {
            throw new RuntimeException(
                    "Functional unit output register names must start with 'out:'");
        }
        return getPipeReg(name);
    }
    
    /**
     * Pipeline registers that are outputs from a functional unit must have names
     * starting with "out:".
     * 
     * @return all local pipeline registers with names that start with "out:"
     */
    default Map<String,IPipeReg> getOutputPipeRegs() {
        Map<String,IPipeReg> regs = new HashMap<>();
        
        for (Map.Entry<String,IPipeReg> entry : getLocalPipeRegs().entrySet()) {
            String name = entry.getKey();
            if (name.startsWith("out:")) {
                regs.put(name, entry.getValue());
            }
        }
        
        return regs;
    }
    
}
