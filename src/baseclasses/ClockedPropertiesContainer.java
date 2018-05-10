/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package baseclasses;

import cpusimulator.CpuSimulator;
import java.util.HashMap;
import java.util.Map;
import utilitytypes.IClocked;
import static utilitytypes.IClocked.addClocked;
import utilitytypes.IClockedProperties;
import static utilitytypes.IProperties.DELETE;
import utilitytypes.Logger;

/**
 *
 * @author millerti
 */
public class ClockedPropertiesContainer extends PropertiesContainer implements IClockedProperties {
    protected Map<String,Object> clocked_properties;

    
    /**
     * Wipe out all properties
     */
    @Override
    public void clear() {
        super.clear();
        if (clocked_properties != null) clocked_properties.clear();
    }
    
    
    public ClockedPropertiesContainer() {
        initClocking();
    }
    
    @Override
    public void initClocking() {
        addClocked(this);
    }
    
    
    protected void alloc_clocked() {
        if (clocked_properties == null) {
            clocked_properties = new HashMap<>();
        }
    }

    
    
    /**
     * Post a property change to not take effect until advanceClock is called
     * on the properties container.
     * 
     * @param name
     * @param val
     */
    @Override
    public void setClockedProperty(String name, Object val) {
        alloc_clocked();
        clocked_properties.put(name, val);
    }
    
    
    
    /**
     * Queue a property to be deleted when advanceClock is called.  A better
     * name for this might be "deletePropertyClocked".
     * 
     * @param name 
     */
    @Override
    public void deleteClockedProperty(String name) {
        alloc_clocked();
        clocked_properties.put(name, DELETE);
    }

    
    /**
     * Apply all queued property changes.
     */
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
