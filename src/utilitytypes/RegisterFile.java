/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilitytypes;

import cpusimulator.CpuSimulator;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author millerti
 */
public class RegisterFile implements IRegFile {
    protected final int[] values;
    protected final boolean[] invalid;
    protected final boolean[] isfloat;  // Used only for diagnostic purposes
    
    protected static final int SET_VALUE = 1;
    protected static final int SET_INVALID = 2;
    protected static final int SET_FLOAT = 4;
    protected static final int SET_ALL = 7;

    @Override
    public void setRegisterImmediately(int index, int value, boolean is_invalid, boolean is_float) {
        values[index] = value;
        invalid[index] = is_invalid;
        isfloat[index] = is_float;
    }
    
    protected static class RegUpdate {
        public final int index;
        public final int flags;
        public final int value;
        public final boolean invalid;
        public final boolean isfloat;
        public RegUpdate(int ix, int val, boolean inv, boolean isf, int fl) {
            index = ix;
            flags = fl;
            value = val;
            invalid = inv;
            isfloat = isf;
        }
    }
    
    protected List<RegUpdate> regUpdates = new ArrayList<>();
    
    
    public RegisterFile(int num_registers) {
        values = new int[num_registers];
        invalid = new boolean[num_registers];
        isfloat = new boolean[num_registers];
    }
    
    public boolean isInvalid(int index) { return invalid[index]; }
    public boolean isValid(int index) { return !isInvalid(index); }
    public boolean isFloat(int index) { return isfloat[index]; }
    
    public void setInvalid(int index, boolean inv) { 
        regUpdates.add(new RegUpdate(index, 0, inv, false, SET_INVALID));
    }
    public void markFloat(int index, boolean is_float) { 
        regUpdates.add(new RegUpdate(index, 0, false, is_float, SET_FLOAT));
    }
    
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
        regUpdates.add(new RegUpdate(index, value, false, false, SET_VALUE));
    }
    
    public void setIntValue(int index, int value) {
        regUpdates.add(new RegUpdate(index, value, false, false, SET_ALL));
    }
    
    public void setFloatValue(int index, int value) {
        regUpdates.add(new RegUpdate(index, value, false, true, SET_ALL));
    }

    public void setFloatValue(int index, float value) {
        int ival = Float.floatToRawIntBits(value);
        regUpdates.add(new RegUpdate(index, ival, false, true, SET_ALL));
    }
    
    public void setValue(int index, int value, boolean is_float) {
        regUpdates.add(new RegUpdate(index, value, false, is_float, SET_ALL));
    }

    @Override
    public int numRegisters() {
        return values.length;
    }

    @Override
    public void advanceClock() {
        StringBuilder sb = null;
        if (CpuSimulator.printRegWrite && regUpdates.size() > 0) {
            Logger.out.println("# Applying " + regUpdates.size() + " register updates:");
        }
        for (RegUpdate upd : regUpdates) {
            if (CpuSimulator.printRegWrite) {
                sb = new StringBuilder();
                sb.append("   R").append(upd.index).append(':');
            }

            if ((upd.flags & SET_FLOAT) != 0) {
                isfloat[upd.index] = upd.isfloat;
            }
            
            if ((upd.flags & SET_VALUE) != 0) {
                values[upd.index] = upd.value;
                if (CpuSimulator.printRegWrite) {
                    sb.append(" VALUE=");
                    if (isfloat[upd.index]) {
                        sb.append(Float.intBitsToFloat(upd.value));
                    } else {
                        sb.append(upd.value);
                    }
                }
            }
            if ((upd.flags & SET_INVALID) != 0) {
                invalid[upd.index] = upd.invalid;
                if (CpuSimulator.printRegWrite) {
                    if (upd.invalid) {
                        sb.append(" INVALID");
                    } else {
                        sb.append(" VALID");
                    }
                }
            }
            if ((upd.flags & SET_FLOAT) != 0) {
                isfloat[upd.index] = upd.isfloat;
                if (CpuSimulator.printRegWrite) {
                    if (upd.isfloat) {
                        sb.append(" FLOAT");
                    } else {
                        sb.append(" INT");
                    }
                }
            }
            if (CpuSimulator.printRegWrite) {
                Logger.out.println(sb.toString());
            }
        }
        regUpdates.clear();
    }
}
