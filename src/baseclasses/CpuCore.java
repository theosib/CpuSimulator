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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import utilitytypes.IFunctionalUnit;
import utilitytypes.IModule;
import utilitytypes.IPipeReg;
import utilitytypes.IPipeStage;
import utilitytypes.IProperties;
import utilitytypes.Logger;

/**
 * This is a base class that can be used to build a CPU simulator.  It contains
 * lists to store pipeline stage and pipeline register objects, and it contains
 * code that automatically tells pipeline stages to compute and pipeline 
 * registers to transfer their inputs to their outputs.
 * 
 * Also see utilitytypes/ICpuCore for more documentation.
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
    protected List<IPipeStage> stage_print_order;
    
    @Override
    public List<IPipeStage> getStageComputeOrder() { return stage_topo_order; }
    
    @Override
    public List<IPipeStage> getStagePrintOrder() { return stage_print_order; }

    
    private void stageTopologicalOrder(IPipeStage stage) {
        stage = stage.getOriginal();
        known_stages.add(stage);
        
        int this_order = stage.getTopoOrder();
//        Logger.out.println("Stage " + stage.getHierarchicalName() + " has order " + this_order);
        
        int new_in_order = this_order - 1;
        List<IPipeReg> inputs = stage.getInputRegisters();
        if (inputs != null) {
            for (IPipeReg in_reg : inputs) {
                IPipeStage in_stage = in_reg.getStageBefore().getOriginal();
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
                IPipeStage out_stage = out_reg.getStageAfter().getOriginal();
                int old_out_order = out_stage.getTopoOrder();
                if (old_out_order < new_out_order) {
                    out_stage.setTopoOrder(new_out_order);
                    stageTopologicalOrder(out_stage);
                }
            }
        }
    }
    
            
    private int sortPrintOrder(IModule parent, int index, IPipeStage start) {
        start.setPrintOrder(index++);
        
        List<IPipeReg> outputs = start.getOutputRegisters();
        if (outputs==null || outputs.size()==0) return index;

        List<IPipeStage> expanded = new ArrayList<>();
        List<IPipeStage> expanded_lower = new ArrayList<>();
        for (IPipeReg reg : outputs) {
            IPipeStage stage = reg.getStageAfter();
            
            if (stage == null) {
                throw new RuntimeException("PipeReg " + reg.getHierarchicalName() + 
                        " does not output to any pipeline stage");
            }
            
            if (stage.getParent() == parent) {
                expanded.add(stage);
            } else {
                expanded_lower.add(stage);
            }
        }
        
        for (IPipeStage stage : expanded) {
            index = sortPrintOrder(parent, index, stage);
        }

        for (IPipeStage stage : expanded_lower) {
            index = sortPrintOrder(stage.getParent(), index, stage);
        }
        
        return index;
    }
    
    @Override
    public void stageTopologicalSort(IPipeStage first_stage) {
        known_stages = new HashSet<>();
        
        first_stage.setTopoOrder(0);
        stageTopologicalOrder(first_stage);
        
        stage_topo_order = new ArrayList(known_stages);
        stage_topo_order.sort((IPipeStage a, IPipeStage b) -> b.getTopoOrder() - a.getTopoOrder());
        
        
        
        stage_print_order = new ArrayList(known_stages);
        sortPrintOrder(this, 0, first_stage);
        stage_print_order.sort((IPipeStage a, IPipeStage b) -> a.getPrintOrder() - b.getPrintOrder());
        
//        for (IPipeStage stage : stage_print_order) {
//            System.out.println(stage.getPrintOrder() + ": " + stage.getHierarchicalName());
//        }
    }
    
    
    private String commonPrefix(String a, String b) {
        int alen = a.length();
        int blen = b.length();
        int i=0;
        for (i=0; i<alen && i<blen; i++) {
            if (a.charAt(i) != b.charAt(i)) {
                while (i>0) {
                    if (a.charAt(i-1) == '.') break;
                    i--;
                }
                if (a.charAt(i) == '.') i++;
                return a.substring(0, i);
            }
        }
        return a.substring(0, i);
    }
    
    private String repeatSpace(int len) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<len; i++) sb.append(' ');
        return sb.toString();
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
        List<IPipeStage> stage_topo_order = getStageComputeOrder();
        for (IPipeStage stage : stage_topo_order) {
            stage.evaluate();
        }
        
        // For diagnostic purposes, print out activity and status of every
        // pipeline stage.
        if (CpuSimulator.printStagesEveryCycle) {
            String last_name = "";
            List<IPipeStage> stage_print_order = getStagePrintOrder();
            for (IPipeStage stage : stage_print_order) {
                String name = stage.getHierarchicalName();
                String prefix = commonPrefix(last_name, name);

                last_name = name;
                
                int len_prefix = prefix.length();
                String indent = repeatSpace(len_prefix);
                name = indent + name.substring(len_prefix);
                
                Logger.out.printf("| %-30s: %-40s %s\n", name, stage.getActivity(),
                        stage.getStatus());
            }
        }        
        
        // Tell every pipeline register to atomicaly move its input to its 
        // output.  Pipeline registers take into account stall conditions.
        for (IPipeReg reg : flattened_registers.values()) {
            reg.advanceClock();
        }
        
        // We use the clocked properties feature on globals and properties
        // of submodules to allow property changes to take effect only
        // for the next clock cycle.  Without this, we get erroneous 
        // communication between stages in the same cycle, which causes
        // unexpected behaviors.
        clockProperties();
        
        // For now, this just causes the logger to print out a blank line,
        // but ONLY if other lines have been printed.
        Logger.out.advanceClock();
    }    

    Set<String> forwarding_sources = new HashSet<>();
    Set<String> forwarding_targets = new HashSet<>();
    
    @Override
    public void addForwardingSource(String name) {
        forwarding_sources.add(name);
    }

    /**
     * This feature isn't implemented yet
     * @param name 
     */
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
//        Logger.out.println("Got status " + stat);
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
        
        List<IPipeStage> stage_order = getStagePrintOrder();
        for (IPipeStage stage : stage_order) {
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
                Logger.out.printf("%-" + maxcol[0] + "s %s %-" + maxcol[1] + "s %s %-" + maxcol[2] + "s\n", 
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

