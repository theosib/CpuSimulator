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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import utilitytypes.IPipeReg;
import utilitytypes.IPipeStage;
import utilitytypes.IProperties;

/**
 * This is a base class that can be used to build a CPU simulator.  It contains
 * lists to store pipeline stage and pipeline register objects, and it contains
 * code that automatically tells pipeline stages to compute and pipeline 
 * registers to transfer their inputs to their outputs.
 * 
 * @author millerti
 * @param <GlobalsType>
 */
public abstract class CpuCore implements ICpuCore {
    int cycle_number = 0;
    
    // Container of data items global to the CPU and/or accessed by multiple
    // pipeline stages.
    protected IGlobals globals;
    
    // List of pipeline stages to be automatically told to compute
    protected Map<String, IPipeStage> stages    = new HashMap<>();
    
    // List of pipeline registrs to be automatically clocked
    protected Map<String, IPipeReg> registers   = new HashMap<>();
    
    @Override
    public IPipeStage getPipeStage(String name) {
        return stages.get(name);
    }
    
    @Override
    public IPipeReg getPipeReg(String name) {
        return registers.get(name);
    }
    
    public void addPipeStage(IPipeStage stage) {
        stages.put(stage.getName(), stage);
    }
    
    public void addPipeReg(IPipeReg reg) {
        registers.put(reg.getName(), reg);
    }
    
    public void connect(IPipeStage source_stage, IPipeReg target_reg) {
        source_stage.addOutputRegister(target_reg);
    }
    
    public void connect(IPipeReg source_reg, IPipeStage target_stage) {
        target_stage.addInputRegister(source_reg);
    }
    
    public void connect(String source_name, String target_name) {
        if (stages.containsKey(source_name) && registers.containsKey(target_name)) {
            connect(stages.get(source_name), registers.get(target_name));
        } else if (registers.containsKey(source_name) && stages.containsKey(target_name)) {
            connect(registers.get(source_name), stages.get(target_name));
        } else {
            throw new RuntimeException("Pipeline construction: Cannot connect " + 
                    source_name + " to " + target_name);
        }
    }
    
    
    protected List<IPipeStage> stage_topo_order;
    public List<IPipeStage> getStageComputeOrder() { return stage_topo_order; }
    
    private void stageTopologicalOrder(IPipeStage stage) {
        int this_order = stage.getTopoOrder();
//        System.out.println("Stage " + stage.getName() + " has order " + this_order);
        
        int new_in_order = this_order - 1;
        List<IPipeReg> inputs = stage.getInputRegisters();
        if (inputs != null) {
            for (IPipeReg in_reg : inputs) {
                IPipeStage in_stage = in_reg.getStageBefore();
                int old_in_order = in_stage.getTopoOrder();
                if (old_in_order > new_in_order) {
                    in_stage.setTopoOrder(new_in_order);
                    stageTopologicalOrder(in_stage);
                }
            }
        }
        
        int new_out_order = this_order + 1;
        List<IPipeReg> outputs = stage.getOutputRegisters();
        if (outputs != null) {
            for (IPipeReg out_reg : outputs) {
                IPipeStage out_stage = out_reg.getStageAfter();
                int old_out_order = out_stage.getTopoOrder();
                if (old_out_order < new_out_order) {
                    out_stage.setTopoOrder(new_out_order);
                    stageTopologicalOrder(out_stage);
                }
            }
        }
    }
    
    public void stageTopologicalSort(IPipeStage first_stage) {
        first_stage.setTopoOrder(0);
        stageTopologicalOrder(first_stage);
        
        stage_topo_order = new ArrayList(stages.values());
        stage_topo_order.sort((IPipeStage a, IPipeStage b) -> b.getTopoOrder() - a.getTopoOrder());
    }
        
    /**
     * Provides access to the object containing CPU global resources
     * @return The globals object
     */
    @Override
    public IGlobals getGlobalResources() {
        return globals;
    }
    
    /**
     * Step the CPU by one clock cycle.
     */
    @Override
    public void advanceClock() {
        cycle_number++;

        // Tell all states to compute their outputs from their inputs, along
        // with stall conditions (waiting on resource or output unable to 
        // accept data).
        
        List<IPipeStage> stage_order = getStageComputeOrder();
        for (IPipeStage stage : stage_order) {
            stage.evaluate();
        }
        
        if (CpuSimulator.printStagesEveryCycle) {
            int n = stage_order.size();
            for (int i=n-1; i>=0; i--) {
                IPipeStage stage = stage_order.get(i);
                System.out.printf("%-12s: %-40s %s\n", stage.getName(), stage.getActivity(),
                        stage.getStatus());
            }
            System.out.println();        
        }        
        
        // Tell every pipeline register to atomicaly move its input to its 
        // output.  Under stall conditions, this may have no effect.
        for (IPipeReg reg : registers.values()) {
            reg.advanceClock();
        }
    }    

    public int getResultRegister(String pipe_reg_name) { 
        return registers.get(pipe_reg_name).getResultRegister();
    }
    public IPipeReg.EnumForwardingStatus matchForwardingRegister(String pipe_reg_name, int regnum) {
        return registers.get(pipe_reg_name).matchForwardingRegister(regnum);
    }
    public int getResultValue(String pipe_reg_name) { 
        return registers.get(pipe_reg_name).getResultValue();
    }
    
    
    /**
     * Reset all processor components to initial state.
     */
    @Override
    public void reset() {
        cycle_number = 0;
        for (Map.Entry<String,IPipeStage> ent : stages.entrySet()) {
            ent.getValue().reset();
        }
        for (Map.Entry<String,IPipeReg> ent : registers.entrySet()) {
            ent.getValue().reset();
        }
        globals.reset();
    }
}

