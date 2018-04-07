/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilitytypes;

import baseclasses.ComponentBase;
import baseclasses.Latch;
import java.util.Set;

/**
 *
 * @author millerti
 */
public class PipeRegAlias extends ComponentBase implements IPipeReg {
    IPipeReg original;

    public PipeRegAlias(IModule parent, String name, IPipeReg original) {
        super(parent, name);
        this.original = original;
    }
    
    @Override
    public IPipeReg getOriginal() { return original.getOriginal(); }

    @Override
    public String getHierarchicalName() {
        String mine = super.getHierarchicalName();
        String orig = original.getHierarchicalName();
        return mine + "{" + orig + "}";
    }
    
    @Override
    public void writeBubble() {
        original.writeBubble();
    }

    @Override
    public boolean isMasterBubble() {
        return original.isMasterBubble();
    }

    @Override
    public void setSlaveStall(boolean stalled) {
        original.setSlaveStall(stalled);
    }

    @Override
    public boolean canAcceptWork() {
        return original.canAcceptWork();
    }

    @Override
    public Latch read() {
        return original.read();
    }

    @Override
    public void consumeSlave() {
        original.consumeSlave();
    }

    @Override
    public Latch readNextCycle() {
        return original.readNextCycle();
    }

    @Override
    public void write(Latch output) {
        original.write(output);
    }

    @Override
    public void advanceClock() {
        original.advanceClock();
    }

    @Override
    public Latch newLatch() {
        return original.newLatch();
    }

    @Override
    public Latch invalidLatch() {
        return original.invalidLatch();
    }

    @Override
    public int getResultRegister() {
        return original.getResultRegister();
    }

    @Override
    public EnumForwardingStatus matchForwardingRegister(int regnum) {
        return original.matchForwardingRegister(regnum);
    }

    @Override
    public int getResultValue() {
        return original.getResultValue();
    }

    @Override
    public boolean isResultFloat() {
        return original.isResultFloat();
    }

    @Override
    public void reset() {
        original.reset();
    }

    @Override
    public void setPropertiesList(Set<String> pl) {
        original.setPropertiesList(pl);
    }

    @Override
    public Set<String> getPropertiesList() {
        return original.getPropertiesList();
    }

    @Override
    public void setStageBefore(IPipeStage s) {
        original.setStageBefore(s);
    }

    @Override
    public void setStageAfter(IPipeStage s) {
        original.setStageAfter(s);
    }

    @Override
    public IPipeStage getStageBefore() {
        return original.getStageBefore();
    }

    @Override
    public IPipeStage getStageAfter() {
        return original.getStageAfter();
    }

    @Override
    public void setIndexInBefore(int ix) {
        original.setIndexInBefore(ix);
    }

    @Override
    public void setIndexInAfter(int ix) {
        original.setIndexInAfter(ix);
    }

    @Override
    public int getIndexInBefore() {
        return original.getIndexInBefore();
    }

    @Override
    public int getIndexInAfter() {
        return original.getIndexInAfter();
    }
}
