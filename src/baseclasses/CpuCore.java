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

    public CpuCore(IModule parent, String name) {
        super(parent, name);
    }
    
    @Override
    public int getCycleNumber() { return cycle_number; }
    
    protected Set<IPipeStage> known_stages;
    protected List<IPipeStage> stage_topo_order;
    @Override
    public List<IPipeStage> getStageComputeOrder() { return stage_topo_order; }
    
    private void stageTopologicalOrder(IPipeStage stage) {
        known_stages.add(stage);
        
        int this_order = stage.getTopoOrder();
//        System.out.println("Stage " + stage.getHierarchicalName() + " has order " + this_order);
        
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
        known_stages = new HashSet<>();
        
        first_stage.setTopoOrder(0);
        stageTopologicalOrder(first_stage);
        
        stage_topo_order = new ArrayList(known_stages);
        stage_topo_order.sort((IPipeStage a, IPipeStage b) -> b.getTopoOrder() - a.getTopoOrder());
        
        int[] last_swapped = {-1, -1};
        
        boolean swapped = true;
        while (swapped) {
            swapped = false;

            for (int i=2; i<stage_topo_order.size(); i++) {
                IPipeStage me = stage_topo_order.get(i);
                IModule myparent = me.getParent();
                if (myparent == null || myparent == this) continue;
                int myorder = me.getTopoOrder();
                IPipeStage pr = stage_topo_order.get(i-1);
                int prorder = pr.getTopoOrder();
                if (myorder != prorder) { continue; }

                int child_ix = -1;
                for (int j=i-2; j>=0; j--) {
                    IPipeStage other = stage_topo_order.get(j);
                    IModule oparent = other.getParent();
                    if (oparent == null) continue;
                    IModule pparent = oparent.getParent();
                    if (pparent == null) continue;
                    if (pparent == myparent) {
//                        System.out.println(me.getHierarchicalName() + " at " + 
//                                i + " attracted to " +
//                                oparent.getHierarchicalName() + " at " + j);
                        child_ix = j;
                        break;
                    }
                }
                if (child_ix < 0) { continue; }

                stage_topo_order.set(i, pr);
                stage_topo_order.set(i-1, me);
                if (i-1 == last_swapped[0] && i == last_swapped[1]) break;
                last_swapped[0] = i-1;
                last_swapped[1] = i;
//                System.out.println("Swapping " + me.getHierarchicalName() + " at " + i +
//                        " with " + pr.getHierarchicalName() + " at " + (i-1));
                swapped = true;
                i--;
                if (i>2) i--;
            }
        }
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
                System.out.printf("%-30s: %-40s %s\n", stage.getHierarchicalName(), stage.getActivity(),
                        stage.getStatus());
            }
            System.out.println();        
        }        
        
        // Tell every pipeline register to atomicaly move its input to its 
        // output.  Under stall conditions, this may have no effect.
        for (IPipeReg reg : flattened_registers.values()) {
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
        IPipeReg reg = getPipeReg(pipe_reg_name);
        if (reg == null) {
            throw new RuntimeException("No such forwarding source register " + pipe_reg_name);
        }
        return reg.getResultRegister();
    }
    @Override
    public IPipeReg.EnumForwardingStatus matchForwardingRegister(String pipe_reg_name, int regnum) {
        IPipeReg reg = getPipeReg(pipe_reg_name);
        if (reg == null) {
            throw new RuntimeException("No such forwarding source register " + pipe_reg_name);
        }
        IPipeReg.EnumForwardingStatus stat = reg.matchForwardingRegister(regnum);
//        System.out.println("Got status " + stat);
        return stat;
    }
    @Override
    public int getResultValue(String pipe_reg_name) { 
        IPipeReg reg = getPipeReg(pipe_reg_name);
        if (reg == null) {
            throw new RuntimeException("No such forwarding source register " + pipe_reg_name);
        }
        return reg.getResultValue();
    }
    
    public boolean isResultFloat(String pipe_reg_name) {
        IPipeReg reg = getPipeReg(pipe_reg_name);
        if (reg == null) {
            throw new RuntimeException("No such forwarding source register " + pipe_reg_name);
        }
        return reg.isResultFloat();
    }
    
    
    
    
    
    private String[] splitRegString(String rstring) {
        String[] split = rstring.split(" ");
        for (int i=1; i<split.length; i++) {
            split[i] = " " + split[i];
        }
        return split;
    }
    
    private int splitRegStringMaxLen(String rstring) {
        String[] split = splitRegString(rstring);
        int maxlen = 0;
        for (int i=0; i<split.length; i++) {
            int len = split[i].length();
            if (len > maxlen) maxlen = len;
        }
        return maxlen;
    }
    
    @Override
    public void printHierarchy() {
        List<String[]> rows = new ArrayList<String[]>();
        int[] maxcol = new int[3];
        
        List<IPipeStage> stage_order = getStageComputeOrder();
        int n = stage_order.size();
        for (int i=n-1; i>=0; i--) {
            IPipeStage stage = stage_order.get(i);
            String[] cols = stage.connectionsToStringArr();
            
            for (int j=0; j<3; j++) {
                int l;
                if (j!=1) {
                    l = splitRegStringMaxLen(cols[j]);
                } else {
                    l = cols[j].length();
                }
                if (l > maxcol[j]) maxcol[j] = l;
            }
            
            rows.add(cols);
        }
        
        for (String[] cols : rows) {
            String[] split0 = splitRegString(cols[0]);
            String[] split2 = splitRegString(cols[2]);
            int nsplit = (split0.length > split2.length) ? split0.length : split2.length;
            for (int j=0; j<nsplit; j++) {
                String arrow = (j==0) ? "->" : "  ";
                String col0 = (j>=split0.length) ? "" : split0[j];
                String col1 = (j==0) ? cols[1] : "";
                String col2 = (j>=split2.length) ? "" : split2[j];
                System.out.printf("%-" + maxcol[0] + "s %s %-" + maxcol[1] + "s %s %-" + maxcol[2] + "s\n", 
                        col0, arrow, col1, arrow, col2);
            }
        }
    }

//    // List of pipeline stages to be automatically told to compute
//    protected Map<String, IPipeStage> flattened_stages;
    
    // List of pipeline registrs to be automatically clocked
    protected Map<String, IPipeReg> flattened_registers;

//    @Override
//    public IPipeStage getPipeStage(String name) {
//        return flattened_stages.get(name);
//    }

//    @Override
//    public IPipeReg getPipeReg(String name) {
//        return flattened_registers.get(name);
//    }

//    @Override
//    public void computeFlattenedPipeStageMap() {
//        flattened_stages = getPipeStagesRecursive();
//    }


    public void computeFlattenedPipeRegMap() {
        flattened_registers = getPipeRegsRecursive();
    }
    
    
    public void initModule() {
        super.initModule();
//        computeFlattenedPipeStageMap();
        computeFlattenedPipeRegMap();
        stageTopologicalSort(getFirstStage());
    }    
}

