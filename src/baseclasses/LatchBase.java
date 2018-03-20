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
    
    
    /**
     * You probably don't need to override this, as long as oper0's target
     * register number isn't changed.  Notice that this only returns a valid
     * number if the instruction will do a writeback.  Only those instructions
     * can have results we want to forward.
     * 
     * @return destination register number
     */
    public int getForwardingDestinationRegisterNumber() {
        if (ins.getOpcode().needsWriteback()) {
            return ins.getOper0().getRegisterNumber();
        } else {
            return -1;
        }        
    }
    
    
    /**
     * You must override this method if it ever needs to return true.
     * 
     * This method returns indication as to whether the value associated with
     * the target register is valid. 
     * - For DecodeToExecute, there is never a valid result.
     * - For ExecuteToMemory, all instructions that will do writeback will
     *   have a valid result *except LOAD*.
     * - For MemoryToWriteback, all instruction that will write back will have
     *   a valid result.
     * 
     * @return Validity of result.
     */
    public boolean isForwardingResultValid() {
        return false;
    }
    
    /**
     * You must override this method if it ever needs to return true.
     * 
     * This method returns indication as to whether the value associated with
     * the target register WILL BE VALID in the NEXT CYCLE in the
     * NEXT PIPELINE REGISTER.
     * - For DecodeToExecute, all instructions that will do a writeback
     *   (except LOAD) will have a valid result in ExecuteToMemory on the
     *   next cycle..
     * - For ExecuteToMemory, all instructions that will do writeback will
     *   have a valid result in MemoryToWriteback on the next cycle;
     * - For MemoryToWriteback, results are written back to the register file,
     *   so you can't perform any forwarding on the next cycle;
     * 
     * @return Validity of result.
     */
    public boolean isForwardingResultValidNextCycle() {
        return false;
    }

    
    /**
     * You must override this method if it ever needs to return a value.
     * 
     * If there is a target register in the instruction, return the computed
     * result value.  Otherwise, it doesn't matter what you return.
     * 
     * @return Result value that will be written to target register.
     */
    public int getForwardingResultValue() {
        return 0;
    }    
}
