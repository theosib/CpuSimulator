/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilitytypes;

import baseclasses.Latch;
import baseclasses.PipelineStageBase;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author millerti
 */
public interface IPipeReg extends IComponent {

    /**
     * Set the list of data properties that latches held by this pipeline
     * register are expected to contain by default.  Latches are not restricted
     * to contain only properties with these names.
     * 
     * It is not strictly necessary to provide a properties list, but it is
     * useful when a pipeline stage uses Latch.copyPropertiesFrom to copy
     * properties from an input to an output.  The list of properties 
     * copied comes from the pipeline register parent of the latch being
     * copied to.
     * 
     * @param pl list of property names
     */
    public void setPropertiesList(Set<String> pl);

    /**
     * @return list of default property names
     */
    public Set<String> getPropertiesList();
    
    /**
     * Connect a pipeline stage as source for this pipeline register
     * @param s
     */
    public void setStageBefore(IPipeStage s);

    /**
     * Connect a pipeline stage as sink for this pipeline register
     * @param s
     */
    public void setStageAfter(IPipeStage s);
    
    /**
     * @return Source pipeline stage for this pipeline register.
     */
    public IPipeStage getStageBefore();

    /**
     * @return Sink pipeline stage for this pipeline register.
     */
    public IPipeStage getStageAfter();
    
    /**
     * Pipeline stages can have multiple outputs.  The preceding pipeline
     * stage uses this method to specify the index of of this pipeline
     * register in its list of outputs.
     * @param ix index in array of output registers in preceding stage.
     */
    public void setIndexInBefore(int ix);
    
    /**
     * Pipeline stages can have multiple inputs.  The succeeding pipeline
     * stage uses this method to specify the index of of this pipeline
     * register in its list of inputs.
     * @param ix index in array of input registers in succeeding stage.
     */
    public void setIndexInAfter(int ix);

    /**
     * @return Index of this pipeline register in preceding pipeline stage's
     * list of outputs.
     */
    public int getIndexInBefore();

    /**
     * @return Index of this pipeline register in succeeding pipeline stage's
     * list of inputs.
     */
    public int getIndexInAfter();
    
    /**
     * Explicitly specifies that no work is being passed trough this 
     * pipeline register.  This is called in the preceding pipeline stage.
     * 
     * The master latch always defaults to a bubble at at the start of
     * each clock cycle, so writeBubble() is most useful to undo writing
     * of other latch contents.
     */
    public void writeBubble();

    /**
     * @return Returns true if this pipeline register's master latch
     * is empty, invalid, or has been written with a bubble.
     */
    public boolean isMasterBubble();
    
    /**
     * Called by the succeeding pipeline stage to indicate whether or not
     * the data in the slave latch of this pipeline register has been consumed.
     * 
     * setSlaveStall(true) indicates that the slave latch has not been
     * consumed.
     * 
     * setSlaveScall(false) indicates that the slave latch contents have
     * been consumed, allowing the pipeline register to pass new work
     * on the next clock cycle.
     * 
     * @param stalled When true, indicates that the slave latch must not
     * be overwritten.
     */
    public void setSlaveStall(boolean stalled);

    /**
     * Returns true when the slave latch data has not been consumed and
     * therefore must not be overwritten.  
     * @return
     */
    //public boolean isSlaveStalled();

    /**
     * Redundant method that returns true when this pipeline register can
     * accept new work.
     * @return
     */
    public boolean canAcceptWork();
    
    /**
     * Returns the contents of the slave latch, which is input to the 
     * succeeding pipeline stage.
     * 
     * @return Input to next pipeline stage.
     */
    public Latch read();
    
    /**
     * Indicate that the slave latch data has been used and consumed by the
     * succeeding pipeline stage.
     */
    public void consumeSlave();

    /**
     * Returns the latch contents that will be returned by read() from THIS
     * pipeline register on the next clock cycle.
     * 
     * This does not attempt to infer future latch contents by looking at
     * other pipeline register. Instead, it checks to see what the preceding
     * pipeline register has written to this register as output.  If the
     * preceding pipeline stage has not yet been evaluated, it will be
     * evaluated on-demand before this method returns.
     * 
     * @return Slave latch contents for THIS pipeline register one cycle in the
     * future.
     */
    public Latch readNextCycle();

    /**
     * Store the given latch contents in the master latch of this pipeline
     * register, preparing it to be moved to the slave latch when 
     * advanceClock is called.
     * 
     * @param output Output data from preceding pipeline stage to be passed
     * trough this pipeline register.
     */
    public void write(Latch output);
    
    /**
     * After all pipeline stages have consumed their inputs and produced 
     * their outputs, advanceClock is called on all pipeline registers to
     * atomically transfer master latch contents to slave latches.
     */
    public void advanceClock();
    
    
    /**
     * Factory method to generate a new latch whose parent is this pipeline
     * register.  This is called by the preceding pipeline stage to get
     * a latch to fill with output data.  
     * 
     * To commit the latch contents to be passed through the pipeline register,
     * the write method must be explicitly called, either on the pipeline
     * register or the latch.
     * 
     * If an allocated latch is never written/committed, it will simply
     * be garbage-collected.  There are no restrictions on how many latches
     * may be allocated or how many times the write method may be called.
     * The last call to the write method is the one that sticks.
     * 
     * @return Newly allocated latch.
     */
    public Latch newLatch();
    
    /**
     * Returns a reference to an invalid/bubble latch.  Calling the write
     * method with an invalid latch is equivalent to calling writeBubble or
     * never writing to the register during a cycle.
     * @return 
     */
    public Latch invalidLatch();

    // Forwarding status pertaining the register being queried:
    // NULL -- Nothing to forward and/or not match to a register being sought.
    // VALID_NOW -- This pipeline register's slave latch currently contains
    //     a result whose target arch/phys register matches the query, and
    //     it contains a value result.  This result can be retrieved immediately
    //     by calling getResultValue on this pipeline register.
    // VALID_NEXT_CYCLE -- After having evaluated the preceeding pipeline
    //     stage, the master latch contains a matching phys/arch register
    //     number and valid result, AND there is no stall condition on the
    //     slave latch.  Therefore, after advanceClock is called, the slave
    //     latch of THIS PIPELINE REGISTER is guaranteed to contain a 
    //     arch/phys register matching the query and a valid result.  This 
    //     is useful determining that forwarding can be performed in the next 
    //     clock cycle.
    public static enum EnumForwardingStatus {
        NULL, VALID_NOW, VALID_NEXT_CYCLE;
    }

    /**
     * If the slave latch contains an instruction that needs to perform 
     * a writeback, return the destination arch/phys register number.  
     * Otherwise return -1.
     * @return
     */
    public int getResultRegister();
    
    /**
     * Perform a query on this pipeline register to determine if the given
     * arch/phys register number matches the instruction in the slave latch,
     * and there is a valid result OR if there will be a match on the next
     * clock cycle.
     * 
     * @param regnum Physical or architectural register number
     * @return EnumForwardingStatus indicator.  See above.
     */
    public EnumForwardingStatus matchForwardingRegister(int regnum);
    
    /**
     * @return Computed result value in the slave latch, if any.
     */
    public int getResultValue();
    
    public boolean isResultFloat();
    
    default public IPipeReg getOriginal() { return this; }
    
}
