/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voidtypes;

import baseclasses.ComponentBase;
import baseclasses.Latch;
import baseclasses.PipelineRegister;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import utilitytypes.ICpuCore;
import utilitytypes.IModule;
import utilitytypes.IPipeReg;
import utilitytypes.IPipeStage;

/**
 *
 * @author millerti
 */
public class VoidRegister extends VoidComponent implements IPipeReg {
    private static VoidRegister singleton = new VoidRegister();
    static public VoidRegister getVoidRegister() {
        return singleton;
    }
    private VoidRegister() {}
    
    @Override
    public boolean isMasterBubble() { return true; }
    @Override
    public boolean canAcceptWork() { return true; }
    
    @Override
    public VoidLatch read() {
        return VoidLatch.getVoidLatch();
    }
        
    @Override
    public void writeBubble() { }
    @Override
    public void setSlaveStall(boolean s) { }
    
    @Override
    public void write(Latch output) { }
    
    @Override
    public void advanceClock() { }

    @Override
    public VoidLatch newLatch() {
        return VoidLatch.getVoidLatch();
    }

    @Override
    public void setPropertiesList(Set<String> pl) {}

    public static final Set<String> proplist = Collections.unmodifiableSet(new HashSet<String>());
    
    @Override
    public Set<String> getPropertiesList() { return proplist; }

    @Override
    public void setStageBefore(IPipeStage s) {}

    @Override
    public void setStageAfter(IPipeStage s) {}

    @Override
    public IPipeStage getStageBefore() { 
        return VoidStage.getVoidStage();
    }

    @Override
    public IPipeStage getStageAfter() { 
        return VoidStage.getVoidStage();
    }

    @Override
    public void setIndexInBefore(int ix) {}

    @Override
    public void setIndexInAfter(int ix) {}

    @Override
    public int getIndexInBefore() { return 0; }

    @Override
    public int getIndexInAfter() { return 0; }

    @Override
    public Latch invalidLatch() {
        return VoidLatch.getVoidLatch();
    }

    @Override
    public void consumeSlave() {}

    @Override
    public Latch readNextCycle() {
        return VoidLatch.getVoidLatch();
    }

    @Override
    public int getResultRegister() {
        return -1;
    }

    @Override
    public EnumForwardingStatus matchForwardingRegister(int regnum) {
        return EnumForwardingStatus.NULL;
    }

    @Override
    public int getResultValue() {
        return 0;
    }

    
    @Override
    public String getShortName() {
        return ComponentBase.computeOnlyCaps(getLocalName(), "r");
    }    

    @Override
    public boolean isResultFloat() { return false; }
}
