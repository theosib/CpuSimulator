/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilitytypes;

/**
 *
 * @author millerti
 */
public class RegisterFile implements IRegFile {
    protected final int[] values;
    protected final boolean[] invalid;
    protected final boolean[] isfloat;  // Used only for diagnostic purposes
    
    public RegisterFile(int num_registers) {
        values = new int[num_registers];
        invalid = new boolean[num_registers];
        isfloat = new boolean[num_registers];
    }
    
    public boolean isInvalid(int index) { return invalid[index]; }
    public boolean isValid(int index) { return !isInvalid(index); }
    public boolean isFloat(int index) { return isfloat[index]; }
    
    public void setInvalid(int index, boolean inv) { invalid[index] = inv; }
    public void markFloat(int index, boolean is_float) { isfloat[index] = is_float; }
    
    /**
     * This returns the raw register contents regardless of type or validity.
     * @param index
     * @return
     */
    public int getValueUnsafe(int index) { return values[index]; }
    
    public int getValue(int index) { 
        if (isInvalid(index)) {
            throw new RuntimeException("Register R" + index + " is not valid");
        }
        return values[index]; 
    }
    
    public float getValueAsFloat(int index) {
        int ivalue = getValue(index);
        if (!isFloat(index)) {
            throw new RuntimeException("Register R" + index + " is not float");
        }
        return Float.intBitsToFloat(ivalue);
    }
    
    public void setValueUnsafe(int index, int value) {
        values[index] = value;
    }
    
    public void setIntValue(int index, int value) {
        values[index] = value;
        isfloat[index] = false;
        invalid[index] = false;
    }
    
    public void setFloatValue(int index, int value) {
        values[index] = value;
        isfloat[index] = true;
        invalid[index] = false;
    }

    public void setFloatValue(int index, float value) {
        values[index] = Float.floatToRawIntBits(value);
        isfloat[index] = true;
        invalid[index] = false;
    }
    
    public void setValue(int index, int value, boolean is_float) {
        values[index] = value;
        isfloat[index] = is_float;
        invalid[index] = false;
    }

    @Override
    public int numRegisters() {
        return values.length;
    }
}
