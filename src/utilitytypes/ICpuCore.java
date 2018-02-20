/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilitytypes;

/**
 * Basic interface implemented by every CPU core.
 * 
 * @author millerti
 */
public interface ICpuCore<GlobalType> {
    public void advanceClock();
    public void reset();
    GlobalType getGlobalResources();
}
