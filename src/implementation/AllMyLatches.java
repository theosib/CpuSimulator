/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package implementation;

import interfaces.InstructionBase;

/**
 *
 * @author millerti
 */
public class AllMyLatches {
    public static class FetchToDecode {
        InstructionBase ins;
    }
    
    public static class DecodeToExecute {
        InstructionBase ins;
        
        // What else do you need here?
    }
    
    public static class ExecuteToMemory {
        // What do you need here?
    }

    public static class MemoryToWriteback {
        // What do you need here?
    }
    
    static Class[] getDeclaredClasses() {
        Class self = AllMyLatches.class;
        return self.getDeclaredClasses();
    }
}
