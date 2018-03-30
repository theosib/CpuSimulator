/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package implementation;

import tools.MyALU;
import utilitytypes.EnumOpcode;
import baseclasses.InstructionBase;
import baseclasses.PipelineRegister;
import baseclasses.PipelineStageBase;
import voidtypes.VoidLatch;
import baseclasses.CpuCore;
import baseclasses.Latch;
import cpusimulator.CpuSimulator;
import static utilitytypes.EnumOpcode.*;
import utilitytypes.ICpuCore;
import utilitytypes.IGlobals;
import utilitytypes.IPipeReg;
import static utilitytypes.IProperties.*;
import utilitytypes.Operand;

/**
 * The AllMyStages class merely collects together all of the pipeline stage 
 * classes into one place.  You are free to split them out into top-level
 * classes.
 * 
 * Each inner class here implements the logic for a pipeline stage.
 * 
 * It is recommended that the compute methods be idempotent.  This means
 * that if compute is called multiple times in a clock cycle, it should
 * compute the same output for the same input.
 * 
 * How might we make updating the program counter idempotent?
 * 
 * @author
 */
public class AllMyStages {
    /*** Fetch Stage ***/
    static class Fetch extends PipelineStageBase {
        public Fetch(ICpuCore core) {
            super(core, "Fetch");
        }
        
        // Does this state have an instruction it wants to send to the next
        // stage?  Note that this is computed only for display and debugging
        // purposes.
        boolean has_work;
                
        /** 
         * For Fetch, this method only has diagnostic value.  However, 
         * stageHasWorkToDo is very important for other stages.
         * 
         * @return Status of Fetch, indicating that it has fetched an 
         *         instruction that needs to be sent to Decode.
         */
        @Override
        public boolean stageHasWorkToDo() {
            return has_work;
        }
        
        
        @Override
        public String getStatus() {
            IGlobals globals = (GlobalData)core.getGlobals();
            if (globals.getPropertyInteger("current_branch_state") == GlobalData.BRANCH_STATE_WAITING) {
                addStatusWord("ResolveWait");
            }
            return super.getStatus();
        }

        @Override
        public void compute(Latch input, Latch output) {
            IGlobals globals = (GlobalData)core.getGlobals();
            
            // Get the PC and fetch the instruction
            int pc = globals.getPropertyInteger(PROGRAM_COUNTER);
            InstructionBase ins = globals.getInstructionAt(pc);
            
            // Initialize this status flag to assume a stall or bubble condition
            // by default.
            has_work = false;
            
            // If the instruction is NULL (like we ran off the end of the
            // program), just return.  However, for diagnostic purposes,
            // we make sure something meaningful appears when 
            // CpuSimulator.printStagesEveryCycle is set to true.
            if (ins.isNull()) {
                // Fetch is working on no instruction at no address
                setActivity("----: NULL");
            }
            
            // Compute the value of the next program counter, to be committed
            // in advanceClock depending on stall states.  This makes 
            // computing the next PC idempotent.  
            globals.setProperty("next_program_counter_nobranch", pc + 1);
            
            // Since there is no input pipeline register, we have to inform
            // the diagnostic helper code explicitly what instruction Fetch
            // is working on.
            has_work = true;
            setActivity(ins.toString());
            
            // If the instruction is a branch, request that the branch wait
            // state be set.  This will be committed in Fetch.advanceClock
            // if Decode isn't stalled.  This too is idempotent.
            if (ins.getOpcode().isBranch()) {
                globals.setProperty("next_branch_state_fetch", GlobalData.BRANCH_STATE_WAITING);
            }
            
            // Send the fetched instruction to the output pipeline register.
            // PipelineRegister.advanceClock will ignore this if 
            // Decode is stalled, and Fetch.compute will keep setting the
            // output instruction to the same thing over and over again.
            // In the stall case Fetch.advanceClock will not change the program 
            // counter, nor will it commit globals.next_branch_state_fetch to 
            // globals.branch_state_fetch.
            
            if (output.canAcceptWork()) {
                output.setInstruction(ins);
                
                if (globals.getPropertyInteger("current_branch_state") == GlobalData.BRANCH_STATE_WAITING) {
                    // If we're currently waiting for a branch resolution...
                    
                    
                    // See if the Decode stage has provided a resolution
                    int branch_state_decode = globals.getPropertyInteger("branch_state_decode");
                    if (branch_state_decode != GlobalData.BRANCH_STATE_NULL) {
                        
                        // Take action based on the resolution.
                        switch (branch_state_decode) {
                            
                            // If Decode resolves that the branch is to be taken...
                            case GlobalData.BRANCH_STATE_TAKEN:     
                                // Set the PC to the branch target
                                globals.setProperty(PROGRAM_COUNTER, 
                                        globals.getPropertyInteger("next_program_counter_takenbranch"));
                                break;

                            // If Decode resolves that the branch is no to be taken...
                            case GlobalData.BRANCH_STATE_NOT_TAKEN:
                                // Set the PC to the address immediately after the branch
                                globals.setProperty(PROGRAM_COUNTER, 
                                        globals.getPropertyInteger("next_program_counter_nobranch"));
                                break;
                                
                        }
                        
                        // Clear the stall state for Fetch
                        globals.setProperty("current_branch_state", GlobalData.BRANCH_STATE_NULL);
                    }                    
                } else {
                    // If we've not been waiting on a branch resolution...
                    
                    int next_branch_state_fetch = globals.getPropertyInteger("next_branch_state_fetch");
                    if (next_branch_state_fetch != GlobalData.BRANCH_STATE_NULL) {
                        // If Fetch wants to change its stall state...
                        
                        // Commit the new state
                        globals.setProperty("current_branch_state", next_branch_state_fetch);
                        
                        // Clear the signal to change the state
                        globals.setProperty("next_branch_state_fetch", GlobalData.BRANCH_STATE_NULL);
                    }

                    // When entering a branch wait state, go ahead and advance
                    // the program counter to fetch the fall-through instruction.
                    // This way, if the branch is not taken, we have already fetched
                    // the next instruction and save a cycle.
                    globals.setProperty(PROGRAM_COUNTER, 
                            globals.getPropertyInteger("next_program_counter_nobranch"));
                }                
            }
        }
    }

    
    /*** Decode Stage ***/
    static class Decode extends PipelineStageBase {
        public Decode(ICpuCore core) {
            super(core, "Decode");
        }
        
        
        // When a branch is taken, we have to squash the next instruction
        // sent in by Fetch, because it is the fall-through that we don't
        // want to execute.  This flag is set only for status reporting purposes.
        boolean squashing_instruction = false;
        

