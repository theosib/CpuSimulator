/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilitytypes;

/**
 * Types of comparisons to be implemented by compare instructions.
 * 
 * @author millerti
 */
public enum EnumComparison {
    /*
    Generally, comparisons are made by subtracting two numbers.  
    For the comparison we want to do, it is sufficient for compare
    instuctions to be aliases for substract instructions.
    
    Comparisons to implement:
    EQ - difference is zero
    NE - difference is nonzero
    GT - difference is greater than zero
    GE - difference is zero or greater
    LT - difference is negative
    LE - difference is zero or negative
    NULL - Void comparison
    */
    
    EQ, NE, GT, GE, LT, LE, NULL;
    
    public static EnumComparison fromString(String name) {
        name = name.trim().toUpperCase();
        EnumComparison op = null;
        try {
            op = EnumComparison.valueOf(name);
        } catch (Exception e) {
            op = null;
        }
        return op;
    }
}
