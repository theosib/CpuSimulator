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

        if (CpuSimulator.printStagesEveryCycle) {
            for (PipelineStageBase stage : stages) {
                Class cl = stage.getClass();
                System.out.printf("%-12s: %-40s %s\n", cl.getSimpleName(), stage.getActivity(),
                        stage.getStatus());
            }
            System.out.println();        
        }        
        
        // Tell every pipeline register to atomicaly move its input to its 
        // output.  Under stall conditions, this may have no effect.
        for (PipelineRegister reg : registers) {
            reg.advanceClock();
        }        
    }    
    
    
    /**
     * You probably don't need to override this method.  
     * 
     * @param index Pipeline register number
     * @return Destination register in pipeline register (slave latch)
     */
    public int getForwardingDestinationRegisterNumber(int index) {
        return registers.get(index).getForwardingDestinationRegisterNumber();
    }
    
    /**
     * If this method is ever to return true, you must override the method
     * of the same name in your subclass of LatchBase.
     * This method returns indication as to whether the value associated with
     * the target register is valid.
     * 
     * Decode will use this to determine when it's possible to forward to
     * itself.
     * 
     * @param index Pipeline register number
     * @return Validity of result in pipeline register (slave latch)
     */
    public boolean isForwardingResultValid(int index) {
        return registers.get(index).isForwardingResultValid();
    }    
    
    /**
     * If this method is ever to return true, you must override the method
     * of the same name in your subclass of LatchBase.
     * This method returns indication as to whether the value associated with
     * the target register will be valid in the next pipeline register on
     * the next cycle.
     * 
     * Decode will use this to determine when valid result is not available
     * NOW but will be available (to Execute) on the next cycle.  Decode
     * can pass information to Execute, specifying which pipeline register
     * it should forward from for each of its inputs.
     * 
     * @param index Pipeline register number
     * @return Validity of result in pipeline register (slave latch)
     */
    public boolean isForwardingResultValidNextCycle(int index) {
        return registers.get(index).isForwardingResultValidNextCycle();
    }
    
    /**
     * If this method is ever to return a value, you must override the
     * method of the same name in your subclass of LatchBase.
     * 
     * @param index Pipeline register number
     * @return Value of result in pipeline register (slave latch)
     */
    public int getForwardingResultValue(int index) {
        return registers.get(index).getForwardingResultValue();
    }
    
    /**
     * Iterates over pipeline registers (except Fetch2Decode) and prints
     * forwarding information in those registers.  The forwarding logic
     * in your Decode stage will look similar to this.
     */
    public void dumpForwardingData() {
        // Pipeline register 0 is FetchToDecode, which never can contain
        // a forwardable result value. Therefore we skip it.
        for (int i=1; i<registers.size(); i++) {
            // Get a latch name just for pretty printing
            String latchtypename = this.registers.get(i).getLatchTypeName();
            while (latchtypename.length() < 18) {
                latchtypename += " ";
            }
            
            // In Decode, you will loop over pipeline registers and 
            // look for forwarding opportunities.
            int regnum = this.getForwardingDestinationRegisterNumber(i);
            if (regnum < 0) {
                System.out.println(latchtypename + " has no target register");
            } else {
                boolean valid = this.isForwardingResultValid(i);
                if (valid) {
                    int value = this.getForwardingResultValue(i);
                    System.out.println(latchtypename + 
                            " has target register R" + regnum + " with value " +
                            value);
                } else {
                    System.out.println(latchtypename + 
                            " has target register R" + regnum + 
                            " with no value");
                }
            }
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

