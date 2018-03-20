/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package baseclasses;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the base class for all pipeline registers.  Generics are used
 * so that pipeline registers with different contents can be uses for
 * different pipeline stages.  
 * 
 * @author millerti
 */
public class PipelineRegister<LatchType extends LatchBase> {
    private LatchType master;
    private LatchType slave;
    private LatchType invalid;
    private Class<LatchType> latchclass;
    private boolean master_bubble, slave_stalled;
    
    /**
     * "master_bubble" means that the pipeline stage that writes to this
     * register is stalled and will produce no output to be passed to subsequent
     * stages.  This stall condition is usually local to the stage, which is
     * waiting on some resource.
     * @return
     */
    public void setMasterBubble(boolean s) { master_bubble = s; }
    public boolean isMasterBubble() { return master_bubble; }

    /**
     * "slave_stalled" means that the pipeline stage that takes input from this
     * register is unable to accept input.  This may be a local condition
     * (e.g. the stage is waiting on a resource) or caused indirectly by 
     * a later pipeline stage being stalled.
     */
    public void setSlaveStall(boolean s) { slave_stalled = s; }
    public boolean isSlaveStalled() { return slave_stalled; }
    public boolean canAcceptData() { return !isSlaveStalled(); }
    
    /**
     * Read the contents of this pipeline register, used by the pipeline stage
     * that has this pipeline register as input.
     * @return input to succeeding pipeline stage
     */
    public LatchType read() {
        return slave;
    }
    
    /**
     * Write to this pipeline register, used by the pipeline stage that has
     * this pipeline register as output.
     * @param output from preceding pipeline stage.
     */
    public void write(LatchType output) {
        master = output;
    }
    
    /**
     * Step forward one clock cycle.  When there are no stall conditions,
     * the contents of the master match are moved to the slave latch so
     * that the data can be read by the succeeding pipeline stage.
     */
    public void advanceClock() {
        if (isSlaveStalled()) {
            // The stage after this one cannot accept new work, so no data
            // can move.  We need to leave the slave latch untouched since
            // the succeeding stage will keep referencing the contents of the
            // slave latch every time compute() is called.
            return;
        }
        
        if (master_bubble) {
            // The succeeding stage is able to accept input.  We assume that
            // it has consumed the contents of the pipeline register's
            // slave latch, so we can clear the slave latch.
            // The preceeding stage, however, is not producing any output,
            // so we do nothing with the master latch.
            slave = invalid;
        } else {
            // No stall and no bubble, so advance data from master to slave and
            // clear master latch.
            slave = master;
            master = invalid;
        }
    }
    
    /**
     * Reset this pipeline stage to initial/blank condition.
     */
    public void reset() {
        try {
            master = latchclass.newInstance();
            slave = latchclass.newInstance();
            invalid = latchclass.newInstance();
            invalid.setInvalid();
        } catch (Exception ex) {
            System.err.println("Exception " + this.getClass().getSimpleName() + " resetting latches: " + ex);
        }
    }
    
    
    /**
     * @return destination register number
     */
    public int getForwardingDestinationRegisterNumber() {
        return slave.getForwardingDestinationRegisterNumber();
    }
    
    
    /**
     * If this method is ever to return true, you must override the method
     * of the same name in your subclass of LatchBase.
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
        return slave.isForwardingResultValid();
    }    

    
    /**
     * If this method is ever to return true, you must override the method
     * of the same name in your subclass of LatchBase.
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
        return slave.isForwardingResultValidNextCycle();
    }
    
    
    /**
     * If this method is ever to return a value, you must override the
     * method of the same name in your subclass of LatchBase.
     * 
     * @return Result value that will be written to target register.
     */
    public int getForwardingResultValue() {
        return slave.getForwardingResultValue();
    }
    
    
    /**
     * Get the class of the latch type that is being handled by this register.
     * This can be useful for debugging purposes.  For instance, you can print
     * out myregister.getLatchType().getSimpleName() to find out which 
     * pipeline register is involved in some activity you want to debug.
     * @return
     */
    public Class<LatchType> getLatchType() {
        return latchclass;
    }
    
    public String getLatchTypeName() {
        return getLatchType().getSimpleName();
    }
    
    public PipelineRegister(Class latchclass) throws Exception {
        this.latchclass = latchclass;
        reset();
    }
    
    /**
     * Use this to create a new latch of the type handled by a subclass of
     * PipelineRegister.
     * 
     * @return
     */
    public LatchType newLatch() {
        try {
            return latchclass.newInstance();
        } catch (Exception ex) {
            System.err.println("Exception " + this.getClass().getSimpleName() + " creating pipeline latch: " + ex);
        }
        return null;
    }
    
    /**
     * Get an invalid/empty/void/null latch of the type handled by a subclass of
     * PipelineRegister.
     * 
     * @return
     */
    public LatchType invalidLatch() {
        return invalid;
    }
}
