/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilitytypes;

import java.util.EnumSet;

/**
 * Enum for all opcodes to be implemented by the CPU simulator.
 * 
 * @author millerti
 */
public enum EnumOpcode {
    /* 
    Opcode meanings are as follows:
    
    ADD - add two registers
    SUB - subtract
    AND - bitwise AND
    OR  - bitwise OR
    SHL - logical shift left (inserts zeros on the right)
    ASR - arithmetic shift right (replicates sign bit)
    LSR - logical shift right (inserts zeros on the left)
    XOR - bitwise exclusive OR
    CMP - Integer compare -- do the same as SUB
    MUL - integer multiply
    DIV - integer divide
    MOD - integer remainder/modulus
    BRA - branch with condition
    JMP - unconditionally branch
    CALL - Branches to target address, puts return address in register
    LOAD - load from memory
    STORE - store to memory
    MOV - load register from literal or register source
    OUT - print contents of oper0 to display
    FADD - Float add
    FSUB - Float subtract (use same unfunctional unit as for FADD)
    FMUL - Float multiply
    FDIV - Float divide
    FOUT - Print oper0 as floating point number
    FCMP - Floating point compare -- do the same as FSUB
    HALT - stop simulation
    NOP - no operation
    INVALID - ???
    NULL - represents pipeline bubble
    */
    
    
    ADD, SUB, AND, OR, SHL, ASR, LSR, XOR, CMP,
    MUL, DIV, MOD,
    BRA, JMP, CALL, 
    LOAD, STORE, MOV, OUT,
    HALT, NOP, INVALID, NULL,
    FADD, FSUB, FMUL, FDIV, FOUT, FCMP;
    
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
    
    static final EnumSet<EnumOpcode> writebackSet = 
            EnumSet.of(ADD, SUB, AND, OR, 
            SHL, ASR, LSR, XOR, CMP, MUL, DIV, MOD,
            CALL, LOAD, MOV, FADD, FSUB, FMUL, FDIV, FCMP);
    static final EnumSet<EnumOpcode> oper0SourceSet = 
            EnumSet.of(BRA, OUT, STORE, JMP, FOUT);
    static final EnumSet<EnumOpcode> branchSet = 
            EnumSet.of(BRA, JMP, CALL);
    static final EnumSet<EnumOpcode> memorySet = 
            EnumSet.of(LOAD, STORE);

    /**
     * Does the given opcode produce a value that must be written back to the
     * register file?
     * 
     * @param op
     * @return
     */
    public static boolean needsWriteback(EnumOpcode op) {
        return writebackSet.contains(op);
    }
    
    public boolean needsWriteback() {
        return writebackSet.contains(this);
    }

    /**
     * Is the first operand actually a source for the given opcode? 
     * (Instead of being the the destination as is the case for most
     * instructions.)
     * 
     * @param op
     * @return
     */
    public static boolean oper0IsSource(EnumOpcode op) {
        return oper0SourceSet.contains(op);
    }
    
    public boolean oper0IsSource() {
        return oper0SourceSet.contains(this);
    }

    /**
     * Returns true if the instruction is a branch (BRA, JMP, CALL).
     * @param op
     * @return
     */
    public static boolean isBranch(EnumOpcode op) {
        return branchSet.contains(op);
    }

    public boolean isBranch() {
        return branchSet.contains(this);
    }

    public boolean isNull() {
        return this == NULL;
    }
    
    
    public static boolean accessesMemory(EnumOpcode op) {
        return memorySet.contains(op);
    }
    
    public boolean accessesMemory() {
        return memorySet.contains(this);
    }
}
