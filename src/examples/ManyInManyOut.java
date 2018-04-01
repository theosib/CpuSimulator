/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package examples;

import baseclasses.Latch;
import baseclasses.PipelineStageBase;
import java.util.List;
import utilitytypes.IModule;
import utilitytypes.IPipeReg;

/**
 * This example module implements a crossbar.  It supports any number
 * of input registers and any number of output registers.  When an input is
 * read, properties are checked to determine which output it is supposed
 * to be sent to.  When the specified output is stalled, then the 
 * corresponding input is not consumed, causing it to stall as well.
 * 
 * @author millerti
 */
public class ManyInManyOut extends PipelineStageBase {
    public ManyInManyOut(IModule parent, String name) {
        super(parent, name);
    }
    
    private static final int default_output_num = 0;
    
    /**
     * This version of the pipeline stage "logic" checks the output stall
     * condition before allocating an output latch.
     */
    @Override
    public void compute() {
        int num_inputs = numInputRegisters();
        
        for (int inum=0; inum<num_inputs; inum++) {
            // Read the slave latch from input number inum
            Latch ilatch = readInput(inum);
            
            // Skip to the next input if we got a bubble
            if (ilatch.isNull()) continue;
            
            // If this stage was a forwarding target, perform forwarding on
            // the original latch while forwarding source still valid.
            // doPostedForwarding(ilatch);            
            
            // Find out which output this is supposed to go to.
            int onum = -1;
            if (ilatch.hasProperty("output_num")) {
                onum = ilatch.getPropertyInteger("output_num");
            } else if (ilatch.hasProperty("output_name")) {
                String oname = ilatch.getPropertyString("output_name");
                onum = lookupOutput(oname);
            }
            
            // If no output was specified, we'll send it through a
            // default output.
            if (onum < 0) onum = default_output_num;
            
            // If the desired output is stalled, skip to the next input.
            // By not consuming the input, the input will stall and will
            // still be available on the next cycle to be sent if the 
            // desired output becomes able to accept work.
            if (!outputCanAcceptWork(onum)) continue;
            
            // Get an output latch for the selected output
            Latch olatch = this.newOutput(onum);
            
            // Copy the instruction and properties from input to output
            olatch.setInstruction(ilatch.getInstruction());
            olatch.copyAllPropertiesFrom(ilatch);
            
            // Commit the output latch to the output register's master latch.
            // When advanceClock is called on the pipeline register, it will
            // be copied to the slave latch so that it can be consumed by the
            // next stage.
            olatch.write();
            
            // Inform the input register that the concents of its slave latch
            // have been consumed, which allows that input to accept new work
            // from the previous stage.
            ilatch.consume();
        }
    }
    
    
    
    /**
     * This minor variant allocates an output latch before checking if 
     * the corresponding register is stalled.  It demonstrates how an
     * output latch remembers which register it belongs to, and the latch
     * can be queried for output stall.
     */
    public void compute_alternative() {
        int num_inputs = numInputRegisters();
        
        for (int inum=0; inum<num_inputs; inum++) {
            // Read the slave latch from input number inum
            Latch ilatch = readInput(inum);
            
            // Skip to the next input if we got a bubble
            if (ilatch.isNull()) continue;

            // If this stage was a forwarding target, perform forwarding on
            // the original latch while forwarding source still valid.
            // doPostedForwarding(ilatch);
            
            // Find out which output this is supposed to go to.
            int onum = -1;
            if (ilatch.hasProperty("output_num")) {
                onum = ilatch.getPropertyInteger("output_num");
            } else if (ilatch.hasProperty("output_name")) {
                String oname = ilatch.getPropertyString("output_name");
                onum = lookupOutput(oname);
            }
            
            // If no output was specified, we'll send it through a
            // default output.
            if (onum < 0) onum = default_output_num;
            
            // Get an output latch for the selected output
            Latch olatch = this.newOutput(onum);
            
            // If the desired output is stalled, skip to the next input.
            // By not consuming the input, the input will stall and will
            // still be available on the next cycle to be sent if the 
            // desired output becomes able to accept work.
            // By not committing the output latch, it will just get 
            // garbage-collected.
            if (!olatch.canAcceptWork()) continue;
            
            // Copy the instruction and properties from input to output
            olatch.setInstruction(ilatch.getInstruction());
            olatch.copyAllPropertiesFrom(ilatch);
            
            // Commit the output latch to the output register's master latch.
            // When advanceClock is called on the pipeline register, it will
            // be copied to the slave latch so that it can be consumed by the
            // next stage.
            olatch.write();
            
            // Inform the input register that the concents of its slave latch
            // have been consumed, which allows that input to accept new work
            // from the previous stage.
            ilatch.consume();
        }
    }
    
    
}
