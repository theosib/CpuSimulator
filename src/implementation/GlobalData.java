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
import utilitytypes.IRegFile;
import utilitytypes.RegisterFile;

/**
 * As a design choice, some data elements that are accessed by multiple
 * pipeline stages are stored in a common object.
 * 
 * @author 
 */
public class GlobalData extends PropertiesContainer implements IGlobals {
    protected InstructionSequence program;
    

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
        this.setProperty(MAIN_MEMORY, new int[1024]);
        this.setProperty("running", false);
        this.setProperty("program_counter_takenbranch", (int)0);
        this.setProperty("branch_state_fetch", BRANCH_STATE_NULL);
        this.setProperty("branch_state_decode", BRANCH_STATE_NULL);
        
        IRegFile regfile = new RegisterFile(32);
	this.setProperty("reg_file", regfile);
    }

    /**
     * Fetches the specified property by name, returning it as a RegisterFile.
     * Throws exception on type mismatch.
     * 
     * @param name
     * @return value
     */
    @Override
    public IRegFile getPropertyRegisterFile(String name) {
        if (properties == null) return null;
        
        Object p = properties.get(name);
        if (p == null) return null;
        
        if (p instanceof RegisterFile) {
            return (RegisterFile)p;
        } else {
            throw new java.lang.ClassCastException("Property " + name + 
                    " cannot be converted from " +
                    p.getClass().getName() + " to RegisterFile.");
        }
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

    @Override
    public IRegFile getRegisterFile() {
        return getPropertyRegisterFile("reg_file");
    }
    
    public void advanceClock() {
        super.advanceClock();
        getRegisterFile().advanceClock();
    }
}
