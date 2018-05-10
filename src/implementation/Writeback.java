/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package implementation;

import baseclasses.CpuCore;
import baseclasses.InstructionBase;
import baseclasses.Latch;
import baseclasses.PipelineStageBase;
import java.util.ArrayList;
import java.util.List;
import utilitytypes.EnumOpcode;
import utilitytypes.ICpuCore;
import utilitytypes.IGlobals;
import utilitytypes.IProperties;
import utilitytypes.IRegFile;
import utilitytypes.Logger;
import utilitytypes.Operand;

/*** Writeback Stage ***/
class Writeback extends PipelineStageBase {
    
    public Writeback(CpuCore core) {
        super(core, "Writeback");
    }

    @Override
    public void compute() {
        // Things to get from globals:
        // - CPU run state
        // - PRF (type IRegFile)
        // - ROB (type InstructionBase[])
        
        // Loop over all input ports, and for each input....
        
        // Skip over any inputs without valid instruction
        
        int ROB_PRF_index = ins.getReorderBufferIndex();
        
        // Update ROB entry to latest state of instruction.
        // (The entry is originally filled with the freshly renamed instruction.
        // Once it reaches Writeback, it now has all of its inputs and any
        // computed result value.)
        
        // call core.incCompleted()
        
        // If the run state is RUN_STATE_FLUSH *and* core.numCompleted()==core.numIssued())
        // then change CPU run state to RUN_STATE_RECOVERY.
        
        // If a completing instruction needs writeback, copy the result value from
        // the instruction to the PRF entry.  For diagnostic purposes, also 
        // mark the PRF entry as being a FLOAT if the result is a float.
                int value = input.getResultValue();
                boolean isfloat = input.isResultFloat();
        
        // If the completing instruction DOES NOT need writeback, simply mark
        // the PRF entry as valid.
                
        // If the CPU run state is RUN_STATE_RUNNING and the completing instruction
        // has a fault, then mark its PRF entry as having a fault.
        // (Or you can just rely on the fact that updating the ROB entry
        // also carries with it fault information.)
        // Change the run state to RUN_STATE_FAULT.
            
        // Don't forget to consume the input.

        
        // Set the activity string to indicate all instructions just completed
    }
    
}
