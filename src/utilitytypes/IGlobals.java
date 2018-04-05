/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilitytypes;

import baseclasses.InstructionBase;
import tools.InstructionSequence;

/**
 * Interface for the class that contains any global data used by the 
 * CPU.
 * 
 * @author millerti
 */
public interface IGlobals extends IProperties {

    /**
     * Restore this properties container to initial conditions
     */
    public default void reset() {
        if (numProperties() == 0) return;
        clear();
        setup();
    }

    /**
     * 
     */
    public void setup();
    public InstructionBase getInstructionAt(int pc_address);
    public void loadProgram(InstructionSequence seq);
    public IRegFile getRegisterFile();
    public void advanceClock();
}
