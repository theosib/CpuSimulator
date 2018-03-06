/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilitytypes;

import voidtypes.VoidOperand;

/**
 * Contains a decoded source operand as produced by the Fetch stage.
 * If the source is a register, the register contents must be looked up
 * in the Decode stage.
 * 
 * @author millerti
 */
public class Operand {    

    /**
     * Creates a new operand that has a register as a source or target.
     * 
     * @param regnum
     * @return
     */
    public static Operand newRegister(int regnum) {
        Operand s = new Operand();
        s.register_num = regnum;
        s.valid_value = false;
        return s;
    }
    
    /**
     * Creates a new operand with a constant value.
     * 
     * @param value
     * @return
     */
    public static Operand newLiteralSource(int value) {
        Operand s = new Operand();
        s.register_num = -1;
        s.value = value;
        s.valid_value = true;
        return s;
    }
    
    /**
     * Returns an empty operand.
     * 
     * @return
     */
    public static Operand newVoidOperand() {
        return VoidOperand.getVoidOperand();
    }
    
    public boolean isRegister() {
        return (register_num >= 0);
    }
    
    public int getRegisterNumber() {
        return register_num;
    }
    
    public int getValue() {
        return value;
    }
    
    /**
     * Automatically get value of this operand from register file if this
     * operand is a register.  If this is not a register, nothing happens.
     * 
     * IMPORTANT:  This does not handle stall conditions (where 
     * GlobalData.register_invalid[register_num] is true).  You must take
     * care of that properly in Decode.compute(input, output) method and return
     * the stall condition in Decode.stageWaitingOnResource()/
     * @param regfile Register file represented as int array.
     */
    public void lookUpFromRegisterFile(int[] regfile) {
        if (isRegister()) {
            setValue(regfile[getRegisterNumber()]);
        }
    }
    
    public void setValue(int v) {
        value = v;
        valid_value = true;
    }
    
    public boolean hasValue() {
        return valid_value;
    }

    public Operand duplicate() {
        Operand op = new Operand();
        op.register_num = this.register_num;
        op.value        = this.value;
        op.valid_value  = this.valid_value;
        return op;
    }
    
    public boolean isNull() { return !isRegister() && !hasValue(); }
    
    protected int register_num;
    protected int value;
    protected boolean valid_value;
    protected Operand() {}
    
    @Override
    public String toString() {
        if (isRegister()) {
            if (hasValue()) {
                return "R" + getRegisterNumber() + "=" + getValue();
            } else {
                return "R" + getRegisterNumber() + "=UNKNOWN";
            }
        } else{
            return "#" + getValue();
        }
    }
}
