/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package examples;

import baseclasses.InstructionBase;
import baseclasses.Latch;
import baseclasses.PipelineStageBase;
import utilitytypes.IModule;

/**
 * This is an example of a pipeline stage 
 * 
 * @author millerti
 */
public class OneInOneOut extends PipelineStageBase {
    public OneInOneOut(IModule parent, String name) {
        super(parent, name);
    }
    
    
    private int getSomeExternalResource(int src1, int src2) {
        // Returning -1 means we couldn't get the resource
        return -1;
    }
    
    private int computeSomeResult(int src1, int src2, int external) {
        // This is some computed result
        return 0;
    }
    
    @Override
    public void compute(Latch input, Latch output) {
        if (input.isNull()) return;
        
        // In this example, this pipeline stage needs forwarding.
        // Forwarding must modify the original latch so as to acquire
        // the forwarded values while they are still available.
        doPostedForwarding(input);
        
        // Other modifications made to the input latch content might need to
        // be discarded if there is a stall.  In that case, make a duplicate
        // of the latch.  This example doesn't require duplicating the latch.
        // input = input.duplicate();
        
        InstructionBase ins = input.getInstruction();
        
        int src1 = ins.getSrc1().getValue();
        int src2 = ins.getSrc2().getValue();
        
        int external = getSomeExternalResource(src1, src2);
        if (external < 0) {
            setResourceWait("resourcename");
            return;
        }
        
        int result = computeSomeResult(src1, src2, external);
        
        output.setInstruction(input.getInstruction());
        output.copyAllPropertiesFrom(input);
        output.setResultValue(result);
    }   
    
}
