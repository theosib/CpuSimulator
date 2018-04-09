/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package baseclasses;

import cpusimulator.CpuSimulator;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import utilitytypes.EnumOpcode;
import utilitytypes.ICpuCore;
import utilitytypes.IFunctionalUnit;
import utilitytypes.IGlobals;
import utilitytypes.IModule;
import utilitytypes.IPipeReg;
import utilitytypes.IPipeStage;
import utilitytypes.IRegFile;
import utilitytypes.Logger;
import utilitytypes.Operand;
import voidtypes.VoidLatch;

/**
 * This is the base class for all pipeline stages.  Generics are used so that
 * specialized subclasses of PipelineRegister can be specified.
 * 
 * @author millerti
 */
public class PipelineStageBase extends ComponentBase implements IPipeStage {
    // TODO:  Make all of these private and provide a full complement of
    // accessor methods
    protected int my_cycle_number = 0;
    protected Set<String> input_doing;
    protected Set<String> output_doing;
    protected Set<String> status_words;
    
    @Override
    public void clearStatus() {
        status_words = null;
    }
    
    @Override
    public void addStatusWord(String word) {
        if (status_words == null) status_words = new HashSet<>();
        status_words.add(word);
    }
    
    private int topo_order = 0;
    @Override
    public void setTopoOrder(int pos) { topo_order = pos; }
    @Override
    public int getTopoOrder() { return topo_order; }
    
    private int print_order = -1;
    @Override
    public void setPrintOrder(int pos) { 
//        System.out.println(getHierarchicalName() + " order " + pos);
        print_order = pos; 
    }
    @Override
    public int getPrintOrder() { return print_order; }    
    
    private List<IPipeReg> input_regs;
    @Override
    public List<IPipeReg> getInputRegisters() { return input_regs; }
    
    @Override
    public int numInputRegisters() {
        if (input_regs == null) return 0;
        return input_regs.size();
    }    
    
    @Override
    public int lookupInput(String name) {
        if (input_regs == null) return -1;
        for (int i=0; i<input_regs.size(); i++) {
            if (name == input_regs.get(i).getLocalName()) return i;
        }
        return -1;
    }

    @Override
    public Latch readInput(int input_num) {
        if (input_regs == null) return VoidLatch.getVoidLatch();
        IPipeReg input = input_regs.get(input_num);
        Latch slave = input.read();
        String doing = slave.ins.toString();
//        Logger.out.println("Stage " + getName() + " input " + input.getName() + 
//                " read " + doing);
        return slave;
    }
    
    @Override
    public void consumedInput(int input_num) {
        IPipeReg input = input_regs.get(input_num);
        String doing = input.read().ins.toString();
//        Logger.out.println("Stage " + getName() + " input " + input.getName() + 
//                " consumed " + doing);
        input_doing.add(doing);
    }
    
    protected List<IPipeReg> output_regs;
    @Override
    public List<IPipeReg> getOutputRegisters() { return output_regs; }
    
    @Override
    public int numOutputRegisters() {
        if (output_regs == null) return 0;
        return output_regs.size();
    }    
    
    @Override
    public int lookupOutput(String name) {
        if (output_regs == null) return -1;
        for (int i=0; i<output_regs.size(); i++) {
            if (name == output_regs.get(i).getLocalName()) return i;
        }
        return -1;
    }
    
