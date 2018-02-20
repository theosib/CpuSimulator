/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package implementation;

import baseclasses.InstructionBase;
import baseclasses.LatchBase;

/**
 * Definitions of latch contents for pipeline registers.  Pipeline registers
 * create instances of these for passing data between pipeline stages.
 * 
 * AllMyLatches is merely to collect all of these classes into one place.
 * It is not necessary for you to do it this way.
 * 
 * You must fill in each latch type with the kind of data that passes between
 * pipeline stages.
 * 
 * @author 
 */
public class AllMyLatches {
    public static class FetchToDecode extends LatchBase {
        // LatchBase already includes a field for the instruction.
    }
    
    public static class DecodeToExecute extends LatchBase {
        // LatchBase already includes a field for the instruction.
        // What else do you need here?
    }
    
    public static class ExecuteToMemory extends LatchBase {
        // LatchBase already includes a field for the instruction.
        // What do you need here?
    }

    public static class MemoryToWriteback extends LatchBase {
        // LatchBase already includes a field for the instruction.
        // What do you need here?
    }    
}
