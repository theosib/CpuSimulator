/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cpusimulator;

import implementation.MyCpuCore;
import java.io.IOException;
import tools.InstructionSequence;
import utilitytypes.Logger;

/**
 *
 * @author millerti
 */
public class CpuSimulator {
    
    public static boolean printStagesEveryCycle = true;
    public static boolean printForwarding = true;
    public static boolean printRegWrite = true;
    public static boolean printPropertyUpdates = false;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, Exception {
        InstructionSequence seq = new InstructionSequence();
        seq.loadFile("samples/irrational.asm");
        seq.printProgram();
        Logger.out.println();
        
        MyCpuCore core = new MyCpuCore();
        core.loadProgram(seq);
        core.runProgram();
    }    
}
