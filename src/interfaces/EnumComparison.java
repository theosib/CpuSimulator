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
public enum EnumComparison {
    EQ, NE, GT, GE, LT, LE, HI, LO, CC, CS, PL, MI, VC, VS, ZR, NZ;
    
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
