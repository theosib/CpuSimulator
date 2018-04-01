/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package baseclasses;

import java.util.HashMap;
import java.util.Map;
import utilitytypes.IFunctionalUnit;
import utilitytypes.IModule;
import utilitytypes.IPipeReg;
import utilitytypes.IPipeStage;

/**
 *
 * @author millerti
 */
public abstract class FunctionalUnitBase extends ModuleBase implements IFunctionalUnit {
    
    public FunctionalUnitBase(IModule parent, String name) {
        super(parent, name);
    }

    protected Map<String, IPipeStage> external_input_pipe_stages;
    
    @Override
    public Map<String, IPipeStage> getExternalInputPipeStages() {
        if (external_input_pipe_stages == null) {
            external_input_pipe_stages = new HashMap<>();
        }
        return external_input_pipe_stages;
    }
    
    protected Map<String, IPipeReg> external_output_pipe_regs;

    @Override
    public Map<String, IPipeReg> getExternalOutputPipeRegs() {
        if (external_output_pipe_regs == null) {
            external_output_pipe_regs = new HashMap<>();
        }
        return external_output_pipe_regs;
    }
    
    
    @Override
    public IPipeStage getInputPipeStage(String name) {
        int first_dot = name.indexOf('.');
        
        if (first_dot > 0) {
            String child_name = name.substring(0, first_dot);
            String inner_name = name.substring(first_dot+1);
            IFunctionalUnit child = getChildUnit(child_name);
            return child.getInputPipeStage(inner_name);
        }
        
        if (!name.equals("in") && !name.startsWith("in:")) {
            throw new RuntimeException(
                    "Functional unit input register names must be 'in' or start with 'in:'");
        }
        return getLocalPipeStage(name);
    }
    
    @Override
    public void specifyExternalInputStage(String name) {
        Map<String,IPipeStage> extins = getExternalInputPipeStages();
        IPipeStage input = getInputPipeStage(name);
        extins.put(name, input);
    }

    @Override
    public Map<String,IPipeStage> getInputPipeStages() {
        Map<String,IPipeStage> stages = new HashMap<>();
        
        for (Map.Entry<String,IPipeStage> entry : getLocalPipeStages().entrySet()) {
            String name = entry.getKey();
            if (name.equals("in") || name.startsWith("in:")) {
                stages.put(name, entry.getValue());
            }
        }
        
        Map<String,IFunctionalUnit> children = getLocalChildUnits();
        for (Map.Entry<String,IFunctionalUnit> unit : children.entrySet()) {
            IFunctionalUnit child = unit.getValue();
            Map<String,IPipeStage> child_externals = child.getExternalInputPipeStages();
            if (child_externals == null) continue;
            
            String prefix = unit.getKey() + '.';
            for (Map.Entry<String,IPipeStage> entry : child_externals.entrySet()) {
                String name = prefix + entry.getKey();
                stages.put(name, entry.getValue());
            }
        }
        
        return stages;
    }
    
    @Override
    public IPipeReg getOutputPipeReg(String name) {
        int first_dot = name.indexOf('.');
        
        if (first_dot > 0) {
            String child_name = name.substring(0, first_dot);
            String inner_name = name.substring(first_dot+1);
            IFunctionalUnit child = getChildUnit(child_name);
            return child.getOutputPipeReg(inner_name);
        }

        if (!name.equals("out") && !name.startsWith("out:")) {
            throw new RuntimeException(
                    "Functional unit output register names must be 'out' or start with 'out:'");
        }
        return getLocalPipeReg(name);
    }

    @Override
    public void specifyExternalOutputReg(String name) {
        Map<String,IPipeReg> extouts = getExternalOutputPipeRegs();
        IPipeReg output = getOutputPipeReg(name);
        extouts.put(name, output);
    }    

    @Override
    public Map<String,IPipeReg> getOutputPipeRegs() {
        Map<String,IPipeReg> regs = new HashMap<>();
        
        for (Map.Entry<String,IPipeReg> entry : getLocalPipeRegs().entrySet()) {
            String name = entry.getKey();
            if (name.equals("out") || name.startsWith("out:")) {
                regs.put(name, entry.getValue());
            }
        }

        Map<String,IFunctionalUnit> children = getLocalChildUnits();
        for (Map.Entry<String,IFunctionalUnit> unit : children.entrySet()) {
            IFunctionalUnit child = unit.getValue();
            Map<String,IPipeReg> child_externals = child.getExternalOutputPipeRegs();
            if (child_externals == null) continue;
            
            String prefix = unit.getKey() + '.';
            for (Map.Entry<String,IPipeReg> entry : child_externals.entrySet()) {
                String name = prefix + entry.getKey();
                regs.put(name, entry.getValue());
            }
        }
        
        return regs;
    }
    
}
