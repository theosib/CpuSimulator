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
        
    /**
     * Create a new pipeline register, with the given list of default
     * property names, and add it to the map of pipeline registers.
     * 
     * @param name Name of new pipeline register
     * @param props String array of property names
     */
    private void createPipeReg(String name, String[] props) {
        IPipeReg pr = new PipelineRegister(this, name, props);
        this.addPipeReg(pr);
    }

    /**
     * Create a new pipeline register with no default property names and
     * add it to the map of pipeline registers.
     * 
     * @param name Name of new pipeline register
     */
    private void createPipeReg(String name) {
        IPipeReg pr = new PipelineRegister(this, name);
        this.addPipeReg(pr);
    }

    /**
     * Configure the pipeline and all of its connections
     */
    private void setup() {
        // Create some pipeline registers.
        createPipeReg("FetchToDecode");
        createPipeReg("DecodeToExecute");
        createPipeReg("DecodeToMemory");
        createPipeReg("ExecuteToWriteback");
        createPipeReg("MemoryToWriteback");
                
        // Instantiate pipeline stages
        addPipeStage(new AllMyStages.Fetch(this));
        addPipeStage(new AllMyStages.Decode(this));
        addPipeStage(new AllMyStages.Execute(this));
        addPipeStage(new AllMyStages.Memory(this));
        addPipeStage(new AllMyStages.Writeback(this));
        
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
        
        // Given the connections created above, compute the optimal
        // order in which to evaluate each pipeline stage.
        stageTopologicalSort(getPipeStage("Fetch"));
        
        globals = new GlobalData();
    }
    
    public MyCpuCore() throws Exception {
        setup();
    }
    
    public void loadProgram(InstructionSequence program) {
        globals.loadProgram(program);
    }
    
    public void runProgram() {
        globals.setProperty("running", true);
        while (globals.getPropertyBoolean("running")) {
            advanceClock();
        }
    }

    @Override
    public void resetGlobals() {
        globals = new GlobalData();
    }
}
