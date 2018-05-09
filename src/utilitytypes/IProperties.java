/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilitytypes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author millerti
 */
public interface IProperties {
    // Globally standardize some property names to avoid typos
    public static final String RESULT_VALUE = "result_value";
    public static final String RESULT_FLOAT = "result_float";
    public static final String RESULT_FAULT = "result_fault";
    public static final String PROGRAM_COUNTER = "program_counter";
    public static final String MAIN_MEMORY = "main_memory";
    public static final String REG_BRANCH_TARGET = "reg_branch_target";
    public static final String FETCH_BRANCH_STATE = "fetch_branch_state";
    public static final String LOOKUP_BRANCH_TARGET = "lookup_branch_target";
    public static final String CPU_RUN_STATE = "cpu_run_state";
    public static final String REGISTER_FILE = "register_file";
    public static final String REGISTER_ALIAS_TABLE = "register_alias_table";
    public static final String ARCH_REG_FILE = "arch_reg_file";
    public static final String REORDER_BUFFER = "reorder_buffer";
    public static final String ROB_USED = "rob_used";
    public static final String ROB_HEAD = "rob_head";
    public static final String ROB_TAIL = "rob_tail";
    
    public static final String[] RESULT_PROPS_ARR = {RESULT_VALUE, RESULT_FLOAT, RESULT_FAULT};
    public static final Set<String> RESULT_PROPS_SET = new HashSet<String>(Arrays.asList(RESULT_PROPS_ARR));
    
    public static final String RECOVERY_PC = "recovery_pc";
    public static final String RECOVERY_TAKEN = "recovery_taken";

    public static final int BRANCH_STATE_NULL = 0;
    public static final int BRANCH_STATE_WAITING = 1;
    public static final int BRANCH_STATE_TARGET = 2;
    
    public static final int RUN_STATE_NULL = 0;
    public static final int RUN_STATE_RUNNING = 1;
    public static final int RUN_STATE_FAULT = 2;
    public static final int RUN_STATE_FLUSH = 3;
    public static final int RUN_STATE_RECOVERY = 4;
    public static final int RUN_STATE_HALTING = 5;
    public static final int RUN_STATE_HALTED = 6;

    
    public static final Object DELETE = new Object();
    
    public Set<String> propertyNames();
    public Map<String,Object> getProperties();
    public int numProperties();
    public boolean hasProperty(String name);
    public void setProperty(String name, Object val);
    public void setClockedProperty(String name, Object val);
    public void deleteProperty(String name);
    public void deleteClockedProperty(String name);
    public Integer getPropertyInteger(String name);
    public int[] getPropertyIntArray(String name);
    public Boolean getPropertyBoolean(String name);
    public boolean[] getPropertyBooleanArray(String name);
    public String getPropertyString(String name);
    public Object getPropertyObject(Object name);
    public void clear();
    public void advanceClock();

    public void copyPropertiesFrom(IProperties source, Set<String> propertiesToCopy);
    public void copyAllPropertiesFrom(IProperties source);
    
    public List<String> toStringList();
}
