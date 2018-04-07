/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voidtypes;

import baseclasses.ComponentBase;
import baseclasses.Latch;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import utilitytypes.IPipeReg;
import utilitytypes.IPipeStage;

/**
 *
 * @author millerti
 */
public class VoidStage extends VoidComponent implements IPipeStage {
    private static final List<IPipeReg> reglist = Collections.unmodifiableList(new ArrayList<IPipeReg>());
    private static final VoidStage singleton = new VoidStage();
    public static VoidStage getVoidStage() { return singleton; }
    protected VoidStage() {}

    @Override
    public void clearStatus() {}

    @Override
    public void addStatusWord(String word) {}

    @Override
    public void clearActivity() {}

    @Override
    public void setActivity(String act) {}

    @Override
    public String getActivity() { return null; }

    @Override
    public String getStatus() { return ""; }

    @Override
    public void setTopoOrder(int pos) { }

    @Override
    public int getTopoOrder() { return 0; }

    @Override
    public boolean stageWaitingOnResource() { return false; }

    @Override
    public void setResourceWait(String reason_stalled) { }

    @Override
    public String getResourceWaitReason() { return null; }

    @Override
    public boolean stageHasWorkToDo() { return false; }

    @Override
    public void evaluate() { }

    @Override
    public int lookupInput(String name) { return 0; }

    @Override
    public void consumedInput(int input_num) { }

    @Override
    public Latch readInput(int input_num) { return VoidLatch.getVoidLatch(); }

    @Override
    public List<IPipeReg> getInputRegisters() { return reglist; }

    @Override
    public int numInputRegisters() { return 0; }

    @Override
    public int lookupOutput(String name) { return 0; }

    @Override
    public boolean outputCanAcceptWork(int out_num) { return true; }

    @Override
    public Latch newOutput(int out_num) {
        return VoidLatch.getVoidLatch();
    }

    @Override
    public Latch invalidOutput(int out_num) {
        return VoidLatch.getVoidLatch();
    }

    @Override
    public void outputWritten(Latch out, int out_num) {}

    @Override
    public List<IPipeReg> getOutputRegisters() { return reglist; }

    @Override
    public int numOutputRegisters() { return 0; }

    @Override
    public void addInputRegister(IPipeReg input_reg) {}

    @Override
    public void addOutputRegister(IPipeReg output_reg) {}

    @Override
    public void registerFileLookup(Latch input) {}

    @Override
    public void forwardingSearch(Latch input) {}

    @Override
    public void doPostedForwarding(Latch input) {}

    
    @Override
    public String[] connectionsToStringArr() {
        String[] stringarr = {"", "", ""};
        return stringarr;
    }

    @Override
    public String getShortName() {
        return ComponentBase.computeOnlyCaps(getLocalName(), "s");
    }

    @Override
    public void setPrintOrder(int pos) {}

    @Override
    public int getPrintOrder() { return -1; }    
}
