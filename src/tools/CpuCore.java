/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import interfaces.ICpuCore;
import interfaces.IGlobals;
import interfaces.PipelineRegister;
import interfaces.PipelineStageBase;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author millerti
 * @param <GlobalsType>
 */
public abstract class CpuCore<GlobalsType extends IGlobals> implements ICpuCore<GlobalsType> {
    protected GlobalsType globals;
    protected List<PipelineStageBase> stages      = new ArrayList<>();
    protected List<PipelineRegister> registers    = new ArrayList<>();
    
    protected PipelineRegister getPipelineRegisterByIndex(int ix) {
        if (ix >= registers.size()) {
            return null;
        } else {
            return registers.get(ix);
        }
    }
    
    @Override
    public GlobalsType getGlobalResources() {
        return globals;
    }
    
    @Override
    public void advanceClock() {
        for (PipelineStageBase stage : stages) {
            stage.compute();
        }
        for (PipelineRegister reg : registers) {
            reg.advanceClock();
        }
    }    
    
    @Override
    public void reset() {
        for (PipelineStageBase stage : stages) {
            stage.reset();
        }
        for (PipelineRegister reg : registers) {
            reg.reset();
        }
        globals.reset();
    }
}

