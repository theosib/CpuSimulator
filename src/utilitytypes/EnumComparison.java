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
    Generally, comparisons are made by subtracting two numbers.  Based on
    the condition code flags, a variety of comparisons can be made.  This
    does not necessarily imply that your simulator must implement
    exactly these condition flags or any condition flags.  Moreover, 
    not all conditions will even be used.  To begin with, you only need to
    support the following:
    - EQ, NE, GT, GE, LT, LE
    
    Condition code flags are typically:
    
    Z - result is zero
    N - result is negative
    V - result has overflowed (result has wrong sign)
    C - carry out (or inverse of borrow)
    
    The comparisons are as follows:
    
    EQ - operands are equal, Z=1
    NE - not equal, Z=0
    GT - signed greater than, (N & V & ~Z) | (~N & ~V & ~Z)
    GE - signed greater than or equal, (N & V) | (~N & ~V)
    LT - signed less than, (N & ~V) | (~N & V)
    LE - signed less than or equal, Z | (N & ~V) | (~N & V)
    HI - unsigned greater than, ~C & ~Z
    LO - unsigned less than, C=1
    HS - unsigned greater or equal, C=0
    LS - unsigned less than or equal, C | Z
    CC - carry clear, C=0
    CS - carry set, C=1
    PL - Not negative, N=0
    MI - Negative, N=1
    VC - No overflow, V=0
    VS - Overflow, V=1
    NULL - Void comparison
    */
    
    EQ, NE, GT, GE, LT, LE, HI, LO, HS, LS, CC, CS, PL, MI, VC, VS, NULL;
    
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
