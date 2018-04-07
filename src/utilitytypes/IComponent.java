/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilitytypes;

/**
 * Any kind of element (module, pipereg, pipestage) that can have a name and
 * a parent.
 * 
 * @author millerti
 */
public interface IComponent {
    /**
     * @return Name of this module
     */
    String getLocalName();
    
    /**
     * @return a highly abbreviated version of the component name
     */
    String getShortName();
    
    /**
     * @return Reference to parent in the hierarchy
     */
    IModule getParent();
    
    default String getName() {
        return getLocalName();
    }
    
    /**
     * @return Name of this module in hierarchy of modules
     */
    String getHierarchicalName();
    
    
    /**
     * @return The top-level module, which should be an instance of ICpuCore
     */
    ICpuCore getCore();
    
    /**
     * @return The current clock cycle number from the core
     */
    int getCycleNumber();
    
    /**
     * Restore this pipeline register to initial conditions.
     */
    public void reset();

    /**
     * @return How far down is this component in the module hierarchy?
     */
    public int getDepth();
}
