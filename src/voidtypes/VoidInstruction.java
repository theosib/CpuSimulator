/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voidtypes;

import utilitytypes.EnumComparison;
import utilitytypes.EnumOpcode;
import baseclasses.InstructionBase;
import utilitytypes.LabelTarget;
import utilitytypes.Operand;

/**
 *
 * @author millerti
 */
public class VoidInstruction extends InstructionBase {
    private static final VoidInstruction singleton = new VoidInstruction();    
    public static VoidInstruction getVoidInstruction() {
        return singleton;
    }
    
    private VoidInstruction() { }
    
    @Override
    public void setInstructionString(String str) { }
    @Override
    public String getInstructionString() { return "NULL"; }
    @Override
    public void setLineNum(int line) { }
    @Override
    public int getLineNum() { return -1; }
    @Override
    public void setPrecedingLabel(String str) { }
    @Override
    public String getPrecedingLabel() { return null; }
    
    @Override
    public void setPCAddress(int pc) { }
    @Override
    public int getPCAddress() { return -1; }

    @Override
    public void setOpcode(EnumOpcode op) { }
    @Override
    public EnumOpcode getOpcode() { return EnumOpcode.NULL; }
    
    @Override
    public void setOper0(Operand op) { }
    @Override
    public Operand getOper0() { return VoidOperand.getVoidOperand(); }
    @Override
    public void setSrc1(Operand op) { }
    @Override
    public void setSrc2(Operand op) {  }
    @Override
    public Operand getSrc1() { return VoidOperand.getVoidOperand(); }
    @Override
    public Operand getSrc2() { return VoidOperand.getVoidOperand(); }

    @Override
    public void setComparison(EnumComparison cmp) { }
    @Override
    public EnumComparison getComparison() { return EnumComparison.NULL; }

    @Override
    public void setLabelTarget(LabelTarget target) { }
    @Override
    public LabelTarget getLabelTarget() { return VoidLabelTarget.getVoidLabelTarget(); }

    @Override
    public VoidInstruction duplicate() {
        return this;
    }
    
    @Override
    public boolean isNull() { return true; }
    
    @Override
    public String toString() {
        return "----: NULL";
    }
}
