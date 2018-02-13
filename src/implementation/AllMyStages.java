/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package implementation;

import implementation.AllMyLatches.*;
import interfaces.EnumOpcode;
import interfaces.InstructionBase;
import interfaces.PipelineRegister;
import interfaces.PipelineStageBase;
import tools.CpuCore;

/**
 *
 * @author millerti
 */
public class AllMyStages {
    static class Fetch extends PipelineStageBase<Void,FetchToDecode> {
        public Fetch(CpuCore core, PipelineRegister input, PipelineRegister output) {
            super(core, input, output);
        }

        @Override
        public void compute(Void input, FetchToDecode output) {
            GlobalData globals = (GlobalData)core.getGlobalResources();
            int pc = globals.program_counter;
            InstructionBase ins = globals.program.getInstructionAt(pc);
            
            output.ins = ins;
            
            // Do something with the program counter....
        }
    }
    
    static class Decode extends PipelineStageBase<FetchToDecode,DecodeToExecute> {
        public Decode(CpuCore core, PipelineRegister input, PipelineRegister output) {
            super(core, input, output);
        }

        @Override
        public void compute(FetchToDecode input, DecodeToExecute output) {
            GlobalData globals = (GlobalData)core.getGlobalResources();
            int[] regfile = globals.register_file;
            
            // Do what the decode stage does....
            
            // Fill output with what passes to Execute...
        }
    }
    
    static class Execute extends PipelineStageBase<DecodeToExecute,ExecuteToMemory> {
        public Execute(CpuCore core, PipelineRegister input, PipelineRegister output) {
            super(core, input, output);
        }

        @Override
        public void compute(DecodeToExecute input, ExecuteToMemory output) {
            InstructionBase ins = input.ins;
            int source1 = input.ins.src1.getValue();
            int source2 = input.ins.src2.getValue();
            
            int result = MyALU.execute(ins.opcode, source1, source2);
            
            // Fill outdata with what passes to Memory...
        }
    }
    
    static class Memory extends PipelineStageBase<ExecuteToMemory,MemoryToWriteback> {
        public Memory(CpuCore core, PipelineRegister input, PipelineRegister output) {
            super(core, input, output);
        }

        @Override
        public void compute(ExecuteToMemory input, MemoryToWriteback output) {
            // Access memory...
        }
    }
    
    static class Writeback extends PipelineStageBase<MemoryToWriteback,Void> {
        public Writeback(CpuCore core, PipelineRegister input, PipelineRegister output) {
            super(core, input, output);
        }

        @Override
        public void compute(MemoryToWriteback inout, Void output) {
            // Write back result to register file
        }
    }

    static Class[] getDeclaredClasses() {
        Class self = AllMyLatches.class;
        return self.getDeclaredClasses();
    }
}
