/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voidtypes;

import utilitytypes.IRegFile;
import utilitytypes.Operand;
import utilitytypes.RegisterFile;

/**
 *
 * @author millerti
 */
public class VoidOperand extends Operand {
    static final private VoidOperand singleton = new VoidOperand();
    
    static public VoidOperand getVoidOperand() {
        return singleton;
    }

    @Override
    public boolean isRegister() { return false; }    
    @Override
    public int getRegisterNumber() { return -1; }
    @Override
    public int getValue() { return 0; }
    @Override
    public float getFloatValue() { return 0; }
    @Override
    public String getValueAsString() { return "??"; }
    public void lookUpFromRegisterFile(IRegFile regfile) {}
    @Override
    public void setIntValue(int v) { }
    @Override
    public void setFloatValue(int v) { }
    @Override
    public void setFloatValue(float v) { }
    
    @Override
    public void markValid(boolean valid) {  }
    @Override
    public void markFloat(boolean fl) { }
    
    @Override
    public boolean hasValue() { return false; }    
    @Override
    public boolean hasFloatValue() { return false; }
    @Override
    public boolean isFloat() { return false; }
    
    @Override
    public String getRegisterName() { return "void"; }

    @Override
    public String toString() { return "void"; }
    
    @Override
    public Operand duplicate() {
        return this;
    }
    
    @Override
    public boolean isNull() { return true; }
    
    private VoidOperand() { }
}