        @Override
        public String getStatus() {
            IGlobals globals = (GlobalData)core.getGlobals();
            String s = super.getStatus();
            if (squashing_instruction) {
                s = "Squashing";
            }
            return s;
        }
        

//        private static final String[] fwd_regs = {"ExecuteToWriteback", 
//            "MemoryToWriteback"};
        
        @Override
        public void compute() {
            // Since this stage has multiple outputs, must read input(s) 
            // explicitly
            Latch input = this.readInput(0).duplicate();
            InstructionBase ins = input.getInstruction();

            // Default to no squashing.
            squashing_instruction = false;
            
            if (ins.isNull()) return;
            
            setActivity(ins.toString());

            IGlobals globals = (GlobalData)core.getGlobals();
            if (globals.getPropertyInteger("branch_state_decode") == GlobalData.BRANCH_STATE_TAKEN) {
                // Drop the fall-through instruction.
                squashing_instruction = true;
                setActivity("----: NULL");
                globals.setProperty("branch_state_decode", GlobalData.BRANCH_STATE_NULL);
                
                // Squashing the fall-through instruction is "consuming" it, so we
                // mustn't forget to consume it.
                input.consume();
                return;
            }
            
            
            EnumOpcode opcode = ins.getOpcode();
            Operand oper0 = ins.getOper0();
            boolean[] reginvalid = globals.getPropertyBooleanArray(REGISTER_INVALID);
            
            // This code is to prevent having more than one of the same regster
            // as a destiation register in the pipeline at the same time.
            if (opcode.needsWriteback()) {
                int oper0reg = oper0.getRegisterNumber();
                if (reginvalid[oper0reg]) {
//                    System.out.println("Stall because dest R" + oper0reg + " is invalid");
                    setResourceStall(true);
                    return;
                }
            }
            
            // See what operands can be fetched from the register file
            registerFileLookup(input);
            
            // See what operands can be fetched by forwarding
            forwardingSearch(input);
            
            Operand src1  = ins.getSrc1();
            Operand src2  = ins.getSrc2();
            
            
            boolean take_branch = false;
            int value0 = 0;
            int value1 = 0;
            
            switch (opcode) {
                case BRA:
                    if (!oper0.hasValue()) {
                        // If we do not already have a value for the branch
                        // condition register, must stall.
//                        System.out.println("Stall BRA wants oper0 R" + oper0.getRegisterNumber());
                        this.setResourceStall(true);
                        // Nothing else to do.  Bail out.
                        return;
                    }
                    value0 = oper0.getValue();
                    
                    // The CMP instruction just sets its destination to
                    // (src1-src2).  The result of that is in oper0 for the
                    // BRA instruction.  See comment in MyALU.java.
                    switch (ins.getComparison()) {
                        case EQ:
                            take_branch = (value0 == 0);
                            break;
                        case NE:
                            take_branch = (value0 != 0);
                            break;
                        case GT:
                            take_branch = (value0 > 0);
                            break;
                        case GE:
                            take_branch = (value0 >= 0);
                            break;
                        case LT:
                            take_branch = (value0 < 0);
                            break;
                        case LE:
                            take_branch = (value0 <= 0);
                            break;
                    }
                    
                    if (take_branch) {
                        // If the branch is taken, send a signal to Fetch
                        // that specifies the branch target address, via
                        // "globals.next_program_counter_takenbranch".  
                        // If the label is valid, then use its address.  
                        // Otherwise, the target address will be found in 
                        // src1.
                        if (ins.getLabelTarget().isNull()) {
                            // If branching to address in register, make sure
                            // operand is valid.
                            if (!src1.hasValue()) {
//                                System.out.println("Stall BRA wants src1 R" + src1.getRegisterNumber());
                                this.setResourceStall(true);
                                // Nothing else to do.  Bail out.
                                return;
                            }
                            
                            value1 = src1.getValue();
                        } else {
                            value1 = ins.getLabelTarget().getAddress();
                        }
                        globals.setProperty("next_program_counter_takenbranch", value1);
                        
                        // Send a signal to Fetch, indicating that the branch
                        // is resolved taken.  This will be picked up by
                        // Fetch.advanceClock on the same clock cycle.
                        globals.setProperty("branch_state_decode", GlobalData.BRANCH_STATE_TAKEN);
//                        System.out.println("Resolving branch taken");
                    } else {
                        // Send a signal to Fetch, indicating that the branch
                        // is resolved not taken.
                        globals.setProperty("branch_state_decode", GlobalData.BRANCH_STATE_NOT_TAKEN);
//                        System.out.println("Resolving branch not taken");
                    }
                    
                    // Having completed execution of the BRA instruction, we must
                    // explicitly indicate that it has been consumed.
                    input.consume();
                    // All done; return.
                    return;
                    
                case JMP:
                    // JMP is an inconditionally taken branch.  If the
                    // label is valid, then take its address.  Otherwise
                    // its operand0 contains the target address.
                    if (ins.getLabelTarget().isNull()) {
                        if (!oper0.hasValue()) {
                            // If branching to address in register, make sure
                            // operand is valid.
//                            System.out.println("Stall JMP wants oper0 R" + oper0.getRegisterNumber());
                            this.setResourceStall(true);
                            // Nothing else to do.  Bail out.
                            return;
                        }
                        
                        value0 = oper0.getValue();
                    } else {
                        value0 = ins.getLabelTarget().getAddress();
                    }
                    globals.setProperty("next_program_counter_takenbranch", value0);
                    globals.setProperty("branch_state_decode", GlobalData.BRANCH_STATE_TAKEN);
                    
                    // Having completed execution of the JMP instruction, we must
                    // explicitly indicate that it has been consumed.
                    input.consume();
                    return;
                    
                case CALL:
                    // Not implemented yet
                    input.consume();
                    return;
            }
            
            
            // Allocate an output latch for the output pipeline register
            // appropriate for the type of instruction being processed.
            Latch output;
            if (opcode.accessesMemory()) {
                int output_num = lookupOutput("DecodeToMemory");
                output = this.newOutput(output_num);
            } else {
                int output_num = lookupOutput("DecodeToExecute");
                output = this.newOutput(output_num);
            }
            
            // If the desired output is stalled, then just bail out.
            // No inputs have been claimed, so this will result in a
            // automatic pipeline stall.
            if (!output.canAcceptWork()) return;
            
            
            int[] srcRegs = new int[3];
            // Only want to forward to oper0 if it's a source.
            srcRegs[0] = opcode.oper0IsSource() ? oper0.getRegisterNumber() : -1;
            srcRegs[1] = src1.getRegisterNumber();
            srcRegs[2] = src2.getRegisterNumber();
            Operand[] operArray = {oper0, src1, src2};
            
            // Loop over source operands, looking to see if any can be
            // forwarded to the next stage.
            for (int sn=0; sn<3; sn++) {
                int srcRegNum = srcRegs[sn];
                // Skip any operands that are not register sources
                if (srcRegNum < 0) continue;
                // Skip any that already have values
                if (operArray[sn].hasValue()) continue;
                
                String propname = "forward" + sn;
                if (!input.hasProperty(propname)) {
                    // If any source operand is not available
                    // now or on the next cycle, then stall.
                    this.setResourceStall(true);
                    // Nothing else to do.  Bail out.
                    return;
                }
            }
                
            
            // If we managed to find all source operands, mark the destination
            // register invalid then finish putting data into the output latch 
            // and send it.
            
            // Mark the destination register invalid
            if (opcode.needsWriteback()) {
                int oper0reg = oper0.getRegisterNumber();
                reginvalid[oper0reg] = true;
            }            
            
            // Copy the forward# properties
            output.copyAllPropertiesFrom(input);
            // Copy the instruction
            output.setInstruction(ins);
            // Send the latch data to the next stage
            output.write();
            
            // And don't forget to indicate that the input was consumed!
            input.consume();
        }
    }
    

