/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilitytypes;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author millerti
 */
public interface IClocked {
    public static final List<IClocked> all_clocked_objects = new ArrayList<IClocked>();
    
    public static void advanceClockAll() {
        for (IClocked ic : all_clocked_objects) {
            ic.advanceClock();
        }
    }
    
    public static void addClocked(IClocked self) {
        all_clocked_objects.add(self);
    }
    
    public void initClocking();
    public void advanceClock();
}
