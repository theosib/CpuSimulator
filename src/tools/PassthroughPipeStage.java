/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import baseclasses.Latch;
import baseclasses.PipelineStageBase;
import utilitytypes.ICpuCore;
import utilitytypes.IModule;

/**
 * When implementing multi-stage functional units, this class is useful
 * for adding extra pipeline stages that do nothing but add cycle delay.
 * 
 * @author millerti
 */
public class PassthroughPipeStage extends PipelineStageBase {

    public PassthroughPipeStage(IModule parent, String name) {
        super(parent, name);
    }
    
    @Override
    public void compute(Latch input, Latch output) {
        if (input.isNull()) return;
        output.setInstruction(input.getInstruction());
        output.copyAllPropertiesFrom(input);
    }   
}
