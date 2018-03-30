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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import utilitytypes.IModule;
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
 */
public abstract class CpuCore extends ModuleBase implements ICpuCore {
    public int cycle_number = 0;
    
    @Override
    public int getCycleNumber() { return cycle_number; }
        
    protected List<IPipeStage> stage_topo_order;
    @Override
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
    
    @Override
    public void stageTopologicalSort(IPipeStage first_stage) {
        first_stage.setTopoOrder(0);
        stageTopologicalOrder(first_stage);
        
        stage_topo_order = new ArrayList(stages.values());
        stage_topo_order.sort((IPipeStage a, IPipeStage b) -> b.getTopoOrder() - a.getTopoOrder());
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

    Set<String> forwarding_sources = new HashSet<>();
    Set<String> forwarding_targets = new HashSet<>();
    
    @Override
    public void addForwardingSource(String name) {
//        System.out.println("addForwardingSource");
//        System.out.println(name);
//        System.out.println(forwarding_sources);
        forwarding_sources.add(name);
    }

    @Override
    public void addForwardingTarget(String name) {
        forwarding_targets.add(name);
    }
    
    @Override
    public Set<String> getForwardingSources() {
        return forwarding_sources;
    }
    @Override
    public Set<String> getForwardingTargets() {
        return forwarding_targets;
    }
    
    @Override
    public int getResultRegister(String pipe_reg_name) {
        IPipeReg reg = flattened_registers.get(pipe_reg_name);
        if (reg == null) {
            throw new RuntimeException("No such forwarding source register " + pipe_reg_name);
        }
        return reg.getResultRegister();
    }
    @Override
    public IPipeReg.EnumForwardingStatus matchForwardingRegister(String pipe_reg_name, int regnum) {
        IPipeReg reg = flattened_registers.get(pipe_reg_name);
        if (reg == null) {
            throw new RuntimeException("No such forwarding source register " + pipe_reg_name);
        }
        return reg.matchForwardingRegister(regnum);
    }
    @Override
    public int getResultValue(String pipe_reg_name) { 
        IPipeReg reg = flattened_registers.get(pipe_reg_name);
        if (reg == null) {
            throw new RuntimeException("No such forwarding source register " + pipe_reg_name);
        }
        return reg.getResultValue();
    }
    


    // List of pipeline stages to be automatically told to compute
    protected Map<String, IPipeStage> flattened_stages;
    
    // List of pipeline registrs to be automatically clocked
    protected Map<String, IPipeReg> flattened_registers;

    @Override
    public IPipeStage getPipeStage(String name) {
        return flattened_stages.get(name);
    }

    @Override
    public IPipeReg getPipeReg(String name) {
        return flattened_registers.get(name);
    }

    @Override
    public void computeFlattenedPipeStageMap() {
        flattened_stages = getPipeStagesRecursive();
    }

    @Override
    public void computeFlattenedPipeRegMap() {
        flattened_registers = getPipeRegsRecursive();
    }
}

