/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package interfaces;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author millerti
 */
public class PipelineRegister<LatchType> {
    private LatchType master, slave;
    private Class<LatchType> latchclass;
    
    public LatchType read() {
        return slave;
    }
    
    public void write(LatchType output) {
        master = output;
    }
    
    public void advanceClock() {
        slave = master;
        master = null;
    }
    
    public void reset() {
        master = null;
        slave = null;
    }
    
    public PipelineRegister(Class latchclass) {
        this.latchclass = latchclass;
    }
    
    public LatchType newLatch() {
        try {
            return latchclass.newInstance();
        } catch (Exception ex) {
            System.err.println("Creating pipeline latch error: " + ex);
        }
        return null;
    }
}
