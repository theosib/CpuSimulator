/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package implementation;

import interfaces.IGlobals;
import tools.InstructionSequence;

/**
 *
 * @author millerti
 */
public class GlobalData implements IGlobals {
    public InstructionSequence program;
    public int program_counter = 0;
    public int[] register_file = new int[32];

    @Override
    public void reset() {
        program_counter = 0;
        register_file = new int[32];
    }
}
