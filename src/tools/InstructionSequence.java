/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import interfaces.EnumComparison;
import interfaces.EnumOpcode;
import interfaces.InstructionBase;
import interfaces.SourceOperand;
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
            if (ins.label_name == null) continue;
            Integer addr = labels.get(ins.label_name);
            if (addr == null) {
                throw new SyntaxError(ins.instruction_string, 
                    "Unknown label or keyword \"" + ins.label_name + "\".",
                    ins.line_num);        
            }
            ins.label_addr = addr;
        }
    }
    
    private void setRegisterOperand(InstructionBase ins, int operand_num, String field) throws SyntaxError {
        int reg_num = Integer.parseInt(field.substring(1));
        switch (operand_num) {
            case 0:
                ins.dst_regnum = reg_num;
                break;
            case 1:
                ins.src1 = SourceOperand.newRegisterSource(reg_num);
                break;
            case 2:
                ins.src2 = SourceOperand.newRegisterSource(reg_num);
                break;
            default:
                throw new SyntaxError(ins.instruction_string, 
                    "Too many operands.",
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
                throw new SyntaxError(ins.instruction_string, 
                    "First data operand must be a register.",
                    ins.line_num);        
            case 1:
                ins.src1 = SourceOperand.newLiteralSource(value);
                break;
            case 2:
                ins.src2 = SourceOperand.newLiteralSource(value);
                break;
            default:
                throw new SyntaxError(ins.instruction_string, 
                    "Too many operands.",
                    ins.line_num);        
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
        
        if (line.length() == 0) {
            line_num++;
            return;
        }
        
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
        
        InstructionBase ins = T.newInstruction();
        ins.instruction_string = original_line;
        ins.pc_address = current_pc;
        ins.line_num = line_num;
        ins.preceding_label = preceding_label;
        
        String[] fields = line.split("\\s*,\\s*|\\s+,?\\s*|\\s*,?\\s+");
        ins.opcode = EnumOpcode.fromString(fields[0]);
        if (ins.opcode == null) {
            throw new SyntaxError(original_line,
                "Unknown opcode \"" + fields[0] + "\"",
                line_num);
        }
        
        int num_data_operands = 0;
        int num_comparisons = 0;
        int num_labels = 0;
        
        for (int i=1; i<fields.length; i++) {
            String field = fields[i];
            
            if (field.matches("[Rr]\\d+")) {
                setRegisterOperand(ins, num_data_operands, field);
                num_data_operands++;
            } else if (field.matches("0x[0-9A-Fa-f]+")) {
                setLiteralOperand(ins, num_data_operands, field.substring(1));
                num_data_operands++;
            } else if (field.matches("[hH][0-9A-Fa-f]+")) {
                setLiteralOperand(ins, num_data_operands, field);
                num_data_operands++;
            } else if (field.matches("[bBoOdD#]?[0-9]+")) {
                setLiteralOperand(ins, num_data_operands, field);
                num_data_operands++;
            } else {
                EnumComparison cmp = EnumComparison.fromString(field);
                if (cmp != null) {
                    if (num_comparisons > 0) {
                        throw new SyntaxError(original_line,
                            "Too many comparison keywords.",
                            line_num);
                    }
                    ins.comparison = cmp;
                    num_comparisons++;
                } else if (isValidLabelName(field)) {
                    if (num_labels > 0) {
                        throw new SyntaxError(original_line,
                            "Too many label names.",
                            line_num);
                    }
                    ins.label_name = field;
                } else {
                    throw new SyntaxError(original_line,
                        "Cannot parse instruction.",
                        line_num);
                }
            }
        }
        
        instructions.add((T)ins);
        
        line_num++;
        current_pc++;
        preceding_label = null;
    }
    
    public void loadFromString(String program) throws IOException {
        loadFile(new BufferedReader(new StringReader(program)));
    }
    
    public void loadFromStrings(String[] program) throws IOException {
        loadFile(new StringArrayReader(program));
    }

    public void loadFromStrings(List<String> program) throws IOException {
        loadFile(new StringArrayReader(program));
    }
    
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
    
    public void loadFile(File f) throws IOException {
        System.out.println("Loading file: " + f.getCanonicalPath());
        loadFile(new BufferedReader(new InputStreamReader(new FileInputStream(f))));
    }
    
    public void loadFile(String fname) throws IOException {
        loadFile(new File(fname));
    }
    
    
    public void printProgram(PrintStream out) {
        for (InstructionBase ins : instructions) {
            if (ins.preceding_label != null) {
                out.println(ins.preceding_label + ":");
            }
            out.println("    " + ins.toString());
        }
    }
    
    public void printProgram() {
        printProgram(System.out);
    }
    
    public T getInstructionAt(int index) {
        return (T)instructions.get(index).duplicate();
    }
}
