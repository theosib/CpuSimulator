/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package interfaces;

import tools.CpuCore;

/**
 *
 * @author millerti
 */
public abstract class PipelineStageBase<IRT, ORT> {
    protected CpuCore core;
    protected PipelineRegister<IRT> input_reg;
    protected PipelineRegister<ORT> output_reg;
    
    public void compute() {
        IRT input = input_reg.read();
        ORT output = output_reg.newLatch();
        compute(input, output);
        output_reg.write(output);
    }
    
    public abstract void compute(IRT input, ORT output);
    public void reset() {}
    
    public PipelineStageBase(CpuCore core, PipelineRegister input, PipelineRegister output) {
        this.core = core;
        this.input_reg = input;
        this.output_reg = output;
    }
}
