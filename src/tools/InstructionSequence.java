/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import utilitytypes.EnumComparison;
import utilitytypes.EnumOpcode;
import baseclasses.InstructionBase;
import utilitytypes.LabelTarget;
import utilitytypes.Operand;
import voidtypes.VoidInstruction;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class serves as both a parser for assembly language programs and
 * a container for sequences of instructions that have been parsed.
 * 
 * Syntax:
 * 
 * - A semicolon (;) indicates that the remainder of a line is a comment.
 * 
 * - A label indicates that the following instruction is a branch target.
 *   A label is any alpha-numeric name with a colon at the start or end. 
 *   A label must not have any other symbols on the same line, but may have
 *   a comment after it.
 *   Examples:
 *   :this_is_a_label
 *   this_is_a_label_2:  ; This is a comment
 * 
 * - The first word of an instruction is the opcode.  See interfaces.EnumOpcode.
 *   Example:
 *   HALT       ; Stop the simulation
 * 
 * - The second word of an instruction is typically the destination register.
 *   Registers are indicated by the letter 'R' prefixed to the register number.
 *   The 'R' is not case sensitive.
 *   Example:
 *   R12        ; register 12
 *   r7         ; register 7
 * 
 * - The second and third words of an instruction are typically sources.
 *   They can be register sources (prefixed by 'R') or literals, which are
 *   constant values provided by the instruction..
 *   Example literals:
 *   103        ; Decimal 103
 *   D809       ; Decimal 809. Lower case 'd' and '#' can also prefix a decimal.
 *   HAFFC      ; Hexadecimal 0xAFFC.  Can also use lower case 'h'
 *   0xFACE     ; Hexadecimal 0xFACE
 *   O7713      ; Octal 7713.  Lower case 'o' can also be used.
 *   B10110     ; Binary 10110.  Can also use lower case 'b'.
 *   
 * - Some instructions, such as branches, can take a condition, which is a
 *   predicate that decides if the instruction is to be executed or not.
 *   Permitted conditions can be found in interfaces.EnumComparison.
 *   The condition can appear as any word in the instruction.
 * 
 * - Some instructions, such as branches, can take a label target.  The
 *   label is any alphanumeric word that is not recognized as a register,
 *   numerical literal, or condition.
 *   Example:
 *   CMP R7 R1 R2       ; Compare R1 and R2, put condition flags into R7
 *   BRA GT R7 my_label ; If R7 indicates greater-than, branch to label.
 * 
 * - Operands are delimited by whitespace.  Comma (,) is also permitted.
 * 
 * @author millerti
 */
public class InstructionSequence<T extends InstructionBase> {
    static public class SyntaxError extends Exception {
        public SyntaxError(String line, String reason, int line_num) {
            super("Error at line " + line_num + ": " + reason +
                "\n" + line);
        }
    }
    
    static boolean[] valid_characters_set;
    static {
        valid_characters_set = new boolean[128];
        
        int a, b;
        a = 'a';
        b = 'z';
        for (int i=a; i<=b; i++) {
            valid_characters_set[i] = true;
        }

        a = 'A';
        b = 'Z';
        for (int i=a; i<=b; i++) {
            valid_characters_set[i] = true;
        }
        
        a = '0';
        b = '9';
        for (int i=a; i<=b; i++) {
            valid_characters_set[i] = true;
        }

        valid_characters_set['_'] = true;
    }
    
    private boolean isValidLabelName(String name) {
        for (int i=0; i<name.length(); i++) {
            int j = name.charAt(i);
            if (j != (j&127)) return false;
            if (!valid_characters_set[j]) return false;
        }
        return true;
    }
    
    
    private Map<String,Integer> labels = new HashMap<>();
    private List<T> instructions = new ArrayList<>();
    private int current_pc, line_num;
    private String preceding_label;
    
    private void matchLabels() throws SyntaxError {
        for (InstructionBase ins : instructions) {
            // If no label target, skip to next instruction
            LabelTarget target = ins.getLabelTarget();
            if (target.isNull()) continue;
            
            // Look up label name in mapping from label names to program
            // addresses.
            Integer addr = labels.get(target.getName());
            if (addr == null) {
                // Error if the label was not defined anywhere.
                throw new SyntaxError(ins.getInstructionString(), 
                    "Unknown label or keyword \"" + target.getName() + "\".",
                    ins.getLineNum());        
            }
            
            // Provide looked-up address.
            target.setAddress(addr);
        }
    }
    
