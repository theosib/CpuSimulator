/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cpusimulator;

import java.io.IOException;
import tools.InstructionSequence;

/**
 *
 * @author millerti
 */
public class CpuSimulator {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        InstructionSequence seq = new InstructionSequence();
        seq.loadFile("samples/test1.asm");
        seq.printProgram();
    }
    
}
