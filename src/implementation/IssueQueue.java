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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import utilitytypes.EnumOpcode;
import utilitytypes.ICpuCore;
import utilitytypes.IModule;
import utilitytypes.IPipeReg;
import utilitytypes.IProperties;
import utilitytypes.Logger;
import utilitytypes.Operand;

/**
 * Some other students had the idea to store the Latch object containing a 
 * dispatched instruction into the IQ.  This allowed them to use the pre-
 * existing doForwardingSearch() method to scan for completing inputs for
 * instructions.  I consider that to be an excellent alternative approach
 * to what I did here.
 * 
 * @author millerti
 */
public class IssueQueue extends PipelineStageBase {
    
    public IssueQueue(IModule parent) {
        super(parent, "IssueQueue");
    }
    
    
    // Data structures...
    
    @Override
    public void compute() {
        // Check run state
        
        // Put non-null input into free IQ entry
        // Don't forget to consume() input.
        
        // Check for forwarding opportunities.
        // Capture desired register inputs available now.
        // Take note of those available next cycle.
        
        // Select an instruction with valid inputs (or ones that will be
        // forwarded next cycle) to be issued to each output port.
        
        // Issue instructions
        // Don't forget to write() outputs.
        
        // Set activity string for diagnostic purposes.  Lines are delimited
        // by newline ('\n').
    }

    
}
