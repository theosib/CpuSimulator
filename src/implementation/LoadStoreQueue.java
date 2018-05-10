/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package implementation;

import baseclasses.InstructionBase;
import baseclasses.Latch;
import baseclasses.PipelineStageBase;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import utilitytypes.EnumOpcode;
import utilitytypes.ICpuCore;
import utilitytypes.IGlobals;
import utilitytypes.IModule;
import utilitytypes.IPipeReg;
import utilitytypes.IProperties;
import utilitytypes.Logger;
import utilitytypes.Operand;

/**
 *
 * @author millerti
 */
public class LoadStoreQueue extends PipelineStageBase {
    
    public LoadStoreQueue(IModule parent) {
        super(parent, "LoadStoreQueue");
        
        // Force PipelineStageBase to use the zero-operand compute() method.
        this.disableTwoInputCompute();
    }
    
    
    // Data structures
    
    public void compute() {        
        // First, mark any retired STORE instructions
        
        // Check CPU run state
        
        // If the LSQ is full and a memory instruction wants to come in from
        // Decode, optionally see if you can proactively free up a retired STORE.
        
        // See if there's room to storea an instruction received from Decode.
        // Read the input latch.  If it's valid, put the instruction into the 
        // LAST slot.
        // LSQ entries are stored in order from oldest to newest.          

        // Loop over the forwarding sources, capturing all values available
        // right now and storing them into LSQ entries needing those
        // register values.
        
        
        // For diagnostic purposes, iterate existing entries of the LSQ and add 
        // their current state to the activity list
        
        // Next, try to do things that require writing to the
        // next pipeline stage.  If we have already written the output, or
        // the output can't accept work, might as well bail out.
        if (wrote_output || !outputCanAcceptWork(0)) {
            setActivity(...);
            return;
        }
        
        // If and only if there is a LOAD in the FIRST entry of the LSQ, it can be
        // issued to the DCache if needed inputs (to compute the address)
        // will be available net cycle.  
        // Set appropriate "forward#" properties on output latch.
        // Only when a LOAD is not prededed by STOREs can be be issued with
        // forwarding in the next cycle.
        // ******
        // When issuing any LOAD, other entries in the LSQ must be shifted
        // to fill the gap.  The LSQ must maintain program order.  This applies
        // to all cases where the LSQ issues a LOAD.
        // ******
        
        // If we issued a load, bail out.  ** Before bailing out, ALWAYS make sure
        // that setActivity has been called with info about all the 
        // instructons in the queue.  This is for diagnostic purposes. **
                
        // Look for a load whose address matches that of a store earlier in the list.
        // Since we don't do speculative loads, if a store is encountered with an
        // unknown address, then no subsequent loads can be issued.
        
        // Outer loop:  Iterate over all LOAD instructions from first to last
        // LSQ entries.
        // Inner loop:  Iterate backwards over STOREs that came before the LOAD.
        // If you find a STORE with a unknown address, skip to the next LOAD
        // in the outer loop.
        
        // If you find a STORE with a matching address, make sure the STORE
        // has a data value. If it does, this LOAD can be ussued as a BYPASS LOAD:
        // copy the value from the STORE to the LOAD and issue the load, 
        // instructing the DCache to NOT fetch from memory.
        // If the STORE does not have a data value, skip to the next LOAD in 
        // the outer loop.  
        // Data that is forwarded from a STORE to a LOAD must some from the
        // matching STORE that is NEAREST to the LOAD in the list.
        
        // If the inner loop finishes and finds neither a matching STORE addresss
        // nor an unknown store address, this LOAD can be issued as an ACCESS
        // LOAD:  The data is fetched from main memory.
                
        // If we issued a LOAD, set activity string, bail out

        // If we find no LOADs to process, see if there are any STORES to ISSUE.
        // An issuable store has known address and data.  To the DCache,
        // an ISSUE STORE passes through to Writeback without modifying memory.  (It will
        // modify memory later on retirement.)  Also, stores that are issued
        // are NOT REMOVED from the LSQ.  Simply mark them as completed.
        
        // If we issued a STORE, set activity string, bail out
        
        // Finally, see if there is a STORE that can be COMMITTED (retired).
        // Only the FIRST entry of the LSQ can be retired (to maintain 
        // program order).
        // To the DCache, a COMMIT STORE writes its data value to memory
        // but is NOT passed on to Writeback.
        
        // Set activity string, return        
        
        // NOTE:
        // ***
        // Whenever you issue any instruction, be sure to call core.incIssued();
        // This is also the case for the IssueQueue.
        // ***
    }
}
