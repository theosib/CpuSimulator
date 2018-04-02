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
    public static final int PC_REGNUM = Integer.MAX_VALUE;

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
        s.is_float = false;
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
        s.is_float = false;
        return s;
    }

    public static Operand newLiteralSource(int value, boolean isfloat) {
        Operand s = new Operand();
        s.register_num = -1;
        s.value = value;
        s.valid_value = true;
        s.is_float = isfloat;
        return s;
    }
    
    /**
     * Creates a new operand with a constant value.
     * 
     * @param value
     * @return
     */
    public static Operand newLiteralSource(float value) {
        Operand s = new Operand();
        s.register_num = -1;
        s.value = Float.floatToRawIntBits(value);
        s.valid_value = true;
        s.is_float = true;
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
    
    public float getFloatValue() {
        return Float.intBitsToFloat(value);
    }
    
    public String getValueAsString() {
        if (!hasValue()) {
            return "??";
        } else if (isFloat()) {
            return Float.toString(Float.intBitsToFloat(value));
        } else {
            return Integer.toString(value);
        }
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
    public void lookUpFromRegisterFile(IRegFile regfile) {
        if (isRegister()) {
            int index = getRegisterNumber();
            setValue(regfile.getValue(index), regfile.isFloat(index));
        }
    }
    
    public void setIntValue(int v) {
        value = v;
        valid_value = true;
        is_float = false;
    }
    
    public void setFloatValue(int v) {
        value = v;
        valid_value = true;
        is_float = true;
    }

    public void setFloatValue(float v) {
        value = Float.floatToRawIntBits(v);
        valid_value = true;
        is_float = true;
    }
    
    public void setValue(int v, boolean isfloat) {
        value = v;
        valid_value = true;
        is_float = isfloat;
    }

    public void markValid(boolean valid) {
        valid_value = valid;
    }
    
    public void markFloat(boolean fl) {
        is_float = fl;
    }
    
    
    public boolean hasValue() {
        return valid_value;
    }
    
    public boolean hasFloatValue() {
        return valid_value && is_float;
    }
    
    public boolean isFloat() {
        return is_float;
    }

    public Operand duplicate() {
        Operand op = new Operand();
        op.register_num = this.register_num;
        op.value        = this.value;
        op.valid_value  = this.valid_value;
        op.is_float     = this.is_float;
        return op;
    }
    
    public boolean isNull() { return !isRegister() && !hasValue(); }
    
    public String getRegisterName() {
        int rn = getRegisterNumber();
        if (rn < 0) {
            return "#";
        } else if (rn == PC_REGNUM) {
            return "PC";
        } else {
            return "R" + rn;
        }
    }
    
    protected int register_num;
    protected int value;
    protected boolean valid_value;
    protected boolean is_float;     // Purely for diagnostic purposes
    protected Operand() {}
    
    @Override
    public String toString() {
        if (isRegister()) {
            return getRegisterName() + "=" + getValueAsString();
        } else{
            return "#" + getValueAsString();
        }
    }
}
