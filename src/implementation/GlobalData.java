/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package implementation;

import baseclasses.InstructionBase;
import baseclasses.PropertiesContainer;
import java.util.Map;
import java.util.Set;
import utilitytypes.IGlobals;
import tools.InstructionSequence;
import utilitytypes.IProperties;

/**
 * As a design choice, some data elements that are accessed by multiple
 * pipeline stages are stored in a common object.
 * 
 * @author 
 */
public class GlobalData extends PropertiesContainer implements IGlobals {
    public InstructionSequence program;

    @Override
    public void reset() {
        setup();
    }
    
    public static final int BRANCH_STATE_NULL = 0;
    public static final int BRANCH_STATE_WAITING = 1;
    public static final int BRANCH_STATE_TAKEN = 2;
    public static final int BRANCH_STATE_NOT_TAKEN = 3;


    @Override
    public void setup() {
        this.setProperty(PROGRAM_COUNTER, (int)0);
        this.setProperty(REGISTER_FILE, new int[32]);
        this.setProperty(REGISTER_INVALID, new boolean[32]);
        this.setProperty(MAIN_MEMORY, new int[1024]);
        this.setProperty("running", false);
        this.setProperty("next_program_counter_nobranch", (int)0);
        this.setProperty("next_program_counter_takenbranch", (int)0);
        this.setProperty("current_branch_state", BRANCH_STATE_NULL);
        this.setProperty("next_branch_state_fetch", BRANCH_STATE_NULL);
        this.setProperty("branch_state_decode", BRANCH_STATE_NULL);
    }

    @Override
    public InstructionBase getInstructionAt(int pc_address) {
        return program.getInstructionAt(pc_address);
    }

    @Override
    public void loadProgram(InstructionSequence seq) {
        program = seq;
    }
    
    public GlobalData() {
        setup();
    }
}
