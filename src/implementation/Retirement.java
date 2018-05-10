/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package implementation;

import baseclasses.InstructionBase;
import baseclasses.Latch;
import baseclasses.PipelineStageBase;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import utilitytypes.ClockedIntArray;
import utilitytypes.EnumOpcode;
import utilitytypes.ICpuCore;
import utilitytypes.IGlobals;
import utilitytypes.IModule;
import utilitytypes.IPipeStage;
import utilitytypes.IProperties;
import static utilitytypes.IProperties.REGISTER_ALIAS_TABLE;
import utilitytypes.IRegFile;
import utilitytypes.Logger;
import utilitytypes.RegisterFile;

/**
 *
 * @author millerti
 */
public class Retirement extends PipelineStageBase {

    public Retirement(IModule parent) {
        super(parent, "Retirement");
        disableTwoInputCompute();
    }
    

    @Override
    public void compute() {
        List<String> doing = new ArrayList<String>();
        ICpuCore core = getCore();
        IGlobals globals = core.getGlobals();
        
        /*
        From Globals, you will need to get the following:
        - Reorder buffer (of type InstructionBase[])
        - PRF (of type IRegFile)
        - ARF (of type IRegFile)
        - RAT (of type ClockedIntArray)
        - CPU run state
        - ROB head pointer
        - Possibly also ROB tail pointer and/or used entry count
        */
        

        // In RUN_STATE_FLUSH, reset ROB head to same as tail and 
        // anything else necessary to mark the ROB as empty.
        // Also, clear all RAT entries to -1 (indicating that arch regs
        // map to the ARF).
        
        // Starting from the ROB head, loop until you either have processed
        // all entries or encounter an entry whose PRF register is not marked
        // valid.  Your code should retire ALL completed entries at the head
        // of the ROB....
        
        // In this loop:
        // This is where you do the actual printing for OUT and FOUT.
        
        // The HALT instruction puts the processor into the RUN_STATE_HALTING
        //   state, where the only thing that is allowed to happen is for any
        //   remaining uncommitted stores to retire.  The LSQ (or rather the
        //   DCache) is what will change the run state to RUN_STATE_HALTED
        //   once all STOREs are retired.
        
        // If the instruction at the head of the queue has a fault:
        // - Set the run state to RUN_STATE_FLUSH
        // - Set the global property RECOVERY_PC to that of the faulting instruction
        // - Set the global property RECOVERY_TAKEN to the CORRECT resolution of the branch.
            
        // Add retired instructions to a set so that the LSQ can tell which
        // STOREs need to be retired.
        
        // For diagnostic purposes, mark the PRF entry corresonding to the
        // ROB head as retired.
        // If the instruction being retired needs a writeback, copy the
        // result value from the PRF to the ARF.
        // If the RAT entry for this arch register points to the head of the
        // ROB, set the RAT entry to -1 to indicate that the only instance
        // of this arch reg has been retired.
        // If the instruction being retired does NOT need a writeback,
        // take no special action on the RAT or ARF.
        
        // Increment the head pointer, wrapping around the end of the ROB.
        // Optionally, update the used-entry count.

        // Quit the loop of the new head instruction is not completed.
        
        // Use core.putRetiredSet() to provide the set of instructions retired
        // this cycle.
        
        // Set the activity string with the contents of ROB.
    }
    
}
