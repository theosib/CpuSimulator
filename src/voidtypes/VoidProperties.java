/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voidtypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import utilitytypes.IProperties;

/**
 *
 * @author millerti
 */
public class VoidProperties implements IProperties {
    private static final Map<String,Object> properties = Collections.unmodifiableMap(new HashMap<String,Object>());
    private static final VoidProperties singleton = new VoidProperties();
    public static VoidProperties getVoidProperties() { return singleton; }
    protected VoidProperties() {}

    @Override
    public Set<String> propertyNames() {
        return VoidRegister.proplist;
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public int numProperties() {
        return 0;
    }

    @Override
    public boolean hasProperty(String name) {
        return false;
    }

    @Override
    public void setProperty(String name, Object val) {}

    @Override
    public void deleteProperty(String name) {}

    @Override
    public Integer getPropertyInteger(String name) {
        return 0;
    }

    @Override
    public int[] getPropertyIntArray(String name) {
        return new int[0];
    }

    @Override
    public Boolean getPropertyBoolean(String name) {
        return false;
    }

    @Override
    public boolean[] getPropertyBooleanArray(String name) {
        return new boolean[0];
    }

    @Override
    public String getPropertyString(String name) {
        return "";
    }

    @Override
    public Object getPropertyObject(Object name) {
        return new Object();
    }

    @Override
    public void clear() {}

    @Override
    public void copyPropertiesFrom(IProperties source, Set<String> propertiesToCopy) {}

    @Override
    public void copyAllPropertiesFrom(IProperties source) {}

    
    public static final List<String> strlist = Collections.unmodifiableList(new ArrayList<String>());
    
    @Override
    public List<String> toStringList() { return strlist; }

    @Override
    public void setClockedProperty(String name, Object val) {}

    @Override
    public void advanceClock() {}

    @Override
    public void deleteClockedProperty(String name) {}
    
}
