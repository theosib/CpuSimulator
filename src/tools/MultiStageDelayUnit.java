/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import baseclasses.FunctionalUnitBase;
import baseclasses.ModuleBase;
import baseclasses.PropertiesContainer;
import java.util.Map;
import utilitytypes.ICpuCore;
import utilitytypes.IFunctionalUnit;
import utilitytypes.IModule;
import utilitytypes.IPipeReg;
import utilitytypes.IPipeStage;
import utilitytypes.IProperties;
import voidtypes.VoidProperties;

/**
 *
 * @author millerti
 */
public class MultiStageDelayUnit extends FunctionalUnitBase {
    int delay;
    public MultiStageDelayUnit(IModule parent, String name, int delay) {
        super(parent, name);
        this.delay = delay;
    }

    @Override
    public void createPipelineRegisters() {
        for (int i=0; i<delay; i++) {
            String name = "pr" + i;
            if (i==delay-1) name = "out";
            createPipeReg(name);  
        }
    }

    @Override
    public void createPipelineStages() {
        for (int i=0; i<delay; i++) {
            String name = "ps" + i;
            if (i==0) name = "in";
            addPipeStage(new PassthroughPipeStage(this, name));
        }
    }

    @Override
    public void createChildModules() {}

    @Override
    public void createConnections() {
        String a=null, b=null;
        for (int i=0; i<delay; i++) {
            a = "ps" + i;
            if (i==0) a = "in";
            if (b != null) connect(b, a);
            
            b = "pr" + i;
            if (i==delay-1) b = "out";
            connect(a, b);
        }
    }

    @Override
    public void specifyForwardingSources() { }
}
