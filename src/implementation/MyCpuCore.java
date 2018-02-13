/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package implementation;

import interfaces.PipelineRegister;
import interfaces.PipelineStageBase;
import tools.CpuCore;
import tools.InstructionSequence;

/**
 *
 * @author millerti
 */
public class MyCpuCore extends CpuCore<GlobalData> {
    private void setup() throws Exception {
        Class[] latchTypes = AllMyLatches.getDeclaredClasses();
        for (Class c : latchTypes) {
            PipelineRegister reg = new PipelineRegister(c);
            registers.add(reg);
        }
        
        int reg_index = 0;
        PipelineRegister prevReg = null;
        PipelineRegister nextReg = this.getPipelineRegisterByIndex(reg_index++);
        
        Class[] stageTypes = AllMyStages.getDeclaredClasses();
        for (Class c : stageTypes) {
            PipelineStageBase stage = 
                    (PipelineStageBase) c.getConstructor(CpuCore.class, PipelineRegister.class, PipelineRegister.class).
                    newInstance(this, prevReg, nextReg);
            
            prevReg = nextReg;
            nextReg = this.getPipelineRegisterByIndex(reg_index++);
        }
    }
    
    public MyCpuCore() throws Exception {
        setup();
    }
    
    public void loadProgram(InstructionSequence program) {
        globals.program = program;
    }
}
