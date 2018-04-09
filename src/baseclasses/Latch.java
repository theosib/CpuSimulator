/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package baseclasses;

import baseclasses.InstructionBase;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import utilitytypes.IPipeReg;
import utilitytypes.IPipeStage;
import utilitytypes.IProperties;
import voidtypes.VoidInstruction;
import voidtypes.VoidRegister;

/**
 * Generic latch class.  A pipeline register will dynamically create
 * instances of these.  Latches already contain an instruction field,
 * and there are convenience methods to access result arch/phys register
 * numbers and result values.  All other values to be passed between
 * stages are set and retrieved by String name.
 * 
 * @author millerti
 */
public class Latch extends PropertiesContainer {
    private boolean invalid = false;
    protected InstructionBase ins = VoidInstruction.getVoidInstruction();
    
    // Reference to containing register
    IPipeReg parent;
    
    /**
     * @return Is the next pipeline stage able to accept new work?
     */
    public boolean canAcceptWork() {
        if (invalid) return true;
        return parent.canAcceptWork();
    }    
    
    /**
     * After filling this latch with data to be passed to the next pipeline
     * stage, use this method to commit this latch to the master latch of
     * the parent pipeline register.  When advanceClock is called on
     * the parent pipeline register, the master latch will be copied to
     * to the slave latch, making the data available to the next stage.
     */
    public void write() {
        if (invalid) return;
        parent.write(this);
//        int stage_out_index = parent.getIndexInBefore();
//        IPipeStage stage = parent.getStageBefore();
//        stage.outputWritten(this, stage_out_index);
    }
    
    /**
     * Inform the parent register that the contents of its slave latch have
     * been accepted by its sink pipeline stage.  This is analogous to
     * dequeueing the pipeline register's slave latch, allowing the preceding
     * pipeline stage to pass more work through this pipeline register.
     * 
     * The default state of all inputs is "unconsumed," which is interpreted
     * as a stall condition by the pipeline register and preceding pipeline
     * stage.  Therefore it is vital that all inputs consumed by a stage
     * be properly consumed.
     */
    public void consume() {
        if (invalid) return;
        
        Latch ident = this;
        while (ident.duplicate_of != null) {
            ident = ident.duplicate_of;
        }
        
        if (ident != parent.read()) {
            throw new RuntimeException("Can only consume input value from slave latch");
        }
        
        parent.consumeSlave();
    }
    
    
    /**
     * Constructor for new latch.  Do not use this constructor.  Instead,
     * use the factor method IPipeReg.newLatch();
     * @param parent
     */
    public Latch(IPipeReg parent) {
        this.parent = parent;
    }
    
    /**
     * @return Pipeline register that produced or contains this latch.
     */
    public IPipeReg getParentRegister() { return parent; }
    //public void setParentRegister(IPipeReg p) { parent = p; }
    

    /**
     * Invalidate the contents of this latch.  This is for passing bubbles
     * down the pipeline. 
     * 
     * Generally do not use this method directory.  Instead, use the factory 
     * method IPipeReg.invalidLatch();
     */
    public void setInvalid() { 
        invalid = true; 
        ins = VoidInstruction.getVoidInstruction();
    }
    
    // Access methods for the instruction in the base class
    public InstructionBase getInstruction() { return ins; }
    public void setInstruction(InstructionBase ins) {
        if (!invalid) {
            this.ins = ins;
        }
    }
    
    /**
     * Turn the contents of this latch (instruction and properties) into a
     * newline-delimited string.
     * 
     * @return Printable representation of this latch's contents.
     */
    @Override
    public String toString() {
        List<String> props = toStringList();
        if (props.size() > 0) {
            return ins.toString() + '\n' + String.join("\n", props);
        } else {
            return ins.toString();
        }
    }
    
    /**
     * Determine if this latch contains an instruction with a destination 
     * register.
     * 
     * @return destination register, if any; -1 otherwise
     */
    public int getResultRegNum() {
        if (invalid) return -1;
        if (ins.getOpcode().needsWriteback()) {
            return ins.getOper0().getRegisterNumber();
        } else {
            return -1;
        }        
    }

    public String getResultRegName() {
        if (invalid) return "invalid";
        if (ins.getOpcode().needsWriteback()) {
            return ins.getOper0().getRegisterName();
        } else {
            return "none";
        }        
    }
    
    public String getResultValueAsString() {
        if (isResultFloat()) {
            float value = getResultValueAsFloat();
            return Float.toString(value);
        } else {
            int value = getResultValue();
            return Integer.toString(value);
        }
    }
    
    /**
     * Determine if this latch has both a target register and a result value
     * for it.
     * 
     * @return
     */
    public boolean hasResultValue() {
        if (invalid || getResultRegNum() < 0) return false;
        return hasProperty(RESULT_VALUE);
    }
    
    public boolean isResultFloat() {
        if (invalid || getResultRegNum() < 0) return false;
        return hasProperty(RESULT_FLOAT);
    }
    
    /**
     * Fetch result value, if any.
     * @return
     */
    public int getResultValue() {
        if (invalid) return 0;
        return getPropertyInteger(RESULT_VALUE);
    }

    public float getResultValueAsFloat() {
        if (invalid) return 0;
        return Float.intBitsToFloat(getPropertyInteger(RESULT_VALUE));
    }
    
    /**
     * Store computed result value
     * 
     * @param value Computed value to be written to an arch/phys register.
     */
    public void setResultValue(int value) {
        if (invalid) return;
        setProperty(RESULT_VALUE, value);
        deleteProperty(RESULT_FLOAT);
    }
    
    /**
     * Store computed result value, a float encoded as an int
     * 
     * @param value Computed value to be written to an arch/phys register.
     */
    public void setResultFloatValue(int value) {
        if (invalid) return;
        setProperty(RESULT_VALUE, value);
        setProperty(RESULT_FLOAT, true);
    }
    
    public void setResultValue(int value, boolean isfloat) {
        if (invalid) return;
        setProperty(RESULT_VALUE, value);
        if (isfloat) {
            setProperty(RESULT_FLOAT, true);
        } else {
            deleteProperty(RESULT_FLOAT);
        }
    }
    
    /**
     * Store computed result value, a float encoded as an int
     * 
     * @param value Computed value to be written to an arch/phys register.
     */
    public void setResultFloatValue(float value) {
        if (invalid) return;
        setProperty(RESULT_VALUE, Float.floatToRawIntBits(value));
        setProperty(RESULT_FLOAT, true);
    }
    
    /**
     * Find out if this latch contains no valid data
     * @return 
     */
    public boolean isNull() { return invalid || ins.isNull(); }
    
    /**
     * @return Name of the pipeline register that contains or produced this
     *         latch.
     */
    public String getName() { return parent.getHierarchicalName(); }
    
    public void copyParentPropertiesFrom(Latch source) {
        if (invalid) return;
        copyPropertiesFrom(source, parent.getPropertiesList());
    }
    
    /**
     *
     * @param source
     */
    @Override
    public void copyAllPropertiesFrom(IProperties source) {
        if (invalid) return;
        copyPropertiesFrom(source, null);
    }
    
    private Latch duplicate_of = null;
    
    public boolean isDuplicate() { return duplicate_of != null; }
    
    public Latch duplicate() {
        if (invalid) return this;
        
        Latch n = new Latch(parent);
        n.ins = ins.duplicate();
        n.copyAllPropertiesFrom(this);
        n.duplicate_of = this;
        
        return n;
    }
}
