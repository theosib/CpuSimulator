/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voidtypes;

import utilitytypes.IRegFile;

/**
 *
 * @author millerti
 */
public class VoidRegFile implements IRegFile {
    private static VoidRegFile singleton = new VoidRegFile();
    static public VoidRegFile getVoidRegFile() {
        return singleton;
    }
    private VoidRegFile() {}

    @Override
    public boolean isInvalid(int index) { return true; }

    @Override
    public boolean isFloat(int index) { return false; }
    
    @Override
    public int numRegisters() { return 0; }

    @Override
    public int getValueUnsafe(int index) { return 0; }

    @Override
    public int getValue(int index) { return 0; }

    @Override
    public float getValueAsFloat(int index) { return 0; }

    @Override
    public void setValueUnsafe(int index, int value) {}

    @Override
    public void setIntValue(int index, int value) {}

    @Override
    public void setFloatValue(int index, int value) {}

    @Override
    public void setFloatValue(int index, float value) {}

    @Override
    public void setValue(int index, int value, boolean is_float) {}

    @Override
    public void setInvalid(int index, boolean inv) {}

    @Override
    public void markFloat(int index, boolean is_float) {}

    @Override
    public void setRegisterImmediately(int index, int value, boolean is_invalid, boolean is_float) {}

    @Override
    public void advanceClock() {}
    
}
