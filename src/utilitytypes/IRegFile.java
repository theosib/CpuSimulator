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
public interface IRegFile {
    
    /**
     * @param index
     * @return Register at index is valid?
     */
    public boolean isInvalid(int index);
    default public boolean isValid(int index) { return !isInvalid(index); }
    
    /**
     * @param index
     * @return Register content result of floating point computation?
     */
    public boolean isFloat(int index);
    
    public void setInvalid(int index, boolean inv);
    default public void markValid(int index) { setInvalid(index, false); }
    default public void markInvalid(int index) { setInvalid(index, true); }
    public void markFloat(int index, boolean is_float);
    
    public int numRegisters();
    
    /**
     * This returns the raw register contents regardless of type or validity.
     * @param index
     * @return
     */
    public int getValueUnsafe(int index);
    
    /**
     * This throws an exception if the register is marked invalid.
     * @param index
     * @return Raw register contents, regardless of type (int, float)
     */
    public int getValue(int index);
    
    /**
     * This throws an exception if the register is marked invalid or
     * if it is not marked as a float.
     * @param index
     * @return Register value reinterpreted as a float.
     */
    public float getValueAsFloat(int index);
    
    /**
     * Modify a register without clock cycle delay.  Probably don't need this.
     * @param index
     * @param value
     * @param is_invalid
     * @param is_float
     */
    public void setRegisterImmediately(int index, int value, 
            boolean is_invalid, boolean is_float);
    
    /**
     * Set register value without changing any flags.
     * Setting is queued to take effect on next clock cycle.
     * @param index
     * @param value
     */
    public void setValueUnsafe(int index, int value);
    
    /**
     * Set register value, marking it as valid and not a float
     * Setting is queued to take effect on next clock cycle.
     * @param index
     * @param value
     */
    public void setIntValue(int index, int value);
    
    /**
     * Set register value, marking it as valid and a float.
     * Setting is queued to take effect on next clock cycle.
     * @param index
     * @param value Floating point value encoded in an int.
     */
    public void setFloatValue(int index, int value);

    /**
     * Set register value, marking it as valid and a float.
     * Setting is queued to take effect on next clock cycle.
     * @param index
     * @param value Floating point value encoded in as a float.
     */
    public void setFloatValue(int index, float value);
    
    /**
     * Sets a register value and marks it as valid.  Also sets the "is float"
     * flag to the value of the is_float argument.
     * Setting is queued to take effect on next clock cycle.
     * @param index
     * @param value Int or float value encoded as an int
     * @param is_float Is the value actually an encoded float?
     */
    public void setValue(int index, int value, boolean is_float);
    
    /**
     * Apply all of the 'set value' operations queued up.
     */
    public void advanceClock();
}
