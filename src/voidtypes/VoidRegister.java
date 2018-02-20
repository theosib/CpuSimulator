/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voidtypes;

import baseclasses.LatchBase;
import baseclasses.PipelineRegister;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author millerti
 */
public class VoidRegister extends PipelineRegister<VoidLatch> {
    private static VoidRegister singleton = null;
    static {
        try {
            singleton = new VoidRegister();
        } catch (Exception ex) {
            System.err.println("Exception creating VoidRegister singleton: " + ex);
            System.exit(0);
        }
    }
    
    static public VoidRegister getVoidRegister() {
        return singleton;
    }
    
    private VoidRegister() throws Exception {
        super(VoidLatch.class);
    }
    
    @Override
    public boolean isMasterBubble() { return true; }
    @Override
    public boolean isSlaveStalled() { return false; }
    
    @Override
    public VoidLatch read() {
        return VoidLatch.getVoidLatch();
    }
        
    @Override
    public void setMasterBubble(boolean s) { }
    @Override
    public void setSlaveStall(boolean s) { }
    
    @Override
    public void write(VoidLatch output) { }
    
    @Override
    public void advanceClock() { }
    
    @Override
    public void reset() { }

    @Override
    public VoidLatch newLatch() {
        return VoidLatch.getVoidLatch();
    }
}
