/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package examples;

import baseclasses.FunctionalUnitBase;
import baseclasses.InstructionBase;
import baseclasses.Latch;
import baseclasses.ModuleBase;
import baseclasses.PipelineStageBase;
import baseclasses.PropertiesContainer;
import java.util.Map;
import tools.MultiStageDelayUnit;
import tools.MyALU;
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
public class MultiStageFunctionalUnit extends FunctionalUnitBase {
    public MultiStageFunctionalUnit(IModule parent, String name) {
        super(parent, name);
    }

    private static class MyMathUnit extends PipelineStageBase {
        public MyMathUnit(IModule parent) {
            // For simplicity, we just call this stage "in".
            super(parent, "in");
//            super(parent, "in:Math");  // this would be fine too
        }
        
        @Override
        public void compute(Latch input, Latch output) {
            if (input.isNull()) return;
            doPostedForwarding(input);
            InstructionBase ins = input.getInstruction();

            int source1 = ins.getSrc1().getValue();
            int source2 = ins.getSrc2().getValue();

            int result = source1 * source2;
                        
            output.setResultValue(result);
            output.setInstruction(ins);
        }
    }
    
    @Override
    public void createPipelineRegisters() {
        createPipeReg("MathToDelay");  
    }

    @Override
    public void createPipelineStages() {
        addPipeStage(new MyMathUnit(this));
    }

    @Override
    public void createChildModules() {
        IFunctionalUnit child = new MultiStageDelayUnit(this, "Delay", 3);
        addChildUnit(child);
    }

    @Override
    public void createConnections() {
        addRegAlias("Delay.out", "out");
        connect("in", "MathToDelay", "Delay");
    }

    @Override
    public void specifyForwardingSources() {
        addForwardingSource("out");
    }
}
