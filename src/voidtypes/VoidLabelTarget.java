/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voidtypes;

import utilitytypes.LabelTarget;

/**
 *
 * @author millerti
 */
public class VoidLabelTarget extends LabelTarget {
    private static final VoidLabelTarget singleton = new VoidLabelTarget();
    public static VoidLabelTarget getVoidLabelTarget() { return singleton; }
    
    @Override
    public String getName() { return ""; }
    @Override
    public int getAddress() { return -1; }
    @Override
    public void setName(String name) { }
    @Override
    public void setAddress(int addr) { }

    @Override
    public boolean isNull() { return true; }
    
    @Override
    public LabelTarget duplicate() { return this; }
}
