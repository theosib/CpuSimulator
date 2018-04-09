/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilitytypes;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author millerti
 */
public interface IProperties {
    // Some common property names
    public static final String RESULT_VALUE = "result_value";
    public static final String RESULT_FLOAT = "result_float";
//    public static final String REGISTER_FILE = "register_file";
//    public static final String REGISTER_INVALID = "register_invalid";
    public static final String PROGRAM_COUNTER = "program_counter";
    public static final String MAIN_MEMORY = "main_memory";
    
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