    private void setRegisterOperand(InstructionBase ins, int operand_num, String field) throws SyntaxError {
        int reg_num = Integer.parseInt(field.substring(1));
        switch (operand_num) {
            case 0:
                ins.setOper0(Operand.newRegister(reg_num));
                break;
            case 1:
                ins.setSrc1(Operand.newRegister(reg_num));
                break;
            case 2:
                ins.setSrc2(Operand.newRegister(reg_num));
                break;
            default:
                throw new SyntaxError(ins.getInstructionString(), 
                    "No more than three register or literal operands permitted.",
                    line_num);        
        }
    }
    
    private void setLiteralOperand(InstructionBase ins, int operand_num, String field) throws SyntaxError {
        int value = 0;
        if (!Character.isDigit(field.charAt(0))) {
            switch (Character.toUpperCase(field.charAt(0))) {
                case 'H':
                case 'X':
                    value = Integer.parseInt(field.substring(1), 16);
                    break;
                case 'D':
                case '#':
                    value = Integer.parseInt(field.substring(1), 10);
                    break;
                case 'O':
                    value = Integer.parseInt(field.substring(1), 8);
                    break;
                case 'B':
                    value = Integer.parseInt(field.substring(1), 2);
                    break;
                default:
                    value = Integer.parseInt(field, 10);
                    break;
            }
        } else {
            value = Integer.parseInt(field, 10);
        }
        switch (operand_num) {
            case 0:
                throw new SyntaxError(ins.getInstructionString(), 
                    "First data operand must be a register.",
                    ins.getLineNum());        
            case 1:
                ins.setSrc1(Operand.newLiteralSource(value));
                break;
            case 2:
                ins.setSrc2(Operand.newLiteralSource(value));
                break;
            default:
                throw new SyntaxError(ins.getInstructionString(), 
                    "No more than three register or literal operands permitted.",
                    ins.getLineNum());        
        }
    }
    
    public void parseLine(String line) throws SyntaxError {
        String original_line = line;
        
        // Eliminate leading and trailing whitespace
        line = line.trim();        
        
        // Remove a comment, if any (starts with semicolon, goes to end of line)
        int semicolon_ix = line.indexOf(';');
        if (semicolon_ix >= 0) {
            line = line.substring(0, semicolon_ix).trim();
        }
        
        // If the line is blank once comments are removed, skip to the next
        // line
        if (line.length() == 0) {
            line_num++;
            return;
        }
        
        // Look for a keyword that is preceeded or followed by a colon.
        // If found, this line is marked with a label.
        int colon_ix = line.indexOf(':');
        if (colon_ix>=0) {
            String label = null;
            if (line.length()>1 && colon_ix==0) {
                label = line.substring(1);
            } else if (line.length()>1 && colon_ix == line.length()-1) {
                label = line.substring(0, line.length()-1);
            } else {
                throw new SyntaxError(original_line, 
                        "For label, colon must appear at beginning or end of valid name.",
                        line_num);
            }
            
            boolean valid_name = isValidLabelName(label);
            if (!valid_name) {
                throw new SyntaxError(original_line, 
                        "Characters in label names must be only alpha-numeric and underscore.",
                        line_num);
            }
            
            if (labels.containsKey(label)) {
                throw new SyntaxError(original_line, 
                        "All label names must be unique.",
                        line_num);                
            }
            
            labels.put(label, current_pc);
            preceding_label = label;
            line_num++;
            return;
        }
        
        // Use a factory to make an instruction in case it is a subclass
        // of InstructonBase.
        InstructionBase ins = T.newInstruction();
        
        // Metadata about the instruction.
        ins.setInstructionString(original_line);
        ins.setPCAddress(current_pc);
        ins.setLineNum(line_num);
        ins.setPrecedingLabel(preceding_label);
        
        // Split the line into words, deliminted by whitespace, a comma, or
        // a comma and whitespace.
        String[] fields = line.split("\\s*,\\s*|\\s+,?\\s*|\\s*,?\\s+");
        
        // First word is the opcode.
        EnumOpcode op = EnumOpcode.fromString(fields[0]);
        if (op == null) {
            throw new SyntaxError(original_line,
                "Unknown opcode \"" + fields[0] + "\"",
                line_num);
        }
        ins.setOpcode(op);
        
        int num_data_operands = 0;
        
        // Loop over the remaining words, identifying registers, literals,
        // conditions, and label targets.
        for (int i=1; i<fields.length; i++) {
            String field = fields[i];
            
            if (field.matches("[Rr]\\d+")) {
                // Register is 'R' or 'r' prefixing a decimal number
                setRegisterOperand(ins, num_data_operands, field);
                num_data_operands++;
            } else if (field.matches("0x[0-9A-Fa-f]+")) {
                // Hexadecimal literal
                setLiteralOperand(ins, num_data_operands, field.substring(1));
                num_data_operands++;
            } else if (field.matches("[hH][0-9A-Fa-f]+")) {
                // Hexadecimal literal
                setLiteralOperand(ins, num_data_operands, field);
                num_data_operands++;
            } else if (field.matches("[bBoOdD#]?[0-9]+")) {
                // Decimal, octal, or binary literal
                // (TODO:  Make sure octals don't use 8 and 9; make sure
                // binary uses only 0 and 1.)
                setLiteralOperand(ins, num_data_operands, field);
                num_data_operands++;
            } else {
                EnumComparison cmp = EnumComparison.fromString(field);
                if (cmp != null) {
                    // Word matches a condition
                    if (ins.getComparison() != EnumComparison.NULL) {
                        throw new SyntaxError(original_line,
                            "No more than one comparison keyword is allowed",
                            line_num);
                    }
                    ins.setComparison(cmp);
                } else if (isValidLabelName(field)) {
                    // Word is a combination of characters permitted to be
                    // in a label.
                    if (!ins.getLabelTarget().isNull()) {
                        throw new SyntaxError(original_line,
                            "No more than one target label allowed",
                            line_num);
                    }
                    
                    // Using address -1 to indicate unknown or invalid
                    // target address.
                    ins.setLabelTarget(new LabelTarget(field, -1));
                } else {
                    // Unparsable word
                    throw new SyntaxError(original_line,
                        "Cannot parse instruction.",
                        line_num);
                }
            }
        }
        
        // Add to list
        instructions.add((T)ins);
        
        line_num++;
        current_pc++;
        preceding_label = null;
    }
    
