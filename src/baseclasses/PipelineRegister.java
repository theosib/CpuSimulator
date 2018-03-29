/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package baseclasses;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import utilitytypes.IPipeReg;
import utilitytypes.IPipeStage;

/**
 * This is a generic pipeline register that is used to connect pipeline 
 * stages together.  The register's input is atomically transferred to its
 * output when advanceClock is called.
 * 
 * @author millerti
 */
public class PipelineRegister implements IPipeReg {
    protected final CpuCore core;
    int cycle_number_slave = 0;
    int cycle_number_master = 0;
    
    protected final String name;
    protected Latch master;
    protected Latch slave;
    protected final Latch invalid;
    protected boolean slave_stalled;
    
    // Connecting pipeline stages
    protected IPipeStage stage_after;
    protected IPipeStage stage_before;

    public void setStageBefore(IPipeStage s) { 
        if (stage_before != null) {
            throw new RuntimeException("Pipeline register " + getName() +
                    " is already connected as an output from pipeline stage " +
                    stage_before.getName());
        }
        stage_before = s; 
    }
    public void setStageAfter(IPipeStage s) { 
        if (stage_after != null) {
            throw new RuntimeException("Pipeline register " + getName() +
                    " is already connected as an input to pipeline stage " +
                    stage_after.getName());
        }
        stage_after = s; 
    }
    public IPipeStage getStageBefore() { return stage_before; }
    public IPipeStage getStageAfter() { return stage_after; }

    protected int index_in_before, index_in_after;
    public void setIndexInBefore(int ix) { index_in_before = ix; }
    public void setIndexInAfter(int ix) { index_in_after = ix; }
    public int getIndexInBefore() { return index_in_before; }
    public int getIndexInAfter() { return index_in_after; }
    
    // Besides the instruction, all values passed between pipeline stages
    // are stored in a String-indexed map.  The list of property names is
    // specified in the pipeline register.
    protected Set<String> propertiesList;
    
    public void setPropertiesList(Set<String> pl) { propertiesList = pl; }
    public Set<String> getPropertiesList() { return propertiesList; }
    
    
    /**
     * "master_bubble" means that the pipeline stage that writes to this
     * register is stalled and will produce no output to be passed to subsequent
     * stages.  This stall condition is usually local to the stage, which is
     * waiting on some resource.
     * @return
     */
    public void writeBubble() { 
        master = invalid; 
        cycle_number_master = core.cycle_number;
    }
    public boolean isMasterBubble() { 
        if (cycle_number_master != core.cycle_number) {
            getStageBefore().evaluate();
            cycle_number_master = core.cycle_number;
        }
        return master.isNull(); 
    }

    /**
     * "slave_stalled" means that the pipeline stage that takes input from this
     * register is unable to accept input.  This may be a local condition
     * (e.g. the stage is waiting on a resource) or caused indirectly by 
     * a later pipeline stage being stalled.
     */
    public void setSlaveStall(boolean s) { 
        slave_stalled = s; 
        cycle_number_slave = core.cycle_number;
    }
    public boolean isSlaveStalled() {         
        if (cycle_number_slave != core.cycle_number) {
            getStageAfter().evaluate();
            cycle_number_slave = core.cycle_number;
        }
        if (slave_stalled) getStageBefore().addStatusWord("OutputStall");
        return slave_stalled;
    }
    
    /**
     * Read the contents of this pipeline register, used by the pipeline stage
     * that has this pipeline register as input.
     * @return input to succeeding pipeline stage
     */
    public Latch read() {
        return slave;
    }
    
    public Latch readNextCycle() {
        if (isMasterBubble()) return invalid;
        if (isSlaveStalled()) return slave;
        return master;
    }
    
    
    /**
     * Write to this pipeline register, used by the pipeline stage that has
     * this pipeline register as output.
     * @param output from preceding pipeline stage.
     */
    public void write(Latch output) {
        master = output;
        cycle_number_master = core.cycle_number;
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
            master = invalid;
            return;
        }
        
        if (isMasterBubble()) {
            // The succeeding stage is able to accept input.  We assume that
            // it has consumed the contents of the pipeline register's
            // slave latch, so we can clear the slave latch.
            master = invalid;
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
        cycle_number_slave = 0;
        cycle_number_master = 0;
        try {
            master = new Latch(this);
            slave = new Latch(this);
        } catch (Exception ex) {
            System.err.println("Exception " + this.getClass().getSimpleName() + " resetting latches: " + ex);
        }
    }
    
    
    /**
     * @return destination register number
     */
    public int getResultRegister() {
        return slave.getResultRegNum();
    }
    
        
    public EnumForwardingStatus matchForwardingRegister(int regnum) {
        if (slave.getResultRegNum()==regnum && slave.hasResultValue()) {
            return EnumForwardingStatus.VALID_NOW;
        }
        Latch next = readNextCycle();
        if (next.getResultRegNum()==regnum && next.hasResultValue()) {
            return EnumForwardingStatus.VALID_NEXT_CYCLE;
        }
        return EnumForwardingStatus.NULL;
    }
    
    public int getResultValue() {
        return slave.getResultValue();
    }
    


    
        
    public String getName() {
        return name;
    }
    
    /**
     * Constructor that accepts Set of property names
     * @param core reference to CpuCore
     * @param name name of this pipeline register
     * @param proplist Set of property names
     */
    public PipelineRegister(CpuCore core, String name, Set<String> proplist) {
        this.core = core;
        this.name = name;
        setPropertiesList(proplist);
        invalid = new Latch(this);
        invalid.setInvalid();
        reset();
    }

    /**
     * Constructor that accepts array of property names
     * @param core reference to CpuCore
     * @param name name of this pipeline register
     * @param proplist array of property names
     */
    public PipelineRegister(CpuCore core, String name, String[] proplist) {
        this.core = core;
        this.name = name;
        setPropertiesList(new HashSet<String>(Arrays.asList(proplist)));
        invalid = new Latch(this);
        invalid.setInvalid();
        reset();
    }
    
    /**
     * Constructor that does not take a list of property names.  
     * setPropertiesList can be used to set them later.  Also, 
     * 
     * @param core reference to CpuCore
     * @param name name of this pipeline register
     * @param proplist array of property names
     */
    public PipelineRegister(CpuCore core, String name) {
        this.core = core;
        this.name = name;
        invalid = new Latch(this);
        invalid.setInvalid();
        reset();
    }

    
    /**
     * Use this to create a new latch.  The latch is automatically configured
     * with this pipeline register as its parent for access to things like
     * the list of property names.
     * 
     * @return
     */
    public Latch newLatch() {
        try {
            return new Latch(this);
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
    public Latch invalidLatch() {
        return invalid;
    }
}
