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
import utilitytypes.IFunctionalUnit;
import utilitytypes.IModule;
import utilitytypes.IPipeReg;
import utilitytypes.IPipeStage;
import utilitytypes.Logger;

/**
 * This is a generic pipeline register that is used to connect pipeline 
 * stages together.  The register's input is atomically transferred to its
 * output when advanceClock is called.
 * 
 * @author millerti
 */
public class PipelineRegister extends ComponentBase implements IPipeReg {
    int cycle_number_slave = 0;
    int cycle_number_master = 0;
    int cycle_number_clock = 0;
    
    protected Latch master;
    protected Latch slave;
    protected final Latch invalid;
    protected boolean slave_stalled;
    
    // Connecting pipeline stages
    protected IPipeStage stage_after;
    protected IPipeStage stage_before;

    public void setStageBefore(IPipeStage s) { 
        if (stage_before != null) {
            throw new RuntimeException("Pipeline register " + getHierarchicalName() +
                    " is already connected as an output from pipeline stage " +
                    stage_before.getHierarchicalName());
        }
        stage_before = s; 
    }
    public void setStageAfter(IPipeStage s) { 
        if (stage_after != null) {
            throw new RuntimeException("Pipeline register " + getHierarchicalName() +
                    " is already connected as an input to pipeline stage " +
                    stage_after.getHierarchicalName());
        }
        stage_after = s; 
    }
    public IPipeStage getStageBefore() { 
        if (stage_before == null) {
            throw new RuntimeException("There is no pipeline stage before PipeReg " + this.getHierarchicalName());
        }
        return stage_before; 
    }
    public IPipeStage getStageAfter() { 
        if (stage_after == null) {
            throw new RuntimeException("There is no pipeline stage after PipeReg " + this.getHierarchicalName());
        }
        return stage_after; 
    }

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
//        Logger.out.println("Register " + getName() + " master written with bubble");
        cycle_number_master = getCycleNumber();
    }
    public boolean isMasterBubble() { 
        int core_cycle = getCycleNumber();
        if (cycle_number_master != core_cycle) {
            IPipeStage before = getStageBefore();
            if (before == null) {
                throw new RuntimeException("No pipeline stage before " + getHierarchicalName());
            }
            before.evaluate();
            cycle_number_master = core_cycle;
        }
        return master.isNull(); 
    }

    /**
     * "slave_stalled" means that the pipeline stage that takes input from this
     * register is unable to accept input.  This may be a local condition
     * (e.g. the stage is waiting on a resource) or caused indirectly by 
     * a later pipeline stage being stalled.
     */
    @Override
    public void setSlaveStall(boolean s) { 
        slave_stalled = s; 
        cycle_number_slave = getCycleNumber();
    }
    
    @Override
    public void consumeSlave() {
        setSlaveStall(false);
        
        int stage_src_index = getIndexInAfter();
        IPipeStage stage = getStageAfter();
        stage.consumedInput(stage_src_index);
    }    

    private boolean isSlaveStalled() {   
        int core_cycle = getCycleNumber();
        if (cycle_number_slave != core_cycle) {
            if (slave.isNull()) {
                slave_stalled = false;
            } else {
                slave_stalled = true;
                getStageAfter().evaluate();
            }
            cycle_number_slave = core_cycle;
        }
        if (slave_stalled) {
            String pregname = getShortName();
            if (pregname.length()>=2 && Character.isLowerCase(pregname.charAt(0)) && Character.isUpperCase(pregname.charAt(1))) {
                pregname = pregname.substring(1);
            }
            getStageBefore().addStatusWord("OutputStall(" + pregname + ")");
        }
        return slave_stalled;
    }
    
    @Override
    public boolean canAcceptWork() {
        return !isSlaveStalled();
    }
    
    /**
     * Read the contents of this pipeline register, used by the pipeline stage
     * that has this pipeline register as input.
     * @return input to succeeding pipeline stage
     */
    @Override
    public Latch read() {
        return slave;
    }
    
    @Override
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
    @Override
    public void write(Latch output) {
//        Logger.out.println("Register " + getName() + " master written with " +
//            output.ins);
        master = output;
        cycle_number_master = getCycleNumber();
        
        int stage_dst_index = getIndexInBefore();
        IPipeStage stage = getStageBefore();
        stage.outputWritten(output, stage_dst_index);        
    }
    
    /**
     * Step forward one clock cycle.  When there are no stall conditions,
     * the contents of the master match are moved to the slave latch so
     * that the data can be read by the succeeding pipeline stage.
     */
    @Override
    public void advanceClock() {
        int core_cycle = getCycleNumber();
        if (cycle_number_clock == core_cycle) return;
        cycle_number_clock = core_cycle;
        
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
        cycle_number_clock = 0;
        master = new Latch(this);
        slave = new Latch(this);
    }
    
    
    /**
     * @return destination register number
     */
    public int getResultRegister() {
        return slave.getResultRegNum();
    }
    
        
    public EnumForwardingStatus matchForwardingRegister(int regnum) {
//        Logger.out.println("Trying to match R" + regnum + " against " +
//                getHierarchicalName());
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
    
    @Override
    public boolean isResultFloat() {
        return slave.isResultFloat();
    }

        
    /**
     * Constructor that accepts Set of property names
     * @param core reference to CpuCore
     * @param name name of this pipeline register
     * @param proplist Set of property names
     */
    public PipelineRegister(IModule parent, String name, Set<String> proplist) {
        super(parent, name);
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
    public PipelineRegister(IModule parent, String name, String[] proplist) {
        super(parent, name);
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
    public PipelineRegister(IModule parent, String name) {
        super(parent, name);
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
        return new Latch(this);
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
