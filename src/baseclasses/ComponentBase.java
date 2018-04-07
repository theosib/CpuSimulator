/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package baseclasses;

import utilitytypes.IComponent;
import utilitytypes.ICpuCore;
import utilitytypes.IFunctionalUnit;
import utilitytypes.IModule;
import utilitytypes.IPipeReg;
import utilitytypes.IPipeStage;

/**
 * See utilitytypes/IComponent for documentation
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
    
    public static String computeOnlyCaps(String str, String prefix) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) sb.append(c);
        }
        String abbr = sb.toString();
        if (abbr.length() == 0) {
            if (prefix.length() > 0) {
                return prefix + "_" + str;
            } else {
                return str;
            }
        }
        return prefix + abbr;
    }
    
    public String getShortName() {
        String name = getLocalName();
        if (this instanceof IPipeReg) {
            int to = name.indexOf("To");
            if (to>=0 && (to+2)<name.length() && Character.isUpperCase(name.charAt(to+2))) {
                String a = name.substring(0, to);
                String b = name.substring(to+2);
                return computeOnlyCaps(a, "r") + "2" + computeOnlyCaps(b, "");
            } else {
                return computeOnlyCaps(name, "r");
            }
        } else if (this instanceof IPipeStage) {
            return computeOnlyCaps(name, "s");
        } else if (this instanceof CpuCore) {
            return computeOnlyCaps(name, "");
        } else if (this instanceof IFunctionalUnit) {
            return computeOnlyCaps(name, "u");
        } else if (this instanceof IModule) {
            return computeOnlyCaps(name, "m");
        } else {
            return computeOnlyCaps(name, "");
        }
    }
    
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
