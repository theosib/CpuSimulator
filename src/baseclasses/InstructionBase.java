/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package baseclasses;

import utilitytypes.Operand;
import utilitytypes.LabelTarget;
import utilitytypes.EnumComparison;
import utilitytypes.EnumOpcode;
import voidtypes.VoidLabelTarget;
import voidtypes.VoidOperand;

/**
 *
 * @author millerti
 */
public class InstructionBase {
    // Metadata, useful for debugging
    private String instruction_string = "NULL";
    private int line_num = -1;
    private String preceding_label = null;
    
    public void setInstructionString(String str) { instruction_string = str; }
    public String getInstructionString() { return instruction_string; }
    public void setLineNum(int line) { line_num = line; }
    public int getLineNum() { return line_num; }
    public void setPrecedingLabel(String str) { preceding_label = str; }
    public String getPrecedingLabel() { return preceding_label; }
    
    
    // Program address of this instruction.
    private int pc_address = -1;
    
    public void setPCAddress(int pc) { pc_address = pc; }
    public int getPCAddress() { return pc_address; }
    
    
    // Instruction opcode -- operation that will be performed
    private EnumOpcode opcode = EnumOpcode.NULL;
    
    public void setOpcode(EnumOpcode op) { opcode = op; }
    public EnumOpcode getOpcode() { return opcode; }

    
    // The first operand is usually a destination, except in the case of
    // the STORE instruction and a few others, where this is a source.  When 
    // this is a destination, all we want is the register number.
    // (HINT: it does no harm if Decode blindly treats this as a source and 
    // looks up an "old" value that we don't use).
    private Operand oper0 = VoidOperand.getVoidOperand();
    
    public void setOper0(Operand op) { oper0 = op; }
    public Operand getOper0() { return oper0; }
    
    // Most instructions have two sources, which are the second and third
    // operands
    private Operand src1 = VoidOperand.getVoidOperand();
    private Operand src2 = VoidOperand.getVoidOperand();
    
    public void setSrc1(Operand op) { src1 = op; }
    public void setSrc2(Operand op) { src2 = op; }
    public Operand getSrc1() { return src1; }
    public Operand getSrc2() { return src2; }

    
    // Comparison for CMP instruction
    private EnumComparison comparison = EnumComparison.NULL;
    
    public void setComparison(EnumComparison cmp) { comparison = cmp; }
    public EnumComparison getComparison() { return comparison; }
    
    
    // Branch target label, if any
    private LabelTarget label_target = VoidLabelTarget.getVoidLabelTarget();
    
    public LabelTarget getLabelTarget() { return label_target; }
    public void setLabelTarget(LabelTarget target) { label_target = target; }
    
    /**
     * Check to see if this is a NOT valid instruction
     * @return
     */
    public boolean isNull() { 
        return getOpcode() == EnumOpcode.NULL || pc_address < 0;
    }
    
    /**
     * Check to see if this IS a valid instruction
     * @return
     */
    public boolean isValid() {
        return !isNull();
    }
    
    
    public InstructionBase duplicate() {
        InstructionBase ins = newInstruction();
        
        ins.instruction_string  = this.instruction_string;
        ins.pc_address          = this.pc_address;
        ins.line_num            = this.line_num;
        ins.opcode              = this.opcode;
        ins.src1                = this.src1.duplicate();
        ins.src2                = this.src2.duplicate();
        ins.oper0               = this.oper0.duplicate();
        
        ins.comparison          = this.comparison;
        ins.label_target        = this.label_target.duplicate();
        
        return ins;
    }
    
    public static InstructionBase newInstruction() {
        return new InstructionBase();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        if (pc_address >= 0) {
            sb.append(String.format("%04d: ", pc_address));
        } else {
            sb.append("----: ");
        }
        sb.append(opcode.toString());
        
        if (comparison != EnumComparison.NULL) {
            sb.append(' ').append(comparison.toString());
        }
        
        if (!oper0.isNull()) {
            if (oper0.isRegister()) {
                sb.append(" R").append(oper0.getRegisterNumber());
            } else {
                sb.append(' ').append(oper0.getValue());
            }
        }
        
        if (!src1.isNull()) {
            if (src1.isRegister()) {
                sb.append(" R").append(src1.getRegisterNumber());
            } else {
                sb.append(' ').append(src1.getValue());
            }
        }

        if (!src2.isNull()) {
            if (src2.isRegister()) {
                sb.append(" R").append(src2.getRegisterNumber());
            } else {
                sb.append(' ').append(src2.getValue());
            }
        }
        
        if (!label_target.isNull()) {
            sb.append(' ').append(label_target.getName());
            if (label_target.getAddress() >= 0) {
                sb.append(String.format("(%04d)", label_target.getAddress()));
            } else {
                sb.append("(!ERR)");
            }
        }
        
        return sb.toString();
    }
}