    /**
     * Parse a whole program from a single string where lines are delimited
     * by newline characters.
     * 
     * @param program
     * @throws IOException
     */
    public void loadFromString(String program) throws IOException {
        loadFile(new BufferedReader(new StringReader(program)));
    }
    
    /**
     * Parse a whole program from an array of Strings, where each array
     * entry is a line of code.
     * 
     * @param program
     * @throws IOException
     */
    public void loadFromStrings(String[] program) throws IOException {
        loadFile(new StringArrayReader(program));
    }

    /**
     * Parse a whole program from a List of Strings, where each List element
     * is a line of code.
     * 
     * @param program
     * @throws IOException
     */
    public void loadFromStrings(List<String> program) throws IOException {
        loadFile(new StringArrayReader(program));
    }
    
    /**
     * Parse a whole program from a BufferedReader input stream.
     * 
     * @param in
     * @throws IOException
     */
    public void loadFile(BufferedReader in) throws IOException {
        for (;;) {
            String line = in.readLine();
            if (line == null) break;
            
            try {
                parseLine(line);
            } catch (SyntaxError ex) {
                System.err.println(ex.getMessage());
                break;
            }
        }
        in.close();
        
        try {
            matchLabels();
        } catch (SyntaxError ex) {
            System.err.println(ex.getMessage());
        }
        System.err.flush();
        System.out.flush();
    }
    
    /**
     * Parse a whole program from the file indicated by a given File object.
     * 
     * @param f
     * @throws IOException
     */
    public void loadFile(File f) throws IOException {
        System.out.println("Loading file: " + f.getCanonicalPath());
        loadFile(new BufferedReader(new InputStreamReader(new FileInputStream(f))));
    }
    
    /**
     * Parse a whole program from the file indicated by the given filename.
     * 
     * @param fname
     * @throws IOException
     */
    public void loadFile(String fname) throws IOException {
        loadFile(new File(fname));
    }
    
    /**
     * Print out the loaded program to a given PrintStream.
     * 
     * @param out
     */
    public void printProgram(PrintStream out) {
        for (InstructionBase ins : instructions) {
            String pl = ins.getPrecedingLabel();
            if (pl != null) {
                out.println(pl + ":");
            }
            out.println("    " + ins.toString());
        }
    }
    
    /**
     * Print out the loaded program to standard out.
     */
    public void printProgram() {
        printProgram(System.out);
    }
    
    /**
     * Fetch an instruction at the provided program address.
     * @param pc_address
     * @return
     */
    public InstructionBase getInstructionAt(int pc_address) {
        // A clone of the instruction is provided so that the simulator
        // may modify elements of the instruction as it passes through
        // the pipeline without affecting the original parsed instruction.
        if (pc_address<0 || pc_address>=instructions.size()) {
            return VoidInstruction.getVoidInstruction();
        }
        return instructions.get(pc_address).duplicate();
    }
}
