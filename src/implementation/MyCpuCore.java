/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package implementation;

import baseclasses.PipelineRegister;
import baseclasses.PipelineStageBase;
import baseclasses.CpuCore;
import tools.InstructionSequence;
import utilitytypes.IPipeReg;
import utilitytypes.IPipeStage;
import static utilitytypes.IProperties.*;
import voidtypes.VoidRegister;

/**
 * This is an example of a class that builds a specific CPU simulator out of
 * pipeline stages and pipeline registers.
 * 
 * @author 
 */
public class MyCpuCore extends CpuCore {
    static final String[] producer_props = {RESULT_VALUE};
        
    public void initProperties() {
        properties = new GlobalData();
    }
    
    public void loadProgram(InstructionSequence program) {
        getGlobals().loadProgram(program);
    }
    
    public void runProgram() {
        properties.setProperty("running", true);
        while (properties.getPropertyBoolean("running")) {
            System.out.println("Cycle number: " + cycle_number);
            advanceClock();
        }
    }

    @Override
    public void createPipelineRegisters() {
        createPipeReg("FetchToDecode");
        createPipeReg("DecodeToExecute");
        createPipeReg("DecodeToMemory");
        createPipeReg("ExecuteToWriteback");
        createPipeReg("MemoryToWriteback");
    }

    @Override
    public void createPipelineStages() {
        addPipeStage(new AllMyStages.Fetch(this));
        addPipeStage(new AllMyStages.Decode(this));
        addPipeStage(new AllMyStages.Execute(this));
        addPipeStage(new AllMyStages.Memory(this));
        addPipeStage(new AllMyStages.Writeback(this));
    }

    @Override
    public void createChildModules() {
    }

    @Override
    public void createConnections() {
        // Connect pipeline elements by name.  Notice that 
        // Decode has two outputs, anle to send to either Memory OR Execute 
        // and that Writeback has two inputs, able to receive from both
        // Execute and Memory.  
        // Memory no longer connects to Execute.  It is now a fully 
        // independent functional unit, parallel to Execute.
        connect("Fetch", "FetchToDecode");
        connect("FetchToDecode", "Decode");
        connect("Decode", "DecodeToExecute");
        connect("Decode", "DecodeToMemory");
        connect("DecodeToExecute", "Execute");
        connect("DecodeToMemory", "Memory");
        connect("Execute", "ExecuteToWriteback");
        connect("Memory", "MemoryToWriteback");
        connect("ExecuteToWriteback", "Writeback");
        connect("MemoryToWriteback", "Writeback");
    }

    @Override
    public void specifyForwardingSources() {
        addForwardingSource("ExecuteToWriteback");
        addForwardingSource("MemoryToWriteback");
    }

    @Override
    public void specifyForwardingTargets() {
        addForwardingTarget("DecodeToMemory");
        addForwardingTarget("DecodeToWriteback");
    }

    @Override
    public IPipeStage getFirstStage() {
        return this.getPipeStage("Fetch");
    }
    
    public MyCpuCore() {
        initModule();
    }
}
