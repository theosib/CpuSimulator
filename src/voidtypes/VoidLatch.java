/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voidtypes;

import baseclasses.InstructionBase;
import baseclasses.Latch;
import baseclasses.PipelineRegister;
import utilitytypes.IPipeReg;

/**
 *
 * @author millerti
 */
public class VoidLatch extends Latch {
    private static final VoidLatch singleton = new VoidLatch();
    
    public static VoidLatch getVoidLatch() { return singleton; }

    public VoidLatch() {
        super(null);
    }
    
    public InstructionBase getInstruction() { 
        return VoidInstruction.getVoidInstruction(); 
    }
    public void setInstruction(InstructionBase ins) {}

    public boolean isNull() { return true; }
    public void setInvalid() {}
    
    public void writeToParentRegister() {}
    
    public IPipeReg getParentRegister() { return VoidRegister.getVoidRegister(); }
    public void setParentRegister(IPipeReg p) { }
    
    public int getResultRegNum() { return -1; }
    public boolean hasResultValue() { return false; }
    public int getResultValue() { return 0; }
    public void setResultValue(int value) { }

    public String getName() { return "VoidRegister"; }
    
    public boolean isSlaveStalled() { return false; }
    public boolean canAcceptData() { return true; }
    public void write() {}
    public void consume() {}
}
