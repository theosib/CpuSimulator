/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package baseclasses;

import baseclasses.InstructionBase;
import voidtypes.VoidInstruction;

/**
 * Base class for latches.  A pipeline register will dynamically create
 * instances of these.  You will extend this base class to add additional
 * data that must flow between pipeline stages.
 * NOTE:  This already contains a field for the instruction.
 * 
 * @author millerti
 */
public class LatchBase {
    private boolean invalid = false;
    private InstructionBase ins = VoidInstruction.getVoidInstruction();
    
    /**
     * Invalidate the contents of this latch.  This is for passing bubbles
     * down the pipeline.
     */
    public void setInvalid() { 
        invalid = true; 
        ins = VoidInstruction.getVoidInstruction();
    }
    
    // Access methods for the instruction in the base class
    public InstructionBase getInstruction() { return ins; }
    public void setInstruction(InstructionBase ins) {
        if (!invalid) {
            this.ins = ins;
        }
    }
    
    /**
     * Find out if this latch contains no valid data
     * @return 
     */
    public boolean isNull() { return invalid || ins.isNull(); }
}
