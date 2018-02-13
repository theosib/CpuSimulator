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
public class SourceOperand {    
    public static SourceOperand newRegisterSource(int regnum) {
        SourceOperand s = new SourceOperand();
        s.register_num = regnum;
        return s;
    }
    
    public static SourceOperand newLiteralSource(int value) {
        SourceOperand s = new SourceOperand();
        s.register_num = -1;
        s.value = value;
        return s;
    }
    
    public static SourceOperand newDummySource() {
        SourceOperand s = new SourceOperand();
        s.register_num = -1;
        return s;
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
    
    public void setValue(int v) {
        value = v;
    }

    public SourceOperand duplicate() {
        SourceOperand op = new SourceOperand();
        op.register_num = this.register_num;
        op.value        = this.value;
        return op;
    }
    
    private int register_num;
    private int value;
    private SourceOperand() {}
}
