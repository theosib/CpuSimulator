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
    CMP - subtract two registers, put comparison flags into destination reg
    ROL - rotate left
    ROR - rotate right
    MULS - signed multiply
    MULU - unsigned multiply
    DIVS - signed divide
    DIVU - unsigned divide
    BRA - branch with condition
    JMP - unconditionally branch
    CALL - Branches to target address, puts return address in register
    LOAD - load from memory
    STORE - store to memory
    MOVC - load register from literal in instruction
    OUT - print contents of src1 to display
    HALT - stop simulation
    NOP - no operation
    INVALID - ???
    NULL - represents pipeline bubble
    */
    
    
    ADD, SUB, AND, OR, SHL, ASR, LSR, XOR, CMP, ROL, ROR,
    MULS, MULU, DIVS, DIVU,
    BRA, JMP, CALL, 
    LOAD, STORE, MOVC, OUT,
    HALT, NOP, INVALID, NULL;
    
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
            SHL, ASR, LSR, XOR, CMP, ROL, ROR, MULS, MULU, DIVS, DIVU,
            CALL, LOAD, MOVC);
    static final EnumSet<EnumOpcode> oper0SourceSet = 
            EnumSet.of(BRA, OUT, STORE, JMP);
    static final EnumSet<EnumOpcode> branchSet = 
            EnumSet.of(BRA, JMP, CALL);

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
    
}
