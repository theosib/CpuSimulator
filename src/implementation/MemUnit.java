/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package implementation;

import baseclasses.FunctionalUnitBase;
import baseclasses.InstructionBase;
import baseclasses.Latch;
import baseclasses.PipelineStageBase;
import tools.MultiStageDelayUnit;
import utilitytypes.ICpuCore;
import utilitytypes.IFunctionalUnit;
import utilitytypes.IGlobals;
import utilitytypes.IModule;
import utilitytypes.IProperties;
import static utilitytypes.IProperties.MAIN_MEMORY;
import utilitytypes.Operand;

/**
 *
 * @author millerti
 */
public class MemUnit extends FunctionalUnitBase {
    
    
    public static final int ACTION_ACCESS = 1;
    public static final int ACTION_BYPASS = 2;
    public static final int ACTION_ISSUE = 3;
    public static final int ACTION_COMMIT = 4;

    public MemUnit(IModule parent) {
        super(parent, "MemUnit");
    }

        
    private static class DCache1 extends PipelineStageBase {
        public DCache1(IModule parent) {
            super(parent, "DCache1");
        }
        
        @Override
        public void compute(Latch input, Latch output) {
            if (input.isNull()) return;
            doPostedForwarding(input);
            
            // Compute address for next stage, pass through
            // as property on output latch.  Don't call that property
            // "result".
            // Also the type of LOAD or STORE is the kind of thing you would
            // want to pass by using a property on the Latch.  Copy that
            // property also from input to output.
        }
    }
        
    static class DCache2 extends PipelineStageBase {
        public DCache2(IModule parent) {
            super(parent, "DCache2");
        }

        @Override
        public void compute(Latch input, Latch output) {
            if (input.isNull()) return;
            InstructionBase ins = input.getInstruction();
            
            /*
            ACCESS STORE -- Fetch data from memory
            BYPASS STORE -- Data already forwarded from STORE in LSO.  Pass through WITHOUT accessing memory.
            ISSUE STORE  -- Pass through to Writeback WITHOUT accessing memory.
            COMMIT STORE -- Write data to memory.  DO NOT pass through to Writeback.
            */
        }
    }
    
    
    @Override
    public void createPipelineRegisters() {
        createPipeReg("LSQToDCache1");
        createPipeReg("DCache1ToDCache2");
        createPipeReg("out");
    }

    @Override
    public void createPipelineStages() {
        addPipeStage(new LoadStoreQueue(this));
        addPipeStage(new DCache1(this));
        addPipeStage(new DCache2(this));
    }

    @Override
    public void createChildModules() {
    }

    @Override
    public void createConnections() {
        addStageAlias("LoadStoreQueue", "in");
        connect("LoadStoreQueue", "LSQToDCache1", "DCache1");
        connect("DCache1", "DCache1ToDCache2", "DCache2");
        connect("DCache2", "out");
    }

    @Override
    public void specifyForwardingSources() {
        addForwardingSource("out");
    }    
}
