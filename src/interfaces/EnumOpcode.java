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
public enum EnumOpcode {
    ADD, SUB, HALT, NOP, INVALID;
    
    public static EnumOpcode fromString(String name) {
        name = name.trim().toUpperCase();
        EnumOpcode op = null;
        try {
            op = EnumOpcode.valueOf(name);
        } catch (Exception e) {
            op = null;
        }
        return op;
    }
}
