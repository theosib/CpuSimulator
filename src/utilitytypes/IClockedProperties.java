/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilitytypes;

/**
 *
 * @author millerti
 */
public interface IClockedProperties extends IProperties, IClocked {
    
    public void setClockedProperty(String name, Object val);
    public void deleteClockedProperty(String name);
    
}
