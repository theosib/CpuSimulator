/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package baseclasses;

import utilitytypes.IComponent;
import utilitytypes.ICpuCore;
import utilitytypes.IModule;

/**
 *
 * @author millerti
 */
public abstract class ComponentBase implements IComponent {
    private final String name;
    private final IModule parent;
    
    /**
     * @return Name of this module
     */
    public String getLocalName() { return name; }
    public String getName() { return getLocalName(); }
    
    /**
     * @return Reference to parent in the hierarchy
     */
    public IModule getParent() { return parent; }
    
    
    /**
     * @return Name of this module in hierarchy of modules
     */
    public String getHierarchicalName() {
        IModule parent = getParent();
        String localname = getLocalName();
        if (parent == null) {
            return localname;
        } else {
            String parentname = null;
            if (parent.getParent() != null) {
                parentname = parent.getHierarchicalName();
            }
            if (parentname!=null && localname!=null) {
                return parentname + '.' + localname;
            } else if (localname != null) {
                return localname;
            } else if (parentname != null) {
                throw new RuntimeException("Named module " + parentname + 
                        " has unnamed child");
            } else {
                throw new RuntimeException("Unnamed module has unnamed child");
            }
        }
    }
    
    
    /**
     * @return The top-level module, which should be an instance of ICpuCore
     */
    private ICpuCore mycore = null;
    public ICpuCore getCore() {
        if (mycore == null) {
            if (this instanceof ICpuCore) {
                mycore = (ICpuCore)this;
            } else {
                mycore = getParent().getCore();
            }
        }
        return mycore;
    }
    
    
    public int getCycleNumber() {
        return getCore().getCycleNumber();
    }
    
    /**
     * Restore this pipeline register to initial conditions.
     */
    public abstract void reset();

    private int depth = -1;
    public int getDepth() {
        if (depth < 0) {
            IComponent p = getParent();
            if (p == null) {
                depth = 0;
            } else {
                depth = p.getDepth() + 1;
            }
        }
        return depth;
    }
    
    public ComponentBase(IModule parent, String name) {
        this.parent = parent;
        this.name = name;
    }    
}
