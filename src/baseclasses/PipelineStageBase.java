/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package baseclasses;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import utilitytypes.EnumOpcode;
import utilitytypes.IPipeReg;
import utilitytypes.IPipeStage;
import voidtypes.VoidLatch;

/**
 * This is the base class for all pipeline stages.  Generics are used so that
 * specialized subclasses of PipelineRegister can be specified.
 * 
 * @author millerti
 */
public class PipelineStageBase implements IPipeStage {
    protected final String name;
    protected CpuCore core;
    int cycle_number = 0;
    Set<String> input_doing;
    Set<String> output_doing;
    Set<String> status_words;
    
    @Override
    public void clearStatus() {
        status_words = null;
    }
    
    @Override
    public void addStatusWord(String word) {
        if (status_words == null) status_words = new HashSet<>();
        status_words.add(word);
    }
    
    int topo_order = 0;
    @Override
    public void setTopoOrder(int pos) { topo_order = pos; }
    @Override
    public int getTopoOrder() { return topo_order; }
    
    protected List<IPipeReg> input_regs;
    protected boolean[] inputs_claimed;
    @Override
    public List<IPipeReg> getInputRegisters() { return input_regs; }
    
    @Override
    public int lookupInput(String name) {
        if (input_regs == null) return -1;
        for (int i=0; i<input_regs.size(); i++) {
            if (name == input_regs.get(i).getName()) return i;
        }
        return -1;
    }

    @Override
    public Latch readInput(int input_num) {
        if (input_regs == null) return VoidLatch.getVoidLatch();
        return input_regs.get(input_num).read();
    }
    
    @Override
    public void claimInput(int input_num) {
        if (inputs_claimed == null) return;
        if (input_num < 0 || input_num >= inputs_claimed.length) return;
        inputs_claimed[input_num] = true;
        input_doing.add(input_regs.get(input_num).read().ins.toString());
//        System.out.println("Stage " + getName() + " claimed input " + input_regs.get(input_num).getName() + ":");
//        System.out.println(input_regs.get(input_num).read());
    }
    
    
    
    protected List<IPipeReg> output_regs;
    protected boolean[] outputs_written;
    @Override
    public List<IPipeReg> getOutputRegisters() { return output_regs; }
    
    @Override
    public int lookupOutput(String name) {
        if (output_regs == null) return -1;
        for (int i=0; i<output_regs.size(); i++) {
            if (name == output_regs.get(i).getName()) return i;
        }
        return -1;
    }
    
    @Override
    public boolean outputCanAcceptWork(int out_num) {
        if (output_regs == null) return true;
        return output_regs.get(out_num).canAcceptData();
    }

    @Override
    public Latch newOutput(int out_num) {
        if (output_regs == null) return VoidLatch.getVoidLatch();
        return output_regs.get(out_num).newLatch();
    }
    
    @Override
    public Latch invalidOutput(int out_num) {
        if (output_regs == null) return VoidLatch.getVoidLatch();
        return output_regs.get(out_num).invalidLatch();
    }
    
    @Override
    public void writeOutput(Latch out, int index) {
        if (outputs_written == null || output_regs == null) return;
        if (index < 0 || index >= outputs_written.length) return;
        outputs_written[index] = true;
        output_regs.get(index).write(out);
        output_doing.add(out.ins.toString());
        if (out.hasResultValue()) {
            int reg = out.getResultRegNum();
            int val = out.getResultValue();
            addStatusWord("R" + reg + "=" + val);
        }
    }
    
    
    // For debugging purposes, a string indicating that the stage is 
    // currently working on.  This needs to be done better.
    private String currently_doing;
    
    @Override
    public void clearActivity() { currently_doing = null; }
    @Override
    public void setActivity(String act) { currently_doing = act; }
    @Override
    public String getActivity() { return currently_doing; }
    @Override
    public String getStatus() {
        if (stageWaitingOnResource()) {
            addStatusWord("ResourceWait");
        }
        if (stageHasWorkToDo()) {
            addStatusWord("HasWork");
        }
        if (status_words == null) return "";
        return String.join(", ", status_words);
    }
    

    protected boolean resource_wait;
    @Override
    public boolean stageWaitingOnResource() { return resource_wait; }
    @Override
    public void setResourceStall(boolean x) { resource_wait = x; }
    
    /**
     * Return true when this pipeline stage has a valid instruction as input 
     * and therefore has work it must perform.
     */
    @Override
    public boolean stageHasWorkToDo() {
        if (input_regs == null) return false;
        for (IPipeReg in : input_regs) {
            if (in.read().getInstruction().isValid()) return true;
        }
        return false;
    }
        
    protected void computeSlaveStall() {
        if (input_regs == null) return;
        
        boolean wait = stageWaitingOnResource();
        if (wait) {
            for (IPipeReg in : input_regs) {
                in.setSlaveStall(true);
            }
            return;
        }
        
        for (int i=0; i<input_regs.size(); i++) {
            boolean claimed = inputs_claimed[i];
            IPipeReg in = input_regs.get(i);
            boolean is_work = in.read().getInstruction().isValid();
            boolean stall = !claimed && is_work;
//            System.out.println("Stage " + getName() + " claimed=" + claimed + " is_work=" + is_work +
//                    " stall=" + stall + " for input " + in.getName());
            in.setSlaveStall(stall);
        }
    }
    
