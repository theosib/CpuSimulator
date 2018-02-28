/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package baseclasses;

import java.lang.reflect.Method;
import java.util.ArrayList;
import voidtypes.VoidLatch;

/**
 * This is the base class for all pipeline stages.  Generics are used so that
 * specialized subclasses of PipelineRegister can be specified.
 * 
 * @author millerti
 */
public abstract class PipelineStageBase<IRT extends LatchBase, ORT extends LatchBase> {
    protected CpuCore core;
    protected PipelineRegister<IRT> input_reg;
    protected PipelineRegister<ORT> output_reg;
    protected boolean stalled_by_later_stages;
    
    // For debugging purposes, a string indicating that the stage is 
    // currently working on.  This needs to be done better.
    private String currently_doing;
    
    public void setActivity(String act) { currently_doing = act; }
    public String getActivity() { return currently_doing; }
    public String getStatus() {
        ArrayList<String> stats = new ArrayList<>();
        if (stageWaitingOnResource()) {
            stats.add("ResourceWait");
        }
        if (stageHasWorkToDo()) {
            stats.add("HasWork");
        }
        if (!nextStageCanAcceptWork()) {
            stats.add("NextStageStall");
        }
        return String.join(", ", stats);
    }
    
    /**
     * For any pipeline stage that can stall, this method can be overridden
     * so that it can return true under a stage-local stall condition.
     * @return true if stalled
     */
    public boolean stageWaitingOnResource() {
        return false;
    }
    
    /**
     * Return true when this pipeline stage has a valid instruction as input 
     * and therefore has work it must perform.
     */
    public boolean stageHasWorkToDo() {
        return input_reg.read().getInstruction().isValid();
    }
    
    /**
     * Returns true if the next stage is not stalled.
     * @return 
     */
    public boolean nextStageCanAcceptWork() {
        return output_reg.canAcceptData();
    }
    
    /**
     * Propagate stall conditions up the pipeline.
     */
    public void propagateStall() {
        boolean wait = stageWaitingOnResource();
        boolean busy = stageHasWorkToDo();
        boolean ncan = nextStageCanAcceptWork();
        
        // This stage (cannot) accept work from preceding stage
        boolean cannot_accept  = wait || (busy && !ncan);
        // Cannot produce work for the next stage
        boolean cannot_produce = wait;
        
        input_reg.setSlaveStall(cannot_accept);
        output_reg.setMasterBubble(cannot_produce);
    }
    
    /**
     * Convenience function that automatically fetches input latch data
     * and sends output latch data.
     */
    public void compute() {
        currently_doing = null;
        
        // Fetch the contents of the slave latch of the preceding pipeline
        // register.
        IRT input = input_reg.read();
        if (!input.getInstruction().isNull()) {
            currently_doing = input.getInstruction().toString();
        }
        
        // Allocate a new latch to hold the output of this pipeline stage,
        // which is then passed to the succeeding pipeline register.
        ORT output = output_reg.newLatch();
        
        // Call the compute method for this pipeline stage.
        compute(input, output);
        
        if (currently_doing == null) {
            currently_doing = output.getInstruction().toString();
        }
        
        if (stageWaitingOnResource()) {
            // If this stage is unable to produce any output due to being
            // stalled waiting on data or resources, actually send empty
            // latch contents to the next pipeline stage.
            output_reg.write(output_reg.invalidLatch());
        } else {
            // Otherwise, send the data computed by this stage.
            output_reg.write(output);
        }
    }
    
    /**
     * This method must be overridden to implement the logic of a pipeline
     * stage.
     * @param input -- data from pervious pipeline stage
     * @param output -- data to next pipeline stage
     */
    public abstract void compute(IRT input, ORT output);
    
    /**
     * Optional method to reset any internal data to the pipeline stage.
     */
    public void reset() {}
    
    
    /**
     * Its function is to advance state to the next clock cycle and
     * can be applied to any data that must be updated but which is
     * not stored in a pipeline register.  Be sure to check for stall
     * conditions like waiting on resource or next stage unable to 
     * receive!
     */
    public void advanceClock() {}
    
    
    public PipelineStageBase(CpuCore core, PipelineRegister input, PipelineRegister output) {
        this.core = core;
        this.input_reg = input;
        this.output_reg = output;
    }
}
