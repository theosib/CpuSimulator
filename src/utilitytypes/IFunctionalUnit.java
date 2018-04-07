/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilitytypes;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This defines a "container" of pipeline stages the together perform a 
 * coherent function.  This should aid in modularizing your processor design.
 * 
 * This extends IModule, giving it local properties and accessors for 
 * contained pipeline stages and pipeline registers and child functional units.
 * 
 * IFunctionalUnit is a specialization of IModule that requires the functional
 * unit to have well-defined input stages and output registers.
 * 
 * @author millerti
 */
public interface IFunctionalUnit extends IModule {
    
    /**
     * Lookup input pipeline register by relative name.  
     * 
     * @param  name of input pipeline register
     * @return IPipeReg
     */
    IPipeStage getInputPipeStage(String name);
    
    
    /**
     * Pipeline registers that are inputs to a functional unit must be named
     * "in" or have a name starting with "in:".
     * 
     * @return all input pipeline registers
     */
    Map<String,IPipeStage> getInputPipeStages();
    
    /**
     * Lookup output pipeline register by relative name.  
     * 
     * @param  name of output pipeline register
     * @return IPipeReg
     */
    IPipeReg getOutputPipeReg(String name);
    
    /**
     * Pipeline registers that are outputs from a functional unit must have names
     * that are "out" or start with "out:".
     * 
     * @return all output pipeline registers
     */
    Map<String,IPipeReg> getOutputPipeRegs();
    
}