    protected void sendBubbles() {
        if (output_regs == null) return;
        
        boolean wait = stageWaitingOnResource();
        if (wait) {
            for (IPipeReg out : output_regs) {
                out.writeBubble();
            }
            return;
        }
        
        for (int i=0; i<output_regs.size(); i++) {
            boolean written = outputs_written[i];
            if (!written) {
                IPipeReg out = output_regs.get(i);
                out.writeBubble();
            }
        }
    }
    
    
    /**
     * Convenience function that automatically fetches input latch data
     * and sends output latch data.
     */
    @Override
    public void evaluate() {
        if (cycle_number == core.cycle_number) return;
//        System.out.println("Running evaluate for " + getName());

        input_doing = new HashSet<>();
        output_doing = new HashSet<>();
        clearStatus();
        clearActivity();
        setResourceStall(false);
        
        int num_inputs = 0;
        int num_outputs = 0;

        if (input_regs != null) {
            num_inputs = input_regs.size();
            inputs_claimed = new boolean[num_inputs];
        }
        if (output_regs != null) {
            num_outputs = output_regs.size();
            outputs_written = new boolean[num_outputs];
        }
        
        // Allocate a new latch to hold the output of this pipeline stage,
        // which is then passed to the succeeding pipeline register.
        
        if (num_inputs <= 1 && num_outputs <= 1) {
            // Fetch the contents of the slave latch of the preceding pipeline
            // register.
            Latch input;
            if (num_inputs == 0) {
                input = VoidLatch.getVoidLatch();
            } else {
                input = readInput(0);
            }
            
            if (!input.getInstruction().getOpcode().isNull()) {
                currently_doing = input.getInstruction().toString();
            }

            Latch output;
            if (num_outputs == 0) {
                output = VoidLatch.getVoidLatch();
            } else {
                output = newOutput(0);
            }
            
            // Call the compute method for this pipeline stage.
            compute(input, output);

            if (currently_doing == null) {
                currently_doing = output.getInstruction().toString();
            }

            if (!stageWaitingOnResource() && output.canAcceptData()) {
                input.claim();
                output.write();
            }            
        } else {
            compute();
        }
        
        computeSlaveStall();
        sendBubbles();
        
        if (currently_doing == null) {
            if (input_doing.size() > 0) {
                setActivity(String.join(", ", input_doing));
            } else if (output_doing.size() > 0) {
                setActivity(String.join(", ", output_doing));
            } else {
                setActivity("----: NULL");
            }
        }
        
        cycle_number = core.cycle_number;
    }
    
    /**
     * If a pipeline stage has at most one input and at most one output,
     * then this method will be called to compute the output from the input.
     * 
     * For this scenario, the evaluate method automatically claims the
     * input and writes the output under appropriate conditions.
     * 
     * If a stall must occur due to waiting on a resource, then 
     * setResourceStall must be explicitly called.
     * 
     * @param input -- data from pervious pipeline stage
     * @param output -- data to next pipeline stage
     */
    protected void compute(Latch input, Latch output) {
        throw new java.lang.UnsupportedOperationException("compute(Latch, Latch) needs to be implemented");
    }

    /**
     * It a stage has more than one of either inputs or outputs, then this
     * method is called by evaluate.  In this scenario, is necessary to 
     * perform each of the following explicitly:
     * - read inputs (readInput)
     * - allocate latches for outputs (newOutput)
     * - check needed output registers for stall conditions 
     *   (theOutputLatch.canAcceptData()).
     * - identify resource wait stalls (setResourceStall)
     * - claim/consume inputs that are actually used (theInputLatch.claim()).
     * - compute outputs
     * - send outputs to output registers (theOutputLatch.write()).
     * 
     * evaluate() attempts to perform some functions automatically, including:
     * - compute input register stalls based on all stall information.
     * - specify output register bubbles based on all stall information.
     * - compute activity (diagnostically report what work is being done)
     * - compute status (diagnostically report state information, e.g.
     *   waiting on resource, has work to do, etc.)
     * 
     * See documentation on IPipeStage.evaluate().
     */
    protected void compute() {
        throw new java.lang.UnsupportedOperationException("compute() needs to be implemented");
    }
    
    /**
     * Optional method to reset any internal data to the pipeline stage.
     */
    @Override
    public void reset() {
        cycle_number = 0;
        currently_doing = null;
        resource_wait = false;
    }   
    
    @Override
    public void addInputRegister(IPipeReg input) {
        if (input_regs == null) input_regs = new ArrayList<>();
        input.setStageAfter(this);
        input.setIndexInAfter(input_regs.size());
        input_regs.add(input);
    }
    
    @Override
    public void addOutputRegister(IPipeReg output) {
        if (output_regs == null) output_regs = new ArrayList<>();
        output.setStageBefore(this);
        output.setIndexInBefore(output_regs.size());
        output_regs.add(output);
    }
    
    public PipelineStageBase(CpuCore core, String name, IPipeReg input, IPipeReg output) {
        this.core = core;
        this.name = name;
        addInputRegister(input);
        addOutputRegister(output);
    }
    public PipelineStageBase(CpuCore core, String name) {
        this.core = core;
        this.name = name;
    }
    
    @Override
    public String getName() { return name; }
}
