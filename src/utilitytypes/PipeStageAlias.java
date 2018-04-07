/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilitytypes;

import baseclasses.ComponentBase;
import baseclasses.Latch;
import java.util.List;

/**
 *
 * @author millerti
 */
public class PipeStageAlias extends ComponentBase implements IPipeStage {
    IPipeStage original;

    public PipeStageAlias(IModule parent, String name, IPipeStage original) {
        super(parent, name);
        this.original = original;
    }
    
    @Override
    public String getHierarchicalName() {
        String mine = super.getHierarchicalName();
        String orig = original.getHierarchicalName();
        return mine + "{" + orig + "}";
    }    

    @Override
    public void reset() {
        original.reset();
    }

    @Override
    public void clearStatus() {
        original.clearStatus();
    }

    @Override
    public void addStatusWord(String word) {
        original.addStatusWord(word);
    }

    @Override
    public void clearActivity() {
        original.clearActivity();
    }

    @Override
    public void setActivity(String act) {
        original.setActivity(act);
    }

    @Override
    public String getActivity() {
        return original.getActivity();
    }

    @Override
    public String getStatus() {
        return original.getStatus();
    }

    @Override
    public void setTopoOrder(int pos) {
        original.setTopoOrder(pos);
    }

    @Override
    public int getTopoOrder() {
        return original.getTopoOrder();
    }

    @Override
    public boolean stageWaitingOnResource() {
        return original.stageWaitingOnResource();
    }

    @Override
    public void setResourceWait(String reason_stalled) {
        original.setResourceWait(reason_stalled);
    }

    @Override
    public String getResourceWaitReason() {
        return original.getResourceWaitReason();
    }

    @Override
    public boolean stageHasWorkToDo() {
        return original.stageHasWorkToDo();
    }

    @Override
    public void evaluate() {
        original.evaluate();
    }

    @Override
    public int lookupInput(String name) {
        return original.lookupInput(name);
    }

    @Override
    public void consumedInput(int input_num) {
        original.consumedInput(input_num);
    }

    @Override
    public Latch readInput(int input_num) {
        return original.readInput(input_num);
    }

    @Override
    public List<IPipeReg> getInputRegisters() {
        return original.getInputRegisters();
    }

    @Override
    public int numInputRegisters() {
        return original.numInputRegisters();
    }

    @Override
    public int lookupOutput(String name) {
        return original.lookupOutput(name);
    }

    @Override
    public boolean outputCanAcceptWork(int out_num) {
        return original.outputCanAcceptWork(out_num);
    }

    @Override
    public Latch newOutput(int out_num) {
        return original.newOutput(out_num);
    }

    @Override
    public Latch invalidOutput(int out_num) {
        return original.invalidOutput(out_num);
    }

    @Override
    public void outputWritten(Latch out, int out_num) {
        original.outputWritten(out, out_num);
    }

    @Override
    public List<IPipeReg> getOutputRegisters() {
        return original.getOutputRegisters();
    }

    @Override
    public int numOutputRegisters() {
        return original.numOutputRegisters();
    }

    @Override
    public void addInputRegister(IPipeReg input_reg) {
        original.addInputRegister(input_reg);
    }

    @Override
    public void addOutputRegister(IPipeReg output_reg) {
        original.addOutputRegister(output_reg);
    }

    @Override
    public void registerFileLookup(Latch input) {
        original.registerFileLookup(input);
    }

    @Override
    public void forwardingSearch(Latch input) {
        original.forwardingSearch(input);
    }

    @Override
    public void doPostedForwarding(Latch input) {
        original.doPostedForwarding(input);
    }

    @Override
    public String[] connectionsToStringArr() {
        return original.connectionsToStringArr();
    }

    @Override
    public IPipeStage getOriginal() { return original.getOriginal(); }

    @Override
    public void setPrintOrder(int pos) {
        original.setPrintOrder(pos);
    }

    @Override
    public int getPrintOrder() {
        return original.getPrintOrder();
    }
    
}
