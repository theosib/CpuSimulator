/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilitytypes;

/**
 * Target of branch instruction when specified by a label.
 * 
 * @author millerti
 */
public class LabelTarget {
    // Name of label target (useful for debugging purposes)
    private String label_name;
    
    // Program address that the label points to
    private int label_address;
    
    public LabelTarget() {}
    public LabelTarget(String name, int address) {
        label_name = name;
        label_address = address;
    }
    
    public String getName() { return label_name; }
    public int getAddress() { return label_address; }
    public void setName(String name) { label_name = name; }
    public void setAddress(int addr) { label_address = addr; }
    
    public boolean isNull() { return label_name==null; }
    
    public LabelTarget duplicate() {
        return new LabelTarget(label_name, label_address);
    }
    
    
    @Override
    public String toString() {
        if (label_address < 0) {
            return getName() + "->unknown";
        } else {
            return getName() + "->" + getAddress();
        }
    }
}
