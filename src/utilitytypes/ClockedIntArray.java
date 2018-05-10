/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilitytypes;

import java.util.ArrayList;
import static utilitytypes.IClocked.addClocked;

/**
 *
 * @author millerti
 */
public class ClockedIntArray implements IClocked {
    private int[] internal_array;
    private Integer[] updates;
    
    public int get(int ix) { return internal_array[ix]; }
    public void set(int ix, int val) { updates[ix] = val; }
        
    public ClockedIntArray(int size) {
        internal_array = new int[size];
        updates = new Integer[size];
        initClocking();
    }
    
    @Override
    public void initClocking() {
        addClocked(this);
    }

    @Override
    public void advanceClock() {
        for (int i=0; i<updates.length; i++) {
            Integer x = updates[i];
            if (x == null) continue;
            internal_array[i] = x;
            updates[i] = null;
        }
    }
    
}
