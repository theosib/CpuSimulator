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
    protected final int[] flags;
    boolean physical;
    String prefix = "R";
    
    @Override
    public void markPhysical() {
        physical = true;
        prefix = "P";
    }
    @Override
    public boolean isPhysical() {
        return physical;
    }

    @Override
    public void setRegisterImmediately(int index, int value, int flagsIn) {
        values[index] = value;
        flags[index] = flagsIn;
    }
    
    protected static class RegUpdate {
        public final int index;
        public final int flags_set, flags_clear;
        public final int value;
        public RegUpdate(int ix, int val, int fset, int fclear) {
            index = ix;
            flags_set = fset;
            flags_clear = fclear;
            value = val;
        }
    }
    
    protected List<RegUpdate> regUpdates = new ArrayList<>();
    
    
    public RegisterFile(int num_registers) {
        values = new int[num_registers];
        flags = new int[num_registers];
    }
    
    @Override
    public boolean isInvalid(int index) { return (flags[index] & FLAG_INVALID) != 0; }
    @Override
    public boolean isValid(int index) { return !isInvalid(index); }
    @Override
    public boolean isFloat(int index) { return (flags[index] & FLAG_FLOAT) != 0; }
    @Override
    public boolean isUsed(int index) { return (flags[index] & FLAG_USED) != 0; }
    @Override
    public boolean isRenamed(int index) { return (flags[index] & FLAG_RENAMED) != 0; }
    @Override
    public int getFlags(int index) { return flags[index]; }
    
    @Override
    public void setInvalid(int index, boolean inv) { 
        regUpdates.add(new RegUpdate(index, 0, inv ? SET_INVALID : 0, inv ? 0 : CLEAR_INVALID));
    }
    @Override
    public void markFloat(int index, boolean is_float) { 
        regUpdates.add(new RegUpdate(index, 0, is_float ? SET_FLOAT : 0, is_float ? 0 : CLEAR_FLOAT));
    }
    @Override
    public void markUsed(int index, boolean is_used) {
        regUpdates.add(new RegUpdate(index, 0, is_used ? SET_USED : 0, is_used ? 0 : CLEAR_USED));
    }
    @Override
    public void markRenamed(int index, boolean is_renamed) {
        regUpdates.add(new RegUpdate(index, 0, is_renamed ? SET_RENAMED : 0, is_renamed ? 0 : CLEAR_RENAMED));
    }
    @Override
    public void changeFlags(int index, int flags_to_set, int flags_to_clear) {
        regUpdates.add(new RegUpdate(index, 0, flags_to_set, flags_to_clear));
    }
    
    /**
     * This returns the raw register contents regardless of type or validity.
     * @param index
     * @return
     */
    @Override
    public int getValueUnsafe(int index) { return values[index]; }
    
    @Override
    public int getValue(int index) { 
        if (isInvalid(index)) {
            throw new RuntimeException("Register " + prefix + index + " is not valid");
        }
        return values[index]; 
    }
    
    @Override
    public float getValueAsFloat(int index) {
        int ivalue = getValue(index);
        if (!isFloat(index)) {
            throw new RuntimeException("Register " + prefix + index + " is not float");
        }
        return Float.intBitsToFloat(ivalue);
    }
    
    @Override
    public void setValueUnsafe(int index, int value) {
        regUpdates.add(new RegUpdate(index, value, SET_VALUE, 0));
    }
    
    @Override
    public void setIntValue(int index, int value) {
        regUpdates.add(new RegUpdate(index, value, SET_VALUE, CLEAR_INVALID | CLEAR_FLOAT));
    }
    
    @Override
    public void setFloatValue(int index, int value) {
        regUpdates.add(new RegUpdate(index, value, SET_VALUE | SET_FLOAT, CLEAR_INVALID));
    }

    @Override
    public void setFloatValue(int index, float value) {
        int ival = Float.floatToRawIntBits(value);
        regUpdates.add(new RegUpdate(index, ival, SET_VALUE | SET_FLOAT, CLEAR_INVALID));
    }
    
    @Override
    public void setValue(int index, int value, boolean is_float) {
        regUpdates.add(new RegUpdate(index, value, SET_VALUE | (is_float ? SET_FLOAT : 0), CLEAR_INVALID | (is_float ? 0 : CLEAR_FLOAT)));
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
                sb.append("   ").append(prefix).append(upd.index).append(':');
            }

            flags[upd.index] |= upd.flags_set;
            flags[upd.index] &= ~upd.flags_clear;
            
            if ((upd.flags_set & SET_VALUE) != 0) {
                values[upd.index] = upd.value;
                if (CpuSimulator.printRegWrite) {
                    sb.append(" VALUE=");
                    if ((flags[upd.index] & FLAG_FLOAT) != 0) {
                        sb.append(Float.intBitsToFloat(upd.value));
                    } else {
                        sb.append(upd.value);
                    }
                }
            }
            if (CpuSimulator.printRegWrite) {
                if (((upd.flags_set | upd.flags_clear) & SET_INVALID) != 0) {
                    if ((flags[upd.index] & FLAG_INVALID) != 0) {
                        sb.append(" INVALID");
                    } else {
                        sb.append(" VALID");
                    }
                }
                if (((upd.flags_set | upd.flags_clear) & SET_FLOAT) != 0) {
                    if ((flags[upd.index] & FLAG_FLOAT) != 0) {
                        sb.append(" FLOAT");
                    } else {
                        sb.append(" INT");
                    }
                }
                if (((upd.flags_set | upd.flags_clear) & SET_USED) != 0) {
                    if ((flags[upd.index] & FLAG_USED) != 0) {
                        sb.append(" USED");
                    } else {
                        sb.append(" FREE");
                    }
                }
                if (((upd.flags_set | upd.flags_clear) & SET_RENAMED) != 0) {
                    if ((flags[upd.index] & FLAG_RENAMED) != 0) {
                        sb.append(" RENAMED");
                    } else {
                        sb.append(" UNRENAMED");
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
