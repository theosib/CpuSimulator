/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voidtypes;

import baseclasses.InstructionBase;
import baseclasses.LatchBase;

/**
 *
 * @author millerti
 */
public class VoidLatch extends LatchBase {
    private static final VoidLatch singleton = new VoidLatch();
    
    public static VoidLatch getVoidLatch() { return singleton; }
    
    public InstructionBase getInstruction() { 
        return VoidInstruction.getVoidInstruction(); 
    }

    public boolean isNull() { return true; }
}