    @Override
    public boolean outputCanAcceptWork(int out_num) {
        if (output_regs == null) return true;
        return output_regs.get(out_num).canAcceptWork();
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
    public void outputWritten(Latch out, int index) {
//        Logger.out.println("Stage " + getName() + " output " + out.getName() + 
//                " written with " + out.ins.toString());
        output_doing.add(out.ins.toString());
        if (out.hasResultValue()) {
            String regname = out.getResultRegName();
            String val = out.getResultValueAsString();
            addStatusWord(regname + "=" + val);
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
            String str = "ResourceWait";
            String reason = getResourceWaitReason();
            if (reason.length()>0) str += "(" + reason + ")";
            addStatusWord(str);
        }
        if (stageHasWorkToDo()) {
            addStatusWord("HasWork");
        }
        if (status_words == null) return "";
        return String.join(", ", status_words);
    }
    

    protected String resource_wait;
    @Override
    public boolean stageWaitingOnResource() { return resource_wait != null; }
    @Override
    public void setResourceWait(String reason) { resource_wait = reason; }
    @Override
    public String getResourceWaitReason() { return resource_wait; }
    
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
    
    
    protected void sendBubbles() {
        if (output_regs == null) return;
        
        boolean wait = stageWaitingOnResource();
        if (wait) {
//            Logger.out.println("Bubbling all outputs of " + getName());
            // Just in case an output has been written, must set all outputs
            // to bubbles if there is a resource wait.
            for (IPipeReg out : output_regs) {
                out.writeBubble();
            }
        }
    }
    
    /**
     * When using this method, be sure to make a duplicate of the input Latch
     * at the top of the compute method!
     * 
     * @param input 
     */
    @Override
    public void registerFileLookup(Latch input) {
        if (!input.isDuplicate()) {
            throw new RuntimeException("regiterFileLookup must be called on a duplicate of the original input latch!");
        }
        InstructionBase ins = input.getInstruction();
        
        // Get the register file and valid flags
        IGlobals globals = getCore().getGlobals();
        IRegFile regfile = globals.getRegisterFile();

        EnumOpcode opcode = ins.getOpcode();
        boolean oper0src = opcode.oper0IsSource();
        
        Operand oper0 = ins.getOper0();
        Operand src1  = ins.getSrc1();
        Operand src2  = ins.getSrc2();
        
        if (oper0src) {
            int oper0reg = oper0.getRegisterNumber();
            if (oper0reg >= 0 && regfile.isValid(oper0reg)) {
                oper0.lookUpFromRegisterFile(regfile);
                if (CpuSimulator.printForwarding) {
                    Logger.out.printf("# Reading %s=%s from regfile for %s oper0\n", 
                            oper0.getRegisterName(), oper0.getValueAsString(),
                            getHierarchicalName());
                }
            }
        }
        
        int src1reg = src1.getRegisterNumber();
        if (src1reg >=0 && regfile.isValid(src1reg)) {
            src1.lookUpFromRegisterFile(regfile);
            if (CpuSimulator.printForwarding) {
                Logger.out.printf("# Reading %s=%s from regfile for %s src1\n", 
                        src1.getRegisterName(), src1.getValueAsString(),
                        getHierarchicalName());
            }
        }
        
        int src2reg = src2.getRegisterNumber();
        if (src2reg >= 0 && regfile.isValid(src2reg)) {
            src2.lookUpFromRegisterFile(regfile);
            if (CpuSimulator.printForwarding) {
                Logger.out.printf("# Reading %s=%s from regfile for %s src2\n", 
                        src2.getRegisterName(), src2.getValueAsString(),
                        getHierarchicalName());
            }
        }
    }
    
    public static final String[] operNames = {"oper0", "src1", "src2"};
    
    /**
     * When using this method, be sure to make a duplicate of the input Latch
     * at the top of the compute method!
     * 
     * @param input 
     */
    @Override
    public void forwardingSearch(Latch input) {
        if (!input.isDuplicate()) {
            throw new RuntimeException("forwardingSearch must be called on a duplicate of the original input latch!");
        }
        InstructionBase ins = input.getInstruction();
        ICpuCore core = getCore();
        
        input.deleteProperty("forward0");
        input.deleteProperty("forward1");
        input.deleteProperty("forward2");
        
        Set<String> fwdSources = core.getForwardingSources();
//        for (String s : fwdSources) {
//            Logger.out.println(s);
//        }

        EnumOpcode opcode = ins.getOpcode();
        boolean oper0src = opcode.oper0IsSource();

        Operand oper0 = ins.getOper0();
        Operand src1  = ins.getSrc1();
        Operand src2  = ins.getSrc2();
        // Put operands into array because we will loop over them,
        // searching the pipeline for forwarding opportunities.
        Operand[] operArray = {oper0, src1, src2};

        // For operands that are not registers, getRegisterNumber() will
        // return -1.  We will use that to determine whether or not to
        // look for a given register in the pipeline.
        int[] srcRegs = new int[3];
        // Only want to forward to oper0 if it's a source.
        srcRegs[0] = oper0src ? oper0.getRegisterNumber() : -1;
        srcRegs[1] = src1.getRegisterNumber();
        srcRegs[2] = src2.getRegisterNumber();

        for (int sn=0; sn<3; sn++) {
            int srcRegNum = srcRegs[sn];
            // Skip any operands that are not register sources
            if (srcRegNum < 0) continue;
            // Skip any operands that already have values
            if (operArray[sn].hasValue()) continue;
            Operand oper = operArray[sn];
            String srcRegName = oper.getRegisterName();
            String operName = operNames[sn];
            
            String srcFoundIn = null;
            boolean next_cycle = false;

            prn_loop:
            for (String fwd_pipe_reg_name : fwdSources) {
                IPipeReg.EnumForwardingStatus fwd_stat = core.matchForwardingRegister(fwd_pipe_reg_name, srcRegNum);

                switch (fwd_stat) {
                    case NULL:
                        break;
                    case VALID_NOW:
                        srcFoundIn = fwd_pipe_reg_name;
                        break prn_loop;
                    case VALID_NEXT_CYCLE:
                        srcFoundIn = fwd_pipe_reg_name;
                        next_cycle = true;
                        break prn_loop;
                }
            }

            if (srcFoundIn != null) {
                if (!next_cycle) {
                    // If the register number was found and there is a valid
                    // result, go ahead and get the value.
                    int value = core.getResultValue(srcFoundIn);
                    boolean isfloat = core.isResultFloat(srcFoundIn);
                    operArray[sn].setValue(value, isfloat);

                    if (CpuSimulator.printForwarding) {
                        Logger.out.printf("# Forwarding %s=%s from %s to %s of %s\n", 
                                srcRegName, oper.getValueAsString(),
                                srcFoundIn, operName,
                                getHierarchicalName());
                    }
                } else {
                    // Post forwarding for the next stage on the next cycle by
                    // setting a property on the latch that specifies which
                    // operand(s) is forwarded from what pipeline register.
                    // For instance, setting the property "forward1" to the 
                    // value "ExecuteToWriteback" will inform the next stage
                    // to get a value for src1 from ExecuteToWriteback.
                    String propname = "forward" + sn;
                    input.setProperty(propname, srcFoundIn);
                    
//                    if (CpuSimulator.printForwarding) {
//                        Logger.out.printf("Posting forward %s from %s to %s next stage\n", 
//                                srcRegName,
//                                srcFoundIn, operName);
//                    }
                }
            }
        }
    }
    
    /**
     * When using this method, be sure to apply it to the ORIGINAL input latch.
     * 
     * If the stage stalls for any reason and the input can't be consumed,
     * then the forwarding information passed from the prior stage will be
     * out-of-date on the next cycle, and stage will acquire bad data from
     * the forwarding sources (which are likely to contain new data).  
     * 
     * Instead, it is necessary to retrieve forwarded values regardless of 
     * any stalls.  If there is a stall, those forwarded values must be kept,
     * and they are kept by modifying the ORIGINAL input latch, rather than
     * a duplicate.
     * 
     * Once forwarded values are retrieved, the forwarding flags 
     * (forward0, forward1, forward2) are deleted from the input latch so that
     * calling doPostedForwarding again on the same latch won't retrieve
     * any bad data from the wrong time.
     * 
     * @param input 
     */
    @Override
    public void doPostedForwarding(Latch input) {
        if (input.isDuplicate()) {
            throw new RuntimeException("doPostedForwarding must be called on the original input latch!");
        }
        InstructionBase ins = input.getInstruction();
        ICpuCore core = getCore();
        
        if (input.hasProperty("forward0")) {
            String pipe_reg_name = input.getPropertyString("forward0");
            int oper0val = core.getResultValue(pipe_reg_name);
            boolean isfloat = core.isResultFloat(pipe_reg_name);
            Operand oper0 = ins.getOper0();
            oper0.setValue(oper0val, isfloat);
            if (CpuSimulator.printForwarding) {
                Logger.out.printf("# Forwarding %s=%s from %s to oper0 of %s\n", 
                        oper0.getRegisterName(), oper0.getValueAsString(),
                        pipe_reg_name, getHierarchicalName());
            }
            
            // Remove the property so that if there's a stall, forwarding is
            // not attempted again on the next cycle.
            input.deleteProperty("forward0");
        }
        
        if (input.hasProperty("forward1")) {
            String pipe_reg_name = input.getPropertyString("forward1");
            int source1 = core.getResultValue(pipe_reg_name);
            boolean isfloat = core.isResultFloat(pipe_reg_name);
            Operand src1 = ins.getSrc1();
            src1.setValue(source1, isfloat);
            if (CpuSimulator.printForwarding) {
                Logger.out.printf("# Forwarding %s=%s from %s to src1 of %s\n", 
                        src1.getRegisterName(), src1.getValueAsString(),
                        pipe_reg_name, getHierarchicalName());
            }

            // Remove the property so that if there's a stall, forwarding is
            // not attempted again on the next cycle.
            input.deleteProperty("forward1");
        }
        
        if (input.hasProperty("forward2")) {
            String pipe_reg_name = input.getPropertyString("forward2");
            int source2 = core.getResultValue(pipe_reg_name);
            boolean isfloat = core.isResultFloat(pipe_reg_name);
            Operand src2 = ins.getSrc2();
            src2.setValue(source2, isfloat);
            if (CpuSimulator.printForwarding) {
                Logger.out.printf("# Forwarding %s=%s from %s to src2 of %s\n", 
                        src2.getRegisterName(), src2.getValueAsString(),
                        pipe_reg_name, getHierarchicalName());
            }

            // Remove the property so that if there's a stall, forwarding is
            // not attempted again on the next cycle.
            input.deleteProperty("forward2");
        }
    }
    
    
    /**
     * Convenience function that automatically fetches input latch data
     * and sends output latch data.
     */
    @Override
    public void evaluate() {
        ICpuCore core = getCore();
        if (my_cycle_number == core.getCycleNumber()) return;
//        Logger.out.println("Running evaluate for " + getName());

        input_doing = new HashSet<>();
        output_doing = new HashSet<>();
        clearStatus();
        clearActivity();
        setResourceWait(null);
        
        int num_inputs = 0;
        int num_outputs = 0;

        if (input_regs != null) {
            num_inputs = input_regs.size();
        }
        if (output_regs != null) {
            num_outputs = output_regs.size();
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

            if (!stageWaitingOnResource() && output.canAcceptWork()) {
                input.consume();
                output.write();
            }            
        } else {
            compute();
        }
        
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
        
        my_cycle_number = core.getCycleNumber();
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
     * - consume/consume inputs that are actually used (theInputLatch.consume()).
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
        my_cycle_number = 0;
        currently_doing = null;
        resource_wait = null;
        input_doing = null;
        output_doing = null;
        status_words = null;
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
    
//    public PipelineStageBase(IModule parent, String name, IPipeReg input, IPipeReg output) {
//        super(parent, name);
//        addInputRegister(input);
//        addOutputRegister(output);
//    }
    public PipelineStageBase(IModule parent, String name) {
        super(parent, name);
    }
        
    
//    @Override
//    public void printHierarchy(int depth, IModule first) {
//        IModule myparent = getParent();
//        //IModule grandparent = (myparent==null) ? null : (myparent.getParent());
//        
//        StringBuilder sb = new StringBuilder();
//        for (int i=0; i<depth; i++) {
//            sb.append("   ");
//        }
//        
//        int num_inputs = input_regs.size();
//        sb.append('(');
//        for (int i=0; i<num_inputs; i++) {
//            
//        }
//        
//    }
    
    
    private String parentheticalList(List<IPipeReg> list, IModule myparent) {
        if (list == null) return "()";
        
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        int count = list.size();
        for (int i=0; i<count; i++) {
            IPipeReg in = list.get(i);
            String name;
            //if (in.getParent() != myparent) {
                name = in.getHierarchicalName();
            //} else {
            //    name = in.getLocalName();
            //}
            
            if (i>0) sb.append(", ");
            sb.append(name);
        }
        sb.append(')');
        return sb.toString();
    }
    
    public String[] connectionsToStringArr() {
        String[] cols = new String[3];
        IModule myparent = getParent();
        
        cols[0] = parentheticalList(input_regs, myparent);
        cols[1] = getHierarchicalName();
        cols[2] = parentheticalList(output_regs, myparent);
        
        return cols;
    }
    
    

    
}