    /*** Execute Stage ***/
    static class Execute extends PipelineStageBase {
        public Execute(ICpuCore core) {
            super(core, "Execute");
        }

        @Override
        public void compute(Latch input, Latch output) {
            if (input.isNull()) return;
            input = input.duplicate();
            doPostedForwarding(input);
            InstructionBase ins = input.getInstruction();

            int source1 = ins.getSrc1().getValue();
            int source2 = ins.getSrc2().getValue();
            int oper0 =   ins.getOper0().getValue();

            int result = MyALU.execute(ins.getOpcode(), source1, source2, oper0);
                        
            output.setResultValue(result);
            output.setInstruction(ins);
        }
    }
    

    /*** Memory Stage ***/
    static class Memory extends PipelineStageBase {
        public Memory(ICpuCore core) {
            super(core, "Memory");
        }

        @Override
        public void compute(Latch input, Latch output) {
            if (input.isNull()) return;
            input = input.duplicate();
            doPostedForwarding(input);
            InstructionBase ins = input.getInstruction();

            int oper0   = ins.getOper0().getValue();
            int source1 = ins.getSrc1().getValue();
            int source2 = ins.getSrc2().getValue();
            
            // The Memory stage no longer follows Execute.  It is an independent
            // functional unit parallel to Execute.  Therefore we must perform
            // address calculation here.
            int addr = source1 + source2;
            
            int value = 0;
            IGlobals globals = (GlobalData)core.getGlobals();
            int[] memory = globals.getPropertyIntArray(MAIN_MEMORY);

            switch (ins.getOpcode()) {
                case LOAD:
                    // Fetch the value from main memory at the address
                    // retrieved above.
                    value = memory[addr];
                    output.setResultValue(value);
                    output.setInstruction(ins);
                    System.out.println("Memory[" + addr + "] -> " + value);
                    break;
                
                case STORE:
                    // For store, the value to be stored in main memory is
                    // in oper0, which was fetched in Decode.
                    value = oper0;
                    memory[addr] = value;
                    System.out.println("Memory[" + addr + "] <- " + value);
                    return;
                    
                default:
                    throw new RuntimeException("Non-memory instruction got into Memory stage");
            }
        }
    }
    

