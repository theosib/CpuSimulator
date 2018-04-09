/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package baseclasses;

import cpusimulator.CpuSimulator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import utilitytypes.IProperties;
import utilitytypes.Logger;

/**
 * Genetic container of properties of various data types, indexed by String
 * name.  This is the base class for global data and for pipeline latches.
 * 
 * @author millerti
 */
public class PropertiesContainer implements IProperties {
    protected Map<String,Object> properties;
    protected Map<String,Object> clocked_properties;
    
    /**
     * For diagnostic purposes
     * @return List of property names and values, in String form.
     */
    @Override
    public List<String> toStringList() {
        List<String> list = new ArrayList<>();
        if (properties == null) return list;
        for (Map.Entry<String, Object> ent : properties.entrySet()) {
            list.add(ent.getKey() + " = " + ent.getValue());
        }
        return list;
    }
    
    /**
     * Wipe out all properties
     */
    @Override
    public void clear() {
        if (properties != null) properties.clear();
        if (clocked_properties != null) clocked_properties.clear();
    }

    protected void alloc() {
        if (properties == null) {
            properties = new HashMap<>();
        }
    }
    
    protected void alloc_clocked() {
        if (clocked_properties == null) {
            clocked_properties = new HashMap<>();
        }
    }

    
    /**
     * @return Set of property names
     */
    @Override
    public Set<String> propertyNames() {
        if (properties == null) return Collections.unmodifiableSet(new HashSet<String>());
        return Collections.unmodifiableSet(properties.keySet());
    }
    
    /**
     * @return The whole map from property names to values
     */
    @Override
    public Map<String,Object> getProperties()  {
        if (properties == null) properties = new HashMap<>();
        return properties;
    }
    
    /**
     * @param name Name of property
     * @return Returns true if property name exists
     */
    @Override
    public boolean hasProperty(String name) {
        return (properties != null) && (properties.containsKey(name));
    }
    
    /**
     * Store given value into container under the given name.
     * 
     * @param name
     * @param val
     */
    @Override
    public void setProperty(String name, Object val) {
        alloc();
        properties.put(name, val);
    }

    /**
     * Store given value into container under the given name.
     * 
     * @param name
     * @param val
     */
    @Override
    public void setClockedProperty(String name, Object val) {
        alloc_clocked();
        clocked_properties.put(name, val);
    }
    
    
    @Override
    public void deleteProperty(String name) {
        if (properties == null) return;
        properties.remove(name);
    }
    
    @Override
    public void deleteClockedProperty(String name) {
        alloc_clocked();
        clocked_properties.put(name, DELETE);
    }

    
    /**
     * Fetches the specified property by name, returning it as an Integer.
     * Throws exception on type mismatch.
     * 
     * @param name
     * @return value
     */
    @Override
    public Integer getPropertyInteger(String name) {
        if (properties == null) return 0;
        
        Object p = properties.get(name);
        if (p == null) return 0;
        
        if (p instanceof Integer) {
            return (Integer)p;
        } else {
            throw new java.lang.ClassCastException("Property " + name + 
                    " cannot be converted from " +
                    p.getClass().getName() + " to Integer.");
        }
    }

    /**
     * Fetches the specified property by name, returning it as an int array.
     * Throws exception on type mismatch.
     * 
     * @param name
     * @return value
     */
    @Override
    public int[] getPropertyIntArray(String name) {
        if (properties == null) return new int[0];
        
        Object p = properties.get(name);
        if (p == null) return new int[0];
        
        if (p instanceof int[]) {
            return (int[])p;
        } else {
            throw new java.lang.ClassCastException("Property " + name + 
                    " cannot be converted from " +
                    p.getClass().getName() + " to int[].");
        }
    }
    
    /**
     * Fetches the specified property by name, returning it as a boolean
     * array.
     * Throws exception on type mismatch.
     * 
     * @param name
     * @return value
     */
    @Override
    public boolean[] getPropertyBooleanArray(String name) {
        if (properties == null) return new boolean[0];
        
        Object p = properties.get(name);
        if (p == null) return new boolean[0];
        
        if (p instanceof boolean[]) {
            return (boolean[])p;
        } else {
            throw new java.lang.ClassCastException("Property " + name + 
                    " cannot be converted from " +
                    p.getClass().getName() + " to boolean[].");
        }
    }
    
    /**
     * Fetches the specified property by name, returning it as a Boolean.
     * Throws exception on type mismatch.
     * 
     * @param name
     * @return value
     */
    @Override
    public Boolean getPropertyBoolean(String name) {
        if (properties == null) return false;
        
        Object p = properties.get(name);
        if (p == null) return false;
        
        if (p instanceof Boolean) {
            return (Boolean)p;
        } else {
            throw new java.lang.ClassCastException("Property " + name + 
                    " cannot be converted from " +
                    p.getClass().getName() + " to Boolean.");
        }
    }

    /**
     * Fetches the specified property by name, returning it as a String.
     * Throws exception on type mismatch.
     * 
     * @param name
     * @return value
     */
    @Override
    public String getPropertyString(String name) {
        if (properties == null) return "";
        
        Object p = properties.get(name);
        if (p == null) return "";
        
        if (p instanceof String) {
            return (String)p;
        } else {
            throw new java.lang.ClassCastException("Property " + name + 
                    " cannot be converted from " +
                    p.getClass().getName() + " to String.");
        }
    }    
    
    /**
     * Fetches the specified property by name, returning it as an Object.
     * 
     * @param name
     * @return value
     */
    @Override
    public Object getPropertyObject(Object name) {
        if (properties == null) return null;
        
        Object p = properties.get(name);
        return p;
    }
    
    /**
     * Copies properties from another PropertiesContainer, limited to the
     * properties whose names are specified in propertiesToCopy.
     * 
     * @param source
     * @param propertiesToCopy
     */
    @Override
    public void copyPropertiesFrom(IProperties source, Set<String> propertiesToCopy) {
        Map<String,Object> srcProps = source.getProperties();
        if (srcProps == null) return;
        if (properties == null) properties = new HashMap<>();
        
        if (propertiesToCopy == null) {
            propertiesToCopy = srcProps.keySet();
        }
        
        for (String name : propertiesToCopy) {
            if (srcProps.containsKey(name)) {
                properties.put(name, srcProps.get(name));
            }
        }
    }
    
    /**
     * Copies all properties from another PropertiesContainer.
     * 
     * @param source
     */
    @Override
    public void copyAllPropertiesFrom(IProperties source) {
        copyPropertiesFrom(source, null);
    }

    @Override
    public int numProperties() {
        if (properties == null) return 0;
        return properties.size();
    }

    @Override
    public void advanceClock() {
        if (clocked_properties == null) return;
        alloc();
        for (Map.Entry<String,Object> ent : clocked_properties.entrySet()) {
            Object value = ent.getValue();
            if (CpuSimulator.printPropertyUpdates) {
                if (value == null) {
                    Logger.out.println("# " + ent.getKey() + " <- null");
                } else if (value == DELETE) {
                    Logger.out.println("# " + ent.getKey() + " deleted");
                } else {
                    Logger.out.println("# " + ent.getKey() + " <- " + ent.getValue());
                }
            }
            if (value == DELETE) {
                properties.remove(ent.getKey());
            } else {
                properties.put(ent.getKey(), value);
            }
        }
        clocked_properties.clear();
    }
}
