/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voidtypes;

import baseclasses.ComponentBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import utilitytypes.ICpuCore;
import utilitytypes.IPipeReg;
import utilitytypes.IPipeStage;

/**
 *
 * @author millerti
 */
public class VoidCore extends VoidModule implements ICpuCore {
    public static final List<IPipeStage> stagelist = Collections.unmodifiableList(new ArrayList<IPipeStage>());
    private static final VoidCore singleton = new VoidCore();
    public static VoidCore getVoidCore() { return singleton; }
    protected VoidCore() {}

    @Override
    public void advanceClock() {}

    @Override
    public void stageTopologicalSort(IPipeStage first_stage) {}

    @Override
    public List<IPipeStage> getStageComputeOrder() { return stagelist; }

    @Override
    public int getResultRegister(String pipe_reg_name) { return -1; }

    @Override
    public int getResultValue(String pipe_reg_name) { return 0; }

    @Override
    public IPipeReg.EnumForwardingStatus matchForwardingRegister(String pipe_reg_name, int regnum) {
        return IPipeReg.EnumForwardingStatus.NULL;
    }

    @Override
    public Set<String> getForwardingSources() {
        return VoidRegister.proplist;
    }

    @Override
    public Set<String> getForwardingTargets() {
        return VoidRegister.proplist;
    }

    @Override
    public void computeFlattenedPipeRegMap() {}

    @Override
    public IPipeStage getFirstStage() {
        return VoidStage.getVoidStage();
    }

    @Override
    public void printHierarchy() {}
    
    @Override
    public String getShortName() {
        return "core";
    }

    @Override
    public boolean isResultFloat(String pipe_reg_name) { return false; }

    @Override
    public List<IPipeStage> getStagePrintOrder() { return stagelist; }
    
}
