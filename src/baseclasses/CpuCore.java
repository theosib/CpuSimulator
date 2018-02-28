/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package baseclasses;

import utilitytypes.ICpuCore;
import utilitytypes.IGlobals;
import baseclasses.PipelineRegister;
import baseclasses.PipelineStageBase;
import cpusimulator.CpuSimulator;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a base class that can be used to build a CPU simulator.  It contains
 * lists to store pipeline stage and pipeline register objects, and it contains
 * code that automatically tells pipeline stages to compute and pipeline 
 * registers to transfer their inputs to their outputs.
 * 
 * @author millerti
 * @param <GlobalsType>
 */
public abstract class CpuCore<GlobalsType extends IGlobals> implements ICpuCore<GlobalsType> {
    // Container of data items global to the CPU and/or accessed by multiple
    // pipeline stages.
    protected GlobalsType globals;
    
    // List of pipeline stages to be automatically told to compute
    protected List<PipelineStageBase> stages      = new ArrayList<>();
    
    // List of pipeline registrs to be automatically clocked
    protected List<PipelineRegister> registers    = new ArrayList<>();
        
    /**
     * Provides access to the object containing CPU global resources
     * @return The globals object
     */
    @Override
    public GlobalsType getGlobalResources() {
        return globals;
    }
    
    /**
     * Step the CPU by one clock cycle.
     */
    @Override
    public void advanceClock() {        
        // Tell all states to compute their outputs from their inputs.
        // Note that this is why compute() must be idempotent.  This is
        // called every cycle regardless of stall condition.  This is necessary
        // so that stages that are stalled can detect when they are no longer
        // stalled.
        //
        // IMPORTANT:  This should compute the stall condition so that 
        // my_pipeline_stage.stageWaitingOnResource() returns true on
        // a stall condition.
        
        for (PipelineStageBase stage : stages) {
            stage.compute();
        }
        
        // Next propagate any stall condition. For now, it is sufficient
        // to run backward down the pipeline.  For more complex designs, 
        // we'll have to do something different.  
        // 
        // IMPORTANT:  This uses the value that 
        // my_pipeline_stage.stageWaitingOnResource() returns in order to
        // properly propagate stall status between pipeline stages.
        
        for (int i=stages.size()-1; i>=0; i--) {
            stages.get(i).propagateStall();
        }
        
        // Tell every pipeline stage to update any internal or global state 
        // that must change when advancing to the next clock cycle.
        // NOTE: It is important to check for stall conditions.
        for (PipelineStageBase stage : stages) {
            stage.advanceClock();
        }
        
        // Tell every pipeline register to atomicaly move its input to its 
        // output.  Under stall conditions, this may have no effect.
        for (PipelineRegister reg : registers) {
            reg.advanceClock();
        }
        
        if (CpuSimulator.printStagesEveryCycle) {
            for (PipelineStageBase stage : stages) {
                Class cl = stage.getClass();
                System.out.printf("%-12s: %-40s %s\n", cl.getSimpleName(), stage.getActivity(),
                        stage.getStatus());
            }
            System.out.println();        
        }        
    }    
    
    /**
     * Reset all processor components to initial state.
     */
    @Override
    public void reset() {
        for (PipelineStageBase stage : stages) {
            stage.reset();
        }
        for (PipelineRegister reg : registers) {
            reg.reset();
        }
        globals.reset();
    }
}

