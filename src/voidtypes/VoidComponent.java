/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voidtypes;

import baseclasses.ComponentBase;
import utilitytypes.IComponent;
import utilitytypes.ICpuCore;
import utilitytypes.IModule;

/**
 *
 * @author millerti
 */
public class VoidComponent implements IComponent {
    private static final VoidComponent singleton = new VoidComponent();    
    public static VoidComponent getVoidComponent() {
        return singleton;
    }
    protected VoidComponent() {}

    @Override
    public String getLocalName() { return getClass().getSimpleName(); }

    @Override
    public IModule getParent() { return VoidModule.getVoidModule(); }

    @Override
    public String getHierarchicalName() { return getClass().getSimpleName(); }

    @Override
    public ICpuCore getCore() {
        return VoidCore.getVoidCore();
    }

    @Override
    public int getCycleNumber() { return 0; }
    
    @Override
    public void reset() {}

    @Override
    public int getDepth() { return 0; }

    @Override
    public String getShortName() {
        return ComponentBase.computeOnlyCaps(getLocalName(), "");
    }
}
