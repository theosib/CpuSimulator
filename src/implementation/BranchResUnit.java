/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package implementation;

import baseclasses.InstructionBase;
import baseclasses.InstructionBase.EnumBranch;
import baseclasses.Latch;
import baseclasses.PipelineStageBase;
import utilitytypes.EnumComparison;
import utilitytypes.IModule;
import utilitytypes.IProperties;

/**
 *
 * @author millerti
 */
public class BranchResUnit extends PipelineStageBase {

    public BranchResUnit(IModule parent) {
        super(parent, "BranchResUnit");
    }
    
    static boolean resolveBranch(EnumComparison condition, int value0) {
        // Add code here...
    }

    @Override
    public void compute(Latch input, Latch output) {
        if (input.isNull()) return;
        doPostedForwarding(input);
        InstructionBase ins = input.getInstruction().duplicate();

        /*
        JMP -- pass through
        BRA -- resolve, compare to prediction, set fault on instruction for disagreement
        CALL -- compute return address and pass on as result value.
        */
                
        output.setInstruction(ins);        
    }
    
    
}
