/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package interfaces;

/**
 *
 * @author millerti
 */
public class InstructionBase {
    // Metadata for debugging
    public String instruction_string;
    public int pc_address, line_num;
    public String preceding_label;
    
    // Instruction opcode and data fields
    public EnumOpcode opcode;
    public SourceOperand src1, src2;
    public int dst_regnum = -1;
    
    // Comparison for CMP instruction
    public EnumComparison comparison;
    
    // Label target, if any
    public String label_name;
    public int label_addr = -1;
    
    public InstructionBase duplicate() {
        InstructionBase ins = newInstruction();
        
        ins.instruction_string  = this.instruction_string;
        ins.pc_address          = this.pc_address;
        ins.line_num            = this.line_num;
        ins.opcode              = this.opcode;
        ins.src1                = this.src1.duplicate();
        ins.src2                = this.src2.duplicate();
        ins.dst_regnum          = this.dst_regnum;
        ins.comparison          = this.comparison;
        ins.label_addr          = this.label_addr;
        ins.label_name          = this.label_name;
        
        return ins;
    }
    
    public static InstructionBase newInstruction() {
        return new InstructionBase();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append(String.format("%04d: ", pc_address));
        sb.append(opcode.toString());
        
        if (comparison != null) {
            sb.append(' ').append(comparison.toString());
        }
        
        if (dst_regnum >= 0) {
            sb.append(" R").append(dst_regnum);
        }
        
        if (src1 != null) {
            if (src1.isRegister()) {
                sb.append(" R").append(src1.getRegisterNumber());
            } else {
                sb.append(' ').append(src1.getValue());
            }
        }

        if (src2 != null) {
            if (src2.isRegister()) {
                sb.append(" R").append(src2.getRegisterNumber());
            } else {
                sb.append(' ').append(src2.getValue());
            }
        }
        
        if (label_name != null) {
            sb.append(' ').append(label_name);
            if (label_addr >= 0) {
                sb.append(String.format("(%04d)", label_addr));
            } else {
                sb.append("(!ERR)");
            }
        }
        
        return sb.toString();
    }
}