    /*** Writeback Stage ***/
    static class Writeback extends PipelineStageBase {
        public Writeback(CpuCore core) {
            super(core, "Writeback");
        }

        @Override
        public void compute() {
            IGlobals globals = (GlobalData)core.getGlobals();
            // Get register file and valid flags from globals
            int[] regfile = globals.getPropertyIntArray(REGISTER_FILE);
            boolean[] reginvalid = globals.getPropertyBooleanArray(REGISTER_INVALID);
            
            // Writeback has two inputs, so we just loop over them
            for (int i=0; i<2; i++) {
                // Get the input by index and the instruction it contains
                Latch input = this.readInput(i);
                InstructionBase ins = input.getInstruction();
                // Skip to the next iteration of there is no instruction.
                if (ins.isNull()) continue;
                
                if (ins.getOpcode().needsWriteback()) {
                    // By definition, oper0 is a register and the destination.
                    // Get its register number;
                    int regnum = ins.getOper0().getRegisterNumber();
                    int value = input.getResultValue();

                    if (CpuSimulator.printRegWrite) {
                        System.out.println("Storing " + value + " to R" + regnum);
                    }
                    
                    addStatusWord("R" + regnum + "=" + value);

                    regfile[regnum] = input.getResultValue();
                    reginvalid[regnum] = false;
                }

                if (input.getInstruction().getOpcode() == EnumOpcode.HALT) {
                    globals.setProperty("running", false);
                }
                
                // There are no outputs that could stall, so just consume
                // all valid inputs.
                input.consume();
            }
        }
    }
}
